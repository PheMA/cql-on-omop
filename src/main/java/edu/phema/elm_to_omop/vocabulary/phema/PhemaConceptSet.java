package edu.phema.elm_to_omop.vocabulary.phema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 * Wrapper around the Circe ConceptSet class that
 * adds a reference to the valuset OID, which is
 * necessary when translating Retrieve expressions.
 * <p>
 * We would potentially use OID as the ConceptSet
 * name, which would obviate the need for this
 * class.
 */
@JsonIgnoreProperties(value = {"circeConceptSet"})
public class PhemaConceptSet extends ConceptSet {

    /**
     * OID of valuset reference din Retrieve
     */
    private String oid;

    /**
     * Default constructor
     */
    public PhemaConceptSet() {
        super();
    }

    /**
     * Full constructor
     *
     * @param id         Concept set ID
     * @param name       Concept set name
     * @param expression Concept set expression
     * @param oid        OID of valueset referenced in Retrieve
     */
    public PhemaConceptSet(int id, String name, ConceptSetExpression expression, String oid) {
        this.id = id;
        this.name = name;
        this.expression = expression;
        this.oid = oid;
    }

    /**
     * Get the ConceptSet OID
     *
     * @return The ConceptSet OID
     */
    public String getOid() {
        return oid;
    }

    /**
     * Set the ConceptSet OID
     *
     * @param oid The new OID
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * Converts this PhemaConceptSet into a Circe
     * ConceptSet, essentially just removing the OID
     *
     * @return The equivalent Circe ConceptSet
     */
    public ConceptSet getCirceConceptSet() {
        ConceptSet conceptSet = new ConceptSet();

        conceptSet.id = this.id;
        conceptSet.name = this.name;
        conceptSet.expression = this.expression;

        return conceptSet;
    }
}
