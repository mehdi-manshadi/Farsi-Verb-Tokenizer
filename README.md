Farsi-Verb-Tokenizer
====================

Tokenizes Farsi Verbs

To run the tokenizer use the following script:

perl farsi-verb-tokenizer.perl [-d resource-directory] input-file [pattern_id]

resource-directory:
	The directory where resource files are located. This directory by default is "./resource". 

input-file:
	A list of sentences to be tokenized.

pattern_id:
	 The id of the transformation to be applied, a number between 0 and the number of transformations (that is the number of lines in "transformation.tsv") minus 1, inclusive.
	 
output:
	The list of tokenized sentences.
	
If no pattern_id is passed, all the transformations will be applied to each individual sentence one by one. 
The transformations in "resource/transformations.tsv" are sorted from largest to smallest, so that if a larger transformation matches (and hence transforms) a substring of a sentence, smaller transformations could no longer affect that substring. 

Example:

perl farsi-verb-tokenizer.perl test-input.txt > test-output.txt

The output should be exactly the same as test-tokenized.txt. You can check that running the following diff command:

diff test-output.txt test-tokenized.txt


