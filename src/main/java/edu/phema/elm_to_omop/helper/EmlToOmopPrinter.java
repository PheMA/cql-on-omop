package edu.phema.elm_to_omop.helper;


/**
 * Prints out collections of information.
 */
public final class EmlToOmopPrinter {

    /**
     * Do not want others to use default constructor.
     */
    private EmlToOmopPrinter()  {
        throw new AssertionError("Instantiating utility class...");
    }
    
    /**
     * Prints the configuration  information.
     * @param config configuration information
     */
    public static void printConfig()  {
        System.out.println("omop base url = " + Config.getOmopBaseUrl());
    }
   
}
