package edu.phema.elm_to_omop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.api.exception.CohortServiceException;
import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.model.omop.ConceptSet;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.valueset.IValuesetService;
import edu.phema.elm_to_omop.valueset.SpreadsheetValuesetService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;

import java.util.List;
import java.util.logging.Logger;

/**
 * Public API for dealing with cohorts and cohort definitions
 */
public class CohortService {
    private Logger logger;
    private List<ConceptSet> conceptSets;

    private Config config;
    private IValuesetService valuesetService;
    private IOmopRepositoryService omopService;

    /**
     * Constructor using just a config object
     *
     * @param config The config object
     * @throws CohortServiceException
     */
    public CohortService(Config config) throws CohortServiceException {
        logger = Logger.getLogger(this.getClass().getName());

        this.omopService = new OmopRepositoryService(config.getOmopBaseURL(), config.getSource());

        this.config = config;
        this.valuesetService = new SpreadsheetValuesetService(omopService, config.getVsFileName(), config.getTab());

        try {
            conceptSets = valuesetService.getConceptSets();
        } catch (Exception e) {
            throw new CohortServiceException("Error initializing concept sets", e);
        }
    }

    /**
     * Constructor using a config object and an existing valueset service
     *
     * @param config          The config object
     * @param valuesetService The valuset service instance
     * @throws CohortServiceException
     */
    public CohortService(Config config, IValuesetService valuesetService) throws CohortServiceException {
        logger = Logger.getLogger(this.getClass().getName());

        this.config = config;
        this.valuesetService = valuesetService;
        this.omopService = new OmopRepositoryService(config.getOmopBaseURL(), config.getSource());

        try {
            conceptSets = valuesetService.getConceptSets();
        } catch (Exception e) {
            throw new CohortServiceException("Error initializing concept sets", e);
        }
    }

    /**
     * Constructor using existing valueset service and OMOP repository instances
     *
     * @param valuesetService The valuset service
     * @param omopService     The OMOP respository service
     * @throws CohortServiceException
     */
    public CohortService(IValuesetService valuesetService, IOmopRepositoryService omopService) throws CohortServiceException {
        logger = Logger.getLogger(this.getClass().getName());

        this.config = null;
        this.valuesetService = valuesetService;
        this.omopService = omopService;

        try {
            conceptSets = valuesetService.getConceptSets();
        } catch (Exception e) {
            throw new CohortServiceException("Error initializing concept sets", e);
        }
    }

    /**
     * Create a cohort definition from a CQL string and a named CQL statement
     *
     * @param cqlString     The CQL string
     * @param statementName The named CQL statement
     * @return The cohort definition that was created
     * @throws CohortServiceException
     */
    public CohortDefinitionDTO createCohortDefinition(String cqlString, String statementName) throws CohortServiceException {

        try {
            ElmToOmopTranslator translator = new ElmToOmopTranslator(config, valuesetService);

            // The following deserialization will hopefully eventually become unnecessary
            String cohortDefinitionJSon = translator.cqlToOmopDoubleEscaped(cqlString, statementName);
            CohortDefinitionDTO cohortDefinitionDTO = new ObjectMapper().readValue(cohortDefinitionJSon, CohortDefinitionDTO.class);

            return omopService.createCohortDefinition(cohortDefinitionDTO);
        } catch (Throwable t) {
            throw new CohortServiceException("Error creating cohort definition", t);
        }
    }

    /**
     * Generate the cohort for a cohort definition specified by the given id
     *
     * @param id The id of the cohort definition to generate
     * @return The created OMOP job
     * @throws CohortServiceException
     */
    public JobExecutionResource queueCohortGeneration(Integer id) throws CohortServiceException {
        try {
            return omopService.queueCohortGeneration(id);
        } catch (Throwable t) {
            throw new CohortServiceException("Error queueing up cohort for generation", t);
        }
    }

    /**
     * Generate the cohort for cohort definition speciied by the given CQL string
     * and statement name
     *
     * @param cqlString     The CQL string
     * @param statementName The named CQL statement that defines the cohort
     * @return The created OMOP job
     * @throws CohortServiceException
     */
    public JobExecutionResource queueCohortGeneration(String cqlString, String statementName) throws CohortServiceException {
        try {
            CohortDefinitionDTO cohortDefinition = createCohortDefinition(cqlString, statementName);

            return omopService.queueCohortGeneration(cohortDefinition.id);
        } catch (Throwable t) {
            throw new CohortServiceException("Error queueing up cohort for generation", t);
        }
    }
}
