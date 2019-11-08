package edu.phema.elm_to_omop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.*;
import edu.phema.elm_to_omop.api.exception.CqlStatementNotFoundException;
import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.io.OmopWriter;
import edu.phema.elm_to_omop.io.ValueSetReader;
import edu.phema.elm_to_omop.model.omop.ConceptSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
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
     * The configuration to use for the translation
     *
     * @param config @see edu.phema.elm_to_omop.helper.Config
     * @throws InvalidFormatException
     * @throws ParseException
     * @throws IOException
     * @throws NullPointerException
     */
    public ElmToOmopTranslator(Config config) throws InvalidFormatException, ParseException, IOException, NullPointerException {
        logger = Logger.getLogger(this.getClass().getName());

        this.buildConceptSets(config);
    }

    /**
     * Not part of the public API, but loads the concepts
     * from the file specified in the config
     *
     * @param config @see edu.phema.elm_to_omop.helper.Config
     * @throws InvalidFormatException
     * @throws ParseException
     * @throws IOException
     * @throws NullPointerException
     */
    private void buildConceptSets(Config config) throws InvalidFormatException, ParseException, IOException, NullPointerException {
        String domain = config.getOmopBaseUrl();
        String source = config.getSource();
        String vsFileName = config.getVsFileName();

        URL url = this.getClass().getClassLoader().getResource(vsFileName);

        ValueSetReader valueSetReader = new ValueSetReader();
        this.conceptSets = valueSetReader.getConceptSets(url.getPath(), config.getTab(), domain, source);
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
        CqlToElmTranslator translator = new CqlToElmTranslator();

        Library library = translator.cqlToElm(cqlString);

        List<ExpressionDef> expressions = library.getStatements().getDef();

        Optional<ExpressionDef> expressionDef = expressions.stream()
            .filter(x -> statementName.equals(x.getName()))
            .findFirst();

        if (!expressionDef.isPresent()) {
            throw new CqlStatementNotFoundException("Could not find statement " + statementName);
        }

        OmopWriter omopWriter = new OmopWriter(logger);

        // 😔 Smooth over some weird JSON representation issues
        // Ideally we need to create POJOs or re-use the ones from
        // https://github.com/OHDSI/WebAPI/tree/master/src/main/java/org/ohdsi/webapi/cohortdefinition
        // and then use a serialization library like GSON to create JSON for us
        String jsonish = omopWriter.generateOmopJson(expressionDef.get(), library, this.conceptSets);

        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse(jsonish).getAsJsonObject();

        String expressionJson = root.get("expression").toString()
            .replace("\\\"", "\"").replaceAll("^\"|\"$", "");

        JsonObject expression = parser.parse(expressionJson).getAsJsonObject();

        root.remove("expression");
        root.add("expression", expression);

        return root;
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
}
