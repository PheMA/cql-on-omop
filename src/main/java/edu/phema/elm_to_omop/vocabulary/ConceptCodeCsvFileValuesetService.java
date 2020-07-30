package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.helper.Terms;
import edu.phema.elm_to_omop.io.SpreadsheetReader;
import edu.phema.elm_to_omop.io.SpreadsheetWriter;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaValueSet;
import edu.phema.elm_to_omop.vocabulary.translate.PhemaVocabularyTranslator;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConceptCodeCsvFileValuesetService implements IValuesetService {
    private IOmopRepositoryService omopService;

    private String csvPath;
    private int conceptSetId;
    private List<PhemaConceptSet> conceptSetCache;
    private boolean enableCaching;

    public ConceptCodeCsvFileValuesetService(IOmopRepositoryService omopService, String csvPath, boolean enableCaching) {
        this.omopService = omopService;
        this.csvPath = csvPath;
        this.enableCaching = enableCaching;

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

    /**
     * Translate the code if supported by the translator
     *
     * @param code The code to potentially translate
     * @return The translated code if translation is supported, else the original code
     * @throws PhemaTranslationException
     */
    private PhemaCode translateIfSupported(PhemaCode code) throws PhemaTranslationException {
        if (PhemaVocabularyTranslator.translationSupportedForCodeSystem(code.getCodeSystem())) {
            return PhemaVocabularyTranslator.translateCode(code);
        }

        return code;
    }

    private ArrayList<PhemaCode> translateCodes(ArrayList<PhemaCode> codes) throws PhemaTranslationException {
        ArrayList<PhemaCode> translated = new ArrayList<>();

        for (PhemaCode code : codes) {
            PhemaCode translatedCode = translateIfSupported(code);

            if (!translated.contains(translatedCode)) {
                translated.add(translatedCode);
            }
        }

        return translated;
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
                conceptSet.name = phemaValueSet.getName();
                conceptSet.setOid(phemaValueSet.getOid());

                ArrayList<PhemaCode> translatedCodes = translateCodes(phemaValueSet.getCodes());

                items = new ArrayList<>();

                for (PhemaCode code : translatedCodes) {
                    List<Concept> concepts = null;
                    // If we have a cached entry with the OMOP concept ID, we can bypass the vocabulary search
                    String omopConceptId = code.getOmopConceptId();
                    if (omopConceptId == null || omopConceptId.equals("")) {
                      concepts = omopService.vocabularySearch(code.getCode(), code.getCodeSystem());
                      // Filter for only exact matches (this isn't possible current with the OHDSI WebAPI)
                      concepts = concepts.stream()
                        .filter(c -> c.conceptCode.equals(code.getCode()))
                        .collect(Collectors.toList());
                    }
                    else {
                      Concept concept = omopService.getConceptMetadata(code.getOmopConceptId());
                      if (concept != null) {
                          concepts = new ArrayList<Concept>(){{ add(concept); }};
                      }
                    }

                    if (concepts.size() > 1) {
                        // The concept code is not specific enough
                        throw new ValuesetServiceException(String.format("Concept code %s does not specify a single concept in code system %s", code.getCode(), code.getCodeSystem()), null);
                    } else if (concepts.isEmpty()) {
                        // The code is missing in the OMOP instance
                        conceptSets.addNotFoundCode(code);
                    } else {
                        ConceptSetExpression.ConceptSetItem item = new ConceptSetExpression.ConceptSetItem();
                        item.concept = concepts.get(0);
                        code.setOmopConceptId(item.concept.conceptId.toString()); // Set this for later caching
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

            if (this.enableCaching) {
                cacheValueSets(csvFilePath + Terms.VS_CACHE_FILE_SUFFIX, valueSets);
            }
        } catch (Exception e) {
            throw new ValuesetServiceException(String.format("Error reading valuset file: %s", csvFilePath), e);
        }

        return conceptSets;
    }

    private void cacheValueSets(String cacheFile, ArrayList<PhemaValueSet> valueSets) throws IOException {
        SpreadsheetWriter writer = new SpreadsheetWriter();
        writer.writeValueSetsCache(cacheFile, valueSets);
    }


    @Override
    public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
        if (this.conceptSetCache == null) {
          this.conceptSetCache = getConceptSetList().getConceptSets();
        }

        return this.conceptSetCache;
    }
}
