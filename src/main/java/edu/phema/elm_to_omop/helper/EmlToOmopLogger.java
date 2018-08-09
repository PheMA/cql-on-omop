package edu.phema.elm_to_omop.helper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Sets up the logging mechanism.
 */
public final class EmlToOmopLogger {
    /**
     * Gets the messages.
     */
    private static FileHandler fh = null;

    /**
     * Do not want others to use default constructor.
     */
    private EmlToOmopLogger()  {
        throw new AssertionError("Instantiating utility class...");
    }
    
    /**
     * Sets the paramets for the logging.
     * @param logName path and file name of the log file
     */
    public static void setupLogging(final String logName)  {
        try {
            fh = new FileHandler(logName, false);
        } catch (IOException e) {
             e.printStackTrace();
        }
        Logger log = Logger.getLogger("");
        fh.setFormatter(new SimpleFormatter());
        log.addHandler(fh);
        log.setLevel(Level.CONFIG);
    }
}
