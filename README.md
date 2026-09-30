Sanskriti Sathi
— Firebase-free Supabase Reel build
This project snapshot removes Firebase dependencies and Firebase-backed sections from the Android app.
Backend
Supabase authentication: SupabaseAuthManager
Supabase configuration: SupabaseConfig
Reels database/storage: ReelSupabaseHelper
Reel comments: ReelCommentSupabaseHelper
Reel UI
Instagram-style full-screen vertical feed
PagerSnapHelper
Media3/ExoPlayer
PlayerView with texture_view
Supabase Storage playback headers
pause/release lifecycle handling
Reel upload, like, comment, share and delete
Important
Open app/src/main/java/com/sanskritisathi/app/SupabaseConfig.java and keep the existing project URL/key configuration. Do not replace it with Firebase configuration.
The Gradle wrapper executable was not present in the supplied project snapshot, so build from Android Studio or from a machine with Gradle installed.
