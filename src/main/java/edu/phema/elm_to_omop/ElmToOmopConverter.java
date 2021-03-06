package edu.phema.elm_to_omop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.api.CohortService;
import edu.phema.elm_to_omop.api.ElmToOmopTranslator;
import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.helper.MyFormatter;
import edu.phema.elm_to_omop.io.OmopWriter;
import edu.phema.elm_to_omop.phenotype.BundlePhenotype;
import edu.phema.elm_to_omop.phenotype.FilePhenotype;
import edu.phema.elm_to_omop.phenotype.IPhenotype;
import edu.phema.elm_to_omop.phenotype.PhenotypeException;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.ConceptCodeCsvFileValuesetService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.json.simple.parser.ParseException;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * This project reads a query file written in ELM and converts to OMOP JSON format and runs it against an OHDSI repository.
 * 1. ELM file is transformed into OHDSI JSON format
 * 2. Uses OHDSI WebAPI to create the definition
 * 3. Uses OHDSI WebAPI to generate the cohort
 * 4. Uses OHDSI WebAPI to poll the status of the execution
 * 5. Uses OHDSI WebAPI to retrieve the results
 */
public class ElmToOmopConverter {
    private static Logger logger = Logger.getLogger(ElmToOmopConverter.class.getName());

    public static void printCohortDefinition(CohortDefinitionDTO cohortDefinition) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        System.out.println(mapper.writeValueAsString(cohortDefinition));
    }

    public static void main(String[] args) {
        ElmToOmopConverter converter = new ElmToOmopConverter();

        converter.run(args);
    }

    public void run(String[] args) {
        try {
            // Setup configuration
            Config config;
            if (args.length > 0) {
                config = new Config(args);
            } else {
                config = new Config(Config.getDefaultConfigPath());
            }

            FileHandler fh = setUpLogging("elmToOhdsiConverter.log");

            logger.info("Config: " + config.configString());

            String domain = config.getOmopBaseURL();
            String source = config.getSource();
            OmopRepositoryService omopService = new OmopRepositoryService(domain, source);
            OmopWriter omopWriter = new OmopWriter(logger);

            IValuesetService valuesetService = null;
            IPhenotype phenotype = null;
            if (config.isUsingBundle()) {
              BundlePhenotype bundlePhenotype = new BundlePhenotype(
                config.getInputBundleName(), config.getPhenotypeExpressions(), omopService);
              valuesetService = bundlePhenotype.getValuesetService();
              phenotype = bundlePhenotype;
            } else {
              // Initialize phenotype, which will do the following:
              //
              // 1. Determine if the user has specified which expression(s) is/are the phenotype definitions of interest.
              //    the CQL/ELM can be vague if not explicitly defined otherwise.
              // 2. Read the elm file and set up the objects
              phenotype = new FilePhenotype(config.getInputFileName(), config.getPhenotypeExpressions());

              // read the value set csv and add to the objects.  If the tab is specified, we assume that it is a spreadsheet.  Otherwise we will use the
              // default CSV reader.
              if (config.isTabSpecified()) {
                valuesetService = new SpreadsheetValuesetService(omopService, config.getVsFileName(), config.getTab());
              }
              else {
                valuesetService = new ConceptCodeCsvFileValuesetService(omopService, config.getVsFileName(), true);
              }
            }

            List<PhemaConceptSet> conceptSets = valuesetService.getConceptSets();

            // For each phenotype definition, get the OMOP JSON and write it out to file
            ElmToOmopTranslator translator = new ElmToOmopTranslator(valuesetService);
            CohortService cohortService = new CohortService(config, valuesetService);
            List<CohortDefinitionDTO> cohortDefinitions = translator.translatePhenotype(phenotype, conceptSets);

            for (CohortDefinitionDTO cohortDefinition : cohortDefinitions) {
                printCohortDefinition(cohortDefinition);

                // connect to the webAPI and post the cohort definition
                CohortDefinitionDTO created = omopService.createCohortDefinition(cohortDefinition);
                System.out.println("cohort definition id = " + created.id);

                // use the webAPI to generate the cohort results
                omopService.queueCohortGeneration(created.id);

                // keep pinging the repository until the definition has completed running
                InclusionRuleReport report = cohortService.getCohortDefinitionReport(created.id);

                // get the final count
                System.out.println("numPatients = " + report.summary.finalCount);
            }

            // Write the resulting cohort definitions to out to the filesystem
            omopWriter.writeOmopJson(cohortDefinitions, config.getOutFileName());
        } catch (PhenotypeException pe) {
            logger.severe(pe.getLocalizedMessage());
        } catch (IOException ioe) {
            logger.severe(ioe.getLocalizedMessage());
        } catch (InvalidFormatException ife) {
            logger.severe(ife.getLocalizedMessage());
        } catch (JAXBException jaxb) {
            logger.severe(jaxb.getLocalizedMessage());
        } catch (ParseException parse) {
            logger.severe(parse.getLocalizedMessage());
        } catch (Exception exc) {
            logger.severe(exc.getLocalizedMessage());
        }
        System.out.println("done");
    }

    private static FileHandler setUpLogging(String fileName) throws IOException {
        FileHandler fh = new FileHandler(fileName);
        logger.addHandler(fh);
        fh.setFormatter(new MyFormatter());
        return fh;
    }
}
