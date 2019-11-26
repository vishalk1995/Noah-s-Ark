package com.mobilecomputing.noahsark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mobilecomputing.noahsark.connectionUtil.PSQLUtil;
import com.mobilecomputing.noahsark.connectionUtil.URLs;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
       // getActionBar().setTitle("Create your profile");

        Button signupBtn = findViewById(R.id.btn_signup_profile);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get data from screen
                TextView name = findViewById(R.id.txt_name_profile);
                TextView age = findViewById(R.id.txt_age_profile);
                TextView phno = findViewById(R.id.txt_ph_no_profile);
                TextView addr = findViewById(R.id.txt_address_profile);
                TextView city = findViewById(R.id.txt_city_profile);
                TextView state = findViewById(R.id.txt_state_profile);
                TextView emergency = findViewById(R.id.txt_emergency_profile);
                TextView bloodgrp = findViewById(R.id.txt_bloodgrp_profile);
                TextView passwd = findViewById(R.id.txt_pass_profile);
                TextView passwdConf = findViewById(R.id.txt_pass_conf_profile);

                boolean isInputValid = validateInput(name, age, phno, addr, city, state, emergency, bloodgrp, passwd, passwdConf);

                if(isInputValid){
                    JSONObject profileJSON = new JSONObject();
                    try{
                        profileJSON.put("name", name.getText().toString());
                        profileJSON.put("age", age.getText().toString());
                        profileJSON.put("phno", phno.getText().toString());
                        profileJSON.put("addr", addr.getText().toString());
                        profileJSON.put("city", city.getText().toString());
                        profileJSON.put("state", state.getText().toString());
                        profileJSON.put("emergency", emergency.getText().toString());
                        profileJSON.put("bloodgrp", bloodgrp.getText().toString());
                        profileJSON.put("passwd", passwd.getText().toString());

                        Log.d("JSON", profileJSON.toString(4));
                    } catch (Exception e){
                        Log.e("JSONException", e.toString());
                    }

                    // Do database insert here
                    String url = URLs.USER_PROFILE;
                    Map<String, String> params = new HashMap();
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, profileJSON,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("REST: " , response.toString());
                                    try {
                                        if(response.getInt("code") != 200) {
                                            Toast toast = Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG);
                                            toast.show();
                                        }else{
                                            Intent intent = new Intent(SignupActivity.this, NavigationActivity.class);
                                            startActivity(intent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO: Handle error
                                    Log.d("REST", error.toString());
                                    Toast toast = Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });

                    PSQLUtil.getInstance(SignupActivity.this).addToRequestQueue(jsonObjectRequest);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Passwords don't match (or) Invalid/Empty Input Attributes!", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });

    }

    private boolean validateInput(TextView name, TextView age, TextView phno, TextView addr, TextView city, TextView state, TextView emergency, TextView bloodgrp, TextView pass, TextView passConf) {
        if(!pass.getText().toString().equals(passConf.getText().toString())){
            return false;
        }
        if(name.getText().toString().isEmpty() || name.getText().toString().trim().equals("")){
            return false;
        }
        if(age.getText().toString().isEmpty() || age.getText().toString().trim().equals("")){
            return false;
        }
        if(phno.getText().toString().isEmpty() || phno.getText().toString().trim().equals("")){
            return false;
        }
        if(addr.getText().toString().isEmpty() || addr.getText().toString().trim().equals("")){
            return false;
        }
        if(city.getText().toString().isEmpty() || city.getText().toString().trim().equals("")){
            return false;
        }
        if(state.getText().toString().isEmpty() || state.getText().toString().trim().equals("")){
            return false;
        }
        if(emergency.getText().toString().isEmpty() || emergency.getText().toString().trim().equals("")){
            return false;
        }
        if(bloodgrp.getText().toString().isEmpty() || bloodgrp.getText().toString().trim().equals("")){
            return false;
        }

        return true;
    }
}
