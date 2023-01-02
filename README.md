The data model for most organizations is built on the back of some amount of slowly changing, mostly static "reference data". 
For a retail organiation this might be the list of `Stores`. 
For an equipment manufacturer, it could be the list of `MachineType` options. 
In the healthcare space, this could be the list of ICD-10, NDC, CPT, or LOINC codes. 

Whenever I am architecting a new project, I try to favor solutions that reduce the amount of "undifferenciated heavy lifting". 
This means I like to use AWS Lambda or containers for my services and DynamoDB, RDS, or another managed service for my database.
Recently, AWS announced that they now have a serverless mode for their "OpenSearch Service".
This was intreging to me as a backing store for the microservices that host the system of record for the previously mentioned reference data.

As I was thinking about this solution, spinning up the database was easy enough using Cloudformation. 
The part that was more tricky was loading it.
ETL pipelines to take reference data and load it into a database are not complicated things to build, but they are annoying to have to build.
My service would host full-text search capabilities for the [National Drug Code (NDC)] lookup for a hypethetical [Utilizaton Review] company.
As I was looking at the National Drug Code Directory [database file](https://download.open.fda.gov/drug/ndc/drug-ndc-0001-of-0001.json.zip), I noticed that it was only 26MB.
This gave me an idea: Instead of loading this into a database that is running seperatly, we can load this into a local embedded database and package the whole thing as a Docker container.

This solution has a lot of advantages:
- We don't have to build an ETL process to run in each environment. Instead, we can just make it part of the build process
- Every environment (dev, test, prod, etc) will always have exactly the same data.
- There is no need to manage or pay for a separate database service.
- To update the data, we just build and deploy a new version of the app.
- Reindexing operations are much simpler, we just change the way the index is built.

There are a few downsides to consider:
- An update requires a deployment, so this solution requires a CI/CD pipeline that can be executed faster than the need to update the data.
- The dataset has to be reasonably small, a few MB is fine, a few GB will make the container much bigger.

In order to support the full text search, I am going to leverage Apache Lucene.
This is the open source project that backs ElasticSearch so it has a long track record for full text search.
Since it is a Java library, we're going to use Java to build our container.

1. Initialize a new Spring Boot project using the [Spring Initialzr](https://start.spring.io).
    Select "Spring for GraphQL" and "Spring Web" as the dependencies
1. Extract the downloaded zip.
1. Add a dependency on [Lucene Core](https://mvnrepository.com/artifact/org.apache.lucene/lucene-core/9.4.2)
1. Create a script to pull down and extract the database file.
    Then that script can invoke a custom entrypoint in our spring app that will build the index file.


