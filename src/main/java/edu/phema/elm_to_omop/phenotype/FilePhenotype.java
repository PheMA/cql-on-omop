package edu.phema.elm_to_omop.phenotype;

import edu.phema.elm_to_omop.io.ElmReader;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FilePhenotype implements IPhenotype {
    private String phenotypeCql;
    private Library phenotypeElm;
    private List<String> phenotypeExpressionNames;

    public FilePhenotype(String phenotypeFilePath, List<String> phenotypeExpressionNames) throws PhenotypeException {
        Logger logger = Logger.getLogger(getClass().getName());

        this.phenotypeExpressionNames = phenotypeExpressionNames;

        if (phenotypeExpressionNames == null || phenotypeExpressionNames.size() == 0) {
            throw new PhenotypeException("No phenotype expression names specified");
        }

        if (phenotypeFilePath.endsWith(".cql")) {

            File file = new File(phenotypeFilePath);
            StringBuilder builder = new StringBuilder();

            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine()).append(System.lineSeparator());
                }
            } catch (Exception e) {
                throw new PhenotypeException("Error reading phenotype file", e);
            }

            phenotypeCql = builder.toString();
            phenotypeElm = ElmReader.readCqlString(phenotypeCql);
        } else {
            phenotypeCql = null;

            try {
                phenotypeElm = ElmReader.readElm("", phenotypeFilePath, logger);
            } catch (Exception e) {
                throw new PhenotypeException("Error reading phenotype ELM", e);
            }
        }
    }

    @Override
    public List<ExpressionDef> getPhenotypeExpressions() {
        return phenotypeElm.getStatements().getDef().stream()
            .filter(x -> phenotypeExpressionNames.contains(x.getName()))
            .collect(Collectors.toList());
    }

    @Override
    public String getPhenotypeCql() {
        return phenotypeCql;
    }

    public void setPhenotypeCql(String phenotypeCql) {
        this.phenotypeCql = phenotypeCql;
    }

    @Override
    public Library getPhenotypeElm() {
        return phenotypeElm;
    }

    public void setPhenotypeElm(Library phenotypeElm) {
        this.phenotypeElm = phenotypeElm;
    }

    public List<String> getPhenotypeExpressionNames() {
        return phenotypeExpressionNames;
    }

    public void setPhenotypeExpressionNames(List<String> phenotypeExpressionNames) {
        this.phenotypeExpressionNames = phenotypeExpressionNames;
    }
}
