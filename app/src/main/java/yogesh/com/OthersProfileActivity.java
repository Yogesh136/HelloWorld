package yogesh.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OthersProfileActivity extends AppCompatActivity {
    private static final String TAG = "OthersProfileActivity";

    private FirebaseAuth mAuth;

    private ImageView mImageView, mCoverImageView;
    private TextView mName, mEmail, mPhoneNumber;
    private RecyclerView mPostRecyclerView;

    private List<ModelPost> postList;
    private PostsAdapter mPostsAdapter;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mImageView = findViewById(R.id.profileImage);
        mCoverImageView = findViewById(R.id.coverPhotoImageView);
        mName = findViewById(R.id.nameTextView);
        mEmail = findViewById(R.id.emailTextView);
        mPhoneNumber = findViewById(R.id.phoneTextView);
        mPostRecyclerView = findViewById(R.id.recyclerviewposts);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Starts");

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phoneNumber = "" + ds.child("phonenumber").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    mName.setText(name);
                    mEmail.setText(email);
                    mPhoneNumber.setText(phoneNumber);
                    try {
                        Picasso.get().load(image).into(mImageView);

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.profile_image).into(mImageView);
                    }

                    try {
                        Picasso.get().load(cover).into(mCoverImageView);

                    } catch (Exception e) {
                        Log.d(TAG, "onDataChange: " + e.getMessage());

                    }

                }
                Log.d(TAG, "onDataChange: Ends");
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        postList = new ArrayList<>();

        checkUserStatus();
        loadHisPosts();
    }

    private void loadHisPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mPostRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    postList.add(myPosts);

                    mPostsAdapter = new PostsAdapter(OthersProfileActivity.this, postList);
                    mPostRecyclerView.setAdapter(mPostsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OthersProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void searchHisPosts(final String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(OthersProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mPostRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(myPosts);

                    }

                    mPostsAdapter = new PostsAdapter(OthersProfileActivity.this, postList);
                    mPostRecyclerView.setAdapter(mPostsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OthersProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void checkUserStatus() {
        Log.d(TAG, "checkUserStatus: Starts");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {


        } else {
            startActivity(new Intent(this, RegisterUserActivity.class));  //change this to NewUserActivity
            finish();
        }

        Log.d(TAG, "checkUserStatus: Ends");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.searchMenu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchHisPosts(s);

                } else {
                    loadHisPosts();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchHisPosts(s);

                } else {
                    loadHisPosts();

                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoutMenu) {
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
