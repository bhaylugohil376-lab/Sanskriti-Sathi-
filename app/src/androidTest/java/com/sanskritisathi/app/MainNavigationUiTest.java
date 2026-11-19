package com.sanskritisathi.app;

import android.content.Intent;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import android.test.InstrumentationTestCase;

public class MainNavigationUiTest extends InstrumentationTestCase {
    private static final String PACKAGE = "com.sanskritisathi.app";
    private UiDevice device;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        device = UiDevice.getInstance(getInstrumentation());
        device.pressHome();
        Intent intent = new Intent();
        intent.setClassName(PACKAGE, PACKAGE + ".MainActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getInstrumentation().getTargetContext().startActivity(intent);
        assertTrue("Home screen did not open", device.wait(Until.hasObject(By.res(PACKAGE, "homeFeedRecyclerView")), 10000));
    }

    private void tapAndVerify(String sourceId, String destinationId) throws Exception {
        assertTrue("Missing or non-clickable control: " + sourceId,
                device.wait(Until.hasObject(By.res(PACKAGE, sourceId)), 5000));
        device.findObject(By.res(PACKAGE, sourceId)).click();
        assertTrue("Expected screen did not open: " + destinationId,
                device.wait(Until.hasObject(By.res(PACKAGE, destinationId)), 8000));
        device.pressBack();
        assertTrue("Did not return to Home after " + sourceId,
                device.wait(Until.hasObject(By.res(PACKAGE, "homeFeedRecyclerView")), 5000));
    }

    public void testStoryAndKnowledgeSectionsOpen() throws Exception {
        tapAndVerify("yourStoryButton", "galleryButton");
        tapAndVerify("templeStoryButton", "templeRecyclerView");
        tapAndVerify("rajaStoryButton", "rajaRecyclerView");
        tapAndVerify("deviStoryButton", "deviDevtaRecyclerView");
        tapAndVerify("gitaStoryButton", "gitaRecyclerView");
    }

    public void testBottomNavigationScreensOpen() throws Exception {
        tapAndVerify("exploreNavButton", "exploreSearchInput");
        tapAndVerify("reelsNavButton", "recyclerViewReels");
        tapAndVerify("chatNavButton", "searchBar");
        tapAndVerify("notificationButton", "notificationsRecyclerView");
    }

    public void testCreateMenuOpensPostAndStoryFlows() throws Exception {
        device.findObject(By.res(PACKAGE, "createTopButton")).click();
        assertTrue("Create menu did not appear",
                device.wait(Until.hasObject(By.text("Create Post")), 4000));
        device.findObject(By.text("Create Post")).click();
        assertTrue("Post upload screen did not open",
                device.wait(Until.hasObject(By.res(PACKAGE, "postDescriptionInput")), 8000));
        device.pressBack();

        device.findObject(By.res(PACKAGE, "createTopButton")).click();
        assertTrue("Create menu did not appear for Story",
                device.wait(Until.hasObject(By.text("Create Story")), 4000));
        device.findObject(By.text("Create Story")).click();
        assertTrue("Story upload screen did not open",
                device.wait(Until.hasObject(By.res(PACKAGE, "galleryButton")), 8000));
    }
}
