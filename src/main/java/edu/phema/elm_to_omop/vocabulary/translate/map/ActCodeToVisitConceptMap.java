package edu.phema.elm_to_omop.vocabulary.translate.map;

import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates the ActCode vocabulary to the OMOP internal visit concept vocabulary
 * <p>
 * Source: https://www.hl7.org/fhir/STU3/v3/ActEncounterCode/vs.html
 * Target: http://athena.ohdsi.org/search-terms/terms?vocabulary=Visit
 */
public class ActCodeToVisitConceptMap implements IVocabularyMap {
    private static Map<String, String> map;

    static {
        map = new HashMap<>();

        // TODO: Have someone validate these mappings
        map.put("AMB", "OP");
        map.put("EMER", "ER");
        map.put("FLD", "OMOP4822457");
        map.put("HH", "OMOP4822459");
        map.put("IMP", "IP");
        map.put("ACUTE", "IP");
        map.put("NONAC", "IP");
        map.put("PRENC", "OP");
        map.put("SS", "OP");
        map.put("VR", "OP");
    }

    @Override
    public String sourceVocabulary() {
        return "http://hl7.org/fhir/v3/ActCode";
    }

    @Override
    public PhemaCode translate(PhemaCode code) {
        code.setCodeSystem("Visit");
        code.setCode(map.get(code.getCode()));

        return code;
    }
}
