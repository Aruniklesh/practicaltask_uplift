package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Member model representing a member portfolio record deserialized from data/members.json.
 * Uses java.math.BigDecimal for all numeric fields to ensure exact decimal precision.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {
    private String memberId;
    private String name;
    private String frequency;
    private BigDecimal contributionAmount;
    private BigDecimal unitsHeld;
    private BigDecimal unitPrice;
    private BigDecimal nextOrderUnits;

    public Member() {
    }

    public Member(String memberId, String name, String frequency, 
                  BigDecimal contributionAmount, BigDecimal unitsHeld, 
                  BigDecimal unitPrice, BigDecimal nextOrderUnits) {
        this.memberId = memberId;
        this.name = name;
        this.frequency = frequency;
        this.contributionAmount = contributionAmount;
        this.unitsHeld = unitsHeld;
        this.unitPrice = unitPrice;
        this.nextOrderUnits = nextOrderUnits;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public BigDecimal getContributionAmount() {
        return contributionAmount != null ? contributionAmount : BigDecimal.ZERO;
    }

    public void setContributionAmount(BigDecimal contributionAmount) {
        this.contributionAmount = contributionAmount;
    }

    public BigDecimal getUnitsHeld() {
        return unitsHeld != null ? unitsHeld : BigDecimal.ZERO;
    }

    public void setUnitsHeld(BigDecimal unitsHeld) {
        this.unitsHeld = unitsHeld;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getNextOrderUnits() {
        return nextOrderUnits != null ? nextOrderUnits : BigDecimal.ZERO;
    }

    public void setNextOrderUnits(BigDecimal nextOrderUnits) {
        this.nextOrderUnits = nextOrderUnits;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId='" + memberId + '\'' +
                ", name='" + name + '\'' +
                ", frequency='" + frequency + '\'' +
                ", contributionAmount=" + contributionAmount +
                ", unitsHeld=" + unitsHeld +
                ", unitPrice=" + unitPrice +
                ", nextOrderUnits=" + nextOrderUnits +
                '}';
    }
}
