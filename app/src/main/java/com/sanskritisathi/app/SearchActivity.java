package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private TextView resultText;
    private TextView emptyText;
    private RecyclerView searchRecyclerView;

    private SearchAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        searchInput = findViewById(
                R.id.searchInput
        );

        resultText = findViewById(
                R.id.searchResultText
        );

        emptyText = findViewById(
                R.id.searchEmptyText
        );

        searchRecyclerView = findViewById(
                R.id.searchRecyclerView
        );

        ImageButton backButton = findViewById(
                R.id.searchBackButton
        );

        searchRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        searchRecyclerView.setHasFixedSize(false);

        adapter = new SearchAdapter(this);

        searchRecyclerView.setAdapter(adapter);

        backButton.setOnClickListener(
                v -> finish()
        );

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Pehle Login karein.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        showInitialState();

        searchInput.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        String query =
                                s == null
                                        ? ""
                                        : s.toString();

                        searchUsers(query);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    private void showInitialState() {

        resultText.setText(
                "Search users"
        );

        emptyText.setText(
                "Name ya username search karein."
        );

        emptyText.setVisibility(
                View.VISIBLE
        );

        searchRecyclerView.setVisibility(
                View.GONE
        );
    }

    private void searchUsers(String query) {

        String search =
                query == null
                        ? ""
                        : query.trim()
                        .toLowerCase(Locale.ROOT);

        if (search.isEmpty()) {

            adapter.setUsers(
                    new ArrayList<>()
            );

            showInitialState();

            return;
        }

        if (search.length() < 2) {

            resultText.setText(
                    "Search users"
            );

            emptyText.setText(
                    "Kam se kam 2 characters type karein."
            );

            emptyText.setVisibility(
                    View.VISIBLE
            );

            searchRecyclerView.setVisibility(
                    View.GONE
            );

            return;
        }

        if (searching) {
            // Previous query may still be running.
            // Firestore result will simply update the latest UI.
        }

        searching = true;

        resultText.setText(
                "Searching..."
        );

        emptyText.setVisibility(
                View.GONE
        );

        searchRecyclerView.setVisibility(
                View.VISIBLE
        );

        String end =
                search + "\uf8ff";

        firestore.collection("users")
                .whereGreaterThanOrEqualTo(
                        "usernameLower",
                        search
                )
                .whereLessThanOrEqualTo(
                        "usernameLower",
                        end
                )
                .limit(20)
                .get()
                .addOnSuccessListener(
                        usernameDocuments -> {

                            searching = false;

                            List<SearchAdapter.UserSearchItem>
                                    results =
                                    new ArrayList<>();

                            for (
                                    DocumentSnapshot document
                                    : usernameDocuments
                            ) {

                                addUserIfValid(
                                        results,
                                        document
                                );
                            }

                            showResults(
                                    results,
                                    search
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            searching = false;

                            Toast.makeText(
                                    SearchActivity.this,
                                    "Search failed. Firestore check karein.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            resultText.setText(
                                    "Search unavailable"
                            );

                            searchRecyclerView.setVisibility(
                                    View.GONE
                            );

                            emptyText.setVisibility(
                                    View.VISIBLE
                            );

                            emptyText.setText(
                                    "Users search nahi ho paaye.\n"
                                            + "Internet connection check karein."
                            );
                        }
                );
    }

    private void addUserIfValid(
            List<SearchAdapter.UserSearchItem> results,
            DocumentSnapshot document
    ) {

        String uid =
                document.getId();

        if (uid.isEmpty()) {
            return;
        }

        FirebaseAuth currentAuth =
                FirebaseAuth.getInstance();

        if (
                currentAuth.getCurrentUser() != null
                        && uid.equals(
                        currentAuth
                                .getCurrentUser()
                                .getUid()
                )
        ) {
            return;
        }

        String name =
                getStringValue(
                        document,
                        "name"
                );

        String username =
                getStringValue(
                        document,
                        "username"
                );

        String bio =
                getStringValue(
                        document,
                        "bio"
                );

        results.add(
                new SearchAdapter.UserSearchItem(
                        uid,
                        name,
                        username,
                        bio
                )
        );
    }

    private String getStringValue(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(field);

        return value == null
                ? ""
                : value.trim();
    }

    private void showResults(
            List<SearchAdapter.UserSearchItem> results,
            String query
    ) {

        adapter.setUsers(results);

        if (results.isEmpty()) {

            searchRecyclerView.setVisibility(
                    View.GONE
            );

            emptyText.setVisibility(
                    View.VISIBLE
            );

            emptyText.setText(
                    "No users found for \""
                            + query
                            + "\""
            );

            resultText.setText(
                    "0 users"
            );

        } else {

            searchRecyclerView.setVisibility(
                    View.VISIBLE
            );

            emptyText.setVisibility(
                    View.GONE
            );

            resultText.setText(
                    results.size()
                            + (
                            results.size() == 1
                                    ? " user found"
                                    : " users found"
                    )
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (searchInput != null) {
            searchInput.postDelayed(
                    () -> {
                        if (!isFinishing()) {
                            searchInput.requestFocus();

                            InputMethodManager imm =
                                    (InputMethodManager)
                                            getSystemService(
                                                    Context.INPUT_METHOD_SERVICE
                                            );

                            if (imm != null) {
                                imm.showSoftInput(
                                        searchInput,
                                        InputMethodManager.SHOW_IMPLICIT
                                );
                            }
                        }
                    },
                    250
            );
        }
    }
}
