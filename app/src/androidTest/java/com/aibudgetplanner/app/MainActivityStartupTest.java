package com.aibudgetplanner.app;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MainActivityStartupTest {

    @Rule
    public final ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void resetLockState() {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences("app_lock_preferences", android.content.Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void startup_showsFragmentContainer() {
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
    }
}
