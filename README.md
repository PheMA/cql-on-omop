# elm-to-ohdsi-executer

This project reads a query file written in ELM and converts to OMOP JSON format and runs it against an OHDSI repository.

This project is under development.  Currently it converts a simple elm file into json using OHDSI format.  Next steps will be to 
  -  run the file against an OMOP repository using the WebAPI
  -  convert more complex files
  -  improve configuration to run files in any location


## Usage:
- Clone repository
- mvn install

Test files are located in the resources/diabetes directory.  The output will appears in the resources directory.


