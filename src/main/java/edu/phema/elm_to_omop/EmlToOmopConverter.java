package edu.phema.elm_to_omop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.io.JAXBReader;
import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model_elm.Library.Parameters;
import edu.phema.elm_to_omop.model_elm.Library.Statements;
import edu.phema.elm_to_omop.model_elm.Library.ValueSets;
import edu.phema.elm_to_omop.model_elm.ValueSetDef;
import edu.phema.elm_to_omop.model_elm.VersionedIdentifier;


public class EmlToOmopConverter 
{
   
    public static void main(String args[]) throws Exception {

        if (args.length > 0)  
        {
            new Config(args);
        }  
        else   
        {
            new Config();
        }
        //EmlToOmopPrinter.printConfig();
        
        readElm();

        System.out.println("\tdone");
    }

    
    private static void readElm() throws FileNotFoundException, IOException, JAXBException  {
        String workingDir = System.getProperty("user.dir");
        File file = new File( workingDir + File.separator + "resources" +File.separator + "diabetes" +File.separator +Config.getElmFileName());

        String json = "";
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        
        JAXBReader xmlReader = new JAXBReader();
        Library elmContents = xmlReader.readXml(file);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        
        Map<String, Object> dtoMap = new HashMap<String, Object>();
        
        VersionedIdentifier vi = elmContents.getIdentifier();
        json = ow.writeValueAsString(vi);
        //System.out.println(json);
        
        Parameters parms = elmContents.getParameters();
        json = ow.writeValueAsString(parms);
        //System.out.println(json);
        
        Statements states = elmContents.getStatements();
        json = ow.writeValueAsString(states);
        //System.out.println(json);
        
        ValueSets vs = elmContents.getValueSets();
        List<ValueSetDef> vsDefs = vs.getDef();
        json = ow.writeValueAsString(vsDefs);
        //System.out.println(json);
        
        dtoMap.put("VersionedIdentifier", vi);
        dtoMap.put("Parameters", parms);
        dtoMap.put("Statements", states);
        dtoMap.put("ValueSets", vs);
        

        String dtoMapAsString = mapper.writeValueAsString(dtoMap);
                
        System.out.println(dtoMapAsString);
               


    }

    
    
}
