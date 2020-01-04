package edu.phema.elm_to_omop.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class ElmToOmopTranslatorTest {
    private static WireMockServer wireMockServer;
    private static IOmopRepositoryService omopRepository;
    private static IValuesetService valuesetService;

    @BeforeEach
    public void setup() throws Exception {
        wireMockServer = new WireMockServer(options().port(53333));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Stub the concept POST request
        stubFor(post(urlEqualTo("/vocabulary/search"))
          .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(PhemaTestHelper.getFileAsString("responses/vocabulary/concepts.45917083.json"))));

        omopRepository = new OmopRepositoryService("http://localhost:53333/", "phema-test");

        String vsPath = PhemaTestHelper.getResourcePath("api/valuesets/simple.csv");
        valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");
    }

    @AfterEach
    public void cleanup() {
        wireMockServer.stop();
    }

    @Test
    void conceptSetSetupTest() {
        assertDoesNotThrow(() -> {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(valuesetService);
        });
    }

    @Test
    void omopTranslatorApiSmokeTest() throws Exception {
        ElmToOmopTranslator translate = new ElmToOmopTranslator(valuesetService);

        String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-simple.cql");

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("api/smoke-test-simple.omop.json"),
            translate.cqlToOmopJson(cqlString, "In Initial Population"));
    }

    @Test
    void OmopTranslatorMultipleStatementTest() throws Exception {
        ElmToOmopTranslator translate = new ElmToOmopTranslator(valuesetService);

        String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-multiple.cql");

        List<String> names = Arrays.asList("In Initial Population", "Another Statement");

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("api/smoke-test-multiple.omop.json"),
            translate.cqlToOmopJson(cqlString, names));
    }
}
