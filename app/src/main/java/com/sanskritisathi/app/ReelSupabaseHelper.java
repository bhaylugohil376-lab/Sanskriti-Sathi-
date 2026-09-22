// ============================================================
// GET ACTIVE REELS
// ============================================================

public interface ReelsCallback {

    void onSuccess(java.util.List<Reel> reels);

    void onError(String message);
}

public static void getActiveReels(
        Context context,
        ReelsCallback callback) {

    if (context == null) {
        if (callback != null) {
            MAIN_HANDLER.post(() ->
                    callback.onError("Context missing.")
            );
        }
        return;
    }

    EXECUTOR.execute(() -> {

        try {

            String userId =
                    SupabaseAuthManager.getUserId(context);

            String accessToken =
                    SupabaseAuthManager.getAccessToken(context);

            if (TextUtils.isEmpty(userId)
                    || TextUtils.isEmpty(accessToken)) {

                MAIN_HANDLER.post(() ->
                        callback.onError(
                                "Login session nahi mili."
                        )
                );

                return;
            }

            /*
             * Public Reels + current user's own Reels.
             *
             * Followers visibility ko next step me
             * actual follow-table/schema ke according
             * properly add karenge.
             */
            String filter =
                    "or=("
                            + "visibility.eq.Public,"
                            + "user_id.eq."
                            + URLEncoder.encode(
                                    userId,
                                    "UTF-8"
                            )
                            + ")";

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/reels"
                            + "?select=*"
                            + "&"
                            + filter
                            + "&order=created_at.desc";

            HttpURLConnection connection =
                    openConnection(
                            url,
                            "GET",
                            accessToken
                    );

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(
                            connection,
                            code
                    );

            connection.disconnect();

            if (code < 200 || code >= 300) {

                MAIN_HANDLER.post(() ->
                        callback.onError(
                                "Reels load failed: "
                                        + response
                        )
                );

                return;
            }

            JSONArray array =
                    new JSONArray(response);

            java.util.List<Reel> result =
                    new java.util.ArrayList<>();

            for (int i = 0;
                 i < array.length();
                 i++) {

                JSONObject json =
                        array.getJSONObject(i);

                String reelId =
                        json.optString(
                                "id",
                                ""
                        );

                String reelUserId =
                        json.optString(
                                "user_id",
                                ""
                        );

                String videoUrl =
                        json.optString(
                                "video_url",
                                ""
                        );

                String caption =
                        json.optString(
                                "caption",
                                ""
                        );

                String visibility =
                        json.optString(
                                "visibility",
                                "Public"
                        );

                int likes =
                        json.optInt(
                                "likes",
                                0
                        );

                int comments =
                        json.optInt(
                                "comments",
                                0
                        );

                int views =
                        json.optInt(
                                "views",
                                0
                        );

                boolean ownReel =
                        userId.equals(
                                reelUserId
                        );

                String username =
                        json.optString(
                                "username",
                                ""
                        );

                /*
                 * Agar reels table me username nahi hai,
                 * profiles se username next step me load
                 * karenge.
                 */
                Reel reel =
                        new Reel(
                                reelId,
                                reelUserId,
                                username,
                                videoUrl,
                                caption,
                                visibility,
                                likes,
                                comments,
                                views,
                                ownReel
                        );

                result.add(reel);
            }

            MAIN_HANDLER.post(() ->
                    callback.onSuccess(result)
            );

        } catch (Exception e) {

            MAIN_HANDLER.post(() ->
                    callback.onError(
                            getSafeError(
                                    e,
                                    "Reels load nahi hui."
                            )
                    )
            );
        }
    });
}
