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

  public void runTestCase(String cqlSourceFile, String valueSetSourceFile, String statementName, String expectedResultFile) throws Exception {
    // Set up the ELM tree
    translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream(cqlSourceFile), modelManager, libraryManager);
    library = translator.toELM();

    // Use the JSON file valueset service
    String valuesetJson = PhemaTestHelper.getFileAsString(valueSetSourceFile);
    valuesetService = new PhemaJsonConceptSetService(valuesetJson);
    conceptSets = valuesetService.getConceptSets();

    // Generate the cohort expression
    ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, statementName);
    CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);

    // Assert against expected
    assertNotNull(ce);
    assertEquals(ce.inclusionRules.size(), 1);
    PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
      PhemaTestHelper.getFileAsString(expectedResultFile),
      PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
  }

  @Test
  public void simpleTest() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "Simplest Case",
      "criteria/translated/correlated-query/correlated-simple.omop.json");
  }

  @Test
  public void simplestAggregate() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "Simple Aggregate",
      "criteria/translated/simple-aggregate.omop.json");
  }

  @Test
  public void simpleWithAggregate() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "With Aggregate",
      "criteria/translated/correlated-query/simple-with-aggregate.omop.json");
  }

  @Test
  public void compositeEncounterCriteriaTest() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "Encounter Criteria",
      "criteria/translated/correlated-query/composite-encounter-criteria.omop.json");
  }

  @Test
  public void adultDiabeticTest() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "Adult Diabetics",
      "criteria/translated/correlated-query/adult-diabetics.omop.json");
  }

  @Test
  public void workbenchDemoCorrelationSimple() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "Workbench Demo Correlation Simple",
      "criteria/translated/correlated-query/workbench-correlation-simple.omop.json");
  }

  @Test
  public void workbenchDemoCorrelationMultiple() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "Workbench Demo Correlation Multiple",
      "criteria/translated/correlated-query/workbench-correlation-multiple.omop.json");
  }

  @Test
  public void workbenchDemoCorrelationMultipleNested() throws Exception {
    runTestCase("criteria/correlated-query.cql",
      "criteria/valuesets/correlated-query-valuesets.omop.json",
      "Workbench Demo Correlation Multiple Nested",
      "criteria/translated/correlated-query/workbench-correlation-multiple-nested.omop.json");
  }
}
