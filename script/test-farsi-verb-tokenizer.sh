#!/bin/bash

dir="../resource"

if [ $# -gt 0 ]; then
dir=$1
fi

export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

java -classpath "../bin/" morphology $dir/farsi-verb-morphology.xml > $dir/transformations.tsv
perl ../farsi-verb-morphology.perl -d $dir input.txt > output.txt
