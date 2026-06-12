package com.aibudgetplanner.app.domain.model;

public class BudgetSnapshot {
    private final double salary;
    private final double savingsGoal;
    private final double totalFixedExpenses;
    private final double totalSpentThisMonth;
    private final double availableBudget;
    private final double remainingBudget;
    private final double dailyBudget;
    private final int remainingDays;
    private final double savingsProgress;

    public BudgetSnapshot(
            double salary,
            double savingsGoal,
            double totalFixedExpenses,
            double totalSpentThisMonth,
            double availableBudget,
            double remainingBudget,
            double dailyBudget,
            int remainingDays,
            double savingsProgress
    ) {
        this.salary = salary;
        this.savingsGoal = savingsGoal;
        this.totalFixedExpenses = totalFixedExpenses;
        this.totalSpentThisMonth = totalSpentThisMonth;
        this.availableBudget = availableBudget;
        this.remainingBudget = remainingBudget;
        this.dailyBudget = dailyBudget;
        this.remainingDays = remainingDays;
        this.savingsProgress = savingsProgress;
    }

    public double getSalary() {
        return salary;
    }

    public double getSavingsGoal() {
        return savingsGoal;
    }

    public double getTotalFixedExpenses() {
        return totalFixedExpenses;
    }

    public double getTotalSpentThisMonth() {
        return totalSpentThisMonth;
    }

    public double getAvailableBudget() {
        return availableBudget;
    }

    public double getRemainingBudget() {
        return remainingBudget;
    }

    public double getDailyBudget() {
        return dailyBudget;
    }

    public int getRemainingDays() {
        return remainingDays;
    }

    public double getSavingsProgress() {
        return savingsProgress;
    }
}
