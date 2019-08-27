package edu.phema.elm_to_omop.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.model.omop.ConceptSet;
import edu.phema.elm_to_omop.model.omop.ExpressionDefinition;
import edu.phema.elm_to_omop.model.omop.InclusionRule;
import edu.phema.elm_to_omop.model.omop.OmopRoot;
import edu.phema.elm_to_omop.model.phema.LibraryHelper;
import org.hl7.elm.r1.*;

public class OmopWriter {
    private Logger logger = null;

    public OmopWriter(Logger logger) {
        this.logger = logger;
    }

    /**
     * Makes sure the json has been created and writes it to file designated in the configuration
     * Returns the json string
     */
    public String writeOmopJson(ExpressionDef expression, Library elmContents, java.util.List<ConceptSet> conceptSets, String directory) throws Exception {
        String jsonFileName = directory + Config.getOutFileName();
        try (FileWriter jsonFile = new FileWriter(jsonFileName)) {
            String json = generateOmopJson(expression, elmContents, conceptSets);

            logger.info(String.format("Preparing to write JSON to %s", jsonFileName));
            jsonFile.write(json);
            return json;
        }
    }

    /**
     * Create the OMOP JSON from an ELM Library object, and associated populated list of OMOP ConceptSets
     * @param expression The expression that defines the phenotype overall
     * @param elmContents The ELM object to transform to OMOP JSON
     * @param conceptSets
     * @return
     * @throws IOException
     */
    public String generateOmopJson(ExpressionDef expression, Library elmContents, java.util.List<ConceptSet> conceptSets) throws Exception {
        OmopRoot root = new OmopRoot();
        root.setName(expression.getName());
        root.setDescription(elmContents.getLocalId());
        root.setExpressionType("SIMPLE_EXPRESSION");          // TODO: hard coded value

        // TODO Get the concept sets that are relevant to the expression.  This may require some recursion through dependent expressions
        ExpressionDefinition exDef = new ExpressionDefinition();
        root.setExpression(exDef);

        Expression exp = expression.getExpression();
        InclusionRule inclusionRule = LibraryHelper.generateInclusionRule(elmContents, exp, conceptSets);
        exDef.addInclusionRule(inclusionRule);
        exDef.setConceptSets(conceptSets);

        return root.getJson();
    }
}
