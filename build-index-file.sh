#!/bin/bash

# curl -o drug-ndc-0001-of-0001.json.zip https://download.open.fda.gov/drug/ndc/drug-ndc-0001-of-0001.json.zip
# unzip drug-ndc-0001-of-0001.json.zip
rm -rf data

export spring_profiles_active=index 
./mvnw spring-boot:run