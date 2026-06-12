package com.aibudgetplanner.app.ui.java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.local.dao.ExpenseDao;
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao;
import com.aibudgetplanner.app.data.local.dao.UserProfileDao;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;
import com.aibudgetplanner.app.domain.model.AiInsight;
import com.aibudgetplanner.app.domain.model.BudgetSnapshot;
import com.aibudgetplanner.app.domain.usecase.BudgetEngineUseCase;
import com.aibudgetplanner.app.domain.usecase.GenerateAiInsightUseCase;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InsightsFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    UserProfileDao userProfileDao;

    @Inject
    FixedExpenseDao fixedExpenseDao;

    @Inject
    ExpenseDao expenseDao;

    @Inject
    BudgetEngineUseCase budgetEngineUseCase;

    @Inject
    GenerateAiInsightUseCase generateAiInsightUseCase;

    private TextView riskValue;
    private TextView healthValue;
    private TextView predictionValue;
    private TextView warningValue;
    private TextView suggestionValue;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_insights, container, false);
        riskValue = root.findViewById(R.id.insights_risk_value);
        healthValue = root.findViewById(R.id.insights_health_value);
        predictionValue = root.findViewById(R.id.insights_prediction_value);
        warningValue = root.findViewById(R.id.insights_warning_value);
        suggestionValue = root.findViewById(R.id.insights_suggestion_value);
        Button refreshButton = root.findViewById(R.id.insights_refresh_button);
        refreshButton.setOnClickListener(v -> loadInsights());
        loadInsights();
        return root;
    }

    private void loadInsights() {
        IO_EXECUTOR.execute(() -> {
            UserProfileEntity profile = userProfileDao.getByUser("local-user");
            if (profile == null) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(this::renderEmpty);
                return;
            }

            List<FixedExpenseEntity> fixedExpenses = fixedExpenseDao.getByUser("local-user");
            List<ExpenseEntity> expenses = expenseDao.getByUser("local-user");

            double fixedTotal = 0.0;
            for (FixedExpenseEntity item : fixedExpenses) {
                fixedTotal += item.amount;
            }

            long monthStart = LocalDate.now()
                    .withDayOfMonth(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            double spentThisMonth = 0.0;
            for (ExpenseEntity item : expenses) {
                if (item.date >= monthStart) {
                    spentThisMonth += item.amount;
                }
            }

            LocalDate today = LocalDate.now();
            int remainingDays = Math.max(today.lengthOfMonth() - today.getDayOfMonth() + 1, 1);
            double availableBudget = budgetEngineUseCase.calculateAvailableBudget(profile.salary, fixedTotal, profile.monthlySavingsGoal);
            double remainingBudget = budgetEngineUseCase.calculateRemainingBudget(availableBudget, spentThisMonth);
            double dailyBudget = budgetEngineUseCase.calculateDailyBudget(availableBudget, spentThisMonth, remainingDays);
            double savingsProgress = budgetEngineUseCase.calculateSavingsProgress(profile.salary, profile.monthlySavingsGoal);

            BudgetSnapshot snapshot = new BudgetSnapshot(
                    profile.salary,
                    profile.monthlySavingsGoal,
                    fixedTotal,
                    spentThisMonth,
                    availableBudget,
                    remainingBudget,
                    dailyBudget,
                    remainingDays,
                    savingsProgress
            );
            AiInsight insight = generateAiInsightUseCase.invoke(snapshot);

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> renderInsight(insight));
        });
    }

    private void renderEmpty() {
        riskValue.setText("No profile yet");
        healthValue.setText("-");
        predictionValue.setText("-");
        warningValue.setText("-");
        suggestionValue.setText("-");
    }

    private void renderInsight(AiInsight insight) {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        riskValue.setText(insight.getRiskLevel());
        healthValue.setText(insight.getFinancialHealthScore() + " - " + insight.getFinancialHealthCategory());
        predictionValue.setText(format.format(insight.getPredictedMonthEndSpend()));
        warningValue.setText(insight.getWarning());
        suggestionValue.setText(insight.getSuggestion());
    }
}
