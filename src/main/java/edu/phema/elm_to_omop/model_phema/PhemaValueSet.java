package edu.phema.elm_to_omop.model_phema;

import java.util.ArrayList;

public class PhemaValueSet {

    private int id;
    private String name;
    ArrayList<PhemaCode> codes;
    
    
    public PhemaValueSet() {
        super();
        // TODO Auto-generated constructor stub
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<PhemaCode> getCodes() {
        return codes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCodes(ArrayList<PhemaCode> codes) {
        this.codes = codes;
    }
    
    public void add(PhemaCode code)  {
        codes.add(code);
    }
    
    
}
