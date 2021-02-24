package edu.phema.elm_to_omop.translate.criteria;

import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.translate.util.map.FhirResourceCirceCriteriaMap;
import edu.phema.elm_to_omop.translate.util.map.QuickResourceCirceCriteriaMap;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.Library.*;
import org.hl7.elm.r1.Retrieve;
import org.ohdsi.circe.cohortdefinition.*;

public class CriteriaTranslator {
    private CriteriaTranslator()  {
        super();
    }

    private static Class<? extends Criteria> getCriteriaClass(String resourceType, Usings usings) throws PhemaTranslationException {
        if (usings.getDef().stream().anyMatch(x -> x.getLocalIdentifier().equals("FHIR"))) {
            return FhirResourceCirceCriteriaMap.resourceCriteriaMap.get(resourceType);
        } else if (usings.getDef().stream().anyMatch(x -> x.getLocalIdentifier().equals("QUICK"))) {
            return QuickResourceCirceCriteriaMap.resourceCriteriaMap.get(resourceType);
        }

        throw new PhemaTranslationException(String.format("Only FHIR and QUICK data models are currently supported"));
    }

    private static Criteria generateCriteriaForRetrieve(Retrieve retrieve, PhemaElmToOmopTranslatorContext context) throws PhemaTranslationException {
        String resourceType = retrieve.getDataType().getLocalPart();

        Class<? extends Criteria> criteriaClass = getCriteriaClass(resourceType, context.getLibrary().getUsings());

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
            } else if (criteria instanceof VisitOccurrence) {
                ((VisitOccurrence) criteria).codesetId = codesetId;
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
