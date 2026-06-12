package com.aibudgetplanner.app.domain.usecase;

import com.aibudgetplanner.app.ai.PredictionInference;
import com.aibudgetplanner.app.ai.SpendingPredictor;
import com.aibudgetplanner.app.domain.model.BudgetSnapshot;
import com.aibudgetplanner.app.domain.model.PredictionResult;

import javax.inject.Inject;

public class ExpensePredictionUseCase {
    private final SpendingPredictor spendingPredictor;

    @Inject
    public ExpensePredictionUseCase(SpendingPredictor spendingPredictor) {
        this.spendingPredictor = spendingPredictor;
    }

    public PredictionResult invoke(BudgetSnapshot snapshot) {
        PredictionInference inference = spendingPredictor.predictMonthEndSpend(snapshot);
        double projectedSpending = inference.getPredictedSpending();
        double projectedSavings = snapshot.getSalary() - snapshot.getTotalFixedExpenses() - projectedSpending;

        double utilization = snapshot.getAvailableBudget() <= 0.0
                ? 1.0
                : projectedSpending / snapshot.getAvailableBudget();

        String riskLevel;
        if (utilization > 1.0) {
            riskLevel = "High";
        } else if (utilization > 0.85) {
            riskLevel = "Medium";
        } else {
            riskLevel = "Low";
        }

        return new PredictionResult(
                projectedSpending,
                projectedSavings,
                riskLevel,
                inference.getModelUsed()
        );
    }
}
