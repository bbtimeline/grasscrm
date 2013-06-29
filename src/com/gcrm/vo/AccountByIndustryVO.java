package com.gcrm.vo;

import java.io.Serializable;

public class AccountByIndustryVO implements Serializable {

    private static final long serialVersionUID = 8250950813769457555L;

    private String industryLabel;
    private int number;

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number
     *            the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the industryLabel
     */
    public String getIndustryLabel() {
        return industryLabel;
    }

    /**
     * @param industryLabel
     *            the industryLabel to set
     */
    public void setIndustryLabel(String industryLabel) {
        this.industryLabel = industryLabel;
    }

}
