---
description: Backend development standards, best practices, and conventions for Java/Spring Boot applications including Domain-Driven Design, SOLID principles, architecture patterns, API design, persistence, and testing practices
globs: ["backend/src/main/java/**/*.java", "backend/src/main/resources/**/*.{yml,yaml,properties,xml}", "backend/src/test/java/**/*.java", "backend/pom.xml", "backend/mvnw", "backend/Dockerfile"]
alwaysApply: true
---

# Backend Project Standards and Best Practices

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
  - [Core Technologies](#core-technologies)
  - [Database & ORM](#database--orm)
  - [Testing Framework](#testing-framework)
  - [Development Tools](#development-tools)
- [Architecture Overview](#architecture-overview)
  - [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
  - [Layered Architecture](#layered-architecture)
  - [Project Structure](#project-structure)
- [Domain-Driven Design Principles](#domain-driven-design-principles)
  - [Entities](#entities)
  - [Value Objects](#value-objects)
  - [Aggregates](#aggregates)
  - [Repositories](#repositories)
  - [Domain Services](#domain-services)
  - [Additional Recommendations](#additional-recommendations)
- [SOLID and DRY Principles](#solid-and-dry-principles)
  - [Single Responsibility Principle (SRP)](#single-responsibility-principle-srp)
  - [Open/Closed Principle (OCP)](#openclosed-principle-ocp)
  - [Liskov Substitution Principle (LSP)](#liskov-substitution-principle-lsp)
  - [Interface Segregation Principle (ISP)](#interface-segregation-principle-isp)
  - [Dependency Inversion Principle (DIP)](#dependency-inversion-principle-dip)
  - [DRY (Don't Repeat Yourself)](#dry-dont-repeat-yourself)
- [Coding Standards](#coding-standards)
  - [Language and Naming Conventions](#language-and-naming-conventions)
    - [Java Usage](#java-usage)
  - [Error Handling](#error-handling)
  - [Validation Patterns](#validation-patterns)
  - [Logging Standards](#logging-standards)
- [API Design Standards](#api-design-standards)
  - [REST Endpoints](#rest-endpoints)
  - [Request/Response Patterns](#requestresponse-patterns)
  - [Error Response Format](#error-response-format)
  - [CORS Configuration](#cors-configuration)
- [Database Patterns](#database-patterns)
    - [JPA Entity Model](#jpa-entity-model)
  - [Migrations](#migrations)
  - [Repository Pattern](#repository-pattern)
- [Testing Standards](#testing-standards)
  - [Unit Testing](#unit-testing)
  - [Integration Testing](#integration-testing)
  - [Test Coverage Requirements](#test-coverage-requirements)
  - [Mocking Standards](#mocking-standards)
- [Performance Best Practices](#performance-best-practices)
  - [Database Query Optimization](#database-query-optimization)
    - [Asynchronous Patterns](#asynchronous-patterns)
  - [Error Handling Performance](#error-handling-performance)
- [Security Best Practices](#security-best-practices)
  - [Input Validation](#input-validation)
  - [Environment Variables](#environment-variables)
  - [Dependency Injection](#dependency-injection)
- [Development Workflow](#development-workflow)
  - [Git Workflow](#git-workflow)
  - [Development Scripts](#development-scripts)
  - [Code Quality](#code-quality)

---

## Overview

This document outlines the best practices, conventions, and standards for Java/Spring Boot backend applications. The backend follows Domain-Driven Design (DDD) principles and implements a layered architecture to ensure code consistency, maintainability, and scalability.

## Technology Stack

### Core Technologies
- **Java 21 (LTS)**: Runtime and language baseline for modern development
- **Spring Boot 3.x**: Application framework and autoconfiguration
- **Spring Web (MVC)**: REST API development
- **Maven 3.9+**: Build and dependency management

### Database & ORM
- **PostgreSQL**: Relational database
- **Spring Data JPA**: Data access abstraction
- **Hibernate ORM 6.x**: JPA implementation
- **Flyway or Liquibase**: Database migration tool

### Testing Framework
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration testing support
- **Coverage Threshold**: 90% for branches, functions, lines, and statements

### Development Tools
- **Checkstyle or Spotless**: Code style and formatting
- **PMD and SpotBugs**: Static analysis
- **JaCoCo**: Coverage reporting
- **Spring Boot Maven Plugin**: Packaging and runtime support

## Architecture Overview

### Domain-Driven Design (DDD)

Domain-Driven Design is a methodology that focuses on modeling software according to business logic and domain knowledge. By centering development on a deep understanding of the domain, DDD facilitates the creation of complex systems.

**Benefits:**
- **Improved Communication**: Promotes a common language between developers and domain experts, improving communication and reducing interpretation errors.
- **Clear Domain Models**: Helps build models that accurately reflect business rules and processes.
- **High Maintainability**: By dividing the system into subdomains, it facilitates maintenance and software evolution.

### Layered Architecture

The backend follows a layered DDD architecture:

**Presentation Layer** (`controller`)
- Controllers handle HTTP requests/responses
- Endpoints are defined with clear API contracts
- Controllers must remain thin and delegate business logic

**Application Layer** (`service` + `util`)
- Services contain business logic and orchestration
- Utility components support reusable cross-cutting operations
- Transaction boundaries are defined at service methods

**Domain Layer** (`entity` + `dto`)
- Entities define core domain concepts and persistence identity
- DTOs represent request/response and cross-layer contracts
- Domain rules stay close to domain concepts

**Persistence Layer** (`repository`)
- Repository interfaces define data access contracts
- Spring Data JPA repositories encapsulate persistence operations
- JPA mappings are explicit and consistent

### Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/app/
│   │   │       ├── controller/      # HTTP request handlers
│   │   │       ├── service/         # Business logic services
│   │   │       ├── util/            # Utility and helper components
│   │   │       ├── repository/      # Spring Data JPA repositories
│   │   │       ├── entity/          # JPA entities
│   │   │       ├── dto/             # API and service DTOs
│   │   │       ├── config/          # Spring configuration
│   │   │       ├── exception/       # Custom exceptions and handlers
│   │   │       └── Application.java # Application entry point
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-pre.yml
│   │       ├── application-prd.yml
│   │       └── db/migration/        # Flyway migrations
│   └── test/
│       └── java/com/example/app/    # Unit and integration tests
├── pom.xml
├── mvnw
└── Dockerfile
```

## Domain-Driven Design Principles

### Entities

Entities are objects with a distinct identity that persists over time.

**Before:**
```java
Map<String, Object> candidate = Map.of(
    "id", 1L,
    "firstName", "John",
    "lastName", "Doe",
    "email", "john.doe@example.com"
);
```

**After:**
```java
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    public void validateEmail() {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}
```

**Explanation**: `Candidate` is an entity because it has a unique identifier (`id`) that distinguishes it from other candidates, even if other properties are identical.

**Best Practice**: Entities should encapsulate business logic related to their domain concept and maintain consistency of their internal state.

### Value Objects

Value Objects describe aspects of the domain without conceptual identity. They are defined by their attributes rather than an identifier.

**Before:**
```java
Map<String, Object> education = Map.of(
    "institution", "University",
    "degree", "Bachelor",
    "startDate", LocalDate.of(2010, 1, 1),
    "endDate", LocalDate.of(2014, 1, 1)
);
```

**After:**
```java
@Embeddable
public class Education {

    @Column(name = "institution", nullable = false)
    private String institution;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
```

**Explanation**: `Education` can be treated as a Value Object when it has no independent lifecycle and only makes sense within the aggregate boundary.

**Recommendation**: Prefer `@Embeddable` and immutable design for Value Objects. Use entity identity only when independent lifecycle or cross-aggregate references are required.

### Aggregates

Aggregates are clusters of objects that must be treated as a unit. They have a root entity that enforces invariants and consistency boundaries.

**Before:**
```java
Candidate candidate = candidateRepository.findById(1L).orElseThrow();
List<EducationRecord> educations = educationRepository.findByCandidateId(1L);
```

**After:**
```java
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "candidate_educations", joinColumns = @JoinColumn(name = "candidate_id"))
    private List<Education> educations = new ArrayList<>();

    public void addEducation(Education education) {
        this.educations.add(education);
    }
}
```

**Explanation**: `Candidate` acts as an aggregate root that controls consistency for associated concepts such as education or work history.

**Recommendation**: Operations that affect child members inside an aggregate should be executed through the aggregate root in the service layer.

### Repositories

Repositories provide interfaces for accessing aggregates and entities, encapsulating data access logic.

**Before:**
```java
public Candidate loadCandidate(Long id) {
    return entityManager.createQuery("select c from Candidate c where c.id = :id", Candidate.class)
        .setParameter("id", id)
        .getSingleResult();
}
```

**After:**
```java
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);
}

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public Optional<Candidate> findById(Long id) {
        return candidateRepository.findById(id);
    }
}
```

**Explanation**: `CandidateRepository` provides a clear contract for candidate access while the service layer coordinates business behavior.

**Recommendation**:
- Define repository contracts per aggregate root
- Keep shared repositories in shared modules in multi-module projects
- Place custom queries behind repository interfaces and avoid persistence logic in controllers

### Domain Services

Domain Services contain business logic that doesn't naturally belong to an entity or value object.

**Before:**
```java
public int calculateAge(LocalDate birthDate) {
    return Period.between(birthDate, LocalDate.now()).getYears();
}
```

**After:**
```java
@Service
public class CandidateDomainService {

    public int calculateAge(Candidate candidate) {
        return Period.between(candidate.getBirthDate(), LocalDate.now()).getYears();
    }
}
```

**Explanation**: `CandidateDomainService` encapsulates domain operations that involve business rules crossing entity boundaries.

### Additional Recommendations

**Use of Factories**

Factories are useful in DDD to encapsulate the logic of creating complex objects, ensuring that all created objects comply with domain rules from the moment of creation.

**Recommendation**: Implement factories for creation of entities and aggregates when constructors become complex or invariants require orchestration.

**Improvement in Relationship Modeling**

Relationships between entities and aggregates must be clear and consistent with business rules.

**Recommendation**: Design JPA associations intentionally (`@OneToMany`, `@ManyToOne`, `@OneToOne`) with clear ownership, fetch strategy, and cascade behavior.

**Domain Events Integration**

Domain events are an important part of DDD and can be used to handle side effects of domain operations in a decoupled manner.

**Recommendation**: Implement domain event publication through Spring events or messaging infrastructure to decouple side effects from core use cases.

## SOLID and DRY Principles

### SOLID Principles

SOLID principles are five object-oriented design principles that help create more understandable, flexible, and maintainable systems.

#### Single Responsibility Principle (SRP)

Each class should have a single responsibility or reason to change.

**Before:**
```java
public class CandidateProcessor {

    public void process(Candidate candidate) {
        if (candidate.getEmail() == null || !candidate.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        candidateRepository.save(candidate);
        emailClient.sendCreatedNotification(candidate);
    }
}
```

**After:**
```java
public class CandidateValidator {
    public void validate(Candidate candidate) {
        if (candidate.getEmail() == null || !candidate.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

@Service
public class CandidateService {

    private final CandidateValidator validator;
    private final CandidateRepository repository;

    public CandidateService(CandidateValidator validator, CandidateRepository repository) {
        this.validator = validator;
        this.repository = repository;
    }

    public Candidate create(Candidate candidate) {
        validator.validate(candidate);
        return repository.save(candidate);
    }
}
```

**Explanation**: Validation and persistence responsibilities are separated, improving maintainability and testability.

#### Open/Closed Principle (OCP)

Software entities should be open for extension but closed for modification.

**Before:**
```java
public class NotificationService {
    public void send(String channel, String message) {
        if ("EMAIL".equals(channel)) {
            // email
        } else if ("SMS".equals(channel)) {
            // sms
        }
    }
}
```

**After:**
```java
public interface NotificationChannel {
    void send(String message);
}

@Component
public class EmailNotificationChannel implements NotificationChannel {
    public void send(String message) {
        // email
    }
}

@Component
public class SmsNotificationChannel implements NotificationChannel {
    public void send(String message) {
        // sms
    }
}
```

**Explanation**: New channels are introduced through extension, without changing existing implementations.

#### Liskov Substitution Principle (LSP)

Objects of a derived class should be replaceable with objects of the base class without altering the program's functionality.

**Before:**
```java
public class TemporaryCandidateService extends CandidateService {
    @Override
    public Candidate create(Candidate candidate) {
        throw new UnsupportedOperationException("Not supported");
    }
}
```

**After:**
```java
public class TemporaryCandidateService extends CandidateService {
    @Override
    public Candidate create(Candidate candidate) {
        // Stores in a temporary persistence boundary with same contract
        return super.create(candidate);
    }
}
```

**Explanation**: Derived services must keep contract behavior and remain safely substitutable.

#### Interface Segregation Principle (ISP)

Many specific interfaces are better than a single general interface.

**Before:**
```java
public interface CandidateOperations {
    Candidate save(Candidate candidate);
    Candidate update(Candidate candidate);
    void sendEmail(Candidate candidate);
    byte[] generateReport(Long candidateId);
}
```

**After:**
```java
public interface CandidatePersistence {
    Candidate save(Candidate candidate);
    Candidate update(Candidate candidate);
}

public interface CandidateNotification {
    void sendEmail(Candidate candidate);
}

public interface CandidateReport {
    byte[] generateReport(Long candidateId);
}
```

**Explanation**: Segregated interfaces reduce accidental coupling and simplify implementation/testing.

#### Dependency Inversion Principle (DIP)

High-level modules should not depend on low-level modules; both should depend on abstractions.

**Before:**
```java
@Service
public class CandidateService {

    private final CandidateJpaRepository candidateJpaRepository = new CandidateJpaRepository();

    public Candidate create(Candidate candidate) {
        return candidateJpaRepository.save(candidate);
    }
}
```

**After:**
```java
public interface CandidateGateway {
    Candidate save(Candidate candidate);
}

@Service
public class CandidateService {

    private final CandidateGateway candidateGateway;

    public CandidateService(CandidateGateway candidateGateway) {
        this.candidateGateway = candidateGateway;
    }

    public Candidate create(Candidate candidate) {
        return candidateGateway.save(candidate);
    }
}
```

**Explanation**: Services depend on abstractions, making persistence technology replaceable and testing easier.

### DRY (Don't Repeat Yourself)

The DRY principle focuses on reducing duplication in code. Each piece of knowledge should have a single, unambiguous, and authoritative representation within a system.

**Before:**
```java
public void createCandidate(Candidate candidate) {
    if (candidate.getEmail() == null || !candidate.getEmail().contains("@")) {
        throw new IllegalArgumentException("Invalid email");
    }
    candidateRepository.save(candidate);
}

public void updateCandidate(Candidate candidate) {
    if (candidate.getEmail() == null || !candidate.getEmail().contains("@")) {
        throw new IllegalArgumentException("Invalid email");
    }
    candidateRepository.save(candidate);
}
```

**After:**
```java
public class CandidateRules {

    public static void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

@Service
public class CandidateService {

    public Candidate create(Candidate candidate) {
        CandidateRules.validateEmail(candidate.getEmail());
        return candidateRepository.save(candidate);
    }

    public Candidate update(Candidate candidate) {
        CandidateRules.validateEmail(candidate.getEmail());
        return candidateRepository.save(candidate);
    }
}
```

**Explanation**: Validation rules are centralized and reused across operations.

## Coding Standards

### Naming Conventions

- **Variable Naming**: Use camelCase for variables and methods (for example, `candidateId`, `findCandidateById`)
- **Class Naming**: Use PascalCase for classes and interfaces (for example, `Candidate`, `CandidateRepository`)
- **Constants Naming**: Use UPPER_SNAKE_CASE for constants (for example, `MAX_CANDIDATES_PER_PAGE`)
- **Package Naming**: Use lowercase package names grouped by layer and bounded context
- **File Naming**: Java file names must match public class names

**Examples:**

```java
@Service
public class CandidateRepositoryAdapter {

    private final CandidateRepository candidateRepository;

    public CandidateRepositoryAdapter(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public Optional<Candidate> findById(Long candidateId) {
        return candidateRepository.findById(candidateId);
    }
}
```

**Error Messages and Logs:**

```java
throw new ResourceNotFoundException("Candidate not found with the provided ID");
log.error("Failed to create candidate", ex);
```

### Java Usage

- **Java Usage**: Use modern Java language features compatible with the project baseline
- **Strong Typing**: Use explicit return types in public APIs and avoid raw types
- **DTO Contracts**: Define DTOs for API boundaries and avoid exposing JPA entities directly
- **Null Safety**: Use Bean Validation, `Optional`, and clear nullability rules

```java
public Optional<CandidateDto> findCandidateById(Long id) {
    return candidateRepository.findById(id).map(candidateMapper::toDto);
}
```

### Error Handling

- **Custom Exception Classes**: Create domain-specific and application-specific exceptions
- **Global Exception Handler**: Use `@RestControllerAdvice` for consistent error responses
- **Error Messages**: Provide descriptive messages and stable machine-readable codes

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(false, "NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
```

### Validation Patterns

- **Input Validation**: Validate all inputs at the API boundary
- **Use Bean Validation**: Use `jakarta.validation` annotations (`@NotNull`, `@Email`, `@Size`)
- **Validate Before Processing**: Always validate before executing business logic

```java
public record CreateCandidateRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email
) {}

@PostMapping
public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CreateCandidateRequest request) {
    CandidateResponse response = candidateService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Logging Standards

- **Use SLF4J**: Use SLF4J API with Logback (default in Spring Boot)
- **Log Levels**: Use appropriate levels (`info`, `warn`, `error`, `debug`)
- **Structured Logging**: Include correlation IDs, request IDs, and business keys when possible

```java
private static final Logger log = LoggerFactory.getLogger(CandidateService.class);

log.info("Candidate created id={} email={}", candidate.getId(), candidate.getEmail());
log.error("Failed to create candidate", ex);
```

## API Design Standards

### REST Endpoints

- **RESTful Naming**: Use RESTful conventions for endpoint naming
- **HTTP Methods**: Use appropriate HTTP methods (GET, POST, PUT, DELETE, PATCH)
- **Resource-Based URLs**: URLs should represent resources, not actions

```text
GET    /candidates          # List candidates
GET    /candidates/{id}     # Get candidate by ID
POST   /candidates          # Create new candidate
PUT    /candidates/{id}     # Update candidate
DELETE /candidates/{id}     # Delete candidate
```

### Request/Response Patterns

- **JSON Format**: Use JSON for request and response bodies
- **Consistent Structure**: Maintain consistent response structure across all endpoints
- **Status Codes**: Use appropriate HTTP status codes

```json
{
  "success": true,
  "data": {},
  "messageCode": "SUCCESS",
  "message": "Operation completed successfully"
}
```

### Error Response Format

- **Consistent Format**: All errors should follow the same response structure
- **Error Codes**: Use meaningful error codes for different error types
- **HTTP Status Codes**: Map errors to appropriate HTTP status codes

```json
{
  "success": false,
  "error": {
    "message": "Validation failed",
    "code": "VALIDATION_ERROR",
    "details": []
  }
}
```

### CORS Configuration

- **Enable CORS Explicitly**: Configure CORS only for allowed origins
- **Secure Configuration**: Use environment-specific origins
- **Credentials**: Enable credentials only when strictly needed

```java
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                    .allowCredentials(true);
            }
        };
    }
}
```

## Database Patterns

### JPA Entity Model

- **Single Source of Truth**: JPA entities and migration scripts are the source of truth for database structure
- **Relationships**: Define relationships with explicit JPA annotations
- **Naming Conventions**: Use consistent conventions for tables, columns, indexes, and constraints

### Migrations

- **Version Control**: All schema changes must be version-controlled through migrations
- **Migration Naming**: Use descriptive migration names and version prefixes
- **Review Migrations**: Review migration files before applying

```bash
# Flyway
mvn -Dflyway.locations=filesystem:src/main/resources/db/migration flyway:migrate

# Liquibase
mvn liquibase:update
```

### Repository Pattern

- **Repository Interfaces**: Define repository interfaces in the persistence boundary
- **Spring Data Implementation**: Use Spring Data JPA and custom repository fragments when needed
- **Entity Scan Alignment**: In multi-module projects, align entity and repository scanning configuration

```java
@EnableJpaRepositories(basePackages = "com.example.app.repository")
@EntityScan(basePackages = "com.example.app.entity")
@SpringBootApplication
public class Application {
}
```

## Testing Standards

The project has strict requirements for code quality and maintainability. These are the unit testing standards and best practices that must be applied.

### Test File Structure
- Use descriptive test file names: `[ComponentName]Test.java`
- Place unit tests under `src/test/java` with mirrored package structure
- Use JUnit 5 with Mockito for unit tests
- Maintain 90% coverage threshold for branches, functions, lines, and statements

### Test Organization Pattern
Template:
```java
@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @BeforeEach
    void setUp() {
        Mockito.reset(candidateRepository);
    }

    @Test
    void should_return_candidate_when_found() {
        // Arrange

        // Act

        // Assert
    }
}
```

Real example:
```java
@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private CandidateService candidateService;

    @Test
    void shouldReturnCandidateWhenFound() {
        Long candidateId = 1L;
        Candidate candidate = new Candidate();
        when(candidateRepository.findById(candidateId)).thenReturn(Optional.of(candidate));

        Optional<Candidate> result = candidateService.findById(candidateId);

        assertTrue(result.isPresent());
        verify(candidateRepository).findById(candidateId);
    }
}
```

### Test Case Naming Convention
- Use descriptive naming: `shouldExpectedBehaviorWhenCondition`
- Group related test cases under descriptive test classes and nested tests
- Prefer expressive method names over generic names

### Test Structure (AAA Pattern)
Always follow the Arrange-Act-Assert pattern:
```java
@Test
void shouldUpdateCandidateStageSuccessfullyWhenValidDataProvided() {
    // Arrange
    Long candidateId = 1L;
    Integer newInterviewStep = 2;

    // Act
    CandidateResponse result = candidateService.updateStage(candidateId, newInterviewStep);

    // Assert
    assertEquals(newInterviewStep, result.interviewStep());
}
```

Assertion pattern:
- Use specific assertions and verifications
- Verify both successful operations and error conditions
- Check that mocks were called with correct parameters
- Assert return values and side effects

### Mocking Standards

- Mock all external dependencies (repositories, clients, gateways)
- Mock repository layer in service tests
- Mock service layer in controller tests (`@WebMvcTest` + `@MockBean`)
- Reset mocks in setup methods to ensure test isolation

### Test Coverage Requirements

- **Comprehensive test coverage**: Include these test categories for each function:
1. **Happy Path Tests**: Valid inputs producing expected outputs
2. **Error Handling Tests**: Invalid inputs, missing data, persistence errors
3. **Edge Cases**: Boundary values, null inputs, empty data
4. **Validation Tests**: Input validation and business rule enforcement
5. **Integration Points**: External service calls and database operations

- **Threshold**: 90% for branches, functions, lines, and statements
- **Coverage Reports**: Generate coverage reports with Maven JaCoCo plugin
- **Coverage Files**: Coverage reports in `target/site/jacoco/` adding dated summaries like `YYYYMMDD-backend-coverage.md`

### Error Testing
- Test both expected and unexpected errors
- Verify error messages are descriptive and useful
- Test error propagation through service layers
- Ensure proper HTTP status codes in controller tests

### Controller Testing Specifics
- Mock the service layer completely
- Test HTTP request/response handling with MockMvc
- Verify parameter parsing and validation
- Test error response formatting

### Service Testing Specifics
- Mock repositories and gateways
- Test business logic in isolation
- Verify data transformation and validation
- Test error handling and edge cases

### Database Testing
- Use `@DataJpaTest` or Testcontainers-based integration tests
- Test both successful and failed database operations
- Verify query behavior and constraints
- Test transaction behavior and rollback scenarios

### Async Testing
- Use `CompletableFuture` for async operations when required
- Use deterministic async tests and explicit timeouts
- Test timeout and failure scenarios where applicable

### Test Data Management
- Use factory methods/builders for creating test data
- Keep test data consistent and realistic
- Avoid hardcoded duplicated values
- Use meaningful scenarios aligned with domain behavior

### Integration Testing

- **Controller Testing**: Test HTTP request/response handling
- **Database Testing**: Test repository implementations with real database containers
- **End-to-End Flow**: Test complete request flows for critical paths

### Code Quality Standards

#### Java Usage
- **Java Usage**: Use strict typing, generics, and clear contracts in tests
- Define proper classes/records for mock data
- Use type casting sparingly and with proper justification
- Leverage compiler checks for better test reliability

#### Documentation
- Write clear, descriptive test names that explain the scenario
- Add comments only for complex test setups
- Document special conditions or edge cases when needed
- Keep test code as readable as production code

#### Performance Considerations
- Keep tests fast and focused
- Avoid unnecessary async operations in tests
- Use mock strategies to avoid real network I/O in unit tests
- Group related tests to reduce setup/teardown overhead

### Integration with Development Workflow
- Run tests before every commit
- Ensure all tests pass before merging
- Use test-driven development when appropriate
- Update tests when modifying existing functionality

### Common Anti-Patterns to Avoid
- Do not test implementation details, test behavior
- Do not create overly complex test setups
- Do not ignore failing tests or skip error scenarios
- Do not use real external services in unit tests
- Do not write tests tightly coupled to implementation details

### Example Test Structure



## Performance Best Practices

### Database Query Optimization

- **Select Specific Fields**: Fetch only required columns
- **Use Indexes**: Ensure proper indexes for frequently queried fields
- **Avoid N+1 Queries**: Use entity graphs, fetch joins, or batch strategies

```java
@Query("select c from Candidate c left join fetch c.educations where c.id = :id")
Optional<Candidate> findByIdWithEducations(@Param("id") Long id);
```

### Asynchronous Patterns

- **Async in Java**: Use `CompletableFuture` and `@Async` only when there is a clear benefit
- **Error Handling**: Handle exceptions in async pipelines (`exceptionally`, `handle`)
- **Parallel Operations**: Use async composition for independent operations

```java
CompletableFuture<List<Candidate>> candidatesFuture = candidateService.findAllAsync();
CompletableFuture<List<Position>> positionsFuture = positionService.findAllAsync();
CompletableFuture.allOf(candidatesFuture, positionsFuture).join();
```

### Error Handling Performance

- **Early Returns**: Validate and return early to avoid unnecessary processing
- **Error Propagation**: Preserve root causes when rethrowing exceptions
- **Avoid Over-Wrapping**: Keep exception wrappers meaningful and minimal

## Security Best Practices

### Input Validation

- **Validate All Inputs**: Validate all user inputs before processing
- **Sanitize Data**: Protect against injection attacks and unsafe deserialization
- **Type Checking**: Use DTO validation and strong typing

### Environment Variables

- **Never Commit Secrets**: Never commit credentials or secrets to version control
- **Use Environment Variables**: Externalize configuration using profiles and env vars
- **Validate Environment**: Validate required properties at startup

```java
@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
    @NotBlank String databaseUrl,
    @NotBlank String jwtSecret
) {}
```

### Dependency Injection

- **Use Spring DI**: Use dependency injection for services, repositories, and infrastructure adapters
- **Avoid Manual Wiring**: Do not instantiate dependencies with `new` in business classes
- **Testability**: Use DI to improve testability and replace dependencies in tests

```java
@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }
}
```

## Development Workflow

### Git Workflow

- **Feature Branches**: Develop features in separate branches using clear descriptive names
- **Descriptive Commits**: Write descriptive commit messages in English
- **Code Review**: Perform code review before merging
- **Small Branches**: Keep branches small and focused

### Development Scripts

```bash
./mvnw spring-boot:run          # Development server
./mvnw clean package            # Build artifact
./mvnw test                     # Run tests
./mvnw verify                   # Run full verification
./mvnw jacoco:report            # Generate coverage report
./mvnw flyway:migrate           # Apply Flyway migrations
./mvnw spotless:apply           # Apply formatting rules
```

### Code Quality

- **Static Analysis Validation**: Run Checkstyle/PMD/SpotBugs before commits
- **Compilation Check**: Ensure compilation without warnings that indicate defects
- **All Tests Passing**: Ensure all tests pass before deployment
- **Code Review**: Review code for adherence to standards and architecture rules

This document serves as the foundation for maintaining code quality and consistency across Java/Spring Boot backend applications. All team members should follow these practices to ensure a maintainable, scalable, and testable codebase.
