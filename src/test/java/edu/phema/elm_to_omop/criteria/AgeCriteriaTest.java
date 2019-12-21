package edu.phema.elm_to_omop.criteria;

import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslator;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

public class AgeCriteriaTest {
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
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/age.phenotype.cql"), modelManager, new LibraryManager(modelManager));
        library = translator.toELM();

        conceptSets = valuesetService.getConceptSets();
    }

    private void runTest(String statementName, String resultFilename) throws Exception {
        ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, statementName);
        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(rule);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString(resultFilename),
            PhemaTestHelper.getJson(rule));
    }

    @Test
    public void AgeSimpleOperatorTests() throws Exception {
        this.runTest("Age gte Value", "criteria/translated/age.gte.value.omop.json");
        this.runTest("Value gte Age", "criteria/translated/value.gte.age.omop.json");

        this.runTest("Age eq Value", "criteria/translated/age.eq.value.omop.json");
        this.runTest("Value eq Age", "criteria/translated/value.eq.age.omop.json");

        this.runTest("Age lte Value", "criteria/translated/age.lte.value.omop.json");
        this.runTest("Value lte Age", "criteria/translated/value.lte.age.omop.json");

        this.runTest("Age neq Value", "criteria/translated/age.neq.value.omop.json");
        this.runTest("Value neq Age", "criteria/translated/value.neq.age.omop.json");

        this.runTest("Age lt Value", "criteria/translated/age.lt.value.omop.json");
        this.runTest("Value lt Age", "criteria/translated/value.lt.age.omop.json");

        this.runTest("Age gt Value", "criteria/translated/age.gt.value.omop.json");
        this.runTest("Value gt Age", "criteria/translated/value.gt.age.omop.json");
    }

    @Test
    public void AgeErrorCases() {
        // Make sure we fail if age is not specified in years
        final ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, "Error bad precision");
        assertThrows(PhemaNotImplementedException.class, () -> PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets));

        // The translator will give us a type mismatch error here
        PhemaTestHelper.getExpressionDefByName(library, "Error non-numeric");
        assertEquals(translator.getErrors().size(), 1);
    }

    @Test
    public void AgeNestedTests() throws Exception {
        this.runTest("Nested Ages 1", "criteria/translated/age.nested.1.omop.json");
        this.runTest("Nested Ages 2", "criteria/translated/age.nested.2.omop.json");
        this.runTest("Nested Ages 3", "criteria/translated/age.nested.3.omop.json");
        this.runTest("Diabetes and Specific Ages", "criteria/translated/age.nested.with.diabetes.omop.json");
    }
}
