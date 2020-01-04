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
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.vocabulary.ConceptSearch;

import java.util.ArrayList;
import java.util.List;

public class VocabularyTranslationTestTransformer extends ResponseTransformer {
    private List<PhemaConceptSet> conceptSetsList;
    private ObjectMapper mapper;

    public VocabularyTranslationTestTransformer() {
        mapper = new ObjectMapper();

        try {
            conceptSetsList = mapper
                .readValue(PhemaTestHelper.getFileAsString("vocabulary/encounter/act-encounter-codes-translated.phema-concept-sets.json"), new TypeReference<List<PhemaConceptSet>>() {
                });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public Response transform(Request request, Response response, FileSource fileSource, Parameters parameters) {

        try {
            ConceptSearch search = mapper.readValue(request.getBodyAsString(), ConceptSearch.class);

            List<Concept> matchingConcepts = getResponse(search.query);

            return Response.Builder.like(response)
                .but().body(mapper.writeValueAsString(matchingConcepts))
                .build();

        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }

    private List<Concept> getResponse(String code) {
        ArrayList<Concept> response = new ArrayList<>();

        PhemaConceptSet concepts = conceptSetsList.get(0);

        ConceptSetExpression.ConceptSetItem[] items = concepts.expression.items;

        for (ConceptSetExpression.ConceptSetItem item : items) {
            if (item.concept.conceptCode.equals(code)) {
                response.add(item.concept);
            }
        }

        return response;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    @Override
    public String getName() {
        return "vocabulary-translation-transformer";
    }
}