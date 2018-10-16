package edu.phema.elm_to_omop;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.helper.MyFormatter;
import edu.phema.elm_to_omop.io.ElmReader;
import edu.phema.elm_to_omop.io.OmopWriter;
import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model_omop.ConceptSets;


public class EmlToOmopConverter 
{
    private static Logger logger = Logger.getLogger(EmlToOmopConverter.class.getName());
    private static FileHandler fh;  
    
    private static String directory;
    
    public static void main(String args[]) throws Exception {

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
        
        OmopWriter.writeOmopJson(elmContents, conceptSets, directory, logger);

        System.out.println("\tdone");
    }


    


   
    
    
 
    
}
