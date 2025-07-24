package aun.dere.rghapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;

@EnableWireMock
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "github.api.url=http://localhost:${wiremock.server.port}",
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RepositoryControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void shouldReturnRepositoriesForValidUser() {
        this.webTestClient.get()
                .uri("/repositories/user")
                .exchange()
                .expectBody()
                .json("[{owner: 'user', name:  'test-repo', branches: [{name: 'master', commitSha: '123abc'}]}]");
    }
}
