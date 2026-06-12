package com.aibudgetplanner.app.ui.navigation;

public final class AppDestination {
    public static final Destination Setup = new Destination("setup");
    public static final Destination Dashboard = new Destination("dashboard");
    public static final Destination FixedExpenses = new Destination("fixed_expenses");
    public static final Destination AddExpense = new Destination("add_expense");
    public static final Destination ExpenseHistory = new Destination("expense_history");
    public static final Destination Reports = new Destination("reports");
    public static final Destination StatementImport = new Destination("statement_import");
    public static final Destination Insights = new Destination("insights");
    public static final Destination Profile = new Destination("profile");

    private AppDestination() {
    }

    public static final class Destination {
        public final String route;

        public Destination(String route) {
            this.route = route;
        }
    }
}
