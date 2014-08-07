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

This Enhancement engine uses Sphinx library to convert the captured audio. Media (audio/video) data file is parsed with the ContentItem. Audio speech is than extracted by Sphinx to 'plain/text' with the annotation of temporal position of the extracted text. Sphinix uses acoustic model, dictionary model and language model to map the utterances with the text, so the engine will also provide support of uploading acoustic model and language model.

mvn install -DskipTests -PinstallBundle \
    -Dsling.url=http://localhost:8080/system/console

