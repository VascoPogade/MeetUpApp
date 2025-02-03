package com.example.meetup.adapters;

import com.example.meetup.models.Meetup;

// Interface for handling click events on the revoke button in the MeetupAdapter
public interface OnRevokeClickListener {
    void onRevokeClick(Meetup meetup);
}