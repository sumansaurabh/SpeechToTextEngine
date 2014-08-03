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
    private String defaultLanguage="en";
    
    //private String languageModelFile=null,dictionaryModelFile=null;//Getter for custom model files
    private HashSet<String> acousticModelFile=new HashSet<String>();
    private HashSet<String> languageModelFile=new HashSet<String>();
    private HashSet<String> dictionaryModelFile=new HashSet<String>();
    
    
    
    protected static boolean CUSTOM_MODEL_AVAILABLE=false;// True when Custom Model files are used
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

	
	
	void initConfig(ContentItem ci) {
		String lang=extractLanguage(ci);
    	lmodel=new LanguageModel();
        amodel=new AcousticModel();
        dmodel=new DictionaryModel();
        
        if(!CUSTOM_MODEL_AVAILABLE) {

            lmodel = MP.getDefaultModel(lang, lmodel);
            amodel = MP.getDefaultModel(lang,amodel);
            dmodel = MP.getDefaultModel(lang,dmodel);
        }
        else {
            lmodel = MP.getModel(languageModelFile, lmodel);
            amodel = MP.getModel(acousticModelFile, amodel);
            dmodel = MP.getModel(dictionaryModelFile, dmodel);
        }
	}
	
	
	public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
	
	public void setBundleSymbolicName(String bundleSymbolicName) {
                MP.setBundleSymbolicName(bundleSymbolicName);
	}
	public String getDefaultLanguage() {
		return defaultLanguage;
	}
	
	public synchronized void setCustomLangModel(String modelFileName){
		this.languageModelFile.add(modelFileName);
	}
	
	public synchronized void setCustomDictModel(String modelFileName){
		this.dictionaryModelFile.add(modelFileName);
	}
	public synchronized void setCustomAcousticModel(String modelFileName) {
		this.acousticModelFile.add(modelFileName);
	}
	public synchronized void removeCustomLanguageModel(String modelFileName) {
		this.languageModelFile.remove(modelFileName);
	}
	public synchronized void removeCustomDictModel(String modelFileName) {
		this.dictionaryModelFile.remove(modelFileName);
	}

	public synchronized void removeCustomAcousticModel(String modelFileName) {
		this.acousticModelFile.remove(modelFileName);
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
            log.info(" ... returned '{}' as default",getDefaultLanguage());
            return getDefaultLanguage();
        }
    }
    
    
	
    public void deleteTemp()//free temp resources
    {
    	ModelProviderImpl mp=(ModelProviderImpl) MP;
    	mp.deactivate(null);
    }

	
	
	public Configuration getConfiguration() {
    	Configuration configuration = new Configuration();

    	configuration.setAcousticModelPath(getAcousticModelLocation().toString());
        configuration.setDictionaryPath(getDictionaryModelLocation().toString());
        configuration.setLanguageModelPath(getLanguageModelLocation().toString());
        return configuration;
    }
	

	

	
}
