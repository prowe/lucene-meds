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
1. Also add a dependency on [Lucene Queryparser](https://mvnrepository.com/artifact/org.apache.lucene/lucene-queryparser/9.4.2) to handle human entered queries.
1. Create [`NDCProduct`] to represent each medication code, and a [`NDCDataset`] to hold the wrapper object in the JSON file.
1. Both indexing and searching will need a `Directory` to work with, so we can define that as a Bean.
1. We also need to create an `Analyzer` to handle word stemming and other analysis.
1. Create a class that will read the JSON file, convert each `NDCProduct` into a Lucene `Document` and add it to an index.
    We also serialize the entire document under the `_source` field so we can easily deserialize it later.
    We mark this spring boot class with `@Profile("index")` so that it will only be run when spring is started with the "index" profile.
1. When we are indexing, we don't want Tomcat to startup. 
    We can create a `application-index.properties` file to supress that behavior.
1. Create a script to pull down and extract the database file.
    Then that script will run maven with the "index" profile
1. Create a [GraphQL schema file] to define our public API.
1. Define an `IndexSearcher` bean that will act as our entry point into our index.
1. Create a [`SearchMedicationCodesController`] that will be responsible for executing our search.
1. After starting our application locally using `./mvnw spring-boot:run` we can execute a search for a medication by sending a request to the `/graphql` endpoint.
1. Finally, we can wrap everything up in a [Dockerfile] that is capable of build our application.

This project is a great example of how one "blessed stack" is not the best solution for all problems.
Rather than blindly applying a single pattern to our microservice because it "works", we looked at the needs for our specific problem. 
Specfically, we identified that the amount of data we were dealing with was fairly small.
We also observed that the data rarely changes.
Full text search is needed so medications can be searched for so we looked for a good full text search library (Lucene) and then choose our language based on that solution.
The result is a small code base that is easy to manage, easy to scale, and will allow a team to move on to other, higher value, work.