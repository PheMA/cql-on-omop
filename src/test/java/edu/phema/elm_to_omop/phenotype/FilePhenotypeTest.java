package edu.phema.elm_to_omop.phenotype;

import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.api.ElmToOmopTranslator;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.lenient;

public class FilePhenotypeTest {
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

        conceptSets = valuesetService.getConceptSets();
    }

    @Test
    public void FilePhenotypeSmokeTest() throws Exception {
        String path = PhemaTestHelper.getResourcePath("phenotype/phenotype.cql");
        List<String> expressionNames = Arrays.asList("Phenotype Case");

        IPhenotype phenotype = new FilePhenotype(path, expressionNames);

        ElmToOmopTranslator translator = new ElmToOmopTranslator(valuesetService);

        List<CohortDefinitionDTO> cohortDefinitions = translator.translatePhenotype(phenotype, conceptSets);

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getFileAsString("phenotype/phenotype.omop.json"),
            PhemaTestHelper.getJson(cohortDefinitions));
    }
}
