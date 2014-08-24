<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

[Speech To Text Enhancement Engine](https://issues.apache.org/jira/browse/STANBOL-1007)

[CMUSphinx](http://cmusphinx.sourceforge.net/wiki/) is a speaker-independent large vocabulary continuous speech recognizer released under BSD style license .

This Enhancement engine uses Sphinx4 library to convert the captured audio. Media (audio/video) data file is parsed with the ContentItem. Audio speech is than extracted by Sphinx to 'plain/text' with the annotation of temporal position of the extracted text. Sphinix uses acoustic model, dictionary model and language model to map the utterances with the text, so the engine will also provide support of uploading acoustic model and language model.

Audio file accepted by Sphinix libraries, accepts sound in following format:

    Frequency: 16 kHz 
    Depth: 16 bit
    Type: mono
    little-endian byte order

[FFmpeg](https://www.ffmpeg.org/) can be used to convert sound file in the above format
    ffmpeg -i input_file -acodec pcm_s16le -ar 16000 -ac 1 output.wav

#### Features
1.  Provide the extracted text
2.  Enhancement Results keep track of the temporal position of the extracted text within the processed media file.

#### Installation

1.  Install Sphinx4 OSGi bundle.
2.  Install [Sphinx4 Model files](https://github.com/sumansaurabh/Sphinx-Model)
3.  Install [Sphinx4 Model Provider Service](https://github.com/sumansaurabh/SphinxModelProvider) 
4.  Install [Speech To Text Engine](https://github.com/sumansaurabh/SpeechToTextEngine) Bundle 


    mvn install -DskipTests -PinstallBundle -Dsling.url=http://localhost:8080/system/console

##### Usage

Default Enhancer usage:

    Acoustic Model: [EN-US Generic](http://sourceforge.net/projects/cmusphinx/files/Acoustic%20and%20Language%20Models/US%20English%20Generic%20Acoustic%20Model/en-us.tar.gz/download)
    Language Model: [en-us.lm.dmp](https://svn.code.sf.net/p/cmusphinx/code/trunk/sphinx4/sphinx4-data/src/main/resources/edu/cmu/sphinx/models/language/en-us.lm.dmp)
    Dictionary Model: [cmudict.0.6d](https://svn.code.sf.net/p/cmusphinx/code/trunk/sphinx4/sphinx4-data/src/main/resources/edu/cmu/sphinx/models/acoustic/wsj/dict/cmudict.0.6d)

Default enhancer uses the above model to extract text from parsed sound file

###### Custom Enhancer usage:

    Acoustic Model: Bundle name is provided as Acoustic Model files have same name for all types of bundle, stanbol.engines.speechtotext.acoustic.bundlename
    Language Model: stanbol.engines.speechtotext.language.model
    Dictionary Model: stanbol.engines.speechtotext.dictionary.model

###### Run enhancer

    curl -v -X POST -H "Accept: application/rdf+xml" -H "Content-type: audio/wav" -T temp.wav "http://localhost:8090/enhancer/engine/sphinx"
    
##### Test Cases Result

  Sound file: temp.wav 
  Spoken Text: 1001-90210-01803
  Predicted Text: one zero zero zero one, nine oh two one oh, cyril one eight zero three






