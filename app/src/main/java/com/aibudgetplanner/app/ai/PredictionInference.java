package com.aibudgetplanner.app.ai;

public class PredictionInference {
    private final double predictedSpending;
    private final String modelUsed;

    public PredictionInference(double predictedSpending, String modelUsed) {
        this.predictedSpending = predictedSpending;
        this.modelUsed = modelUsed;
    }

    public double getPredictedSpending() {
        return predictedSpending;
    }

    public String getModelUsed() {
        return modelUsed;
    }
}
