package com.backend.business.dao.entity.prize;

import java.math.BigDecimal;
import java.util.Date;

public class UserPrizeStatistics {
    private String userId;

    private BigDecimal withdrawRedAmount;

    private BigDecimal usedWithdrawRedAmount;

    private BigDecimal guaranteeVouchersAmount;

    private BigDecimal usedGuaranteeVouchersAmount;

    private BigDecimal guaranteeInvestmentVouchersAmount;

    private BigDecimal usedGuaranteeInvestmentVouchersAmount;

    private BigDecimal investmentVouchersAmount;

    private BigDecimal usedInvestmentVouchersAmount;

    private BigDecimal cashRedAmount;

    private BigDecimal usedCashRedAmount;

    private BigDecimal guaranteeExperienceAmount;

    private BigDecimal usedGuaranteeExperienceAmount;

    private BigDecimal guaranteeExperienceProfit;

    private BigDecimal guaranteeInvestmentExperienceAmount;

    private BigDecimal usedGuaranteeInvestmentExperienceAmount;

    private BigDecimal investmentExperienceAmount;

    private BigDecimal usedInvestmentExperienceAmount;

    private BigDecimal investmentExperienceProfit;

    private Integer investmentRaiseCount;

    private Integer usedInvestmentRaiseCount;

    private BigDecimal investmentRaiseProfit;

    private BigDecimal investmentCashbackAmount;

    private BigDecimal usedInvestmentCashbackAmount;

    private BigDecimal guaranteeCashbackAmount;

    private BigDecimal usedGuaranteeCashbackAmount;

    private BigDecimal loanCashbackAmount;

    private BigDecimal usedLoanCashbackAmount;

    private Integer borrowInterestRateCount;

    private Integer usedBorrowInterestRateCount;

    private BigDecimal borrowInterestRateProfit;

    private BigDecimal personReceivedRedAmount;

    private BigDecimal personSendRedAmount;

    private BigDecimal groupReceivedAmount;

    private BigDecimal groupSendAmount;

    private Date updateTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getWithdrawRedAmount() {
        return withdrawRedAmount;
    }

    public void setWithdrawRedAmount(BigDecimal withdrawRedAmount) {
        this.withdrawRedAmount = withdrawRedAmount;
    }

    public BigDecimal getUsedWithdrawRedAmount() {
        return usedWithdrawRedAmount;
    }

    public void setUsedWithdrawRedAmount(BigDecimal usedWithdrawRedAmount) {
        this.usedWithdrawRedAmount = usedWithdrawRedAmount;
    }

    public BigDecimal getGuaranteeVouchersAmount() {
        return guaranteeVouchersAmount;
    }

    public void setGuaranteeVouchersAmount(BigDecimal guaranteeVouchersAmount) {
        this.guaranteeVouchersAmount = guaranteeVouchersAmount;
    }

    public BigDecimal getUsedGuaranteeVouchersAmount() {
        return usedGuaranteeVouchersAmount;
    }

    public void setUsedGuaranteeVouchersAmount(BigDecimal usedGuaranteeVouchersAmount) {
        this.usedGuaranteeVouchersAmount = usedGuaranteeVouchersAmount;
    }

    public BigDecimal getGuaranteeInvestmentVouchersAmount() {
        return guaranteeInvestmentVouchersAmount;
    }

    public void setGuaranteeInvestmentVouchersAmount(BigDecimal guaranteeInvestmentVouchersAmount) {
        this.guaranteeInvestmentVouchersAmount = guaranteeInvestmentVouchersAmount;
    }

    public BigDecimal getUsedGuaranteeInvestmentVouchersAmount() {
        return usedGuaranteeInvestmentVouchersAmount;
    }

    public void setUsedGuaranteeInvestmentVouchersAmount(BigDecimal usedGuaranteeInvestmentVouchersAmount) {
        this.usedGuaranteeInvestmentVouchersAmount = usedGuaranteeInvestmentVouchersAmount;
    }

    public BigDecimal getInvestmentVouchersAmount() {
        return investmentVouchersAmount;
    }

    public void setInvestmentVouchersAmount(BigDecimal investmentVouchersAmount) {
        this.investmentVouchersAmount = investmentVouchersAmount;
    }

    public BigDecimal getUsedInvestmentVouchersAmount() {
        return usedInvestmentVouchersAmount;
    }

    public void setUsedInvestmentVouchersAmount(BigDecimal usedInvestmentVouchersAmount) {
        this.usedInvestmentVouchersAmount = usedInvestmentVouchersAmount;
    }

    public BigDecimal getCashRedAmount() {
        return cashRedAmount;
    }

    public void setCashRedAmount(BigDecimal cashRedAmount) {
        this.cashRedAmount = cashRedAmount;
    }

    public BigDecimal getUsedCashRedAmount() {
        return usedCashRedAmount;
    }

    public void setUsedCashRedAmount(BigDecimal usedCashRedAmount) {
        this.usedCashRedAmount = usedCashRedAmount;
    }

    public BigDecimal getGuaranteeExperienceAmount() {
        return guaranteeExperienceAmount;
    }

    public void setGuaranteeExperienceAmount(BigDecimal guaranteeExperienceAmount) {
        this.guaranteeExperienceAmount = guaranteeExperienceAmount;
    }

    public BigDecimal getUsedGuaranteeExperienceAmount() {
        return usedGuaranteeExperienceAmount;
    }

    public void setUsedGuaranteeExperienceAmount(BigDecimal usedGuaranteeExperienceAmount) {
        this.usedGuaranteeExperienceAmount = usedGuaranteeExperienceAmount;
    }

    public BigDecimal getGuaranteeExperienceProfit() {
        return guaranteeExperienceProfit;
    }

    public void setGuaranteeExperienceProfit(BigDecimal guaranteeExperienceProfit) {
        this.guaranteeExperienceProfit = guaranteeExperienceProfit;
    }

    public BigDecimal getGuaranteeInvestmentExperienceAmount() {
        return guaranteeInvestmentExperienceAmount;
    }

    public void setGuaranteeInvestmentExperienceAmount(BigDecimal guaranteeInvestmentExperienceAmount) {
        this.guaranteeInvestmentExperienceAmount = guaranteeInvestmentExperienceAmount;
    }

    public BigDecimal getUsedGuaranteeInvestmentExperienceAmount() {
        return usedGuaranteeInvestmentExperienceAmount;
    }

    public void setUsedGuaranteeInvestmentExperienceAmount(BigDecimal usedGuaranteeInvestmentExperienceAmount) {
        this.usedGuaranteeInvestmentExperienceAmount = usedGuaranteeInvestmentExperienceAmount;
    }

    public BigDecimal getInvestmentExperienceAmount() {
        return investmentExperienceAmount;
    }

    public void setInvestmentExperienceAmount(BigDecimal investmentExperienceAmount) {
        this.investmentExperienceAmount = investmentExperienceAmount;
    }

    public BigDecimal getUsedInvestmentExperienceAmount() {
        return usedInvestmentExperienceAmount;
    }

    public void setUsedInvestmentExperienceAmount(BigDecimal usedInvestmentExperienceAmount) {
        this.usedInvestmentExperienceAmount = usedInvestmentExperienceAmount;
    }

    public BigDecimal getInvestmentExperienceProfit() {
        return investmentExperienceProfit;
    }

    public void setInvestmentExperienceProfit(BigDecimal investmentExperienceProfit) {
        this.investmentExperienceProfit = investmentExperienceProfit;
    }

    public Integer getInvestmentRaiseCount() {
        return investmentRaiseCount;
    }

    public void setInvestmentRaiseCount(Integer investmentRaiseCount) {
        this.investmentRaiseCount = investmentRaiseCount;
    }

    public Integer getUsedInvestmentRaiseCount() {
        return usedInvestmentRaiseCount;
    }

    public void setUsedInvestmentRaiseCount(Integer usedInvestmentRaiseCount) {
        this.usedInvestmentRaiseCount = usedInvestmentRaiseCount;
    }

    public BigDecimal getInvestmentRaiseProfit() {
        return investmentRaiseProfit;
    }

    public void setInvestmentRaiseProfit(BigDecimal investmentRaiseProfit) {
        this.investmentRaiseProfit = investmentRaiseProfit;
    }

    public BigDecimal getInvestmentCashbackAmount() {
        return investmentCashbackAmount;
    }

    public void setInvestmentCashbackAmount(BigDecimal investmentCashbackAmount) {
        this.investmentCashbackAmount = investmentCashbackAmount;
    }

    public BigDecimal getUsedInvestmentCashbackAmount() {
        return usedInvestmentCashbackAmount;
    }

    public void setUsedInvestmentCashbackAmount(BigDecimal usedInvestmentCashbackAmount) {
        this.usedInvestmentCashbackAmount = usedInvestmentCashbackAmount;
    }

    public BigDecimal getGuaranteeCashbackAmount() {
        return guaranteeCashbackAmount;
    }

    public void setGuaranteeCashbackAmount(BigDecimal guaranteeCashbackAmount) {
        this.guaranteeCashbackAmount = guaranteeCashbackAmount;
    }

    public BigDecimal getUsedGuaranteeCashbackAmount() {
        return usedGuaranteeCashbackAmount;
    }

    public void setUsedGuaranteeCashbackAmount(BigDecimal usedGuaranteeCashbackAmount) {
        this.usedGuaranteeCashbackAmount = usedGuaranteeCashbackAmount;
    }

    public BigDecimal getLoanCashbackAmount() {
        return loanCashbackAmount;
    }

    public void setLoanCashbackAmount(BigDecimal loanCashbackAmount) {
        this.loanCashbackAmount = loanCashbackAmount;
    }

    public BigDecimal getUsedLoanCashbackAmount() {
        return usedLoanCashbackAmount;
    }

    public void setUsedLoanCashbackAmount(BigDecimal usedLoanCashbackAmount) {
        this.usedLoanCashbackAmount = usedLoanCashbackAmount;
    }

    public Integer getBorrowInterestRateCount() {
        return borrowInterestRateCount;
    }

    public void setBorrowInterestRateCount(Integer borrowInterestRateCount) {
        this.borrowInterestRateCount = borrowInterestRateCount;
    }

    public Integer getUsedBorrowInterestRateCount() {
        return usedBorrowInterestRateCount;
    }

    public void setUsedBorrowInterestRateCount(Integer usedBorrowInterestRateCount) {
        this.usedBorrowInterestRateCount = usedBorrowInterestRateCount;
    }

    public BigDecimal getBorrowInterestRateProfit() {
        return borrowInterestRateProfit;
    }

    public void setBorrowInterestRateProfit(BigDecimal borrowInterestRateProfit) {
        this.borrowInterestRateProfit = borrowInterestRateProfit;
    }

    public BigDecimal getPersonReceivedRedAmount() {
        return personReceivedRedAmount;
    }

    public void setPersonReceivedRedAmount(BigDecimal personReceivedRedAmount) {
        this.personReceivedRedAmount = personReceivedRedAmount;
    }

    public BigDecimal getPersonSendRedAmount() {
        return personSendRedAmount;
    }

    public void setPersonSendRedAmount(BigDecimal personSendRedAmount) {
        this.personSendRedAmount = personSendRedAmount;
    }

    public BigDecimal getGroupReceivedAmount() {
        return groupReceivedAmount;
    }

    public void setGroupReceivedAmount(BigDecimal groupReceivedAmount) {
        this.groupReceivedAmount = groupReceivedAmount;
    }

    public BigDecimal getGroupSendAmount() {
        return groupSendAmount;
    }

    public void setGroupSendAmount(BigDecimal groupSendAmount) {
        this.groupSendAmount = groupSendAmount;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}