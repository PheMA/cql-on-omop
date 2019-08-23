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

    private static String vocabSouce;
    private static String serverUrl;

    public static List<ConceptSet> getConceptSets(Library elmContents, String directory, Logger logger, String domain, String source) throws MalformedURLException, ProtocolException, IOException, ParseException, InvalidFormatException {
        vocabSouce = source;
        serverUrl = domain;
        String vsDirectory = directory + Config.getVsFileName();

        SpreadsheetReader vsReader = new SpreadsheetReader();
        ArrayList<PhemaValueSet>  codes = new ArrayList<PhemaValueSet> ();
        codes = vsReader.getSpreadsheetData(vsDirectory, Config.getTab());
        List<ConceptSet> conceptSets = getConceptSets(elmContents, codes);

        return conceptSets;
    }

    private static List<ConceptSet> getConceptSets(Library elmContents, ArrayList<PhemaValueSet>  pvsList) throws MalformedURLException, ProtocolException, IOException, ParseException {
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
                concept = OmopRepository.getConceptMetadata(serverUrl, vocabSouce, code.getCode());

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
