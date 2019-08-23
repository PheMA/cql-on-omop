package edu.phema.elm_to_omop;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import edu.phema.elm_to_omop.helper.WebApiFormatter;
import edu.phema.elm_to_omop.io.OmopRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;
import org.json.simple.parser.ParseException;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.helper.MyFormatter;
import edu.phema.elm_to_omop.io.ElmReader;
import edu.phema.elm_to_omop.io.OmopWriter;
import edu.phema.elm_to_omop.io.ValueSetReader;
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

public class ElmToOmopConverter
{
    private static Logger logger = Logger.getLogger(ElmToOmopConverter.class.getName());

    public static void main(String args[])  {

        try  {
            FileHandler fh = setUpLogging("elmToOhdsiConverter.log");

            String directory = setUpConfiguration(args);

            // Determine if the user has specified which expression(s) is/are the phenotype definitions of interest.
            // the CQL/ELM can be vague if not explicitly defined otherwise.
            List<String> phenotypeExpressionNames = Config.getPhenotypeExpressions();
            if (phenotypeExpressionNames.size() == 0) {
                System.out.println("Please provide the name or names of the expressions to use as your phenotype(s) - set this using PHENOTYPE_EXPRESSIONS");
                return;
            }

            String domain = Config.getOmopBaseUrl();
            String source = Config.getSource();
            OmopWriter omopWriter = new OmopWriter(logger);

            // read the elm file and set up the objects
            Library elmContents = ElmReader.readElm(directory, logger);
            List<ExpressionDef> expressions = elmContents.getStatements().getDef();

            List<ExpressionDef> phenotypeExpressions = expressions.stream()
                .filter(x -> phenotypeExpressionNames.contains(x.getName()))
                .collect(Collectors.toList());
            if (phenotypeExpressions == null || phenotypeExpressions.size() == 0) {
                System.out.println("Could not find any of the expressions you designated as phenotypes.  Please provide the name or names of the expressions to use as your phenotype(s) - set this using PHENOTYPE_EXPRESSIONS");
                return;
            }

            // read the value set csv and add to the objects
            List<ConceptSet> conceptSets = ValueSetReader.getConceptSets(elmContents, directory, logger, domain, source);

            // For each phenotype definition, get the OMOP JSON and write it out to file
            for (ExpressionDef phenotypeExpression : phenotypeExpressions) {
                String omopJson = omopWriter.writeOmopJson(phenotypeExpression, elmContents, conceptSets, directory);
                System.out.println(omopJson);

                //convert statement to one accepted by webAPI
                omopJson = WebApiFormatter.getWebApiJson(omopJson);

                // connect to the webAPI and post the cohort definition
                String id = OmopRepository.postCohortDefinition(domain, omopJson);
                System.out.println("cohort definition id = " +id);

                // use the webAPI to generate the cohort results
                OmopRepository.generateCohort(domain, id, source);

                // keep pinging the repository until the definition has completed running
                String status = "";
                int count = 1;
                while(!status.equalsIgnoreCase("COMPLETE") && count < 1000)  {
                    status = OmopRepository.getExecutionStatus(domain, id);
                    TimeUnit.SECONDS.sleep(1);
                    count++;
                }

                // get the final count
                String numPatients = OmopRepository.getCohortCount(domain, id, source);
                System.out.println("numPatients = " +numPatients);

            }
        }  catch(IOException ioe)   {
            ioe.printStackTrace();
        }  catch(InvalidFormatException ife)   {
            ife.printStackTrace();
        }  catch(JAXBException jaxb)   {
            jaxb.printStackTrace();
        }  catch(ParseException pe)   {
            pe.printStackTrace();
//        }  catch(InterruptedException ie)   {
//            ie.printStackTrace();
        }
        catch (Exception exc) {
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

    private static String setUpConfiguration(String args[])  {
        if (args.length > 0)
        {
            new Config(args);
        }
        else
        {
            new Config();
        }

        String workingDir = System.getProperty("user.dir");
        return workingDir + File.separator + "resources" + File.separator;
    }





}
