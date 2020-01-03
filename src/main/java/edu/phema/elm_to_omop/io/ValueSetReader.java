package edu.phema.elm_to_omop.io;

import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaValueSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression.ConceptSetItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads Value sets from a spreadsheet formatted in PhEMA authoring tool standard.
 */
public class ValueSetReader {
    private IOmopRepositoryService repository;

    public ValueSetReader(IOmopRepositoryService repository) {
        this.repository = repository;
    }

    public List<PhemaConceptSet> getConceptSets(String valueSetPath, String tab) throws IOException, OmopRepositoryException, InvalidFormatException {
        SpreadsheetReader vsReader = new SpreadsheetReader();
        ArrayList<PhemaValueSet> codes = vsReader.getSpreadsheetData(valueSetPath, tab);
        return getConceptSets(codes);
    }

    private List<PhemaConceptSet> getConceptSets(ArrayList<PhemaValueSet> pvsList) throws OmopRepositoryException {
        List<PhemaConceptSet> conceptSets = new ArrayList<>();
        ConceptSetExpression expression = null;

        int conceptSetId = 0;
        PhemaConceptSet conceptSet = null;
        ArrayList<ConceptSetItem> itemsList = null;
        for (PhemaValueSet pvs : pvsList) {
            conceptSet = new PhemaConceptSet();
            conceptSet.setOid(pvs.getOid());

            conceptSet.id = conceptSetId;
            conceptSet.name = pvs.getName();

            ArrayList<PhemaCode> codes = pvs.getCodes();
            itemsList = new ArrayList<>();
            for (PhemaCode code : codes) {
                List<Concept> concepts = repository.vocabularySearch(code.getCode(), code.getCodeSystem());

                // Filter for only exact matches (this isn't possible current with the OHDSI WebAPI)
                concepts = concepts.stream()
                    .filter(c -> c.conceptCode.equals(code.getCode()))
                    .collect(Collectors.toList());

                for (Concept concept : concepts) {
                    ConceptSetItem item = new ConceptSetItem();
                    item.concept = concept;
                    itemsList.add(item);
                }
            }

            ConceptSetItem[] items = new ConceptSetItem[itemsList.size()];
            expression = new ConceptSetExpression();
            expression.items = itemsList.toArray(items);

            conceptSet.expression = expression;

            conceptSets.add(conceptSet);
            conceptSetId++;
        }

        return conceptSets;
    }

}
