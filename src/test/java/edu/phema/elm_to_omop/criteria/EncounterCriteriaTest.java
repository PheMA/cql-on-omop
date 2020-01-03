package edu.phema.elm_to_omop.criteria;

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

public class EncounterCriteriaTest {
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

    private void runTest(String statementName, String cqlPath, String valuesetPath, String expectedResultPath) throws Exception {
        // Set up the ELM tree
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream(cqlPath), modelManager, libraryManager);
        library = translator.toELM();

        // Use the JSON file valueset service
        String valuesetJson = PhemaTestHelper.getFileAsString(valuesetPath);
        valuesetService = new PhemaJsonConceptSetService(valuesetJson);
        conceptSets = valuesetService.getConceptSets();

        // Generate the cohort expression
        ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, statementName);
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);

        // Assert against expected
        assertNotNull(ce);
        assertEquals(ce.inclusionRules.size(), 1);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString(expectedResultPath),
            PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
    }

    @Test
    public void testSimpleCase() throws Exception {
        runTest("Simple Case",
            "criteria/encounter-criteria.cql",
            "criteria/valuesets/encounter-type-valuesets.omop.json",
            "criteria/translated/encounter/simple-case.omop.json");
    }
}
