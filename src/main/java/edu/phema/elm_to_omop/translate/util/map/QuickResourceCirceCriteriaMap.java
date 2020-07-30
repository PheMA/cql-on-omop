package edu.phema.elm_to_omop.translate.util.map;

import org.ohdsi.circe.cohortdefinition.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mappings are from the Common Data Model Harmonization project, and can be found at
 * http://build.fhir.org/ig/HL7/cdmh/profiles.html#omop-to-fhir-mappings
 */
public class QuickResourceCirceCriteriaMap {
    private QuickResourceCirceCriteriaMap()  {
        super();
    }

    public static final Map<String, Class<? extends Criteria>> resourceCriteriaMap = new HashMap<>();

    static {
        // We currently support translating Encounter.class to VisitOccurrence
        // TODO: Support translating Encounter.type
        resourceCriteriaMap.put("Encounter", VisitOccurrence.class);

        resourceCriteriaMap.put("Procedure", ProcedureOccurrence.class);
        resourceCriteriaMap.put("Observation", Measurement.class);
        resourceCriteriaMap.put("MedicationStatement", DrugExposure.class);
        resourceCriteriaMap.put("Condition", ConditionOccurrence.class);

        resourceCriteriaMap.put("AdverseEvent", Death.class);
    }
}

