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
import org.apache.stanbol.commons.sphinx.impl.ModelProviderImpl;
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

public class SpeechToTextEngineTest {

    private static final Logger log = LoggerFactory.getLogger(SpeechToTextEngineTest.class);
    private static final ContentItemFactory ciFactory = InMemoryContentItemFactory.getInstance();
    private static SpeechToTextEngine engine;
    private static MockComponentContext context;
    private static ModelProviderImpl MP=new ModelProviderImpl(new ClasspathDataFileProvider("DUMMY"));

    @BeforeClass
    public static void setUpServices() throws IOException {
        context = new MockComponentContext();
        context.properties.put(SpeechToTextEngine.PROPERTY_NAME, "SpeechToText");
    }
	 
    @Before
    public void bindServices() throws ConfigurationException {
        
    }
    @Test 
    public void testDefaultEnhancements() throws EngineException, IOException, ParseException {
    	
        engine = new SpeechToTextEngine(ciFactory, MP);
        
        log.info(">>> Default Model Sphinix Testing WAV  <<<");
        ContentItem ci = createContentItem("10001-90210-01803.wav", "audio/wav");
        assertFalse(engine.canEnhance(ci) == CANNOT_ENHANCE);
        //System.out.println("##################################################"+ci.getMetadata());
        engine.computeEnhancements(ci);
        Entry<UriRef,Blob> contentPart = ContentItemHelper.getBlob(ci, singleton("text/plain"));
        //	String text = ContentItemHelper.getText(contentPart.getValue());
        //System.out.println("##################################################"+ci.getMetadata());
        assertNotNull(contentPart);
        Blob plainTextBlob = contentPart.getValue();
        assertNotNull(plainTextBlob);        
    }
    @Test 
    public void testCustomEnhancements() throws EngineException, IOException, ParseException {
        engine = new SpeechToTextEngine(ciFactory, MP);
        log.info(">>> Custom Model Sphinix Testing WAV  <<<");
        ContentItem ci = createContentItem("10001-90210-01803.wav", "audio/wav");
        assertFalse(engine.canEnhance(ci) == CANNOT_ENHANCE);
        engine.config.setCustomLangModel("en-us.lm.dmp");
        engine.config.setCustomDictModel("en-cmu.dict");
        String acousticResource[]={"feat.params", "mdef", "means", "mixture_weights", "noisedict", "transition_matrices", "variances"};
		for(String resourceName: acousticResource) {
			engine.config.setCustomAcousticModel(resourceName);
		}
		SphinxConfig.CUSTOM_MODEL_AVAILABLE=true;
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
