package edu.phema.elm_to_omop.phenotype;

import edu.phema.elm_to_omop.cql.InMemoryLibrarySourceLoader;
import edu.phema.elm_to_omop.io.ElmReader;
import edu.phema.elm_to_omop.io.FhirReader;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.ValuesetServiceException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.hl7.fhir.r4.model.Attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BundlePhenotype implements IPhenotype {
  Logger logger = Logger.getLogger(getClass().getName());

  static final String CQL_CONTENT_TYPE = "text/cql";
  static final String ELM_CONTENT_TYPE = "application/elm+json";

  /**
   * Internal class to structure storage of both the text-based CQL code definition, as well as the
   * processed ELM library.
   */
  private class CqlDefinition {
    public String id;
    public String code;
    public Library elm;
    public boolean hasDependencies;
  }

  private List<CqlDefinition> libraries = new ArrayList<CqlDefinition>();
  private List<String> phenotypeExpressionNames = new ArrayList<String>();
  private IValuesetService valuesetService = null;
  private CqlDefinition entryPointLibrary = null;

  public InMemoryLibrarySourceLoader getSourceLoader() {
    return sourceLoader;
  }

  public void setSourceLoader(InMemoryLibrarySourceLoader sourceLoader) {
    this.sourceLoader = sourceLoader;
  }

  private InMemoryLibrarySourceLoader sourceLoader = null;

  public List<CqlDefinition> getLibraries() {
    return libraries;
  }

  public void setLibraries(List<CqlDefinition> libraries) {
    this.libraries = libraries;
  }

  public void addLibrary(org.hl7.fhir.r4.model.Library library) {
    if (this.libraries == null) {
      this.libraries = new ArrayList<CqlDefinition>();
    }

    CqlDefinition definition = new CqlDefinition();
    List<Attachment> attachments = library.getContent();
    for (Attachment attach : attachments) {
      if (attach.getContentType().equals(CQL_CONTENT_TYPE)) {
        byte[] data = attach.getData();
        if (data != null) {
          definition.id = library.getId();
          definition.code = new String(data);

          // Add the library to the in-memory source map for resolution during parsing
          this.sourceLoader.addCql(definition.code);

          definition.hasDependencies = library.getRelatedArtifact().stream().anyMatch(
            x -> x.getType().getDisplay().equalsIgnoreCase("Depends On"));
          this.libraries.add(definition);
          return;
        }
      }
    }
  }

  public void generateELM() {
    for (CqlDefinition definition : libraries) {
      definition.elm = ElmReader.readCqlString(definition.code, this.sourceLoader);
    }
  }

  public void addLibraries(List<org.hl7.fhir.r4.model.Library> libraries) {
    if (libraries == null) {
      return;
    }

    for (org.hl7.fhir.r4.model.Library library : libraries) {
      addLibrary(library);
    }
  }

  public void setValuesetService(IValuesetService service) {
    this.valuesetService = service;
  }

  public IValuesetService getValuesetService() {
    return this.valuesetService;
  }

  public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
    if (this.valuesetService == null) {
      return null;
    }
    return this.valuesetService.getConceptSets();
  }

  public List<String> getPhenotypeExpressionNames() {
    return phenotypeExpressionNames;
  }

  public void setPhenotypeExpressionNames(List<String> phenotypeExpressionNames) {
    this.phenotypeExpressionNames = phenotypeExpressionNames;
  }

  public void setEntryPointLibrary(String entryPointId) {
    this.entryPointLibrary = libraries.stream()
      .filter(x -> x.id.equals(entryPointId))
      .findFirst()
      .get();
  }

  public void setEntryPointLibrary(CqlDefinition entryPointLibrary) {
    this.entryPointLibrary = entryPointLibrary;
  }

  public CqlDefinition getEntryPointLibrary() {
    return this.entryPointLibrary;
  }

  public BundlePhenotype() {
    this.sourceLoader = new InMemoryLibrarySourceLoader();
  }

  public BundlePhenotype(String bundlePath, List<String> phenotypeExpressionNames, IOmopRepositoryService omopService) throws PhenotypeException {
    this.phenotypeExpressionNames = phenotypeExpressionNames;
    if (phenotypeExpressionNames == null || phenotypeExpressionNames.isEmpty()) {
      throw new PhenotypeException("No phenotype expression names specified");
    }

    BundlePhenotype loadedPhenotype = FhirReader.readBundleFromFile(bundlePath, omopService);
    this.setLibraries(loadedPhenotype.getLibraries());
    this.setSourceLoader(loadedPhenotype.getSourceLoader());
    this.setValuesetService(loadedPhenotype.getValuesetService());
    this.setEntryPointLibrary(loadedPhenotype.getEntryPointLibrary());
  }

  @Override
  public String getPhenotypeCql() {
    return this.entryPointLibrary.code;
  }

  @Override
  public Library getPhenotypeElm() {
    return this.entryPointLibrary.elm;
  }

  @Override
  public List<ExpressionDef> getPhenotypeExpressions() {
    return getPhenotypeElm().getStatements().getDef().stream()
      .filter(x -> phenotypeExpressionNames.contains(x.getName()))
      .collect(Collectors.toList());
  }
}
