# ChatApp

A real-time public chat room built with Spring Boot and WebSocket.

## Features

- Real-time messaging via WebSocket (STOMP over SockJS)
- Message history — last 50 messages loaded on join
- Online users sidebar with live presence tracking
- Typing indicators
- Browser push notifications for incoming messages
- Clear history button
- Dark / light mode toggle
- Persistent storage in MySQL

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4, Spring WebSocket, Spring Data JPA |
| Database | MySQL |
| Frontend | Vanilla JS, SockJS, STOMP.js |
| Font | Inter (Google Fonts) |

## Prerequisites

**Local development**
- Java 21+
- MySQL running on `localhost:3306`
- Gradle (wrapper included)

**Docker**
- Docker + Docker Compose

## Getting Started

**1. Configure environment variables**

Copy the example env file and fill in your MySQL credentials:

```bash
cp .env.example .env
```

`.env.example`:
```env
DB_URL=jdbc:mysql://localhost:3306/chatapp?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

The Gradle `bootRun` task loads `.env` automatically. The database and `messages` table are created on first run.

**2. Run the application**

```bash
./gradlew bootRun
```

**3. Open the app**

Navigate to [http://localhost:8080](http://localhost:8080), enter a username, and start chatting.

## Docker

**1. Set credentials in `.env`**

```bash
cp .env.example .env
# fill in DB_USERNAME and DB_PASSWORD
```

**2. Build and start both services**

```bash
docker compose up --build
```

This starts:
- `db` — MySQL 8 with a persistent named volume (`mysql_data`)
- `app` — the Spring Boot app, waits for MySQL to be healthy before starting

> `docker-compose.yml` overrides `DB_URL` automatically to point to the `db` service instead of `localhost`, so no manual change is needed.

**3. Stop and remove containers**

```bash
docker compose down
```

To also remove the database volume:

```bash
docker compose down -v
```

## Project Structure

```
src/main/
├── java/com/example/chatapp/
│   ├── controller/
│   │   ├── ChatController.java          # WebSocket message handlers
│   │   └── MessageRestController.java   # REST API (GET/DELETE /api/messages)
│   ├── model/
│   │   └── ChatMessage.java             # JPA entity (CHAT, JOIN, LEAVE, TYPING)
│   ├── service/
│   │   └── MessageService.java          # Business logic
│   ├── repository/
│   │   └── MessageRepository.java       # Spring Data JPA repository
│   ├── config/
│   │   └── WebSocketConfig.java         # STOMP broker configuration
│   └── websocket/
│       ├── UserSessionRegistry.java     # Tracks online users
│       └── WebSocketEventListener.java  # Connect / disconnect events
└── resources/
    ├── static/
    │   ├── index.html                   # UI
    │   ├── main.js                      # Frontend logic
    │   └── style.css                    # Styles
    └── application.properties
```

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/messages` | Returns the last 50 chat messages |
| `DELETE` | `/api/messages` | Deletes all messages |

## WebSocket Topics

| Destination | Direction | Description |
|-------------|-----------|-------------|
| `/app/chat.sendMessage` | Client → Server | Send a chat message |
| `/app/chat.addUser` | Client → Server | Announce joining |
| `/app/chat.typing` | Client → Server | Send typing event |
| `/topic/public` | Server → Clients | Broadcast messages |
| `/topic/typing` | Server → Clients | Broadcast typing events |
| `/topic/users` | Server → Clients | Broadcast online user list |
