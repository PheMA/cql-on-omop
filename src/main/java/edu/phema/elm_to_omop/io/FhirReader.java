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
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.ValueSet;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FhirReader {
  // Initial file-based support will focus just on JSON files
  public static final String BUNDLE_EXTENSION = ".json";
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

      IValuesetService service = new FhirBundleConceptSetService(valueSetEntries, omopService);
      BundlePhenotype phenotype = new BundlePhenotype();
      phenotype.addLibraries(libraryEntries);
      phenotype.setValuesetService(service);
      return phenotype;
    } catch (Exception e) {
      throw new PhenotypeException("Error reading phenotype bundle", e);
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
