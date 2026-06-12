package com.aibudgetplanner.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aibudgetplanner.app.ui.java.AddExpenseFragment;
import com.aibudgetplanner.app.ui.java.AppLockFragment;
import com.aibudgetplanner.app.ui.java.DashboardFragment;
import com.aibudgetplanner.app.ui.java.ExpenseHistoryFragment;
import com.aibudgetplanner.app.ui.java.FixedExpensesFragment;
import com.aibudgetplanner.app.ui.java.InsightsFragment;
import com.aibudgetplanner.app.ui.java.ProfileFragment;
import com.aibudgetplanner.app.ui.java.ReportsFragment;
import com.aibudgetplanner.app.ui.java.SetupFragment;
import com.aibudgetplanner.app.ui.java.StatementImportFragment;
import com.aibudgetplanner.app.data.local.dao.UserProfileDao;
import com.aibudgetplanner.app.security.AppLockRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    AppLockRepository appLockRepository;

    @Inject
    UserProfileDao userProfileDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            if (appLockRepository.isLockEnabled()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AppLockFragment())
                        .commit();
            } else {
                openInitialDestination();
            }
        }
    }

    public void onAppUnlocked() {
        openInitialDestination();
    }

    private void openInitialDestination() {
        IO_EXECUTOR.execute(() -> {
            boolean hasProfile = userProfileDao.getByUser("local-user") != null;
            runOnUiThread(() -> {
                if (hasProfile) {
                    openDashboard();
                } else {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new SetupFragment())
                            .commit();
                }
            });
        });
    }

    public void openDashboard() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
                .commit();
    }

    public void openAddExpense() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AddExpenseFragment())
                .addToBackStack("add_expense")
                .commit();
    }

    public void openExpenseHistory() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ExpenseHistoryFragment())
                .addToBackStack("expense_history")
                .commit();
    }

    public void openReports() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ReportsFragment())
                .addToBackStack("reports")
                .commit();
    }

    public void openProfile() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .addToBackStack("profile")
                .commit();
    }

    public void openFixedExpenses() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new FixedExpensesFragment())
                .addToBackStack("fixed_expenses")
                .commit();
    }

    public void openStatementImport() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new StatementImportFragment())
                .addToBackStack("statement_import")
                .commit();
    }

    public void openInsights() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new InsightsFragment())
                .addToBackStack("insights")
                .commit();
    }
}
