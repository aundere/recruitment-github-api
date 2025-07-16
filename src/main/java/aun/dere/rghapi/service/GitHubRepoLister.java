package aun.dere.rghapi.service;

import aun.dere.rghapi.config.GitHubConfig;
import aun.dere.rghapi.dto.api.ApiRepoResponseDto;
import aun.dere.rghapi.dto.github.GitHubBranchResponseDto;
import aun.dere.rghapi.dto.github.GitHubRepoResponseDto;
import aun.dere.rghapi.exception.ForbiddenException;
import aun.dere.rghapi.exception.RateLimitException;
import aun.dere.rghapi.exception.RepoNotFoundException;
import aun.dere.rghapi.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

        if (config.getToken() != null && !config.getToken().equals("${GITHUB_TOKEN}")) {
            headers.set("Authorization", "Bearer " + config.getToken());
        }

        var entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
        } catch (HttpClientErrorException e) {
            // GitHub returns 403 Forbidden when the rate limit is exceeded instead of 429 Too Many Requests.
            if (e.getStatusCode() == HttpStatus.FORBIDDEN && e.getMessage().contains("rate limit")) {
                throw new RateLimitException();
            }

            // And 403 Forbidden when the access is denied.
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new ForbiddenException();
            }

            throw e;
        }
    }

    private GitHubRepoResponseDto[] getRepos(String username) {
        String url = String.format(GITHUB_API_URL + "/users/%s/repos", username);

        try {
            return this.makeRequest(url, GitHubRepoResponseDto[].class);
        } catch (HttpClientErrorException e) {
            // Checking for 404 Not Found to handle the case when the user does not exist.
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException();
            }

            throw e;
        }
    }

    private GitHubBranchResponseDto[] getBranches(String owner, String repo) {
        String url = String.format(GITHUB_API_URL + "/repos/%s/%s/branches", owner, repo);

        try {
            return this.makeRequest(url, GitHubBranchResponseDto[].class);
        } catch (HttpClientErrorException e) {
            // Checking for 404 Not Found to handle the case when the repository does not exist.
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RepoNotFoundException();
            }

            throw e;
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
