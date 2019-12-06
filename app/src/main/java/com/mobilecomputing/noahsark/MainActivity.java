package com.mobilecomputing.noahsark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String ADMIN_USER = "admin7";
    private final String ADMIN_PASS = "admin7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check for internet permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "internet permission not granted");
        }
        setContentView(R.layout.activity_main);

        Button login_btn = findViewById(R.id.btn_login);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate database login
                TextView widget = findViewById(R.id.txt_user);
                String userStr = widget.getText().toString();
                widget = findViewById(R.id.txt_pass);
                String passStr = widget.getText().toString();
                checkValidUser(userStr, passStr);

            }

        });

        TextView sign_up = findViewById(R.id.txt_signup);
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SignupActivity.class);
                view.getContext().startActivity(intent);
            }
        });
    }

    private boolean checkValidUser(String userStr, String passStr) {
        // Hardcoded admin login
        if(userStr.equals("a")){
            return true;
        }
        if(ADMIN_USER.equals(userStr) && ADMIN_PASS.equals(passStr)){
            return true;
        }

        // Do database check here
        String url = URLs.LOGIN;
        Map<String, String> params = new HashMap();
        params.put("user", userStr);
        params.put("pass", passStr);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameters,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("REST: " , response.toString());
                    try {
                        if(response.getInt("code") != 200) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Invalid Login Credentials!", Toast.LENGTH_LONG);
                            toast.show();
                        }else{
                            Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
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
                    Toast toast = Toast.makeText(getApplicationContext(), "Server Error: "+error.toString(), Toast.LENGTH_LONG);
                    toast.show();
                }
            });

        PSQLUtil.getInstance(this).addToRequestQueue(jsonObjectRequest);
        // fail case
        return false;
    }


}
