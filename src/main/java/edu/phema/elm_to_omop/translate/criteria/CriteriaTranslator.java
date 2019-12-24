package edu.phema.elm_to_omop.translate.criteria;

import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.translate.util.map.QuickResourceCirceCriteriaMap;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.Retrieve;
import org.ohdsi.circe.cohortdefinition.*;

public class CriteriaTranslator {
    private static Criteria generateCriteriaForRetrieve(Retrieve retrieve, PhemaElmToOmopTranslatorContext context) throws PhemaTranslationException {
        String resourceType = retrieve.getDataType().getLocalPart();

        Class<? extends Criteria> criteriaClass = QuickResourceCirceCriteriaMap.resourceCriteriaMap.get(resourceType);

        if (criteriaClass == null) {
            throw new PhemaTranslationException(String.format("Cannot create Circe criteria for QUICK resource type: %s", resourceType));
        }

        try {
            Criteria criteria = criteriaClass.getConstructor().newInstance();

            int codesetId = context.getCodesetIdForRetrieve(retrieve);

            if (criteria instanceof ConditionOccurrence) {
                ((ConditionOccurrence) criteria).codesetId = codesetId;
            } else if (criteria instanceof ProcedureOccurrence) {
                ((ProcedureOccurrence) criteria).codesetId = codesetId;
            } else if (criteria instanceof DrugExposure) {
                ((DrugExposure) criteria).codesetId = codesetId;
            } else if (criteria instanceof Measurement) {
                ((Measurement) criteria).codesetId = codesetId;
            } else {
                throw new PhemaNotImplementedException(String.format("%s criteria are not yet implemented", criteria.getClass().getSimpleName()));
            }

            return criteria;
        } catch (Exception e) {
            throw new PhemaTranslationException(String.format("Error creating Circe criteria for QUICK resource type: %s", resourceType), e);
        }
    }

    public static Criteria generateCriteriaForExpression(Expression expression, PhemaElmToOmopTranslatorContext context) throws PhemaNotImplementedException, PhemaTranslationException {
        if (expression instanceof Retrieve) {
            return generateCriteriaForRetrieve((Retrieve) expression, context);
        } else {
            throw new PhemaNotImplementedException(String.format("Criteria generation not supported for expression of type %s", expression.getClass().getSimpleName()));
        }
    }
}
