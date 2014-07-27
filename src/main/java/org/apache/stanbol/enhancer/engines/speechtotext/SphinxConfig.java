package org.apache.stanbol.enhancer.engines.speechtotext;

import java.util.HashSet;

import org.apache.stanbol.commons.sphinx.ModelProvider;
import org.apache.stanbol.commons.sphinx.impl.ModelProviderImpl;
import org.apache.stanbol.commons.sphinx.model.AcousticModel;
import org.apache.stanbol.commons.sphinx.model.BaseModel;
import org.apache.stanbol.commons.sphinx.model.DictionaryModel;
import org.apache.stanbol.commons.sphinx.model.LanguageModel;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.Configuration;



public class SphinxConfig {
	
	
    private static final Logger log = LoggerFactory.getLogger(SpeechToTextEngine.class);
    private ModelProvider MP;
    private BaseModel lmodel, amodel, dmodel;
    
    
    
    public SphinxConfig() {
    	MP=new ModelProviderImpl();
    }
    
    /**
     * For Unit Test Cases outside OSGI Environment
     * @param MP
     */

	public SphinxConfig(ModelProviderImpl MP) {
		this.MP=MP;
	}

	private static HashSet<String> extractModels(ContentItem ci) {
		// TODO Auto-generated method stub
		return null;
	}
	
	void initConfig(ContentItem ci) {
		String lang=extractLanguage(ci);
    	HashSet<String> models=extractModels(ci);
    	lmodel=new LanguageModel();
        amodel=new AcousticModel();
        dmodel=new DictionaryModel();
        
        if(models==null) {
            lmodel = MP.getDefaultModel(lang, lmodel);
            amodel = MP.getDefaultModel(lang,amodel);
            dmodel = MP.getDefaultModel(lang,dmodel);
        }
        else {
            lmodel = MP.getModel(lang, models, lmodel);
            amodel = MP.getModel(lang, models, amodel);
            dmodel = MP.getModel(lang, models, dmodel);
        }
	}
	
	
	
	 public String getDefaultLanguage() {
	        return "en";
	 }
	
	public AcousticModel getAcousticModelLocation() {
		return (AcousticModel)amodel;
	}
	public DictionaryModel getDictionaryModelLocation() {
		return (DictionaryModel)dmodel;
	}
	public LanguageModel getLanguageModelLocation() {		
        return (LanguageModel)lmodel;
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
            log.info(" ... return '{}' as default",getDefaultLanguage());
            return getDefaultLanguage();
        }
    }
    
    public Configuration getConfiguration()
    {
    	Configuration configuration = new Configuration();
        //System.out.println("$$$$$$$$$"+getAcousticModelLocation().toString());
    	configuration.setAcousticModelPath(getAcousticModelLocation().toString());
        configuration.setDictionaryPath(getDictionaryModelLocation().toString());
        configuration.setLanguageModelPath(getLanguageModelLocation().toString());
        return configuration;
    }
	
    public void deleteTemp()//free temp resources
    {
    	ModelProviderImpl mp=(ModelProviderImpl) MP;
    	mp.deactivate(null);
    }
}
