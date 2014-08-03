package org.apache.stanbol.enhancer.engines.speechtotext;

/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
import org.apache.felix.scr.annotations.Reference;
import org.apache.stanbol.commons.sphinx.impl.ModelProviderImpl;
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

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

/**
 * EnhancementEngine based on Sphinx that converts the content of parsed 
 * content items to plain text. 
 * 
 * @author Suman Saurabh
 *
 */




public class SpeechToTextEngine 
	extends AbstractEnhancementEngine<IOException,RuntimeException> 
	implements EnhancementEngine {

	/**
     * Using slf4j for logging
     */
    private static final Logger log = LoggerFactory.getLogger(SpeechToTextEngine.class);

	
    protected static final Charset UTF8 = Charset.forName("UTF-8");
    
    
    
    /**
     * the Time at which selected text was spoken.
     */
    public static final UriRef ENHANCER_TIME_START = new UriRef("http://www.w3.org/TR/prov-o/#startedAtTime");
    public static final UriRef ENHANCER_TIME_END = new UriRef("http://www.w3.org/TR/prov-o/#endedAtTime");

    protected SphinxConfig config;//=new SphinxConfig();

    
    
    /**
     * The {@link ContentItemFactory} is used to create {@link Blob}s for the
     * plain text and XHTML version of the processed ContentItem
     */
    @Reference
    private ContentItemFactory ciFactory;
    
    public SpeechToTextEngine() {}
    
    /**
     * Used by the unit tests to init the {@link ContentItemFactory} outside
     * an OSGI environment.
     * @param cifactory
     */
    
    public SpeechToTextEngine(ContentItemFactory cifactory, ModelProviderImpl MP) {
    	this.ciFactory = cifactory;
    	config=new SphinxConfig(MP);
	}

    /**
     * @return if and how (asynchronously) we can enhance a ContentItem
     */
    @Override
    public int canEnhance(ContentItem ci) throws EngineException {
        // check if content is present
        try {
            if ((ci.getBlob() == null)
                    || (ci.getBlob().getStream().read() == -1)) {
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
        config.initConfig(ci);
        List<ArrayList<String>> resultPredicted;
        StringBuffer recogString=new StringBuffer();

        final InputStream in;
        Configuration configuration = config.getConfiguration();
        try {
            in = ci.getStream();
            //Extracting Text from Media File parsed
            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            recognizer.startRecognition(in);
            SpeechResult result;
            resultPredicted=new ArrayList<ArrayList<String>>();//for time-stamp mapping
            
           //Recognising the Media Data and storing each line in @resultPredicted
            while ((result = recognizer.getResult()) != null)
            {
                List<WordResult> wordlist=result.getWords();
                ArrayList<String>sentencePredicted=new ArrayList<String>();
                sentencePredicted.add(timeStampCalculator(wordlist.get(0).getTimeFrame().getStart()));
                sentencePredicted.add(timeStampCalculator(wordlist.get(wordlist.size()-2).getTimeFrame().getEnd()));
                
                sentencePredicted.add(result.getHypothesis());
                resultPredicted.add(sentencePredicted);
            	recogString.append(result.getHypothesis()+"\n");
                //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^ "+result.getHypothesis());
            }
            recognizer.stopRecognition();
        } catch (IOException ex) {
            log.error("Exception reading content item.", ex);
            throw new InvalidContentException("Exception reading content item.", ex);
        }
        //now add the Blob to the ContentItem
        ContentSink plainTextSink;
            try {
                plainTextSink = ciFactory.createContentSink("text/plain" +"; charset="+UTF8);

            } catch (IOException e) {
                IOUtils.closeQuietly(in); //close the input stream
                throw new EngineException("Error while initialising Blob for" +
                		"writing the text/plain version of the parsed content",e);
            }
            //final Writer plainTextWriter = new OutputStreamWriter(plainTextSink.getOutputStream(), UTF8);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(plainTextSink.getOutputStream(), UTF8));
            try
            { // parse the writer to the framework that extracts the text 
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
            try
            {
            	 
                MGraph metadata = ci.getMetadata();
            	LiteralFactory lf = LiteralFactory.getInstance();
    			UriRef timestampAnnotation = EnhancementEngineHelper.createTextEnhancement(ci, this);
    			
            	for (ArrayList<String> entry : resultPredicted) {
        			metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_START,lf.createTypedLiteral(entry.get(0))));//Start time of the spoken text
        			metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_TIME_END,lf.createTypedLiteral(entry.get(1))));// End time of the spoken text
        			metadata.add(new TripleImpl(timestampAnnotation, ENHANCER_SELECTED_TEXT,lf.createTypedLiteral(entry.get(2))));// Spoken text at the particular time frame
            	}
            }finally{
                ci.getLock().writeLock().unlock();
            }
            
    }
    	
    String timeStampCalculator(long timeStamp) {
 	   	long millis=timeStamp%1000;
 	   	long second = (timeStamp / 1000) % 60;
 	   	long minute = (timeStamp / (1000 * 60)) % 60;
 	   	long hour = (timeStamp / (1000 * 60 * 60)) % 24;
 	   	String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
    	return time;
    }
   
    
    
    
}
