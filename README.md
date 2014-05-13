stanbol-1007
============

TBD enhancement engine uses Sphinix library to convert the captured audio. Media (audio/video) data file is parsed with the ContentItem and formatted to proper audio format by Xuggler libraries. Audio speech is than extracted by Sphinix to 'plain/text' with the annotation of temporal position of the extracted text. Sphinix uses acoustic model and language model to map the utterances with the text, so the engine will also provide support of uploading acoustic model and language model.
