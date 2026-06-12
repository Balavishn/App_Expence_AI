package com.aibudgetplanner.app.domain.model;

public class StatementImportResult {
    private final String statementType;
    private final int importedCount;
    private final int duplicateCount;
    private final int totalParsedCount;
    private final int autoCategorizedCount;

    public StatementImportResult(
            String statementType,
            int importedCount,
            int duplicateCount,
            int totalParsedCount,
            int autoCategorizedCount
    ) {
        this.statementType = statementType;
        this.importedCount = importedCount;
        this.duplicateCount = duplicateCount;
        this.totalParsedCount = totalParsedCount;
        this.autoCategorizedCount = autoCategorizedCount;
    }

    public String getStatementType() {
        return statementType;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public int getTotalParsedCount() {
        return totalParsedCount;
    }

    public int getAutoCategorizedCount() {
        return autoCategorizedCount;
    }
}
