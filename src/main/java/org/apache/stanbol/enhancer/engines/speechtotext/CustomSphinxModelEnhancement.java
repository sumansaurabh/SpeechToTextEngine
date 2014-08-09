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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.commons.sphinx.AcousticModel;
import org.apache.stanbol.commons.sphinx.DictionaryModel;
import org.apache.stanbol.commons.sphinx.LanguageModel;
import org.apache.stanbol.commons.sphinx.ModelProvider;


import org.apache.stanbol.commons.stanboltools.datafileprovider.DataFileListener;
import org.apache.stanbol.commons.stanboltools.datafileprovider.DataFileTracker;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This Engine is used to load custom model files for Speech to Text Enhancement
 * 
 * @author Suman Saurabh
 *
 */
@Component(immediate = true,
	metatype = true, 
	inherit = true,
	configurationFactory = true, 
	policy = ConfigurationPolicy.REQUIRE,
	specVersion = "1.1",
	label = "%stanbol.CustomSphinxModelEnhancement.name", 
	description = "%stanbol.CustomSphinxModelEnhancement.description"
)
@Service
@Properties(value = {//sample configuration that is made available in this engine
    @Property(name = EnhancementEngine.PROPERTY_NAME, value = "sphinx-custom"),
    @Property(name=CustomSphinxModelEnhancement.DICTIONARY_MODEL, cardinality=Integer.MAX_VALUE,
        value={"en-cmu.dict"}),//Same as default language model used in this engine
    @Property(name=CustomSphinxModelEnhancement.LANGUAGE_MODEL, cardinality=Integer.MAX_VALUE,
        value={"en-us.lm.dmp"}),// Same as default language model used in this engine
    @Property(name=CustomSphinxModelEnhancement.ACOUSTIC_MODEL_PROVIDER_BUNDLE_NAME, cardinality=Integer.MAX_VALUE,
        value={"org.apache.stanbol.data.sphinx.model.acoustic"}),//since all model types of acoustic model configuration files have same name
                                                                //inorder to distinguish between them bundle name should be made available
    @Property(name=Constants.SERVICE_RANKING,intValue=-100)
})
@Reference(name="ModelProvider",referenceInterface=ModelProvider.class, 
    cardinality=ReferenceCardinality.MANDATORY_UNARY,
    policy=ReferencePolicy.STATIC)

public class CustomSphinxModelEnhancement 
	extends SpeechToTextEngine  
	implements EnhancementEngine, ServiceProperties{
	
	
    /**
     * Using slf4j for logging
     */
    protected final Logger log = LoggerFactory.getLogger(CustomSphinxModelEnhancement.class);
	
	
    /**
     * Allows to define the list of custom dictionary models
     */
    public static final String DICTIONARY_MODEL = "stanbol.engines.speechtotext.dictionary.model";
    
    /**
     * Allows to define the list of custom language models
     */
    public static final String LANGUAGE_MODEL = "stanbol.engines.speechtotext.language.model";
    /**
     * Allows to define the bundle name of the model provider used as acoustic model file names
     * for all bundles are same.
     */
    public static final String ACOUSTIC_MODEL_PROVIDER_BUNDLE_NAME = "stanbol.engines.speechtotext.acoustic.bundlename";

    
    @Reference
    private DataFileTracker dataFileTracker;
    private DataFileListener modelFileListener;
    
    @Reference
    protected ContentItemFactory ci;
    
    /**
     * Bind method of {@link SpeechToTextEngine#ModelProviderImpl}
     * @param MPi
     */
    protected void bindModelProvider(ModelProvider MPi){
        this.MPi = MPi;
    }
    /**
     * Unbind method of {@link SpeechToTextEngine#ModelProviderImpl}
     * @param MPi
     */
    protected void unbindModelProvider(ModelProvider MPi){
        this.MPi = null;
    }
    
    public CustomSphinxModelEnhancement(){}
    
    public CustomSphinxModelEnhancement(ContentItemFactory cifactory) {
    	ci = cifactory;
    	//config=new SphinxConfig(MP);
    }
    /**
     * Activates 
     */
	@Override
    protected void activate(ComponentContext ctx) throws IOException, ConfigurationException{
	super.activate(ctx);
        config = new SphinxConfig();
        ciFactory=ci;
        String acoustic_bundleSymbolicName=null;//Getter for acoustic bundle name
        config.CUSTOM_MODEL_AVAILABLE=true;
        Object value = ctx.getProperties().get(ACOUSTIC_MODEL_PROVIDER_BUNDLE_NAME);
        if(value != null && !value.toString().isEmpty()){
            acoustic_bundleSymbolicName=value.toString();
            config.setBundleSymbolicName(value.toString());
	}
        
		
		
	//Presently Sphinx supports only one model file for language and dictionary model, but may be in future
	//more than one model is supported thence this part of code needs to be changed.
	/************************************************************************************************************************************/
	value = ctx.getProperties().get(DICTIONARY_MODEL);
	if(value != null && !value.toString().isEmpty()){
	    modelFileListener = new NamedModelFileListener<DictionaryModel>();
	    dataFileTracker.add(modelFileListener,value.toString(), null);
	}else {
            throw new ConfigurationException(DICTIONARY_MODEL, "Configurations for the " 
            + getClass().getSimpleName() +" MUST HAVE at least a single custom "
            + "Sphinx model configured! Supported are comma separated " 
            + "Strings, Arrays and Collections. Values are the file names of the " 
            + "Modles. Models are Loaded via the Apache Stanbol DataFileProvider "
            + "Infrastructure (usually user wants to copy modles in the 'datafile' "
            + "directory under the {stanbol.home} directory - {working.dir}/stanbol"
            + "/datafiles).");
        }
	value = ctx.getProperties().get(LANGUAGE_MODEL);
	if(value != null && !value.toString().isEmpty()){
	    modelFileListener = new NamedModelFileListener<LanguageModel>();
	    dataFileTracker.add(modelFileListener,value.toString(), null);
	}else {
            throw new ConfigurationException(DICTIONARY_MODEL, "Configurations for the " 
            + getClass().getSimpleName() +" MUST HAVE at least a single custom "
            + "Sphinx model configured! Supported are comma separated " 
            + "Strings, Arrays and Collections. Values are the file names of the " 
            + "Modles. Models are Loaded via the Apache Stanbol DataFileProvider "
            + "Infrastructure (usually user wants to copy modles in the 'datafile' "
            + "directory under the {stanbol.home} directory - {working.dir}/stanbol"
            + "/datafiles).");
	}
	/************************************************************************************************************************************/
	//Locating the Acoustic Model files in the parsed @bundleSymbolicName
	String acousticResource[]={"feat.params", "mdef", "means", "mixture_weights", "noisedict", "transition_matrices", "variances","feature_transform"};
	for(String resourceName: acousticResource) {
	    dataFileTracker.add(modelFileListener,acoustic_bundleSymbolicName,resourceName, null);
	}
    }
    protected void deactivate(ComponentContext ctx) {
        dataFileTracker.removeAll(modelFileListener); //remove all tracked files
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
    public Map<String, Object> getServiceProperties() {
        return Collections.unmodifiableMap(Collections.singletonMap(
                ENHANCEMENT_ENGINE_ORDERING, (Object)ORDERING_PRE_PROCESSING));
    }

    /**
     * 
     * @author Suman Saurabh
     *
     * @param <T> {@link LanguageModel}, {@link DictionaryModel}, {@link AcousticModel},  
     */
    		
    private class  NamedModelFileListener<T> implements DataFileListener {
                
        private T modelType;
        
        @Override
        public boolean available(String resourceName, InputStream is) {
            try {
                
                //register the new model to the configuration
            	if(modelType.getClass().equals(LanguageModel.class))
            		config.setCustomLangModel(resourceName);
            	else if(modelType.getClass().equals(DictionaryModel.class))
            		config.setCustomDictModel(resourceName);
            	else
            		config.setCustomAcousticModel(resourceName);
                
            } catch (RuntimeException e){
                log.warn("Error while loading custom model from resource " +
                        " resourceName. This model will NOT be available for the "+
                        getClass().getSimpleName()+" (name:"+getName()+")",e);
            }
            return false; //keep tracking
        }

        @Override
        public boolean unavailable(String resourceName) {
            //remove the unavailable model from the configuration
            if(modelType.getClass().equals(LanguageModel.class))
        	config.removeCustomLanguageModel(resourceName);
            else if(modelType.getClass().equals(DictionaryModel.class))
        	config.removeCustomDictModel(resourceName);
            else
        	config.removeCustomAcousticModel(resourceName);
            return false; //keep tracking
        }
        
    }
}
