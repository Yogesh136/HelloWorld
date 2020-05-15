package yogesh.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import yogesh.com.Fragments.HomeFragment;
import yogesh.com.Fragments.ListFragment;
import yogesh.com.Fragments.NotificationsFragment;
import yogesh.com.Fragments.ProfileFragment;
import yogesh.com.Fragments.RegisterFragment;
import yogesh.com.notifications.Sender;
import yogesh.com.notifications.Token;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    BottomNavigationView mBottomNavigationView;
    private FirebaseAuth mAuth;
    private String mUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mBottomNavigationView = findViewById(R.id.bottom_view);

        getFragment(new HomeFragment());


        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    getSupportActionBar().setTitle("Home");
                    getFragment(new HomeFragment());

                } else if (item.getItemId() == R.id.register) {
                    getSupportActionBar().setTitle("Register Complaint");
                    getFragment(new RegisterFragment());

                } else if (item.getItemId() == R.id.list) {
                    getSupportActionBar().setTitle("List");
                    getFragment(new ListFragment());

                } else if (item.getItemId() == R.id.profile) {
                    getSupportActionBar().setTitle("My Profile");
                    getFragment(new ProfileFragment());

                } else if (item.getItemId() == R.id.notifications) {
                    getSupportActionBar().setTitle("Notifications");
                    getFragment(new NotificationsFragment());
                }
                return false;
            }
        });

        checkUserStatus();

//        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

//    public void updateToken(String token) {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
//        Token mToken = new Token(token);
//        ref.child(mUID).setValue(mToken);
//
//    }

    private void getFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    private void checkUserStatus() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
//            user is signed in
            mUID = currentUser.getUid();
            SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

        } else {
            startActivity(new Intent(MainActivity.this, RegisterUserActivity.class));  //change this to NewUserActivity
            finish();
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }


}
