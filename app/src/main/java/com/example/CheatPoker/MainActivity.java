package com.example.CheatPoker;

import static android.view.View.VISIBLE;

import static java.util.Calendar.getInstance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView rules, textPopupRules;
    Button btnOnlineGame, btnPrivateGame, btnSignOut;
    ProgressBar progressBar;

    FirebaseAuth fireBaseUser = FirebaseAuth.getInstance();
    String userUid = fireBaseUser.getCurrentUser().getUid();
    User user = new User(userUid);
    Vibrator vibrator;

    SensorManager sensorManager;
    Sensor sensorLight;

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        final Boolean[] isDark = {false};
// create light sensor
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.sensorLight = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        // listen for sensor events
        SensorEventListener sensorEventListenerLight = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

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

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {} // i don't use this one.
        };
        // set the delay for the sensor
        this.sensorManager.registerListener(sensorEventListenerLight,this.sensorLight,sensorManager.SENSOR_DELAY_NORMAL);


        broadcastReceiver = new WiFiBroadcastReceiver();
        registerNetworkBroadcastReceiver();

        vibrator =  (Vibrator) getSystemService(VIBRATOR_SERVICE);

        rules = (TextView) findViewById(R.id.rules);
        rules.setOnClickListener(this);

        textPopupRules = (TextView) findViewById(R.id.textPopupRules);

        btnPrivateGame = (Button) findViewById(R.id.btnPrivateGame);
        btnPrivateGame.setOnClickListener(this);

        btnOnlineGame = (Button) findViewById(R.id.btnOnlineGame);
        btnOnlineGame.setOnClickListener(this);

        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnOnlineGame:
//                vibrator.vibrate(50);
//                Intent intentOnline = new Intent(MainActivity.this, MainActivity4.class);
//                intentOnline.putExtra("userUid",user.getUid());
//                startActivity(intentOnline);
//                finish();
                break;
            case R.id.btnPrivateGame:
                vibrator.vibrate(50);
                Intent intentPrivate = new Intent(MainActivity.this, PrivateGameActivity.class);
                intentPrivate.putExtra("userUid",user.getUid());
                startActivity(intentPrivate);
                finish();
                break;
            case R.id.rules:
                vibrator.vibrate(50);
                textPopupRules.setVisibility(VISIBLE);
                break;

            case R.id.btnSignOut:
                vibrator.vibrate(50);
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "signed out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SignInMainActivity.class));
                break;
            default:


        }
    };

    protected void registerNetworkBroadcastReceiver() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisteredNetwork() {
        try {
            unregisterReceiver (broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
                super.onDestroy();
                unregisteredNetwork();
            }
}