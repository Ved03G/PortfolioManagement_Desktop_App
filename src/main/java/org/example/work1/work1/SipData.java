package org.example.work1.work1;

import java.time.LocalDate;
import java.time.Period;

public class SipData {
    private final String sipName;
    private final String frequency;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double investedAmount;
    private final double totalUnits;
    private final double currentAmount;
    private final double returns;

    public SipData(String sipName, String frequency, LocalDate startDate, LocalDate endDate, double investedAmount, double totalUnits, double currentAmount, double returns) {
        this.sipName = sipName;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.investedAmount = investedAmount;
        this.totalUnits = totalUnits;
        this.currentAmount = currentAmount;
        this.returns = returns;
    }

    public String getSipName() {
        return sipName;
    }

    public String getFrequency() {
        return frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getInvestedAmount() {
        return investedAmount;
    }

    public double getTotalUnits() {
        return totalUnits;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public double getReturns() {
        return returns;
    }

    public String getPeriod() {
        Period period = Period.between(startDate, endDate);
        return String.format("%d years, %d months, %d days", period.getYears(), period.getMonths(), period.getDays());
    }
}
