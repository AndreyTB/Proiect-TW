package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword, ConfirmUserPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        initializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = ConfirmUserPassword.getText().toString();

        if (TextUtils.isEmpty(email))
            Toast.makeText(this, "Please enter the email...", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(password))
            Toast.makeText(this, "Please enter the password...", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(confirmPassword))
            Toast.makeText(this, "Please enter the confirmation", Toast.LENGTH_SHORT).show();
        else if (confirmPassword.equals(password) == false)
            Toast.makeText(this, "Parolele nu coincid!", Toast.LENGTH_SHORT).show();
        else {

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait while we are creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        // MODIFICAT
                        SendEmailVerification();
                        //
                        String currentUserID = mAuth.getCurrentUser().getUid();
                        rootRef.child("Users").child(currentUserID).setValue(" ");

                        loadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account Created Succesfully", Toast.LENGTH_SHORT).show();
                        SendUserToLoginActivity();
                    } else {

                        Toast.makeText(RegisterActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void initializeFields() {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        ConfirmUserPassword = (EditText) findViewById(R.id.register_password_confirmation);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);

        loadingBar = new ProgressDialog(this);
    }

    private void SendEmailVerification() {

        FirebaseUser user = mAuth.getCurrentUser();

        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterActivity.this, "Email send!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Don't send!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean ValidateEmailAdress(String email){

        if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return true;
        return false;
    }

}

