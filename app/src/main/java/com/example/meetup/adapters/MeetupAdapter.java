package com.example.meetup.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.meetup.R;
import com.example.meetup.models.Meetup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class MeetupAdapter extends RecyclerView.Adapter<MeetupAdapter.MeetupViewHolder> {

    private List<Meetup> meetupList;
    private OnRevokeClickListener revokeListener;
    private FirebaseFirestore db;
    private String userId;


    public MeetupAdapter(List<Meetup> meetupList, OnRevokeClickListener revokeListener, String userId) {
        this.meetupList = meetupList;
        this.revokeListener = revokeListener;
        this.userId = userId;

        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MeetupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meetup, parent, false);
        return new MeetupViewHolder(view);
    }

    /**
     * binds each meetups data to the specific UI element in the layout
     * is called when .notifyDataSetChanged() is called in the fetchMeetups() method
     */
    @Override
    public void onBindViewHolder(@NonNull MeetupViewHolder holder, int position) {
        Meetup meetup = meetupList.get(position);
        holder.tvDayTime.setText(meetup.getDay() + " at " + meetup.getTime());
        holder.tvDate.setText(meetup.getDate());
        holder.tvStatus.setText("Status: " + meetup.getStatus());

        // If the meetup is not assigned to a group, show a message
        holder.tvBar.setText("Not assigned to a group yet. Once you will be assigned to a group you will see your Meetup location here :)");

        db.collection("meetups").document(meetup.getId())
                .collection("groups")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        // Assuming one group per user per meetup
                        DocumentSnapshot groupDoc = snapshot.getDocuments().get(0);
                        String bar = groupDoc.getString("bar");
                        if(bar != null) {
                            holder.tvBar.setText("Bar: " + bar);
                        } else {
                            holder.tvBar.setText("Bar: Not assigned");
                        }
                    }
                    // If snapshot is empty, default text remains
                })
                .addOnFailureListener(e -> {
                    // Logs the error and shows a message
                    Log.e("MeetupAdapter", "onBindViewHolder: " + e.getMessage());
                    Toast.makeText(holder.itemView.getContext(), "Error loading bar information.", Toast.LENGTH_SHORT).show();
                });



        // Set listener for revoke button
        holder.btnRevoke.setOnClickListener(v -> {
            if (revokeListener != null) {
                revokeListener.onRevokeClick(meetup);
            }
        });
    }

    /**
     * returns the number of items in the list
     */
    @Override
    public int getItemCount() {
        return meetupList.size();
    }

    /**
     * ViewHolder class to hold the UI elements from the xml layout
     */
    static class MeetupViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayTime, tvDate, tvStatus, tvBar;
        Button btnRevoke;

        public MeetupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayTime = itemView.findViewById(R.id.tvMeetupDayTime);
            tvDate = itemView.findViewById(R.id.tvMeetupDate);
            tvStatus = itemView.findViewById(R.id.tvMeetupStatus);
            tvBar = itemView.findViewById(R.id.tvBar);
            btnRevoke = itemView.findViewById(R.id.btnRevoke);
        }
    }
}