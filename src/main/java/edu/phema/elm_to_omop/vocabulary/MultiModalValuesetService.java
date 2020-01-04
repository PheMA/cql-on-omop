package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;

import java.util.ArrayList;
import java.util.List;

public class MultiModalValuesetService implements IValuesetService {
    private List<IValuesetService> valuesetServices;

    private int codesetId;

    public MultiModalValuesetService(IValuesetService... valuesetServices) {
        this.valuesetServices = new ArrayList<>();

        for (IValuesetService valuesetService : valuesetServices) {
            this.valuesetServices.add(valuesetService);
        }

        codesetId = 0;
    }

    private PhemaConceptSetList mergeConceptSetLists(List<PhemaConceptSetList> phemaConceptSetLists) {
        PhemaConceptSetList phemaConceptSetListResult = new PhemaConceptSetList();

        for (PhemaConceptSetList phemaConceptSetList : phemaConceptSetLists) {
            List<PhemaConceptSet> conceptSets = phemaConceptSetList.getConceptSets();

            // Update IDs so we don't get duplicates
            for (PhemaConceptSet phemaConceptSet : conceptSets) {
                phemaConceptSet.id = codesetId++;
            }
            phemaConceptSetListResult.addAllConceptSets(conceptSets);

            phemaConceptSetListResult.addAllNotFoundCodes(phemaConceptSetList.getNotFoundCodes());
        }

        // Reset ID to produce consistent results
        codesetId = 0;

        return phemaConceptSetListResult;
    }

    @Override
    public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
        List<PhemaConceptSetList> phemaConceptSetLists = new ArrayList<>();

        for (IValuesetService valuesetService : valuesetServices) {
            phemaConceptSetLists.add(valuesetService.getConceptSetList());
        }

        return mergeConceptSetLists(phemaConceptSetLists);
    }

    @Override
    public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
        return getConceptSetList().getConceptSets();
    }
}
