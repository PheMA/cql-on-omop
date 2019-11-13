package edu.phema.elm_to_omop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.valueset.IValuesetService;
import edu.phema.elm_to_omop.valueset.SpreadsheetValuesetService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class CohortServiceTest {
    private static WireMockServer wireMockServer;
    private static IOmopRepositoryService omopRepository;
    private static IValuesetService valuesetService;

    @BeforeAll
    public static void setup() throws Exception {
        wireMockServer = new WireMockServer(options().port(53333));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Stub the concept get request
        stubFor(get(urlEqualTo("/vocabulary/phema-test/concept/45917083"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/vocabulary/concept.45917083.json"))));

        // Stub the cohort definition create request
        stubFor(post(urlEqualTo("/cohortdefinition"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/cohortdefinition.108.json"))));

        omopRepository = new OmopRepositoryService("http://localhost:53333/", "phema-test");

        String vsPath = "/api/valuesets/simple.csv";
        valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");
    }

    @AfterAll
    public static void cleanup() {
        wireMockServer.stop();
    }

    @Test
    void testCreateCohortDefinitionRoundtripSerialization() {
        try {
            String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-simple.cql");

            CohortService cs = new CohortService(valuesetService, omopRepository);

            // Deserialize the response from the OHDSI WebAPI
            CohortDefinitionDTO response = cs.createCohortDefinition(cqlString, "In Initial Population");

            // Serialize again
            String result = new ObjectMapper().writeValueAsString(response);

            PhemaTestHelper.assertStringsEqualIgnoreWhitespace(result, PhemaTestHelper.getFileAsString("responses/cohortdefinition/cohortdefinition.108.json"));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    void testCreateCohortDefinitionFailureCase() {
        // Stub the cohort definition create request
        stubFor(post(urlEqualTo("/cohortdefinition"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "text/plain")
                .withBody("Womp womp")));

        try {
            String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-simple.cql");

            CohortService cs = new CohortService(valuesetService, omopRepository);

            // Try to create the cohort definition
            cs.createCohortDefinition(cqlString, "In Initial Population");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Error creating cohort definition");
        }
    }

    @Test
    void testQueueCohortGeneration() throws Exception {
        // Stub the cohort definition create request
        stubFor(get(urlEqualTo("/cohortdefinition/108/generate/phema-test"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/generate/job.171.json"))));

        try {
            CohortService cs = new CohortService(valuesetService, omopRepository);

            // Queue the cohort up for generation
            JobExecutionResource job = cs.queueCohortGeneration(108);

            assertEquals(job.getStatus(), "STARTED");
            assertEquals(job.getJobInstanceResource().getName(), "generateCohort");
            assertEquals(job.getExitStatus(), "UNKNOWN");
            assertEquals(job.getExecutionId(), 171);
        } catch (Exception e) {
            assertEquals(e, null);
        }
    }

    @Test
    void testQueueCohortGenerationFromCqlString() throws Exception {
        // Stub the cohort definition create request
        stubFor(get(urlEqualTo("/cohortdefinition/108/generate/phema-test"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/generate/job.171.json"))));

        try {
            String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-simple.cql");

            CohortService cs = new CohortService(valuesetService, omopRepository);

            // Queue the cohort up for generation
            JobExecutionResource job = cs.queueCohortGeneration(cqlString, "In Initial Population");

            assertEquals(job.getStatus(), "STARTED");
            assertEquals(job.getJobInstanceResource().getName(), "generateCohort");
            assertEquals(job.getExitStatus(), "UNKNOWN");
            assertEquals(job.getExecutionId(), 171);
        } catch (Exception e) {
            assertEquals(e, null);
        }
    }

    @Test
    void testQueryCohortGenerationFailureCase() {
        // Stub the cohort definition create request
        stubFor(get(urlEqualTo("/cohortdefinition/108/generate/phema-test"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "text/plain")
                .withBody("Womp womp")));

        try {
            CohortService cs = new CohortService(valuesetService, omopRepository);

            // Queue the cohort up for generation
            cs.queueCohortGeneration(108);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Error queueing up cohort for generation");
        }
    }
}