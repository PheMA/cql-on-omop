package edu.phema.elm_to_omop.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.helper.Config;
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
import static org.junit.jupiter.api.Assertions.assertNull;

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

        // Stub the concept get request
        stubFor(get(urlEqualTo("/vocabulary/phema-test/concept/45917083"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/vocabulary/concept.45917083.json"))));

        omopRepository = new OmopRepositoryService("http://localhost:53333/", "phema-test");

        String vsPath = "/api/valuesets/simple.csv";
        valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");
    }

    @AfterEach
    public void cleanup() {
        wireMockServer.stop();
    }

    @Test
    void conceptSetSetupTest() {
        Config config = new Config(Config.getDefaultConfigPath());

        assertDoesNotThrow(() -> {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(valuesetService);
        });
    }

    @Test
    void omopTranslatorApiSmokeTest() {
        Config config = new Config(Config.getDefaultConfigPath());

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(valuesetService);

            String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-simple.cql");

            String omopJson = translate.cqlToOmopJson(cqlString, "In Initial Population");

            String expected = PhemaTestHelper.getFileAsString("api/smoke-test-simple.omop.json");

            PhemaTestHelper.assertStringsEqualIgnoreWhitespace(omopJson, expected);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    void OmopTranslatorMultipleStatementTest() {
        Config config = new Config(Config.getDefaultConfigPath());

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(valuesetService);

            String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-multiple.cql");

            List<String> names = Arrays.asList("In Initial Population", "Another Statement");

            String omopJson = translate.cqlToOmopJson(cqlString, names);

            String expected = PhemaTestHelper.getFileAsString("api/smoke-test-multiple.omop.json");

            PhemaTestHelper.assertStringsEqualIgnoreWhitespace(omopJson, expected);
        } catch (Exception e) {
            assertNull(e);
        }
    }
}
