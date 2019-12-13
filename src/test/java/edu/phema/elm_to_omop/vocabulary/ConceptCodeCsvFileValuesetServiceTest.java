package edu.phema.elm_to_omop.vocabulary;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConceptCodeCsvFileValuesetServiceTest {
    private static WireMockServer wireMockServer;

    private IOmopRepositoryService omopRepository;
    private IValuesetService valuesetService;

    @BeforeEach
    public void setup() throws Exception {
        wireMockServer = new WireMockServer(options().port(43333).extensions("edu.phema.elm_to_omop.vocabulary.ConceptCodeCsvFileResponseTransformer"));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Create a stub based using the transformer
        stubFor(post(urlEqualTo("/vocabulary/search"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withTransformers("concept-transformer")));

        omopRepository = new OmopRepositoryService("http://localhost:43333/", "OHDSI-CDMV5");
    }

    @AfterEach
    public void cleanup() {
        wireMockServer.stop();
    }

    @Test
    public void test() throws Exception {
        // Test simple case with one terminology where all codes exists
        valuesetService = new ConceptCodeCsvFileValuesetService(omopRepository, "/vocabulary/icd9-only.csv", "");
        PhemaConceptSetList concepts = valuesetService.getConceptSetList();

        assertEquals(concepts.getConceptSets().size(), 1);
        assertEquals(concepts.getConceptSets().get(0).expression.items.length, 7);
        assertEquals(concepts.getNotFoundCodes().size(), 0);

        // Test two terminologies where all codes exists
        valuesetService = new ConceptCodeCsvFileValuesetService(omopRepository, "/vocabulary/icd9-and-icd10.csv", "");
        concepts = valuesetService.getConceptSetList();

        assertEquals(concepts.getConceptSets().size(), 1);
        assertEquals(concepts.getConceptSets().get(0).expression.items.length, 7);
        assertEquals(concepts.getNotFoundCodes().size(), 0);

        // Test two terminologies where some codes don't exist
        valuesetService = new ConceptCodeCsvFileValuesetService(omopRepository, "/vocabulary/icd9-and-icd10-and-missing.csv", "");
        concepts = valuesetService.getConceptSetList();

        assertEquals(concepts.getConceptSets().size(), 1);
        assertEquals(concepts.getConceptSets().get(0).expression.items.length, 7);
        assertEquals(concepts.getNotFoundCodes().size(), 2);
    }
}
