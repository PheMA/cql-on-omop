package edu.phema.elm_to_omop.helper;

import org.ohdsi.circe.cohortdefinition.*;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.service.CohortDefinitionService;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Various utility methods, some of which are necessary because
 * the Circe classes don't have constructors, and it's possible to
 * generate invalid objects using the empty default constructors.
 */
public class CirceUtil {
    private CirceUtil()  {
        super();
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
     * Extends a primitive array of DemographicCriterias by adding a new DemographicCriteria.
     * This is necessary because Circe uses primitive arrays instead of Java collections.
     *
     * @param demographicCriteriaGroupArray The primitive DemographicCriteria array to extend
     * @param demographicCriteria           The new DemographicCriteria to add
     * @return The extended DemographicCriteria array
     */
    public static DemographicCriteria[] addDemographicCriteria(DemographicCriteria[] demographicCriteriaGroupArray, DemographicCriteria demographicCriteria) {
        DemographicCriteria[] newGroupArray = new DemographicCriteria[demographicCriteriaGroupArray.length + 1];
        ArrayList<DemographicCriteria> groupArrayList = new ArrayList<>(Arrays.asList(demographicCriteriaGroupArray));
        groupArrayList.add(demographicCriteria);
        return groupArrayList.toArray(newGroupArray);
    }

    /**
     * The translator focuses on creating InclusionRules (CriteraGroups really),
     * and we let the initial population consist of anyone with any visit
     * occurrence
     *
     * @return A PrimaryCriteria representing any visit occurrence
     */
    public static PrimaryCriteria defaultPrimaryCriteria() {
        PrimaryCriteria primaryCriteria = new PrimaryCriteria();

        primaryCriteria.observationWindow = new ObservationFilter();
        primaryCriteria.criteriaList = new Criteria[]{
            new VisitOccurrence()
        };

        return primaryCriteria;
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

        corelatedCriteria.occurrence = defaultOccurrence();

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
    public static CohortDefinitionService.CohortDefinitionDTO defaultCohortDefinition() {
        CohortDefinitionService.CohortDefinitionDTO cohortDefinition = new CohortDefinitionService.CohortDefinitionDTO();

        cohortDefinition.expressionType = ExpressionType.SIMPLE_EXPRESSION;

        return cohortDefinition;
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
     * Returns a CorelatedCriteria from a Circe Criteria instance, Occurrence object, and restrictVisit boolean
     *
     * @param criteria      The Criteria instance to wrap
     * @param occurrence    The Occurrence object
     * @param restrictVisit Whether or not to restrict the CorelatedCriteria to same visit as the parent Criteria or
     *                      cohort entry event
     * @return The created CorelatedCriteria
     */
    public static CorelatedCriteria corelatedCriteriaFromCriteria(Criteria criteria, Occurrence occurrence, boolean restrictVisit) {
        CorelatedCriteria corelatedCriteria = defaultCorelatedCriteria();

        corelatedCriteria.criteria = criteria;
        corelatedCriteria.occurrence = occurrence;
        corelatedCriteria.restrictVisit = restrictVisit;

        return corelatedCriteria;
    }

    /**
     * Generates a Circe CriteriaGroup from a single CorelatedCriteria and group inclusion properties
     *
     * @param criteria The CorelatedCriteria
     * @param type     The inclusion type
     * @param count    The inclusion count
     * @return The created CriteriaGroup
     */
    public static CriteriaGroup criteriaGroupFromCorelatedCriteria(CorelatedCriteria criteria, CirceConstants.CriteriaGroupType type, Integer count) {
        CriteriaGroup group = new CriteriaGroup();

        group.criteriaList = new CorelatedCriteria[]{criteria};

        group.type = type.toString();
        group.count = count;

        return group;
    }

    /**
     * Generates a Circe CriteriaGroup from a single Criteria, group inclusion, and restrictVisit properties. Defaults
     * are used for the occurrence and window properties.
     *
     * @param criteria The Criteria
     * @param type     The inclusion type
     * @param count    The inclusion count
     * @return The created CriteriaGroup
     */
    public static CriteriaGroup criteriaGroupFromCriteria(Criteria criteria, CirceConstants.CriteriaGroupType type, Integer count, boolean restrictVisit) {
        CorelatedCriteria corelatedCriteria = defaultCorelatedCriteria();

        corelatedCriteria.criteria = criteria;
        corelatedCriteria.restrictVisit = restrictVisit;

        return criteriaGroupFromCorelatedCriteria(corelatedCriteria, type, count);
    }
}
