package yogesh.com.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import yogesh.com.R;

public class RegisterUserActivity extends AppCompatActivity {
    private static final String TAG = "RegisterUserActivity";

    ProgressDialog mProgressDialog;
    private EditText mEmail, mPassword;
    private Button mRegisterButton;
    private TextView mLoginText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create New Account");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEmail = findViewById(R.id.registerEmailEditText);
        mPassword = findViewById(R.id.registerPasswordEditText);
        mRegisterButton = findViewById(R.id.registerButton);
        mLoginText = findViewById(R.id.logInTextView);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Creating New Account...");

        mAuth = FirebaseAuth.getInstance();

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmail.setError("Invalid Email ID");
                    mEmail.setFocusable(true);

                } else if (password.length() < 6) {
                    mPassword.setError("Password should be more than 6 characters long");
                    mPassword.setFocusable(true);

                } else {
                    registerUser(email, password);

                }
            }
        });

        mLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUserActivity.this, LoginUserActivity.class));
                finish();

            }
        });


    }

    private void registerUser(String email, String password) {

        mProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mProgressDialog.dismiss();
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            String email = user.getEmail();
                            String uid = user.getUid();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", "");
                            hashMap.put("phonenumber", "");
                            hashMap.put("image", "");
                            hashMap.put("cover", "");


                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");  //users path storage
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterUserActivity.this, "New Account Created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterUserActivity.this, MainActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            mProgressDialog.dismiss();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUserActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mProgressDialog.dismiss();
                Toast.makeText(RegisterUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
