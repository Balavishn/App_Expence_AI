package com.aibudgetplanner.app.domain.usecase;

import org.junit.Assert;
import org.junit.Test;

public class BudgetEngineUseCaseTest {

    private final BudgetEngineUseCase useCase = new BudgetEngineUseCase();

    @Test
    public void calculateAvailableBudget_returnsExpected() {
        double result = useCase.calculateAvailableBudget(100000.0, 30000.0, 20000.0);

        Assert.assertEquals(50000.0, result, 0.0001);
    }

    @Test
    public void calculateDailyBudget_handlesRemainingDays() {
        double result = useCase.calculateDailyBudget(60000.0, 15000.0, 15);

        Assert.assertEquals(3000.0, result, 0.0001);
    }

    @Test
    public void calculateSavingsProgress_handlesZeroSalary() {
        double result = useCase.calculateSavingsProgress(0.0, 10000.0);

        Assert.assertEquals(0.0, result, 0.0001);
    }
}
