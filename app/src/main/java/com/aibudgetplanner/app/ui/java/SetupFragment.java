package com.aibudgetplanner.app.ui.java;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aibudgetplanner.app.MainActivity;
import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;
import com.aibudgetplanner.app.data.repository.BudgetRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SetupFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    BudgetRepository budgetRepository;

    private EditText salaryInput;
    private EditText savingsInput;
    private EditText salaryDateInput;
    private EditText currencyInput;
    private EditText goalsInput;
    private Button continueButton;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_setup, container, false);
        salaryInput = root.findViewById(R.id.setup_salary);
        savingsInput = root.findViewById(R.id.setup_savings_goal);
        salaryDateInput = root.findViewById(R.id.setup_salary_date);
        currencyInput = root.findViewById(R.id.setup_currency);
        goalsInput = root.findViewById(R.id.setup_financial_goals);
        continueButton = root.findViewById(R.id.setup_continue_button);

        salaryDateInput.setText("1");
        currencyInput.setText("INR");
        goalsInput.setText("Emergency fund, investments");

        continueButton.setOnClickListener(v -> saveAndContinue());
        return root;
    }

    private void saveAndContinue() {
        continueButton.setEnabled(false);

        final double salary = parseDoubleOrDefault(salaryInput.getText().toString(), 0.0);
        final double savingsGoal = parseDoubleOrDefault(savingsInput.getText().toString(), 0.0);
        final int salaryDate = parseIntOrDefault(salaryDateInput.getText().toString(), 1);
        final String currency = defaultIfBlank(currencyInput.getText().toString(), "INR");
        final String goals = defaultIfBlank(goalsInput.getText().toString(), "Emergency fund, investments");

        IO_EXECUTOR.execute(() -> {
            long now = System.currentTimeMillis();
            budgetRepository.upsertProfile(new UserProfileEntity(
                    "local-user",
                    salary,
                    salaryDate,
                    savingsGoal,
                    currency,
                    now,
                    goals,
                    now
            ));

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                MainActivity activity = (MainActivity) requireActivity();
                activity.openDashboard();
            });
        });
    }

    private static double parseDoubleOrDefault(String value, double fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int parseIntOrDefault(String value, int fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String defaultIfBlank(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
