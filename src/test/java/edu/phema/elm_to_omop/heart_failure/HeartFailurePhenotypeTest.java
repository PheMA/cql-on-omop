package edu.phema.elm_to_omop.heart_failure;

import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslator;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

public class HeartFailurePhenotypeTest {
    private CqlTranslator translator;
    private Library library;
    private List<PhemaConceptSet> conceptSets;

    //    @Mock
    private IOmopRepositoryService omopRepository;

    private IValuesetService valuesetService;

    @BeforeEach
    public void setup() throws Exception {

        HeartFailurePhenotypeTestHelper.createMockOmopServer(47474);

/*
        OMOP_BASE_URL=http://52.162.236.199/WebAPI/
        SOURCE=OHDSI-CDMV5
            */

        omopRepository = new OmopRepositoryService("http://52.162.236.199/WebAPI/", "OHDSI-CDMV5");


        String vsPath = "/heart-failure/valuesets/heart-failure-diagnosis-icd-codes.valueset.csv";
        valuesetService = new SpreadsheetValuesetService(omopRepository, vsPath, "simple");

        ModelManager modelManager = new ModelManager();
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("heart-failure/cql/step-2-adults-with-hf-echo.phenotype.cql"), modelManager, new LibraryManager(modelManager));
        library = translator.toELM();

        conceptSets = valuesetService.getConceptSets();
    }

    @Test
    public void StepTwoTest() throws Exception {
        ExpressionDef expression = PhemaElmToOmopTranslator.getExpressionDefByName(library, "Case");

        CohortExpression ce = PhemaElmToOmopTranslator.generateCohortExpression(library, expression, conceptSets);
        org.ohdsi.circe.cohortdefinition.InclusionRule rule = ce.inclusionRules.get(0);

        assertNotNull(ce);
        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("translated/ExistsDirectCondition.json"),
            PhemaTestHelper.getJson(rule));
    }
}
