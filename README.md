# elm-to-ohdsi-executer

This project reads a query file written in ELM and converts to OMOP JSON format and runs it against an OHDSI repository.

1. ELM file is transformed into OHDSI JSON format
2. Uses OHDSI WebAPI to create the definition
3. Uses OHDSI WebAPI to generate the cohort
4. Uses OHDSI WebAPI to poll the status of the execution
5. Uses OHDSI WebAPI to retrieve the results

Currently the code runs only a very simple example.  Project plans will increase the complexity of the phenotypes, 
    Temporal operators
    Logical operators
    Attribute constraints
    Error checking
    Logging

## Usage:
- Clone repository
- `mvn install`

Test files are located in the `resources/diabetes` directory.  The output will appear in the resources directory.

## References
* We use the ELM POJOs made available from the [clinical\_quality\_language project](https://github.com/cqframework/clinical_quality_language/blob/master/Src/java/cql-to-elm/OVERVIEW.md).  
Additional information on the ELM schema can be found at - [https://cql.hl7.org/elm.html](https://cql.hl7.org/elm.html)
* The OMOP POJOs were created by hand, derived from review of the [ATLAS](https://github.com/OHDSI/Atlas) and [WebAPI](https://github.com/OHDSI/WebAPI/) source code.


## Processing Methodology

CQL allows for multiple expressions to be created, which contain a named identifier.  This is very powerful for reuse, but the challenge is that it requires a naming convention to identify the main "entry point" of the logic.  For things like quality measures, pre-established terms (e.g., "Initial Patient Population", "Numerator", "Denominator") may make this more readily identifiable.  For a phenotype, we may have multiple definitions in a single phenotype - e.g., "Type 2 Diabetes Cases", "Type 2 Diabetes Controls".

We will need the user to specify a list of expression identifiers to use for the phenotype.  During translation, these can then be turned into one or more actual cohorts that exist in OHDSI.    If no identifier is provided, our best guess is to find any public definition (`<def accessLevel="Public">`) that is not used within another expression.  This will give us "top-level" expressions, which are most likely the phenotypes of interest.


## OHDSI JSON structure
```
{
    name:
    description:
    expressionType:
    expression:
    {  
        ConceptSets: 
        [ 
            { 
                id:  
                name: 
                expression: 
                {  
                    items: 
                    [
                        { 
                            concept: 
                            {   
                                CONCEPT_ID:   
                                CONCEPT_NAME:
                                STANDARD_CONCEPT: 
                                STANDARD_CONCEPT_CAPTION:
                                INVALID_REASON: 
                                INVALID_REASON_CAPTION: 
                                CONCEPT_CODE: 
                                DOMAIN_ID: 
                                VOCABULARY_ID:   
                                CONCEPT_CLASS_ID: 
                            }   // concept         
                        }   // items
                    ]   // items       
                }   // explression    
            }   // ConceptSets
        ],  // ConceptSets
        PrimaryCriteria: 
        {     
            CriteriaList: 
            [       
                {         
                    VisitOccurrence: {}       
                }     
            ],       
            ObservationWindow: 
            {       
                PriorDays: ,       
                PostDays:          
            },     
            PrimaryCriteriaLimit: 
            {       
                Type: 
            }   
        },       // primaryCriteria
        QualifiedLimit: 
        {     
            Type: 
        },   
        ExpressionLimit: 
        {     
            Type: 
        },   
        InclusionRules: 
        [     
            {       
                name:        
                expression: 
                {         
                    Type: ALL,         
                    CriteriaList: 
                    [           
                        { 
                            Criteria: 
                            {   
                                ConditionOccurrence: 
                                {     
                                    CodesetId:   
                                } 
                            },   // 
                            StartWindow: 
                            {   
                                Start: 
                                {     
                                    Coeff:    
                                },   
                                End: {     
                                    Coeff:  
                                } 
                            },   // startWindow
                            Occurrence: 
                            {   
                              Type:    
                              Count:  
                            }           
                        }         // CriteriaList
                    ],      // CriteriaList   
                    DemographicCriteriaList: [],         
                    Groups: []       
                }      // expression
            }     // InclusionRules
        ],     // InclusionRules
        CensoringCriteria: [],   
        CollapseSettings: 
        {     
            CollapseType:      
            EraPad:    
        },   
        CensorWindow: {} 
    }
}
```
