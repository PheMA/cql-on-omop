package edu.phema.elm_to_omop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhemaTestHelper {
    public static String getFileAsString(String filePath) throws IOException {
        URL url = PhemaTestHelper.class.getClassLoader().getResource(filePath);

        File file = new File(url.getPath());
        StringBuilder fileContents = new StringBuilder((int) file.length());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + System.lineSeparator());
            }
            return fileContents.toString();
        }
    }

    public static String getResourcePath(String resource) {
        return PhemaTestHelper.class.getClassLoader().getResource(resource).getPath();
    }

    public static void assertStringsEqualIgnoreWhitespace(String lhs, String rhs) {
        String left = lhs.replaceAll("\\s+", "");
        String right = rhs.replaceAll("\\s+", "");

        assertEquals(left, right);
    }

    public static String getJson(Object o) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper.writeValueAsString(o);
    }

    public static ExpressionDef getExpressionDefByName(Library library, String expressionName) {
        if (library == null || expressionName == null) {
            return null;
        }

        Optional<ExpressionDef> phenotypeExpression = library.getStatements().getDef().stream()
            .filter(x -> expressionName.equals(x.getName()))
            .findFirst();
        return phenotypeExpression.isPresent() ? phenotypeExpression.get() : null;
    }
}
