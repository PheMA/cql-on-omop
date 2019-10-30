package edu.phema.elm_to_omop.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class sets the configuration information.  It looks first for command line
 * arguments.  If not present, then pulls them from configuration file.
 * Config file is must be located in the config directory and named config.properties.
 */
public final class Config  {
    /**
     *  Logs messages to file.
     */
    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    /**
     * Configuration file name.
     */
    private String configFileName = "config.properties";
    /**
     * Path to the configuration file.
     */
    private String configFullPath = "";
    /**
     * Configuration properties.
     */
    private Properties configProps;

    private static final String PHENOTYPE_NAME_DELIMITER = "\\|";


    private static String omopBaseURL;

    private static String inputFileName;

    private static String vsFileName;

    private static String outFileName;

    private static String source;

    private static String tab;

    private static List<String> phenotypeExpressions;

    /**
     * Constructor finds the working directory and loads the configuration properties.
     */
    public Config()  {
        String workingDir = System.getProperty("user.dir");
        configFullPath = workingDir + File.separator + "config" + File.separator + configFileName;
        configProps = new java.util.Properties();

        try {
            InputStream is = new FileInputStream(configFullPath);
            configProps.load(is);
            setConfig();
        } catch (Exception eta) {
            eta.printStackTrace();
        }
    }

    /**
     * Constructor finds the working directory and loads the configuration properties.
     *
     * @param inArgs the arguments to be used for the configuration settings
     */
    public Config(final String[] inArgs)  {
        this();
        for (int i = 0; i < inArgs.length; i++) {
            LOGGER.info("Found argument:" + inArgs[i]);
            setArg(inArgs[i]);
        }
        runPropertyCheck();
    }

    /**
     * get the property value.
     * @param key name of the property
     * @return property value
     */
    public String getProperty(final String key)  {
       String value = this.configProps.getProperty(key);
       return value;
    }

    /**
     * sets the configuration properties.
     */
    public void setConfig()  {
        omopBaseURL = getProperty("OMOP_BASE_URL");
        inputFileName = getProperty("INPUT_FILE_NAME");
        vsFileName = getProperty("VS_FILE_NAME");
        outFileName = getProperty("OUT_FILE_NAME");
        source = getProperty("SOURCE");
        tab = getProperty("VS_TAB");
        phenotypeExpressions = parsePhenotypeExpressions(getProperty("PHENOTYPE_EXPRESSIONS"));

        runPropertyCheck();
    }

    public static String getOmopBaseUrl() {
        return omopBaseURL;
    }

    public static String getInputFileName() {
        return inputFileName;
    }

    public static String getVsFileName() {
        return vsFileName;
    }

    public static String getOutFileName() {
        return outFileName;
    }

    public static String getSource() {
        return source;
    }

    public static String getTab() {
        return tab;
    }

    public static List<String> getPhenotypeExpressions() {
      return phenotypeExpressions;
    }

    /**
     * Sets the values for the property.
     * @param inArg property value
     */
    private void setArg(final String inArg)  {
        String args = inArg;
        if (args.startsWith("-"))  {
            args = args.substring(1);
        }
        int pos = args.indexOf("=");
        String prop = args.substring(0, pos);
        String val = args.substring(pos + 1);

        if (prop.equalsIgnoreCase("OMOP_BASE_URL"))  {
            omopBaseURL = val;
        }
        if (prop.equalsIgnoreCase("INPUT_FILE_NAME"))  {
            inputFileName = val;
        }
        if (prop.equalsIgnoreCase("VS_FILE_NAME"))  {
            vsFileName = val;
        }
        if (prop.equalsIgnoreCase("OUT_FILE_NAME"))  {
            outFileName = val;
        }
        if (prop.equalsIgnoreCase("SOURCE"))  {
            source = val;
        }
        if (prop.equalsIgnoreCase("VS_TAB"))  {
            tab = val;
        }

        if (prop.equalsIgnoreCase("PHENOTYPE_EXPRESSIONS")) {
            phenotypeExpressions = parsePhenotypeExpressions(val);
        }
    }

    private List<String> parsePhenotypeExpressions(String phenotypes) {
        List<String> expressions = new ArrayList<String>();
        if (phenotypes != null) {
            expressions = Arrays.asList(phenotypes.split(PHENOTYPE_NAME_DELIMITER));
        }

        return expressions;
    }

    /**
     * Validate that all the properties were set correctly.
     */
    private void runPropertyCheck()  {
        if (omopBaseURL == null)  {
            LOGGER.severe("ERROR - missing parameter OMOP_BASE_URL");
        }
        if (inputFileName == null)  {
            LOGGER.severe("ERROR - missing parameter INPUT_FILE_NAME");
        }
        if (vsFileName == null)  {
            LOGGER.severe("ERROR - missing parameter VS_FILE_NAME");
        }
        if (outFileName == null)  {
            LOGGER.severe("ERROR - missing parameter OUT_FILE_NAME");
        }
        if (source == null)  {
            LOGGER.severe("ERROR - missing parameter SOURCE");
        }
        if (tab == null)  {
            LOGGER.severe("ERROR - missing parameter VS_TAB");
        }
    }

    public static String configString() {
        return String.format("OMOP_BASE_URL=%s, INPUT_FILE_NAME=%s, VS_FILE_NAME=%s, OUT_FILE_NAME=%s, SOURCE=%s, VS_TAB=%s", omopBaseURL, inputFileName, vsFileName, outFileName, source, tab);
    }
}
