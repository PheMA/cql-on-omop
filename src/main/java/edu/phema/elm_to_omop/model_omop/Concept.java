package edu.phema.elm_to_omop.model_omop;

public class Concept {

    private String conceptClassId;
    private String conceptCode;
    private String id;
    private String name;
    private String domainId;
    private String invalidReason;
    private String invalidReasonCaption;
    private String standardConcept;
    private String standardConceptCaption;
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
