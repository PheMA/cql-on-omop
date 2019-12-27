package edu.phema.elm_to_omop.translate.criteria.correlation;

import java.util.Objects;

/**
 * Class representing a QUICK resource and attribute pair
 */
public class QuickResourceAttributePair {
    public QuickResource resource;
    public CorrelationConstants.QuickResourceAttribute attribute;

    public QuickResourceAttributePair(QuickResource resource, CorrelationConstants.QuickResourceAttribute attribute) {
        this.resource = resource;
        this.attribute = attribute;
    }

    public static QuickResourceAttributePair from(String resource, String attribute) throws CorrelationException {
        return new QuickResourceAttributePair(QuickResource.from(resource, null), CorrelationConstants.QuickResourceAttribute.create(attribute));
    }

    public static QuickResourceAttributePair from(String resource, String attribute, String valuesetFilter) throws CorrelationException {
        return new QuickResourceAttributePair(QuickResource.from(resource, valuesetFilter), CorrelationConstants.QuickResourceAttribute.create(attribute));
    }

    public QuickResource getResource() {
        return resource;
    }

    public CorrelationConstants.QuickResourceAttribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QuickResourceAttributePair)) {
            return false;
        } else {
            return this.resource.equals(((QuickResourceAttributePair) o).getResource()) &&
                this.attribute.equals(((QuickResourceAttributePair) o).getAttribute());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, attribute);
    }
}