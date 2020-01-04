package edu.phema.elm_to_omop.helper;

/**
 * Terms which might be used in more than one location
 * or have the possibility of changing.
 */
public class Terms {

    public static int COL_VS_OID = 0;
    public static int COL_VS_NAME = 1;
    public static int COL_CODE = 2;
    public static int COL_DESC = 3;
    public static int COL_CS = 4;
    public static int COL_CS_VER = 5;
    public static int COL_CS_OID = 6;
    public static int COL_TTY = 7;
    public static int COL_OMOP_CONCEPT_ID = 8;

    public static String COL_VS_OID_NAME = "value_set_oid";
    public static String COL_VS_NAME_NAME = "value_set_name";
    public static String COL_CODE_NAME = "code";
    public static String COL_DESC_NAME = "description";
    public static String COL_CS_NAME = "code_system";
    public static String COL_CS_VER_NAME = "code_system_version";
    public static String COL_CS_OID_NAME = "code_system_oid";
    public static String COL_TTY_NAME = "tty";
    public static String COL_OMOP_CONCEPT_ID_NAME = "omop_concept_id";

    public static String CONCEPT_ID = "CONCEPT_ID";
    public static String CONCEPT_NAME = "CONCEPT_NAME";
    public static String STANDARD_CONCEPT = "STANDARD_CONCEPT";
    public static String STANDARD_CONCEPT_CAPTION = "STANDARD_CONCEPT_CAPTION";
    public static String INVALID_REASON = "INVALID_REASON";
    public static String INVALID_REASON_CAPTION = "INVALID_REASON_CAPTION";
    public static String CONCEPT_CODE = "CONCEPT_CODE";
    public static String DOMAIN_ID = "DOMAIN_ID";
    public static String VOCABULARY_ID = "VOCABULARY_ID";
    public static String CONCEPT_CLASS_ID = "CONCEPT_CLASS_ID";


    public static final String VS_CACHE_FILE_SUFFIX = ".cache";
}
