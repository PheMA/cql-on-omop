package edu.phema.elm_to_omop;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.helper.MyFormatter;
import edu.phema.elm_to_omop.io.ElmReader;
import edu.phema.elm_to_omop.io.FileWriter;
import edu.phema.elm_to_omop.io.OmopRepository;
import edu.phema.elm_to_omop.io.OmopWriter;
import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model_omop.ConceptSets;


public class EmlToOmopConverter 
{
    private static Logger logger = Logger.getLogger(EmlToOmopConverter.class.getName());
    private static FileHandler fh;  
    
    private static String directory;
    
    public static void main(String args[])  {

        try  {
            fh = new FileHandler("elmToOhdsiConverter.log");  
            logger.addHandler(fh); 
            fh.setFormatter(new MyFormatter());
            
            if (args.length > 0)  
            {
                new Config(args);
            }  
            else   
            {
                new Config();
            }
    
            String workingDir = System.getProperty("user.dir");
            directory = workingDir + File.separator + "resources" +File.separator;
            
            Library elmContents = ElmReader.readElm(directory, logger);
    
            ConceptSets conceptSets = ValueSetReader.getConcepts(elmContents, directory, logger);
            
            String omopJson = OmopWriter.writeOmopJson(elmContents, conceptSets, directory, logger);
            
            String domain = Config.getOmopBaseUrl();
            String sources = OmopRepository.getSources(domain);
            FileWriter.write(directory + "sources.json", sources);
            
            String search = OmopRepository.getSearch(domain, "cardiomyopathy");
            FileWriter.write(directory + "search.json", search);
    
//            String cohortCharacter = OmopRepository.getCohortCharacter(domain);
//            FileWriter.write(directory + "cohortCharacter.json", cohortCharacter);
    
            String conceptSet = OmopRepository.getConceptSet(domain);
            FileWriter.write(directory + "conceptSet.json", conceptSet);
    
            String conceptSetById = OmopRepository.getConceptSetById(domain, 8168);
            FileWriter.write(directory + "conceptSetById.json", conceptSetById);
            
            String conceptSetExpression = OmopRepository.getConceptSetExpression(domain, 8168);
            FileWriter.write(directory + "conceptSetExpression.json", conceptSetExpression);
            
            String conceptItems = OmopRepository.getConceptItems(domain, 8168);
            FileWriter.write(directory + "conceptItems.json", conceptItems);
            
//            String cohortDefinition = OmopRepository.getCohortDefinition(domain);
//            FileWriter.write(directory + "cohortDefinition.json", cohortDefinition);

            String cohortDefinitionById = OmopRepository.getCohortDefinitionById(domain, 1);
            FileWriter.write(directory + "cohortDefinitionById.json", cohortDefinitionById);
            
            System.out.println("JSON - " +omopJson);
    
            String importJson = OmopRepository.postImportJson(domain, omopJson);
            FileWriter.write(directory + "cohortDefinitionById.json", importJson);
            
    //        String cohortDefinitionById = OmopRepository.postCohortDefinitionById(domain, 176393);
    //        FileWriter.write(directory + "cohortDefinitionById.json", cohortDefinitionById);
            
    //        String cohortDefinitionSQL = OmopRepository.getCohortDefinitionSQL(domain);
    //        FileWriter.write(directory + "cohortDefinitioSQLn.json", cohortDefinitionSQL);
            
            
    //        GET /cohortdefinition/
    //        POST /cohortdefinition/
    //        POST /cohortdefinition/sql
    //        GET /cohortdefinition/{id}
    //        PUT /cohortdefinition/{id}
    //        DELETE /cohortdefinition/{id}
    //        GET /cohortdefinition/{id}/cancel/{sourceKey}
    //        GET /cohortdefinition/{id}/check
    //        POST /cohortdefinition/{id}/check
    //        GET /cohortdefinition/{id}/copy
    //        GET /cohortdefinition/{id}/export/conceptset
    //        GET /cohortdefinition/{id}/generate/{sourceKey}
    //        GET /cohortdefinition/{id}/info
    //        GET /cohortdefinition/{id}/report/{sourceKey}
        
            
        }  catch(IOException ioe)   {
            ioe.printStackTrace();
        }  catch(InvalidFormatException ife)   {
            ife.printStackTrace();
        }  catch(JAXBException jaxb)   {
            jaxb.printStackTrace();
        }
        
        System.out.println("\tdone");
    }


    


   
    
    
 
    
}
