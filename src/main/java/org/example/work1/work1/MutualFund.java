package org.example.work1.work1;
public class MutualFund {

    private String schemeCode;
    private String schemeName;

    public MutualFund(String schemeCode, String schemeName) {
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
    }

    public String getSchemeCode() {
        return schemeCode;
    }

    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }
}

