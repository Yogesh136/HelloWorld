package yogesh.com.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import yogesh.com.Activity.AddPostActivity;
import yogesh.com.Activity.RegisterUserActivity;
import yogesh.com.Activity.SettingsActivity;
import yogesh.com.AdapterAndModels.ModelPost;
import yogesh.com.AdapterAndModels.PostsAdapter;
import yogesh.com.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FirebaseAuth mAuth;
    private FloatingActionButton mfab;
    private RecyclerView mRecyclerView;
    private List<ModelPost> mPostList;
    PostsAdapter mPostsAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Starts");
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mfab = view.findViewById(R.id.postFab);

        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });

        mRecyclerView = view.findViewById(R.id.postsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(layoutManager);

        mPostList = new ArrayList<>();
        loadPosts();

        Log.d(TAG, "onCreateView: Ends");
        return view;
    }

    private void loadPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPostList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    mPostList.add(modelPost);

                    mPostsAdapter = new PostsAdapter(getActivity(), mPostList);
                    mRecyclerView.setAdapter(mPostsAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d(TAG, "loadPosts: Ends");
    }

    private void searchPosts(final String searchQuery) {
        Log.d(TAG, "searchPosts: Starts");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPostList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        mPostList.add(modelPost);
                    }

                    mPostsAdapter = new PostsAdapter(getActivity(), mPostList);
                    mRecyclerView.setAdapter(mPostsAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        Log.d(TAG, "searchPosts: Ends");
    }

    private void checkUserStatus() {
        Log.d(TAG, "checkUserStatus: Starts");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {


        } else {
            startActivity(new Intent(getActivity(), RegisterUserActivity.class));  //change this to NewUserActivity
            getActivity().finish();
        }

        Log.d(TAG, "checkUserStatus: Ends");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.searchMenu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);

                } else {
                    loadPosts();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);

                } else {
                    loadPosts();

                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoutMenu) {
            mAuth.signOut();
            checkUserStatus();

        } else if (id == R.id.settingsMenu) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
