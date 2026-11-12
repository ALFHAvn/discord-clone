# Discord Clone Backend development plan

Build a Discord-like backend one feature at a time. Use each phase to practice skills that can be demonstrated in a backend internship interview, from tracing a REST request through Spring and PostgreSQL to testing, packaging, and deploying the application.

## Current checkpoint

- [x] Phase 0 skeleton is in place with a Maven wrapper, PostgreSQL configuration, and `GET /health`.
- [x] The User slice has `POST /api/users`, `GET /api/users`, and `GET /api/users/{id}`. The repository filename is correct, and the main and test sources compiled with Java 25 and Spring Boot 4.1.1.
- [x] Read the PostgreSQL password from `DISCORD_DB_PASSWORD` and document local setup in [README.md](README.md).
- [x] Verify the User endpoints and the `users` row against temporary local PostgreSQL 17.5. The Spring context test passed, and HTTP smoke checks covered health, create, get by ID, list, and password omission.
- [ ] Rotate the previously committed database password if it is still in use. The smoke check used a disposable database, so the existing local role password was left alone.

Use Java 25, Spring Boot 4.1.1, and PostgreSQL 17 for the current setup. The local database is `discord_clone` and the development role is `discord_user`. The Maven commands below use Windows PowerShell.

## Phase 1 — Core REST API

### 1. Finish the User slice

- [x] Establish a baseline against PostgreSQL. Check `/health`, create a dummy user, fetch it by ID, confirm it appears in the list, and check the row in `users`. Confirm that responses omit the password. Verified with PowerShell HTTP requests and `psql`. The [README](README.md#check-the-user-api) has the equivalent Postman steps.
- [x] Validate the request with `@Valid`, `@NotBlank`, and `@Email`. MVC tests confirm invalid requests return `400` before calling the service. A live check against temporary PostgreSQL confirmed that an invalid request saves no row and a valid request saves one.
- [x] Return `201 Created` with a `Location` header pointing to the new user. Map a missing user to `404` and duplicate usernames or emails to `409` through `@RestControllerAdvice`. A live PostgreSQL check confirmed that two concurrent requests with the same username and email create one row and return `201` and `409`.
- [x] Add profile update and deletion. Live PostgreSQL checks verified that updates preserve the password, duplicate updates leave the profile intact, and concurrent deletion requests return `204` and `404`.

User deletion returns `409` while the user owns a server or has authored messages. Successful deletion removes ordinary memberships in the same transaction.

**Done when** the User endpoints work against PostgreSQL, the success and error cases have been checked, and no response contains a password. Keep test data local and disposable while passwords are stored as plaintext.

Suggested first commit message `Read the database password from the environment`.

### 2. Add the Server and ServerMember slice

- [x] Model a server with a name and an owner. Store memberships in `server_members` with a unique constraint on the server-user pair.
- [x] Add `POST /api/servers` and `GET /api/servers/{id}`. Live PostgreSQL checks verified the owner membership, invalid and missing IDs, and rollback of server creation when the membership insert failed.
- [x] Apply the user deletion policy for server owners and ordinary memberships. Live checks confirmed that owners get `409` and that deleting an ordinary member removes their membership.
- [x] Add server list, rename, and delete endpoints. Live PostgreSQL checks verified listing in ID order, rename validation, and deletion of only the target server and its memberships. Concurrent server and ordinary-member deletions both succeeded.
- [x] Add, list, look up, and remove members under `/api/servers/{serverId}/members`. Live PostgreSQL checks confirmed unknown IDs return `404`, concurrent additions create one membership, and removing a member leaves the account and other memberships intact. Duplicate additions and owner removal return `409`.

Server deletion removes memberships in the same transaction. If the server still has channels, deletion returns `409` and keeps the server and its memberships intact.

**Done when** a server has a valid owner and exactly one membership for that owner, and the API handles invalid IDs and duplicate memberships predictably.

### 3. Add the Channel slice

- [x] Model a channel with a server, name, and type. PostgreSQL stores `TEXT` as the type name.
- [x] Add channel create, read, list, rename, and delete endpoints scoped to a server. Live PostgreSQL checks verified that lookups, renames, and deletions under a different server return `404`.
- [x] Apply the server deletion policy for servers that still have channels. Live checks verified that a blocked deletion preserves the server and its memberships and that concurrent channel creation and server deletion leave a consistent result.

**Done when** channels persist under the intended server and invalid parent or channel IDs return `404`.

### 4. Add the Message slice

- [x] Store messages with a channel, author, content, and server-generated timestamp. Validate content and require an existing author and channel.
- [x] Add message create, read, list, edit, and delete endpoints scoped to a channel within a server. Live PostgreSQL checks verified ID-ordered cursor pagination, rejected mismatched IDs, and retrieved the edited history after an application restart.
- [x] Return `409` when deleting an author or channel with messages. Live checks confirmed that blocked deletion preserves the history and that deletion succeeds after the messages are removed.

**Done when** a message survives a restart and can be retrieved in order from its channel. Check invalid content and mismatched channel/message IDs.

## Phase 2 — Authentication and access control

- [ ] Add password hashing with BCrypt, registration and login, and Spring Security with JWT. Agree on how to handle any existing local plaintext rows before enabling login.
- [ ] Use the authenticated user as the actor for protected operations. Check server membership for channel and message access, and enforce ownership for edits or deletes where appropriate. Review which user fields should be visible to other accounts.
- [ ] Verify valid login, invalid credentials, expired tokens, missing authentication, and denied access between two accounts. Use `401` when authentication is missing or invalid and `403` when an authenticated account lacks permission.

**Done when** stored account passwords are hashes and protected operations enforce the caller's permissions. Complete this phase before exposing the API publicly.

## Phase 3 — Real-time chat

- [ ] Add WebSocket messaging with STOMP. Save each accepted message through the same service used by the REST API, then broadcast it to the channel after the database transaction succeeds.
- [ ] Apply membership checks when clients subscribe and send. Verify delivery with two clients and confirm that the message appears in REST history after reconnecting.

**Done when** channel members receive persisted messages and unauthorized clients cannot send or subscribe.

## Phase 4 — Automated tests

- [ ] Use JUnit 5 and Mockito for focused service tests. Add controller tests for request validation and HTTP responses.
- [ ] Use PostgreSQL Testcontainers for integration tests that exercise JPA mappings, database constraints, and key request flows. Give tests a disposable database so they can run independently of the developer's local data.

**Done when** `.\mvnw.cmd test` verifies the main API and authorization behavior with documented prerequisites.

## Phase 5 — Docker and database setup

- [ ] Add a Dockerfile and Docker Compose configuration for the app and PostgreSQL. Document startup, shutdown, and the environment variables needed for local use.
- [ ] Before deploying to a persistent environment, move schema changes from `ddl-auto=update` to versioned migrations with Flyway or Liquibase. Verify migrations on a fresh database and an existing development database with disposable data.

**Done when** a fresh checkout can start the app and database with Docker Compose and complete the documented smoke check.

## Phase 6 — CI/CD and portfolio handoff

- [ ] Add GitHub Actions to test the project and build the container image. Document the architecture, local setup, API examples, and how to run tests in the README.
- [ ] If deployment is useful, choose a host, configure its secrets and PostgreSQL, and verify the deployed API with dummy accounts.

**Done when** GitHub Actions runs the build and tests and a reviewer can reproduce the documented demo.

## Phase 7 — Stretch work

- [ ] Pick one feature with a clear use case, such as Redis-backed presence, Redis pub/sub for multiple app instances, Kafka message events, or server roles and permissions. Give it its own verification steps before starting another.

## How to work through the plan

1. Pick the first unfinished item in the current phase. Keep each change small enough to review as one commit. Ask before adding dependencies or making a large refactor.
2. Trace the request through the controller, service, repository, and database. Use a DTO for input or output wherever the API needs a shape of its own.
3. Compile with `.\mvnw.cmd compile` and run the relevant tests. For API changes, verify one success and the error cases affected by the change. If PostgreSQL is unavailable, record which checks remain pending.
4. Update this file after each verified slice. Explain what changed and why, note any unverified behavior, and suggest a commit message. Check an item off only once its verification is complete.
