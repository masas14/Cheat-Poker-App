package com.example.CheatPoker;

import static org.junit.Assert.assertEquals;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

//import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PrivateGameActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextInsertCode;
    TextView textView;
    Button btnInsertCode, btnGenerateCode, btnJoin, btnBack;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference gamesReference = db.collection("games");

    Map<String,Object> hashGame = new HashMap<>();
    Map<String,Object> hashGamer = new HashMap<>();

    String lastCode = null;

    FirebaseAuth fireBaseUser = FirebaseAuth.getInstance();
    String userUid = fireBaseUser.getCurrentUser().getUid();
    String userEmail = fireBaseUser.getCurrentUser().getEmail();
    User user = new User(userUid, userEmail);

    Intent p;

    SensorManager sensorManager;
    Sensor sensorLight;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);


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


        p = new Intent(PrivateGameActivity.this, MainActivity4.class);

        btnInsertCode = findViewById(R.id.btnInsertCode);
        btnInsertCode.setOnClickListener(this);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        btnGenerateCode.setOnClickListener(this);

        btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(this);

        editTextInsertCode = findViewById(R.id.editTextInsertCode);
        editTextInsertCode.setOnClickListener(this);

//        for(int i=0; i<13;i++){
//            fake.add(fakeCard);
//        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                startActivity(new Intent(PrivateGameActivity.this, MainActivity.class));
                finish();
                break;
            case R.id.btnGenerateCode:
                editTextInsertCode.setVisibility(View.GONE);
                Bundle extras = getIntent().getExtras();
                String code = addNewGame();
                lastCode = code;
                p.putExtra("generateCode", code);
                p.putExtra("hostUid", fireBaseUser.getCurrentUser().getUid());
                p.putExtra("fullName", extras.getString("fullName"));
                startActivity(p);
                finish();

                break;
            case R.id.btnInsertCode:
                editTextInsertCode.setVisibility(View.VISIBLE);
                btnJoin.setVisibility(View.VISIBLE);

                break;
            case R.id.btnJoin:
                String insertCode = editTextInsertCode.getText().toString().trim();
                if(insertCode.length()!=5){
                    Toast.makeText(PrivateGameActivity.this, "code must be 5 digits", Toast.LENGTH_SHORT).show();
                } else
                gamesReference.document(insertCode).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            DocumentReference gameRef = gamesReference.document(insertCode);
                            gameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    Bundle extras = getIntent().getExtras();
                                    if(documentSnapshot.getDouble("numGamers")!=null) {
                                        int numGamers = documentSnapshot.getDouble("numGamers").intValue();
                                        gameRef.update("numGamers", numGamers + 1);
                                        gameRef.update("numCards", (int) (26 / (numGamers + 1)));
                                        CollectionReference gamersRef = gamesReference.document(insertCode).collection("gamers");
                                        gameRef.collection("gamers").document(user.getUid());
                                        hashGamer.put("minusCards", 0);
                                        hashGamer.put("username", user.getEmail().replace("@gmail.com", ""));
                                        hashGamer.put("myLastGamer", documentSnapshot.getString("lastGamer"));
                                        user.setMyLastUser(documentSnapshot.getString("lastGamer"));
                                        p.putExtra("myLastGamer", documentSnapshot.getString("lastGamer"));
                                        gameRef.collection("gamers").document(user.getUid()).set(hashGamer);
                                        //gamersRef.document(user.getFullName()).collection("hand").add(null);
                                        gameRef.update("lastGamer", user.getUid());
                                        p.putExtra("insertCode", insertCode);
                                        p.putExtra("joined","joined");
                                        startActivity(p);
                                        finish();
                                    } else {
                                        Toast.makeText(PrivateGameActivity.this, "the game is not exist", Toast.LENGTH_SHORT).show();
                                    }
                        }
                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PrivateGameActivity.this, "Failed to join to the game",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                break;

        }

    }

    public String addNewGame() {
        Random random = new Random();
        int rnd = random.nextInt(89999) + 10000;
        String code = String.valueOf(rnd);
        HashMap<String,Object> hashCard = new HashMap<>();
        DocumentReference gameRef = gamesReference.document(code);
        hashGame.put("host", user.getUid());
        hashGame.put("lastGamer", user.getUid());
        hashGame.put("status", false);
        hashGame.put("loser", null);
        hashGame.put("numGamers", 1);
        hashGame.put("numCards", 10);
        hashGame.put("lastHandStrength", 0);
        gameRef.set(hashGame);
//
        hashGamer.put("minusCards",0);
        hashGamer.put("myLastGamer", null);
        hashGamer.put("username", (user.getEmail().replace("@gmail.com", "")));
        gameRef.collection("gamers").document(user.getUid()).set(hashGamer);
        return code;
    }
}




