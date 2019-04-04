package edu.phema.elm_to_omop.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model_omop.ConceptSets;
import edu.phema.elm_to_omop.model_omop.ExpressionDefinition;
import edu.phema.elm_to_omop.model_omop.OmopRoot;

public class OmopWriter {

    public static String writeOmopJson(Library elmContents, ConceptSets conceptSets, String directory, Logger inLogger)  throws IOException {
        String json = writeOmopJson(elmContents, conceptSets);

        try (FileWriter jsonFile = new FileWriter(directory +"ohdsiCohortDefinition.json")) {
            jsonFile.write(json);
        }
        
        return json;
    }
    
    
    private static String writeOmopJson(Library elmContents, ConceptSets conceptSets) throws IOException   {
        ExpressionDefinition exDef = new ExpressionDefinition();
        exDef.setConceptSets(conceptSets);
        
        OmopRoot root = new OmopRoot();
        root.setExpression(exDef);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
//        String dtoMapAsString = mapper.writeValueAsString(dtoMap);
        String dtoMapAsString = mapper.writeValueAsString(root);
        dtoMapAsString = dtoMapAsString.replace("{\"expression\":", "");
//        logger.info(dtoMapAsString);
        
        return dtoMapAsString;
    }
    
}
