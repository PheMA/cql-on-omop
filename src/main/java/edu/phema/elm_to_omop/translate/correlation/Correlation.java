package edu.phema.elm_to_omop.translate.correlation;

import org.hl7.elm.r1.Expression;

import java.util.Objects;

public class Correlation {
    public QuickResourceAttributePair lhs;
    public QuickResourceAttributePair rhs;
    public Expression correlationExpression;

    public Correlation(QuickResourceAttributePair lhs, QuickResourceAttributePair rhs, Expression correlationExpression) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.correlationExpression = correlationExpression;
    }

    public static Correlation from(String outerResource, String outerAttribute, String innerResource, String innerAttribute) throws CorrelationException {
        QuickResourceAttributePair lhs = QuickResourceAttributePair.from(outerResource, outerAttribute);
        QuickResourceAttributePair rhs = QuickResourceAttributePair.from(innerResource, innerAttribute);

        return new Correlation(lhs, rhs, null);
    }

    public QuickResourceAttributePair getLhs() {
        return lhs;
    }

    public QuickResourceAttributePair getRhs() {
        return rhs;
    }

    public Expression getCorrelationExpression() {
        return correlationExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Correlation)) {
            return false;
        } else {
            // ignore expression
            return ((this.lhs == null && ((Correlation) o).getLhs() == null) || this.lhs.equals(((Correlation) o).getLhs())
                && (this.rhs == null && ((Correlation) o).getRhs() == null) || this.rhs.equals(((Correlation) o).getRhs()));
        }
    }

    @Override
    public int hashCode() {
        // ignore expression
        return Objects.hash(lhs.resource, lhs.attribute, rhs.resource, rhs.attribute);
    }

    @Override
    public String toString() {
        return String.format("𝜌(%s.%s, %s.%s)", lhs.getResource().toString(), lhs.getAttribute().toString(), rhs.getResource().toString(), rhs.getAttribute().toString());
    }
}