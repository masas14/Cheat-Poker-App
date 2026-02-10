package com.example.CheatPoker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpMainActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    TextView textBanner;
    EditText editTextUserName, editTextEmail, editTextPassword;
    Button btnRegister;
    ProgressBar progressBar;
    public User user;

    SensorManager sensorManager;
    Sensor sensorLight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);


        final Boolean[] isDark = {false};
// create light sensor
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.sensorLight = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        // listen for sensor events
        SensorEventListener sensorEventListenerLight = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                /*
                this function will run when the lightSensor level changed
                param: sensorEvent : SensorEvent
                return:void
                */
                float lux = sensorEvent.values[0];
                if(lux < 1 && !isDark[0]) // if lux is less than 1 its very dark outside
                {
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, "It's Dark, Lower your phone brightness", Toast.LENGTH_SHORT);
                    toast.show();
                    isDark[0] = true;
                }
                else if (lux > 200)
                {
                    isDark[0] = false;
                }
            }

//            @Override
//            public void onSensorChanged(SensorEvent event) {
//
//            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {} // i don't use this one.
        };
        // set the delay for the sensor
        this.sensorManager.registerListener(sensorEventListenerLight,this.sensorLight,sensorManager.SENSOR_DELAY_NORMAL);


        mAuth = FirebaseAuth.getInstance();

        textBanner = (TextView) findViewById(R.id.textBanner);
        textBanner.setOnClickListener(this);

        btnRegister = (Button) findViewById(R.id.btnSignUp);
        btnRegister.setOnClickListener(this);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.textBanner:
                startActivity(new Intent(this, SignInMainActivity.class));
                break;
            case R.id.btnSignUp:
                RegisterUser();

                progressBar.setVisibility(View.GONE);
                break;

        }
    }

    private void RegisterUser() {

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String userName = editTextEmail.getText().toString().trim().replace("@gmail.com", "");

        if (userName.isEmpty()) {
            editTextUserName.setError("Username is required!");
            editTextUserName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please provide valid email!");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }
        if(password.length()<6) {
            editTextPassword.setError("password should be minimum 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpMainActivity.this, "User has been sign in successfully!",
                                                Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.VISIBLE);

                                      User user = new User(userName, email);
                                      Intent i = new Intent(SignUpMainActivity.this, PrivateGameActivity.class);
                                      i.putExtra("user", (Parcelable) user);
                                      i.putExtra("fullName", userName);
                                      startActivity(i);

                                    } else {
                                        // redirect to Login Layout!

                                        Toast.makeText(SignUpMainActivity.this, "Failed to sign up! Try again!",
                                                Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                }
                            });

                        }else{
                            Toast.makeText(SignUpMainActivity.this, "Failed to sign up! Try again!",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                });


        }
    }