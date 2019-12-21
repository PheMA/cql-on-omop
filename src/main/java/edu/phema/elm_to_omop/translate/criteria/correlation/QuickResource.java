package edu.phema.elm_to_omop.translate.criteria.correlation;

import java.util.Objects;

/**
 * Class representing a Quick resource
 */
public class QuickResource {
    private CorrelationConstants.QuickResourceType type;
    private String valuesetFilter;

    public QuickResource(CorrelationConstants.QuickResourceType type, String valuesetFilter) {
        this.type = type;
        this.valuesetFilter = valuesetFilter;
    }

    public static QuickResource from(String type, String valuesetFilter) throws CorrelationException {
        return new QuickResource(CorrelationConstants.QuickResourceType.create(type), valuesetFilter);
    }

    public CorrelationConstants.QuickResourceType getType() {
        return type;
    }

    public String getValuesetFilter() {
        return valuesetFilter;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(type);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QuickResource)) {
            return false;
        }

        return type.equals(((QuickResource) o).getType());
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
