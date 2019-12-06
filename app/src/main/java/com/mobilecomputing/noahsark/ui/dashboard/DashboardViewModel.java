package com.mobilecomputing.noahsark.ui.dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mapbox.mapboxsdk.camera.CameraPosition;

public class DashboardViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private CameraPosition cameraPos;

    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public void savePositiion(CameraPosition pos){
        Log.d("CAMPOS", "Saved camera pos");
        this.cameraPos = pos;
    }

    public CameraPosition getCameraPos(){
        Log.d("CAMPOS", "returned camera pos"+this.cameraPos);
        return this.cameraPos;
    }
    public LiveData<String> getText() {
        return mText;
    }
}