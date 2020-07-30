package edu.phema.elm_to_omop.io;

import edu.phema.elm_to_omop.helper.Terms;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaValueSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;


/**
 *
 */
public class SpreadsheetReader {
    private static Logger logger = Logger.getLogger(SpreadsheetReader.class.getName());

    public ArrayList<PhemaValueSet> getSpreadsheetData(String filePath, String sheetName) throws IOException {
        ArrayList<PhemaValueSet> valueSets = new ArrayList<>();

        InputStream in = null;
        Reader reader = null;
        try {
            // First, do we have a cached version of the file available?
            in = getInputStream(filePath + Terms.VS_CACHE_FILE_SUFFIX);
            // If not, get the regular version
            if (in == null) {
                in = getInputStream(filePath);
            }

            reader = new BufferedReader(new InputStreamReader(in));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
            valueSets = readSheet(csvParser);
        } catch (FileNotFoundException e) {
            logger.severe("Error in opening the spreadsheet");
            throw new FileNotFoundException(e.getMessage());
        } catch (IOException ioe) {
            logger.severe("Error in opening the spreadsheet");
            throw new IOException(ioe.getMessage());
        }
        finally {
            if(reader!=null) {
                reader.close();
            }
        }
        return valueSets;
    }

    private InputStream getInputStream(String filePath) {
      try {
        return new FileInputStream(filePath);
      } catch (FileNotFoundException e) {
        return null;
      }
    }


    private static ArrayList<PhemaValueSet> readSheet(CSVParser csvParser) throws IllegalArgumentException {
        ArrayList<PhemaCode> codeList = new ArrayList<>();
        ArrayList<PhemaValueSet> pvsList = new ArrayList<>();

        PhemaValueSet pvs = new PhemaValueSet();
        String currOid = null;
        int count = 0;
        PhemaCode code = new PhemaCode();
        boolean isCachedVersion = (csvParser.getHeaderMap().containsKey(Terms.COL_OMOP_CONCEPT_ID_NAME));
        for (CSVRecord csvRecord : csvParser) {
            String vsOid = csvRecord.get(Terms.COL_VS_OID);
            if (currOid == null) {
                currOid = vsOid;
            }

            if (!currOid.equals(vsOid)) {
                pvs.setId(count);
                pvs.setOid(currOid);
                pvs.setName(code.getValueSetName());
                pvs.setCodes(codeList);
                pvsList.add(pvs);

                pvs = new PhemaValueSet();
                codeList = new ArrayList<>();
                currOid = vsOid;
                count++;
            }

            code = new PhemaCode();
            code.setValueSetOid(vsOid);
            code.setValueSetName(csvRecord.get(Terms.COL_VS_NAME));
            code.setCode(csvRecord.get(Terms.COL_CODE));
            code.setDescription(csvRecord.get(Terms.COL_DESC));
            code.setCodeSystem(csvRecord.get(Terms.COL_CS));
            code.setCodeSystemVersion(csvRecord.get(Terms.COL_CS_VER));
            code.setCodeSystemOid(csvRecord.get(Terms.COL_CS_OID));
            code.setTty(csvRecord.get(Terms.COL_TTY));

            if (isCachedVersion) {
              code.setOmopConceptId(csvRecord.get(Terms.COL_OMOP_CONCEPT_ID));
            }

            codeList.add(code);
        }

        pvs.setId(count);
        pvs.setOid(currOid);
        pvs.setName(code.getValueSetName());
        pvs.setCodes(codeList);
        pvsList.add(pvs);

        return pvsList;
    }

}
