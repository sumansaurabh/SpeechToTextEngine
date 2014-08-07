/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.apache.stanbol.enhancer.engines.speechtotext;


import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import static org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper.randomUUID;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_SELECTED_TEXT;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.commons.io.IOUtils;
import org.apache.stanbol.commons.sphinx.ModelProviderImpl;
import org.apache.stanbol.commons.stanboltools.datafileprovider.DataFileProvider;
import org.apache.stanbol.enhancer.servicesapi.Blob;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.ContentSink;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.InvalidContentException;
import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.AbstractEnhancementEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * EnhancementEngine based on Sphinx that converts the content of parsed 
 * {@link ContentItem} to plain text.  Enhancement Results keep track of 
 * the temporal position of the extracted text within the processed media file.
 * 
 * @author Suman Saurabh
 *
 */

public abstract class SpeechToTextEngine 
	extends AbstractEnhancementEngine<IOException,RuntimeException> 
	implements EnhancementEngine {

    /**
     * Using slf4j for logging
     */
    private static final Logger log = LoggerFactory.getLogger(SpeechToTextEngine.class);

	
    protected static final Charset UTF8 = Charset.forName("UTF-8");
    
    /**
     * Start Time at which selected text was spoken.
     */
    public static final UriRef ENHANCER_TIME_START = new UriRef("http://www.w3.org/TR/prov-o/#startedAtTime");
    /**
     * End Time at which selected text was spoken.
     */
    public static final UriRef ENHANCER_TIME_END = new UriRef("http://www.w3.org/TR/prov-o/#endedAtTime");

    protected SphinxConfig config;
    protected ModelProviderImpl MPi;

    
    
    /**
     * The {@link ContentItemFactory} is used to create {@link Blob}s for the
     * plain text and XHTML version of the processed ContentItem
     */
    protected ContentItemFactory ciFactory;
    
    /**
     * If used sub classes MUST ensure that {@link #MPi} and {@link #config}
     * are set before calling {@link #canEnhance(ContentItem)} or
     * {@link #computeEnhancements(ContentItem)}
     */
    
    protected SpeechToTextEngine() {}
    
    public SpeechToTextEngine(ModelProviderImpl MPi, SphinxConfig config) {
    	if(MPi == null){
            throw new IllegalArgumentException("The parsed ModelProvider instance MUST NOT be NULL!");
        }
        if(config == null){
            throw new IllegalArgumentException("The parsed Sphinx engine configuration MUST NOT be NULL!");
        }
        this.MPi = MPi;
        this.config = config;
    }
    
    SpeechToTextEngine(DataFileProvider dfp,SphinxConfig config) throws IOException {
        this(new ModelProviderImpl(dfp),config);
    }
   
   

 
    /**
     * @return if and how (asynchronously) we can enhance a ContentItem
     */
    @Override
    public int canEnhance(ContentItem ci) throws EngineException {
        // check if content is present
        try {
            if ((ci.getBlob() == null)|| 
                    (ci.getBlob().getStream().read() == -1)||
                    (ci.getMimeType().compareToIgnoreCase("audio/wav"))!=0){
                return CANNOT_ENHANCE;
            }
        } catch (IOException e) {
            log.error("Failed to get the text for "
                    + "enhancement of content: " + ci.getUri(), e);
            throw new InvalidContentException(this, ci, e);
        }
        // no reason why we should require to be executed synchronously
        return ENHANCE_ASYNC;
    }
    
    
    @SuppressWarnings("deprecation")
    @Override
    public void computeEnhancements(ContentItem ci) throws EngineException {
        List<ArrayList<String>> resultPredicted=new ArrayList<ArrayList<String>>();
        StringBuilder recogString=new StringBuilder();
        /*
        recogString.append("Stanbol Can detetct famous cities like Paris and famous people like Bob Marley. ");
        ArrayList<String> sentence=new ArrayList<String>();
        sentence.add(timeStampCalculator(777));
        sentence.add(timeStampCalculator(5678));
        sentence.add("Stanbol Can detetct famous cities like Paris and famous people like Bob Marley. ");
        resultPredicted.add(sentence);
        sentence.clear();
        sentence.add(timeStampCalculator(5942));
        sentence.add(timeStampCalculator(7632));
        sentence.add("This is Speech to text Enhancement Engine. ");
        resultPredicted.add(sentence);
        */
        final InputStream in;
        String lang=extractLanguage(ci);
        if(lang!=null) {
            config.setDefaultLanguage(lang);
        }
        config.initConfig(MPi);

        
        
        
        
        Configuration configuration = config.getConfiguration();        
        try {
            in = ci.getBlob().getStream();
            //Extracting Text from Media File parsed
            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            
            recognizer.startRecognition(in);
            SpeechResult result;
            resultPredicted=new ArrayList<ArrayList<String>>();//for time-stamp mapping
            
           //Recognizing the Media Data and storing each line in @resultPredicted
            while ((result = recognizer.getResult()) != null) {
                List<WordResult> wordlist=result.getWords();
                ArrayList<String>sentencePredicted=new ArrayList<String>();
                sentencePredicted.add(timeStampCalculator(wordlist.get(0).getTimeFrame().getStart()));
                sentencePredicted.add(timeStampCalculator(wordlist.get(wordlist.size()-2).getTimeFrame().getEnd()));
                log.info(result.getHypothesis());
                sentencePredicted.add(result.getHypothesis());
                resultPredicted.add(sentencePredicted);
            	recogString.append(result.getHypothesis()).append("\n");
            }
            recognizer.stopRecognition();
        } catch (IOException ex) {
            log.error("Exception reading content item.", ex);
            throw new InvalidContentException("Exception reading content item.", ex);
        }
        //Speech Recognized now add the Blob to the ContentItem
        
        
        ContentSink plainTextSink;
        try {
            plainTextSink = ciFactory.createContentSink("text/plain" +"; charset="+UTF8);
        } catch (IOException e) {
            IOUtils.closeQuietly(in); //close the input stream
            throw new EngineException("Error while initialising Blob for" +
                		"writing the text/plain version of the parsed content",e);
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(plainTextSink.getOutputStream(), UTF8));
        try { // parse the writer to the framework that extracts the text 
            	out.write(recogString.toString());
        } catch (IOException e) {
        	throw new EngineException("Unable to write extracted" +
                		"plain text to Blob (blob impl: "
                        + plainTextSink.getBlob().getClass()+")",e);
        }
        finally
        { IOUtils.closeQuietly(out); }
            
        String random = randomUUID().toString();
        UriRef textBlobUri = new UriRef("urn:Sphinx:text:"+random);//create an UriRef for the Blob
        ci.addPart(textBlobUri, plainTextSink.getBlob());            
        plainTextSink=null;
        
        ci.getLock().writeLock().lock();
        try {	 
            MGraph metadata = ci.getMetadata();
            LiteralFactory lf = LiteralFactory.getInstance();//UriRef timestampAnnotation = EnhancementEngineHelper.createTextEnhancement(ci, this);
            
            for (ArrayList<String> entry : resultPredicted) {
                random = randomUUID().toString();
                UriRef timestampAnnotation = new UriRef("urn:Sphinx:text:"+random);
		metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_START,lf.createTypedLiteral(entry.get(0))));//Start time of the spoken text
                metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_END,lf.createTypedLiteral(entry.get(1))));// End time of the spoken text
		metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_SELECTED_TEXT,lf.createTypedLiteral(entry.get(2))));// Spoken text at the particular time frame                                
            }
            /*
            random = randomUUID().toString();
                UriRef timestampAnnotation = new UriRef("urn:Sphinx:text:"+random);
		metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_START,lf.createTypedLiteral(timeStampCalculator(777))));//Start time of the spoken text
                metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_END,lf.createTypedLiteral(timeStampCalculator(4000))));// End time of the spoken text
		metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_SELECTED_TEXT,lf.createTypedLiteral("This is Speech to text")));// Spoken text at the particular time frame                                
                
                random = randomUUID().toString();
                timestampAnnotation = new UriRef("urn:Sphinx:text:"+random);
		metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_START,lf.createTypedLiteral(timeStampCalculator(4567))));//Start time of the spoken text
                metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_END,lf.createTypedLiteral(timeStampCalculator(8792))));// End time of the spoken text
		metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_SELECTED_TEXT,lf.createTypedLiteral("I am perfectus")));// Spoken text at the particular time frame                                
            */
        }finally{
            ci.getLock().writeLock().unlock();
        }    
    }
    	
    private String timeStampCalculator(long timeStamp) {
        long millis=timeStamp%1000;
   	long second = (timeStamp / 1000) % 60;
        long minute = (timeStamp / (1000 * 60)) % 60;
   	long hour = (timeStamp / (1000 * 60 * 60)) % 24;
        String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
    	return time;
    }
    /**
     * Extracts the language of the parsed ContentItem by using
     * {@link EnhancementEngineHelper#getLanguage(ContentItem)} and 
     * {@link #defaultLang} as default
     * @param ci the content item
     * @return the language
     */
    private String extractLanguage(ContentItem ci) {
        String lang = EnhancementEngineHelper.getLanguage(ci);
        
        if(lang != null){
            return lang;
        } else {
            log.info("Unable to extract language for ContentItem %s!",ci.getUri().getUnicodeString());
            log.info(" ... returned '{}' as default",config.getDefaultLanguage());
            return null;
        }
    }
    
    
    
}
