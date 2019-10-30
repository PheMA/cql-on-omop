package edu.phema.elm_to_omop.model.omop;

import java.math.BigDecimal;

public class WindowBoundary {

    private String coeff;
    private BigDecimal days;

    public WindowBoundary(String coeff) {
        super();
        this.coeff = coeff;
    }

  public WindowBoundary(String coeff, BigDecimal days) {
    this.coeff = coeff;
    this.days = days;
  }

    public String getCoeff() {
        return coeff;
    }
    public void setCoeff(String coeff) {
        this.coeff = coeff;
    }

  public BigDecimal getDays() {
    return days;
  }

  public void setDays(BigDecimal days) {
    this.days = days;
  }
}
