package edu.phema.elm_to_omop.model.omop;

public class StartWindow {

    private WindowBoundary start;
    private WindowBoundary end;

    public StartWindow(WindowBoundary start, WindowBoundary end) {
        super();
        this.start = start;
        this.end = end;
    }

    public WindowBoundary getStart() {
        return start;
    }

    public WindowBoundary getEnd() {
        return end;
    }

    public void setStart(WindowBoundary start) {
        this.start = start;
    }

    public void setEnd(WindowBoundary end) {
        this.end = end;
    }

    public String getJsonFragment() {
      StringBuilder builder = new StringBuilder();
      builder.append("{ \"Start\": {  \"Coeff\": ");
      builder.append(start.getCoeff());
      if (start.getDays() != null) {
        builder.append(", \"Days\": ");
        builder.append(start.getDays());
      }

      builder.append(" }, \"End\": { \"Coeff\": ");
      builder.append(end.getCoeff());
      if (end.getDays() != null) {
        builder.append(", \"Days\": ");
        builder.append(end.getDays());
      }

      builder.append(" } }");
      return builder.toString();
    }
}
