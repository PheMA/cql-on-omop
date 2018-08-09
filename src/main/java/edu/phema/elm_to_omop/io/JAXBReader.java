package edu.phema.elm_to_omop.io;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import edu.phema.elm_to_omop.model_elm.Library;

public class JAXBReader {

    public JAXBReader() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Library readXml(File file) throws JAXBException {
        Library patient = new Library();
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Library.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            patient = (Library) jaxbUnmarshaller.unmarshal(file);

        } catch (JAXBException e) { 
            throw new JAXBException("Error while parsing the xml - " +e.getMessage());
        }
        return patient;

    }
}