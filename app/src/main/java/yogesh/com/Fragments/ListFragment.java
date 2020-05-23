package yogesh.com.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import yogesh.com.Activity.RegisterUserActivity;
import yogesh.com.Activity.SettingsActivity;
import yogesh.com.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    private FirebaseAuth mAuth;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        return view;
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
