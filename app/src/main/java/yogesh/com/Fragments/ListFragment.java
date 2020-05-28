package yogesh.com.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import yogesh.com.Activity.RegisterUserActivity;
import yogesh.com.Activity.SettingsActivity;
import yogesh.com.AdapterAndModels.ComplaintsAdapter;
import yogesh.com.AdapterAndModels.ModelsComplaintList;
import yogesh.com.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    private ArrayList<ModelsComplaintList> complaintList;
    private ComplaintsAdapter complaintsAdapter;
    private FirebaseAuth mAuth;
    private RecyclerView complaintsRecycler;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        complaintsRecycler = view.findViewById(R.id.complaintRecyclerView);

        mAuth = FirebaseAuth.getInstance();

        loadComplaints();

        return view;
    }

    private void loadComplaints() {
        complaintList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(mAuth.getUid()).child("Complaints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                complaintList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelsComplaintList modelsComplaintList = ds.getValue(ModelsComplaintList.class);

                    complaintList.add(modelsComplaintList);

                    complaintsAdapter = new ComplaintsAdapter(getActivity(), complaintList);
                    complaintsRecycler.setAdapter(complaintsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

        } else {
            startActivity(new Intent(getActivity(), RegisterUserActivity.class));
            getActivity().finish();

        }

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
