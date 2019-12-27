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

        // Generate the cohort expression
        ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, "Simplest Case");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);

        // Assert against expected
        assertNotNull(ce);
        assertEquals(ce.inclusionRules.size(), 1);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("criteria/translated/correlated-query/correlated-simple.omop.json"),
            PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
    }

    @Test
    public void simplestAggregate() throws Exception {
        // Set up the ELM tree
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/correlated-query.cql"), modelManager, libraryManager);
        library = translator.toELM();

        // Use the JSON file valueset service
        String valuesetJson = PhemaTestHelper.getFileAsString("criteria/valuesets/correlated-query-valuesets.omop.json");
        valuesetService = new PhemaJsonConceptSetService(valuesetJson);
        conceptSets = valuesetService.getConceptSets();

        // Generate the cohort expression
        ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, "Simple Aggregate");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);

        // Assert against expected
        assertNotNull(ce);
        assertEquals(ce.inclusionRules.size(), 1);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("criteria/translated/simple-aggregate.omop.json"),
            PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
    }

    @Test
    public void simpleWithAggregate() throws Exception {
        // Set up the ELM tree
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/correlated-query.cql"), modelManager, libraryManager);
        library = translator.toELM();

        // Use the JSON file valueset service
        String valuesetJson = PhemaTestHelper.getFileAsString("criteria/valuesets/correlated-query-valuesets.omop.json");
        valuesetService = new PhemaJsonConceptSetService(valuesetJson);
        conceptSets = valuesetService.getConceptSets();

        // Generate the cohort expression
        ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, "With Aggregate");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);

        // Assert against expected
        assertNotNull(ce);
        assertEquals(ce.inclusionRules.size(), 1);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("criteria/translated/correlated-query/simple-with-aggregate.omop.json"),
            PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
    }

    @Test
    public void compositeEncounterCriteriaTest() throws Exception {
        // Set up the ELM tree
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/correlated-query.cql"), modelManager, libraryManager);
        library = translator.toELM();

        // Use the JSON file valueset service
        String valuesetJson = PhemaTestHelper.getFileAsString("criteria/valuesets/correlated-query-valuesets.omop.json");
        valuesetService = new PhemaJsonConceptSetService(valuesetJson);
        conceptSets = valuesetService.getConceptSets();

        ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, "Encounter Criteria");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        assertEquals(ce.inclusionRules.size(), 1);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("criteria/translated/correlated-query/composite-encounter-criteria.omop.json"),
            PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
    }

    @Test
    public void adultDiabeticTest() throws Exception {
        // Set up the ELM tree
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/correlated-query.cql"), modelManager, libraryManager);
        library = translator.toELM();

        // Use the JSON file valueset service
        String valuesetJson = PhemaTestHelper.getFileAsString("criteria/valuesets/correlated-query-valuesets.omop.json");
        valuesetService = new PhemaJsonConceptSetService(valuesetJson);
        conceptSets = valuesetService.getConceptSets();

        ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, "Adult Diabetics");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        assertEquals(ce.inclusionRules.size(), 1);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("criteria/translated/correlated-query/adult-diabetics.omop.json"),
            PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
    }
}
