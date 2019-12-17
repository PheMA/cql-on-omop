package edu.phema.elm_to_omop.vocabulary.phema;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for a list of resolved concept sets along
 * with any codes that were not found
 */
public class PhemaConceptSetList {
    private ArrayList<PhemaConceptSet> conceptSets;
    private ArrayList<PhemaCode> notFoundList;

    public PhemaConceptSetList() {
        conceptSets = new ArrayList<>();
        notFoundList = new ArrayList<>();
    }

    public void addConceptSet(PhemaConceptSet phemaConceptSet) {
        conceptSets.add(phemaConceptSet);
    }

    public void addAllConceptSets(List<PhemaConceptSet> conceptSets) {
        this.conceptSets.addAll(conceptSets);
    }

    public ArrayList<PhemaConceptSet> getConceptSets() {
        return conceptSets;
    }

    public void setConceptSets(List<PhemaConceptSet> conceptSets) {
        this.conceptSets = new ArrayList<>(conceptSets);
    }

    public void addNotFoundCode(PhemaCode phemaCode) {
        notFoundList.add(phemaCode);
    }

    public void addAllNotFoundCodes(List<PhemaCode> notFoundCodes) {
        notFoundList.addAll(notFoundCodes);
    }

    public ArrayList<PhemaCode> getNotFoundCodes() {
        return notFoundList;
    }
}
