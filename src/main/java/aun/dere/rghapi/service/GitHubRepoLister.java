package aun.dere.rghapi.service;

import aun.dere.rghapi.dto.api.ApiRepoResponseDto;
import aun.dere.rghapi.dto.github.GitHubBranchResponseDto;
import aun.dere.rghapi.dto.github.GitHubRepoResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GitHubRepoLister {

    private final String GITHUB_API_URL = "https://api.github.com";

    private final RestTemplate restTemplate;

    @Autowired
    public GitHubRepoLister(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private GitHubRepoResponseDto[] getRepos(String username) {
        String url = String.format(GITHUB_API_URL + "/users/%s/repos", username);
        return restTemplate.getForObject(url, GitHubRepoResponseDto[].class);
    }

    private GitHubBranchResponseDto[] getBranches(String owner, String repo) {
        String url = String.format(GITHUB_API_URL + "/repos/%s/%s/branches", owner, repo);
        return restTemplate.getForObject(url, GitHubBranchResponseDto[].class);
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
