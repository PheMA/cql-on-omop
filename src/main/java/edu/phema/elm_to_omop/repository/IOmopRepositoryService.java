package edu.phema.elm_to_omop.repository;

import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;
import org.ohdsi.webapi.service.CohortDefinitionService.GenerateSqlResult;

import java.util.List;

/**
 * Represents and instance of an OMOP server
 */
public interface IOmopRepositoryService {
    /**
     * Gets concept metadata for a given concept ID
     *
     * @param id The concept ID
     * @return The concept metadata
     * @throws OmopRepositoryException
     */
    public Concept getConceptMetadata(String id) throws OmopRepositoryException;

    /**
     * Submits a query to the WebAPIs /vocabulary/search endpoint
     * that specifies a query and a vocabularyId
     *
     * @param query        The code, or partial code from the vocabulary
     * @param vocabularyId The ID of the vocabulary (e.g. "CPT4)
     * @return
     * @throws OmopRepositoryException
     */
    public List<Concept> vocabularySearch(String query, String vocabularyId) throws OmopRepositoryException;

    /**
     * Create a new cohort definition in the OMOP database. This only creates
     * the definition, and does not actually generate the cohort.
     *
     * @param cohortDefintion The cohort definition to create
     * @return The created cohort definition
     * @throws OmopRepositoryException
     */
    public CohortDefinitionDTO createCohortDefinition(CohortDefinitionDTO cohortDefintion) throws OmopRepositoryException;

    /**
     * Gets the cohort definition for a given id
     *
     * @param id The cohort definition id
     * @return The cohort definition
     * @throws OmopRepositoryException
     */
    public CohortDefinitionDTO getCohortDefinition(Integer id) throws OmopRepositoryException;

    /**
     * Queue up a specific cohort definition for generation. This will return
     * the created cohort definition job.
     *
     * @param id The ID of the cohort definition to generate
     * @return The cohort generation job
     * @throws OmopRepositoryException
     */
    public JobExecutionResource queueCohortGeneration(Integer id) throws OmopRepositoryException;

    /**
     * Get information about the cohort definition, such as the generation status.
     *
     * @param id The cohort definition id
     * @return A list of cohort definition info objects
     * @throws OmopRepositoryException
     */
    public List<CohortGenerationInfo> getCohortDefinitionInfo(Integer id) throws OmopRepositoryException;

    /**
     * Get a report for a given cohort definition, including statistics for
     * each inclusion rule.
     *
     * @param id The cohort definition id
     * @return The report object
     * @throws OmopRepositoryException
     */
    public InclusionRuleReport getCohortDefinitionReport(Integer id) throws OmopRepositoryException;

    /**
     * Get the CQL for a given cohort definition. One of the following target dialects may optionally
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
     * @param id The cohort definition id
     * @return The generation result obejct
     * @throws OmopRepositoryException
     */
    public GenerateSqlResult getCohortDefinitionSql(Integer id, String targetDialect) throws OmopRepositoryException;
}
