package edu.phema.elm_to_omop.translate;

import edu.phema.elm_to_omop.helper.CirceConstants;
import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.transform.ElmTransformer;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.InclusionRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recursive descent ELM tree walker that translates supported ELM expressions according to a set of rules. The end
 * result is a Circe CohortExpression that can be serialized and posted to the OHDSI WebAPI to generated a cohort.
 * <p>
 * We support CQL written against the QUICK data model, and translate to the OHDSI data model according to the
 * CDMH mappings given here: http://build.fhir.org/ig/HL7/cdmh/profiles.html#omop-to-fhir-mappings
 * <p>
 * Some of the high level rules are:
 * <p>
 * - Boolean AND and OR expression are translated to CriteriaGroups of type ALL and ANY respectively
 * - Exists expressions are translated to CriteriaGroups of type AT_LEAST with count 1
 * - Retrieve expression are translated to Circe domain Criteria based on the CDMH mappings
 * <p>
 * Some expression subtrees we look for are:
 * <p>
 * - CalculateAge [Comparison Operator] [Literal], which translates to a Circe DemographicCriteria
 * - Count([Expression]) [Comparison Operator] [Literal], which translates
 */
public class PhemaElmToOmopTranslator {
    /**
     * Given an expression definition from a CQL/ELM library, generate an OHDSI
     * cohort expression.
     *
     * @param library       The CQL/ELM library that the expression is from
     * @param expressionDef The expression to translate into a phenotype
     * @param conceptSets   The list of value sets (concept sets) related to the CQL/ELM definition
     * @return An OHDSI cohort expression that can be included in a cohort definition
     * @throws Exception
     */
    public static CohortExpression generateCohortExpression(Library library, ExpressionDef expressionDef, List<PhemaConceptSet> conceptSets) throws Exception {
        PhemaElmToOmopTranslatorContext context = new PhemaElmToOmopTranslatorContext(library, conceptSets);

        List<InclusionRule> inclusionRules = new ArrayList<>();

        ElmTransformer elmTransformer = new ElmTransformer();
        elmTransformer.resolveReferences(library, expressionDef);

        Expression expression = expressionDef.getExpression();

        CriteriaGroup criteriaGroup = CriteriaGroupTranslator.generateCriteriaGroupForExpression(expression, context);

        // Default to ALL if not set
        if (criteriaGroup.type == null) {
            criteriaGroup.type = CirceConstants.CriteriaGroupType.ALL.toString();
        }

        inclusionRules.add(CirceUtil.inclusionRuleFromCriteriaGroup(expressionDef.getName(), expressionDef.getName(), criteriaGroup));

        CohortExpression cohortExpression = new CohortExpression();

        cohortExpression.primaryCriteria = CirceUtil.defaultPrimaryCriteria();

        cohortExpression.title = expressionDef.getName();

        ConceptSet[] circeConceptSets = new ConceptSet[conceptSets.size()];
        cohortExpression.conceptSets = conceptSets.stream().map(PhemaConceptSet::getCirceConceptSet).collect(Collectors.toList()).toArray(circeConceptSets);
        cohortExpression.inclusionRules = inclusionRules;

        return cohortExpression;
    }

    public static boolean isBooleanExpression(Expression expression) {
        return (expression instanceof Or) ||
            (expression instanceof And) ||
            (expression instanceof Not);
    }

    /**
     * Determine the inclusion expression type to use, given an expression.
     *
     * @param expression The expression
     * @return The inclusion group type
     * @throws Exception
     */
    public static CirceConstants.CriteriaGroupType getInclusionExpressionType(Expression expression) throws PhemaNotImplementedException {
        if (expression instanceof Or) {
            return CirceConstants.CriteriaGroupType.ANY;
        } else if (expression instanceof And) {
            return CirceConstants.CriteriaGroupType.ALL;
        }

        throw new PhemaNotImplementedException("Currently the translator only handles And and Or expressions");
    }

    /**
     * The CQL parser will generate Not(Equal) (two AST nodes) instead of a single NotEqual node as a simplification
     * technique. Circe criteria don't have a universal negation operator, so for now we simply collapse Not(Equal) into
     * a NotEqual expression.
     * <p>
     * TODO: Determine which other cases to support
     */
    public static Expression invertNot(Expression expression) throws PhemaNotImplementedException {
        if (expression instanceof Not) {
            Not not = (Not) expression;

            NotEqual notEqual = null;
            if (not.getOperand() instanceof Equal) {
                Equal equal = (Equal) not.getOperand();

                notEqual = new NotEqual();

                notEqual = notEqual.withOperand(equal.getOperand());
            } else {
                throw new PhemaNotImplementedException(String.format("Negation not supported for operand: %s", not.getOperand().getClass().getName()));
            }

            return notEqual;
        } else {
            return expression;
        }
    }
}
