package edu.phema.elm_to_omop.repository;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OmopRepositoryServiceTest {
    private static WireMockServer wireMockServer;
    private static IOmopRepositoryService omopRepository;

    @BeforeEach
    public void setup() throws Exception {
        wireMockServer = new WireMockServer(options().port(43333));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Stub zero results request
        stubFor(post(urlEqualTo("/vocabulary/search"))
            .withRequestBody(equalToJson(PhemaTestHelper.getFileAsString("repository/empty-search-request.omop.json")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("repository/empty-search-results.omop.json"))));

        // Stub single result request
        stubFor(post(urlEqualTo("/vocabulary/search"))
            .withRequestBody(equalToJson(PhemaTestHelper.getFileAsString("repository/single-search-request.omop.json")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("repository/single-search-result.omop.json"))));

        // Stub multiple results request
        stubFor(post(urlEqualTo("/vocabulary/search"))
            .withRequestBody(equalToJson(PhemaTestHelper.getFileAsString("repository/multiple-search-request.omop.json")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("repository/multiple-search-results.omop.json"))));

        omopRepository = new OmopRepositoryService("http://localhost:43333/", "phema-test");
    }

    @AfterEach
    public void cleanup() {
        wireMockServer.stop();
    }

    @Test
    public void vocabularySearchTest() throws Exception {
        List<Concept> results;

        // Test zero results
        results = omopRepository.vocabularySearch("zxzxzxzx", "CPT4");
        assertEquals(results.size(), 0);

        // Test one results
        results = omopRepository.vocabularySearch("93303", "CPT4");
        assertEquals(results.size(), 1);
        Concept concept = results.get(0);

        assertEquals(concept.conceptId, 2313867);
        assertEquals(concept.conceptName, "Transthoracic echocardiography for congenital cardiac anomalies; complete");
        assertEquals(concept.vocabularyId, "CPT4");
        assertEquals(concept.conceptCode, "93303");

        // Test multiple results
        results = omopRepository.vocabularySearch("933", "CPT4");
        assertEquals(results.size(), 52);
    }
}
