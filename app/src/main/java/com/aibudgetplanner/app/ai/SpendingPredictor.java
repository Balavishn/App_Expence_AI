package com.aibudgetplanner.app.ai;

import com.aibudgetplanner.app.domain.model.BudgetSnapshot;

public interface SpendingPredictor {
    PredictionInference predictMonthEndSpend(BudgetSnapshot snapshot);
}
