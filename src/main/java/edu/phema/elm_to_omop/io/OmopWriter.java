package edu.phema.elm_to_omop.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model_omop.ConceptSets;
import edu.phema.elm_to_omop.model_omop.ExpressionDefinition;
import edu.phema.elm_to_omop.model_omop.OmopRoot;

public class OmopWriter {

    private static String json = null;
    
    /** 
     * Makes sure the json has been created and writes it to file designated in the configuration
     * Returns the json string
     */
    public static String writeOmopJson(Library elmContents, ConceptSets conceptSets, String directory, Logger inLogger)  throws IOException {
        try (FileWriter jsonFile = new FileWriter(directory +Config.getOutFileName())) {
            jsonFile.write(getOmopJson(elmContents, conceptSets));
        }
        
        return json;
    }
    
    private static String getOmopJson(Library elmContents, ConceptSets conceptSets) throws IOException  {
        if(json==null)  {
            json = setOmopJson(elmContents, conceptSets);
        }
        return json;
    }
    
    private static String setOmopJson(Library elmContents, ConceptSets conceptSets) throws IOException   {
        ExpressionDefinition exDef = new ExpressionDefinition();
        exDef.setExpression(conceptSets);
        
        OmopRoot root = new OmopRoot();
        
        // TODO: using the value set name here.  need to find a better way to name the algorithm
        String name = elmContents.getValueSets().getDef().get(0).getName();
        root.setName(name);
        // TODO: hard coded values
        root.setDescription("none");
        root.setExpressionType("SIMPLE_EXPRESSION");
        root.setExpression(exDef);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        String dtoMapAsString = mapper.writeValueAsString(root);
        
        return dtoMapAsString;
    }
    
}
