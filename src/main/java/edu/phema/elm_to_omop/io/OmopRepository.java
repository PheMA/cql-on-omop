package edu.phema.elm_to_omop.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.phema.elm_to_omop.helper.Terms;
import edu.phema.elm_to_omop.model.omop.Concept;

/**
 * This class uses the WebAPI to interact with with the OMOP repository.
 */
public class OmopRepository {

    private static HttpURLConnection con;

    public static String getSources(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"source/sources";
        String content = get(url);
        return content;
    }

    public static Concept getConceptMetadata(String domain, String source, String id) throws MalformedURLException, ProtocolException, IOException, ParseException  {
        String url = domain +"vocabulary/" +source+ "/concept/" +id;
        String response = get(url);

        JSONParser parser = new JSONParser();
        JSONObject jObj = (JSONObject) parser.parse(response.toString());

        Concept concept = new Concept();
        concept.setId("" +jObj.get(Terms.CONCEPT_ID));
        concept.setName("" +jObj.get(Terms.CONCEPT_NAME));
        concept.setStandardConcept("" +jObj.get(Terms.STANDARD_CONCEPT));
        concept.setStandardConceptCaption("" +jObj.get(Terms.STANDARD_CONCEPT_CAPTION));
        concept.setInvalidReason("" +jObj.get(Terms.INVALID_REASON));
        concept.setInvalidReasonCaption("" +jObj.get(Terms.INVALID_REASON_CAPTION));
        concept.setConceptCode("" +jObj.get(Terms.CONCEPT_CODE));
        concept.setDomainId("" +jObj.get(Terms.DOMAIN_ID));
        concept.setVocabularyId("" +jObj.get(Terms.VOCABULARY_ID));
        concept.setConceptClassId("" +jObj.get(Terms.CONCEPT_CLASS_ID));

        return concept;
    }

   /*
    * Posts the cohort definition and returns the id of the saved
    * The id will be -1 if there is an error code
    */
    public static String postCohortDefinition(String domain, String json) throws MalformedURLException, ProtocolException, IOException, ParseException {
        String url = domain +"cohortdefinition";
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

            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            }

            JSONParser parser = new JSONParser();
            jObj = (JSONObject) parser.parse(response.toString());

            id = "" +jObj.get("id");

            in .close();
            System.out.println(response.toString());

//        } else {
//            System.out.println("POST FAILED");
//        }
        return id;
    }

    public static String generateCohort(String domain, String id, String source) throws MalformedURLException, ProtocolException, IOException, ParseException  {
        String exeId = "-1";
        JSONObject jObj;

        String url = domain +"cohortdefinition/" +id +"/generate/" +source;
        String response = get(url);

        JSONParser parser = new JSONParser();
        jObj = (JSONObject) parser.parse(response.toString());

        exeId = "" +jObj.get("executionId");

        return exeId;
    }

    public static String getExecutionStatus(String domain, String id) throws MalformedURLException, ProtocolException, IOException, ParseException  {
        String status = "";
        JSONArray jArr;

        String url = domain +"cohortdefinition/" +id +"/info";
        String response = get(url);

        JSONParser parser = new JSONParser();
        jArr = (JSONArray) parser.parse(response.toString());
        JSONObject jObj = (JSONObject)jArr.get(0);

        status = "" +jObj.get("status");

        return status;
    }

    public static String getCohortCount(String domain, String id, String source) throws MalformedURLException, ProtocolException, IOException, ParseException  {
        String count = "-1";
        JSONObject jObj;
        JSONArray jArr;

        String url = domain +"cohortdefinition/" +id +"/info";
        String response = get(url);

        JSONParser parser = new JSONParser();
        jArr = (JSONArray) parser.parse(response.toString());
        jObj = (JSONObject)jArr.get(0);

        count = "" +jObj.get("personCount");

        return count;
    }

    private static String get(String url) throws MalformedURLException, ProtocolException, IOException {
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

}
