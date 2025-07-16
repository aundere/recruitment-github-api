package aun.dere.rghapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RepositoryControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnRepositoriesForValidUser() throws Exception {
        mvc.perform(get("/repositories/octocat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6)) // octocat has 6 public not forked repos
                .andExpect(jsonPath("$[0].owner").value("octocat"))
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].branches[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].branches[0].commitSha").isNotEmpty())
                .andExpect(jsonPath("$[?(@.name == 'linguist')]").doesNotExist()) // linguist is a forked repo
                .andExpect(jsonPath("$[?(@.name == 'Spoon-Knife')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Spoon-Knife')].branches.length()").value(3)) // Spoon-Knife has 3 branches
                .andExpect(jsonPath("$[?(@.name == 'Spoon-Knife')].branches[?(@.name == 'change-the-title')].commitSha").exists())
                .andExpect(jsonPath("$[?(@.name == 'Spoon-Knife')].branches[?(@.name == 'change-the-title')].commitSha")
                        .value("f439fc5710cd87a4025247e8f75901cdadf5333d"));

    }
}
