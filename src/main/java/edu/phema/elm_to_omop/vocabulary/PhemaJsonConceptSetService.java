package edu.phema.elm_to_omop.vocabulary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;

import java.util.List;

public class PhemaJsonConceptSetService implements IValuesetService {
    private List<PhemaConceptSet> conceptSets;

    /**
     * Constructor that uses a prebuilt list of concept sets
     *
     * @param conceptSets The concept sets the valueset service should use
     */
    public PhemaJsonConceptSetService(List<PhemaConceptSet> conceptSets) {
        this.conceptSets = conceptSets;
    }

    /**
     * Constructor that attempts to deserialize a list of concept sets from a JSON string
     *
     * @param conceptSetsJson The serialized list of concept sets
     * @throws ValuesetServiceException
     */
    public PhemaJsonConceptSetService(String conceptSetsJson) throws ValuesetServiceException {
        try {
            this.conceptSets = new ObjectMapper()
                .readValue(conceptSetsJson, new TypeReference<List<PhemaConceptSet>>() {
                });
        } catch (Exception e) {
            throw new ValuesetServiceException("Error deserializing list of concept sets from JSON string", e);
        }
    }

    @Override
    public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
        return conceptSets;
    }

    @Override
    public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
        PhemaConceptSetList phemaConceptSetList = new PhemaConceptSetList();

        phemaConceptSetList.setConceptSets(conceptSets);

        return phemaConceptSetList;
    }
}
