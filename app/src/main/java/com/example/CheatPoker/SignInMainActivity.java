package com.example.CheatPoker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInMainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textRegister;
    EditText editTextEmail, editTextPassword;
    Button btnLogin;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    String playerName = "";
    FirebaseDatabase database;
    DatabaseReference playerRef;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null) {
            String userUid = firebaseUser.getUid();
            //String userEmail = firebaseUser.getEmail();
            Intent i = new Intent(SignInMainActivity.this, MainActivity.class);
            i.putExtra("userUid", userUid);
            //i.putExtra("userEmail", userEmail);
            startActivity(i);
            finish();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        textRegister = (TextView) findViewById(R.id.textSignUp);
        textRegister.setOnClickListener(this);


        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        btnLogin = (Button) findViewById(R.id.btnSignIn);
        btnLogin.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        //check if the player exists and get reference
        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("playerName", "");
        if (playerName.equals("")) {
            playerRef = database.getReference("players/" + playerName);
            addEventListener();
            playerRef.setValue("");
        }

    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:
                UserLogin();
                break;
            case R.id.textSignUp:
                startActivity(new Intent(this, SignUpMainActivity.class));
                break;
        }
    }

    public void UserLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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
        if (password.length() < 6) {
            editTextPassword.setError("password should be minimum 6 characters");
            editTextPassword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user.isEmailVerified()) {
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(firebaseUser!=null) {
                        String userUid = firebaseUser.getUid();
                        String userEmail = firebaseUser.getEmail();
                        Intent i = new Intent(SignInMainActivity.this, MainActivity.class);
                        i.putExtra("userUid", userUid);
                        i.putExtra("userEmail", userEmail);
                        startActivity(i);
                        finish();
                    }
                } else {
                    user.sendEmailVerification();
                    Toast.makeText(SignInMainActivity.this, "Check your email to verify your account", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(SignInMainActivity.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void addEventListener() {
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //success - continue to the next screen after saving the player name
                if (!playerName.equals("")) {
                    SharedPreferences preferences = getSharedPreferences("PREFS", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("playerName", playerName);
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
