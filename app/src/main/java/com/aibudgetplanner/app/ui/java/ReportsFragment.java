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
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReportsFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    ExpenseDao expenseDao;

    private TextView dailyTotal;
    private TextView weeklyTotal;
    private TextView monthlyTotal;
    private TextView yearlyTotal;
    private TextView topCategory;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_reports, container, false);
        dailyTotal = root.findViewById(R.id.reports_daily_total_value);
        weeklyTotal = root.findViewById(R.id.reports_weekly_total_value);
        monthlyTotal = root.findViewById(R.id.reports_monthly_total_value);
        yearlyTotal = root.findViewById(R.id.reports_yearly_total_value);
        topCategory = root.findViewById(R.id.reports_top_category_value);
        Button refresh = root.findViewById(R.id.reports_refresh_button);
        refresh.setOnClickListener(v -> loadReports());
        loadReports();
        return root;
    }

    private void loadReports() {
        IO_EXECUTOR.execute(() -> {
            List<ExpenseEntity> expenses = expenseDao.getByUser("local-user");

            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(6);
            YearMonth thisMonth = YearMonth.now();
            int year = today.getYear();

            double daily = 0.0;
            double weekly = 0.0;
            double monthly = 0.0;
            double yearly = 0.0;
            Map<String, Double> categoryTotals = new HashMap<>();

            for (ExpenseEntity expense : expenses) {
                LocalDate date = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate();
                if (date.equals(today)) {
                    daily += expense.amount;
                }
                if (!date.isBefore(weekStart) && !date.isAfter(today)) {
                    weekly += expense.amount;
                }
                if (YearMonth.from(date).equals(thisMonth)) {
                    monthly += expense.amount;
                }
                if (date.getYear() == year) {
                    yearly += expense.amount;
                }
                categoryTotals.put(expense.category, categoryTotals.getOrDefault(expense.category, 0.0) + expense.amount);
            }

            String bestCategory = "-";
            double bestTotal = -1.0;
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                if (entry.getValue() > bestTotal) {
                    bestTotal = entry.getValue();
                    bestCategory = entry.getKey() + " (" + formatAmount(entry.getValue()) + ")";
                }
            }

            if (!isAdded()) {
                return;
            }

            final double finalDaily = daily;
            final double finalWeekly = weekly;
            final double finalMonthly = monthly;
            final double finalYearly = yearly;
            final String finalBestCategory = bestCategory;
            requireActivity().runOnUiThread(() -> {
                dailyTotal.setText(formatAmount(finalDaily));
                weeklyTotal.setText(formatAmount(finalWeekly));
                monthlyTotal.setText(formatAmount(finalMonthly));
                yearlyTotal.setText(formatAmount(finalYearly));
                topCategory.setText(finalBestCategory);
            });
        });
    }

    private static String formatAmount(double value) {
        return new DecimalFormat("#,##0.00").format(value);
    }
}
