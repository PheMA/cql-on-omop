package edu.phema.elm_to_omop.model_omop;

public class CollapseSettings {

    private String collapseType;
    private String eraPad;
    
    public CollapseSettings(String collapseType, String eraPad) {
        super();
        this.collapseType = collapseType;
        this.eraPad = eraPad;
    }
    
    public String getCollapseType() {
        return collapseType;
    }
    public String getEraPad() {
        return eraPad;
    }
    public void setCollapseType(String collapseType) {
        this.collapseType = collapseType;
    }
    public void setEraPad(String eraPad) {
        this.eraPad = eraPad;
    }
    
    public String getCollapseSettingJson()  {
        return "\"CollapseSettings\": { \"CollapseType\": " + collapseType +", \"EraPad\": " +eraPad +" }, ";
    }
    
}
