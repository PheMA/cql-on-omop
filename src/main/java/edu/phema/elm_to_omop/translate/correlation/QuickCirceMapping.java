package edu.phema.elm_to_omop.translate.correlation;

public class QuickCirceMapping {
    private CorrelationConstants.QuickResourceType quickResource;
    private CorrelationConstants.QuickResourceAttribute quickResourceAttribute;

    private CorrelationConstants.CirceCriteria circeCriteria;
    private CorrelationConstants.CirceCriteriaAttribute circeCriteriaAttribute;

    public QuickCirceMapping(String quickResourceName, CorrelationConstants.QuickResourceAttribute quickResourceAttribute,
                             CorrelationConstants.CirceCriteria circeCriteria, CorrelationConstants.CirceCriteriaAttribute circeCriteriaAttribute) throws CorrelationException {
        this.quickResource = CorrelationConstants.QuickResourceType.create(quickResourceName);
        this.quickResourceAttribute = quickResourceAttribute;

        this.circeCriteria = circeCriteria;
        this.circeCriteriaAttribute = circeCriteriaAttribute;
    }

    public QuickCirceMapping(CorrelationConstants.QuickResourceType quickResourceName, CorrelationConstants.QuickResourceAttribute quickResourceAttribute,
                             CorrelationConstants.CirceCriteria circeCriteria, CorrelationConstants.CirceCriteriaAttribute circeCriteriaAttribute) throws CorrelationException {
        this.quickResource = quickResourceName;
        this.quickResourceAttribute = quickResourceAttribute;

        this.circeCriteria = circeCriteria;
        this.circeCriteriaAttribute = circeCriteriaAttribute;
    }

    public CorrelationConstants.QuickResourceType getQuickResource() {
        return quickResource;
    }

    public CorrelationConstants.QuickResourceAttribute getQuickResourceAttribute() {
        return quickResourceAttribute;
    }

    public CorrelationConstants.CirceCriteria getCirceCriteria() {
        return circeCriteria;
    }

    public CorrelationConstants.CirceCriteriaAttribute getCirceCriteriaAttribute() {
        return circeCriteriaAttribute;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QuickCirceMapping)) {
            return false;
        } else {
            return this.quickResource.equals(((QuickCirceMapping) o).quickResource) &&
                this.quickResourceAttribute.equals(((QuickCirceMapping) o).quickResourceAttribute) &&
                this.circeCriteria.equals(((QuickCirceMapping) o).getCirceCriteria()) &&
                this.circeCriteriaAttribute.equals(((QuickCirceMapping) o).getCirceCriteriaAttribute());
        }
    }
}
