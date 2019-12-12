package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PhemaJsonConceptSetServiceTest {
    private IValuesetService valuesetService;

    @Test
    public void success() throws Exception {
        String conceptSetList = PhemaTestHelper.getFileAsString("vocabulary/heart-failure-diagnosis-icd-codes.phema-concept-sets.json");

        valuesetService = new PhemaJsonConceptSetService(conceptSetList);

        List<PhemaConceptSet> concepts = valuesetService.getConceptSets();

        assertEquals(concepts.size(), 1);
        assertEquals(concepts.get(0).getOid(), "2.16.840.1.999999.2");
        assertEquals(concepts.get(0).expression.items.length, 37);
    }

    @Test
    public void failure() throws Exception {
        String conceptSetList = PhemaTestHelper.getFileAsString("vocabulary/icd9-only.csv");

        assertThrows(ValuesetServiceException.class, () -> valuesetService = new PhemaJsonConceptSetService(conceptSetList));
    }
}
