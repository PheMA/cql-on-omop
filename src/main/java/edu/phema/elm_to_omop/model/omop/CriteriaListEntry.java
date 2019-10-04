package edu.phema.elm_to_omop.model.omop;

import java.math.BigDecimal;

public class CriteriaListEntry {
    public static final int PrimaryCriteriaFormat = 1;
    public static final int InclusionCriteriaFormat = 2;


    private Criteria criteria;
    private StartWindow startWindow;
    private Occurrence occurrence;
    private VisitOccurrence visitOcc;

    private static StartWindow defaultStartWindow = new StartWindow(
      new WindowBoundary("-1", null),   // Start
      new WindowBoundary("1", null));   // End

    public CriteriaListEntry() {
    }

    public CriteriaListEntry(VisitOccurrence visitOcc) {
        this.visitOcc = visitOcc;
    }

    public VisitOccurrence getVisitOcc() {
        return visitOcc;
    }

    public void setVisitOcc(VisitOccurrence visitOcc) {
        this.visitOcc = visitOcc;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public StartWindow getStartWindow() {
        return startWindow;
    }

    public void setStartWindow(StartWindow startWindow) {
        this.startWindow = startWindow;
    }

    public Occurrence getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Occurrence occurrence) {
        this.occurrence = occurrence;
    }

    public String getJsonFragment(int format) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        if (format == InclusionCriteriaFormat) {
            builder.append("\"Criteria\": ");
            builder.append(criteria.getJsonFragment());
            builder.append(", \"StartWindow\": ");
            builder.append((startWindow == null) ? this.defaultStartWindow.getJsonFragment() : startWindow.getJsonFragment());
            builder.append(", \"Occurrence\": ");
            builder.append(occurrence.getJsonFragment());
        }
        else if (format == PrimaryCriteriaFormat) {
            builder.append("\"VisitOccurrence\": ");
            builder.append(visitOcc.getJsonFragment());
        }
        else {
            throw new Exception("Invalid CriteriaListEntry format");
        }
        builder.append("} ");
        return builder.toString();
    }
}
