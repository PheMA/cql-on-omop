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
        // put("Encounter", VisitOccurrence.class);
        // FIXME: Encounter types appear to be capture in the PROCEDURE_OCCURRENCE table
        put("Encounter", ProcedureOccurrence.class);

        put("Procedure", ProcedureOccurrence.class);
        put("Observation", Measurement.class);
        put("MedicationStatement", DrugExposure.class);
        put("Condition", ConditionOccurrence.class);

        // 🤷🏻‍
        put("AdverseEvent", Death.class);
    }};
}

