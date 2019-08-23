package edu.phema.elm_to_omop.model.omop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "CONCEPT_ID", "CONCEPT_NAME", "STANDARD_CONCEPT", "STANDARD_CONCEPT_CAPTION",
    "INVALID_REASON", "INVALID_REASON_CAPTION", "CONCEPT_CODE", "DOMAIN_ID", "VOCABULARY_ID", "CONCEPT_CLASS_ID" })

public class Concept {

    @JsonProperty("CONCEPT_CLASS_ID")
    private String conceptClassId;

    @JsonProperty("CONCEPT_CODE")
    private String conceptCode;

    @JsonProperty("CONCEPT_ID")
    private String id;

    @JsonProperty("CONCEPT_NAME")
    private String name;

    @JsonProperty("DOMAIN_ID")
    private String domainId;

    @JsonProperty("INVALID_REASON")
    private String invalidReason;

    @JsonProperty("INVALID_REASON_CAPTION")
    private String invalidReasonCaption;

    @JsonProperty("STANDARD_CONCEPT")
    private String standardConcept;

    @JsonProperty("STANDARD_CONCEPT_CAPTION")
    private String standardConceptCaption;

    @JsonProperty("VOCABULARY_ID")
    private String vocabularyId;

    public Concept() {
        super();
        // TODO Auto-generated constructor stub
    }

    public String getConceptClassId() {
        return conceptClassId;
    }
    public String getConceptCode() {
        return conceptCode;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDomainId() {
        return domainId;
    }
    public String getInvalidReason() {
        return invalidReason;
    }
    public String getInvalidReasonCaption() {
        return invalidReasonCaption;
    }
    public String getStandardConcept() {
        return standardConcept;
    }
    public String getStandardConceptCaption() {
        return standardConceptCaption;
    }
    public String getVocabularyId() {
        return vocabularyId;
    }
    public void setConceptClassId(String conceptClassId) {
        this.conceptClassId = conceptClassId;
    }
    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }
    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
    public void setInvalidReasonCaption(String invalidReasonCaption) {
        this.invalidReasonCaption = invalidReasonCaption;
    }
    public void setStandardConcept(String standardConcept) {
        this.standardConcept = standardConcept;
    }
    public void setStandardConceptCaption(String standardConceptCaption) {
        this.standardConceptCaption = standardConceptCaption;
    }
    public void setVocabularyId(String vocabularyId) {
        this.vocabularyId = vocabularyId;
    }
}
