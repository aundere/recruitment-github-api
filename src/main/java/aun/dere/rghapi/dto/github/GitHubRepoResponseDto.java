package aun.dere.rghapi.dto.github;

public record GitHubRepoResponseDto(boolean fork, String name, GitHubRepoOwner owner) {

    public record GitHubRepoOwner(String login) { }
}
