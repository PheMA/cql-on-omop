package edu.phema.elm_to_omop.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.helper.Terms;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;
import org.ohdsi.webapi.service.CohortDefinitionService.GenerateSqlRequest;
import org.ohdsi.webapi.service.CohortDefinitionService.GenerateSqlResult;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

/**
 * This class uses the WebAPI to interact with with the OMOP repository.
 */
public class OmopRepositoryService implements IOmopRepositoryService {

    private String domain;
    private String source;

    private HttpURLConnection con;
    private Client client;

    /**
     * Creates an instance of an OMOP repository service provider with
     * a specific domain and source
     *
     * @param domain The OMOP WebAPI URL
     * @param source The data source
     */
    public OmopRepositoryService(String domain, String source) {
        this.domain = domain;
        this.source = source;
        this.client = ClientBuilder.newClient();
    }

    /**
     * Gets concept metadata for a given concept ID
     *
     * @param id The concept ID
     * @return The concept metadata
     * @throws OmopRepositoryException
     */
    public Concept getConceptMetadata(String id) throws OmopRepositoryException {
        try {
            return client
                .target(domain + "vocabulary/" + source + "/concept/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get(Concept.class);
        } catch (Throwable t) {
            throw new OmopRepositoryException("Error creating cohort", t);
        }
    }

    /**
     * Create a new cohort definition in the OMOP database. This only creates
     * the definition, and does not actually generate the cohort.
     *
     * @param cohortDefintion The cohort definition to create
     * @return The created cohort definition
     * @throws OmopRepositoryException
     */
    public CohortDefinitionDTO createCohortDefinition(CohortDefinitionDTO cohortDefintion) throws OmopRepositoryException {
        Response response = client
            .target(domain + "cohortdefinition")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(cohortDefintion, MediaType.APPLICATION_JSON));

        try {
            return response.readEntity(CohortDefinitionDTO.class);
        } catch (Throwable t) {
            throw new OmopRepositoryException("Error creating cohort", t);
        }
    }

    /**
     * Queue up a specific cohort definition for generation. This will return
     * the created cohort definition job.
     *
     * @param id The ID of the cohort definition to generate
     * @return The cohort generation job
     * @throws OmopRepositoryException
     */
    public JobExecutionResource queueCohortGeneration(Integer id) throws OmopRepositoryException {
        try {
            return client
                .target(domain + "cohortdefinition/" + id + "/generate/" + source)
                .request(MediaType.APPLICATION_JSON)
                .get(JobExecutionResource.class);
        } catch (Throwable t) {
            throw new OmopRepositoryException("Error queueing up cohort for generation", t);
        }
    }

    /**
     * Get information about the cohort definition, such as the generation status.
     *
     * @param id The cohort definition id
     * @return A list of cohort definition info objects
     * @throws OmopRepositoryException
     */
    public List<CohortGenerationInfo> getCohortDefinitionInfo(Integer id) throws OmopRepositoryException {
        try {
            return client
                .target(domain + "cohortdefinition/" + id + "/info")
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class)
                .readEntity(new GenericType<List<CohortGenerationInfo>>() {
                });
        } catch (Throwable t) {
            throw new OmopRepositoryException("Error getting cohort definition info", t);
        }
    }

    /**
     * Get a report for a given cohort definition, including statistics for
     * each inclusion rule.
     *
     * @param id The cohort definition id
     * @return The report object
     * @throws OmopRepositoryException
     */
    public InclusionRuleReport getCohortDefinitionReport(Integer id) throws OmopRepositoryException {
        try {
            return client
                .target(domain + "cohortdefinition/" + id + "/report/" + source)
                .request(MediaType.APPLICATION_JSON)
                .get(InclusionRuleReport.class);
        } catch (Throwable t) {
            throw new OmopRepositoryException("Error getting cohort definition report", t);
        }
    }

    /**
     * Gets the cohort definition for a given id
     *
     * @param id The cohort definition id
     * @return The cohort definition
     * @throws OmopRepositoryException
     */
    public CohortDefinitionDTO getCohortDefinition(Integer id) throws OmopRepositoryException {
        try {
            return client
                .target(domain + "cohortdefinition/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get(CohortDefinitionDTO.class);
        } catch (Throwable t) {
            throw new OmopRepositoryException("Error getting cohort definition", t);
        }
    }

    /**
     * Get the CQL for a given cohort definition. One of the following target dialects may optionally
     * be specified:
     * <p>
     * - "sql server"
     * - "pdw"
     * - "oracle"
     * - "postgresql"
     * - "redshift"
     * - "impala"
     * - "netezza"
     * </p>
     *
     * @param id The cohort definition id
     * @return The generation result obejct
     * @throws OmopRepositoryException
     */
    public GenerateSqlResult getCohortDefinitionSql(Integer id, String targetDialect) throws OmopRepositoryException {
        try {
            // Get the cohort definition
            CohortDefinitionDTO cohortDefinition = getCohortDefinition(id);

            GenerateSqlRequest request = new GenerateSqlRequest();
            // 🤷🏻‍ Apparently they mix the use of string and POJOs internally
            request.expression = new ObjectMapper().readValue(cohortDefinition.expression, CohortExpression.class);

            // Get the SQL
            Response response = client
                .target(domain + "cohortdefinition/sql")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON));
            GenerateSqlResult result = response.readEntity(GenerateSqlResult.class);

            // Translate if necessary
            if (targetDialect != null) {
                SourceStatement translateRequest = new SourceStatement();

                translateRequest.sql = result.templateSql;
                translateRequest.targetDialect = targetDialect;

                Response translateResponse = client
                    .target(domain + "sqlrender/translate")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(translateRequest, MediaType.APPLICATION_JSON));
                TranslatedStatement translated = translateResponse.readEntity(TranslatedStatement.class);

                result.templateSql = translated.targetSQL;
            }

            return result;
        } catch (Throwable t) {
            throw new OmopRepositoryException("Error getting cohort definition sql", t);
        }
    }
}
