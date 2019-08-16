package edu.phema.elm_to_omop.model.omop;

import java.util.ArrayList;
import java.util.List;

public class CriteriaList {
    private List<CriteriaListEntry> entries = new ArrayList<CriteriaListEntry>();

    public CriteriaList() {
        super();
    }

    public List<CriteriaListEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CriteriaListEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(CriteriaListEntry entry) { this.entries.add(entry); }
}
