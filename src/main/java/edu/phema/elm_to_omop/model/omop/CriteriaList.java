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

    public String getJsonFragment(int format) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("\"CriteriaList\": [");
        if (entries != null && entries.size() > 0) {
            int numEntries = entries.size();
            for (int index = 0; index < numEntries; index++) {
                CriteriaListEntry entry = entries.get(index);
                builder.append(entry.getJsonFragment(format));
                if (index < (numEntries - 1)) {
                    builder.append(", ");
                }
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
