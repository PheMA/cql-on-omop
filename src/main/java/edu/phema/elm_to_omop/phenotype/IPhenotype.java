package edu.phema.elm_to_omop.phenotype;

import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.Library;

import java.util.List;

public interface IPhenotype {
    public String getPhenotypeCql();

    public Library getPhenotypeElm();

    public List<ExpressionDef> getPhenotypeExpressions();
}
