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

    private <T> T makeRequest(String url, Class<T> responseType) {
        var headers = new HttpHeaders();

        // Default token value is "${GITHUB_TOKEN}", not null.
        if (!config.getToken().equals("${GITHUB_TOKEN}")) {
            headers.set("Authorization", "Bearer " + config.getToken());
        }

        var entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
        } catch (HttpClientErrorException.Unauthorized _e) {
            throw new UnauthorizedException();
        } catch (HttpClientErrorException.Forbidden e) {
            // GitHub returns 403 Forbidden when the rate limit is exceeded instead of 429 Too Many Requests.
            if (e.getMessage().contains("rate limit")) {
                throw new RateLimitException();
            }

            throw new ForbiddenException();
        }
    }

    private GitHubRepoResponseDto[] getRepos(String username) {
        var url = String.format(GITHUB_API_URL + "/users/%s/repos", username);

        try {
            return this.makeRequest(url, GitHubRepoResponseDto[].class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException();
        }
    }

    private GitHubBranchResponseDto[] getBranches(String owner, String repo) {
        var url = String.format(GITHUB_API_URL + "/repos/%s/%s/branches", owner, repo);

        try {
            return this.makeRequest(url, GitHubBranchResponseDto[].class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RepoNotFoundException();
        }
    }

    public List<ApiRepoResponseDto> getRepositories(String username) {
        var repos = Arrays.stream(this.getRepos(username))
                .filter(x -> !x.fork())
                .map(x -> new ApiRepoResponseDto(x.owner().login(), x.name(), new ArrayList<>()))
                .toList();

        for (var repo : repos) {
            var branches = Arrays.stream(this.getBranches(repo.owner(), repo.name()))
                    .map(x -> new ApiRepoResponseDto.GitHubRepoBranch(x.name(), x.commit().sha()))
                    .toList();

            repo.branches().addAll(branches);
        }

        return repos;
    }
}
