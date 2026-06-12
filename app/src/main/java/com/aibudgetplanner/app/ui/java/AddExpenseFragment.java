package com.aibudgetplanner.app.ui.java;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aibudgetplanner.app.MainActivity;
import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.repository.BudgetRepository;
import com.aibudgetplanner.app.domain.model.ExpenseCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddExpenseFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    BudgetRepository budgetRepository;

    private EditText amountInput;
    private Spinner categorySpinner;
    private EditText descriptionInput;
    private EditText paymentMethodInput;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_add_expense, container, false);
        amountInput = root.findViewById(R.id.expense_amount_input);
        categorySpinner = root.findViewById(R.id.expense_category_spinner);
        descriptionInput = root.findViewById(R.id.expense_description_input);
        paymentMethodInput = root.findViewById(R.id.expense_payment_method_input);
        saveButton = root.findViewById(R.id.expense_save_button);

        paymentMethodInput.setText("UPI");

        List<String> categories = new ArrayList<>();
        for (ExpenseCategory category : ExpenseCategory.values()) {
            categories.add(category.name());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> saveExpense());
        return root;
    }

    private void saveExpense() {
        double amount = parseDoubleOrDefault(amountInput.getText().toString(), -1.0);
        if (amount <= 0.0) {
            amountInput.setError("Enter a valid amount");
            return;
        }

        saveButton.setEnabled(false);
        final String category = String.valueOf(categorySpinner.getSelectedItem());
        final String description = defaultIfBlank(descriptionInput.getText().toString(), "Manual entry");
        final String paymentMethod = defaultIfBlank(paymentMethodInput.getText().toString(), "UPI");

        IO_EXECUTOR.execute(() -> {
            budgetRepository.addExpense(new ExpenseEntity(
                    0,
                    "local-user",
                    System.currentTimeMillis(),
                    category,
                    amount,
                    description,
                    paymentMethod,
                    System.currentTimeMillis()
            ));

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                MainActivity activity = (MainActivity) requireActivity();
                activity.openExpenseHistory();
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

    private static String defaultIfBlank(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
