package yogesh.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import yogesh.com.Fragments.HomeFragment;
import yogesh.com.Fragments.ListFragment;
import yogesh.com.Fragments.ProfileFragment;
import yogesh.com.Fragments.RegisterFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    BottomNavigationView mBottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starts");
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

                }
                return false;
            }
        });


        Log.d(TAG, "onCreate: Ends");
    }

    private void getFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    private void checkUserStatus() {
        Log.d(TAG, "checkUserStatus: Starts");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {


        } else {
            startActivity(new Intent(MainActivity.this, RegisterUserActivity.class));  //change this to NewUserActivity
            finish();
        }

        Log.d(TAG, "checkUserStatus: Ends");
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
