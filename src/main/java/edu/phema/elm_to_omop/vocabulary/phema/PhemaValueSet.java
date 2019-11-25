package edu.phema.elm_to_omop.vocabulary.phema;

import java.util.ArrayList;

/**
 * PhemaValueSet object contains a list of one to many PhemaCode objects
 */
public class PhemaValueSet {

    private int id;
    private String oid;
    private String name;
    ArrayList<PhemaCode> codes;

    public PhemaValueSet() {
        super();
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

    public void add(PhemaCode code) {
        codes.add(code);
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
