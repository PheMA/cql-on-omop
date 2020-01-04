package edu.phema.elm_to_omop.io;

import edu.phema.elm_to_omop.helper.Terms;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaValueSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Creates a spreadsheet file on disk
 */
public class SpreadsheetWriter {

  /**
   * Creates a cache file containing value set information as a CSV file.  The cache file contains the OMOP Concept ID
   * so that long-running searches are not needed on subsequent runs.
   * @param cacheFile
   * @param valueSets
   * @throws IOException
   */
  public void writeValueSetsCache(String cacheFile, ArrayList<PhemaValueSet> valueSets) throws IOException {
    try (CSVPrinter printer = new CSVPrinter(new FileWriter(cacheFile), CSVFormat.DEFAULT)) {
      printer.printRecord(Terms.COL_VS_OID_NAME, Terms.COL_VS_NAME_NAME, Terms.COL_CODE_NAME, Terms.COL_DESC_NAME, Terms.COL_CS_NAME, Terms.COL_CS_VER_NAME, Terms.COL_CS_OID_NAME, Terms.COL_TTY_NAME, Terms.COL_OMOP_CONCEPT_ID_NAME);
      for (PhemaValueSet valueSet : valueSets) {
        String valuesetName = valueSet.getName();
        String valuesetOid = valueSet.getOid();
        for (PhemaCode code : valueSet.getCodes()) {
          printer.printRecord(valuesetOid, valuesetName, code.getCode(), code.getDescription(), code.getCodeSystem(), code.getCodeSystemVersion(), code.getCodeSystemOid(), code.getTty(), code.getOmopConceptId());
        }
      }
    }
  }
}
