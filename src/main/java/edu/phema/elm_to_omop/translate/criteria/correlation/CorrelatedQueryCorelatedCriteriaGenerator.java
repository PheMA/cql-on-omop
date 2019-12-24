package edu.phema.elm_to_omop.translate.criteria.correlation;

import edu.phema.elm_to_omop.helper.CirceConstants;
import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.PhemaElmaToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import org.hl7.elm.r1.Equal;
import org.hl7.elm.r1.Expression;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;

public class CorrelatedQueryCorelatedCriteriaGenerator {
    /**
     * Generate a correlated criteria for a QUICK Encounter/Condition correlation
     *
     * @param correlation The correlation
     * @param context     The ELM translation context
     * @return The corresponding Circe CriteriaGroup
     */
    public static CorelatedCriteria encounterCondition(Correlation correlation, PhemaElmaToOmopTranslatorContext context) throws CorrelationException, PhemaTranslationException {
        QuickResourceAttributePair lhs = correlation.getLhs();
        QuickResourceAttributePair rhs = correlation.getRhs();

        QuickResource encounter, condition;

        if (lhs.getResource().getType().equals(CorrelationConstants.QuickResourceType.ENCOUNTER)) {
            encounter = lhs.getResource();
            condition = rhs.getResource();
        } else {
            condition = lhs.getResource();
            encounter = rhs.getResource();
        }

        Expression correlationExpression = correlation.getCorrelationExpression();
        if (correlationExpression instanceof Equal) {
            ConditionOccurrence conditionOccurrence = new ConditionOccurrence();
            ProcedureOccurrence procedureOccurrence = new ProcedureOccurrence();

            conditionOccurrence.codesetId = context.getCodesetId(condition.getValuesetFilter());
            procedureOccurrence.codesetId = context.getCodesetId(encounter.getValuesetFilter());

            // Make sure the outer retrieve is the parent Circe criteria
            if (lhs.getResource().getType().equals(CorrelationConstants.QuickResourceType.ENCOUNTER)) {
                // It's important to restrict the visit here, since this is what the CQL is explicitly stating
                procedureOccurrence.CorrelatedCriteria = CirceUtil.criteriaGroupFromCriteria(conditionOccurrence, CirceConstants.CriteriaGroupType.ALL, null, true);
                return CirceUtil.corelatedCriteriaFromCriteria(procedureOccurrence, CirceUtil.defaultOccurrence(), false);
            } else {
                conditionOccurrence.CorrelatedCriteria = CirceUtil.criteriaGroupFromCriteria(procedureOccurrence, CirceConstants.CriteriaGroupType.ALL, null, true);
                return CirceUtil.corelatedCriteriaFromCriteria(conditionOccurrence, CirceUtil.defaultOccurrence(), false);
            }
        } else {
            throw new CorrelationException(String.format("Correlation expression %s not supported for Encounter/Condition correlation", correlationExpression.getClass().getSimpleName()));
        }
    }
}
