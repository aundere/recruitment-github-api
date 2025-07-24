package aun.dere.rghapi.service;

import aun.dere.rghapi.config.GitHubApiConfig;
import aun.dere.rghapi.dto.api.ApiRepoResponseDto;
import aun.dere.rghapi.dto.github.GitHubBranchResponseDto;
import aun.dere.rghapi.dto.github.GitHubRepoResponseDto;
import aun.dere.rghapi.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class GitHubRepoLister {

    private final RestClient restClient;

    @Autowired
    public GitHubRepoLister(GitHubApiConfig config) {
        var builder = RestClient.builder()
                .baseUrl(config.getUrl())
                .defaultStatusHandler(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> this.throwExceptionByStatus(res.getStatusCode().value()));

        if (!config.getToken().equals("${GITHUB_API_TOKEN}")) {
            builder.defaultHeader("Authorization", "Bearer " + config.getToken());
        }

        this.restClient = builder.build();
    }

    private void throwExceptionByStatus(int status) {
        switch (HttpStatus.valueOf(status)) {
            case UNAUTHORIZED -> throw new AppException.UnauthorizedException();
            case FORBIDDEN -> throw new AppException.ForbiddenException();
            case NOT_FOUND -> throw new AppException.NotFoundException();
            default -> throw new RuntimeException("Unexpected GitHub API status code: " + status);
        }
    }

    private <T> T executeApiRequest(Class<T> responseType, String url, Object... uriParameters) {
        return restClient.get()
                .uri(url, uriParameters)
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    private GitHubRepoResponseDto[] fetchRepositories(String username) {
        return this.executeApiRequest(GitHubRepoResponseDto[].class, "/users/{username}/repos", username);
    }

    private GitHubBranchResponseDto[] fetchBranches(String owner, String repo) {
        return this.executeApiRequest(GitHubBranchResponseDto[].class, "/repos/{owner}/{repo}/branches", owner, repo);
    }

    private ApiRepoResponseDto mapToApiRepoResponseDto(GitHubRepoResponseDto repo) {
        return new ApiRepoResponseDto(repo.owner().login(), repo.name(), new ArrayList<>());
    }

    private ApiRepoResponseDto.GitHubRepoBranch mapToApiRepoBranch(GitHubBranchResponseDto branch) {
        return new ApiRepoResponseDto.GitHubRepoBranch(branch.name(), branch.commit().sha());
    }

    private List<ApiRepoResponseDto.GitHubRepoBranch> fetchAndMapBranches(String owner, String repo) {
        var branches = this.fetchBranches(owner, repo);
        return Arrays.stream(branches)
                .map(this::mapToApiRepoBranch)
                .toList();
    }

    public List<ApiRepoResponseDto> getRepositories(String username) {
        var repos = Arrays.stream(this.fetchRepositories(username))
                .filter(x -> !x.fork())
                .map(this::mapToApiRepoResponseDto)
                .toList();

        repos.forEach(x -> x.branches().addAll(this.fetchAndMapBranches(x.owner(), x.name())));

        return repos;
    }
}
