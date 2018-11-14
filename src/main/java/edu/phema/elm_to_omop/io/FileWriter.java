package edu.phema.elm_to_omop.io;

import java.io.IOException;
import java.io.PrintWriter;

public class FileWriter {

    public static void write(String directory, String content)  throws IOException {

        try (PrintWriter out = new PrintWriter(directory)) {
            out.println(content);
        }
    }
    
}
