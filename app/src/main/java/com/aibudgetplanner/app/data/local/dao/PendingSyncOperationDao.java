package com.aibudgetplanner.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aibudgetplanner.app.data.local.entity.PendingSyncOperationEntity;

import java.util.List;

@Dao
public interface PendingSyncOperationDao {
    @Insert
    void insert(PendingSyncOperationEntity operation);

    @Update
    void update(PendingSyncOperationEntity operation);

    @Query("SELECT * FROM pending_sync_operation ORDER BY createdAt ASC LIMIT :limit")
    List<PendingSyncOperationEntity> getPending(int limit);

    @Query("DELETE FROM pending_sync_operation WHERE id = :id")
    void deleteById(long id);
}
