# Discord Clone Backend

A Java and Spring Boot backend for a Discord-like app. The current API provides a health check and endpoints to manage users, servers, text channels, and messages. See [the development plan](DEVELOPMENT_PLAN.md) for the roadmap.

## Run locally on Windows

Install JDK 25 and PostgreSQL 17, then start PostgreSQL. The app connects to `localhost:5432` using the `discord_clone` database and the `discord_user` role. If you already have that database and role, skip the creation commands below.

Connect as a PostgreSQL administrator using SQL Shell (psql) or PowerShell with `psql` on your `PATH`.

```powershell
psql -h localhost -U postgres -d postgres
```

At the `psql` prompt, create the development role and database. The `\password` command prompts for the new password without putting it in the SQL command history.

```sql
CREATE ROLE discord_user LOGIN;
\password discord_user
CREATE DATABASE discord_clone OWNER discord_user;
\q
```

If the role still uses a password previously committed to Git, run `\password discord_user` as a PostgreSQL administrator and choose a new password.

From the repository root, set `DISCORD_DB_PASSWORD` in the same PowerShell session you use to run Maven. Enter the password for `discord_user` when prompted.

```powershell
$password = Read-Host 'PostgreSQL password for discord_user' -AsSecureString
$credential = [System.Management.Automation.PSCredential]::new('discord_user', $password)
$env:DISCORD_DB_PASSWORD = $credential.GetNetworkCredential().Password
Remove-Variable password,credential
.\mvnw.cmd spring-boot:run
```

In IntelliJ IDEA, edit the application run configuration and set `DISCORD_DB_PASSWORD` under **Environment variables**. Keep that configuration local to your machine.

## Check the User API

Use `http://localhost:8080` as the base URL in Postman. `GET /health` should return `Backend is alive!`.

Send `POST /api/users` with a JSON body. Use a new username and email for each run and a dummy password because the current app stores account passwords as plaintext.

```json
{
  "username": "demo-user",
  "email": "demo-user@example.test",
  "password": "dummy-password"
}
```

Successful creation returns `201 Created`. Follow the `Location` response header to fetch the new user, then check `GET /api/users` for the same user. Each response should omit the password. To confirm that PostgreSQL stored the user, connect with `psql -h localhost -U discord_user -d discord_clone` and run the query below.

```sql
SELECT id, username, email FROM users WHERE username = 'demo-user';
```

An invalid creation request returns `400 Bad Request`. A lookup for a missing ID returns `404 Not Found`. A creation request with an existing username or email returns `409 Conflict`.

To update a profile, send `PUT /api/users/{id}` with both `username` and `email` in the JSON body. The response is `200 OK` with the updated profile. Invalid fields return `400`, a missing ID returns `404`, and a username or email used by another account returns `409`.

Send `DELETE /api/users/{id}` to remove a dummy user. A successful deletion returns `204 No Content`. Fetching or deleting that ID again returns `404`.

## Check the Server API

Create a dummy user first. Send `POST /api/servers` with a name and that user's ID as `ownerId`.

```json
{
  "name": "Study Group",
  "ownerId": 1
}
```

Successful creation returns `201 Created` and adds the owner to the server's members. Follow `Location` to fetch the server with `GET /api/servers/{id}`. An invalid creation request returns `400`, and an unknown owner or server ID returns `404`.

`GET /api/servers` lists servers in ascending ID order. To rename a server, send `PUT /api/servers/{id}` with a JSON body such as `{"name":"Weekend Study Group"}`. The response is `200 OK` with the updated server. A blank name or a name over 255 characters returns `400`.

`DELETE /api/servers/{id}` returns `204 No Content` and removes the server's memberships in the same transaction. The users keep their accounts. A server with channels returns `409 Conflict` and keeps its memberships. Renaming or deleting an unknown server ID returns `404`.

Deleting a user who owns a server or has authored messages returns `409 Conflict`. Successful user deletion also removes that user's ordinary server memberships.

### Manage server members

Use `GET /api/servers/{serverId}/members` to list members by user ID. To add an existing user, send `POST /api/servers/{serverId}/members` with a JSON body such as `{"userId":2}`. The response is `201 Created`, and its `Location` header points to `GET /api/servers/{serverId}/members/{userId}`. Member responses contain the user ID and username.

Send `DELETE /api/servers/{serverId}/members/{userId}` to remove a member and receive `204 No Content`. Unknown servers, users, or memberships return `404` when referenced. An invalid add request returns `400`. Adding a duplicate or trying to remove the owner returns `409`.

## Check the Channel API

To create a text channel, send `POST /api/servers/{serverId}/channels` with `{"name":"general","type":"TEXT"}`. The response is `201 Created` with a `Location` header pointing to the new channel. `TEXT` is the supported channel type.

- `GET /api/servers/{serverId}/channels` lists that server's channels in ID order.
- `GET /api/servers/{serverId}/channels/{channelId}` fetches one channel.
- `PUT /api/servers/{serverId}/channels/{channelId}` renames the channel with a body such as `{"name":"study-chat"}` and returns `200 OK`.
- `DELETE /api/servers/{serverId}/channels/{channelId}` removes the channel and returns `204 No Content`. A channel with messages returns `409 Conflict`.

Invalid request bodies return `400`. Unknown IDs and channels that belong to a different server return `404`.

## Check the Message API

Use `/api/servers/{serverId}/channels/{channelId}/messages` as the collection URL. Send a `POST` with `{"authorId":2,"content":"Hello!"}` using an existing user ID. The response is `201 Created` with a `Location` header and a server-generated `createdAt` timestamp.

- `GET` on the collection returns `messages` in ascending ID order. Use `limit` to set the page size from 1 to 100 (default 20). When `nextAfterId` is present and non-null, send it as `afterId` to fetch the next page.
- `GET` on `/{messageId}` under the collection fetches one message.
- `PUT` on `/{messageId}` updates the content with a body such as `{"content":"Updated message"}` and returns `200 OK`.
- `DELETE` on `/{messageId}` removes the message and returns `204 No Content`.

Content must contain non-whitespace text and fit within 2,000 characters. Invalid inputs return `400`. Unknown IDs and requests that use the wrong server or channel return `404`.
