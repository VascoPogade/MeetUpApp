package com.example.meetup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.meetup.R;
import com.example.meetup.activities.SignInActivity;
import com.example.meetup.models.Group;
import com.example.meetup.models.User;
import com.example.meetup.viewmodels.UserViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button buttonThursday, buttonSunday, buttonFriday, buttonLogout;
    // Group size for each Meetup group
    private final int GROUP_SIZE = 6;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        buttonThursday = view.findViewById(R.id.buttonThursday8pm);
        buttonSunday = view.findViewById(R.id.buttonSunday2pm);
        buttonFriday = view.findViewById(R.id.buttonFriday8pm);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Initialize upcoming meetups for the next 4 weeks
        initializeUpcomingMeetups(4);

        // Set onClick listeners
        buttonThursday.setOnClickListener(v -> joinMeetup("Thursday", "8pm"));
        buttonSunday.setOnClickListener(v -> joinMeetup("Sunday", "2pm"));
        buttonFriday.setOnClickListener(v -> joinMeetup("Friday", "8pm"));

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
            Log.i("Logout", "User signed out.");
            getActivity().finish();
        });
    }

    /**
     * Initializes the meetups collection with predefined meetups if they don't exist.
     * @param numberOfWeeksAhead The number of weeks to create Meetups for.
     */
    private void initializeUpcomingMeetups(int numberOfWeeksAhead) {
        CollectionReference meetupsRef = db.collection("meetups");
        if (mAuth.getCurrentUser() == null) {
            Log.e("HomeFragment", "User not authenticated.");
            return;
        }

        // Define Meetup days and times
        String[][] meetupSchedules = {
                {"Thursday", "8pm"},
                {"Sunday", "2pm"},
                {"Friday", "8pm"}
        };

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int week = 0; week < numberOfWeeksAhead; week++) {
            for (String[] schedule : meetupSchedules) {
                String day = schedule[0];
                String time = schedule[1];

                Calendar meetupDate = (Calendar) calendar.clone();
                meetupDate.add(Calendar.WEEK_OF_YEAR, week);

                // Set the day of the week
                switch (day) {
                    case "Sunday":
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        break;
                    case "Thursday":
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                        break;
                    case "Friday":
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid day: " + day);
                }


                String dateStr = sdf.format(meetupDate.getTime());

                // Construct the unique Meetup ID
                String meetupId = day + "_" + dateStr + "_" + time.replace(" ", "");

                // Check if Meetup already exists
                meetupsRef.document(meetupId).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create Meetup document
                        Map<String, Object> meetup = new HashMap<>();
                        meetup.put("day", day);
                        meetup.put("time", time);
                        meetup.put("date", dateStr);
                        meetup.put("status", "open");
                        meetup.put("groupSize", GROUP_SIZE);
                        meetup.put("participants", new ArrayList<String>());
                        meetup.put("createdAt", FieldValue.serverTimestamp());

                        meetupsRef.document(meetupId)
                                .set(meetup)
                                .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Meetup created: " + meetupId))
                                .addOnFailureListener(e -> Log.e("HomeFragment", "Error creating Meetup: ", e));
                    } else {
                        Log.d("HomeFragment", "Meetup already exists: " + meetupId);
                    }
                }).addOnFailureListener(e -> Log.e("HomeFragment", "Error checking Meetup existence: ", e));
            }
        }
    }

    /**
     * Allows the user to join a Meetup.
     * @param day The day of the Meetup.
     * @param time The time of the Meetup.
     */
    private void joinMeetup(String day, String time) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference meetupsRef = db.collection("meetups");

        // Calculate the correct date for the selected day in the current week
        String dateStr;
        try {
            dateStr = getDateForDayInCurrentWeek(day);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), "Invalid meetup day selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construct the Meetup ID
        String meetupId = day + "_" + dateStr + "_" + time.replace(" ", "");

        meetupsRef.document(meetupId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot meetupDoc = task.getResult();
                if (meetupDoc.exists()) {
                    String status = meetupDoc.getString("status");

                    if ("closed".equals(status)) {
                        Toast.makeText(getContext(), "Meetup is closed for signups.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add user to signups subcollection
                    Map<String, Object> signup = new HashMap<>();
                    signup.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("meetups").document(meetupId)
                            .collection("signups").document(userId)
                            .set(signup)
                            .addOnCompleteListener(signupTask -> {
                                if (signupTask.isSuccessful()) {
                                    // Update participants array after successful signup
                                    db.collection("meetups").document(meetupId)
                                            .update("participants", FieldValue.arrayUnion(userId))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Successfully signed up and added to participants!", Toast.LENGTH_SHORT).show();
                                                // Attempt group assignment
                                                attemptGroupAssignment(meetupId, userId);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Signed up, but failed to update participants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Toast.makeText(getContext(), "Failed to sign up: " + signupTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Meetup not found for this week.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error fetching Meetup: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Calculates the date string for the specified day in the current week.
     *
     * @param day The day of the week (e.g., "Sunday", "Thursday", "Friday").
     * @return The date string in "yyyy-MM-dd" format.
     */
    private String getDateForDayInCurrentWeek(String day) {
        Calendar calendar = Calendar.getInstance();
        // Set to the start of the week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Find the desired day within the current week
        switch (day) {
            case "Sunday":
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case "Thursday":
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                break;
            case "Friday":
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                break;
            default:
                throw new IllegalArgumentException("Invalid day: " + day);
        }

        // Format the date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }


    /**
     * Attempts to assign the user to an existing group based on interests or creates a new group if possible.
     * @param meetupId The ID of the Meetup.
     * @param userId The ID of the user.
     */
    private void attemptGroupAssignment(String meetupId, String userId) {
        // Fetch the user's interests
        db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                User user = userDoc.toObject(User.class);
                if (user == null) {
                    Toast.makeText(getContext(), "User profile incomplete.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> userInterests = user.getInterests();
                if (userInterests == null || userInterests.isEmpty()) {
                    Toast.makeText(getContext(), "Please update your interests to be assigned to a group.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Find groups with available spots and matching interests
                CollectionReference groupsRef = db.collection("meetups").document(meetupId).collection("groups");
                groupsRef.whereArrayContains("commonInterests", userInterests.get(0)) // simple matching for now
                        .get().addOnSuccessListener(groupsSnapshot -> {
                            boolean assigned = false;
                            for (DocumentSnapshot groupDoc : groupsSnapshot.getDocuments()) {
                                List<String> members = (List<String>) groupDoc.get("members");
                                List<String> commonInterests = (List<String>) groupDoc.get("commonInterests");
                                if (members != null && members.size() < GROUP_SIZE && hasCommonInterests(userInterests, commonInterests)) {
                                    // Assign to this group
                                    groupsRef.document(groupDoc.getId()).update("members", FieldValue.arrayUnion(userId))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Assigned to an existing group.", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Failed to assign to group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                    assigned = true;
                                    break;
                                }
                            }

                            if (!assigned) {
                                // Check if enough users are available to form a new group
                                db.collection("meetups").document(meetupId).collection("signups")
                                        .get().addOnSuccessListener(signupsSnapshot -> {
                                            int totalSignups = signupsSnapshot.size();
                                            CollectionReference existingGroupsRef = db.collection("meetups").document(meetupId).collection("groups");
                                            existingGroupsRef.get().addOnSuccessListener(existingGroupsSnapshot -> {
                                                int existingGroups = existingGroupsSnapshot.size();
                                                int requiredSignupsForNewGroup = GROUP_SIZE;

                                                if (totalSignups >= (existingGroups + 1) * GROUP_SIZE) {
                                                    // Fetch users to form a new group
                                                    List<String> groupMembers = new ArrayList<>();
                                                    for (DocumentSnapshot signupDoc : signupsSnapshot.getDocuments()) {
                                                        String uid = signupDoc.getId();
                                                        if (!isUserInAnyGroup(uid, existingGroupsSnapshot)) {
                                                            groupMembers.add(uid);
                                                            if (groupMembers.size() == GROUP_SIZE) break;
                                                        }
                                                    }

                                                    if (groupMembers.size() == GROUP_SIZE) {
                                                        // Determine common interests
                                                        determineCommonInterests(groupMembers, meetupId);
                                                    } else {
                                                        // Not enough ungrouped users to form a new group
                                                        Toast.makeText(getContext(), "Waiting for more signups to form a new group.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    // Not enough signups to form a new group yet
                                                    Toast.makeText(getContext(), "Waiting to form a new group.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Error fetching signups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error fetching groups: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getContext(), "User profile not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error fetching user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Checks if two lists have any common elements.
     * @param userInterests List of the user's interests as Strings.
     * @param groupInterests List of the group's common interests as Strings.
     */
    private boolean hasCommonInterests(List<String> userInterests, List<String> groupInterests) {
        for (String interest : userInterests) {
            if (groupInterests.contains(interest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines common interests among group members and creates a new group.
     * @param groupMembers List of user IDs to form the new group.
     * @param meetupId The ID of the Meetup.
     */
    private void determineCommonInterests(List<String> groupMembers, String meetupId) {
        db.collection("users").whereIn(FieldPath.documentId(), groupMembers)
                .get().addOnSuccessListener(usersSnapshot -> {
                    List<List<String>> interestsList = new ArrayList<>();
                    for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                        User user = userDoc.toObject(User.class);
                        if (user != null && user.getInterests() != null) {
                            interestsList.add(user.getInterests());
                        }
                    }

                    // Find common interests
                    List<String> commonInterests = new ArrayList<>();
                    if (!interestsList.isEmpty()) {
                        commonInterests.addAll(interestsList.get(0));
                        for (int i = 1; i < interestsList.size(); i++) {
                            commonInterests.retainAll(interestsList.get(i));
                        }
                    }

                    // List of bar names
                    List<String> barNames = Arrays.asList(
                            "Bäreneck",
                            "Café am Neuen See",
                            "Clash",
                            "Heckmeck",
                            "Monkey Bar",
                            "Trude Ruth und Goldammer",
                            "Hirsch",
                            "Klunkerkranich"
                    );

                    // Pick a random bar from the list
                    Random rand = new Random();
                    String selectedBar = barNames.get(rand.nextInt(barNames.size()));



                    // Create the new group
                    Group newGroup = new Group(groupMembers, commonInterests, Timestamp.now(), selectedBar);

                    Map<String, Object> groupData = new HashMap<>();
                    groupData.put("members", newGroup.getMembers());
                    groupData.put("commonInterests", newGroup.getCommonInterests());
                    groupData.put("createdAt", FieldValue.serverTimestamp());
                    groupData.put("bar", newGroup.getBar());

                    db.collection("meetups").document(meetupId).collection("groups")
                            .add(groupData)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(getContext(), "New group created successfully!", Toast.LENGTH_SHORT).show();
                                // Optionally, notify all group members
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error determining common interests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Checks if a user is already in any group within the Meetup.
     * @param userId The ID of the user.
     * @param existingGroupsSnapshot The snapshot of existing groups within the Meetup.
     */
    private boolean isUserInAnyGroup(String userId, QuerySnapshot existingGroupsSnapshot) {
        for (DocumentSnapshot groupDoc : existingGroupsSnapshot.getDocuments()) {
            List<String> members = (List<String>) groupDoc.get("members");
            if (members != null && members.contains(userId)) {
                return true;
            }
        }
        return false;
    }
}