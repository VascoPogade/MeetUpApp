package com.example.meetup.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class UserViewModel extends ViewModel {
    // LiveData objects (in case changes want to be observed in upscaled version)
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<Integer> age = new MutableLiveData<>();
    private final MutableLiveData<List<String>> interests = new MutableLiveData<>();

    // Setters
    public void setName(String name) {
        this.name.setValue(name);
    }

    public void setAge(int age) {
        this.age.setValue(age);
    }

    public void setInterests(List<String> interests) {
        this.interests.setValue(interests);
    }

    // Getters
    public LiveData<String> getName() {
        return name;
    }

    public LiveData<Integer> getAge() {
        return age;
    }

    public LiveData<List<String>> getInterests() {
        return interests;
    }

    // method to check if all required data is present
    public boolean isProfileComplete() {
        return name.getValue() != null && age.getValue() != null && interests.getValue() != null;
    }
}