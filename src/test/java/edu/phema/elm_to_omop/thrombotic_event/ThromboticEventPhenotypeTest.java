package edu.phema.elm_to_omop.thrombotic_event;

import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.api.ElmToOmopTranslator;
import edu.phema.elm_to_omop.cql.InMemoryLibrarySourceLoader;
import edu.phema.elm_to_omop.io.FhirReader;
import edu.phema.elm_to_omop.phenotype.BundlePhenotype;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslator;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.service.CohortDefinitionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ThromboticEventPhenotypeTest {
  private CqlTranslator translator;
  private Library library;

  private IValuesetService valuesetService;
  private List<PhemaConceptSet> conceptSets;

  private ModelManager modelManager;
  private LibraryManager libraryManager;

  @Mock
  private IOmopRepositoryService omopRepository;

  @BeforeEach
  public void setup() throws Exception {
    modelManager = new ModelManager();

    MockitoAnnotations.initMocks(this);

    // mock for method vocabularySearch
    when(omopRepository.vocabularySearch(anyString(), anyString())).thenAnswer(new Answer<ArrayList<Concept>>() {
      @Override
      public ArrayList<Concept> answer(InvocationOnMock invocation) throws Throwable {

        /*
          "CONCEPT_ID": 44819697,
          "CONCEPT_NAME": "Acute myocardial infarction of other anterior wall, subsequent episode of care",
          "STANDARD_CONCEPT": "N",
          "STANDARD_CONCEPT_CAPTION": "Non-Standard",
          "INVALID_REASON": "V",
          "INVALID_REASON_CAPTION": "Valid",
          "CONCEPT_CODE": "410.12",
          "DOMAIN_ID": "Condition",
          "VOCABULARY_ID": "ICD9CM",
          "CONCEPT_CLASS_ID": "5-dig billing code"
        */

        Object[] arguments = invocation.getArguments();
        Concept concept = new Concept();
        concept.conceptId = 123L;
        concept.conceptName = "Dummy";
        concept.standardConcept = "N";
        concept.invalidReason = "Value";
        concept.conceptCode = (String) arguments[0];
        concept.vocabularyId = (String) arguments[1];
        concept.domainId = "Condition";
        concept.conceptClassId = "5-dig billing code";
        return new ArrayList<Concept>() {{
          add(concept);
        }};
      }
    });
  }

  @Test
  public void thromboticEventPhenotypeTest() throws Exception {
    // Set up dependency resolver
    InMemoryLibrarySourceLoader librarySourceLoader = new InMemoryLibrarySourceLoader();
    librarySourceLoader.addCql(PhemaTestHelper.getFileAsString("thrombotic-event/cql/includes/FHIRHelpers-4.0.0.cql"));
    librarySourceLoader.addCql(PhemaTestHelper.getFileAsString("thrombotic-event/cql/includes/PhEMAHelpers-1.0.0.cql"));
    librarySourceLoader.addCql(PhemaTestHelper.getFileAsString("thrombotic-event/cql/includes/PhEMATemporal-1.0.0.cql"));

    LibraryManager libraryManager = new LibraryManager(modelManager);
    libraryManager.setLibrarySourceLoader(librarySourceLoader);

    // Set up the ELM tree
    String thromboticEventCql = PhemaTestHelper.getFileAsString("thrombotic-event/cql/1516.thrombotic-event.cql");
    translator = CqlTranslator.fromText(thromboticEventCql, modelManager, libraryManager);
    library = translator.toELM();

    // Set up value set service from CSV file
    String vsPath = PhemaTestHelper.getResourcePath("thrombotic-event/valuesets/1516.thrombotic-event.valuesets.csv");
    valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");
    conceptSets = valuesetService.getConceptSets();

    // Generate cohort expression
    ExpressionDef expression = PhemaTestHelper.getExpressionDefByName(library, "Case");
    CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);

    // Assert against expected
    assertNotNull(ce);
    assertEquals(ce.inclusionRules.size(), 1);
    PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
      PhemaTestHelper.getFileAsString("thrombotic-event/translated/1516.thrombotic-event.omop.json"),
      PhemaTestHelper.getJson(ce.inclusionRules.get(0)));
  }

  @Test
  public void BundlePhenotypeSmokeTest() throws Exception {
    String bundleString = PhemaTestHelper.getFileAsString("thrombotic-event/bundle/phema-phenotype.1516.thrombotic-event.bundle.json");
    List<String> expressionNames = Arrays.asList("Case");

    BundlePhenotype bundlePhenotype = FhirReader.loadBundlePhenotypeFromString(bundleString, omopRepository);
    bundlePhenotype.setPhenotypeExpressionNames(expressionNames);

    ElmToOmopTranslator translator = new ElmToOmopTranslator(bundlePhenotype.getValuesetService());

    conceptSets = bundlePhenotype.getValuesetService().getConceptSets();

    List<CohortDefinitionService.CohortDefinitionDTO> cohortDefinitions = translator.translatePhenotype(bundlePhenotype, conceptSets);

    CohortExpression ce = CohortExpression.fromJson(cohortDefinitions.get(0).expression);

    assertNotNull(ce);
    PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
      PhemaTestHelper.getFileAsString("thrombotic-event/translated/1516.thrombotic-event.cohort-definition.omop.json"),
      PhemaTestHelper.getJson(ce));

    // Check if Circe can generate the SQL for the expression
    CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();

    String sql = queryBuilder.buildExpressionQuery(ce, null);

    assertNotNull(sql);
    PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
      PhemaTestHelper.getFileAsString("thrombotic-event/translated/1516.thrombotic-event.cohort-definition.omop.sql"),
      sql);
  }
}