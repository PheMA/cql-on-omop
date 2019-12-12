package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.io.SpreadsheetReader;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaValueSet;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConceptCodeCsvFileValuesetService implements IValuesetService {
    private IOmopRepositoryService omopService;

    private String csvFilePath;
    private String valuesetSpreadsheetTab;

    public ConceptCodeCsvFileValuesetService(IOmopRepositoryService omopService, String csvFilePath, String valuesetSpreadsheetTab) {
        this.omopService = omopService;
        this.csvFilePath = csvFilePath;
        this.valuesetSpreadsheetTab = valuesetSpreadsheetTab;
    }

    @Override
    public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
        PhemaConceptSetList conceptSets = new PhemaConceptSetList();

        SpreadsheetReader vsReader = new SpreadsheetReader();

        int conceptSetId = 0;
        ConceptSetExpression conceptSetExpression = null;
        PhemaConceptSet conceptSet = null;
        ArrayList<ConceptSetExpression.ConceptSetItem> items = null;

        try {
            ArrayList<PhemaValueSet> valueSets = vsReader.getSpreadsheetData(csvFilePath, valuesetSpreadsheetTab);

            for (PhemaValueSet phemaValueSet : valueSets) {

                conceptSet = new PhemaConceptSet();
                conceptSet.setOid(phemaValueSet.getOid());

                ArrayList<PhemaCode> codes = phemaValueSet.getCodes();

                items = new ArrayList<>();

                for (PhemaCode code : codes) {

                    List<Concept> concept = omopService.vocabularySearch(code.getCode(), code.getCodeSystem());

                    // Filter for only exact matches (this isn't possible current with the OHDSI WebAPI)
                    concept = concept.stream()
                        .filter(c -> c.conceptCode.equals(code.getCode()))
                        .collect(Collectors.toList());

                    if (concept.size() > 1) {
                        // The concept code is not specific enough
                        throw new ValuesetServiceException(String.format("Concept code %s does not specify a single concept in code system %s", code.getCode(), code.getCodeSystem()), null);
                    } else if (concept.size() == 0) {
                        // The code is missing in the OMOP instance
                        conceptSets.addNotFoundCode(code);
                    } else {
                        ConceptSetExpression.ConceptSetItem item = new ConceptSetExpression.ConceptSetItem();
                        item.concept = concept.get(0);
                        items.add(item);
                    }
                }

                ConceptSetExpression.ConceptSetItem[] itemArray = new ConceptSetExpression.ConceptSetItem[items.size()];
                conceptSetExpression = new ConceptSetExpression();
                conceptSetExpression.items = items.toArray(itemArray);

                conceptSet.expression = conceptSetExpression;

                conceptSets.addConceptSet(conceptSet);
                conceptSetId++;
            }
        } catch (Exception e) {
            throw new ValuesetServiceException(String.format("Error reading valuset file: %s", csvFilePath), e);
        }

        return conceptSets;
    }

    @Override
    public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
        return getConceptSetList().getConceptSets();
    }
}
