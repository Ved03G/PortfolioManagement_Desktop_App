package org.example.work1;

import java.util.Date;

public class Transaction {
    private String amount;
    private String type;
    private Date date;
    private String fundtype;
    private String fundname;
    private double units;

    public Transaction(String amount, String type, Date date, String fundtype, String fundname, double units) {
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.fundtype = fundtype;
        this.fundname = fundname;
        this.units = units;

    }
    public String getAmount() {
        return amount;
    }
    public String getType() {
        return type;
    }
    public Date getDate() {
        return date;
    }
    public String getFundtype() {
        return fundtype;
    }
    public String getFundname() {
        return fundname;
    }
    public double getUnits() {
        return units;
    }
}
