
# Original Readme file
# Federation JVM Spring Example

[Apollo Federation JVM](https://github.com/apollographql/federation-jvm) example implementation using [Spring for GraphQL](https://docs.spring.io/spring-graphql/docs/current/reference/html/).
If you want to discuss the project or just say hi, stop by [the Apollo community forums](https://community.apollographql.com/).

The repository contains two separate projects:

1. `products-subgraph`: A Java GraphQL service providing the federated `Product` type
2. `reviews-subgraph`: A Java GraphQL service that extends the `Product` type with `reviews`

See individual projects READMEs for detailed instructions on how to run them.

Running the demo
----

1. Start `products-subgraph` by running the `ProductsApplication` Spring Boot app from the IDE or by running `./gradlew :products-subgraph:bootRun` from the root project directory
2. Start `reviews-subgraph` by running the `ReviewsApplication` Spring Boot app from the IDE or `./gradlew :reviews-subgraph:bootRun` from the root project directory
3. Start Federated Router
   1. Install [rover CLI](https://www.apollographql.com/docs/rover/getting-started)
   2. Start router and compose products schema using [rover dev command](https://www.apollographql.com/docs/rover/commands/dev)

    ```shell
    # start up router and compose products schema
    rover dev --name products --schema ./products-subgraph/src/main/resources/graphql/schema.graphqls --url http://localhost:8080/graphql
    ```

   3. In **another** shell run `rover dev` to compose reviews schema

    ```shell
    rover dev --name reviews --schema ./reviews-subgraph/src/main/resources/graphql/schema.graphqls --url http://localhost:8081/graphql
    ```

4. Open http://localhost:3000 for the query editor

Example federated query

```graphql
query ExampleQuery {
    products {
        id
        name
        description
        reviews {
            id
            text
            starRating
        }
    }
}
```

## Other Federation JVM examples

* [Netflix DGS Federation Example](https://github.com/Netflix/dgs-federation-example)
* [GraphQL Java Kickstart Federation Example](https://github.com/setchy/graphql-java-kickstart-federation-example)



# My Readme Extension
# Project Flow Diagram and Detailed Setup Steps

This section provides a text-based flow diagram of the federated GraphQL architecture, followed by detailed step-by-step instructions for setting up and running the project.  

This project uses **Spring Boot** for the subgraphs (**products** and **reviews**), **Apollo Rover** for supergraph composition, and **Apollo Router** for serving the federated API, all containerized with **Docker Compose**.

---

## Flow Diagram

The following ASCII diagram illustrates the high-level flow of the project setup and runtime process. It shows how the components interact, from building the subgraphs to querying the supergraph via the router.

```
+-----------------------------------+
| Host Machine                      |
|                                   |
|  +-------------------+            |
|  | Build Subgraphs   |            |
|  | - Products JAR    |            |
|  | - Reviews JAR     |            |
|  +-------------------+            |
|          |                        |
|          v                        |
|  +-------------------+            |
|  | Configure Files   |            |
|  | - supergraph.yaml |            |
|  | - router.yaml     |            |
|  | - schema.graphqls |            |
|  +-------------------+            |
|          |                        |
|          v                        |
|  +-------------------+            |
|  | docker-compose up |            |
|  +-------------------+            |
|          |                        |
+----------|------------------------+
           v
+-----------------------------------+
| Docker Environment                |
|                                   |
|  +-------------------+            |
|  | Products Service  | <----------+-- HTTP (8080/graphql)
|  | (Subgraph)        |            |
|  +-------------------+            |
|                                   |
|  +-------------------+            |
|  | Reviews Service   | <----------+-- HTTP (8081/graphql)
|  | (Subgraph)        |            |
|  +-------------------+            |
|          ^                        |
|          |                        |
|          |                        |
|          |                        |
|          v                        |
|  +---------------------+          |
|  | Rover Service       |          |
|  | - Install Rover     |          |
|  | - Compose           |          |
|  |   supergraph.graphql|          |
|  +---------------------+          |
|          |                        |
|          v                        |
|  +-------------------+            |
|  | Router Service    | <----------+-- HTTP (4000/graphql)
|  | - Install Router  |            |
|  | - Load supergraph |            |
|  | - Serve Queries   |            |
|  +-------------------+            |
|                                   |
+-----------------------------------+
           ^
           |
+----------|------------------------+
| Host Machine                      |
|                                   |
|  +-------------------+            |
|  | Query Supergraph  |            |
|  | (curl or client)  |            |
|  +-------------------+            |
|                                   |
+-----------------------------------+
```

---

## Production Architecture Diagram
The diagram reflects the use of Apollo GraphOS for schema composition and real-time delivery, removing Rover and S3 for schema storage.
```
+------------------------------------+
| Clients (Web/Mobile Apps, Tools)   |
|                                    |
|  +-----------------------+         |
|  | GraphQL Queries       |         |
|  | (e.g., curl, Postman) |         |
|  +-----------------------+         |
|            |                       |
|            v                       |
+------------|-----------------------+
             v
+---------------------------------------------------+
| Edge Layer (AWS API Gateway)                      |
| - HTTPS: api.example.com                          |
| - Rate Limiting, WAF                              |
| - Cognito Auth / API Keys                         |
| - Routes to Router ALB (Application Load Balancer)|
+------------|--------------------------------------+
             v
+----------------------------------------+
| API Layer (Apollo Router)              |
| - EKS Pods (2-10 replicas, HPA)        |
| - Polls GraphOS for supergraph.graphql |
|   every 10 seconds for new schema      |
| - Routes queries to subgraphs          |
| - Telemetry: Prometheus/Grafana        |
| - Health: /health (8088)               |
+------------|---------------------------+
             |                       |
             v                       v
+------------------------------------|-----------------------+
| Subgraphs Layer (EKS Pods)         |                       |
|  +-------------------+             |                       |
|  | Products Service  | <--- ALB (8080/graphql)             |
|  | - Spring Boot JAR |                                     |
|  +-------------------+                                     |
|  +-------------------+                                     |
|  | Reviews Service   | <--- ALB (8081/graphql)             |
|  | - Spring Boot JAR |                                     |
|  +-------------------+                                     |
|                                                            |
+------------------------------------|-----------------------+
             |                       |                       |
             v                       v                       |
+------------------------------------|-----------------------+
| Data Layer                         |                       |
| - RDS PostgreSQL (Multi-AZ) for    |                       |
|   products/reviews data            |                       |
| - ElastiCache (Redis) for caching  |                       |
+------------------------------------|-----------------------+
             |                       |                       |
             |                       |                       |
+------------|-----------------------|-----------------------+
| Operations Layer                   |                       |
| - CI/CD: GitHub Actions            |                       |
|   - On schema change:              |                       |
|     - Publish schemas to GraphOS   |                       |
|     - GraphOS composes supergraph  |                       |
| - Monitoring: Prometheus, Grafana  |                       |
| - Logging: OpenSearch (ELK)        |                       |
| - Tracing: AWS X-Ray / Jaeger      |                       |
| - Secrets: AWS Secrets Manager     |                       |
| - IaC: Terraform / CDK             |                       |
+------------------------------------|-----------------------+
             ^                       |
             |                       |
             +-------------------------------+
             | Apollo GraphOS                |
             | - Schema Registry             |
             | - Composes supergraph.graphql |
             | - Delivers to Router          |
             +-------------------------------+
```

---


## Diagram Explanation

- **Host Machine (Build & Configure)**  
  Build the Spring Boot JARs for the subgraphs, configure YAML and schema files, and run Docker Compose.  

- **Docker Environment**  
  - **Subgraphs**: Products and reviews services run as Spring Boot apps, exposing GraphQL endpoints.  
  - **Rover**: Waits for subgraphs, fetches schemas via HTTP, and composes `supergraph.graphql`.  
  - **Router**: Waits for the supergraph, loads it, and serves the federated API at **port 4000**.  

- **Host Machine (Query)**  
  Query the router at [http://localhost:4000/graphql](http://localhost:4000/graphql), which routes requests to the subgraphs.  

---

## A. Detailed Setup Steps

### 1. Clone the Repository
```bash
git clone https://github.com/ankitrajput0096/simple_federated_graphql_application
cd federation-jvm-spring-example
```

### 2. Build the Subgraph JARs
Run below command from the root project directory to generate jar for products-subgraph and reviews-subgraph

```bash
./gradlew bootJar
```

Generates:
- `products.jar`  
- `reviews.jar`  

### 3. Run the Project
```bash
docker-compose up --build
```

- Rover waits **60s** for subgraphs → composes `supergraph.graphql`.  
- Router waits **90s** → installs router → serves on **port 4000**.  

---

### 4. Test the Federated API

Example query:
```bash
curl -X POST http://localhost:4000/graphql   -H "Content-Type: application/json"   -d '{"query": "query { products { id name reviews { id text starRating } } }"}'
```

Check logs:
```bash
cat rover/rover.log
cat rover/router.log
```

Inspect supergraph:
```bash
cat rover/supergraph.graphql
```

---

### 5. Stop the Project
```bash
docker-compose down
```

## B. Subgraph Schemas

**Products Schema** – `products-subgraph/src/main/resources/graphql/schema.graphqls`
```graphql
extend schema
  @link(url: "https://specs.apollo.dev/link/v1.0")
  @link(url: "https://specs.apollo.dev/federation/v2.0", import: ["@key", "@shareable"])

type Query {
  products: [Product!]!
}

type Product @key(fields: "id") {
  id: ID!
  name: String!
  description: String
}
```

**Reviews Schema** – `reviews-subgraph/src/main/resources/graphql/schema.graphqls`
```graphql
extend schema
  @link(url: "https://specs.apollo.dev/link/v1.0")
  @link(url: "https://specs.apollo.dev/federation/v2.0", import: ["@key", "@shareable", "@provides"])

type Query {
  allReviews: [Review!]!
}

type Review @key(fields: "id") {
  id: ID!
  text: String!
  starRating: Int!
  product: Product @provides(fields: "id")
}

type Product @key(fields: "id") @shareable {
  id: ID!
}
```

---

