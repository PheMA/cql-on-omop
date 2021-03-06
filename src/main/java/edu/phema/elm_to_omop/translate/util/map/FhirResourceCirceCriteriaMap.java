package edu.phema.elm_to_omop.translate.util.map;

import org.ohdsi.circe.cohortdefinition.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mappings are from the Common Data Model Harmonization project, and can be found at
 * http://build.fhir.org/ig/HL7/cdmh/profiles.html#omop-to-fhir-mappings
 */
public class FhirResourceCirceCriteriaMap {
    private FhirResourceCirceCriteriaMap()  {
        super();
    }

    public static final Map<String, Class<? extends Criteria>> resourceCriteriaMap = new HashMap<>();

    static {
        resourceCriteriaMap.put("Encounter", VisitOccurrence.class);
        resourceCriteriaMap.put("Procedure", ProcedureOccurrence.class);
        // TODO we need to disambiguate mappings for Procedure
        // resourceCriteriaMap.put("Procedure", DeviceExposure.class);
        resourceCriteriaMap.put("Observation", Measurement.class);
        // TODO we need to disambiguate mappings for Observation
        // resourceCriteriaMap.put("Observation", Observation.class);
        resourceCriteriaMap.put("MedicationRequest", DrugExposure.class);
        resourceCriteriaMap.put("Condition", ConditionOccurrence.class);
        resourceCriteriaMap.put("AdverseEvent", Death.class);
        resourceCriteriaMap.put("Specimen", Specimen.class);
    }
}

