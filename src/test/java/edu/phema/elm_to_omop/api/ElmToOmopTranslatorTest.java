package edu.phema.elm_to_omop.api;

import edu.phema.elm_to_omop.helper.Config;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class ElmToOmopTranslatorTest {

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
        Config config = new Config(new String[]{});

        config.setVsFileName("diabetes/diabetes.csv");

        assertDoesNotThrow(() -> {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config);
        });
    }

    @Test
    void omopTranslatorApiSmokeTest() {
        Config config = new Config(new String[]{});

        config.setVsFileName("diabetes/diabetes.csv");

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config);

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
        Config config = new Config(new String[]{});

        config.setVsFileName("diabetes/diabetes.csv");

        try {
            ElmToOmopTranslator translate = new ElmToOmopTranslator(config);

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
