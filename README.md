[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![CI Pipeline](https://github.com/OpenDataJungle/OpenDataJungle-Conversation-Api/actions/workflows/ci.yml/badge.svg)](https://github.com/OpenDataJungle/OpenDataJungle-Conversation-Api/actions/workflows/ci.yml)

# OpenDataJungle Conversation API

A REST API for conversational AI. It manages conversations and their message history, routes chat requests to
configurable LLM providers (OpenAI-compatible or Ollama), and augments prompts with organization knowledge through
Retrieval-Augmented Generation (RAG) and MCP tool integrations.

Part of the [OpenDataJungle](https://www.opendatajungle.com) platform, alongside
the [Knowledge API](https://github.com/OpenDataJungle/OpenDataJungle-Knowledge-Api) it queries for semantic search and
resource content.

## Architecture

Hexagonal architecture (Ports & Adapters): `client` (REST controllers/DTOs) → `business` (domain services/models) →
`infra` (persistence, LLM clients, external integrations), enforced by ArchUnit tests.

## Features

- **Conversation management** — create, list, update, and delete conversations, each with its own title and system
  message.
- **Chat endpoint** — send a message to a conversation and receive an LLM-generated response, with full message history
  persisted.
- **Pluggable LLM providers** — configure multiple named models backed by OpenAI-compatible APIs or Ollama, selectable
  per chat request.
- **RAG-aware pre-processing pipeline** — a chain of pre-processors that can categorize attached resources, inject a
  default system prompt, and pull relevant content from the Knowledge API via semantic search before the LLM call.
- **MCP tool support** — dynamically expose Model Context Protocol tools to the LLM.
- **OAuth2 / JWT security** — endpoints are protected with scope-based authorization (`conversations.read`,
  `conversations.write`, `conversations.delete`, `conversations.admin`).
- **Observability** — Actuator health/info/metrics endpoints with Prometheus support out of the box.

## Tech stack

| Component | Technology                               |
|-----------|------------------------------------------|
| Framework | Spring Boot 4                            |
| Language  | Java 25                                  |
| AI        | Spring AI 2 — OpenAI, Ollama, MCP client |
| Database  | PostgreSQL                               |
| Security  | Spring Security, OAuth2 Resource Server  |
| Testing   | JUnit 5, Testcontainers, ArchUnit        |
| Metrics   | Micrometer + Prometheus                  |

## Getting started

### Prerequisites

- JDK 25
- Docker (for the local PostgreSQL database and integration tests)
- An OpenAI-compatible API key and/or a running Ollama instance
- An OAuth2/OIDC provider issuing JWTs (e.g. Keycloak) for authentication — not required in `local`/`test` profile,
  see [Security](#security)

### Run the database

```bash
cd infra/container
docker compose up -d
```

This starts a PostgreSQL instance with the `pgvector` extension on port `5432`.

### Run the application

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080` by default.

### Run the tests

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker for Testcontainers)
./mvnw verify -Pit
```

## Configuration

Configuration lives in `src/main/resources/application.yml` and is overridable via environment variables.

#### Application & server

| Variable              | Description         | Default                           |
|-----------------------|---------------------|-----------------------------------|
| `APPLICATION_TITLE`   | Application title   | `OpenDataJungle Conversation API` |
| `APPLICATION_VERSION` | Application version | `pom.xml` version                 |
| `SERVER_PORT`         | HTTP port           | `8080`                            |

#### Database

| Variable                                | Description            | Default                                             |
|-----------------------------------------|------------------------|-----------------------------------------------------|
| `DATABASE_URL`                          | PostgreSQL JDBC URL    | `jdbc:postgresql://localhost:5432/open_data_jungle` |
| `DATABASE_USERNAME`                     | PostgreSQL user        | `user`                                              |
| `DATABASE_PASSWORD`                     | PostgreSQL password    | `password`                                          |
| `DATABASE_DRIVER`                       | JDBC driver            | `org.postgresql.Driver`                             |
| `JPA_DDL_AUTO`                          | Hibernate DDL mode     | `none`                                              |
| `JPA_SHOW_SQL` / `HIBERNATE_FORMAT_SQL` | Log/format SQL queries | `false`                                             |
| `JPA_OPEN_IN_VIEW`                      | Open Session In View   | `false`                                             |

#### Security

| Variable                              | Description                              | Default                               |
|---------------------------------------|------------------------------------------|---------------------------------------|
| `JWT_ISSUER_URI`                      | OAuth2/OIDC issuer used to validate JWTs | `http://localhost:8090/realms/master` |
| `SECURITY_SCOPE_CONVERSATIONS_READ`   | Scope to read conversations              | `conversations.read`                  |
| `SECURITY_SCOPE_CONVERSATIONS_WRITE`  | Scope to create/update/chat              | `conversations.write`                 |
| `SECURITY_SCOPE_CONVERSATIONS_DELETE` | Scope to delete conversations            | `conversations.delete`                |
| `SECURITY_SCOPE_CONVERSATIONS_ADMIN`  | Scope to list all users' conversations   | `conversations.admin`                 |

#### CORS

| Variable                 | Description                        | Default                                                         |
|--------------------------|------------------------------------|-----------------------------------------------------------------|
| `CORS_ALLOWED_ORIGINS`   | Allowed origins                    | localhost dev ports                                             |
| `CORS_ALLOWED_METHODS`   | Allowed HTTP methods               | `GET,POST,PUT,PATCH,DELETE,OPTIONS`                             |
| `CORS_ALLOWED_HEADERS`   | Allowed headers                    | `Authorization,Content-Type,X-Requested-With,Accept,Origin,...` |
| `CORS_EXPOSED_HEADERS`   | Headers exposed to the client      | `Access-Control-Allow-Origin,Access-Control-Allow-Credentials`  |
| `CORS_ALLOW_CREDENTIALS` | Allow credentials                  | `false`                                                         |
| `CORS_MAX_AGE`           | Preflight cache duration (seconds) | `3600`                                                          |

#### LLM & MCP

| Variable                                               | Description                                                              | Default                       |
|--------------------------------------------------------|--------------------------------------------------------------------------|-------------------------------|
| `OPEN_DATA_JUNGLE_LLM_MODELS`                          | JSON map of named LLM model configs (provider, API key, base URL, model) | single `default` OpenAI model |
| `OPEN_DATA_JUNGLE_LLM_DEFAULT_PROVIDER_OPENAI_ENABLED` | Enable the built-in OpenAI provider                                      | `true`                        |
| `OPEN_DATA_JUNGLE_LLM_DEFAULT_PROVIDER_OLLAMA_ENABLED` | Enable the built-in Ollama provider                                      | `true`                        |
| `OPEN_DATA_JUNGLE_MCP_SERVERS`                         | JSON map of MCP server configs to expose as tools                        | `{}`                          |

`OPEN_DATA_JUNGLE_LLM_MODELS` is a map keyed by model id — `default` is required, `speed`/`categorizer`/`long-context`
are optional and used by the pre-processing pipeline when present. Any other key is only reachable via the `llm_model`
field of a chat request:

```json
{
  "default": {
    "provider": "openai",
    "apiKey": "sk-...",
    "baseUrl": "",
    "model": "gpt-4o",
    "name": "GPT-4o",
    "options": {
      "temperature": 0.7,
      "maxTokens": 4096
    }
  },
  "speed": {
    "provider": "ollama",
    "apiKey": "",
    "baseUrl": "http://localhost:11434",
    "model": "llama3.1",
    "name": "Llama 3.1 (local)",
    "options": {
      "temperature": 0.5,
      "topK": 40
    }
  }
}
```

`OPEN_DATA_JUNGLE_MCP_SERVERS` is a map keyed by server id. `type` is `sse` or `http`; `required` servers have their
tools always available, non-required ones only when listed in a chat request's `enabled_tools`:

```json
{
  "Context7": {
    "name": "Context7",
    "type": "http",
    "url": "https://mcp.context7.com/mcp",
    "required": false,
    "headers": {
      "CONTEXT7_API_KEY": "..."
    }
  }
}
```

#### Chat / RAG

| Variable                                                               | Description                                                                                                                                            | Default                 |
|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| `OPEN_DATA_JUNGLE_KNOWLEDGE_API_BASE_URL`                              | Base URL of the [Knowledge API](https://github.com/OpenDataJungle/OpenDataJungle-Knowledge-Api) (semantic search & resource content)                   | `http://localhost:8081` |
| `OPEN_DATA_JUNGLE_CHAT_MAX_CONTEXT_TOKENS`                             | Max tokens kept in the conversation context                                                                                                            | `100000`                |
| `OPEN_DATA_JUNGLE_CHAT_MAX_FILE_CONTENTS_SIZE`                         | Max tokens of resource content injected into the prompt                                                                                                | `5000`                  |
| `OPEN_DATA_JUNGLE_CHAT_PRE_PROCESSORS_DEFAULT_SYSTEM_PROMPT_ENABLED`   | Enable the default system prompt pre-processor                                                                                                         | `true`                  |
| `OPEN_DATA_JUNGLE_CHAT_PRE_PROCESSORS_RESOURCE_CATEGORIZATION_ENABLED` | Enable the resource-routing (between vector search (RAG) and direct file inclusion in the prompt) pre-processor                                        | `true`                  |
| `OPEN_DATA_JUNGLE_CHAT_PRE_PROCESSORS_ADD_RESOURCE_TO_PROMPT_ENABLED`  | Enable the resource-manager (conditional resource inclusion in the prompt and agent configuration based on the preceding categorization) pre-processor | `true`                  |

#### Validation

| Variable                             | Description                                 | Default |
|--------------------------------------|---------------------------------------------|---------|
| `VALIDATION_MESSAGE_MAX_SIZE`        | Max size of a chat message                  | `50000` |
| `VALIDATION_SYSTEM_MESSAGE_MAX_SIZE` | Max size of a conversation's system message | `8000`  |
| `VALIDATION_TITLE_MAX_SIZE`          | Max size of a conversation title            | `500`   |

#### HTTP client & logging

| Variable                                                                   | Description                                 | Default    |
|----------------------------------------------------------------------------|---------------------------------------------|------------|
| `HTTP_CLIENT_CONNECT_TIMEOUT_SECONDS` / `HTTP_CLIENT_READ_TIMEOUT_SECONDS` | Timeouts for outbound calls (Knowledge API) | `5` / `30` |
| `LOGGING_LEVEL_OPENDATAJUNGLE`                                             | Log level for OpenDataJungle packages       | `INFO`     |
| `LOGGING_LEVEL_SPRING_AI`                                                  | Log level for Spring AI                     | `INFO`     |
| `JACKSON_TIME_ZONE`                                                        | Jackson timezone                            | `UTC`      |

See `application.yml` for the full list, including the RAG pre-processor system prompts.

## Security

Endpoints require a JWT (OAuth2 Resource Server) with the scopes listed above.

Two Spring profiles disable authentication entirely and allow anonymous access—useful for standalone environments, local
development, and automated testing:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

- **`local`** — for running the API locally without an OAuth2 server.
- **`test`** — used automatically by the test suite (`@ActiveProfiles("test")`).

In production, no profile is active by default, so OAuth2/JWT authentication is enforced.

## API overview

All endpoints are under `/api/v1/conversations` and require a valid JWT with the appropriate scope (unless the `local`/
`test` profile is active, see [Security](#security)).

| Method   | Path             | Scope                  | Description                                  |
|----------|------------------|------------------------|----------------------------------------------|
| `POST`   | `/`              | `conversations.write`  | Create a conversation                        |
| `GET`    | `/`              | `conversations.read`   | List the current user's conversations        |
| `GET`    | `/{id}`          | `conversations.read`   | Get a conversation                           |
| `PATCH`  | `/{id}`          | `conversations.write`  | Update a conversation's title/system message |
| `POST`   | `/{id}/chat`     | `conversations.write`  | Send a chat message and get the LLM response |
| `GET`    | `/{id}/messages` | `conversations.read`   | List a conversation's message history        |
| `DELETE` | `/`              | `conversations.delete` | Delete one or more conversations             |
| `GET`    | `/admin`         | `conversations.admin`  | List all conversations (admin)               |

## Contact

- **Website:** [www.opendatajungle.com](https://www.opendatajungle.com)
- **Email:** [contact@opendatajungle.com](mailto:contact@opendatajungle.com)
- **Organization:** [github.com/OpenDataJungle](https://github.com/OpenDataJungle)

## License

Licensed under the [GNU General Public License v3.0](LICENSE).
