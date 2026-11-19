package com.sanskritisathi.app;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainNavigationUiTest {
    private static final String PACKAGE = "com.sanskritisathi.app";
    private UiDevice device;

    @Before
    public void setUp() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();
        Intent intent = new Intent();
        intent.setClassName(PACKAGE, PACKAGE + ".MainActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        InstrumentationRegistry.getInstrumentation().getTargetContext().startActivity(intent);
        assertTrue("Home screen did not open",
                device.wait(Until.hasObject(By.res(PACKAGE, "homeFeedRecyclerView")), 15000));
    }

    private void tapAndVerify(String sourceId, String destinationId) throws Exception {
        assertTrue("Missing control: " + sourceId,
                device.wait(Until.hasObject(By.res(PACKAGE, sourceId)), 5000));
        device.findObject(By.res(PACKAGE, sourceId)).click();
        assertTrue("Expected screen did not open: " + destinationId,
                device.wait(Until.hasObject(By.res(PACKAGE, destinationId)), 8000));
        device.pressBack();
        assertTrue("Did not return to Home after " + sourceId,
                device.wait(Until.hasObject(By.res(PACKAGE, "homeFeedRecyclerView")), 5000));
    }

    @Test
    public void storyAndKnowledgeSectionsOpen() throws Exception {
        tapAndVerify("yourStoryButton", "galleryButton");
        tapAndVerify("templeStoryButton", "templeRecyclerView");
        tapAndVerify("rajaStoryButton", "rajaRecyclerView");
        tapAndVerify("deviStoryButton", "deviDevtaRecyclerView");
        tapAndVerify("gitaStoryButton", "gitaRecyclerView");
    }

    @Test
    public void bottomNavigationScreensOpen() throws Exception {
        tapAndVerify("exploreNavButton", "exploreSearchInput");
        tapAndVerify("reelsNavButton", "recyclerViewReels");
        tapAndVerify("chatNavButton", "searchBar");
        tapAndVerify("notificationButton", "notificationsRecyclerView");
    }

    @Test
    public void createMenuOpensPostAndStoryFlows() throws Exception {
        assertTrue("Create button missing",
                device.wait(Until.hasObject(By.res(PACKAGE, "createTopButton")), 5000));
        device.findObject(By.res(PACKAGE, "createTopButton")).click();
        assertTrue("Create Post menu item missing", device.wait(Until.hasObject(By.text("Create Post")), 4000));
        device.findObject(By.text("Create Post")).click();
        assertTrue("Post upload screen did not open",
                device.wait(Until.hasObject(By.res(PACKAGE, "postCaptionInput")), 8000));
        device.pressBack();

        device.findObject(By.res(PACKAGE, "createTopButton")).click();
        assertTrue("Create Story menu item missing", device.wait(Until.hasObject(By.text("Create Story")), 4000));
        device.findObject(By.text("Create Story")).click();
        assertTrue("Story upload screen did not open",
                device.wait(Until.hasObject(By.res(PACKAGE, "galleryButton")), 8000));
    }
}