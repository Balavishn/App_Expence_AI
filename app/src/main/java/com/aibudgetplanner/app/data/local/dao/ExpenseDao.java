package com.aibudgetplanner.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aibudgetplanner.app.data.local.entity.ExpenseEntity;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ExpenseEntity expense);

    @Update
    void update(ExpenseEntity expense);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("SELECT * FROM expense WHERE userId = :userId ORDER BY date DESC")
    Flow<List<ExpenseEntity>> observeByUser(String userId);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expense WHERE userId = :userId AND date >= :fromDate")
    Flow<Double> observeSpentFrom(String userId, long fromDate);

    @Query("SELECT * FROM expense WHERE userId = :userId")
    List<ExpenseEntity> getByUser(String userId);
}
