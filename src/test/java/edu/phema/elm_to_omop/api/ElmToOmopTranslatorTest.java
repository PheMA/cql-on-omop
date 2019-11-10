package edu.phema.elm_to_omop.api;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.io.IOmopRepository;
import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.model.omop.Concept;
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
    private IOmopRepository omopRepository;

    private ValueSetReader valueSetReader;

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

        Mockito.when(omopRepository.getConceptMetadata("http://52.162.236.199/WebAPI/", "OHDSI-CDMV5", "45917083")).thenReturn(concept);
        valueSetReader = new ValueSetReader(omopRepository);
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

        config.setVsFileName(getClass().getClassLoader().getResource("api/valuesets/simple-valueset.csv").getPath());

        assertDoesNotThrow(() -> {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config, valueSetReader);
        });
    }

    @Test
    void omopTranslatorApiSmokeTest() {
        Config config = new Config(Config.getDefaultConfigPath());

        config.setVsFileName(getClass().getClassLoader().getResource("api/valuesets/simple-valueset.csv").getPath());

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config, valueSetReader);

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

        config.setVsFileName(getClass().getClassLoader().getResource("api/valuesets/simple-valueset.csv").getPath());

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config, valueSetReader);

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
