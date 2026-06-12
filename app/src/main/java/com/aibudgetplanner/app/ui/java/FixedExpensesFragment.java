package com.aibudgetplanner.app.ui.java;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao;
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;
import com.aibudgetplanner.app.data.repository.BudgetRepository;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FixedExpensesFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    FixedExpenseDao fixedExpenseDao;

    @Inject
    BudgetRepository budgetRepository;

    private final List<FixedExpenseEntity> expenses = new ArrayList<>();
    private final List<String> categories = Arrays.asList(
            "Rent",
            "EMI",
            "Insurance",
            "Internet",
            "School Fees",
            "Subscriptions",
            "Other"
    );

    private EditText nameInput;
    private Spinner categorySpinner;
    private EditText amountInput;
    private EditText dueDateInput;
    private CheckBox recurringCheckbox;
    private Button saveButton;
    private Button cancelEditButton;
    private TextView editModeLabel;
    private ListView listView;
    private TextView emptyLabel;
    private ArrayAdapter<String> listAdapter;

    private Long editingExpenseId = null;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_fixed_expenses, container, false);

        nameInput = root.findViewById(R.id.fixed_name_input);
        categorySpinner = root.findViewById(R.id.fixed_category_spinner);
        amountInput = root.findViewById(R.id.fixed_amount_input);
        dueDateInput = root.findViewById(R.id.fixed_due_date_input);
        recurringCheckbox = root.findViewById(R.id.fixed_recurring_checkbox);
        saveButton = root.findViewById(R.id.fixed_save_button);
        cancelEditButton = root.findViewById(R.id.fixed_cancel_edit_button);
        editModeLabel = root.findViewById(R.id.fixed_edit_mode_label);
        listView = root.findViewById(R.id.fixed_list);
        emptyLabel = root.findViewById(R.id.fixed_empty_label);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        listAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(listAdapter);

        dueDateInput.setText("1");
        recurringCheckbox.setChecked(true);
        setCategorySelection("Other");

        saveButton.setOnClickListener(v -> saveFixedExpense());
        cancelEditButton.setOnClickListener(v -> clearForm());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < expenses.size()) {
                startEdit(expenses.get(position));
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < expenses.size()) {
                deleteFixedExpense(expenses.get(position));
            }
            return true;
        });

        loadFixedExpenses();
        updateEditModeUi();
        return root;
    }

    private void loadFixedExpenses() {
        IO_EXECUTOR.execute(() -> {
            List<FixedExpenseEntity> data = fixedExpenseDao.getByUser("local-user");
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                expenses.clear();
                expenses.addAll(data);
                renderList();
            });
        });
    }

    private void renderList() {
        DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
        List<String> rows = new ArrayList<>();
        for (FixedExpenseEntity item : expenses) {
            rows.add(
                    item.name + " | " + item.category + " | " + amountFormat.format(item.amount)
                            + " | Due " + item.dueDate
                            + (item.isRecurring ? " | Recurring" : " | One-time")
            );
        }
        listAdapter.clear();
        listAdapter.addAll(rows);
        listAdapter.notifyDataSetChanged();
        emptyLabel.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        listView.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void saveFixedExpense() {
        String name = defaultIfBlank(nameInput.getText().toString(), "Untitled");
        String category = String.valueOf(categorySpinner.getSelectedItem());
        double amount = parseDoubleOrDefault(amountInput.getText().toString(), -1.0);
        int dueDate = parseIntOrDefault(dueDateInput.getText().toString(), 1);
        boolean recurring = recurringCheckbox.isChecked();

        if (amount <= 0.0) {
            amountInput.setError("Enter a valid amount");
            return;
        }

        if (dueDate < 1 || dueDate > 31) {
            dueDateInput.setError("Due date must be 1-31");
            return;
        }

        saveButton.setEnabled(false);

        IO_EXECUTOR.execute(() -> {
            FixedExpenseEntity entity = new FixedExpenseEntity(
                    editingExpenseId == null ? 0 : editingExpenseId,
                    "local-user",
                    name,
                    category,
                    amount,
                    dueDate,
                    recurring,
                    System.currentTimeMillis()
            );

            if (editingExpenseId == null) {
                budgetRepository.addFixedExpense(entity);
            } else {
                budgetRepository.updateFixedExpense(entity);
            }

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                clearForm();
                saveButton.setEnabled(true);
                loadFixedExpenses();
            });
        });
    }

    private void startEdit(FixedExpenseEntity expense) {
        editingExpenseId = expense.expenseId;
        nameInput.setText(expense.name);
        setCategorySelection(expense.category);
        amountInput.setText(Double.toString(expense.amount));
        dueDateInput.setText(Integer.toString(expense.dueDate));
        recurringCheckbox.setChecked(expense.isRecurring);
        updateEditModeUi();
    }

    private void clearForm() {
        editingExpenseId = null;
        nameInput.setText("");
        setCategorySelection("Other");
        amountInput.setText("");
        dueDateInput.setText("1");
        recurringCheckbox.setChecked(true);
        updateEditModeUi();
    }

    private void deleteFixedExpense(FixedExpenseEntity expense) {
        IO_EXECUTOR.execute(() -> {
            budgetRepository.deleteFixedExpense(expense);
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(this::loadFixedExpenses);
        });
    }

    private void updateEditModeUi() {
        boolean editing = editingExpenseId != null;
        editModeLabel.setVisibility(editing ? View.VISIBLE : View.GONE);
        cancelEditButton.setVisibility(editing ? View.VISIBLE : View.GONE);
        saveButton.setText(editing ? "Update Fixed Expense" : "Save Fixed Expense");
    }

    private void setCategorySelection(String category) {
        int index = categories.indexOf(category);
        categorySpinner.setSelection(index >= 0 ? index : categories.indexOf("Other"));
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
