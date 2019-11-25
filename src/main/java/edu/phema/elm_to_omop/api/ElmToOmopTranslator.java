package edu.phema.elm_to_omop.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.phema.elm_to_omop.api.exception.CqlStatementNotFoundException;
import edu.phema.elm_to_omop.api.exception.OmopTranslatorException;
import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.io.OmopWriter;
import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.model.omop.CirceUtil;
import edu.phema.elm_to_omop.model.omop.ConceptSet;
import edu.phema.elm_to_omop.model.phema.LibraryHelper;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.translate.OmopTranslateContext;
import edu.phema.elm_to_omop.translate.OmopTranslateVisitor;
import edu.phema.elm_to_omop.valueset.IValuesetService;
import edu.phema.elm_to_omop.valueset.SpreadsheetValuesetService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.json.simple.parser.ParseException;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.service.CohortDefinitionService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Provides methods for converting a CQL string
 * to OMOP WebAPI requests using the functionality
 * implemented in this library.
 */
public class ElmToOmopTranslator {
    private Logger logger;
    private List<ConceptSet> conceptSets;

    /**
     * Specify configuration to use for the translation
     *
     * @param config @see edu.phema.elm_to_omop.helper.Config
     * @throws InvalidFormatException
     * @throws ParseException
     * @throws IOException
     * @throws NullPointerException
     */
    public ElmToOmopTranslator(Config config) throws OmopTranslatorException {
        logger = Logger.getLogger(this.getClass().getName());

        OmopRepositoryService omopService = new OmopRepositoryService(config.getOmopBaseURL(), config.getSource());

        SpreadsheetValuesetService valuesetService = new SpreadsheetValuesetService(omopService, config.getVsFileName(), config.getTab());

        try {
            conceptSets = valuesetService.getConceptSets();
        } catch (Exception e) {
            throw new OmopTranslatorException("Error initializing concept sets", e);
        }
    }

    /**
     * Specify configuration to use for the translation, as well as a custom valueset reader.
     * This is most to support mocking during testing
     *
     * @param config @see edu.phema.elm_to_omop.helper.Config
     * @throws InvalidFormatException
     * @throws ParseException
     * @throws IOException
     * @throws NullPointerException
     */
    public ElmToOmopTranslator(Config config, IValuesetService valuesetService) throws OmopTranslatorException {
        logger = Logger.getLogger(this.getClass().getName());

        try {
            conceptSets = valuesetService.getConceptSets();
        } catch (Exception e) {
            throw new OmopTranslatorException("Error initializing concept sets", e);
        }
    }

    protected String cqlToOmopDoubleEscaped(String cqlString, String statementName) throws Exception {
        CohortDefinitionService.CohortDefinitionDTO cohortDefinition = this.cqlToOmopCohortDefinition(cqlString, statementName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper.writeValueAsString(cohortDefinition);
    }

    /**
     * Convert a single named statement into an OMOP JSONObject
     *
     * @param cqlString     String containing the CQL library
     * @param statementName The statement name to convert
     * @return The OMOP WebAPI request JSONObject
     * @throws Exception
     */
    public JsonObject cqlToOmopJsonObject(String cqlString, String statementName) throws Exception {
        String jsonish = cqlToOmopDoubleEscaped(cqlString, statementName);

        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(jsonish).getAsJsonObject();

        String expressionJson = root.get("expression").toString()
            .replace("\\\"", "\"").replaceAll("^\"|\"$", "");

        JsonObject expression = parser.parse(expressionJson).getAsJsonObject();

        root.remove("expression");
        root.add("expression", expression);

        return root;
    }

    public CohortDefinitionService.CohortDefinitionDTO cqlToOmopCohortDefinition(String cqlString, String statementName) throws Exception {
        if (statementName == null) {
            throw new CqlStatementNotFoundException("No named CQL statement specified");
        }

        CqlToElmTranslator translator = new CqlToElmTranslator();

        Library library = translator.cqlToElm(cqlString);

        List<ExpressionDef> expressions = library.getStatements().getDef();

        Optional<ExpressionDef> expressionDefOptional = expressions.stream()
            .filter(x -> statementName.equals(x.getName()))
            .findFirst();

        if (!expressionDefOptional.isPresent()) {
            throw new CqlStatementNotFoundException("Could not find statement " + statementName);
        }

        ExpressionDef expressionDef = expressionDefOptional.get();

        CohortExpression cohortExpression = LibraryHelper.generateCohortExpression(library, expressionDef, this.conceptSets);

        CohortDefinitionService.CohortDefinitionDTO cohortDefinition = CirceUtil.getDefaultCohortDefinition();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        cohortDefinition.name = expressionDef.getName();
        cohortDefinition.description = library.getLocalId();

        // This manual serialization isn't required in later versions of the WebAPI, see:
        // https://github.com/OHDSI/WebAPI/blob/v2.7.4/src/main/java/org/ohdsi/webapi/cohortdefinition/dto/CohortDTO.java#L10
        cohortDefinition.expression = mapper.writeValueAsString(cohortExpression);

        return cohortDefinition;
    }

    /**
     * Convert a single named statement into an OMOP JSON string
     *
     * @param cqlString     String containing the CQL library
     * @param statementName The statement name to convert
     * @return The OMOP WebAPI request JSON string
     * @throws Exception
     */
    public String cqlToOmopJson(String cqlString, String statementName) throws Exception {
        JsonObject object = this.cqlToOmopJsonObject(cqlString, statementName);

        return object.toString();
    }

    /**
     * Convert a list of named statement into an OMOP JSON string
     *
     * @param cqlString      String containing the CQL library
     * @param statementNames The list of statement names to convert
     * @return The string representation of a JSON array containing OMOP WebAPI requests for each statement
     * @throws Exception
     */
    public String cqlToOmopJson(String cqlString, List<String> statementNames) throws Exception {
        JsonArray results = new JsonArray();

        for (String name : statementNames) {
            results.add(this.cqlToOmopJsonObject(cqlString, name));
        }

        return results.toString();
    }

    /**
     * Translate an ELM expression into a Circe cohort expression
     *
     * @param expression The expression to translate
     * @return The equivalent Circe cohort expression
     */
    public CohortExpression translate(Expression expression) {
        OmopTranslateContext context = new OmopTranslateContext();

        OmopTranslateVisitor visitor = new OmopTranslateVisitor();

        visitor.visitElement(expression, context);

        return context.generateCohortExpression();
    }
}
