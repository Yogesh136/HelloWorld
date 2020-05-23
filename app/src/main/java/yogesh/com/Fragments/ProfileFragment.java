package yogesh.com.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yogesh.com.Activity.RegisterUserActivity;
import yogesh.com.Activity.SettingsActivity;
import yogesh.com.AdapterAndModels.ModelPost;
import yogesh.com.AdapterAndModels.PostsAdapter;
import yogesh.com.R;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;

    private StorageReference mStorageReference;
    private String storagePath = "Users_Profile_Cover_Images/";

    private ImageView mImageView, mCoverImageView;
    private TextView mName, mEmail, mPhoneNumber;
    private FloatingActionButton mfab;
    private RecyclerView mPostRecyclerView;

    private ProgressDialog mProgressDialog;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private List<ModelPost> postList;
    private PostsAdapter mPostsAdapter;
    private String uid;

    private String cameraPermissions[];
    private String storagePermissions[];

    private Uri image_uri;

    private String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Starts");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference("Users");
        mStorageReference = getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        mImageView = view.findViewById(R.id.profileImage);
        mCoverImageView = view.findViewById(R.id.coverPhotoImageView);
        mName = view.findViewById(R.id.nameTextView);
        mEmail = view.findViewById(R.id.emailTextView);
        mPhoneNumber = view.findViewById(R.id.phoneTextView);
        mfab = view.findViewById(R.id.fab);
        mPostRecyclerView = view.findViewById(R.id.recyclerviewposts);


        mProgressDialog = new ProgressDialog(getActivity());

        Query query = mDatabaseReference.orderByChild("email").equalTo(mUser.getEmail());
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


        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();

            }
        });

        postList = new ArrayList<>();
        checkUserStatus();
        loadMyPosts();

        Log.d(TAG, "onCreateView: Ends");
        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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

                    mPostsAdapter = new PostsAdapter(getActivity(), postList);
                    mPostRecyclerView.setAdapter(mPostsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(getActivity(), "" +databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void searchMyPosts(final String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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

                    mPostsAdapter = new PostsAdapter(getActivity(), postList);
                    mPostRecyclerView.setAdapter(mPostsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private boolean checkStoragePermission() {
        Log.d(TAG, "checkStoragePermission: Starts");

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        Log.d(TAG, "checkStoragePermission: Ends");
        return result;
    }


    private void requestStoragePermissin() {
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }


    private boolean checkCameraPermission() {
        Log.d(TAG, "checkCameraPermission: Starts");

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        Log.d(TAG, "checkCameraPermission: Ends");
        return result && result1;
    }


    private void requestCameraPermissin() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }


    private void showEditProfileDialog() {
        Log.d(TAG, "showEditProfileDialog: Starts");

        String options[] = {"Edit Profile Picture", "Edit Cover Picture", "Edit Name", "Edit PhoneNumber", "Change Password"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    mProgressDialog.setMessage("Updating Profile Picture...");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();

                } else if (which == 1) {
                    mProgressDialog.setMessage("Updating Cover Photo...");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();

                } else if (which == 2) {
                    mProgressDialog.setMessage("Updating Name...");
                    showNamePhoneUpdateDialog("name");

                } else if (which == 3) {
                    mProgressDialog.setMessage("Updating PhoneNumber...");
                    showNamePhoneUpdateDialog("phonenumber");

                } else if (which == 4) {
                    mProgressDialog.setMessage("Changing Password...");
                    showChangePasswordDialog();

                }


            }
        });
        builder.create().show();

        Log.d(TAG, "showEditProfileDialog: Ends");
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password, null);
        final EditText currentpasswordEditText = view.findViewById(R.id.currentPasswordEdittext);
        final EditText newpasswordEditText = view.findViewById(R.id.newPasswordEditText);
        Button updateButton = view.findViewById(R.id.updatePasswordButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = currentpasswordEditText.getText().toString().trim();
                String newPassword = newpasswordEditText.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(getActivity(), "Enter your Current Password...", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (newPassword.length() < 6) {
                    Toast.makeText(getActivity(), "Password should be more than 6 characters...", Toast.LENGTH_SHORT).show();
                    return;

                }

                dialog.dismiss();
                updatePassword(oldPassword, newPassword);

            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        mProgressDialog.show();

        final FirebaseUser user = mAuth.getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressDialog.dismiss();
                        Toast.makeText(getActivity(), "Password Updated Successfully...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void showNamePhoneUpdateDialog(final String key) {    //key parameter will either contain name or phonenumber value
        Log.d(TAG, "showNamePhoneUpdateDialog: Starts");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    mProgressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    mDatabaseReference.child(mUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated Successfully", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                    if (key.equals("name")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")) {
                                        String child1 = "" + dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                } else {
                    Toast.makeText(getActivity(), "Please Enter " + key, Toast.LENGTH_SHORT).show();

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();

    }

    private void showImagePicDialog() {
        Log.d(TAG, "showImagePicDialog: Starts");

        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermissin();
                    } else {
                        pickFromCamera();
                    }


                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermissin();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "Please Enable Camera and Storage Permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //method called after picking image
        Log.d(TAG, "onActivityResult: Starts");
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: Ends");
    }

    private void uploadProfileCoverPhoto(Uri uri) {  //path and name to be stored in firebase database
        Log.d(TAG, "uploadProfileCoverPhoto: Starts");

        mProgressDialog.show();

        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + mUser.getUid();
        StorageReference storageReference2nd = mStorageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        final Uri downloadUri = uriTask.getResult();

                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            mDatabaseReference.child(mUser.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Image Update Successfully", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Error Updating Image", Toast.LENGTH_SHORT).show();

                                }
                            });

                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")) {
                                                String child1 = "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText(getActivity(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        Log.d(TAG, "uploadProfileCoverPhoto: Ends");
    }

    private void pickFromGallery() {
        Log.d(TAG, "pickFromGallery: Starts");

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

        Log.d(TAG, "pickFromGallery: Ends");
    }


    private void pickFromCamera() {
        Log.d(TAG, "pickFromCamera: starts");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);

        Log.d(TAG, "pickFromCamera: Ends");
    }

    private void checkUserStatus() {
        Log.d(TAG, "checkUserStatus: Starts");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();

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

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.searchMenu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchMyPosts(s);

                } else {
                    loadMyPosts();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s)) {
                    searchMyPosts(s);

                } else {
                    loadMyPosts();

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