package aun.dere.rghapi.model;

import java.util.List;

public record GitHubRepoDto(String owner, String name, List<GitHubRepoBranch> branches) {

    public record GitHubRepoBranch(String name, String commitSha) { }
}
