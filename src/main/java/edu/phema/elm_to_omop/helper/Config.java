package edu.phema.elm_to_omop.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class sets the configuration information.  It looks first for command line
 * arguments.  If not present, then pulls them from configuration file.
 * Config file is must be located in the config directory and named configuration.properties.
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


    private static String umlsUser;

    private static String umlsPass;

    private static String cts2URL;
    
    private static String omopBaseURL;
    
    private static String vsOid;
    
    private static String vsName;

    private static String vsVer;

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
        umlsUser = getProperty("UMLS_USER");
        umlsPass = getProperty("UMLS_PASS");
        cts2URL = getProperty("CTS2_URL");
        omopBaseURL = getProperty("OMOP_BASE_URL");
        vsOid = getProperty("VS_OID");
        vsName = getProperty("VS_NAME");
        vsVer = getProperty("VS_VER");

        runPropertyCheck();
    }
    
    public static String getUmlsUser() {
        return umlsUser;
    }

    public static String getUmlsPass() {
        return umlsPass;
    }

    public static String getVsOid() {
        return vsOid;
    }
    
    public static String getVsName() {
        return vsName;
    }

    public static String getVsVer() {
        return vsVer;
    }
 
    public static String getCts2Url() {
        return cts2URL;
    }
    
    public static String getOmopBaseUrl() {
        return omopBaseURL;
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

        if (prop.equalsIgnoreCase("UMLS_USER"))  {
            umlsUser = val;
        } else if (prop.equalsIgnoreCase("UMLS_PASS"))  {
            umlsPass = val;
        } else if (prop.equalsIgnoreCase("VS_OID"))  {
            vsOid = val;
        } else if (prop.equalsIgnoreCase("VS_NAME"))  {
            vsName = val;
        } else if (prop.equalsIgnoreCase("VS_VER"))  {
            vsVer = val;
        } else if (prop.equalsIgnoreCase("CTS2_URL"))  {
            cts2URL = val;
        } else if (prop.equalsIgnoreCase("OMOP_BASE_URL"))  {
            omopBaseURL = val;
        }

    }

    /**
     * Validate that all the properties were set correctly.
     */
    private void runPropertyCheck()  {
        if (umlsUser == null)  {
            LOGGER.severe("ERROR - missing parameter UMLS_USER");
        }
        if (umlsPass == null)  {
            LOGGER.severe("ERROR - missing parameter UMLS_PASS");
        }
        if (vsOid == null)  {
            LOGGER.severe("ERROR - missing parameter VS_OID");
        }
        if (vsName == null)  {
            LOGGER.severe("ERROR - missing parameter VS_NAME");
        }
        if (cts2URL == null)  {
            LOGGER.severe("ERROR - missing parameter CTS2_URL");
        }
        if (omopBaseURL == null)  {
            LOGGER.severe("ERROR - missing parameter OMOP_BASE_URL");
        }
       
    }

}
