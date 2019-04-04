package edu.phema.elm_to_omop.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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


    private static String omopBaseURL;
    
    private static String elmFileName;
    
    private static String vsFileName;
    
    private static String source;
    
    private static String tab;

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
        for (int i = 0; i < inArgs.length; i++) {
            LOGGER.info(inArgs[i]);
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
        elmFileName = getProperty("ELM_FILE_NAME");
        vsFileName = getProperty("VS_FILE_NAME");
        source = getProperty("SOURCE");
        tab = getProperty("VS_TAB");

        runPropertyCheck();
    }
    
    public static String getOmopBaseUrl() {
        return omopBaseURL;
    }
    
    public static String getElmFileName() {
        return elmFileName;
    }
    
    public static String getVsFileName() {
        return vsFileName;
    }
    
    public static String getSource() {
        return source;
    }
    
    public static String getTab() {
        return tab;
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
        int pos = inArg.indexOf("=");
        String prop = inArg.substring(0, pos);
        String val = inArg.substring(pos + 1);

        if (prop.equalsIgnoreCase("OMOP_BASE_URL"))  {
            omopBaseURL = val;
        }
        if (prop.equalsIgnoreCase("ELM_FILE_NAME"))  {
            elmFileName = val;
        }
        if (prop.equalsIgnoreCase("VS_FILE_NAME"))  {
            vsFileName = val;
        }
        if (prop.equalsIgnoreCase("SOURCE"))  {
            source = val;
        }
        if (prop.equalsIgnoreCase("VS_TAB"))  {
            tab = val;
        }
    }

    /**
     * Validate that all the properties were set correctly.
     */
    private void runPropertyCheck()  {
        if (omopBaseURL == null)  {
            LOGGER.severe("ERROR - missing parameter OMOP_BASE_URL");
        }
        if (elmFileName == null)  {
            LOGGER.severe("ERROR - missing parameter ELM_FILE_NAME");
        }
        if (vsFileName == null)  {
            LOGGER.severe("ERROR - missing parameter VS_FILE_NAME");
        }
        if (source == null)  {
            LOGGER.severe("ERROR - missing parameter SOURCE");
        }
        if (tab == null)  {
            LOGGER.severe("ERROR - missing parameter VS_TAB");
        }
    }

}
