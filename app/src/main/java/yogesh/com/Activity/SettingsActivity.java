package yogesh.com.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import yogesh.com.R;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat postsSwitch;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String TOPIC_POST_NOTIFICATION = "POST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        postsSwitch = findViewById(R.id.postSwitch);

        sharedPreferences = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sharedPreferences.getBoolean("" + TOPIC_POST_NOTIFICATION, false);
        if (isPostEnabled) {
            postsSwitch.setChecked(true);

        } else {
            postsSwitch.setChecked(false);
        }

        postsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPreferences.edit();
                editor.putBoolean("" + TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();

                if (isChecked) {
                    enablePostNotification();

                } else {
                    disablePostNotification();
                }


            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void disablePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("" + TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "You will not receive Post Notifications";
                if (!task.isSuccessful()) {
                    msg = "unSubscription Failed";

                }
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void enablePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "You will receive Post Notifications";
                if (!task.isSuccessful()) {
                    msg = "Subscription Failed";

                }
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();

            }
        });

    }
}
