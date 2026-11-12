# Discord Clone Backend

Follow [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md). Read the current checkpoint before starting work. If a user request changes the order or scope, update the plan to reflect the decision.

- Work in small vertical slices and keep the project compiling. Explain changes and unfamiliar concepts briefly in plain language. Prefer straightforward Java over clever abstractions.
- When continuing the plan in response to “next,” name the current phase in progress updates and the final response.
- Organize code by feature under `com.alfha.discord_clone`. Use constructor injection and keep HTTP, business logic, and database access in the controller, service, and repository respectively. Return DTOs from controllers.
- Use the Maven wrapper (`.\mvnw.cmd` in PowerShell). Check `pom.xml` for the Java and Spring Boot versions.
- Ask before adding dependencies or making a large refactor. Keep real credentials out of Git and use dummy accounts for local checks.
- Suggest a commit message after each slice. When making commits, use `ALFHAvn <alfhaminecraft@gmail.com>` as both author and committer. Apply the identity per command or locally to this repository.
- Commit and push verified slices of an incomplete phase to that phase's branch. When the phase is complete and verified, integrate it into `main` and push `main` to GitHub.
