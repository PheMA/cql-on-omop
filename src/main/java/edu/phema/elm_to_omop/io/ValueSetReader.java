package edu.phema.elm_to_omop.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hl7.elm.r1.Library;
import org.json.simple.parser.ParseException;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.model.omop.Concept;
import edu.phema.elm_to_omop.model.omop.ConceptSet;
import edu.phema.elm_to_omop.model.omop.Expression;
import edu.phema.elm_to_omop.model.omop.Items;
import edu.phema.elm_to_omop.model.phema.PhemaCode;
import edu.phema.elm_to_omop.model.phema.PhemaValueSet;

/**
 * Reads Value sets from a spreadsheet formatted in PhEMA authoring tool standard.
 */
public class ValueSetReader {
    private IOmopRepository repository;

    public ValueSetReader() {
        this.repository = new OmopRepository();
    }

    public ValueSetReader(IOmopRepository repository) {
        this.repository = repository;
    }

    public List<ConceptSet> getConceptSets(String valueSetPath, String tab, String omopServerUrl, String omopVocabSource) throws IOException, ParseException, InvalidFormatException {
        SpreadsheetReader vsReader = new SpreadsheetReader();
        ArrayList<PhemaValueSet> codes = vsReader.getSpreadsheetData(valueSetPath, tab);
        List<ConceptSet> conceptSets = getConceptSets(codes, omopServerUrl, omopVocabSource);
        return conceptSets;
    }

    private List<ConceptSet> getConceptSets(ArrayList<PhemaValueSet> pvsList, String omopServerUrl, String omopVocabSource) throws IOException, ParseException {
        List<ConceptSet> conceptSets = new ArrayList<ConceptSet>();
        Expression expression = null;

        int conceptSetId = 0;
        ConceptSet conceptSet = null;
        Items items = null;
        for (PhemaValueSet pvs : pvsList) {
            expression = new Expression();
            items = new Items();
            conceptSet = new ConceptSet();
            conceptSet.setId(conceptSetId);
            conceptSet.setOid(pvs.getOid());
            conceptSet.setName(pvs.getName());


            ArrayList<PhemaCode> codes = pvs.getCodes();
            for (PhemaCode code : codes) {
                Concept concept = new Concept();
                concept = repository.getConceptMetadata(omopServerUrl, omopVocabSource, code.getCode());

                items = new Items();
                items.setConcept(concept);
                expression.addItem(items);
            }

            conceptSet.setExpression(expression);
            conceptSets.add(conceptSet);
            conceptSetId++;
        }

        return conceptSets;
    }

}
