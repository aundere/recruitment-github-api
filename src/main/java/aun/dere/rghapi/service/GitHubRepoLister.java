package aun.dere.rghapi.service;

import aun.dere.rghapi.config.GitHubConfig;
import aun.dere.rghapi.dto.api.ApiRepoResponseDto;
import aun.dere.rghapi.dto.github.GitHubBranchResponseDto;
import aun.dere.rghapi.dto.github.GitHubRepoResponseDto;
import aun.dere.rghapi.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Service
public class GitHubRepoLister {

    private final String GITHUB_API_URL = "https://api.github.com";

    private final RestTemplate restTemplate;

    private final GitHubConfig config;

    @Autowired
    public GitHubRepoLister(RestTemplate restTemplate, GitHubConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    private <T> T executeApiRequest(String url, Class<T> responseType) {
        var headers = new HttpHeaders();
        if (!config.getToken().equals("${GITHUB_TOKEN}")) {
            headers.set("Authorization", "Bearer " + config.getToken());
        }

        var entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UnauthorizedException();
        } catch (HttpClientErrorException.Forbidden e) {
            throw e.getMessage().contains("rate limit")
                    ? new RateLimitException() // GitHub returns 403 Forbidden when the rate limit is exceeded
                    : new ForbiddenException();
        }
    }

    private <T> T handleNotFound(Supplier<T> supplier, RuntimeException exception) {
        try {
            return supplier.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw exception;
        }
    }

    private GitHubRepoResponseDto[] fetchRepositories(String username) {
        var url = String.format(GITHUB_API_URL + "/users/%s/repos", username);
        return handleNotFound(() -> this.executeApiRequest(url, GitHubRepoResponseDto[].class), new UserNotFoundException());
    }

    private GitHubBranchResponseDto[] fetchBranches(String owner, String repo) {
        var url = String.format(GITHUB_API_URL + "/repos/%s/%s/branches", owner, repo);
        return handleNotFound(() -> this.executeApiRequest(url, GitHubBranchResponseDto[].class), new RepoNotFoundException());
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
