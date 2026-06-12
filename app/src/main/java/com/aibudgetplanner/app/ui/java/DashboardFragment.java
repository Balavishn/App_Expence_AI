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

import com.aibudgetplanner.app.MainActivity;
import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.local.dao.ExpenseDao;
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao;
import com.aibudgetplanner.app.data.local.dao.UserProfileDao;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;
import com.aibudgetplanner.app.domain.model.BudgetSnapshot;
import com.aibudgetplanner.app.domain.model.PredictionResult;
import com.aibudgetplanner.app.domain.usecase.BudgetEngineUseCase;
import com.aibudgetplanner.app.domain.usecase.ExpensePredictionUseCase;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
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
    ExpensePredictionUseCase expensePredictionUseCase;

    private TextView salaryValue;
    private TextView availableBudgetValue;
    private TextView spentThisMonthValue;
    private TextView dailyBudgetValue;
    private TextView predictionValue;
    private TextView riskValue;
    private Button refreshButton;
    private Button addExpenseButton;
    private Button expenseHistoryButton;
    private Button reportsButton;
    private Button profileButton;
    private Button fixedExpensesButton;
    private Button statementImportButton;
    private Button insightsButton;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        salaryValue = root.findViewById(R.id.dashboard_salary_value);
        availableBudgetValue = root.findViewById(R.id.dashboard_available_budget_value);
        spentThisMonthValue = root.findViewById(R.id.dashboard_spent_this_month_value);
        dailyBudgetValue = root.findViewById(R.id.dashboard_daily_budget_value);
        predictionValue = root.findViewById(R.id.dashboard_prediction_value);
        riskValue = root.findViewById(R.id.dashboard_risk_value);
        refreshButton = root.findViewById(R.id.dashboard_refresh_button);
        addExpenseButton = root.findViewById(R.id.dashboard_add_expense_button);
        expenseHistoryButton = root.findViewById(R.id.dashboard_expense_history_button);
        reportsButton = root.findViewById(R.id.dashboard_reports_button);
        profileButton = root.findViewById(R.id.dashboard_profile_button);
        fixedExpensesButton = root.findViewById(R.id.dashboard_fixed_expenses_button);
        statementImportButton = root.findViewById(R.id.dashboard_statement_import_button);
        insightsButton = root.findViewById(R.id.dashboard_insights_button);

        refreshButton.setOnClickListener(v -> loadSummary());
        addExpenseButton.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openAddExpense();
        });
        expenseHistoryButton.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openExpenseHistory();
        });
        reportsButton.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openReports();
        });
        profileButton.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openProfile();
        });
        fixedExpensesButton.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openFixedExpenses();
        });
        statementImportButton.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openStatementImport();
        });
        insightsButton.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openInsights();
        });
        loadSummary();
        return root;
    }

    private void loadSummary() {
        refreshButton.setEnabled(false);
        IO_EXECUTOR.execute(() -> {
            UserProfileEntity profile = userProfileDao.getByUser("local-user");
            if (profile == null) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    renderEmpty();
                    refreshButton.setEnabled(true);
                });
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
            PredictionResult prediction = expensePredictionUseCase.invoke(snapshot);

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                renderSnapshot(profile.currency, snapshot, prediction);
                refreshButton.setEnabled(true);
            });
        });
    }

    private void renderEmpty() {
        salaryValue.setText("No profile yet");
        availableBudgetValue.setText("-");
        spentThisMonthValue.setText("-");
        dailyBudgetValue.setText("-");
        predictionValue.setText("-");
        riskValue.setText("-");
    }

    private void renderSnapshot(String currency, BudgetSnapshot snapshot, PredictionResult prediction) {
        DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
        salaryValue.setText(currency + " " + amountFormat.format(snapshot.getSalary()));
        availableBudgetValue.setText(currency + " " + amountFormat.format(snapshot.getAvailableBudget()));
        spentThisMonthValue.setText(currency + " " + amountFormat.format(snapshot.getTotalSpentThisMonth()));
        dailyBudgetValue.setText(currency + " " + amountFormat.format(snapshot.getDailyBudget()));
        predictionValue.setText(currency + " " + amountFormat.format(prediction.getPredictedSpending()));
        riskValue.setText(prediction.getRiskLevel() + " (" + prediction.getModelUsed() + ")");
    }
}
