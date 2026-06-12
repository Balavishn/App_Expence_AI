package com.aibudgetplanner.app.ui.java;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aibudgetplanner.app.R;
import com.aibudgetplanner.app.data.local.dao.ExpenseDao;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.repository.BudgetRepository;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpenseHistoryFragment extends Fragment {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    ExpenseDao expenseDao;

    @Inject
    BudgetRepository budgetRepository;

    private final List<ExpenseEntity> expenses = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView emptyLabel;
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_expense_history, container, false);
        emptyLabel = root.findViewById(R.id.expense_history_empty_label);
        listView = root.findViewById(R.id.expense_history_list);
        Button refreshButton = root.findViewById(R.id.expense_history_refresh_button);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= expenses.size()) {
                return;
            }
            ExpenseEntity target = expenses.get(position);
            deleteExpense(target);
        });

        refreshButton.setOnClickListener(v -> loadExpenses());
        loadExpenses();
        return root;
    }

    private void loadExpenses() {
        IO_EXECUTOR.execute(() -> {
            List<ExpenseEntity> data = expenseDao.getByUser("local-user");
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                expenses.clear();
                expenses.addAll(data);

                List<String> rows = new ArrayList<>();
                DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (ExpenseEntity item : expenses) {
                    String date = Instant.ofEpochMilli(item.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(dateFormatter);
                    rows.add(
                            date + " | " + item.category + " | " + amountFormat.format(item.amount) + "\n"
                                    + item.description + " (" + item.paymentMethod + ")"
                    );
                }

                adapter.clear();
                adapter.addAll(rows);
                adapter.notifyDataSetChanged();
                emptyLabel.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
                listView.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void deleteExpense(ExpenseEntity expense) {
        IO_EXECUTOR.execute(() -> {
            budgetRepository.deleteExpense(expense);
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(this::loadExpenses);
        });
    }
}
