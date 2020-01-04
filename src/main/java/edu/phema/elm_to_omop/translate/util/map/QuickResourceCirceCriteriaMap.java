package edu.phema.elm_to_omop.translate.util.map;

import org.ohdsi.circe.cohortdefinition.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mappings are from the Common Data Model Harmonization project, and can be found at
 * http://build.fhir.org/ig/HL7/cdmh/profiles.html#omop-to-fhir-mappings
 */
public class QuickResourceCirceCriteriaMap {
    public static Map<String, Class<? extends Criteria>> resourceCriteriaMap = new HashMap<String, Class<? extends Criteria>>() {{
        // We currently support translating Encounter.class to VisitOccurrence
        // TODO: Support translating Encounter.type
        put("Encounter", VisitOccurrence.class);

        put("Procedure", ProcedureOccurrence.class);
        put("Observation", Measurement.class);
        put("MedicationStatement", DrugExposure.class);
        put("Condition", ConditionOccurrence.class);

        // 🤷🏻‍
        put("AdverseEvent", Death.class);
    }};
}

