package edu.phema.elm_to_omop.valueset;

import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.model.omop.ConceptSet;
import edu.phema.elm_to_omop.model.omop.Items;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that loads a valueset from a file, and uses an OMOP
 * repository service to generate the matched in OMOP concept
 * sets
 */
public class SpreadsheetValuesetService implements IValuesetService {

    // The OMOP service provider
    private IOmopRepositoryService omopService;

    // The location of the valueset spreadsheet
    private String valuesetSpreadsheetPath;

    // The tab in the valueset spreadsheet
    private String valuesetSpreadsheetTab;

    /**
     * Constuctor that takes an instance of an OMOP repository
     * service provider, and the path to a valueset file.
     *
     * @param omopService             The OMOP service provider
     * @param valuesetSpreadsheetPath Path to the valueset file
     * @param valuesetSpreadsheetTab  Spreadsheet tab
     */
    public SpreadsheetValuesetService(IOmopRepositoryService omopService, String valuesetSpreadsheetPath, String valuesetSpreadsheetTab) {
        this.omopService = omopService;
        this.valuesetSpreadsheetPath = valuesetSpreadsheetPath;
        this.valuesetSpreadsheetTab = valuesetSpreadsheetTab;
    }

    @Override
    public List<ConceptSet> getConceptSets() throws Exception {
        ValueSetReader valueSetReader = new ValueSetReader(this.omopService);

        return valueSetReader.getConceptSets(valuesetSpreadsheetPath, valuesetSpreadsheetTab);
    }

    public IOmopRepositoryService getOmopService() {
        return omopService;
    }

    public void setOmopService(IOmopRepositoryService omopService) {
        this.omopService = omopService;
    }

    public String getValuesetSpreadsheetPath() {
        return valuesetSpreadsheetPath;
    }

    public void setValuesetSpreadsheetPath(String valuesetSpreadsheetPath) {
        this.valuesetSpreadsheetPath = valuesetSpreadsheetPath;
    }

    public String getValuesetSpreadsheetTab() {
        return valuesetSpreadsheetTab;
    }

    public void setValuesetSpreadsheetTab(String valuesetSpreadsheetTab) {
        this.valuesetSpreadsheetTab = valuesetSpreadsheetTab;
    }
}
