package edu.phema.elm_to_omop.vocabulary;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class VocabularyTranslationTest {
    private static WireMockServer wireMockServer;

    private IOmopRepositoryService omopRepository;
    private IValuesetService valuesetService;

    private List<PhemaConceptSet> conceptSets;

    @BeforeEach
    public void setup() throws Exception {
        wireMockServer = new WireMockServer(options().port(38383).extensions("edu.phema.elm_to_omop.vocabulary.VocabularyTranslationTestTransformer"));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Create a stub based using the transformer
        stubFor(post(urlEqualTo("/vocabulary/search"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withTransformers("vocabulary-translation-transformer")));

        omopRepository = new OmopRepositoryService("http://localhost:38383/", "OHDSI-CDMV5");
    }

    @Test
    public void ActTranslationTest() throws Exception {
        valuesetService = new ConceptCodeCsvFileValuesetService(omopRepository, PhemaTestHelper.getResourcePath("vocabulary/encounter/act-encounter-codes.valueset.csv"));
        conceptSets = valuesetService.getConceptSets();

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getJson(conceptSets),
            PhemaTestHelper.getFileAsString("vocabulary/encounter/act-encounter-codes-translated.phema-concept-sets.json"));
    }
}
