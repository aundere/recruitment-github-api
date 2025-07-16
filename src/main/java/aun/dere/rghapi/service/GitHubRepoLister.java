package aun.dere.rghapi.service;

import aun.dere.rghapi.model.GitHubRepoDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class GitHubRepoLister {

    public Collection<GitHubRepoDto> getRepositories(String username) {
        var branches = new ArrayList<GitHubRepoDto.GitHubRepoBranch>();
        branches.add(new GitHubRepoDto.GitHubRepoBranch("main", "1234567890abcdef1234567890abcdef12345678"));

        var list = new ArrayList<GitHubRepoDto>();
        list.add(new GitHubRepoDto(username, "repo", branches));

        return list;
    }
}
