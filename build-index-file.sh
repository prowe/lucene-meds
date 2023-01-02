#!/bin/bash

# curl -o drug-ndc-0001-of-0001.json.zip https://download.open.fda.gov/drug/ndc/drug-ndc-0001-of-0001.json.zip
# unzip drug-ndc-0001-of-0001.json.zip

CLASSPATH=$CLASSPATH:./target/classes java com.sourceallies.lucinemeds.loader.BuildIndex drug-ndc-0001-of-0001.json