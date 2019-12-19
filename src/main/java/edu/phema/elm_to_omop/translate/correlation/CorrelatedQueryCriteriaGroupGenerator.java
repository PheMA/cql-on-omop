package edu.phema.elm_to_omop.translate.correlation;

import edu.phema.elm_to_omop.helper.CirceConstants;
import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.PhemaElmaToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.PhemaTranslationException;
import org.hl7.elm.r1.Equal;
import org.hl7.elm.r1.Expression;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;

public class CorrelatedQueryCriteriaGroupGenerator {

    /**
     * Generate a correlated criteria for a QUICK Encounter/Condition correlation
     *
     * @param correlation The correlation
     * @param context     The ELM translation context
     * @return The corresponding Circe CriteriaGroup
     */
    public static CriteriaGroup encounterCondition(Correlation correlation, PhemaElmaToOmopTranslatorContext context) throws CorrelationException, PhemaTranslationException {
        QuickResourceAttributePair lhs = correlation.getLhs();
        QuickResourceAttributePair rhs = correlation.getRhs();

        QuickResource encounter, condition;
        CorrelationConstants.QuickResourceAttribute encounterAttribute, conditionAttribute;

        if (lhs.getResource().getType().equals(CorrelationConstants.QuickResourceType.ENCOUNTER)) {
            encounter = lhs.getResource();
            encounterAttribute = lhs.getAttribute();
            condition = rhs.getResource();
            conditionAttribute = rhs.getAttribute();
        } else {
            condition = lhs.getResource();
            conditionAttribute = lhs.getAttribute();
            encounter = rhs.getResource();
            encounterAttribute = rhs.getAttribute();
        }

        Expression correlationExpression = correlation.getCorrelationExpression();
        if (correlationExpression instanceof Equal) {
            ConditionOccurrence conditionOccurrence = new ConditionOccurrence();
            ProcedureOccurrence procedureOccurrence = new ProcedureOccurrence();

            conditionOccurrence.codesetId = context.getCodesetId(condition.getValuesetFilter());
            procedureOccurrence.codesetId = context.getCodesetId(encounter.getValuesetFilter());

            // It's important to restrict the visit here, since this is what the CQL is explicitly stating
            CriteriaGroup procedureGroup = CirceUtil.groupFromCriteria(procedureOccurrence, CirceConstants.CriteriaGroupType.ALL, null, true);

            conditionOccurrence.CorrelatedCriteria = procedureGroup;

            return CirceUtil.groupFromCriteria(conditionOccurrence, CirceConstants.CriteriaGroupType.ALL, null, false);
        } else {
            throw new CorrelationException(String.format("Correlation expression %s not supported for Encounter/Condition correlation", correlationExpression.getClass().getSimpleName()));
        }
    }
}
