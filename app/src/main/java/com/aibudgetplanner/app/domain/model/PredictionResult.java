package com.aibudgetplanner.app.domain.model;

public class PredictionResult {
    private final double predictedSpending;
    private final double predictedSavings;
    private final String riskLevel;
    private final String modelUsed;

    public PredictionResult(double predictedSpending, double predictedSavings, String riskLevel, String modelUsed) {
        this.predictedSpending = predictedSpending;
        this.predictedSavings = predictedSavings;
        this.riskLevel = riskLevel;
        this.modelUsed = modelUsed;
    }

    public double getPredictedSpending() {
        return predictedSpending;
    }

    public double getPredictedSavings() {
        return predictedSavings;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getModelUsed() {
        return modelUsed;
    }
}
