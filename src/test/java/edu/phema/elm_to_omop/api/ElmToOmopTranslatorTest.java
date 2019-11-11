package edu.phema.elm_to_omop.api;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.model.omop.Concept;
import edu.phema.elm_to_omop.valueset.IValuesetService;
import edu.phema.elm_to_omop.valueset.SpreadsheetValuesetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ElmToOmopTranslatorTest {
    @Mock
    private IOmopRepositoryService omopRepository;

    private IValuesetService valuesetService;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        Concept concept = new Concept();
        concept.setId("45917083");
        concept.setName("controlledtype1diabeteswithneuropathy");
        concept.setStandardConcept("N");
        concept.setStandardConceptCaption("Non-Standard");
        concept.setInvalidReason("V");
        concept.setInvalidReasonCaption("Valid");
        concept.setConceptCode("154239");
        concept.setDomainId("Condition");
        concept.setVocabularyId("CIEL");
        concept.setConceptClassId("Diagnosis");

        Mockito.when(omopRepository.getConceptMetadata("45917083")).thenReturn(concept);

        String vsPath = "/api/valuesets/simple.csv";

        valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");
    }

    private String getFileAsString(String filePath) throws IOException {
        URL url = this.getClass().getClassLoader().getResource(filePath);

        File file = new File(url.getPath());
        StringBuilder fileContents = new StringBuilder((int) file.length());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + System.lineSeparator());
            }
            return fileContents.toString();
        }
    }

    private void assertStringsEqualIgnoreWhitespace(String lhs, String rhs) {
        String left = lhs.replaceAll("\\s+", "");
        String right = rhs.replaceAll("\\s+", "");

        assertEquals(left, right);
    }

    @Test
    void conceptSetSetupTest() {
        Config config = new Config(Config.getDefaultConfigPath());

        assertDoesNotThrow(() -> {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config, valuesetService);
        });
    }

    @Test
    void omopTranslatorApiSmokeTest() {
        Config config = new Config(Config.getDefaultConfigPath());

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config, valuesetService);

            String cqlString = this.getFileAsString("api/smoke-test-simple.cql");

            String omopJson = translate.cqlToOmopJson(cqlString, "In Initial Population");

            String expected = this.getFileAsString("api/smoke-test-simple.omop.json");

            this.assertStringsEqualIgnoreWhitespace(omopJson, expected);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    void OmopTranslatorMultipleStatementTest() {
        Config config = new Config(Config.getDefaultConfigPath());

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config, valuesetService);

            String cqlString = this.getFileAsString("api/smoke-test-multiple.cql");

            List<String> names = Arrays.asList("In Initial Population", "Another Statement");

            String omopJson = translate.cqlToOmopJson(cqlString, names);

            String expected = this.getFileAsString("api/smoke-test-multiple.omop.json");

            this.assertStringsEqualIgnoreWhitespace(omopJson, expected);
        } catch (Exception e) {
            assertNull(e);
        }
    }
}
