package edu.phema.elm_to_omop.model.phema;

import edu.phema.elm_to_omop.io.IOmopRepository;
import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.model.omop.Concept;
import edu.phema.elm_to_omop.model.omop.ConceptSet;
import edu.phema.elm_to_omop.model.omop.InclusionRule;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class LibraryHelperTest {

    private CqlTranslator translator;
    private Library library;
    private List<ConceptSet> conceptSets;

    @Mock
    private IOmopRepository omopRepository;

    private ValueSetReader vsReader;

    @BeforeEach
    public void setup() throws IOException, ParseException, InvalidFormatException {
        MockitoAnnotations.initMocks(this);

        when(omopRepository.getConceptMetadata("", "", "1.2.3.4.5")).thenReturn(new Concept());
        vsReader = new ValueSetReader(omopRepository);

        ModelManager modelManager = new ModelManager();
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("LibraryHelperTests.cql"), modelManager, new LibraryManager(modelManager));
        library = translator.toELM();
        conceptSets = vsReader.getConceptSets(
            this.getClass().getClassLoader().getResource("LibraryHelperTests.csv").getPath(), "LibraryHelperTests", "", "");
    }

    @Test
    void getExpressionDefByName_Null() {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, null);
        assertNull(expression);

        expression = LibraryHelper.getExpressionDefByName(null, "Expression");
        assertNull(expression);
    }

    @Test
    void getExpressionDefByName_Invalid() {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Invalid Expression Name");
        assertNull(expression);
    }

    @Test
    void getExpressionDefByName_Valid() {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Exists direct condition");
        assertNotNull(expression);
    }

    @Test
    void generateInclusionRule_ExistsDirectCondition() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Exists direct condition");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"Diabetes\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_ExistsFromExpression() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Exists from expression");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"Diabetes Expression\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_ExistsFromReferencedExpression() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Exists from referenced expression");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"Exists from expression\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_OrDirectConditions() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Or direct conditions");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"One or more of the following\",  \"expression\": {\"Type\": \"ANY\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} , { \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 1 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_BooleanFromExpressions() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Or from expressions");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"One or more of the following\",  \"expression\": {\"Type\": \"ANY\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} , { \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 1 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());

        expression = LibraryHelper.getExpressionDefByName(library, "And from expressions");
        rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"All of the following\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} , { \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 1 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_OrMixedDirectAndExpression() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Or mixed direct and expression");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"One or more of the following\",  \"expression\": {\"Type\": \"ANY\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} , { \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 1 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_OrFromReferencedExpressions() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Or from referenced expressions");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"One or more of the following\",  \"expression\": {\"Type\": \"ANY\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} , { \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 1 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_CountDirectCondition() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Greater than direct condition");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"Greater\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 3 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());

        expression = LibraryHelper.getExpressionDefByName(library, "Greater than or equal direct condition");
        rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"GreaterOrEqual\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 2 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());

        expression = LibraryHelper.getExpressionDefByName(library, "Equal direct condition");
        rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"Equal\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 0, \"Count\": 2 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());

        expression = LibraryHelper.getExpressionDefByName(library, "Less than direct condition");
        rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"Less\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 1, \"Count\": 1 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());

        expression = LibraryHelper.getExpressionDefByName(library, "Less than or equal direct condition");
        rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"LessOrEqual\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 1, \"Count\": 2 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }

    @Test
    void generateInclusionRule_CountFromExpression_Invalid() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Count from expression");
        assertThrows(Exception.class, () -> LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets));
    }

    @Test
    void generateInclusionRule_CountExpressionReference() throws Exception {
        ExpressionDef expression = LibraryHelper.getExpressionDefByName(library, "Count expression reference");
        InclusionRule rule = LibraryHelper.generateInclusionRule(library, expression.getExpression(), conceptSets);
        assertNotNull(rule);
        assertEquals("{\"name\": \"Diabetes with Count\",  \"expression\": {\"Type\": \"ALL\",  \"CriteriaList\": [{ \"Criteria\": { \"ConditionOccurrence\": {  \"CodesetId\": 0 } } , \"StartWindow\": { \"Start\": {  \"Coeff\": -1 },  \"End\": { \"Coeff\": 1 } } , \"Occurrence\": { \"Type\": 2, \"Count\": 3 }} ], \"DemographicCriteriaList\": [], \"Groups\": [] }}",
            rule.getJsonFragment());
    }
}
