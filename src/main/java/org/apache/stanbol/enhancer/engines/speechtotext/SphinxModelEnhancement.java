package org.apache.stanbol.enhancer.engines.speechtotext;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;


@Component(
	    metatype = true, 
	    immediate = true,
	    inherit = true,
	    configurationFactory = true, 
	    policy = ConfigurationPolicy.OPTIONAL,
	    specVersion = "1.1", 
	    label = "%stanbol.SphinxModelEnhancement.name", 
	    description = "%stanbol.SphinxModelEnhancement.description")
	@Service
	@org.apache.felix.scr.annotations.Properties(value={
	    @Property(name=EnhancementEngine.PROPERTY_NAME,value="sphinx"),
	    @Property(name=SphinxModelEnhancement.DEFAULT_LANGUAGE,value=""),
	    //set the ranking of the default config to a negative value (ConfigurationPolicy.OPTIONAL) 
	    @Property(name=Constants.SERVICE_RANKING,intValue=-100) 
	})
public class SphinxModelEnhancement 
	extends SpeechToTextEngine  
	implements EnhancementEngine, ServiceProperties{
	
	
	/**
     * Allows to define the default language assumed for parsed Content if no language
     * detection is available. If <code>null</code> or empty this engine will not
     * process content with an unknown language
     */
    public static final String DEFAULT_LANGUAGE = "stanbol.NamedEntityExtractionEnhancementEngine.defaultLanguage";
    
    protected void activate(ComponentContext ctx) throws IOException, ConfigurationException {
    	
        super.activate(ctx);
        
        config = new SphinxConfig();
        SphinxConfig.CUSTOM_MODEL_AVAILABLE=false;
        // Need to register the default data before loading the models
        Object value = ctx.getProperties().get(DEFAULT_LANGUAGE);
        if(value != null && !value.toString().isEmpty()){
            config.setDefaultLanguage(value.toString());
        } //else no default language
        
    }
    
    protected void deactivate(ComponentContext ctx) {
        config = null;
        super.deactivate(ctx);
    }
    /**
     * ServiceProperties are currently only used for automatic ordering of the 
     * execution of EnhancementEngines (e.g. by the WeightedChain implementation).
     * ORDERING_PRE_PROCESSING: All values >= 200 are considered for engines that
     * do some kind of preprocessing of the content. This includes e.g. the 
     * conversion of media formats such as extracting the plain text from HTML, 
     * keyframes from videos, wave form from mp3 ...; extracting metadata directly 
     * encoded within the parsed content such as ID3 tags from MP3 or RDFa, 
     * microdata provided by HTML content.
     * use a value < {@link ServiceProperties#ORDERING_PRE_PROCESSING}
     * and >= {@link ServiceProperties#ORDERING_PRE_PROCESSING}.
     */
    @Override
    public Map<String, Object> getServiceProperties() {
        return Collections.unmodifiableMap(Collections.singletonMap(
                ENHANCEMENT_ENGINE_ORDERING, (Object)ORDERING_PRE_PROCESSING));
    }

}
