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
- mvn install

Test files are located in the resources/diabetes directory.  The output will appears in the resources directory.

##Reference Files

ELM POJO created from xsd files - https://cql.hl7.org/elm.html


