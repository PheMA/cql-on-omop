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

public class ElmCodeResolvingValuesetServiceTest {
  private static ModelManager modelManager;
  private static LibraryManager libraryManager;
  private static WireMockServer wireMockServer;
  private static IOmopRepositoryService omopRepository;

  private CqlTranslator translator;
  private Library library;

  private IValuesetService valuesetService;

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
  public void testCodesWithoutTranslation() throws Exception {
    translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/elm-codes-no-translation.cql"), modelManager, libraryManager);
    library = translator.toELM();

    valuesetService = new ElmCodeResolvingValuesetService(omopRepository, library);
    conceptSets = valuesetService.getConceptSets();

    PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
      PhemaTestHelper.getJson(conceptSets),
      PhemaTestHelper.getFileAsString("vocabulary/translated/single-icd9-code.phema-concept-sets.json"));
  }

  @Test
  public void testCodesWithTranslation() throws Exception {
    translator = CqlTranslator.fromStream(this.getClass().getClassLoader().getResourceAsStream("criteria/encounter-criteria.cql"), modelManager, libraryManager);
    library = translator.toELM();

    valuesetService = new ElmCodeResolvingValuesetService(omopRepository, library);
    conceptSets = valuesetService.getConceptSets();

    PhemaTestHelper.assertStringsEqualIgnoreWhitespace(
      PhemaTestHelper.getJson(conceptSets),
      PhemaTestHelper.getFileAsString("vocabulary/translated/elm-code-visit-concepts.phema-concept-sets.json"));
  }

  @AfterAll
  public static void cleanup() {
    wireMockServer.stop();
  }

}
