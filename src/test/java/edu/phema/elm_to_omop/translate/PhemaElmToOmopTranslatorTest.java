package edu.phema.elm_to_omop.translate;

import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.vocabulary.Concept;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PhemaElmToOmopTranslatorTest {
    // In our CQL file, we may allow true errors to occur for our testing.  Since everything is in one file, we need
    // to define the threshold of known errors here.
    private static final int ALLOWED_ERRORS_IN_CQL = 1;

    private CqlTranslator translator;
    private Library library;
    private List<PhemaConceptSet> conceptSets;

    @Mock
    private IOmopRepositoryService omopRepository;

    private IValuesetService valuesetService;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        lenient().when(omopRepository.getConceptMetadata("1.2.3.4")).thenReturn(new Concept());

        String vsPath = "/LibraryHelperTests.csv";
        valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");

        ModelManager modelManager = new ModelManager();
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("LibraryHelperTests.cql"), modelManager, new LibraryManager(modelManager));
        library = translator.toELM();

        List<CqlTranslatorException> errors = translator.getErrors();
        if (errors.size() > ALLOWED_ERRORS_IN_CQL) {
            throw new Exception("Too many errors in CQL - stopping");
        }

        conceptSets = valuesetService.getConceptSets();
    }

    @Test
    void getExpressionDefByName_Null() {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, null);
        assertNull(expression);

        expression = PhemaElmToOmopTranslator.getExpressionDefByName(null, "Expression");
        assertNull(expression);
    }

    @Test
    void getExpressionDefByName_Invalid() {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Invalid Expression Name");
        assertNull(expression);
    }

    @Test
    void getExpressionDefByName_Valid() {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Exists direct condition");
        assertNotNull(expression);
    }

    @Test
    void generateInclusionRule_ExistsDirectCondition() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Exists direct condition");

        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(ce);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/ExistsDirectCondition.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_ExistsFromExpression() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Exists from expression");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/ExistsFromExpression.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_ExistsFromReferencedExpression() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Exists from referenced expression");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/ExistsFromReferencedExpression.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_OrDirectConditions() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Or direct conditions");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/OrDirectConditions.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_BooleanFromExpressions() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Or from expressions");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/BooleanFromExpressions.1.json"),
            PhemaTestHelper.getJson(rule));

        expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "And from expressions");
        ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/BooleanFromExpressions.2.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_OrMixedDirectAndExpression() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Or mixed direct and expression");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/OrMixedDirectAndExpression.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_OrFromReferencedExpressions() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Or from referenced expressions");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/OrFromReferencedExpressions.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_CountDirectCondition() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Greater than direct condition");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/CountDirectCondition.1.json"),
            PhemaTestHelper.getJson(rule));

        expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Greater than or equal direct condition");
        ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/CountDirectCondition.2.json"),
            PhemaTestHelper.getJson(rule));

        expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Equal direct condition");
        ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/CountDirectCondition.3.json"),
            PhemaTestHelper.getJson(rule));

        expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Less than direct condition");
        ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/CountDirectCondition.4.json"),
            PhemaTestHelper.getJson(rule));

        expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Less than or equal direct condition");
        ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/CountDirectCondition.5.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_CountFromExpression_Invalid() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Count from expression");
        assertThrows(Exception.class, () -> PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets));
    }

    @Test
    void generateInclusionRule_CountExpressionReference() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Count expression reference");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/CountExpressionReference.json"),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    void generateInclusionRule_NestedBooleanDirectConditions() throws Exception {
        // This expression is in the form of:
        //   item1 AND (item2 OR item3)
        // We expect just one expression because the item1 expression will be turned into the criteria for the expression,
        // and the items in the parentheses will be represented in that criteria's Groups collection.
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Nested boolean direct conditions");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        List<org.ohdsi.circe.cohortdefinition.InclusionRule> rules = ce.inclusionRules;
        assertNotNull(rules);
        assertEquals(1, rules.size());
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/NestedBooleanDirectConditions.1.json"),
            PhemaTestHelper.getJson(rules.get(0)));

        // This expression is in the form of:
        //   (item1 OR item2) AND (item1 OR item3)
        // We still expect just one rule to be created, because the criteria in parentheses will get unpacked into the
        // Groups portion of the initial expression.  Note that there is no criteria in the CriteriaList - this is
        // correct, and is allowed by Atlas.
        expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Two nested boolean direct conditions");
        ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        rules = ce.inclusionRules;
        assertNotNull(rules);
        assertEquals(1, rules.size());
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/NestedBooleanDirectConditions.2.json"),
            PhemaTestHelper.getJson(rules.get(0)));
    }

    @Test
    void generateInclusionRule_NestedBooleanFromReferencedBooleanExpressions() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Nested boolean from referenced boolean expressions");
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        List<org.ohdsi.circe.cohortdefinition.InclusionRule> rules = ce.inclusionRules;
        assertNotNull(rules);
        assertEquals(1, rules.size());
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/NestedBooleanFromReferencedBooleanExpressions.json"),
            PhemaTestHelper.getJson(rules.get(0)));
    }

//    @Test
//    void generateInclusionRule_Age() throws Exception {
//      ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Age");
//      //ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "tmp");
//      List<InclusionRule> rules = LibraryHelper.generateInclusionRules(library, expression.getExpression(), conceptSets);
//      assertNotNull(rules);
//    }

//    @Test
//    void generateInclusionRule_TemporalRelationship() throws Exception {
//        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Temporal relationship");
//        List<InclusionRule> rules = LibraryHelper.generateInclusionRules(library, expression.getExpression(), conceptSets);
//        assertNotNull(rules);
//        assertEquals(1, rules.size());
//        assertEquals("{\"name\": \"Diabetes\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": { \"CorrelatedCriteria\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": { \"CodesetId\": 1 } }, \"StartWindow\": { \"Start\": {  \"Coeff\": -1, \"Days\": 30 }, \"End\": { \"Coeff\": 1, \"Days\": 0 } }, \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] },\"CodesetId\": 0 } }, \"StartWindow\": { \"Start\": {  \"Coeff\": -1 }, \"End\": { \"Coeff\": 1 } }, \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
//            rules.get(0).getJsonFragment());
//    }

    @Test
    void convertToDays_NullEmpty() {
        // Entire object is null or empty
        assertThrows(PhemaAssumptionException.class, () -> PhemaElmToOmopTranslator.convertToDays(null));
        assertThrows(PhemaAssumptionException.class, () -> PhemaElmToOmopTranslator.convertToDays(new Quantity()));
    }

    @Test
    void convertToDays_MissingAttributes() {
        // One of our required attributes is missing
        Quantity quantity = new Quantity();
        quantity.setUnit("days");
        assertThrows(PhemaAssumptionException.class, () -> PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit(null);
        quantity.setValue(BigDecimal.valueOf(100));
        assertThrows(PhemaAssumptionException.class, () -> PhemaElmToOmopTranslator.convertToDays(quantity));
    }

    @Test
    void convertToDays_UnknownUnits() {
        Quantity quantity = new Quantity();
        quantity.setValue(BigDecimal.valueOf(100));

        quantity.setUnit("d");
        assertThrows(PhemaNotImplementedException.class, () -> PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit("blah");
        assertThrows(PhemaNotImplementedException.class, () -> PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit("dayss");
        assertThrows(PhemaNotImplementedException.class, () -> PhemaElmToOmopTranslator.convertToDays(quantity));
    }

    @Test
    void convertToDays_NoConversion() throws PhemaAssumptionException, PhemaNotImplementedException {
        Quantity quantity = new Quantity();
        quantity.setUnit("days");
        quantity.setValue(BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(100), PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit("day");
        quantity.setValue(BigDecimal.valueOf(1));
        assertEquals(BigDecimal.valueOf(1), PhemaElmToOmopTranslator.convertToDays(quantity));
    }

    @Test
    void convertToDays_Years() throws PhemaAssumptionException, PhemaNotImplementedException {
        Quantity quantity = new Quantity();
        quantity.setUnit("years");
        quantity.setValue(BigDecimal.valueOf(10));
        assertEquals(BigDecimal.valueOf(3650), PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit("year");
        quantity.setValue(BigDecimal.valueOf(1));
        assertEquals(BigDecimal.valueOf(365), PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit("year");
        quantity.setValue(BigDecimal.valueOf(0.5));
        assertEquals(BigDecimal.valueOf(182.5), PhemaElmToOmopTranslator.convertToDays(quantity));
    }

    @Test
    void convertToDays_Months() throws PhemaAssumptionException, PhemaNotImplementedException {
        Quantity quantity = new Quantity();
        quantity.setUnit("months");
        quantity.setValue(BigDecimal.valueOf(10));
        assertEquals(BigDecimal.valueOf(300), PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit("month");
        quantity.setValue(BigDecimal.valueOf(1));
        assertEquals(BigDecimal.valueOf(30), PhemaElmToOmopTranslator.convertToDays(quantity));
        quantity.setUnit("month");
        quantity.setValue(BigDecimal.valueOf(0.5));
        assertEquals(BigDecimal.valueOf(15.0), PhemaElmToOmopTranslator.convertToDays(quantity));
    }
}
