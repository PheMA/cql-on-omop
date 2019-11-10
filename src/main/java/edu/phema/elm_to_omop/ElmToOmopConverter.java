package edu.phema.elm_to_omop;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import edu.phema.elm_to_omop.io.*;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.valueset.SpreadsheetValuesetService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.json.simple.parser.ParseException;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.helper.MyFormatter;
//import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model.omop.ConceptSet;

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

    public static void main(String args[]) {

        try {
            // Setup configuration
            Config config;
            if (args.length > 0) {
                config = new Config(args);
            } else {
                config = new Config(Config.getDefaultConfigPath());
            }

            FileHandler fh = setUpLogging("elmToOhdsiConverter.log");

            String directory = getResourceDirectory();
            logger.info("Config: " + config.configString());

            // Determine if the user has specified which expression(s) is/are the phenotype definitions of interest.
            // the CQL/ELM can be vague if not explicitly defined otherwise.
            List<String> phenotypeExpressionNames = config.getPhenotypeExpressions();
            if (phenotypeExpressionNames.size() == 0) {
                System.out.println("Please provide the name or names of the expressions to use as your phenotype(s) - set this using PHENOTYPE_EXPRESSIONS");
                return;
            }

            String domain = config.getOmopBaseUrl();
            String source = config.getSource();
            OmopRepositoryService omopService = new OmopRepositoryService(domain, source);
            OmopWriter omopWriter = new OmopWriter(logger);

            // read the elm file and set up the objects
            Library elmContents = ElmReader.readElm(directory, config.getInputFileName(), logger);
            List<ExpressionDef> expressions = elmContents.getStatements().getDef();

            List<ExpressionDef> phenotypeExpressions = expressions.stream()
                .filter(x -> phenotypeExpressionNames.contains(x.getName()))
                .collect(Collectors.toList());
            if (phenotypeExpressions == null || phenotypeExpressions.size() == 0) {
                System.out.println("Could not find any of the expressions you designated as phenotypes.  Please provide the name or names of the expressions to use as your phenotype(s) - set this using PHENOTYPE_EXPRESSIONS");
                return;
            }

            // read the value set csv and add to the objects
            SpreadsheetValuesetService valuesetService = new SpreadsheetValuesetService(omopService, directory + config.getVsFileName(), config.getTab());

            List<ConceptSet> conceptSets = valuesetService.getConceptSets();

            // For each phenotype definition, get the OMOP JSON and write it out to file
            for (ExpressionDef phenotypeExpression : phenotypeExpressions) {
                String omopJson = omopWriter.writeOmopJson(phenotypeExpression, elmContents, conceptSets, directory, config.getOutFileName());
                System.out.println(omopJson);

                // connect to the webAPI and post the cohort definition
                String id = omopService.postCohortDefinition(omopJson);
                System.out.println("cohort definition id = " + id);

                // use the webAPI to generate the cohort results
                omopService.generateCohort(id);

                // keep pinging the repository until the definition has completed running
                String status = "";
                int count = 1;
                while (!status.equalsIgnoreCase("COMPLETE") && count < 1000) {
                    status = omopService.getExecutionStatus(id);
                    TimeUnit.SECONDS.sleep(1);
                    count++;
                }

                // get the final count
                String numPatients = omopService.getCohortCount(id);
                System.out.println("numPatients = " + numPatients);

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InvalidFormatException ife) {
            ife.printStackTrace();
        } catch (JAXBException jaxb) {
            jaxb.printStackTrace();
        } catch (ParseException pe) {
            pe.printStackTrace();
//        }  catch(InterruptedException ie)   {
//            ie.printStackTrace();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        System.out.println("done");
    }

    private static FileHandler setUpLogging(String fileName) throws IOException {
        FileHandler fh = new FileHandler(fileName);
        logger.addHandler(fh);
        fh.setFormatter(new MyFormatter());
        return fh;
    }

    private static String getResourceDirectory() {
        String workingDir = System.getProperty("user.dir");

        if (!workingDir.endsWith("src" + File.separator + "main")) {
            workingDir += File.separator + "src" + File.separator + "main";
        }

        return workingDir + File.separator + "resources" + File.separator;
    }


}
