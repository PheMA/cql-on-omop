package edu.phema.elm_to_omop.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslator;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.PhemaJsonConceptSetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CorrelatedQueryTest {
    private CqlTranslator translator;
    private Library library;

    private IValuesetService valuesetService;
    private List<PhemaConceptSet> conceptSets;

    private ModelManager modelManager;
    private LibraryManager libraryManager;

    @BeforeEach
    public void setup() throws Exception {
        modelManager = new ModelManager();
        libraryManager = new LibraryManager(modelManager);
    }

    @Test
    public void simpleTest() throws Exception {
        // Set up the ELM tree
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/correlated-query.cql"), modelManager, libraryManager);
        library = translator.toELM();

        // Use the JSON file valueset service
        String valuesetJson = PhemaTestHelper.getFileAsString("criteria/valuesets/correlated-query-valuesets.omop.json");
        valuesetService = new PhemaJsonConceptSetService(valuesetJson);
        conceptSets = valuesetService.getConceptSets();

        CohortExpression target = new ObjectMapper().readValue(PhemaTestHelper.getFileAsString("heart-failure/translated/step-4-full-heart-failure.omop.json"), CohortExpression.class);

        // Generate the cohort expression
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Simplest Case");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);

        // Assert against expected
        assertNotNull(ce);
        assertEquals(ce.inclusionRules.size(), 1);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("criteria/translated/correlated-query/correlated-simple.omop.json"),
            PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
    }
}
