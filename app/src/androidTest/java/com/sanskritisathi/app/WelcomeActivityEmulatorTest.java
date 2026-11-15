package com.sanskritisathi.app;

import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WelcomeActivityEmulatorTest {

    @Test
    public void welcomeScreenStartsAndShowsAllLanguageChoices() {
        Context targetContext =
                InstrumentationRegistry.getInstrumentation().getTargetContext();

        targetContext.getSharedPreferences(
                "SanskritiSathiPrefs", Context.MODE_PRIVATE
        ).edit().clear().commit();

        try (ActivityScenario<WelcomeActivity> scenario =
                     ActivityScenario.launch(WelcomeActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull("Hindi language button should be visible",
                        activity.findViewById(R.id.hindiButton));
                assertNotNull("English language button should be visible",
                        activity.findViewById(R.id.englishButton));
                assertNotNull("Gujarati language button should be visible",
                        activity.findViewById(R.id.gujaratiButton));
            });
        }
    }
}
