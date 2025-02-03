package com.example.meetup.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;
import com.example.meetup.adapters.MeetupAdapter;
import com.example.meetup.adapters.OnRevokeClickListener;
import com.example.meetup.models.Meetup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetUpsFragment extends Fragment implements OnRevokeClickListener {

    private RecyclerView recyclerViewUpcoming;
    private RecyclerView recyclerViewPast;
    private MeetupAdapter upcomingAdapter;
    private MeetupAdapter pastAdapter;
    private List<Meetup> upcomingList;
    private List<Meetup> pastList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meet_ups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBarMeetups);

        // Initialize RecyclerView for upcoming meetups
        recyclerViewUpcoming = view.findViewById(R.id.recyclerViewUpcoming);
        recyclerViewPast = view.findViewById(R.id.recyclerViewPast);

        // Set layout manager for RecyclerView
        recyclerViewUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPast.setLayoutManager(new LinearLayoutManager(getContext()));

        // Disable nested scrolling for RecyclerViews so that they don't interfere with the parent ScrollView
        recyclerViewUpcoming.setNestedScrollingEnabled(false);
        recyclerViewPast.setNestedScrollingEnabled(false);

        // Initialize lists
        upcomingList = new ArrayList<>();
        pastList = new ArrayList<>();

        // Initialize adapters
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        upcomingAdapter = new MeetupAdapter(upcomingList, this, currentUserId);
        pastAdapter = new MeetupAdapter(pastList, this, currentUserId);

        // Set adapters to RecyclerViews
        recyclerViewUpcoming.setAdapter(upcomingAdapter);
        recyclerViewPast.setAdapter(pastAdapter);

        db = FirebaseFirestore.getInstance();
        //used for specification of the current user
        mAuth = FirebaseAuth.getInstance();

        fetchUserMeetups();
    }

    /**
     * Fetches all meetups from Firestore that the current user is a participant of and updates the UI.
     */
    private void fetchUserMeetups() {
        String userId = mAuth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);

        db.collection("meetups")
                .whereArrayContains("participants", userId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Clear previous data
                        upcomingList.clear();
                        pastList.clear();

                        // Date format and current date for comparison
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date currentDate = new Date();

                        // Add meetups to the appropriate list based on date
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Meetup meetup = document.toObject(Meetup.class);
                            try {
                                Date meetupDate = sdf.parse(meetup.getDate());
                                if (meetupDate != null) {
                                    if (meetupDate.after(currentDate)) {
                                        // Future meetup
                                        upcomingList.add(meetup);
                                    } else {
                                        // Past meetup
                                        pastList.add(meetup);
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        // Notify adapters of the data change
                        upcomingAdapter.notifyDataSetChanged();
                        pastAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("MeetUpsFragment", "Error getting documents.", task.getException());
                        Toast.makeText(getContext(), "Failed to load meetups.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRevokeClick(Meetup meetup) {
        String userId = mAuth.getCurrentUser().getUid();
        String meetupId = meetup.getId();

        // Remove user from 'participants' array in the meetup document
        db.collection("meetups").document(meetupId)
                .update("participants", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    // Remove user from 'signups' subcollection in the meetup document
                    db.collection("meetups").document(meetupId)
                            .collection("signups").document(userId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Successfully revoked sign-up.", Toast.LENGTH_SHORT).show();
                                fetchUserMeetups();  // Refresh lists after revocation

                                // Remove user from 'members' array in all groups of this meetup
                                db.collection("meetups").document(meetupId)
                                        .collection("groups")
                                        .whereArrayContains("members", userId)
                                        .get()
                                        .addOnSuccessListener(groupsSnapshot -> {
                                            for (DocumentSnapshot groupDoc : groupsSnapshot.getDocuments()) {
                                                groupDoc.getReference().update("members", FieldValue.arrayRemove(userId));
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Revoked sign-up, but failed to remove from signups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                fetchUserMeetups();  // Refresh anyway
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to revoke sign-up: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}