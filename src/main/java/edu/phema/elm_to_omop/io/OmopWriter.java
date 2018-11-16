package edu.phema.elm_to_omop.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.phema.elm_to_omop.model_elm.Library;
import edu.phema.elm_to_omop.model_omop.CensorWindow;
import edu.phema.elm_to_omop.model_omop.CensoringCriteria;
import edu.phema.elm_to_omop.model_omop.CollapseSettings;
import edu.phema.elm_to_omop.model_omop.ConceptSets;
import edu.phema.elm_to_omop.model_omop.ConditionOccurrence;
import edu.phema.elm_to_omop.model_omop.CriteriaList;
import edu.phema.elm_to_omop.model_omop.ExpressionLimit;
import edu.phema.elm_to_omop.model_omop.InclusionRules;
import edu.phema.elm_to_omop.model_omop.ObservationWindow;
import edu.phema.elm_to_omop.model_omop.PrimaryCriteria;
import edu.phema.elm_to_omop.model_omop.PrimaryCriteriaLimit;
import edu.phema.elm_to_omop.model_omop.QualifiedLimit;

public class OmopWriter {

    private static Logger logger; 
    
    public static String writeOmopJson(Library elmContents, ConceptSets conceptSets, String directory, Logger inLogger)  throws IOException {
        logger = inLogger;
        
        String json = writeOmopJson(elmContents, conceptSets);
        logger.info(directory +"ohdsiJsonOutput.txt");
        
        try (FileWriter jsonFile = new FileWriter(directory +"ohdsiCohortDefinition.json")) {
            jsonFile.write(json);
        }
        return json;
    }
    
    
    private static String writeOmopJson(Library elmContents, ConceptSets conceptSets) throws IOException   {

        Map<String, Object> dtoMap = new HashMap<String, Object>();

        dtoMap.put("ConceptSets", conceptSets);
        
        PrimaryCriteria pc = getPrimaryCriteria(elmContents);
        dtoMap.put("PrimaryCriteria", pc);
        
        QualifiedLimit ql = getQualifiedLimit(elmContents);
        dtoMap.put("QualifiedLimit", ql);
        
        ExpressionLimit el = getExpressionLimit(elmContents);
        dtoMap.put("ExpressionLimit", el);
        
        InclusionRules ir = getInclusionRules(elmContents);
        dtoMap.put("InclusionRules", ir);
        
        CensoringCriteria cc = getCensoringCriteria(elmContents);
        dtoMap.put("CensoringCriteria", cc);
        
        CollapseSettings cols = getCollapseSettings(elmContents);
        dtoMap.put("CollapseSettings", cols);
        
        CensorWindow cw = getCensorWindow(elmContents);
        dtoMap.put("CensorWindow", cw);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        
        String dtoMapAsString = mapper.writeValueAsString(dtoMap);
        logger.info(dtoMapAsString);
        
        return dtoMapAsString;
    }
    
    private static PrimaryCriteria getPrimaryCriteria(Library elmContents)  {
        PrimaryCriteria pc = new PrimaryCriteria();
                
        CriteriaList cl = new CriteriaList();
        ConditionOccurrence co = new ConditionOccurrence();
        co.setCondesetId("XX - 0");
        cl.setCondOcc(co);
        pc.setCritList(cl);
        
        ObservationWindow obWin = new ObservationWindow();
        obWin.setPriorDays("XX - 0");
        obWin.setPostDays("XX - 0");
        pc.setObWin(obWin);

        PrimaryCriteriaLimit primCritLimit = new PrimaryCriteriaLimit();
        primCritLimit.setType("XX - First");
        pc.setPrimCritLimit(primCritLimit);
        
        return pc;
    }
    
    private static QualifiedLimit getQualifiedLimit(Library elmContents)  {
        QualifiedLimit ql = new QualifiedLimit();
        
        ql.setType("XX - First");
        
        return  ql;
    }
    
    private static ExpressionLimit getExpressionLimit(Library elmContents)  {
        ExpressionLimit el = new  ExpressionLimit();
        
        el.setType("XX - First");
        
        return el;
    }
    
    private static InclusionRules getInclusionRules(Library elmContents)  {
        InclusionRules ir = new InclusionRules();
        
        return ir;
    }
    
    private static CensoringCriteria getCensoringCriteria(Library elmContents)  {
        CensoringCriteria cc = new CensoringCriteria();
        
        return cc;
    }
    
    private static CollapseSettings getCollapseSettings(Library elmContents)  {
        CollapseSettings cs = new CollapseSettings();
        
        cs.setCollapseType("XX - ERA");
        cs.setEraPad("XX _ 0");
        return cs;
    }
    
    private static CensorWindow getCensorWindow(Library elmContents)  {
        CensorWindow cw = new CensorWindow();
        
        return cw;
    }
    
}
