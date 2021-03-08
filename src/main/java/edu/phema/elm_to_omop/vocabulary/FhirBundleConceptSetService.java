package edu.phema.elm_to_omop.vocabulary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.io.FhirReader;
import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.List;

public class FhirBundleConceptSetService implements IValuesetService {
  private List<PhemaConceptSet> conceptSets;
  private IOmopRepositoryService omopService;
  private List<ValueSet> valueSets;

  /**
   * Constructor that attempts to convert a list of FHIR ValueSet resources to PhemaConceptSets
   *
   * @param valueSets The resources containing our value sets
   * @throws ValuesetServiceException
   */
  public FhirBundleConceptSetService(List<ValueSet> valueSets, IOmopRepositoryService omopService) throws ValuesetServiceException {
    this.omopService = omopService;
    this.valueSets = valueSets;
  }

  @Override
  public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
    try {
      FhirReader fhirReader = new FhirReader(this.omopService);
      return fhirReader.getConceptSets(this.valueSets);
    } catch (Exception e) {
      throw new ValuesetServiceException("Error converting FHIR ValueSet to concept set", e);
    }
  }

  @Override
  public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
    PhemaConceptSetList phemaConceptSetList = new PhemaConceptSetList();

    phemaConceptSetList.setConceptSets(conceptSets);

    return phemaConceptSetList;
  }
}
