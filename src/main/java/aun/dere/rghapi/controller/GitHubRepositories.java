package aun.dere.rghapi.controller;

import aun.dere.rghapi.model.GitHubRepoDto;
import aun.dere.rghapi.service.GitHubRepoLister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class GitHubRepositories {

    private final GitHubRepoLister lister;

    @Autowired
    public GitHubRepositories(GitHubRepoLister lister) {
        this.lister = lister;
    }

    @GetMapping("/repositories/{username}")
    public Collection<GitHubRepoDto> hello(@PathVariable String username) {
        return lister.getRepositories(username);
    }
}
