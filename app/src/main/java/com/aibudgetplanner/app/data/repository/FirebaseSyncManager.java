package com.aibudgetplanner.app.data.repository;

import com.aibudgetplanner.app.data.local.dao.ExpenseDao;
import com.aibudgetplanner.app.data.local.dao.FixedExpenseDao;
import com.aibudgetplanner.app.data.local.dao.UserProfileDao;
import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;
import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseSyncManager {
    private final FirebaseFirestore firestore;
    private final UserProfileDao userProfileDao;
    private final FixedExpenseDao fixedExpenseDao;
    private final ExpenseDao expenseDao;

    @Inject
    public FirebaseSyncManager(
            FirebaseFirestore firestore,
            UserProfileDao userProfileDao,
            FixedExpenseDao fixedExpenseDao,
            ExpenseDao expenseDao
    ) {
        this.firestore = firestore;
        this.userProfileDao = userProfileDao;
        this.fixedExpenseDao = fixedExpenseDao;
        this.expenseDao = expenseDao;
    }

    public static boolean shouldUseLocalVersion(long localUpdatedAt, Long remoteUpdatedAt) {
        return remoteUpdatedAt == null || localUpdatedAt >= remoteUpdatedAt;
    }

    public void upsertRemoteProfile(String userId, UserProfileEntity profile) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("profile")
                        .document("main")
                        .set(RemoteUserProfile.from(profile))
        );
    }

    public void upsertRemoteExpense(String userId, ExpenseEntity expense) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("expenses")
                        .document(String.valueOf(expense.expenseId))
                        .set(RemoteExpense.from(expense))
        );
    }

    public void deleteRemoteExpense(String userId, long expenseId) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("expenses")
                        .document(String.valueOf(expenseId))
                        .delete()
        );
    }

    public void upsertRemoteFixedExpense(String userId, FixedExpenseEntity expense) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("fixed_expenses")
                        .document(String.valueOf(expense.expenseId))
                        .set(RemoteFixedExpense.from(expense))
        );
    }

    public void deleteRemoteFixedExpense(String userId, long expenseId) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("fixed_expenses")
                        .document(String.valueOf(expenseId))
                        .delete()
        );
    }

    public SyncSummary sync() throws Exception {
        return sync("local-user");
    }

    public SyncSummary sync(String userId) throws Exception {
        int uploaded = 0;
        int downloaded = 0;
        int conflicts = 0;

        var userRoot = firestore.collection("users").document(userId);

        UserProfileEntity localProfile = userProfileDao.getByUser(userId);
        if (localProfile != null) {
            DocumentSnapshot remoteProfileDoc = Tasks.await(
                    userRoot.collection("profile").document("main").get()
            );
            RemoteUserProfile remote = remoteProfileDoc.toObject(RemoteUserProfile.class);

            if (shouldUseLocalVersion(localProfile.updatedAt, remote == null ? null : remote.updatedAt)) {
                Tasks.await(userRoot.collection("profile").document("main").set(RemoteUserProfile.from(localProfile)));
                uploaded += 1;
            } else {
                userProfileDao.upsert(remote.toEntity());
                downloaded += 1;
                conflicts += 1;
            }
        }

        List<FixedExpenseEntity> localFixed = fixedExpenseDao.getByUser(userId);
        QuerySnapshot remoteFixedSnapshot = Tasks.await(userRoot.collection("fixed_expenses").get());
        Map<String, DocumentSnapshot> remoteFixedMap = new HashMap<>();
        Map<String, FixedExpenseEntity> localFixedMap = new HashMap<>();
        for (DocumentSnapshot document : remoteFixedSnapshot.getDocuments()) {
            remoteFixedMap.put(document.getId(), document);
        }
        for (FixedExpenseEntity local : localFixed) {
            localFixedMap.put(String.valueOf(local.expenseId), local);
        }

        for (FixedExpenseEntity local : localFixed) {
            String docId = String.valueOf(local.expenseId);
            DocumentSnapshot doc = remoteFixedMap.get(docId);
            RemoteFixedExpense remote = doc == null ? null : doc.toObject(RemoteFixedExpense.class);
            if (shouldUseLocalVersion(local.updatedAt, remote == null ? null : remote.updatedAt)) {
                Tasks.await(userRoot.collection("fixed_expenses").document(docId).set(RemoteFixedExpense.from(local)));
                uploaded += 1;
            } else {
                fixedExpenseDao.insert(remote.toEntity());
                downloaded += 1;
                conflicts += 1;
            }
        }

        for (Map.Entry<String, DocumentSnapshot> entry : remoteFixedMap.entrySet()) {
            if (localFixedMap.containsKey(entry.getKey())) {
                continue;
            }
            RemoteFixedExpense remote = entry.getValue().toObject(RemoteFixedExpense.class);
            if (remote == null) {
                continue;
            }
            fixedExpenseDao.insert(remote.toEntity());
            downloaded += 1;
        }

        List<ExpenseEntity> localExpenses = expenseDao.getByUser(userId);
        QuerySnapshot remoteExpenseSnapshot = Tasks.await(userRoot.collection("expenses").get());
        Map<String, DocumentSnapshot> remoteExpenseMap = new HashMap<>();
        Map<String, ExpenseEntity> localExpenseMap = new HashMap<>();
        for (DocumentSnapshot document : remoteExpenseSnapshot.getDocuments()) {
            remoteExpenseMap.put(document.getId(), document);
        }
        for (ExpenseEntity local : localExpenses) {
            localExpenseMap.put(String.valueOf(local.expenseId), local);
        }

        for (ExpenseEntity local : localExpenses) {
            String docId = String.valueOf(local.expenseId);
            DocumentSnapshot doc = remoteExpenseMap.get(docId);
            RemoteExpense remote = doc == null ? null : doc.toObject(RemoteExpense.class);
            if (shouldUseLocalVersion(local.updatedAt, remote == null ? null : remote.updatedAt)) {
                Tasks.await(userRoot.collection("expenses").document(docId).set(RemoteExpense.from(local)));
                uploaded += 1;
            } else {
                expenseDao.insert(remote.toEntity());
                downloaded += 1;
                conflicts += 1;
            }
        }

        for (Map.Entry<String, DocumentSnapshot> entry : remoteExpenseMap.entrySet()) {
            if (localExpenseMap.containsKey(entry.getKey())) {
                continue;
            }
            RemoteExpense remote = entry.getValue().toObject(RemoteExpense.class);
            if (remote == null) {
                continue;
            }
            expenseDao.insert(remote.toEntity());
            downloaded += 1;
        }

        return new SyncSummary(uploaded, downloaded, conflicts, "Last write wins by updatedAt");
    }

    public static class SyncSummary {
        public final int uploaded;
        public final int downloaded;
        public final int conflictsResolved;
        public final String strategy;

        public SyncSummary(int uploaded, int downloaded, int conflictsResolved, String strategy) {
            this.uploaded = uploaded;
            this.downloaded = downloaded;
            this.conflictsResolved = conflictsResolved;
            this.strategy = strategy;
        }
    }

    public static class RemoteUserProfile {
        public String userId = "";
        public double salary = 0.0;
        public int salaryDate = 1;
        public double monthlySavingsGoal = 0.0;
        public String currency = "INR";
        public long createdDate = 0L;
        public String financialGoals = "";
        public long updatedAt = 0L;

        public RemoteUserProfile() {
        }

        public UserProfileEntity toEntity() {
            return new UserProfileEntity(
                    userId,
                    salary,
                    salaryDate,
                    monthlySavingsGoal,
                    currency,
                    createdDate,
                    financialGoals,
                    updatedAt
            );
        }

        public static RemoteUserProfile from(UserProfileEntity entity) {
            RemoteUserProfile remote = new RemoteUserProfile();
            remote.userId = entity.userId;
            remote.salary = entity.salary;
            remote.salaryDate = entity.salaryDate;
            remote.monthlySavingsGoal = entity.monthlySavingsGoal;
            remote.currency = entity.currency;
            remote.createdDate = entity.createdDate;
            remote.financialGoals = entity.financialGoals;
            remote.updatedAt = entity.updatedAt;
            return remote;
        }
    }

    public static class RemoteFixedExpense {
        public long expenseId = 0;
        public String userId = "";
        public String name = "";
        public String category = "Other";
        public double amount = 0.0;
        public int dueDate = 1;
        public boolean isRecurring = true;
        public long updatedAt = 0L;

        public RemoteFixedExpense() {
        }

        public FixedExpenseEntity toEntity() {
            return new FixedExpenseEntity(
                    expenseId,
                    userId,
                    name,
                    category,
                    amount,
                    dueDate,
                    isRecurring,
                    updatedAt
            );
        }

        public static RemoteFixedExpense from(FixedExpenseEntity entity) {
            RemoteFixedExpense remote = new RemoteFixedExpense();
            remote.expenseId = entity.expenseId;
            remote.userId = entity.userId;
            remote.name = entity.name;
            remote.category = entity.category;
            remote.amount = entity.amount;
            remote.dueDate = entity.dueDate;
            remote.isRecurring = entity.isRecurring;
            remote.updatedAt = entity.updatedAt;
            return remote;
        }
    }

    public static class RemoteExpense {
        public long expenseId = 0;
        public String userId = "";
        public long date = 0L;
        public String category = "OTHER";
        public double amount = 0.0;
        public String description = "";
        public String paymentMethod = "SMS";
        public long updatedAt = 0L;

        public RemoteExpense() {
        }

        public ExpenseEntity toEntity() {
            return new ExpenseEntity(
                    expenseId,
                    userId,
                    date,
                    category,
                    amount,
                    description,
                    paymentMethod,
                    updatedAt
            );
        }

        public static RemoteExpense from(ExpenseEntity entity) {
            RemoteExpense remote = new RemoteExpense();
            remote.expenseId = entity.expenseId;
            remote.userId = entity.userId;
            remote.date = entity.date;
            remote.category = entity.category;
            remote.amount = entity.amount;
            remote.description = entity.description;
            remote.paymentMethod = entity.paymentMethod;
            remote.updatedAt = entity.updatedAt;
            return remote;
        }
    }
}
