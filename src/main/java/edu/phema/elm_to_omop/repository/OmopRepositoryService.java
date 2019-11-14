package edu.phema.elm_to_omop.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.helper.Terms;
import edu.phema.elm_to_omop.model.omop.Concept;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
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

    public String getSources(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain + "source/sources";
        String content = get(url);
        return content;
    }

    public Concept getConceptMetadata(String id) throws IOException, ParseException {
        String url = domain + "vocabulary/" + source + "/concept/" + id;
        String response = get(url);

        JSONParser parser = new JSONParser();
        JSONObject jObj = (JSONObject) parser.parse(response.toString());

        Concept concept = new Concept();
        concept.setId("" + jObj.get(Terms.CONCEPT_ID));
        concept.setName("" + jObj.get(Terms.CONCEPT_NAME));
        concept.setStandardConcept("" + jObj.get(Terms.STANDARD_CONCEPT));
        concept.setStandardConceptCaption("" + jObj.get(Terms.STANDARD_CONCEPT_CAPTION));
        concept.setInvalidReason("" + jObj.get(Terms.INVALID_REASON));
        concept.setInvalidReasonCaption("" + jObj.get(Terms.INVALID_REASON_CAPTION));
        concept.setConceptCode("" + jObj.get(Terms.CONCEPT_CODE));
        concept.setDomainId("" + jObj.get(Terms.DOMAIN_ID));
        concept.setVocabularyId("" + jObj.get(Terms.VOCABULARY_ID));
        concept.setConceptClassId("" + jObj.get(Terms.CONCEPT_CLASS_ID));

        return concept;
    }

    /*
     * Posts the cohort definition and returns the id of the saved
     * The id will be -1 if there is an error code
     */
    public String postCohortDefinition(String json) throws IOException, ParseException {
        String url = domain + "cohortdefinition";
        URL obj = new URL(url);
        HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
        postConnection.setDoOutput(true);
        postConnection.setRequestMethod("POST");
        postConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream os = postConnection.getOutputStream();
        os.write(json.getBytes());
        os.flush();
        os.close();

        // TOO - should check for response code, but started to get 403 even though it saved

//        int responseCode = postConnection.getResponseCode();
        StringBuffer response = new StringBuffer();
        JSONObject jObj;
        String id = "-1";
//        if (responseCode == HttpURLConnection.HTTP_OK) { //success
        BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        JSONParser parser = new JSONParser();
        jObj = (JSONObject) parser.parse(response.toString());

        id = "" + jObj.get("id");

        in.close();
        System.out.println(response.toString());

//        } else {
//            System.out.println("POST FAILED");
//        }
        return id;
    }

    public String generateCohort(String id) throws IOException, ParseException {
        String exeId = "-1";
        JSONObject jObj;

        String url = domain + "cohortdefinition/" + id + "/generate/" + source;
        String response = get(url);

        JSONParser parser = new JSONParser();
        jObj = (JSONObject) parser.parse(response.toString());

        exeId = "" + jObj.get("executionId");

        return exeId;
    }

    public String getExecutionStatus(String id) throws IOException, ParseException {
        String status = "";
        JSONArray jArr;

        String url = domain + "cohortdefinition/" + id + "/info";
        String response = get(url);

        JSONParser parser = new JSONParser();
        jArr = (JSONArray) parser.parse(response.toString());
        JSONObject jObj = (JSONObject) jArr.get(0);

        status = "" + jObj.get("status");

        return status;
    }

    public String getCohortCount(String id) throws IOException, ParseException {
        String count = "-1";
        JSONObject jObj;
        JSONArray jArr;

        String url = domain + "cohortdefinition/" + id + "/info";
        String response = get(url);

        JSONParser parser = new JSONParser();
        jArr = (JSONArray) parser.parse(response.toString());
        jObj = (JSONObject) jArr.get(0);

        count = "" + jObj.get("personCount");

        return count;
    }

    private String get(String url) throws MalformedURLException, ProtocolException, IOException {
        StringBuilder content;

        try {
            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
        } finally {
            con.disconnect();
        }

        return content.toString();
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
