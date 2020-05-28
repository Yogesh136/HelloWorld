package yogesh.com.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import yogesh.com.Activity.RegisterUserActivity;
import yogesh.com.Activity.SettingsActivity;
import yogesh.com.GovtDepartments;
import yogesh.com.R;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements LocationListener {
    private static final String TAG = "RegisterFragment";

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int LOCATION_REQUEST_CODE = 400;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private String[] locationPermissions;
    private FirebaseAuth mAuth;
    private String email, uid;

    private LocationManager locationManager;

    private double latitude = 0.0, longitude = 0.0;


    private EditText fullname, mobileNumber, description, addressLocation;
    private TextView departments;
    private ImageView capturedImage;
    private Button addImageButton, registerButton;
    private Switch locationSwitch;
    private ProgressDialog mProgressDialog;

    private Uri image_uri = null;


    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        mProgressDialog = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();
        View view = inflater.inflate(R.layout.fragment_register, container, false);


        fullname = view.findViewById(R.id.nameEditText);
        mobileNumber = view.findViewById(R.id.numberEditText);
        description = view.findViewById(R.id.complaintDescriptionEditText);
        capturedImage = view.findViewById(R.id.capturedImageView);
        addImageButton = view.findViewById(R.id.cameraButton);
        registerButton = view.findViewById(R.id.registerbutton);
        locationSwitch = view.findViewById(R.id.locationSwitch);
        addressLocation = view.findViewById(R.id.addressEditText);
        departments = view.findViewById(R.id.departmentTextView);


        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) {
                    pickFromCamera();

                } else {
                    requestCameraPermission();
                }
            }
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkLocationPermission()) {
                    getUserLocation();

                } else {
                    requestLocationPermission();

                }

            }
        });

        departments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                departmentDialog();

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = fullname.getText().toString().trim();
                final String number = mobileNumber.getText().toString().trim();
                final String address = addressLocation.getText().toString().trim();
                final String govtDepartment = departments.getText().toString().trim();
                final String desc = description.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), "Name is Required...", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(number)) {
                    Toast.makeText(getActivity(), "Phone Number is Required...", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(govtDepartment)) {
                    Toast.makeText(getActivity(), "Select Govt Department...", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (latitude == 0.0 || longitude == 0.0) {
                    Toast.makeText(getActivity(), "Please enable GPS to detect Location...", Toast.LENGTH_SHORT).show();
                    return;

                }
                if(!locationSwitch.isChecked()){
                    Toast.makeText(getActivity(), "Enable Location Switch.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(image_uri == null){
                    Toast.makeText(getActivity(), "Image is Required...", Toast.LENGTH_SHORT).show();
                    return;

                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Register Complaint");
                    builder.setMessage("The above details provided by me is correct and can be used for further contacting regarding the issue...");
                    builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            uploadData(name, number, desc, address, govtDepartment, String.valueOf(image_uri));
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                }
//                    uploadData(name, number, desc, address, govtDepartment, String.valueOf(image_uri));
                }

        });

        return view;
    }

    private void departmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Govt Department").setItems(GovtDepartments.govtDepartmentNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String department = GovtDepartments.govtDepartmentNames[which];
                departments.setText(department);
            }
        }).show();

    }

    private void uploadData(final String name, final String number, final String desc, final String address, final String govtDepartment, String valueOf) {
        mProgressDialog.setMessage("Registering Complaint...");
        mProgressDialog.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Complaints/" + "post_" + timestamp;

        Bitmap bitmap = ((BitmapDrawable) capturedImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        reference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("complaintId",""+ timestamp);
                            hashMap.put("uid",""+ uid);
                            hashMap.put("fullName", ""+ name);
                            hashMap.put("phoneNumber", ""+ number);
                            hashMap.put("govtDepartment", ""+ govtDepartment);
                            hashMap.put("complaintImage", ""+ downloadUri);
                            hashMap.put("complaintDesc", ""+ desc);
                            hashMap.put("compliantTime", ""+ timestamp);
                            hashMap.put("latitude", "" + latitude);
                            hashMap.put("longitude", "" + longitude);
                            hashMap.put("complaintAddress", "" + address);
                            hashMap.put("Status","In Progress");


                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(mAuth.getUid()).child("Complaints").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Complaint has been Registered Successfully...", Toast.LENGTH_SHORT).show();
                                            fullname.setText("");
                                            mobileNumber.setText("");
                                            departments.setText("");
                                            description.setText("");
                                            addressLocation.setText("");
                                            capturedImage.setImageURI(null);
                                            image_uri = null;

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(getActivity(), locationPermissions, LOCATION_REQUEST_CODE);
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
            uid = currentUser.getUid();

        } else {
            startActivity(new Intent(getActivity(), RegisterUserActivity.class));
            getActivity().finish();

        }
    }

    private void getUserLocation() {
        Toast.makeText(getActivity(), "Please Wait...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();

    }

    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);

            addressLocation.setText(address);

        } catch (Exception e) {
            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), "Please Enable Location...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.searchMenu).setVisible(false);
        MenuItem item = menu.findItem(R.id.searchMenu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();

                    } else {
                        Toast.makeText(getActivity(), "Camera Permission is Necessary...", Toast.LENGTH_SHORT).show();

                    }

                } else {

                }
            }
            break;

            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted) {
                        getUserLocation();

                    } else {
                        Toast.makeText(getActivity(), "Location Permission are Necessary...", Toast.LENGTH_SHORT).show();

                    }

                } else {

                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                capturedImage.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
