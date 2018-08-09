package edu.phema.elm_to_omop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import edu.phema.elm_to_omop.helper.Config;


public class Eml {

//    private static CTS2ValueSetDefinition vsd;
//    
//  
//  public static CTS2ValueSet[] getVsacValueSets() throws Exception {      
//      String url = "http://" +Config.getUmlsUser() +":" +Config.getUmlsPass() +"@" +Config.getCts2Url() +"/valuesets";
//      
//      //put the if statement in if looking to work with versions.  otherwise will get latest version
////      if(config.getVsVer()!=null)  {
////          url = url +"/definition/" +config.getVsVer();
////      }
//      System.out.println(url);
//      
//      HttpResponse<String> response = Unirest.get(url).header("accept",  "application/json").asString();
//
//      JsonElement jelement = new JsonParser().parse(response.getBody());       
//      JsonObject  jobject = jelement.getAsJsonObject();
//      jobject = jobject.getAsJsonObject("valueSetCatalogEntryDirectory");
//      JsonArray jarray = jobject.getAsJsonArray("entryList");
//      
//      int numConcepts = jarray.size();
//      CTS2ValueSet[] valueSets = new CTS2ValueSet[numConcepts];
//      for (int i = 0; i < jarray.size(); i++) 
//      {
//          valueSets[i] = new CTS2ValueSet();
//          
//          jobject = jarray.get(i).getAsJsonObject();
//          
//          for (int j = 0; j < valueSets.length; j++) 
//          {
//              valueSets[i].setValueSetOid(jobject.get("valueSetName").toString().replaceAll("\"", ""));
//              valueSets[i].setValueSetFormalName(jobject.get("formalName").toString().replaceAll("\"", ""));
//          }
//      }
//      return valueSets;
//      
//  }
//    
//    public static CTS2ValueSetDefinition getVsacValueSetDefinition(CTS2ValueSet valueSet) throws Exception {
//
//        vsd = new CTS2ValueSetDefinition();
//        
//        String url = "http://" +Config.getUmlsUser() +":" +Config.getUmlsPass() +"@" +Config.getCts2Url() +"/valueset/" +valueSet.getValueSetOid();
//        //put the if statement in if looking to work with versions.  otherwise will get latest version
////        if(config.getVsVer()!=null)  {
////            url = url +"/definition/" +config.getVsVer();
////        }
//        url = url +"/resolution";
//        //url = url +"/definition";
//
//        System.out.println(url);
//        
//        HttpResponse<String> response = Unirest.get(url)
//          .header("accept",  "application/json")
//          .asString();
//        
//        //JsonReader.setLenient(true);
//        JsonElement jelement = new JsonParser().parse(response.getBody());       
//        JsonObject  jobject = jelement.getAsJsonObject();
//        
//        //setDefinition(jobject) ;
//        setConcepts(jobject);
//
//        return vsd;
//    }
//    
//    
//    private static void setDefinition(JsonObject jobject)  {
//
//        jobject = jobject.getAsJsonObject("iteratableResolvedValueSet");
//        
//        JsonArray jarray = jobject.getAsJsonArray("resolutionInfo");
//        
//        int numConcepts = jarray.size();
//
//    }
//    
//    
//    private static void setConcepts(JsonObject jobject)  {
//        jobject = jobject.getAsJsonObject("iteratableResolvedValueSet");
//        
//        JsonArray jarray = jobject.getAsJsonArray("entryList");
//        
//        int numConcepts = jarray.size();
//        CTS2Concept[] concepts = new CTS2Concept[numConcepts];
//        
//        for (int i = 0; i < jarray.size(); i++) {
//            concepts[i] = new CTS2Concept();
//            
//            jobject = jarray.get(i).getAsJsonObject();
//            
//            concepts[i].setDesignation(jobject.get("designation").toString());
//            concepts[i].setUri(jobject.get("uri").toString());
//            if(jobject.get("href")!=null)  {
//                concepts[i].setHref(jobject.get("href").toString());
//            }
//            concepts[i].setNamespace(codeSystemMapCheck(jobject.get("namespace").toString()));
//
//            concepts[i].setName(jobject.get("name").toString());
//        }
//        
////        VsacToOmopPrinter.printVSAC(concepts[0]);
//        
//        vsd.setConcepts(concepts);
//    }
//    
//    private static String codeSystemMapCheck(String systemName)  {
//        if(systemName.equalsIgnoreCase("\"SNOMEDCT\""))  {
//            return "\"SNOMED\"";
//        }
//        if(systemName.equalsIgnoreCase("\"RXNORM\""))  {
//            return "\"RxNorm\"";
//        }
//        if(systemName.contains("\"CPT\""))  {
//            return "\"CPT4\"";  
//        }
//        if(systemName.contains("\"10\""))  {
//            return "\"ICD10CM\"";  
//        }
//        
//        if(systemName.contains("\"9\""))  {
//            return "\"ICD9CM\"";
//        }
//        return systemName;
//    }
//    
}
