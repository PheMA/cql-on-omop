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
public final class Config {
    /**
     * Logs messages to file.
     */
    private final Logger LOGGER = Logger.getLogger(Config.class.getName());

    /**
     * Configuration file name.
     */
    private static String configFileName = "config.properties";
    /**
     * Path to the configuration file.
     */
    private String configFullPath = "";
    /**
     * Configuration properties.
     */
    private Properties configProps;

    private final String PHENOTYPE_NAME_DELIMITER = "\\|";


    private String omopBaseURL;

    private String inputFileName;

    private String vsFileName;

    private String outFileName;

    private String source;

    private String tab;

    private List<String> phenotypeExpressions;

    /**
     * Empty constructor removes dependency
     * on `config.properties` file, which is
     * useful for library consumers
     */
    public Config() {
    }

    /**
     * Loads the configuration from a file at the given path
     */
    public Config(String configFilePath) {
        configProps = new java.util.Properties();

        try {
            InputStream is = new FileInputStream(configFilePath);
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
    public Config(final String[] inArgs) {
        // First initialize with default configuration
        this(Config.getDefaultConfigPath());

        // Override any arguments specified
        for (int i = 0; i < inArgs.length; i++) {
            LOGGER.info("Found argument:" + inArgs[i]);
            setArg(inArgs[i]);
        }
        runPropertyCheck();
    }

    /**
     * Gets the default path for the config file
     *
     * @return
     */
    public static String getDefaultConfigPath() {
        String workingDir = System.getProperty("user.dir");
        return workingDir + File.separator + "config" + File.separator + configFileName;
    }

    /**
     * get the property value.
     *
     * @param key name of the property
     * @return property value
     */
    public String getProperty(final String key) {
        String value = this.configProps.getProperty(key);
        return value;
    }

    /**
     * sets the configuration properties.
     */
    public void setConfig() {
        omopBaseURL = getProperty("OMOP_BASE_URL");
        inputFileName = getProperty("INPUT_FILE_NAME");
        vsFileName = getProperty("VS_FILE_NAME");
        outFileName = getProperty("OUT_FILE_NAME");
        source = getProperty("SOURCE");
        tab = getProperty("VS_TAB");
        phenotypeExpressions = parsePhenotypeExpressions(getProperty("PHENOTYPE_EXPRESSIONS"));

        runPropertyCheck();
    }

    public String getOmopBaseUrl() {
        return omopBaseURL;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getVsFileName() {
        return vsFileName;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public String getSource() {
        return source;
    }

    public String getTab() {
        return tab;
    }

    public List<String> getPhenotypeExpressions() {
        return phenotypeExpressions;
    }

    /**
     * Sets the values for the property.
     *
     * @param inArg property value
     */
    private void setArg(final String inArg) {
        String args = inArg;
        if (args.startsWith("-")) {
            args = args.substring(1);
        }
        int pos = args.indexOf("=");
        String prop = args.substring(0, pos);
        String val = args.substring(pos + 1);

        if (prop.equalsIgnoreCase("OMOP_BASE_URL")) {
            omopBaseURL = val;
        }
        if (prop.equalsIgnoreCase("INPUT_FILE_NAME")) {
            inputFileName = val;
        }
        if (prop.equalsIgnoreCase("VS_FILE_NAME")) {
            vsFileName = val;
        }
        if (prop.equalsIgnoreCase("OUT_FILE_NAME")) {
            outFileName = val;
        }
        if (prop.equalsIgnoreCase("SOURCE")) {
            source = val;
        }
        if (prop.equalsIgnoreCase("VS_TAB")) {
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
    private void runPropertyCheck() {
        if (omopBaseURL == null) {
            LOGGER.severe("ERROR - missing parameter OMOP_BASE_URL");
        }
        if (inputFileName == null) {
            LOGGER.severe("ERROR - missing parameter INPUT_FILE_NAME");
        }
        if (vsFileName == null) {
            LOGGER.severe("ERROR - missing parameter VS_FILE_NAME");
        }
        if (outFileName == null) {
            LOGGER.severe("ERROR - missing parameter OUT_FILE_NAME");
        }
        if (source == null) {
            LOGGER.severe("ERROR - missing parameter SOURCE");
        }
        if (tab == null) {
            LOGGER.severe("ERROR - missing parameter VS_TAB");
        }
    }

    public String configString() {
        return String.format("OMOP_BASE_URL=%s, INPUT_FILE_NAME=%s, VS_FILE_NAME=%s, OUT_FILE_NAME=%s, SOURCE=%s, VS_TAB=%s", omopBaseURL, inputFileName, vsFileName, outFileName, source, tab);
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getConfigFullPath() {
        return configFullPath;
    }

    public void setConfigFullPath(String configFullPath) {
        this.configFullPath = configFullPath;
    }

    public Properties getConfigProps() {
        return configProps;
    }

    public void setConfigProps(Properties configProps) {
        this.configProps = configProps;
    }

    public String getOmopBaseURL() {
        return omopBaseURL;
    }

    public void setOmopBaseURL(String omopBaseURL) {
        this.omopBaseURL = omopBaseURL;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public void setVsFileName(String vsFileName) {
        this.vsFileName = vsFileName;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public void setPhenotypeExpressions(List<String> phenotypeExpressions) {
        this.phenotypeExpressions = phenotypeExpressions;
    }
}
