package edu.phema.elm_to_omop.helper;

/**
 * Make adjustments to the straight JSON to fit the syntax expected by the WebAPI
 */
public class WebApiFormatter {

    public static String getWebApiJson(String oldJson) {
        String newJson = oldJson;

        newJson = newJson.replaceAll("\"", "\\\"");

        return newJson;
    }  

}
