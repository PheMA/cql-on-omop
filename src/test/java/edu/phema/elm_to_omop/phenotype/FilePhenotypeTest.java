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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

public class FilePhenotypeTest {
    private List<PhemaConceptSet> conceptSets;

    @Mock
    private IOmopRepositoryService omopRepository;

    private IValuesetService valuesetService;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        // mock for method vocabularySearch
        when(omopRepository.vocabularySearch(anyString(), anyString())).thenAnswer(new Answer<ArrayList<Concept>>() {
            @Override
            public ArrayList<Concept> answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                Concept concept = new Concept();
                concept.conceptCode = (String)arguments[0];
                concept.vocabularyId = (String)arguments[1];
                return new ArrayList<Concept>() {{ add(concept); }};
            }
        });

        String vsPath = PhemaTestHelper.getResourcePath("LibraryHelperTests.csv");
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
