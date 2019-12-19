package edu.phema.elm_to_omop.translate.correlation;

public class CorrelatedQuickPair {
    private QuickResourceAttributePair outerResourceAttributePair;
    private QuickResourceAttributePair innerResourceAttributePair;

    public CorrelatedQuickPair(QuickResource outerQuickResource, CorrelationConstants.QuickResourceAttribute outerQuickResourceAttribute,
                               QuickResource innerQuickResource, CorrelationConstants.QuickResourceAttribute innerQuickResourceAttribute) throws CorrelationException {

        outerResourceAttributePair = new QuickResourceAttributePair(outerQuickResource, outerQuickResourceAttribute);
        innerResourceAttributePair = new QuickResourceAttributePair(innerQuickResource, innerQuickResourceAttribute);
    }

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

