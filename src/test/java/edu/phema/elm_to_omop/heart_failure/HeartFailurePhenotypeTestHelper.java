package edu.phema.elm_to_omop.heart_failure;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class HeartFailurePhenotypeTestHelper {
    private static WireMockServer wireMockServer;
    private static IOmopRepositoryService omopRepository;

    public static void createMockOmopServer(Integer port) throws Exception {
        wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Stub the concept get request
        stubFor(get(urlEqualTo("/vocabulary/phema-test/concept/45917083"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/vocabulary/concept.45917083.json"))));

        omopRepository = new OmopRepositoryService("http://localhost:53333/", "phema-test");

//        String vsPath = "/api/valuesets/simple.csv";
//        valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");

    }

    public static void destroyMockOmopServer() {
        wireMockServer.stop();
    }

    private static void createGetStub(String url, String responseFilename) {

    }

    private static void createPostStub(String url, String responseFilename) {

    }
}
