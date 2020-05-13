# CQL on OMOP

[![PhEMA](./repo-badge.svg)](https://projectphema.org)
[![Build Status](https://travis-ci.org/PheMA/cql-on-omop.svg?branch=master)](https://travis-ci.org/PheMA/cql-on-omop)
[![Download](https://img.shields.io/badge/dynamic/json.svg?label=latest&query=name&url=https://bintray.com/api/v1/packages/phema/maven/phema-elm-to-ohdsi/versions/_latest) ](https://bintray.com/phema/maven/phema-elm-to-ohdsi/)
[![Coverage Status](https://coveralls.io/repos/github/PheMA/cql-on-omop/badge.svg?branch=master)](https://coveralls.io/github/PheMA/cql-on-omop?branch=master)
[![Javadocs](https://img.shields.io/badge/dynamic/json.svg?label=javadoc&color=yellow&query=name&url=https://bintray.com/api/v1/packages/phema/maven/phema-elm-to-ohdsi/versions/_latest) ](https://phema.github.io/elm-to-ohdsi-executer/)

This project reads a query file written in CQL and converts to OMOP JSON format and runs it against an OHDSI repository.

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

## Usage

### CLI
- Clone repository
- `mvn install`

Test files are located in the `resources/diabetes` directory.  The output will appear in the resources directory.

#### Configuration

The application is configured using the [`config.properties`](./config/config.properties) file. The following properties
are supported:

|**Name**|**Description**|
|---|---|
|OMOP_BASE_URL| The base URL of the OHDSI WebAPI (should end with `WebAPI/`)|
|INPUT_FILE_NAME| The path of the CQL library relative to `src/main/resources`|
|VS_FILE_NAME| Name of the valueset CSV file relative to `src/main/resources`|
|OUT_FILE_NAME| Output filename (will be created in `src/main/resources`) |
|SOURCE|The OHDSI data source name |
|VS_TAB| Name of the tab in the valueset spreadsheet (?) |
|PHENOTYPE_EXPRESSIONS| Name of the CQL/ELM expression to evaluate |

#### Execution

Once configuration is complete, run the translator as follows:

```
mvn clean compile exec:java
```

Individual properties can also be overwritten on the command line as follows:

```
mvn compile exec:java -Dexec.arguments="OMOP_BASE_URL=http://projectphema.org/WebAPI/ INPUT_FILE_NAME=autism/simple-dx-elm.xml"
```

You can optionally build an executable JAR with all dependencies packaged (NOTE: this is a large file):
```
mvn clean package
```

### Library

To use the translator library, add the PhEMA Maven repo to your `pom.xml`:

```xml
<repositories>
   <repository>
        <id>phema-bintray</id>
        <name>phema-bintray</name>
        <url>https://dl.bintray.com/phema/maven</url>
    </repository>
</repositories>
```

Then add the library as a dependency:

```xml
<dependency>
    <groupId>edu.phema</groupId>
    <artifactId>phema-elm-to-ohdsi</artifactId>
    <version>0.1.1</version>
</dependency>
```

Then use the library:

```java
import edu.phema.elm_to_omop.api.CqlToElmTranslator;

public class Main {
    public static void main(String[] args) {
        CqlToElmTranslator translator = new CqlToElmTranslator();

        String cql = "define test: 1 + 1";

        System.out.println(translator.cqlToElmJson(cql));
    }
}
```

See the [Javadoc](https://phema.github.io/elm-to-ohdsi-executer/) for the full API.

### Deployment

The maven artifact for this project is hosted on
[Bintray](https://bintray.com/beta/#/phema/maven/phema-elm-to-ohdsi?tab=overview). To publish a new version, you must be
a member of the [`phema` organization](https://bintray.com/phema) on Bintray. Once you have created an account and
joined the organization, read [this
article](https://blog.bintray.com/2015/09/17/publishing-your-maven-project-to-bintray/) for instructions on how to
publish. Basically,

1. Add the following to the `<servers>` tag in your Maven `settings.xml` file (probaby at `~/.m2/settings.xml`):

    ```xml
    <server>
        <id>bintray-phema-maven</id>
        <username>__BINTRAY_USERNAME__</username>
        <password>__BINTRAY_API_KEY__</password>
    </server>
    ```
   
   Find your API key in the menu on the left [here](https://bintray.com/profile/edit).
   
2. Run `mvn clean install deploy`

Alternatively, just push a tag to this repo:

```shell script
$ git tag 1.2.3
$ git push --tags
```

:bulb: Note that snapshot releases cannot be deployed to Bintray, so make sure the version in `pom.xml` does not end
with `SNAPSHOT` when you run the deploy.
   

## References
* We use the ELM POJOs made available from the [clinical\_quality\_language project](https://github.com/cqframework/clinical_quality_language/blob/master/Src/java/cql-to-elm/OVERVIEW.md).  
Additional information on the ELM schema can be found at - [https://cql.hl7.org/elm.html](https://cql.hl7.org/elm.html)
* The OMOP POJOs were created by hand, derived from review of the [ATLAS](https://github.com/OHDSI/Atlas) and [WebAPI](https://github.com/OHDSI/WebAPI/) source code.


## Processing Methodology

CQL allows for multiple expressions to be created, which contain a named identifier. This is very powerful for reuse,
but the challenge is that it requires a naming convention to identify the main "entry point" of the logic.  For things
like quality measures, pre-established terms (e.g., "Initial Patient Population", "Numerator", "Denominator") may make
this more readily identifiable.  For a phenotype, we may have multiple definitions in a single phenotype - e.g., "Type 2
Diabetes Cases", "Type 2 Diabetes Controls".

We will need the user to specify a list of expression identifiers to use for the phenotype. During translation, these
can then be turned into one or more actual cohorts that exist in OHDSI. If no identifier is provided, our best guess is
to find any public definition (`<def accessLevel="Public">`) that is not used within another expression.  This will give
us "top-level" expressions, which are most likely the phenotypes of interest.

For further implementation considerations, see the [design document](docs/DESIGN.md).
