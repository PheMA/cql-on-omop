package edu.phema.elm_to_omop;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.gson.Gson;

import edu.phema.elm_to_omop.helper.Config;


public class Omop {
//    private static String omopBaseURL = Config.getOmopBaseUrl(); 
//    
//    public static void setOmopConceptSet(CTS2Concept[]  concepts, String valueSetName) throws Exception {
//        ArrayList<OMOPConcept[]> omopConcepts = new ArrayList<OMOPConcept[]>();
//        
//        for (CTS2Concept concept : concepts) {
//            OMOPConcept[] omopConcept = getOmopConcepts(concept);
//            //System.out.println(omopConcept.length); 
//            if(omopConcept.length != 0) {
//                omopConcepts.add(omopConcept);
//            }
//            else  {
//                System.out.println("\t" +concept.getNamespace() +" code " +concept.getName() +" has size " +omopConcept.length); 
//            }
//            
//        }
//        
//        // create the container in the repository and populate with omop concepts
////        String containerId = createNewConceptSetContainer(valueSetName);
////        setConceptSet(containerId, omopConcepts);    
//  }
//    
//    
//    private static OMOPConcept[] getOmopConcepts(CTS2Concept concept)  throws IOException {
//     // Going to hard code source for now.  Should do a call to sources first and cycle through to find.
//        String omopURL = omopBaseURL + "OHDSI-CDMV5/vocabulary/search";
//        
//        URL obj = new URL(omopURL); 
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection(); 
//        
//        // Setting basic post request 
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5"); 
//        con.setRequestProperty("Content-Type","application/json");  
//        String postJsonData = "{\"QUERY\":" +concept.getName() +", \"VOCABULARY_ID\":[" +concept.getNamespace() +"]}"; 
//        
//        //System.out.println(postJsonData); 
//        
//        // Send post request 
//        con.setDoOutput(true); 
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream()); 
//        wr.writeBytes(postJsonData); 
//        wr.flush(); 
//        wr.close(); 
//        
//        BufferedReader buffRead = new BufferedReader( new InputStreamReader(con.getInputStream())); 
//        String output; 
//        StringBuffer jsonData = new StringBuffer(); 
//        while ((output = buffRead.readLine()) != null) 
//        { 
//            jsonData.append(output); 
//        } 
//        buffRead.close(); 
//        
//        Gson gson = new Gson();
//        //System.out.println(jsonData.toString()); 
//        OMOPConcept[] omopConcept = gson.fromJson(jsonData.toString(), OMOPConcept[].class);
//        
//        return omopConcept;
//    }
//    
//    private static String createNewConceptSetContainer(String valueSetName) throws Exception {
//        String omopURL = omopBaseURL + "conceptset/";
//        
//        URL obj = new URL(omopURL); 
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection(); 
//        
//        // Setting basic post request 
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5"); 
//        con.setRequestProperty("Content-Type","application/json");  
//        String postJsonData = "{\"name\":\"" +valueSetName +"\", \"id\":0}"; 
//        
//        // Send post request 
//        con.setDoOutput(true); 
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream()); 
//        wr.writeBytes(postJsonData); 
//        wr.flush(); 
//        wr.close(); 
//        
//        BufferedReader buffRead = new BufferedReader( new InputStreamReader(con.getInputStream())); 
//        String output; 
//        StringBuffer jsonData = new StringBuffer(); 
//        while ((output = buffRead.readLine()) != null) 
//        { 
//            jsonData.append(output); 
//        } 
//        buffRead.close(); 
//        
//        Gson gson = new Gson();
////        System.out.println(jsonData.toString()); 
//        OMOPConceptSetContainer omopConceptSetContainer = gson.fromJson(jsonData.toString(), OMOPConceptSetContainer.class);
//        
//        return omopConceptSetContainer.getId();
//        
//    }
//    
//    private static void setConceptSet(String id, ArrayList<OMOPConcept[]> omopConcepts)  throws IOException { 
//        String omopURL = omopBaseURL + "conceptset/" +id +"/items";
//        
////        System.out.println(omopURL);
//        
//        URL obj = new URL(omopURL); 
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection(); 
//        
//        // Setting basic post request 
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5"); 
//        con.setRequestProperty("Content-Type","application/json");  
//        
//        String postJsonData = "[";
//        for (Iterator<OMOPConcept[]> iterator = omopConcepts.iterator(); iterator.hasNext();) {
//            OMOPConcept[] omopConcept = (OMOPConcept[]) iterator.next();
//            postJsonData = postJsonData +",{\"conceptId\":" +omopConcept[0].getConceptId() +",\"isExcluded\":0,\"includeDescendants\":0,\"includeMapped\":0}"; 
//        }
//        postJsonData = postJsonData.replaceFirst(",", "");
//        postJsonData = postJsonData +"]";
//        
//        //System.out.println(postJsonData);
//        
//        // Send post request 
//        con.setDoOutput(true); 
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream()); 
//        wr.writeBytes(postJsonData); 
//        wr.flush(); 
//        wr.close(); 
//        
//        BufferedReader buffRead = new BufferedReader( new InputStreamReader(con.getInputStream())); 
//        String output; 
//        StringBuffer jsonData = new StringBuffer(); 
//        while ((output = buffRead.readLine()) != null) 
//        { 
//            jsonData.append(output); 
//        } 
//        buffRead.close(); 
//
//        
//    }
    
    
//    private static void setConceptSet(String id, OMOPConcept[] omopConcept)  throws IOException {
//        //id="0";
//        String omopURL = omopBaseURL + "conceptset/" +id +"/items";
//        
//        System.out.println(omopURL);
//        
//        URL obj = new URL(omopURL); 
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection(); 
//        
//        // Setting basic post request 
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5"); 
//        con.setRequestProperty("Content-Type","application/json");  
//        
//        String postJsonData = "[{\"conceptId\":" +omopConcept[0].getConceptId() +",\"isExcluded\":0,\"includeDescendants\":0,\"includeMapped\":0}]";
//        
//        
//        System.out.println(postJsonData);
//        
//        // Send post request 
//        con.setDoOutput(true); 
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream()); 
//        wr.writeBytes(postJsonData); 
//        wr.flush(); 
//        wr.close(); 
//        
//        BufferedReader buffRead = new BufferedReader( new InputStreamReader(con.getInputStream())); 
//        String output; 
//        StringBuffer jsonData = new StringBuffer(); 
//        while ((output = buffRead.readLine()) != null) 
//        { 
//            jsonData.append(output); 
//        } 
//        buffRead.close(); 
//
//        System.out.println("done");
//    }
    
}
