package edu.phema.elm_to_omop.io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class OmopRepository {

    private static HttpURLConnection con;

    public static String getSources(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/source/sources";
        String content = get(url);
        return content;
    }

    public static String getSearch(String domain, String term) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/vocabulary/search/" +term;
        String content = get(url);
        return content;
    }
    
    public static String getCohortCharacter(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohort-characterization";
        String content = get(url);
        return content;
    }
    
    public static String getCohortDesign(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohort-characterization/design";
        String content = get(url);
        return content;
    }
    
    public static String getConceptSet(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/conceptset";
        String content = get(url);
        return content;
    }
    
    public static String getConceptSetById(String domain, int id) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/conceptset/" +id;
        String content = get(url);
        return content;
    }
    
    public static String getConceptSetExpression(String domain, int id) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/conceptset/" +id +"/expression";
        String content = get(url);
        return content;
    }
    
    public static String getConceptItems(String domain, int id) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/conceptset/" +id +"/items";
        String content = get(url);
        return content;
    }
    
    public static String getCohortAnalysis(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohortAnalysis";
        String content = get(url);
        return content;
    }
    
    public static String getCohortDefinition(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohortdefinition";
        String content = get(url);
        return content;
    }
    
    public static String getCohortDefinitionById(String domain, int id) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohortdefinition/" +id;
        String content = get(url);
        return content;
    }
    
    public static String postCohortDefinitionById(String domain, int id) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohortdefinition/" +id;
        String content = post(url);
        return content;
    }
    
    public static String getCohortDefinitionSQL(String domain) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohortdefinition/sql";
        String content = get(url);
        return content;
    }
    
    public static String postImportJson(String domain, String json) throws MalformedURLException, ProtocolException, IOException {
        String url = domain +"/WebAPI/cohort/import";
        URL obj = new URL(url);
        HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
        postConnection.setDoOutput(true);
        postConnection.setRequestMethod("POST");
        postConnection.setRequestProperty("Content-Type", "application/json");
        
        OutputStream os = postConnection.getOutputStream();
        os.write(json.getBytes());
        os.flush();
        os.close();
        
        int responseCode = postConnection.getResponseCode();
        System.out.println("POST Response Code :  " + responseCode);
        System.out.println("POST Response Message : " + postConnection.getResponseMessage());
        
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_CREATED) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                postConnection.getInputStream()));
            String inputLine;
            
            while ((inputLine = in .readLine()) != null) {
                response.append(inputLine);
            } in .close();
            // print result
            System.out.println(response.toString());
        } else {
            System.out.println("POST FAILED");
        }
        return response.toString();
    }

    private static String get(String url) throws MalformedURLException, ProtocolException, IOException {
        StringBuilder content;

        try {
            System.out.println("\nSending 'GET' request to URL : " + url);
            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

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
    
    private static String post(String url) throws MalformedURLException, ProtocolException, IOException {

        StringBuilder content;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

//        String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
        
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        content = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        
        return content.toString();
    }
    
}
