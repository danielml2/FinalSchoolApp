package me.danielml.finalschoolapp.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import me.danielml.finalschoolapp.R;
import me.danielml.finalschoolapp.managers.FirebaseManager;

public class SignInActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button signInSubmit;

    private FirebaseManager fbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        signInSubmit.setOnClickListener((v) -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            
            // Sign in logic here.
        });

    }
}