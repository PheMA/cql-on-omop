package edu.phema.elm_to_omop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.api.exception.CohortServiceException;
import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.ConceptCodeCsvFileValuesetService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;
import org.ohdsi.webapi.service.CohortDefinitionService.GenerateSqlResult;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

/**
 * Public API for dealing with cohorts and cohort definitions.
 */
public class CohortService {
    private Logger logger;
    private List<PhemaConceptSet> conceptSets;

    private Config config;
    private IValuesetService valuesetService;
    private IOmopRepositoryService omopService;

    /**
     * Constructor using just a config object.
     *
     * @param config The config object
     * @throws CohortServiceException
     */
    public CohortService(Config config) throws CohortServiceException {
        logger = Logger.getLogger(this.getClass().getName());

        this.omopService = new OmopRepositoryService(config.getOmopBaseURL(), config.getSource());

        this.config = config;
        this.valuesetService = new SpreadsheetValuesetService(omopService, config.getVsFileName(), config.getTab());
        if (config.isTabSpecified()) {
          valuesetService = new SpreadsheetValuesetService(omopService, config.getVsFileName(), config.getTab());
        }
        else {
          valuesetService = new ConceptCodeCsvFileValuesetService(omopService, config.getVsFileName(), true);
        }

        try {
            conceptSets = valuesetService.getConceptSets();
        } catch (Exception e) {
            throw new CohortServiceException("Error initializing concept sets", e);
        }
    }

    /**
     * Constructor using a config object and an existing valueset service.
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
     * Constructor using existing valueset service and OMOP repository instances.
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
     * Create a cohort definition from a CQL string and a named CQL statement.
     *
     * @param cqlString     The CQL string
     * @param statementName The named CQL statement
     * @return The cohort definition that was created
     * @throws CohortServiceException
     */
    public CohortDefinitionDTO createCohortDefinition(String cqlString, String statementName) throws CohortServiceException {

        try {
            ElmToOmopTranslator translator = new ElmToOmopTranslator(valuesetService);

            // The following deserialization will hopefully eventually become unnecessary
            String cohortDefinitionJSon = translator.cqlToOmopDoubleEscaped(cqlString, statementName);
            CohortDefinitionDTO cohortDefinitionDTO = new ObjectMapper().readValue(cohortDefinitionJSon, CohortDefinitionDTO.class);

            return omopService.createCohortDefinition(cohortDefinitionDTO);
        } catch (Throwable t) {
            throw new CohortServiceException("Error creating cohort definition", t);
        }
    }

    /**
     * Generate the cohort for a cohort definition specified by the given id.
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
     * and statement name.
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

    /**
     * Get information a given cohort definition, such as the generation status.
     *
     * @param id The cohort definition id
     * @return A list of cohort definition info objects
     * @throws CohortServiceException
     */
    public List<CohortGenerationInfo> getCohortDefinitionInfo(Integer id) throws CohortServiceException {
        try {
            return omopService.getCohortDefinitionInfo(id);
        } catch (Throwable t) {
            throw new CohortServiceException("Error getting cohort definition info", t);
        }
    }

    /**
     * Creates a cohort definition from CQL, queues it up for generation, and returns info about it.
     *
     * @param cqlString     The CQL string
     * @param statementName The CQL statement name that defines the cohort
     * @return A list of cohort definition info objects
     * @throws CohortServiceException
     */
    public List<CohortGenerationInfo> getCohortDefinitionInfo(String cqlString, String statementName) throws CohortServiceException {
        try {
            CohortDefinitionDTO cohortDefinition = createCohortDefinition(cqlString, statementName);

            omopService.queueCohortGeneration(cohortDefinition.id);

            return omopService.getCohortDefinitionInfo(cohortDefinition.id);
        } catch (Throwable t) {
            throw new CohortServiceException("Error getting cohort definition info", t);
        }
    }

    /**
     * Gets the report for a given cohort definition.
     *
     * @param id The cohort definition id
     * @return The cohort report
     * @throws CohortServiceException
     */
    public InclusionRuleReport getCohortDefinitionReport(Integer id) throws CohortServiceException {
        try {
            omopService.queueCohortGeneration(id);

            RetryPolicy retryPolicy = new RetryPolicy();
            // FIXME: Just taking the first info object can't be correct
            retryPolicy.handleResultIf(info -> ((List<CohortGenerationInfo>) info).get(0).getStatus() != GenerationStatus.COMPLETE);
            retryPolicy.withBackoff(1, 30, ChronoUnit.SECONDS);

            Failsafe.with(retryPolicy).get(() -> omopService.getCohortDefinitionInfo(id));

            return omopService.getCohortDefinitionReport(id);
        } catch (Throwable t) {
            throw new CohortServiceException("Error getting cohort definition report", t);
        }
    }

    /**
     * Creates a cohort definition from CQL, queues it up for generation, waits until generation is
     * complete, and then returns the cohort definition report
     *
     * @param cqlString     The CQL string
     * @param statementName The CQL statement name that defines the cohort
     * @return The cohort report
     * @throws CohortServiceException
     */
    public InclusionRuleReport getCohortDefinitionReport(String cqlString, String statementName) throws CohortServiceException {
        try {
            CohortDefinitionDTO cohortDefinition = createCohortDefinition(cqlString, statementName);

            return getCohortDefinitionReport(cohortDefinition.id);
        } catch (Throwable t) {
            throw new CohortServiceException("Error getting cohort definition report", t);
        }
    }

    /**
     * Gets the cohort definition SQL. One of the following target dialects may optionally
     * be specified:
     * <p>
     * - "sql server"
     * - "pdw"
     * - "oracle"
     * - "postgresql"
     * - "redshift"
     * - "impala"
     * - "netezza"
     * </p>
     *
     * @param id            The cohort definition id
     * @param targetDialect The target SQL dialect
     * @return The resulting SQL
     * @throws CohortServiceException
     */
    public GenerateSqlResult getCohortDefinitionSql(Integer id, String targetDialect) throws CohortServiceException {
        try {
            return omopService.getCohortDefinitionSql(id, targetDialect);
        } catch (Throwable t) {
            throw new CohortServiceException("Error getting cohort definition sql", t);
        }
    }
}
