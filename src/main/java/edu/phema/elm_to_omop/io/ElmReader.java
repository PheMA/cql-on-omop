package edu.phema.elm_to_omop.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import edu.phema.elm_to_omop.helper.Config;
import edu.phema.elm_to_omop.model_elm.Library;

/**
 * Unmarshals an ELM file into a Library object.
 */
public class ElmReader {

    public ElmReader() {
        super();
    }

    public static Library readElm(String directory, Logger logger) throws FileNotFoundException, IOException, JAXBException  
    {
        File file = new File( directory + Config.getElmFileName());
        Library elmContents = readXml(file);
      
        return elmContents;      
    }
    
    public static Library readXml(File file) throws JAXBException 
    {
        Library elmContents = new Library();
        
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Library.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            elmContents = (Library) jaxbUnmarshaller.unmarshal(file);
        } 
        catch (JAXBException e) { 
            throw new JAXBException("Error while parsing the xml - " +e.getMessage());
        }
        return elmContents;
    }
}