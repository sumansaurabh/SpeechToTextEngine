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

import static java.util.Collections.singleton;
import static org.apache.stanbol.enhancer.servicesapi.EnhancementEngine.CANNOT_ENHANCE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map.Entry;

import org.apache.clerezza.rdf.core.UriRef;
//import org.apache.stanbol.commons.sphinx.ModelProviderImpl;
import org.apache.stanbol.enhancer.contentitem.inmemory.InMemoryContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.Blob;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.StreamSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Requires lot of memory for testing
 * 
 * @author perfectus
 *
 */
public class SpeechToTextEngineTest {

    private static final Logger log = LoggerFactory.getLogger(SpeechToTextEngineTest.class);
    private static final ContentItemFactory ciFactory = InMemoryContentItemFactory.getInstance();
    private static SpeechToTextEngine engine;
    private static MockComponentContext context;
    //private static ModelProviderImpl MP=new ModelProviderImpl(new ClasspathDataFileProvider("DUMMY"));

    @BeforeClass
    public static void setUpServices() throws IOException {
        context = new MockComponentContext();
        context.properties.put(SpeechToTextEngine.PROPERTY_NAME, "SpeechToText");
    }
	 
    @Before
    public void bindServices() throws ConfigurationException {
        
    }
    /*
    @Test
    public void testSphinx() throws IOException
    {
    	System.out.println("TEsting");
    	Configuration configuration = new Configuration();

        // Load model from the jar
        //configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/acoustic/wsj");
        configuration.setAcousticModelPath("/home/perfectus/Downloads/models_used/en-us");
        
        
        // You can also load model from folder
        // configuration.setAcousticModelPath("file:en-us");
        
        //configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/acoustic/wsj/dict/cmudict.0.6d");
        configuration.setDictionaryPath("/tmp/en-cmu.dict");
        //configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/language/en-us.lm.dmp");
        configuration.setLanguageModelPath("/tmp/en-us.lm.dmp");
        StreamSpeechRecognizer recognizer = 
            new StreamSpeechRecognizer(configuration);
        ContentItem ci = createContentItem("temp.wav", "audio/wav");

        //InputStream stream = RecognizeSpeech.class.getResourceAsStream("temp.wav");
        //InputStream stream = new URL("/tmp/tem").openStream();
        recognizer.startRecognition(ci.getStream());

        SpeechResult result;

        while ((result = recognizer.getResult()) != null) {
        
            System.out.format("Hypothesis: %s\n",
                              result.getHypothesis());
                              
            System.out.println("List of recognized words and their times:");
            for (WordResult r : result.getWords()) {
        	System.out.println(r);
            }

            System.out.println("Best 3 hypothesis:");            
            for (String s : result.getNbest(3))
                System.out.println(s);

            System.out.println("Lattice contains " + result.getLattice().getNodes().size() + " nodes");
        }

        recognizer.stopRecognition();
    
    }
    */
    @Test 
    public void testDefaultEnhancements() throws EngineException, IOException, ParseException {
    	
        //engine = new SpeechToTextEngine(ciFactory, MP);
        
        log.info(">>> Default Model Sphinix Testing WAV  <<<");
        ContentItem ci = createContentItem("temp.wav", "audio/wav1");
        assertFalse(engine.canEnhance(ci) == CANNOT_ENHANCE);
        //System.out.println("##################################################"+ci.getMetadata());
        System.out.println("##### Engine open ");
        engine.computeEnhancements(ci);
        System.out.println("##### Engine Close");
        Entry<UriRef,Blob> contentPart = ContentItemHelper.getBlob(ci, singleton("text/plain"));
        //	String text = ContentItemHelper.getText(contentPart.getValue());
        //System.out.println("##################################################"+ci.getMetadata());
        
        assertNotNull(contentPart);
        Blob plainTextBlob = contentPart.getValue();
        //log.info("Recongised String: {}",ContentItemHelper.getText(plainTextBlob));
        assertNotNull(plainTextBlob);        
    }
    
    
    
    @Test 
    public void testCustomEnhancements() throws EngineException, IOException, ParseException {
        //engine = new SpeechToTextEngine(ciFactory, MP);
        log.info(">>> Custom Model Sphinix Testing WAV  <<<");
        ContentItem ci = createContentItem("temp.wav", "audio/wav");
        assertFalse(engine.canEnhance(ci) == CANNOT_ENHANCE);
        //engine.config.setCustomLangModel("en-us.lm.dmp");
        //engine.config.setCustomDictModel("en-cmu.dict");
        String acousticResource[]={"feat.params", "mdef", "means", "mixture_weights", "noisedict", "transition_matrices", "variances","feature_transform"};
		for(String resourceName: acousticResource) {
			//engine.config.setCustomAcousticModel(resourceName);
		}
	//	SphinxConfig.CUSTOM_MODEL_AVAILABLE=true;
        //System.out.println("##################################################"+ci.getMetadata());
        engine.computeEnhancements(ci);
        Entry<UriRef,Blob> contentPart = ContentItemHelper.getBlob(ci, singleton("text/plain"));
        //	String text = ContentItemHelper.getText(contentPart.getValue());
        //System.out.println("##################################################"+ci.getMetadata());
        assertNotNull(contentPart);
        Blob plainTextBlob = contentPart.getValue();
        assertNotNull(plainTextBlob);        
    }
    
    private ContentItem createContentItem(String resourceName, String contentType) throws IOException {
        InputStream in = SpeechToTextEngineTest.class.getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(in);
        return ciFactory.createContentItem(new StreamSource(in,contentType));
    }
    @After
    public void unbindServices() { 

    }
    @AfterClass
    public static void shutdownServices() {
    	log.info("\n>>Cleaning Temporary Resource<<");
        engine = null;
    }	    
}
