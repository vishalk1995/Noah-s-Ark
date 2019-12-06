package com.mobilecomputing.noahsark.ui.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeatherViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public WeatherViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is weather fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}