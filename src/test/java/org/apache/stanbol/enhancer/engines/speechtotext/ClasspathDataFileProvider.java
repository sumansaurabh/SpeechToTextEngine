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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.stanbol.commons.stanboltools.datafileprovider.DataFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DataFileProvider that looks in our class resources */
public class ClasspathDataFileProvider implements DataFileProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /*
     * NOTE: This path needs to be the same as the one used by the
     *       org.apache.stanbol.data.sphinx.model.wsj bundle to store the 
     *       Sphinx models
     */
    public static final String RESOURCE_BASE_PATH_ACOUSTIC = "acoustic/";
    public static final String RESOURCE_BASE_PATH_LANGUAGE = "language/";
    public static final String RESOURCE_BASE_PATH_DICTIONARY = "acoustic/dict/";
    
    private String symbolicName;
    
    public ClasspathDataFileProvider(String bundleSymbolicName) {
        symbolicName = bundleSymbolicName;
    }
    
    @Override
    public InputStream getInputStream(String bundleSymbolicName,
            String filename, Map<String, String> comments) 
    throws IOException {
    	symbolicName=bundleSymbolicName;// For testing purpose only
        URL dataFile = getDataFile(bundleSymbolicName, filename);

        // Returning null is fine - if we don't have the data file, another
        // provider might supply it
        return dataFile != null ? dataFile.openStream() : null;
    }

    /**
     * @param bundleSymbolicName
     * @param filename
     * @return
     */
    private URL getDataFile(String bundleSymbolicName, String filename) {
        //If the symbolic name is not null check that is equals to the symbolic
        //name used to create this classpath data file provider

    	final String resourcePath;
        if(bundleSymbolicName != null && !symbolicName.equals(bundleSymbolicName)) {
            log.debug("Requested bundleSymbolicName {} does not match mine ({}), request ignored",
                    bundleSymbolicName, symbolicName);
            return null;
        }
        // load default Sphinx models from classpath (embedded in the default data bundle)
        if(filename.compareTo("en-cmu.dict")==0||filename.compareTo("en-digits.dict")==0)
        	resourcePath = RESOURCE_BASE_PATH_DICTIONARY + filename;
        else if(filename.compareTo("en-us.lm.dmp")==0)
        	resourcePath = RESOURCE_BASE_PATH_LANGUAGE + filename;
        else
        	resourcePath = RESOURCE_BASE_PATH_ACOUSTIC + filename;


        URL dataFile = getClass().getClassLoader().getResource(resourcePath);
        if(dataFile==null)
            return null;
        
        return dataFile;
    }
    @Override
    public boolean isAvailable(String bundleSymbolicName, String filename, Map<String,String> comments) {
        return getDataFile(bundleSymbolicName, filename) != null;
    }
}
