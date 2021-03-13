package edu.phema.elm_to_omop.io;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import edu.phema.elm_to_omop.phenotype.BundlePhenotype;
import edu.phema.elm_to_omop.phenotype.PhenotypeException;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryException;
import edu.phema.elm_to_omop.vocabulary.FhirBundleConceptSetService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.ValuesetServiceException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaValueSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hl7.fhir.r4.model.*;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class FhirReader {
  // Initial file-based support will focus just on JSON files
  public static final String BUNDLE_EXTENSION = ".json";
  // Section title within Composition of Bundle
  public static final String PHENOTYPE_ENTRY_POINT = "Phenotype Entry Point";

  private IOmopRepositoryService repository;
  private static Map<String, String> codeSystemUriToOmopName = new HashMap<String, String>() {{
    // TODO Add more mappings as needed
    put("http://www.ama-assn.org/go/cpt", "CPT4");
    put("http://hl7.org/fhir/sid/icd-9-cm", "ICD9CM");
    put("http://hl7.org/fhir/sid/icd-9-proc", "ICD9Proc");
    put("http://loinc.org", "LOINC");
    put("http://www.nlm.nih.gov/research/umls/rxnorm", "RxNorm");
    put("http://snomed.info/sct", "SNOMED");
    put("http://hl7.org/fhir/sid/icd-10", "ICD10");
    put("http://hl7.org/fhir/sid/icd-10-cm", "ICD10CM");
    put("http://www.icd10data.com/icd10pcs", "ICD10PCS");
    put("http://hl7.org/fhir/sid/icd-10-pcs", "ICD10PCS");
    put("http://hl7.org/fhir/sid/ndc", "NDC");
    put("http://hl7.org/fhir/ndfrt", "NDFRT");
    put("http://unitsofmeasure.org", "UCUM");
    put("http://www.whocc.no/atc", "ATC");
    put("http://terminology.hl7.org/CodeSystem/HCPCS", "HCPCS");
  }};

  public FhirReader(IOmopRepositoryService repository) {
    this.repository = repository;
  }

  /**
   * Given a FHIR Bundle stored as a JSON file, read all components (libraries, valuesets, etc.)
   * and prepare it as a consolidated BundlePhenotype
   * @param bundleFilePath The path to the Bundle JSON file
   * @param omopService OMOP service to resolve concepts
   * @return The consoldiated BundlePhenotype
   * @throws PhenotypeException
   */
  public static BundlePhenotype readBundleFromFile(String bundleFilePath, IOmopRepositoryService omopService) throws PhenotypeException {
    File file = new File(bundleFilePath);
    FhirContext ctx = FhirContext.forR4();
    IParser parser = ctx.newJsonParser();
    try {
      Bundle bundle = parser.parseResource(Bundle.class, new FileReader(file));
      BundlePhenotype phenotype = FhirReader.convertBundle(bundle, omopService);
      return phenotype;
    } catch (Exception e) {
      throw new PhenotypeException("Error reading phenotype bundle", e);
    }
  }

  /**
   * Read and load a FHIR Bundle stored as a JSON string
   * @param bundleJson The Bundle JSON represented as a string
   * @return The Bundle resource
   * @throws PhenotypeException
   */
  public static Bundle readBundleFromString(String bundleJson) throws PhenotypeException {
    FhirContext ctx = FhirContext.forR4();
    IParser parser = ctx.newJsonParser();
    try {
      return parser.parseResource(Bundle.class, bundleJson);
    } catch (Exception e) {
      throw new PhenotypeException("Error loading FHIR bundle", e);
    }
  }


  /**
   * Given a FHIR Bundle stored as a JSON string, read all components (libraries, valuesets, etc.)
   * and prepare it as a consolidated BundlePhenotype
   * @param bundleJson The Bundle JSON represented as a string
   * @param omopService OMOP service to resolve concepts
   * @return The consolidated BundlePhenotype
   * @throws PhenotypeException
   */
  public static BundlePhenotype loadBundlePhenotypeFromString(String bundleJson, IOmopRepositoryService omopService) throws PhenotypeException {
    try {
      Bundle bundle = FhirReader.readBundleFromString(bundleJson);
      BundlePhenotype phenotype = FhirReader.convertBundle(bundle, omopService);
      return phenotype;
    } catch (Exception e) {
      throw new PhenotypeException("Error loading phenotype bundle", e);
    }
  }

  /**
   * Convert a FHIR Bundle into the BundlePhenotype format used for execution
   * @param bundle       The FHIR Bundle resource to convert
   * @param omopService  The OMOP service to use for value set validation
   * @return The consolidated BundlePhenotype
   * @throws PhenotypeException
   */
  public static BundlePhenotype convertBundle(Bundle bundle, IOmopRepositoryService omopService) throws PhenotypeException {
    try {
      List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
      // Load all of the libraries - this includes phenotype and supporting libraries
      List<Library> libraryEntries = entries.stream()
        .filter(x -> x.getResource().getResourceType() == ResourceType.Library)
        .map(x -> (Library)x.getResource())
        .collect(Collectors.toList());

      List<ValueSet> valueSetEntries = entries.stream()
        .filter(x -> x.getResource().getResourceType() == ResourceType.ValueSet)
        .map(x -> (ValueSet)x.getResource())
        .collect(Collectors.toList());

      Optional<Composition> compositionEntry = entries.stream()
        .filter(x -> x.getResource().getResourceType() == ResourceType.Composition)
        .map(x -> (Composition)x.getResource())
        .findFirst();
      if (!compositionEntry.isPresent()) {
        throw new PhenotypeException("Invalid Bundle - expect to have a Composition resource");
      }

      Composition composition = compositionEntry.get();
      List<Composition.SectionComponent> sections = composition.getSection();
      Optional<Composition.SectionComponent> entrySection = sections.stream()
        .filter(x -> x.getTitle().equals(PHENOTYPE_ENTRY_POINT))
        .findFirst();

      IValuesetService service = new FhirBundleConceptSetService(valueSetEntries, omopService);
      BundlePhenotype phenotype = new BundlePhenotype();
      phenotype.addLibraries(libraryEntries);
      phenotype.setValuesetService(service);
      phenotype.setEntryPointLibrary(entrySection.get().getEntryFirstRep().getReference());
      return phenotype;
    } catch (Exception e) {
      throw new PhenotypeException("Error converting phenotype bundle", e);
    }
  }

  /**
   * Convert FHIR ValueSet resources to the PhemaConceptSet representation
   * @param valueSets List of FHIR ValueSet resources
   * @return List of PhemaConceptSets
   * @throws IOException
   * @throws OmopRepositoryException
   * @throws InvalidFormatException
   * @throws ValuesetServiceException
   */
  public List<PhemaConceptSet> getConceptSets(List<ValueSet> valueSets) throws IOException, OmopRepositoryException, InvalidFormatException, ValuesetServiceException {
    List<PhemaConceptSet> conceptSets = new ArrayList<PhemaConceptSet>(valueSets.size());
    for (int index = 0; index < valueSets.size(); index++) {
      ValueSet valueSet = valueSets.get(index);
      conceptSets.add(getConceptSet(valueSet, index));
    }
    return conceptSets;
  }

  /**
   * Convert a ValueSet to a PhemaConceptSet
   * @param valueSet FHIR ValueSet resource to convert
   * @param index The index the valueset appears - must be unique
   * @return PhemaConceptSet containing all codes from the value set
   * @throws OmopRepositoryException
   * @throws ValuesetServiceException
   */
  private PhemaConceptSet getConceptSet(ValueSet valueSet, int index) throws OmopRepositoryException, ValuesetServiceException {
    // TODO - account for more than just "include", but this will work for now
    List<Concept> concepts = new ArrayList<>();
    List<ValueSet.ConceptSetComponent> components = valueSet.getCompose().getInclude();
    for (ValueSet.ConceptSetComponent component : components) {
      String codeSystem = component.getSystem();
      if (!codeSystemUriToOmopName.containsKey(codeSystem)) {
        throw new ValuesetServiceException(String.format("Unable to transform the code system %s", codeSystem));
      }
      codeSystem = codeSystemUriToOmopName.get(codeSystem);
      for (ValueSet.ConceptReferenceComponent concept : component.getConcept()) {
        concepts.addAll(repository.vocabularySearch(concept.getCode(), codeSystem));
      }
    }

    PhemaConceptSet conceptSet = new PhemaConceptSet();
    conceptSet.setOid(valueSet.getIdElement().getIdPart());
    conceptSet.id = index;
    conceptSet.name = valueSet.getName();
    ArrayList<ConceptSetExpression.ConceptSetItem> itemsList = new ArrayList<>();
    for (Concept concept : concepts) {
      ConceptSetExpression.ConceptSetItem item = new ConceptSetExpression.ConceptSetItem();
      item.concept = concept;
      itemsList.add(item);
    }

    ConceptSetExpression.ConceptSetItem[] items = new ConceptSetExpression.ConceptSetItem[itemsList.size()];
    ConceptSetExpression expression = new ConceptSetExpression();
    expression.items = itemsList.toArray(items);

    conceptSet.expression = expression;

    return conceptSet;
  }
}
