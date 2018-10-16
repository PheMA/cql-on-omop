package edu.phema.elm_to_omop.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model_omop.Concept;
import edu.phema.elm_to_omop.model_omop.ConceptSets;
import edu.phema.elm_to_omop.model_omop.Expression;
import edu.phema.elm_to_omop.model_omop.Items;
import edu.phema.elm_to_omop.model_phema.PhemaCode;
import edu.phema.elm_to_omop.model_phema.PhemaValueSet;

public class ValueSetReader {

    public static ConceptSets getConcepts(Library elmContents, String directory, Logger logger) throws IOException, InvalidFormatException {
        
        String vsDirectory = directory + Config.getVsFileName();
        logger.info("vsFile location - " +vsDirectory);
        
        SpreadsheetReader vsReader = new SpreadsheetReader();
        ArrayList<PhemaValueSet>  codes = new ArrayList<PhemaValueSet> ();
        codes = vsReader.getPatientSpreadsheetData(vsDirectory, "diabetes");
        ConceptSets conceptSets = getConceptSets(elmContents, codes);
        
        return conceptSets;
    }
    
    private static ConceptSets getConceptSets(Library elmContents, ArrayList<PhemaValueSet>  pvsList)  {
        
        ConceptSets conceptSets = new ConceptSets();
        Expression expression = new Expression();
        Items items = new Items();
        
        int count = 0;
        for (PhemaValueSet pvs : pvsList) {
            count++;
            
            conceptSets.setId("" +count);
            conceptSets.setName(pvs.getName());
            
            expression = new Expression();
            items = new Items();
            
            ArrayList<PhemaCode> codes = pvs.getCodes();            
            
            for (PhemaCode code : codes) {
                Concept concept = new Concept();
                concept.setConceptCode(code.getCode());
                concept.setName(code.getDescription());
                concept.setVocabularyId(code.getCodeSystem());
                items.add(concept);
            }
        }
        expression.setItems(items);
        conceptSets.setExpression(expression);
        
        return conceptSets;
    }
    
    
    
}
