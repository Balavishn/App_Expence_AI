package com.aibudgetplanner.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.aibudgetplanner.app.data.local.entity.UserProfileEntity;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserProfileEntity profile);

    @Query("SELECT * FROM user_profile LIMIT 1")
    Flow<UserProfileEntity> observeProfile();

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    UserProfileEntity getByUser(String userId);
}
