# CarBuilder GraphQL Platform

A federated GraphQL platform for car building using Spring Boot + WebFlux + Spring for GraphQL.

## Table of Contents
- [Phase 0 Overview](#phase-0-overview)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Installation & Setup](#installation--setup)
- [Building the Project](#building-the-project)
- [Running the Application](#running-the-application)
- [Testing GraphQL Endpoints](#testing-graphql-endpoints)
- [Troubleshooting](#troubleshooting)
- [Development Workflow](#development-workflow)
- [Next Phases](#next-phases)

## Phase 0 Overview

**Goal**: Create a runnable multi-module Spring Boot (WebFlux) project skeleton that exposes a minimal GraphQL endpoint, has GraphiQL enabled, includes a sample schema + annotated controller, and is organized so each future subgraph can be developed as its own module.

**Deliverables**:
- Multi-module Maven project structure
- Working car-service with GraphQL endpoint
- GraphiQL interface enabled
- Reactive Spring Boot setup with WebFlux
- Sample schema and controller with proper annotations
- Comprehensive documentation and testing commands

## Prerequisites

Verify you have these installed:

```bash
# Check Java version (17+ required)
java -version

# Check Maven version (3.8+ required)
mvn -version

# Check Git
git --version
```

Required versions:
- **Java**: 17 or later
- **Maven**: 3.8 or later
- **Git**: Any recent version

## Project Structure

```
car-builder/
├── pom.xml                           # Parent POM (multi-module)
├── README.md                         # This file
├── .gitignore                        # Git ignore rules
├── common/                           # Shared utilities and DTOs
│   └── pom.xml                       # Common module POM
├── car-service/                      # Main service (Phase 0 implementation)
│   ├── pom.xml                       # Car service dependencies
│   ├── src/main/java/
│   │   └── com/example/carservice/
│   │       ├── CarServiceApplication.java     # Main application class
│   │       └── controller/
│   │           └── CarController.java         # GraphQL controller
│   ├── src/main/resources/
│   │   ├── application.properties             # Configuration
│   │   └── graphql/
│   │       └── schema.graphqls               # GraphQL schema
│   └── src/test/java/                        # Unit tests
├── engine-service/                   # Future subgraph (placeholder)
├── body-service/                     # Future subgraph (placeholder)
└── tyre-service/                     # Future subgraph (placeholder)
```

## Installation & Setup

### 1. Clone or Create Repository

```bash
# If cloning existing repo
git clone <repository-url>
cd car-builder

# If creating new repo
mkdir car-builder
cd car-builder
git init
git branch -M main
```

### 2. Verify Prerequisites

```bash
# Verify Java 17+
java -version

# Verify Maven 3.8+
mvn -version
```

## Building the Project

### Build All Modules

```bash
# Clean and compile all modules
mvn clean compile

# Package all modules (creates JAR files)
mvn clean package

# Skip tests during build (if needed)
mvn clean package -DskipTests
```

### Build Specific Module

```bash
# Build only car-service and its dependencies
mvn -pl car-service -am clean package

# Compile only car-service
mvn -pl car-service -am compile
```

### Build with Different Profiles

```bash
# Build for development
mvn clean package -Pdev

# Build for production
mvn clean package -Pprod
```

## Running the Application

### Start car-service

```bash
# Run car-service using Maven plugin
mvn -pl car-service spring-boot:run

# Alternative: Run as packaged JAR
java -jar car-service/target/car-service-0.0.1-SNAPSHOT.jar

# Run with specific profile
mvn -pl car-service spring-boot:run -Dspring-boot.run.profiles=dev

# Run with JVM arguments
mvn -pl car-service spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"
```

### Verify Application Startup

Look for these log messages:
```
Started CarServiceApplication in X.XXX seconds
Loaded 1 resource(s) in the GraphQL schema
```

Application will be available at: http://localhost:8080

## Testing GraphQL Endpoints

### GraphiQL Interface

**Access GraphiQL**:
- Primary URL: http://localhost:8080/graphiql
- Direct URL: http://localhost:8080/graphiql?path=/graphql
- If loading issues, try incognito/private browsing

**Note**: If GraphiQL shows "Loading..." indefinitely, use curl commands below for testing.

### Command Line Testing

#### Basic Health Check
```bash
# Simple ping test
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ ping }"}'

# Expected response:
# {"data":{"ping":"pong - car service is running!"}}
```

#### Pretty JSON Output
```bash
# Install jq for pretty printing (optional)
brew install jq  # macOS
# or: sudo apt-get install jq  # Ubuntu

# Ping with pretty output
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ ping }"}' | jq
```

#### Test Car Queries
```bash
# Test car query with argument
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query { car(vin: \"TEST123\") { vin model color year isElectric } }"}' | jq

# Test cars list query
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query { cars(limit: 2) { vin model color } }"}' | jq
```

#### Schema Introspection
```bash
# Test schema introspection (required for GraphiQL)
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query IntrospectionQuery { __schema { queryType { name fields { name } } } }"}' | jq

# Get schema as text (if enabled)
curl http://localhost:8080/graphql/schema
```

#### GraphQL with Variables
```bash
# Using variables in queries
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetCar($vin: ID!) { car(vin: $vin) { vin model color } }",
    "variables": {"vin": "VIN123"}
  }' | jq
```

### Sample GraphQL Queries (for GraphiQL)

Copy these into GraphiQL interface when it loads:

#### Query 1: Health Check
```graphql
query {
  ping
}
```

#### Query 2: Single Car
```graphql
query {
  car(vin: "VIN123") {
    vin
    model
    color
    year
    isElectric
  }
}
```

#### Query 3: Multiple Cars
```graphql
query {
  cars(limit: 3) {
    vin
    model
    color
    isElectric
  }
}
```

#### Query 4: With Variables
```graphql
query GetCarDetails($vin: ID!, $limit: Int) {
  car(vin: $vin) {
    vin
    model
    color
    year
  }
  cars(limit: $limit) {
    vin
    model
  }
}
```

Variables (paste in GraphiQL variables panel):
```json
{
  "vin": "VIN123",
  "limit": 2
}
```

## Running Tests

### Unit Tests
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn -pl car-service test

# Run tests with output
mvn -pl car-service test -Dtest.verbose=true

# Run specific test class
mvn -pl car-service test -Dtest=CarControllerTest

# Run tests and generate reports
mvn -pl car-service test jacoco:report
```

### Integration Tests
```bash
# Run integration tests (if any exist)
mvn -pl car-service integration-test

# Run with Spring Boot test profile
mvn -pl car-service test -Dspring.profiles.active=test
```

### Test Coverage
```bash
# Generate test coverage report
mvn -pl car-service jacoco:prepare-agent test jacoco:report
```

## Development Workflow

### Daily Development Commands

```bash
# 1. Start development session
cd car-builder
git pull origin main

# 2. Clean build and run
mvn clean package -pl car-service -am
mvn -pl car-service spring-boot:run

# 3. Test changes
curl -X POST http://localhost:8080/graphql -H "Content-Type: application/json" -d '{"query":"{ ping }"}' | jq

# 4. Run tests
mvn -pl car-service test

# 5. Commit changes
git add .
git commit -m "Your commit message"
git push origin main
```

### Hot Reload Development

With `spring-boot-devtools` enabled:
1. Make changes to Java code
2. Save files
3. Application automatically restarts
4. Test changes immediately

### Adding New GraphQL Operations

1. **Update schema** (`src/main/resources/graphql/schema.graphqls`)
2. **Add controller method** with appropriate annotation
3. **Test with curl** before using GraphiQL
4. **Write unit test** for the new operation

## Troubleshooting

### Common Issues and Solutions

#### Issue: Application Won't Start
```bash
# Check Java version
java -version

# Check for port conflicts
lsof -i :8080

# Run with different port
mvn -pl car-service spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

#### Issue: GraphQL Parameter Binding Errors
**Error**: `Name for argument of type [java.lang.String] not specified`

**Solution**: Ensure Maven compiler preserves parameter names:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <parameters>true</parameters>
    </configuration>
</plugin>
```

#### Issue: GraphiQL Shows "Loading..."
**Solutions to try**:
```bash
# 1. Try direct redirect URL
# Open: http://localhost:8080/graphiql?path=/graphql

# 2. Test if GraphQL endpoint works
curl -X POST http://localhost:8080/graphql -H "Content-Type: application/json" -d '{"query":"{ ping }"}'

# 3. Test schema introspection
curl -X POST http://localhost:8080/graphql -H "Content-Type: application/json" -d '{"query":"{ __schema { queryType { name } } }"}'

# 4. Clear browser cache or try incognito mode

# 5. Check browser developer console for errors
```

#### Issue: 404 on GraphQL Endpoint
```bash
# Verify schema file location
ls -la car-service/src/main/resources/graphql/

# Check application properties
cat car-service/src/main/resources/application.properties

# Verify GraphQL starter dependency
mvn -pl car-service dependency:tree | grep graphql
```

#### Issue: Tests Failing
```bash
# Run tests with debug output
mvn -pl car-service test -X

# Run specific failing test
mvn -pl car-service test -Dtest=CarControllerTest#pingQuery

# Skip tests temporarily
mvn -pl car-service package -DskipTests
```

### Debug Information Commands

```bash
# Check what's running on port 8080
lsof -i :8080

# View application logs
mvn -pl car-service spring-boot:run | tee app.log

# Check GraphQL schema is loaded
curl http://localhost:8080/graphql/schema

# Test endpoint availability
curl -I http://localhost:8080/graphql

# Check HTTP methods allowed
curl -v http://localhost:8080/graphql
```

### Performance Monitoring

```bash
# Monitor application startup time
time mvn -pl car-service spring-boot:run

# Check memory usage
jps -v | grep CarServiceApplication

# Monitor HTTP requests
curl -w "@curl-format.txt" -X POST http://localhost:8080/graphql -H "Content-Type: application/json" -d '{"query":"{ ping }"}'
```

Create `curl-format.txt`:
```
     time_namelookup:  %{time_namelookup}\n
        time_connect:  %{time_connect}\n
     time_appconnect:  %{time_appconnect}\n
    time_pretransfer:  %{time_pretransfer}\n
       time_redirect:  %{time_redirect}\n
  time_starttransfer:  %{time_starttransfer}\n
                     ----------\n
          time_total:  %{time_total}\n
```

## Environment Configuration

### Development Environment
```properties
# car-service/src/main/resources/application-dev.properties
server.port=8080
spring.graphql.graphiql.enabled=true
logging.level.org.springframework.graphql=DEBUG
logging.level.com.example.carservice=DEBUG
spring.devtools.restart.enabled=true
```

### Production Environment
```properties
# car-service/src/main/resources/application-prod.properties
server.port=8080
spring.graphql.graphiql.enabled=false
logging.level.org.springframework.graphql=INFO
logging.level.com.example.carservice=INFO
```

### Test Environment
```properties
# car-service/src/test/resources/application.properties
spring.graphql.graphiql.enabled=false
logging.level.org.springframework.graphql=WARN
```

## Phase 0 Acceptance Criteria

Verify each item is working:

- [ ] **Multi-module project builds**: `mvn clean package`
- [ ] **car-service runs**: `mvn -pl car-service spring-boot:run`
- [ ] **GraphQL endpoint responds**: `curl -X POST http://localhost:8080/graphql -H "Content-Type: application/json" -d '{"query":"{ ping }"}'`
- [ ] **GraphiQL accessible**: http://localhost:8080/graphiql (or direct URL: http://localhost:8080/graphiql?path=/graphql)
- [ ] **Schema introspection works**: `curl -X POST http://localhost:8080/graphql -H "Content-Type: application/json" -d '{"query":"{ __schema { queryType { name } } }"}'`
- [ ] **Car queries return data**: Test car(vin) and cars(limit) operations
- [ ] **Tests pass**: `mvn -pl car-service test`
- [ ] **Documentation complete**: This README
- [ ] **Git repository setup**: Project committed to version control

## Quick Start Commands

```bash
# Complete setup and verification in one go
git clone <your-repo>
cd car-builder

# Build everything
mvn clean package

# Start car-service
mvn -pl car-service spring-boot:run

# In another terminal - test GraphQL
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ ping }"}' | jq

# Test GraphiQL
open http://localhost:8080/graphiql?path=/graphql
```

## GraphQL Schema Reference

Current schema (`car-service/src/main/resources/graphql/schema.graphqls`):

```graphql
"""
Phase 0 minimal schema to test GraphQL + GraphiQL integration
"""
type Query {
    """Simple health check endpoint"""
    ping: String!
    
    """Fetch a car by VIN"""
    car(vin: ID!): Car
    
    """List all cars with optional limit"""
    cars(limit: Int = 10): [Car!]!
}

"""
Represents a car in the system
"""
type Car {
    vin: ID!
    model: String!
    color: String!
    year: Int
    isElectric: Boolean!
}
```

## API Examples

### Using curl

```bash
# Health check
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ ping }"}'

# Get specific car
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ car(vin: \"VIN123\") { vin model color year isElectric } }"}'

# Get multiple cars
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ cars(limit: 3) { vin model color isElectric } }"}'

# Complex query with alias
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ 
    ping 
    specificCar: car(vin: \"VIN123\") { vin model } 
    allCars: cars(limit: 2) { vin color } 
  }"}'
```

### Using Variables

```bash
# Query with variables
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetCarInfo($vin: ID!, $limit: Int) { 
      car(vin: $vin) { vin model color year } 
      cars(limit: $limit) { vin model } 
    }",
    "variables": {
      "vin": "VIN123",
      "limit": 2
    }
  }'
```

## Development Notes

### Hot Reload
- Changes to Java code trigger automatic restart (via spring-boot-devtools)
- Changes to `application.properties` trigger restart
- Changes to GraphQL schema files trigger restart

### Adding New Operations
1. Update schema in `schema.graphqls`
2. Add controller method with appropriate annotation:
    - `@QueryMapping` for queries
    - `@MutationMapping` for mutations
    - `@SubscriptionMapping` for subscriptions
3. Use `@Argument` annotation for method parameters
4. Return `Mono<T>` or `Flux<T>` for reactive support

### Schema Development Tips
- Use triple quotes `"""` for documentation
- Always specify non-null with `!` where appropriate
- Provide default values for optional arguments: `limit: Int = 10`
- Use descriptive type and field names

## Technology Stack

- **Spring Boot**: 3.3.2
- **Spring WebFlux**: Reactive web framework
- **Spring for GraphQL**: 1.3.2
- **Java**: 17+
- **Maven**: 3.8+
- **GraphiQL**: Built-in web interface

## Next Phases Roadmap

### Phase 1: Complete car-service Implementation
- Full CRUD operations (Create, Read, Update, Delete)
- Input validation and error handling
- Service layer with mock data
- Comprehensive test suite

### Phase 2: Additional Subgraphs + DataLoader
- Implement engine-service, body-service, tyre-service
- Add @BatchMapping for N+1 problem resolution
- Cross-service data fetching

### Phase 3: Federation Setup
- Schema registry implementation
- Apollo Gateway or custom gateway
- Cross-subgraph entity resolution

### Phase 4: Advanced Features
- Custom directives implementation
- Instrumentation for monitoring
- Error classification and handling

### Phase 5: Production Readiness
- Security (JWT + Spring Security)
- Comprehensive testing strategy
- Observability and monitoring

## Contributing

### Code Standards
- Use Java 17+ features appropriately
- Follow Spring Boot conventions
- Write reactive code with Mono/Flux
- Include unit tests for all new features
- Update schema documentation

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and test
mvn clean package
mvn -pl car-service test

# Commit changes
git add .
git commit -m "feat: add your feature description"

# Push and create PR
git push origin feature/your-feature-name
```

## Support

For issues or questions:
1. Check this README's troubleshooting section
2. Review application logs for error details
3. Test GraphQL endpoint directly with curl
4. Check Spring Boot and Spring GraphQL documentation

---

**Phase 0 Status**: ✅ Complete  
**Next Phase**: Ready for Phase 1 implementation