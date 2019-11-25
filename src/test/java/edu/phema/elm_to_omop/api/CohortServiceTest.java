package edu.phema.elm_to_omop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.api.exception.CohortServiceException;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;
import org.ohdsi.webapi.service.CohortDefinitionService.GenerateSqlResult;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class CohortServiceTest {
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

    @AfterEach
    public void cleanup() {
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
        // Stub the cohort definition generate request
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
            assertNull(e);
        }
    }

    @Test
    void testQueueCohortGenerationFromCqlString() throws Exception {
        // Stub the cohort definition generate create request
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
            assertNull(e);
        }
    }

    @Test
    void testQueryCohortGenerationFailureCase() {
        // Stub the cohort definition generate request
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

    @Test
    void testCohortGenerationInfo() {
        try {
            // Stub the cohort definition info request
            stubFor(get(urlEqualTo("/cohortdefinition/108/info"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/info/info.108.complete.json"))));

            CohortService cs = new CohortService(valuesetService, omopRepository);

            List<CohortGenerationInfo> info = cs.getCohortDefinitionInfo(108);

            assertEquals(info.size(), 1);
            assertEquals(info.get(0).getId().getCohortDefinitionId(), 108);
            assertEquals(info.get(0).getStatus(), GenerationStatus.COMPLETE);
            assertNull(info.get(0).getFailMessage());
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    void testCohortGenerationInfoFromCqlString() {
        try {
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

            String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-simple.cql");

            CohortService cs = new CohortService(valuesetService, omopRepository);

            // Queue the cohort up for generation
            List<CohortGenerationInfo> info = cs.getCohortDefinitionInfo(cqlString, "In Initial Population");

            assertEquals(info.size(), 1);
            assertEquals(info.get(0).getId().getCohortDefinitionId(), 108);
            assertEquals(info.get(0).getStatus(), GenerationStatus.COMPLETE);
            assertNull(info.get(0).getFailMessage());
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    void testCohortGenerationInfoFailureCase() {
        try {
            // Stub the cohort definition info request
            stubFor(get(urlEqualTo("/cohortdefinition/108/info"))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Womp womp")));

            CohortService cs = new CohortService(valuesetService, omopRepository);

            cs.getCohortDefinitionInfo(108);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Error getting cohort definition info");
        }
    }

    @Test
    void testCohortGenerationReport() {
        try {
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

            CohortService cs = new CohortService(valuesetService, omopRepository);

            InclusionRuleReport report = cs.getCohortDefinitionReport(108);

            assertEquals(report.inclusionRuleStats.size(), 2);
            assertEquals(report.summary.baseCount, 938);
            assertEquals(report.summary.finalCount, 246);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    void testCohortGenerationReportFromCqlString() {
        try {
            // Stub the cohort definition generate create request
            stubFor(get(urlEqualTo("/cohortdefinition/108/generate/phema-test"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/generate/job.171.json"))));

            // Stub the cohort definition info request. Return "RUNNING" twice, then return "COMPLETE"
            stubFor(get(urlEqualTo("/cohortdefinition/108/info"))
                .inScenario("Long running cohort generation")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("Running")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/info/info.108.running.json"))));

            stubFor(get(urlEqualTo("/cohortdefinition/108/info"))
                .inScenario("Long running cohort generation")
                .whenScenarioStateIs("Running")
                .willSetStateTo("Still running")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/info/info.108.running.json"))));

            stubFor(get(urlEqualTo("/cohortdefinition/108/info"))
                .inScenario("Long running cohort generation")
                .whenScenarioStateIs("Still running")
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

            String cqlString = PhemaTestHelper.getFileAsString("api/smoke-test-simple.cql");

            CohortService cs = new CohortService(valuesetService, omopRepository);

            InclusionRuleReport report = cs.getCohortDefinitionReport(cqlString, "In Initial Population");

            assertEquals(report.inclusionRuleStats.size(), 2);
            assertEquals(report.summary.baseCount, 938);
            assertEquals(report.summary.finalCount, 246);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    void testSqlRendering() throws Exception {
        // Stub getting the cohort definition
        stubFor(get(urlEqualTo("/cohortdefinition/87"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/cohortdefinition.87.json"))));

        // Stub the SQL render request
        stubFor(post(urlEqualTo("/cohortdefinition/sql"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/sql/sql.87.json"))));

        CohortService cs = new CohortService(valuesetService, omopRepository);

        GenerateSqlResult result = cs.getCohortDefinitionSql(87, null);

        GenerateSqlResult expected = new ObjectMapper().readValue(PhemaTestHelper.getFileAsString("responses/cohortdefinition/sql/sql.87.json"), GenerateSqlResult.class);

        assertEquals(result.templateSql, expected.templateSql);
    }

    @Test
    void testSqlRenderingWithTranslate() throws Exception {
        // Stub getting the cohort definition
        stubFor(get(urlEqualTo("/cohortdefinition/87"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/cohortdefinition.87.json"))));

        // Stub the SQL render request
        stubFor(post(urlEqualTo("/cohortdefinition/sql"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/cohortdefinition/sql/sql.87.json"))));

        // Stub the SQL translate request
        stubFor(post(urlEqualTo("/sqlrender/translate"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(PhemaTestHelper.getFileAsString("responses/sqlrender/translate/sql.87.postgresql.json"))));

        CohortService cs = new CohortService(valuesetService, omopRepository);

        GenerateSqlResult result = cs.getCohortDefinitionSql(87, "postgresql");

        GenerateSqlResult expected = new ObjectMapper().readValue(PhemaTestHelper.getFileAsString("responses/cohortservice/getCohortDefinitionSql/sql.87.postgresql.json"), GenerateSqlResult.class);

        assertEquals(result.templateSql, expected.templateSql);
    }

    @Test
    void testSqlRenderingFailureCase() throws Exception {
        try {
            // Stub getting the cohort definition
            stubFor(get(urlEqualTo("/cohortdefinition/87"))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Womp womp")));

            CohortService cs = new CohortService(valuesetService, omopRepository);

            GenerateSqlResult result = cs.getCohortDefinitionSql(87, null);
        } catch (CohortServiceException e) {
            assertEquals(e.getMessage(), "Error getting cohort definition sql");
        }
    }
}