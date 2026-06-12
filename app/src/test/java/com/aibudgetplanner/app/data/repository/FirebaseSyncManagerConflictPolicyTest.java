package com.aibudgetplanner.app.data.repository;

import org.junit.Assert;
import org.junit.Test;

public class FirebaseSyncManagerConflictPolicyTest {

    @Test
    public void shouldUseLocalVersion_whenRemoteMissing_returnsTrue() {
        Assert.assertTrue(FirebaseSyncManager.shouldUseLocalVersion(100L, null));
    }

    @Test
    public void shouldUseLocalVersion_whenLocalIsNewer_returnsTrue() {
        Assert.assertTrue(FirebaseSyncManager.shouldUseLocalVersion(200L, 100L));
    }

    @Test
    public void shouldUseLocalVersion_whenRemoteIsNewer_returnsFalse() {
        Assert.assertFalse(FirebaseSyncManager.shouldUseLocalVersion(100L, 200L));
    }
}
