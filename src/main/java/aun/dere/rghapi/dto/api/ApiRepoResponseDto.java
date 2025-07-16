package aun.dere.rghapi.dto.api;

import java.util.List;

public record ApiRepoResponseDto(String owner, String name, List<GitHubRepoBranch> branches) {

    public record GitHubRepoBranch(String name, String commitSha) { }
}
