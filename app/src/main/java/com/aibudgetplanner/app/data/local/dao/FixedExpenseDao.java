package com.aibudgetplanner.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aibudgetplanner.app.data.local.entity.FixedExpenseEntity;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface FixedExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FixedExpenseEntity expense);

    @Update
    void update(FixedExpenseEntity expense);

    @Delete
    void delete(FixedExpenseEntity expense);

    @Query("SELECT * FROM fixed_expense WHERE userId = :userId")
    Flow<List<FixedExpenseEntity>> observeByUser(String userId);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM fixed_expense WHERE userId = :userId")
    Flow<Double> observeTotalByUser(String userId);

    @Query("SELECT * FROM fixed_expense WHERE userId = :userId")
    List<FixedExpenseEntity> getByUser(String userId);
}
