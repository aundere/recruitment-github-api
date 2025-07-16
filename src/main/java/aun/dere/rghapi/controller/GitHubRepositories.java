package aun.dere.rghapi.controller;

import aun.dere.rghapi.dto.api.ApiRepoResponseDto;
import aun.dere.rghapi.exception.RateLimitException;
import aun.dere.rghapi.exception.UserNotFoundException;
import aun.dere.rghapi.service.GitHubRepoLister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
public class GitHubRepositories {

    private final GitHubRepoLister lister;

    @Autowired
    public GitHubRepositories(GitHubRepoLister lister) {
        this.lister = lister;
    }

    @GetMapping("/repositories/{username}")
    public List<ApiRepoResponseDto> hello(@PathVariable String username) {
        try {
            return lister.getRepositories(username);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException(username);
            }

            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new RateLimitException();
            }

            throw e; // Re-throw other exceptions
        }
    }
}
