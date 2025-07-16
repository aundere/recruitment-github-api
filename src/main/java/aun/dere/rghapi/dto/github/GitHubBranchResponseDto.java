package aun.dere.rghapi.dto.github;

public record GitHubBranchResponseDto(String name, GitHubBranchCommit commit) {

    public record GitHubBranchCommit(String sha) { }
}
