<?xml version="1.0" encoding="utf-8"?>

<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="10dp"
    android:layout_marginEnd="10dp"
    android:layout_marginTop="8dp"
    android:layout_marginBottom="8dp"
    app:cardBackgroundColor="#FFFFFF"
    app:cardCornerRadius="18dp"
    app:cardElevation="2dp"
    app:strokeColor="#EAD8C6"
    app:strokeWidth="1dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- PROFILE HEADER -->

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center_vertical"
            android:orientation="horizontal"
            android:paddingStart="14dp"
            android:paddingTop="13dp"
            android:paddingEnd="8dp"
            android:paddingBottom="10dp">

            <ImageView
                android:id="@+id/postProfileImage"
                android:layout_width="44dp"
                android:layout_height="44dp"
                android:background="@drawable/circle_background"
                android:contentDescription="Profile"
                android:padding="2dp"
                android:scaleType="centerCrop"
                android:src="@drawable/icon_foreground" />

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="11dp"
                android:orientation="vertical">

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/postAuthor"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Sanskriti Sathi"
                        android:textColor="#2E2118"
                        android:textSize="15sp"
                        android:textStyle="bold"
                        android:maxLines="1"
                        android:ellipsize="end" />

                    <TextView
                        android:id="@+id/followStatus"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="5dp"
                        android:background="?attr/selectableItemBackground"
                        android:clickable="true"
                        android:focusable="true"
                        android:text=" • Follow"
                        android:textColor="#A45D20"
                        android:textSize="12sp" />

                </LinearLayout>

                <TextView
                    android:id="@+id/postCategory"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="2dp"
                    android:text="संस्कृति"
                    android:textColor="#A45D20"
                    android:textSize="12sp" />

            </LinearLayout>

            <TextView
                android:id="@+id/postMenu"
                android:layout_width="42dp"
                android:layout_height="42dp"
                android:gravity="center"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:clickable="true"
                android:focusable="true"
                android:text="⋮"
                android:textColor="#4B3A2D"
                android:textSize="25sp" />

        </LinearLayout>


        <!-- POST IMAGE -->

        <ImageView
            android:id="@+id/postImage"
            android:layout_width="match_parent"
            android:layout_height="260dp"
            android:adjustViewBounds="true"
            android:background="#F4E8DC"
            android:contentDescription="Post"
            android:scaleType="centerCrop" />


        <!-- ACTION BUTTONS -->

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:gravity="center_vertical"
            android:orientation="horizontal"
            android:paddingStart="8dp"
            android:paddingEnd="8dp">

            <TextView
                android:id="@+id/likeButton"
                android:layout_width="0dp"
                android:layout_height="match_parent"
                android:layout_weight="1"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:focusable="true"
                android:gravity="center"
                android:text="♡  Like"
                android:textColor="#33251B"
                android:textSize="13sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/commentButton"
                android:layout_width="0dp"
                android:layout_height="match_parent"
                android:layout_weight="1"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:focusable="true"
                android:gravity="center"
                android:text="💬  Comment"
                android:textColor="#33251B"
                android:textSize="13sp" />

            <TextView
                android:id="@+id/shareButton"
                android:layout_width="0dp"
                android:layout_height="match_parent"
                android:layout_weight="1"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:focusable="true"
                android:gravity="center"
                android:text="↗  Share"
                android:textColor="#33251B"
                android:textSize="13sp" />

            <TextView
                android:id="@+id/saveButton"
                android:layout_width="52dp"
                android:layout_height="match_parent"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:focusable="true"
                android:gravity="center"
                android:text="♡"
                android:textColor="#33251B"
                android:textSize="22sp" />

        </LinearLayout>


        <!-- LIKE COUNT -->

        <TextView
            android:id="@+id/likeCount"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingStart="14dp"
            android:paddingTop="2dp"
            android:paddingEnd="14dp"
            android:paddingBottom="3dp"
            android:text="❤️ 0 likes"
            android:textColor="#5E4A3B"
            android:textSize="12sp"
            android:textStyle="bold" />


        <!-- CAPTION -->

        <TextView
            android:id="@+id/postCaption"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingStart="14dp"
            android:paddingTop="4dp"
            android:paddingEnd="14dp"
            android:paddingBottom="12dp"
            android:text="भारतीय संस्कृति और विरासत"
            android:textColor="#302218"
            android:textSize="14sp"
            android:lineSpacingExtra="2dp" />


        <!-- DELETE -->

        <TextView
            android:id="@+id/deletePostButton"
            android:layout_width="match_parent"
            android:layout_height="44dp"
            android:layout_marginStart="14dp"
            android:layout_marginEnd="14dp"
            android:layout_marginBottom="12dp"
            android:background="?attr/selectableItemBackground"
            android:gravity="center"
            android:text="🗑 Delete Post"
            android:textColor="#D32F2F"
            android:textSize="13sp"
            android:textStyle="bold"
            android:visibility="gone" />

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
