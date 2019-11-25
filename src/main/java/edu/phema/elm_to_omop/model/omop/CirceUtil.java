package edu.phema.elm_to_omop.model.omop;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.circe.cohortdefinition.Occurrence;
import org.ohdsi.circe.cohortdefinition.PrimaryCriteria;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;
import org.ohdsi.circe.cohortdefinition.*;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression.ConceptSetItem;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.service.CohortDefinitionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Various utility methods, some of which are necessary because
 * the Circe classes don't have constructors, and it's possible to
 * generate invalid objects using the empty default constructors.
 */
public class CirceUtil {

    /**
     * The translator focuses on creating InclusionRules (CriteraGroups really),
     * and we let the initial population consist of anyone with any visit
     * occurrence
     *
     * @return A PrimaryCriteria representing any visit occurrence
     */
    public static PrimaryCriteria getDefaultPrimaryCriteria() {
        PrimaryCriteria primaryCriteria = new PrimaryCriteria();

        primaryCriteria.observationWindow = new ObservationFilter();
        primaryCriteria.criteriaList = new Criteria[]{
            new VisitOccurrence()
        };

        return primaryCriteria;
    }

    /**
     * Extends a primitive array of CriteriaGroups by adding a new CriteriaGroup.
     * This is necessary because Circe uses primitive arrays instead of Java collections.
     *
     * @param criteriaGroupArray The primitive CriteriaGroup array to extend
     * @param group              The new CriteriaGroup to add
     * @return The extended CriteriaGroup array
     */
    public static CriteriaGroup[] addCriteriaGroup(CriteriaGroup[] criteriaGroupArray, CriteriaGroup group) {
        CriteriaGroup[] newGroupArray = new CriteriaGroup[criteriaGroupArray.length + 1];
        ArrayList<CriteriaGroup> groupArrayList = new ArrayList<>(Arrays.asList(criteriaGroupArray));
        groupArrayList.add(group);
        return groupArrayList.toArray(newGroupArray);
    }

    /**
     * Extends a primitive array of CorelatedCriterias by adding a new CorelatedCriteria.
     * This is necessary because Circe uses primitive arrays instead of Java collections.
     *
     * @param corelatedCriteriaGroupArray The primitive CorelatedCriteria array to extend
     * @param corelatedCriteria           The new CorelatedCriteria to add
     * @return The extended CorelatedCriteria array
     */
    public static CorelatedCriteria[] addCorelatedCriteria(CorelatedCriteria[] corelatedCriteriaGroupArray, CorelatedCriteria corelatedCriteria) {
        CorelatedCriteria[] newGroupArray = new CorelatedCriteria[corelatedCriteriaGroupArray.length + 1];
        ArrayList<CorelatedCriteria> groupArrayList = new ArrayList<>(Arrays.asList(corelatedCriteriaGroupArray));
        groupArrayList.add(corelatedCriteria);
        return groupArrayList.toArray(newGroupArray);
    }

    /**
     * InclusionRules are just a thin wrapper around CriteriaGroups, which contain the
     * expression logic. This method performs the wrapping.
     *
     * @param name          The name of the InclusionRule
     * @param description   The description of the InclusionRule
     * @param criteriaGroup The CriteriaGroup (expression) to wrap
     * @return The created InclusionRule
     */
    public static InclusionRule inclusionRuleFromCriteriaGroup(String name, String description, CriteriaGroup criteriaGroup) {
        InclusionRule inclusionRule = new InclusionRule();

        inclusionRule.name = name;
        inclusionRule.description = description;
        inclusionRule.expression = criteriaGroup;

        return inclusionRule;
    }

    /**
     * Converts an instance of our ConceptSet class to a Circe ConceptSet. We still need
     * our ConceptSet class because it contains a reference to a valuset oid, which we
     * need while translating the ELM library.
     *
     * @param conceptSet The PhEMA ConceptSet instance
     * @return The Circe ConceptSet
     */
    public static org.ohdsi.circe.cohortdefinition.ConceptSet convertConceptSetToCirce(ConceptSet conceptSet) {
        if (conceptSet == null) return null;

        org.ohdsi.circe.cohortdefinition.ConceptSet circeConceptSet = new org.ohdsi.circe.cohortdefinition.ConceptSet();

        circeConceptSet.id = conceptSet.getId();
        circeConceptSet.name = conceptSet.getName();

        List<ConceptSetItem> concepts = new ArrayList<>();
        ConceptSetItem conceptSetItem;
        for (Items concept : conceptSet.getExpression().getItems()) {
            conceptSetItem = new ConceptSetExpression.ConceptSetItem();
            conceptSetItem.concept = concept.getConcept();
            concepts.add(conceptSetItem);
        }

        ConceptSetExpression conceptSetExpression = new ConceptSetExpression();

        ConceptSetItem[] items = new ConceptSetItem[concepts.size()];
        conceptSetExpression.items = concepts.toArray(items);

        circeConceptSet.expression = conceptSetExpression;

        return circeConceptSet;
    }

    /**
     * Creates an empty CorelatedCriteria using the default values first specified at:
     * https://github.com/PheMA/elm-to-ohdsi-executer/blob/ad9fe6c280817a486ab0449244a39cf72347354e/src/main/java/edu/phema/elm_to_omop/model/omop/CriteriaListEntry.java#L15-L17
     *
     * @return A new CorelatedCriteria with default start window
     */
    public static CorelatedCriteria defaultCorelatedCriteria() {
        CorelatedCriteria corelatedCriteria = new CorelatedCriteria();

        Window startWindow = new Window();
        startWindow.start = startWindow.new Endpoint();
        startWindow.start.coeff = -1;

        startWindow.end = startWindow.new Endpoint();
        startWindow.end.coeff = 1;

        corelatedCriteria.startWindow = startWindow;

        return corelatedCriteria;
    }

    /**
     * Generate an empty Occurrence with the default value of "at least 1".
     *
     * @return The default occurrence
     */
    public static Occurrence defaultOccurrence() {
        Occurrence occurrence = new Occurrence();

        occurrence.type = Occurrence.AT_LEAST;
        occurrence.count = 1;

        return occurrence;
    }

    /**
     * Creates a cohort definition and sets the type to SIMPLE_EXPRESSION.
     *
     * @return The default cohort definition
     */
    public static CohortDefinitionService.CohortDefinitionDTO getDefaultCohortDefinition() {
        CohortDefinitionService.CohortDefinitionDTO cohortDefinition = new CohortDefinitionService.CohortDefinitionDTO();

        cohortDefinition.expressionType = ExpressionType.SIMPLE_EXPRESSION;

        return cohortDefinition;
    }
}
