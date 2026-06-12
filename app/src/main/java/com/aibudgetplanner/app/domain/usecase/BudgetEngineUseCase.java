package com.aibudgetplanner.app.domain.usecase;

import javax.inject.Inject;

public class BudgetEngineUseCase {

    @Inject
    public BudgetEngineUseCase() {
    }

    public double calculateAvailableBudget(double salary, double fixedExpenses, double savingsGoal) {
        return salary - fixedExpenses - savingsGoal;
    }

    public double calculateDailyBudget(double availableBudget, double spentThisMonth, int remainingDays) {
        double remainingBudget = availableBudget - spentThisMonth;
        return remainingDays <= 0 ? remainingBudget : remainingBudget / remainingDays;
    }

    public double calculateRemainingBudget(double availableBudget, double spentThisMonth) {
        return availableBudget - spentThisMonth;
    }

    public double calculateSavingsProgress(double salary, double savingsGoal) {
        if (salary <= 0.0) {
            return 0.0;
        }
        return (savingsGoal / salary) * 100.0;
    }
}
