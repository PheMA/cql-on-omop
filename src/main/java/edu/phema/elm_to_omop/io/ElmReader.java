package edu.phema.elm_to_omop.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import edu.phema.elm_to_omop.helper.Config;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.Library;

/**
 * Unmarshals an ELM file into a Library object.
 */
public class ElmReader {

    public static final String CQL_EXTENSION = ".cql";

    public ElmReader() {
        super();
    }

    public static Library readElm(String directory, String filename, Logger logger) throws IOException, JAXBException
    {
        File file = new File( directory + filename);

        Library elmContents = null;

        // If this is a CQL file, perform the conversion to ELM automatically.  Otherwise we can just
        // load the ELM XML.
        if (file.getPath().toLowerCase().endsWith(CQL_EXTENSION)) {
            ModelManager modelManager = new ModelManager();
            elmContents = CqlTranslator.fromFile(file, modelManager, new LibraryManager(modelManager)).toELM();
            String tmp = CqlTranslator.fromFile(file, modelManager, new LibraryManager(modelManager)).toXml();
            System.out.println(tmp);
        }
        else {
            elmContents = readXml(file);
        }

        return elmContents;      
    }
    
    public static Library readXml(File file) throws JAXBException 
    {
        Library elmContents = null;
        
        try {
            JAXBContext jaxbContext = CqlTranslator.getJaxbContext();

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<Library> library = jaxbUnmarshaller.unmarshal(new StreamSource(file), Library.class);
            elmContents = library.getValue();
        }
        catch (JAXBException e) { 
            throw new JAXBException("Error while parsing the xml - " + e);
        }
        return elmContents;
    }
}