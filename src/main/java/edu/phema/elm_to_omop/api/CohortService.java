package edu.phema.elm_to_omop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.phema.elm_to_omop.api.exception.CohortServiceException;
import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.io.FhirReader;
import edu.phema.elm_to_omop.phenotype.BundlePhenotype;
import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryService;
import edu.phema.elm_to_omop.vocabulary.ConceptCodeCsvFileValuesetService;
import edu.phema.elm_to_omop.vocabulary.IValuesetService;
import edu.phema.elm_to_omop.vocabulary.SpreadsheetValuesetService;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.hl7.fhir.r4.model.Bundle;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;
import org.ohdsi.webapi.service.CohortDefinitionService.GenerateSqlResult;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

  private String errorMsg = "Error initializing concept sets";

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
    } else if (config.isUsingBundle()) {
      // Delay creating the service until we have a bundle
      valuesetService = null;
      return;
    } else {
      valuesetService = new ConceptCodeCsvFileValuesetService(omopService, config.getVsFileName(), true);
    }

    try {
      conceptSets = valuesetService.getConceptSets();
    } catch (Exception e) {
      throw new CohortServiceException(errorMsg, e);
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
      throw new CohortServiceException(errorMsg, e);
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
      throw new CohortServiceException(errorMsg, e);
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
    logger.info("Creating cohort definition");

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
   * Create a cohort definition from a FHIR bundle and a named CQL statement.
   *
   * @param bundle        The FHIR bundle
   * @param statementName The named CQL statement
   * @return The cohort definition that was created
   * @throws CohortServiceException
   */
  public CohortDefinitionDTO createCohortDefinition(Bundle bundle, String statementName) throws CohortServiceException {

    try {
      ElmToOmopTranslator translator = new ElmToOmopTranslator(omopService);

      CohortDefinitionDTO cohortDefinitionDTO = translator.bundleToOmopCohortDefinition(bundle, statementName);

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
   * Generate the cohort for cohort definition specified by the given CQL string
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
   * Generate the cohort for cohort definition specified by the given FHIR bundle
   * and statement name.
   *
   * @param bundle        The FHIR Bundle
   * @param statementName The named CQL statement that defines the cohort
   * @return The created OMOP job
   * @throws CohortServiceException
   */
  public JobExecutionResource queueCohortGeneration(Bundle bundle, String statementName) throws CohortServiceException {
    try {
      BundlePhenotype phenotype = FhirReader.convertBundle(bundle, omopService);
      phenotype.setPhenotypeExpressionNames(Arrays.asList(statementName));

      this.valuesetService = phenotype.getValuesetService();
      return queueCohortGeneration(phenotype.getPhenotypeCql(), statementName);
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
   * Creates a cohort definition from a FHIR Bundle, queues it up for generation, and returns info about it.
   *
   * @param bundle        The FHIR Bundle
   * @param statementName The CQL statement name that defines the cohort
   * @return A list of cohort definition info objects
   * @throws CohortServiceException
   */
  public List<CohortGenerationInfo> getCohortDefinitionInfo(Bundle bundle, String statementName) throws CohortServiceException {
    try {
      BundlePhenotype phenotype = FhirReader.convertBundle(bundle, omopService);
      this.valuesetService = phenotype.getValuesetService();
      return getCohortDefinitionInfo(phenotype.getPhenotypeCql(), statementName);
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

      // Initialize retry settings
      int maxRetries;
      String maxRetriesProp = System.getProperty("edu.phema.maxRetries");

      if (maxRetriesProp == null) {
        maxRetries = 10;
      } else {
        maxRetries = Integer.parseInt(maxRetriesProp);
      }

      int maxDelay;
      String maxDelayProp = System.getProperty("edu.phema.maxDelaySeconds");

      if (maxRetriesProp == null) {
        maxDelay = 90;
      } else {
        maxDelay = Integer.parseInt(maxDelayProp);
      }

      logger.info("Getting Cohort Report with maxRetries=" + maxRetries + " and maxDelay=" + maxDelay);

      // Retry while the cohort is generating
      RetryPolicy retryPolicy = new RetryPolicy();
      retryPolicy.withMaxRetries(maxRetries);

      retryPolicy.handleResultIf(info -> {
        Optional<CohortGenerationInfo> maybeGenInfo = ((List<CohortGenerationInfo>) info).stream().filter(cgi -> cgi.getId().getCohortDefinitionId().equals(id)).findFirst();

        if (!maybeGenInfo.isPresent()) {
          logger.info("Cohort generation is not running for: " + id);

          return false;
        }

        CohortGenerationInfo genInfo = maybeGenInfo.get();

        logger.info("Waiting for cohort generation to complete, got status: " + genInfo.getStatus());

        return genInfo.getStatus() != GenerationStatus.COMPLETE;
      });
      retryPolicy.withBackoff(1, maxDelay, ChronoUnit.SECONDS);
      Failsafe.with(retryPolicy).get(() -> omopService.getCohortDefinitionInfo(id));

      // Retry until we actually get an inclusionRules result back
      // Seems like it takes a second for the result to be persisted in the database
      retryPolicy.handleResultIf(inclReport -> {
        int size = ((InclusionRuleReport) inclReport).inclusionRuleStats.size();

        logger.info("Waiting until we get stats for inclusion rule. Currently have: " + size);

        return size == 0;
      });
      Failsafe.with(retryPolicy).get(() -> omopService.getCohortDefinitionReport(id));

      InclusionRuleReport report = omopService.getCohortDefinitionReport(id);

      return report;
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
   * Creates a cohort definition from CQL in a FHIR Bundle, queues it up for generation, waits
   * until generation is complete, and then returns the cohort definition report
   *
   * @param bundle        The FHIR Bundle resource
   * @param statementName The CQL statement name that defines the cohort
   * @return The cohort report
   * @throws CohortServiceException
   */
  public InclusionRuleReport getCohortDefinitionReport(Bundle bundle, String statementName) throws CohortServiceException {
    logger.info("Creating cohort report");

    try {
      CohortDefinitionDTO cohortDefinition = createCohortDefinition(bundle, statementName);

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
