# recruitment-github-api

This is a recruitment task. Implemented in **Java 21** and **Spring 3.5**.

This API provides endpoints to list all GitHub repositories for a given user, which are not forks.

## Build and Run

To build and run the application, you need to have **Java 21** installed.

Run the following command to build the project:

```bash
./gradlew bootJar
```

The output JAR file will be located in the `build/libs` directory.

To run the application, use the following command:

```bash
java -jar recruitment-github-api-1.0.0.jar
```

## Usage

### Endpoints

This API provides the `/repositories/{username}` endpoint to retrieve all not forked repositories for a given
GitHub user and their branches with the latest commit SHA for each branch.

There is no additional functionality such as pagination or filtering, as the task is focused on a simple implementation.

### Authentication

This API does not require authentication, but you can set up a personal access token in the `GITHUB_TOKEN` environment
variable to avoid rate limiting issues with the GitHub API.

### Example usage with `curl`

```bash
curl -sX GET "http://localhost:8080/repositories/octocat" | jq
```

Should return a JSON response with the list of repositories for the specified user:

```js
[
  {
    "owner": "octocat",
    "name": "git-consortium",
    "branches": [
      {
        "name": "master",
        "commitSha": "b33a9c7c02ad93f621fa38f0e9fc9e867e12fa0e"
      }
    ]
  },
  // ...
]
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
