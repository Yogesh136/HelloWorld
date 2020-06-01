package yogesh.com.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import yogesh.com.R;

public class ComplaintDetailsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActionBar actionBar;

    private String email, uid;
    private Button submitButton;
    private TextView complaintNumber, name, phoneNumber, department, date, address, description, status;
    private RadioButton suggestionButton, feedbackButton;
    private RatingBar mRatingBar;
    private ImageView capturedImage;
    private EditText suggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Complaint Details");
        actionBar.setSubtitle(email);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        complaintNumber = findViewById(R.id.complaintNumberTextView);
        name = findViewById(R.id.NameTextView);
        phoneNumber = findViewById(R.id.phoneNumberTextView);
        department = findViewById(R.id.departmentTextView);
        date = findViewById(R.id.complaintDateTextView);
        address = findViewById(R.id.addressTextView);
        description = findViewById(R.id.complaintDescriptionTextView);
        status = findViewById(R.id.complaintStatusTextView);
        suggestionButton = findViewById(R.id.suggestionRadioButton);
        feedbackButton = findViewById(R.id.feedbackRadioButton);
        mRatingBar = findViewById(R.id.ratingBar);
        capturedImage = findViewById(R.id.capturedImageView);
        suggestion = findViewById(R.id.suggOrFeedbackEditText);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadReview();
            }
        });
    }

    private void uploadReview() {
        String ratings = "" + mRatingBar.getRating();
        String sugg = suggestion.getText().toString().trim();
        String timeStamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + mAuth.getUid());
        hashMap.put("Review", "" + ratings);
        hashMap.put("Suggestions", sugg);
        hashMap.put("timeStamp", timeStamp);

    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}