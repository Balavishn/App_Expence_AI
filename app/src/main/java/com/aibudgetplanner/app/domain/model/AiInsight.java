package com.aibudgetplanner.app.domain.model;

public class AiInsight {
    private final String riskLevel;
    private final String warning;
    private final String suggestion;
    private final double predictedMonthEndSpend;
    private final int financialHealthScore;
    private final String financialHealthCategory;

    public AiInsight(
            String riskLevel,
            String warning,
            String suggestion,
            double predictedMonthEndSpend,
            int financialHealthScore,
            String financialHealthCategory
    ) {
        this.riskLevel = riskLevel;
        this.warning = warning;
        this.suggestion = suggestion;
        this.predictedMonthEndSpend = predictedMonthEndSpend;
        this.financialHealthScore = financialHealthScore;
        this.financialHealthCategory = financialHealthCategory;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getWarning() {
        return warning;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public double getPredictedMonthEndSpend() {
        return predictedMonthEndSpend;
    }

    public int getFinancialHealthScore() {
        return financialHealthScore;
    }

    public String getFinancialHealthCategory() {
        return financialHealthCategory;
    }
}
