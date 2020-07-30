package edu.phema.elm_to_omop;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static junit.framework.TestCase.assertNull;

public class CliTest {
    private static WireMockServer wireMockServer;

    @BeforeEach
    public void setup() throws Exception {
        wireMockServer = new WireMockServer(options().port(53333));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Stub the concept search request
        stubFor(post(urlEqualTo("/vocabulary/search"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/vocabulary/concepts.45917083.json"))));

        // Stub the cohort definition create request
        stubFor(post(urlEqualTo("/cohortdefinition"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/cohortdefinition.108.json"))));

        // Stub the cohort definition generate create request
        stubFor(get(urlEqualTo("/cohortdefinition/108/generate/phema-test"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/generate/job.171.json"))));

        // Stub the cohort definition info request
        stubFor(get(urlEqualTo("/cohortdefinition/108/info"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/info/info.108.complete.json"))));

        // Stub the cohort definition report request
        stubFor(get(urlEqualTo("/cohortdefinition/108/report/phema-test"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/report/report.108.json"))));
    }

    @AfterEach
    public void cleanup() {
        wireMockServer.stop();
    }

    @Test
    public void CliTest() throws Exception {
        try {
            ElmToOmopConverter converter = new ElmToOmopConverter();

            String temp = PhemaTestHelper.getResourcePath("LibraryHelperTests.cql");
//            URL resource = getClass().getResource(PhemaTestHelper.getResourcePath("LibraryHelperTests.cql"));
//            File file = Paths.get(resource.toURI()).toFile();
            File file = new File(PhemaTestHelper.getResourcePath("LibraryHelperTests.cql"));

            String[] args = new String[]{
              "OMOP_BASE_URL=http://localhost:53333/",
              String.format("VS_FILE_NAME=%s", PhemaTestHelper.getResourcePath("cli/simple.csv")),
              String.format("INPUT_FILE_NAME=%s", PhemaTestHelper.getResourcePath("cli/simple.cql")),
              String.format("OUT_FILE_NAME=%s/cli/simple.omop.json", file.getParent()),
              "SOURCE=phema-test",
              "PHENOTYPE_EXPRESSIONS=edu.phema.elm_to_omop.CliTest"
            };

          converter.run(args);

            PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
                PhemaTestHelper.getFileAsString("cli/simple-expected.omop.json"),
                PhemaTestHelper.getFileAsString("cli/simple.omop.json"));
        } catch (Exception e) {
            assertNull(e);
        }
    }
}
