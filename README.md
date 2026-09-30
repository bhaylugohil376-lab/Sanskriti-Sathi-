Sanskriti Sathi🇮🇳


Sanskriti Sathi — Instagram-style final project
This project is a Firebase-free Android app shell aligned to the approved 24-screen Instagram-style reference.
Approved 24-screen reference
INSTAGRAM_STYLE_FINAL_REFERENCE.png
Screens covered:
Splash / Welcome
Login
Home Feed
Reels
Create Reel
Reel Editor
Profile
Professional Dashboard
Edit Profile
Messages / keyboard-resize chat
Chat List
Explore / Categories
Reel Upload Options
Audio Selection
Filters
Profile Posts/Reels/Tagged tabs
Notifications
Settings
Create Post
Search
Like / Comment / Share
Bottom Navigation
Dark Mode
About / App Info
Backend
Supabase authentication/database remains configured through SupabaseConfig.
Firebase dependencies and Firebase-backed Java code are not present in this project snapshot.
Existing Backblaze B2 media flow is retained through the existing Supabase Edge Function bright-action for profile media.
Reels currently use the existing reels Supabase Storage path in ReelSupabaseHelper. To make reel video storage B2-only, the deployed bright-action Edge Function must expose a B2 video upload/download contract; this project does not contain that server-side function source.
Reels
Vertical RecyclerView + PagerSnapHelper.
Media3 / ExoPlayer.
texture_view PlayerView.
Player pause/release lifecycle handling.
Detailed playback errors in Logcat.
Like, comment, share and delete actions.
Build note
The supplied project does not include the Gradle wrapper executable/JAR. Open it in Android Studio and let Android Studio regenerate/sync the Gradle wrapper before building.
Final Reel/B2 build notes
Reel.java uses Gson @SerializedName; Gson is included in app/build.gradle.
Reel video records use b2://... references and are resolved through the Supabase b2-media Edge Function before ExoPlayer playback.
The APK must have the b2-media Edge Function deployed with B2_APPLICATION_KEY_ID, B2_APPLICATION_KEY, B2_BUCKET_ID, and B2_BUCKET_NAME configured.
SupabaseConfig is intentionally unchanged.
Android Studio can import the project and sync Gradle normally.
