package edu.phema.elm_to_omop.translate.criteria.correlation;

/**
 * Class representing two QuickResourceAttributePairs, used to represent correlations that we support
 */
public class CorrelatedQuickPair {
    private QuickResourceAttributePair outerResourceAttributePair;
    private QuickResourceAttributePair innerResourceAttributePair;

    /**
     * Create from existing QuickResource and QuickResourceAttribute objects
     *
     * @param outerQuickResource          The outer QUICK resource
     * @param outerQuickResourceAttribute The outer QUICK resource attribute
     * @param innerQuickResource          The outer QUICK resource
     * @param innerQuickResourceAttribute The outer QUICK resource attribute
     * @throws CorrelationException
     */
    public CorrelatedQuickPair(QuickResource outerQuickResource, CorrelationConstants.QuickResourceAttribute outerQuickResourceAttribute,
                               QuickResource innerQuickResource, CorrelationConstants.QuickResourceAttribute innerQuickResourceAttribute) throws CorrelationException {

        outerResourceAttributePair = new QuickResourceAttributePair(outerQuickResource, outerQuickResourceAttribute);
        innerResourceAttributePair = new QuickResourceAttributePair(innerQuickResource, innerQuickResourceAttribute);
    }

    /**
     * Create from a QUICK resources type name and QuickResourceAttribute objects
     *
     * @param outerQuickResourceName      The outer QUICK resource type name (e.g. "Encounter")
     * @param outerQuickResourceAttribute The outer QUICK resource attribute
     * @param innerQuickResourceName      The outer QUICK resource type name
     * @param innerQuickResourceAttribute The outer QUICK resource attribute
     * @throws CorrelationException
     */
    public CorrelatedQuickPair(String outerQuickResourceName, CorrelationConstants.QuickResourceAttribute outerQuickResourceAttribute,
                               String innerQuickResourceName, CorrelationConstants.QuickResourceAttribute innerQuickResourceAttribute) throws CorrelationException {

        outerResourceAttributePair = new QuickResourceAttributePair(QuickResource.from(outerQuickResourceName, null), outerQuickResourceAttribute);
        innerResourceAttributePair = new QuickResourceAttributePair(QuickResource.from(innerQuickResourceName, null), innerQuickResourceAttribute);
    }

    public QuickResourceAttributePair getOuterResourceAttributePair() {
        return outerResourceAttributePair;
    }

    public QuickResourceAttributePair getInnerResourceAttributePair() {
        return innerResourceAttributePair;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CorrelatedQuickPair)) {
            return false;
        } else {
            return this.outerResourceAttributePair.equals(((CorrelatedQuickPair) o).getOuterResourceAttributePair()) &&
                this.innerResourceAttributePair.equals(((CorrelatedQuickPair) o).getInnerResourceAttributePair());
        }
    }
}

