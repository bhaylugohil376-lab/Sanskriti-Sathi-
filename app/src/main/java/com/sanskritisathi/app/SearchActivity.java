package com.sanskritisathi.app;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private TextView searchResultText;
    private TextView searchEmptyText;
    private RecyclerView searchRecyclerView;
    private SearchUserAdapter adapter;
    private boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInput = findViewById(R.id.searchInput);
        searchResultText = findViewById(R.id.searchResultText);
        searchEmptyText = findViewById(R.id.searchEmptyText);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchUserAdapter(this);
        searchRecyclerView.setAdapter(adapter);

        View back = findViewById(R.id.searchBackButton);
        if (back != null) back.setOnClickListener(v -> finish());

        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                performSearch();
                return true;
            }
            return false;
        });

        String initialQuery = getIntent().getStringExtra("search_query");
        if (initialQuery != null && !initialQuery.trim().isEmpty()) {
            searchInput.setText(initialQuery);
            searchInput.setSelection(searchInput.length());
            performSearch();
        } else {
            showEmpty("Search users by name or username.");
            searchInput.requestFocus();
        }
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        hideKeyboard();

        if (query.isEmpty()) {
            adapter.setUsers(new ArrayList<>());
            showEmpty("Name ya username search karein.");
            searchResultText.setText("Search users");
            return;
        }

        if (searching) return;

        String token = SupabaseAuthManager.getAccessToken(this);
        if (token == null || token.trim().isEmpty()) {
            adapter.setUsers(new ArrayList<>());
            showEmpty("Search karne ke liye login karein.");
            searchResultText.setText("Login required");
            return;
        }

        searching = true;
        adapter.setUsers(new ArrayList<>());
        searchResultText.setText("Searching Supabase...");
        searchEmptyText.setVisibility(View.VISIBLE);
        searchEmptyText.setText("Please wait...");

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String filter = "or=(username.ilike.*" + query
                        + "*,name.ilike.*" + query + "*)";
                String endpoint = SupabaseConfig.PROJECT_URL
                        + "/rest/v1/profiles?select=id,username,name,profile_image_url"
                        + "&" + URLEncoder.encode(filter, "UTF-8")
                        + "&limit=30&order=username.asc";

                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Accept", "application/json");

                int status = connection.getResponseCode();
                String response = readResponse(connection, status);
                if (status < 200 || status >= 300) {
                    throw new Exception("Supabase search failed (HTTP " + status + "): "
                            + extractError(response));
                }

                JSONArray rows = new JSONArray(response);
                List<SearchUser> users = new ArrayList<>();
                String currentUserId = SupabaseAuthManager.getUserId(this);

                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    String id = row.optString("id", "");
                    if (id.isEmpty() || id.equals(currentUserId)) continue;
                    users.add(new SearchUser(
                            id,
                            row.optString("username", ""),
                            row.optString("name", "Sanskriti Sathi member"),
                            row.optString("profile_image_url", "")
                    ));
                }

                runOnUiThread(() -> {
                    searching = false;
                    adapter.setUsers(users);
                    searchResultText.setText("Results for \"" + query + "\"");
                    if (users.isEmpty()) showEmpty("Koi matching profile nahi mila.");
                    else searchEmptyText.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    searching = false;
                    adapter.setUsers(new ArrayList<>());
                    searchResultText.setText("Search unavailable");
                    showEmpty(e.getMessage() == null
                            ? "Supabase search error." : e.getMessage());
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private String readResponse(HttpURLConnection connection, int status) throws Exception {
        InputStream stream = status >= 200 && status < 400
                ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private String extractError(String response) {
        try {
            JSONObject json = new JSONObject(response);
            return json.optString("message",
                    json.optString("hint", "Request rejected"));
        } catch (Exception ignored) {
            return response == null || response.isEmpty()
                    ? "Request rejected" : response;
        }
    }

    private void showEmpty(String message) {
        searchEmptyText.setText(message);
        searchEmptyText.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        if (searchInput == null) return;
        InputMethodManager manager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
        searchInput.clearFocus();
    }

    @Override
    protected void onPause() {
        hideKeyboard();
        super.onPause();
    }
}
