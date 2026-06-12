package com.aibudgetplanner.app.domain.usecase;

import com.aibudgetplanner.app.domain.model.AiInsight;
import com.aibudgetplanner.app.domain.model.BudgetSnapshot;

import javax.inject.Inject;

public class GenerateAiInsightUseCase {

    @Inject
    public GenerateAiInsightUseCase() {
    }

    public AiInsight invoke(BudgetSnapshot snapshot) {
        double predictedSpend = snapshot.getTotalSpentThisMonth() +
                (snapshot.getDailyBudget() * snapshot.getRemainingDays());
        double budgetUtilization = snapshot.getAvailableBudget() <= 0.0
                ? 1.0
                : snapshot.getTotalSpentThisMonth() / snapshot.getAvailableBudget();

        String riskLevel;
        if (budgetUtilization > 1.0) {
            riskLevel = "High";
        } else if (budgetUtilization > 0.85) {
            riskLevel = "Medium";
        } else {
            riskLevel = "Low";
        }

        String warning;
        if (budgetUtilization > 1.0) {
            warning = "You are overspending. Reduce variable spending today to protect your savings goal.";
        } else if (budgetUtilization > 0.85) {
            warning = "Spending is close to limit. Focus on essentials for the rest of this month.";
        } else {
            warning = "No major risk detected.";
        }

        String suggestion;
        if (budgetUtilization > 1.0) {
            suggestion = "Cut discretionary categories immediately and cap daily spend to remaining budget.";
        } else if (budgetUtilization > 0.85) {
            suggestion = "Reduce food and shopping categories by 10% for the rest of month.";
        } else {
            suggestion = "Allocate extra surplus toward your monthly savings goal.";
        }

        int score = calculateHealthScore(snapshot);
        String category;
        if (score >= 90) {
            category = "Excellent";
        } else if (score >= 70) {
            category = "Good";
        } else if (score >= 50) {
            category = "Average";
        } else {
            category = "Needs Improvement";
        }

        return new AiInsight(
                riskLevel,
                warning,
                suggestion,
                predictedSpend,
                score,
                category
        );
    }

    private int calculateHealthScore(BudgetSnapshot snapshot) {
        double savingsRatio = snapshot.getSalary() > 0.0
                ? snapshot.getSavingsGoal() / snapshot.getSalary()
                : 0.0;
        double expenseRatio = snapshot.getSalary() > 0.0
                ? snapshot.getTotalSpentThisMonth() / snapshot.getSalary()
                : 1.0;
        double adherence = snapshot.getAvailableBudget() > 0.0
                ? Math.max(snapshot.getRemainingBudget() / snapshot.getAvailableBudget(), 0.0)
                : 0.0;

        double clampedExpenseRatio = Math.min(expenseRatio, 1.0);
        double raw = (savingsRatio * 40.0) + ((1.0 - clampedExpenseRatio) * 35.0) + (adherence * 25.0);
        int score = (int) raw;
        return Math.max(0, Math.min(100, score));
    }
}
