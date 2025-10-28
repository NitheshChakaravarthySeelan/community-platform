- `AuthServiceApplication.java`: This is the main entry point for your Spring Boot application.
- `config/`: This is an excellent place to put all your configuration classes. For authentication, this would include:
  - JWT Configuration: Defines how JWTs are signed, their expiration times, and the secret key used.
  - Security Configuration: Configures Spring Security (if used) to define endpoint access rules, password encoders, and custom
    authentication filters.
- `domain/`: This is where your core business entities and logic reside. For authentication, this should contain:
  - User Entity/Model: The Java class representing your User (e.g., User.java) with fields like id, email, passwordHash, roles.
  - User Repository Interface: An interface (e.g., UserRepository.java) defining methods for database operations on the User entity (e.g.,
    findByEmail, save).
- `infrastructure/`: This layer provides the concrete implementations for interfaces defined in the domain, connecting to external services
  like databases.
  - User Repository Implementation: The concrete class implementing your UserRepository interface, using Spring Data JPA or another ORM to
    interact with the database.
  - JWT Utility Implementation: A class that handles the actual creation and validation of JWTs using a library like JJWT.
  - Password Encoder Implementation: A class that wraps your chosen password hashing library (e.g., BCryptPasswordEncoder from Spring
    Security).
- `application/`: This layer typically contains application services or use cases that orchestrate interactions between the domain and
  infrastructure layers.
  - Authentication Service: A class (e.g., AuthService.java) that encapsulates the core logic for user registration, login, and token
    validation. It would use the UserRepository, JWT utilities, and password encoder.
- `interfaces/`: This layer deals with external communication, primarily your REST controllers.
  - Authentication Controller: A REST controller (e.g., AuthController.java) that exposes your /register, /login, /validate, and
    potentially /refresh-token endpoints. It would call methods from your AuthService.

---

Detailed Steps for Coding `auth-service` (Java/Spring Boot Focus):

1.  Configure `pom.xml`:
    - Action: Add necessary dependencies for a Spring Boot REST application, Spring Data JPA (if using relational DB), your chosen database
      driver (e.g., PostgreSQL driver), JJWT (for JWTs), and Spring Security (for password hashing and potentially advanced security
      features).
    - Reasoning: Provides the libraries your service will use.

2.  Define `User` Entity (`domain/User.java`):
    - Action: Create the User class with annotations for JPA (e.g., @Entity, @Id, @GeneratedValue, @Column) to map it to a database table.
    - Reasoning: This is your core user data model.

3.  Create `UserRepository` Interface (`domain/UserRepository.java`):
    - Action: Define an interface that extends JpaRepository<User, Long> (or MongoRepository, etc.) from Spring Data. Add custom methods like
      User findByEmail(String email);.
    - Reasoning: Spring Data will automatically provide implementations for CRUD operations and your custom query methods.

4.  Implement JWT Utility (`infrastructure/JwtService.java`):
    - Action: Create a service class.
    - Methods:
      - String generateToken(UserDetails userDetails): Takes user details (could be your User entity or a Spring Security UserDetails
        object) and generates a signed JWT. This needs to load the secret key from config.
      - Boolean validateToken(String token, UserDetails userDetails): Validates the token's signature, expiration, and ensures it matches
        the provided user details.
      - String extractUsername(String token): Extracts the username (email) from the token.
    - Reasoning: Centralizes JWT logic, making it reusable.

5.  Configure Password Encoder (`config/SecurityConfig.java`):
    - Action: Create a Spring @Configuration class. Define a @Bean method that returns a PasswordEncoder (e.g., BCryptPasswordEncoder).
    - Reasoning: Provides a globally available and secure password hashing mechanism.

6.  Implement `AuthService` (`application/AuthService.java`):
    - Action: Create a service class annotated with @Service. Autowire UserRepository, PasswordEncoder, and JwtService.
    - Methods:
      - User registerUser(String email, String plaintextPassword, List<String> roles):
        - Check if user exists via userRepository.findByEmail.
        - Hash the password using passwordEncoder.encode().
        - Create a new User entity.
        - Save the user via userRepository.save().
        - Return the created User.
      - String loginUser(String email, String plaintextPassword):
        - Retrieve user via userRepository.findByEmail.
        - Use passwordEncoder.matches() to compare plaintext password with stored hash.
        - If match, generate JWT via jwtService.generateToken().
        - Return the JWT. If no match/user not found, throw an AuthenticationException.
      - Map<String, Object> validateToken(String token): (Optional, depending on caller needs)
        - Extract username from token jwtService.extractUsername().
        - Load UserDetails (or your User entity) for that username.
        - Use jwtService.validateToken() with the token and user details.
        - If valid, return relevant user claims (e.g., user ID, roles) in a Map.
    - Reasoning: Encapsulates the main business logic for authentication.

7.  Implement `AuthController` (`interfaces/AuthController.java`):
    - Action: Create a REST controller annotated with @RestController and @RequestMapping("/auth"). Autowire AuthService.
    - Endpoints:
      - @PostMapping("/register"): Takes RegisterRequest DTO (Data Transfer Object) with email/password. Calls authService.registerUser().
        Returns success response.
      - @PostMapping("/login"): Takes LoginRequest DTO with email/password. Calls authService.loginUser(). Returns a LoginResponse DTO
        containing the JWT.
      - @GetMapping("/validate") or @PostMapping("/validate"): Takes JWT from Authorization header. Calls authService.validateToken().
        Returns status and user claims.
    - Reasoning: Exposes your authentication functionalities via RESTful API endpoints.

8.  Add Configuration Properties (`src/main/resources/application.properties` or `application.yml`):
    - Action: Store properties like your JWT secret key, token expiration, and database connection details.
    - Reasoning: Centralized configuration, easy to manage and change per environment.
