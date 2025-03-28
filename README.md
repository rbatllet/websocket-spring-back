# WebSocket Spring Backend

This is the backend part of the WebSocket Demo application, built with Spring Boot.

## Features

- WebSocket server implementation using Spring's WebSocket support
- Structured message handling with a ChatMessage model
- Support for different message types (CHAT, JOIN, LEAVE, etc.)
- Username management and tracking
- User count broadcasting
- Message broadcasting to all connected clients
- Ping/pong communication mechanism
- Support for standalone, integrated, and Docker deployment modes
- Integrated static resource serving for the Vue.js frontend

## Technical Specifications

- Java 21
- Spring Boot 3.4
- Spring WebSocket
- Lombok for code simplification
- Jackson for JSON serialization/deserialization

## Project Structure

- `model/ChatMessage.java` - Structured message model with message types (using Lombok)
- `ChatMessageHandler.java` - WebSocket message handling and broadcasting
- `WebSocketConfig.java` - WebSocket configuration
- `WebsocketSpringBackApplication.java` - Main application class
- `FrontendController.java` - Controller for serving the frontend
- `config/CorsConfig.java` - CORS configuration for development mode

## Running the Application

### Development Mode (Standalone)

```sh
# Run with API context path for standalone mode
API_CONTEXT_PATH=/api ./mvnw spring-boot:run
```

In development mode, the backend runs on `http://localhost:8080/api` and expects the frontend to run separately.

### Production Mode (with integrated frontend)

First, build the frontend:

```sh
cd ../websocket-vue-front
npm run dist
```

Then run the backend without context path:

```sh
cd ../websocket-spring-back
API_CONTEXT_PATH= ./mvnw spring-boot:run
```

In this mode, the application serves both the API and the frontend from the same server.

### Docker Mode

From the project root directory:

```sh
# Default (no context path)
./build-docker.sh
docker-compose up

# Or with a context path
./build-docker.sh latest "/api"
docker-compose up
```

This builds and runs the entire application in a Docker container with Java 21. The application can run either with or without a context path based on the build parameters.

## Static Resources Management

The application is configured to serve the Vue.js frontend from the `src/main/resources/static` directory. This directory is:

- Excluded from git tracking (via `.gitignore`) except for a `.gitkeep` file
- Automatically cleaned before each frontend build by build scripts
- Configured to serve content at the root path (`/`)

The cleaning process:
```bash
# From build scripts
find "$STATIC_DIR" -type f -not -name ".gitkeep" -delete
find "$STATIC_DIR" -type d -empty -delete
```

This ensures that no stale files remain between builds and prevents potential conflicts or outdated resources being served.

## Building for Production

```sh
./mvnw clean package
```

This produces a standalone JAR file in the `target` directory.

## Configuration

Configuration is in:

- `src/main/resources/application.properties` - Standard configuration
- `src/main/resources/application-docker.properties` - Docker-specific configuration

Key configuration properties:

- `server.port` - HTTP server port (default: 8080)
- `server.servlet.context-path` - API base path (configurable via `API_CONTEXT_PATH` environment variable)
- `websocket.endpoint` - WebSocket endpoint path (default: /chat)
- `websocket.allowed-origins` - Allowed origins for CORS
- `app.name` - Application name used in welcome messages
- `spring.web.resources.static-locations` - Location of static resources
- `spring.mvc.static-path-pattern` - URL pattern for static resources
- `websocket.container.max-text-message-buffer-size` - WebSocket buffer size for text messages
- `websocket.container.max-binary-message-buffer-size` - WebSocket buffer size for binary messages
- `websocket.container.max-session-idle-timeout` - WebSocket session timeout

## ChatMessage Model

The application uses a structured ChatMessage model with Lombok:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String name;            // Sender name
    private String message;         // Message content
    private String timestamp;       // ISO timestamp
    private MessageType type;       // Message type enum
    
    public enum MessageType {
        CHAT, JOIN, LEAVE, ERROR, USER_COUNT, PING, PONG
    }
    
    // Static factory methods using Builder pattern...
}
```

This model provides:
- Type safety with the MessageType enum
- Builder pattern for creating messages
- Automatic getters/setters via Lombok
- Factory methods for creating different message types
- Timestamp tracking
- Standard JSON serialization/deserialization

## WebSocket Implementation

The application uses Spring's WebSocket support:

- `WebSocketConfig` configures the WebSocket endpoints at both paths:
  - `/chat` - For standalone mode
  - `/api/chat` - For integrated and Docker modes

- `ChatMessageHandler` provides:
  - Connection tracking
  - Username management
  - Message broadcasting
  - Ping/pong handling
  - User join/leave notifications
  - User count broadcasting

## Docker Support

The application includes Docker support via the Dockerfile in the project root:

1. A single build stage installs both Maven and Node.js
2. The Vue.js frontend is built directly into the Spring Boot's static resources directory
3. The Spring Boot application is built with the integrated frontend
4. A final lightweight runtime image with Java 21 is created for deployment

This approach simplifies the build process by:
1. Avoiding complex file copying between build stages
2. Ensuring reliable builds with direct integration of frontend into backend
3. Maintaining a clean static directory structure
4. Providing flexibility for different context path configurations

To run the application in Docker:

```bash
# From the project root, with no context path
./build-docker.sh
docker-compose up

# Or with a context path
./build-docker.sh latest "/api"
docker-compose up
```

Docker-specific configuration is activated via the Spring profile `docker` in `application-docker.properties`.
