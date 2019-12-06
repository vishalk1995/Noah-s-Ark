package com.mobilecomputing.noahsark.ui.weather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.mobilecomputing.noahsark.R;

public class WeatherFragment extends Fragment {

    private WeatherViewModel weatherViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        weatherViewModel = ViewModelProviders.of(this).get(WeatherViewModel.class);
        View root = inflater.inflate(R.layout.fragment_weather, container, false);
        weatherViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;
    }
}