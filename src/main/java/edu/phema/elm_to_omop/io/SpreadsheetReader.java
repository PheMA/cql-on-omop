package edu.phema.elm_to_omop.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import edu.phema.elm_to_omop.helper.Terms;
import edu.phema.elm_to_omop.model_phema.PhemaCode;
import edu.phema.elm_to_omop.model_phema.PhemaValueSet;


/** 
 * 
 */
public class SpreadsheetReader {
    private static Logger logger = Logger.getLogger(SpreadsheetReader.class.getName());
    
    public ArrayList<PhemaValueSet> getSpreadsheetData(String patientFileLoc, String sheetName) throws FileNotFoundException, InvalidFormatException, IOException {
        ArrayList<PhemaValueSet>  valueSets = new ArrayList<PhemaValueSet> ();
        
        try  {
            Reader reader = Files.newBufferedReader(Paths.get(patientFileLoc));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            valueSets = readSheet(csvParser, sheetName);
        }  
        catch(FileNotFoundException e)  {
            logger.severe("Error in opening the spreadsheet");
            throw new FileNotFoundException(e.getMessage());
        }  
        catch(IOException ioe)  {
            logger.severe("Error in opening the spreadsheet");
            throw new IOException(ioe.getMessage());
        } 
        return valueSets;
    }
   
    
    private static ArrayList<PhemaValueSet> readSheet(CSVParser csvParser, String sheetName) throws IllegalArgumentException {
        ArrayList<PhemaCode> codeList = new ArrayList<PhemaCode>();
        ArrayList<PhemaValueSet> pvsList = new ArrayList<PhemaValueSet>();
        
        PhemaValueSet pvs = new PhemaValueSet();
        String currOid = null;
        int count = 0;
        PhemaCode code = new PhemaCode();
        for (CSVRecord csvRecord : csvParser) {

            String vsOid = csvRecord.get(Terms.COL_VS_OID);
            if(currOid==null)  {
                currOid = vsOid;
            }
            
            if(!currOid.equals(vsOid))  {
                pvs.setId(count);
                pvs.setName(code.getValueSetName());
                pvs.setCodes(codeList);
                pvsList.add(pvs);
                
                pvs = new PhemaValueSet();
                codeList = new ArrayList<PhemaCode>();
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
            
            codeList.add(code);   
        }
        
        pvs.setId(count);
        pvs.setName(code.getValueSetName());
        pvs.setCodes(codeList);
        pvsList.add(pvs);
        
        // take out headers
        pvsList.remove(0);
        
        return pvsList;
    }
    
}
