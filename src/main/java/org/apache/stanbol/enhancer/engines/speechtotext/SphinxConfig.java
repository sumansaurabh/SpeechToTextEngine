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

import java.util.HashSet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.Configuration;
import org.apache.stanbol.commons.sphinx.AcousticModel;
import org.apache.stanbol.commons.sphinx.BaseModel;
import org.apache.stanbol.commons.sphinx.DictionaryModel;
import org.apache.stanbol.commons.sphinx.LanguageModel;
import org.apache.stanbol.commons.sphinx.ModelProvider;

/**
 * Configures parsed Model files and makes it available to {@link SpeechToTextEngine} 
 * for Speech to Text Recognition
 * 
 * @author Suman Saurabh
 *
 */

public class SphinxConfig {
	
	
    private static final Logger log = LoggerFactory.getLogger(SphinxConfig.class);
    
    private ModelProvider MPi;
    
    private BaseModel lmodel, amodel, dmodel;// Getter for Model files wrapper returned by {@link ModelProviderImpl}
    
    private String defaultLanguage="en";//Getter for language used by {@link SpeechToTextEngine}
    
    private String bundleSymbolicName=null;//Getter for parsed symbolic name of bundle
    
    
    private HashSet<String> acousticModelFile; //Getter for acoustic Model Set
    private HashSet<String> languageModelFile;//Getter for Language Model Set,
    																//currently one model file is made available
    private HashSet<String> dictionaryModelFile;//Getter for Dictionary Model
    																//currently one model file is made available
    
    protected boolean CUSTOM_MODEL_AVAILABLE=false;// True when Custom Model files are used
    
    public SphinxConfig() {
        this.dictionaryModelFile = new HashSet<String>();
        this.languageModelFile = new HashSet<String>();
        this.acousticModelFile = new HashSet<String>();
}
    
   	
    protected boolean initConfig(ModelProvider MPi) {
        this.MPi=MPi;
    	lmodel=new LanguageModel();
        amodel=new AcousticModel();
        dmodel=new DictionaryModel();
        
        if(!CUSTOM_MODEL_AVAILABLE) {
            System.out.println("Getting to acosutic");

            amodel = MPi.getDefaultModel(getDefaultLanguage(),amodel);
            //amodel = MPi.getDefaultModel(getDefaultLanguage(),amodel);

            System.out.println("Getting to dictionary");
            dmodel = MPi.getDefaultModel(getDefaultLanguage(),dmodel);                        
            System.out.println("Getting to language");
            lmodel = MPi.getDefaultModel(getDefaultLanguage(),lmodel);
            
            
           
        }
        else {
            lmodel = this.MPi.getModel(languageModelFile, lmodel,bundleSymbolicName);
            amodel = this.MPi.getModel(acousticModelFile, amodel,bundleSymbolicName);
            dmodel = this.MPi.getModel(dictionaryModelFile, dmodel,bundleSymbolicName);
        }
        
        return amodel != null && dmodel != null && lmodel != null;
    }
	
	
	public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
	
	public void setBundleSymbolicName(String bundleSymbolicName) {
		this.bundleSymbolicName=bundleSymbolicName;
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
		lmodel=null;
		deleteUnavailableResource(lmodel);
		this.languageModelFile.remove(modelFileName);
	}
	public synchronized void removeCustomDictModel(String modelFileName) {
		dmodel=null;
		deleteUnavailableResource(dmodel);
		this.dictionaryModelFile.remove(modelFileName);
	}

	public synchronized void removeCustomAcousticModel(String modelFileName) {
		amodel=null;
		deleteUnavailableResource(amodel);
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
     * Deletes the unavailable resource 
     * 
     * @param modelType {@link LanguageModel}, {@link DictionaryModel}, {@link AcousticModel}
     */
    public void deleteUnavailableResource(BaseModel modelType) {//free temp resources
    	MPi.removeUnavailableResource(modelType);
    }

    /**
     * 
     * @return Returns the initialized {@link Configuration} to {@link SpeechToTextEngine}
     */
	
    protected Configuration getConfiguration() {
	Configuration configuration = new Configuration();
        
    	configuration.setAcousticModelPath(getAcousticModelLocation().toString());           
        //configuration.setAcousticModelPath("/tmp/model/acoustic");
        configuration.setDictionaryPath(getDictionaryModelLocation().toString());
        //configuration.setDictionaryPath("/tmp/model/en-cmu.dict");
        configuration.setLanguageModelPath(getLanguageModelLocation().toString());
        // configuration.setLanguageModelPath("/tmp/model/en-us.lm.dmp");
        return configuration;
    }
	

	

	
}
