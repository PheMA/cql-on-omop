package edu.phema.elm_to_omop.model.omop;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "items" })
public class Expression {
    private ArrayList<Items> items;

    public Expression() {
        super();
        items = new ArrayList<Items>();
    }

    public Expression(ArrayList<Items> items) {
        super();
        this.items = items;
    }

    public ArrayList<Items> getItems() {
        return items;
    }

    public void addItem(Items item) {
        this.items.add(item);
    }

}
