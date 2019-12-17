package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.io.SpreadsheetReader;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaValueSet;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConceptCodeCsvFileValuesetService implements IValuesetService {
    private IOmopRepositoryService omopService;

    private String csvPath;
    private int conceptSetId;

    public ConceptCodeCsvFileValuesetService(IOmopRepositoryService omopService, String csvPath) {
        this.omopService = omopService;
        this.csvPath = csvPath;

        this.conceptSetId = 0;
    }

    @Override
    public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
        if (this.isDirectory()) {
            List<String> csvFiles = getCsvFilesInDirectory(csvPath);

            List<PhemaConceptSetList> conceptSetListList = new ArrayList<>();
            for (String csvFilename : csvFiles) {
                conceptSetListList.add(this.getConceptSet(csvFilename));
            }

            // Flatten the result into a single PhemaConceptSetList
            PhemaConceptSetList result = new PhemaConceptSetList();
            for (PhemaConceptSetList list : conceptSetListList) {
                result.addAllConceptSets(list.getConceptSets());
                result.addAllNotFoundCodes(list.getNotFoundCodes());
            }

            return result;
        } else {
            return this.getConceptSet(this.csvPath);
        }
    }

    private List<String> getCsvFilesInDirectory(String directoryPath) {
        File directory;

        if (this.isResource()) {
            directory = new File(this.getClass().getResource(csvPath).getPath());
        } else {
            directory = new File(directoryPath);
        }

        File[] csvFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toUpperCase().endsWith(".CSV");
            }
        });

        if (csvFiles == null) {
            return new ArrayList<>();
        } else {
            return Arrays.stream(csvFiles).map(File::getPath).sorted().collect(Collectors.toList());
        }
    }

    /**
     * Determine if we're dealing with a Java resource
     * or an absolute filesystem path
     *
     * @return True if resource, else false
     */
    private boolean isResource() {
        InputStream in = this.getClass().getResourceAsStream(csvPath);
        return in != null;
    }

    /**
     * Determine if we are dealing with a single valueset, or a directory of valuesets
     *
     * @return True if directory, else false
     */
    private boolean isDirectory() {
        if (this.isResource()) {
            return new File(this.getClass().getResource(csvPath).getPath()).isDirectory();
        } else {
            return new File(csvPath).isDirectory();
        }
    }

    private PhemaConceptSetList getConceptSet(String csvFilePath) throws ValuesetServiceException {
        PhemaConceptSetList conceptSets = new PhemaConceptSetList();

        SpreadsheetReader vsReader = new SpreadsheetReader();

        ConceptSetExpression conceptSetExpression = null;
        PhemaConceptSet conceptSet = null;
        ArrayList<ConceptSetExpression.ConceptSetItem> items = null;

        try {
            ArrayList<PhemaValueSet> valueSets = vsReader.getSpreadsheetData(csvFilePath, "");

            for (PhemaValueSet phemaValueSet : valueSets) {

                conceptSet = new PhemaConceptSet();
                conceptSet.id = conceptSetId;
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
