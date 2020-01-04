package edu.phema.elm_to_omop.vocabulary;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.Library;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MultiModalValuesetServiceTest {
    private static ModelManager modelManager;
    private static LibraryManager libraryManager;
    private static WireMockServer wireMockServer;
    private static IOmopRepositoryService omopRepository;

    private CqlTranslator translator;
    private Library library;

    private List<PhemaConceptSet> conceptSets;

    @BeforeAll
    public static void setup() throws Exception {
        wireMockServer = new WireMockServer(options().port(38383).extensions("edu.phema.elm_to_omop.vocabulary.VocabularyTranslationTestTransformer"));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        // Create a stub based using the transformer
        stubFor(post(urlEqualTo("/vocabulary/search"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withTransformers("vocabulary-translation-transformer")));

        omopRepository = new OmopRepositoryService("http://localhost:38383/", "OHDSI-CDMV5");

        modelManager = new ModelManager();
        libraryManager = new LibraryManager(modelManager);
    }

    @Test
    public void testCodeAndJson() throws Exception {
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("vocabulary/cql/code-and-valueset-vocabularies.cql"), modelManager, libraryManager);
        library = translator.toELM();

        IValuesetService elmCodeResolvingValuesetService = new ElmCodeResolvingValuesetService(omopRepository, library);
        IValuesetService phemaJsonConceptSetService = new PhemaJsonConceptSetService(PhemaTestHelper.getFileAsString("vocabulary/heart-failure-diagnosis-icd-codes.phema-concept-sets.json"));

        IValuesetService multiModalValuesetService = new MultiModalValuesetService(elmCodeResolvingValuesetService, phemaJsonConceptSetService);

        conceptSets = multiModalValuesetService.getConceptSets();

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getJson(conceptSets),
            PhemaTestHelper.getFileAsString("vocabulary/translated/code-and-valueset-vocabularies.phema-concept-sets.json"));
    }

    @Test
    public void testCodeAndCsv() throws Exception {
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("vocabulary/cql/code-and-valueset-vocabularies.cql"), modelManager, libraryManager);
        library = translator.toELM();

        IValuesetService elmCodeResolvingValuesetService = new ElmCodeResolvingValuesetService(omopRepository, library);
        IValuesetService conceptCodeCsvFileValuesetService = new ConceptCodeCsvFileValuesetService(omopRepository, PhemaTestHelper.getResourcePath("vocabulary/icd9-only.csv"));

        IValuesetService multiModalValuesetService = new MultiModalValuesetService(elmCodeResolvingValuesetService, conceptCodeCsvFileValuesetService);

        conceptSets = multiModalValuesetService.getConceptSets();

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getJson(conceptSets),
            PhemaTestHelper.getFileAsString("vocabulary/translated/code-and-valueset-csv-vocabularies.phema-concept-sets.json"));
    }

    @Test
    public void testConsistentResults() throws Exception {
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("vocabulary/cql/code-and-valueset-vocabularies.cql"), modelManager, libraryManager);
        library = translator.toELM();

        IValuesetService elmCodeResolvingValuesetService = new ElmCodeResolvingValuesetService(omopRepository, library);
        IValuesetService phemaJsonConceptSetService = new PhemaJsonConceptSetService(PhemaTestHelper.getFileAsString("vocabulary/heart-failure-diagnosis-icd-codes.phema-concept-sets.json"));

        IValuesetService multiModalValuesetService = new MultiModalValuesetService(elmCodeResolvingValuesetService, phemaJsonConceptSetService);

        conceptSets = multiModalValuesetService.getConceptSets();

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getJson(conceptSets),
            PhemaTestHelper.getFileAsString("vocabulary/translated/code-and-valueset-vocabularies.phema-concept-sets.json"));

        conceptSets = multiModalValuesetService.getConceptSets();

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getJson(conceptSets),
            PhemaTestHelper.getFileAsString("vocabulary/translated/code-and-valueset-vocabularies.phema-concept-sets.json"));
    }

    @Test
    public void testCaching() throws Exception {
        translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("vocabulary/cql/code-and-valueset-vocabularies.cql"), modelManager, libraryManager);
        library = translator.toELM();

        IValuesetService elmCodeResolvingValuesetService = new ElmCodeResolvingValuesetService(omopRepository, library);
        IValuesetService conceptCodeCsvFileValuesetService = new ConceptCodeCsvFileValuesetService(omopRepository, PhemaTestHelper.getResourcePath("vocabulary/icd9-only.csv"));

        IValuesetService multiModalValuesetService = new MultiModalValuesetService(elmCodeResolvingValuesetService, conceptCodeCsvFileValuesetService);

        conceptSets = multiModalValuesetService.getConceptSets();

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getJson(conceptSets),
            PhemaTestHelper.getFileAsString("vocabulary/translated/code-and-valueset-csv-vocabularies.phema-concept-sets.json"));

        verify(exactly(8), postRequestedFor(urlEqualTo("/vocabulary/search")));

        conceptSets = multiModalValuesetService.getConceptSets();

        PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
            PhemaTestHelper.getJson(conceptSets),
            PhemaTestHelper.getFileAsString("vocabulary/translated/code-and-valueset-csv-vocabularies.phema-concept-sets.json"));

        verify(exactly(8), postRequestedFor(urlEqualTo("/vocabulary/search")));
    }

    @AfterAll
    public static void cleanup() {
        wireMockServer.stop();
    }
}