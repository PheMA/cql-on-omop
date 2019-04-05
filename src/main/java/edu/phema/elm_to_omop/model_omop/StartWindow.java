package edu.phema.elm_to_omop.model_omop;

public class StartWindow {

    private Start start;
    private End end;
    
    public StartWindow(Start start, End end) {
        super();
        this.start = start;
        this.end = end;
    }

    public Start getStart() {
        return start;
    }

    public End getEnd() {
        return end;
    }

    public void setStart(Start start) {
        this.start = start;
    }

    public void setEnd(End end) {
        this.end = end;
    }
    
    
}
