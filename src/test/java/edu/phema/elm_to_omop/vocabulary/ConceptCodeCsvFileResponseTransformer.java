package edu.phema.elm_to_omop.vocabulary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import edu.phema.elm_to_omop.PhemaTestHelper;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.ConceptSearch;

import java.util.*;
import java.util.stream.Collectors;

public class ConceptCodeCsvFileResponseTransformer extends ResponseTransformer {
    private Map<String, List<Concept>> terminology;
    private ObjectMapper mapper;

    public ConceptCodeCsvFileResponseTransformer() {
        mapper = new ObjectMapper();

        // Read the ICD codes
        List<PhemaConceptSet> conceptSetsList = new ArrayList<>();

        try {
            conceptSetsList = mapper
                .readValue(PhemaTestHelper.getFileAsString("vocabulary/heart-failure-diagnosis-icd-codes.phema-concept-sets.json"), new TypeReference<List<PhemaConceptSet>>() {
                });
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // Add codes to terminology map
        terminology = new HashMap<String, List<Concept>>();

        List<Concept> icd9codes = Arrays
            .asList(conceptSetsList.get(0).expression.items)
            .stream()
            .map(ci -> ci.concept)
            .filter(c -> c.vocabularyId.equals("ICD9CM"))
            .collect(Collectors.toList());

        List<Concept> icd10codes = Arrays
            .asList(conceptSetsList.get(0).expression.items)
            .stream()
            .map(ci -> ci.concept)
            .filter(c -> c.vocabularyId.equals("ICD10CM"))
            .collect(Collectors.toList());

        terminology.put("ICD9CM", icd9codes);
        terminology.put("ICD10CM", icd10codes);
    }

    @Override
    public Response transform(Request request, Response response, FileSource fileSource, Parameters parameters) {

        try {
            ConceptSearch search = mapper.readValue(request.getBodyAsString(), ConceptSearch.class);

            List<Concept> matchingConcepts = getResponse(search.query, search.vocabularyId[0]);

            return Response.Builder.like(response)
                .but().body(mapper.writeValueAsString(matchingConcepts))
                .build();

        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }

    private List<Concept> getResponse(String code, String vocabulary) {
        List<Concept> concepts = terminology.get(vocabulary);

        List<Concept> response = concepts
            .stream()
            .filter(c -> c.conceptCode.equals(code))
            .collect(Collectors.toList());

        return response;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    @Override
    public String getName() {
        return "concept-transformer";
    }
}