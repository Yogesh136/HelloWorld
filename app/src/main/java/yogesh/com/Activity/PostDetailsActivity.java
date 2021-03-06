package yogesh.com.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import yogesh.com.AdapterAndModels.CommentsAdapter;
import yogesh.com.AdapterAndModels.ModelComments;
import yogesh.com.R;

public class PostDetailsActivity extends AppCompatActivity {

    private String hisUid, myUid, myEmail, myName, myDp, postId, pLikes, hisDp, hisName, pImage;

    private boolean mProcessComment = false;
    private boolean mProcessLike = false;

    private ImageView userPic, postPic;
    private TextView name, postTime, postTitle, postDescription, postLikes, postComments;
    private ImageButton moreButton;
    private Button likeButton, shareButton;
    private LinearLayout profileLayout;
    private RecyclerView recyclerView;

    private List<ModelComments> commentList;
    private CommentsAdapter commentsAdapter;

    private ProgressDialog mProgressDialog;

    private EditText commentText;
    private ImageButton sendButton;
    private ImageView commentPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        userPic = findViewById(R.id.userPicImageView);
        postPic = findViewById(R.id.postImageView);
        name = findViewById(R.id.userNameTextView);
        postTime = findViewById(R.id.postTimeTextView);
        postTitle = findViewById(R.id.postTitleTextView);
        postDescription = findViewById(R.id.postDescriptionTextView);
        postLikes = findViewById(R.id.postLikesTextView);
        postComments = findViewById(R.id.postCommentsTextView);
        moreButton = findViewById(R.id.moreButton);
        likeButton = findViewById(R.id.likeButton);
        shareButton = findViewById(R.id.shareButton);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);


        commentText = findViewById(R.id.commentEditText);
        sendButton = findViewById(R.id.sendButton);
        commentPic = findViewById(R.id.commentProfileImageView);


        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();

        actionBar.setSubtitle("SignedIn as: " + myEmail);

        loadComments();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = postTitle.getText().toString().trim();
                String pDescription = postDescription.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable) postPic.getDrawable();
                if (bitmapDrawable == null) {
                    shareTextOnly(pTitle, pDescription);

                } else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);

                }

            }
        });
    }

    private void addToHisnotifications(String hisUid, String pId, String notification) {
        String timestamp = "" + System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle + "\n" + pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription;

        Uri uri = saveImageToShare(bitmap);

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "yogesh.com.fileprovider", file);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelComments modelComments = ds.getValue(ModelComments.class);

                    commentList.add(modelComments);

                    commentsAdapter = new CommentsAdapter(getApplicationContext(), commentList, myUid, postId);
                    recyclerView.setAdapter(commentsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreButton, Gravity.END);

        if (hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    beginDelete();

                } else if (id == 1) {
                    Intent intent = new Intent(PostDetailsActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);

                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if (pImage.equals("noImage")) {
            deleteWithoutImage();

        } else {
            deleteWithImage();

        }
    }

    private void deleteWithImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting Post...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailsActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting Post...");

        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailsActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)) {
                    likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_icon, 0, 0, 0);
                    likeButton.setText("Liked");

                } else {
                    likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
                    likeButton.setText("Like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        mProcessLike = true;
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLike) {
                    if (dataSnapshot.child(postId).hasChild(myUid)) {
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) - 1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                    } else {

                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;

                        addToHisnotifications("" + hisUid, "" + postId, "Liked your post.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Posting Comment...");

        String comment = commentText.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Comment is Empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                commentText.setText("");
                updateCommentCount();

                addToHisnotifications("" + hisUid, "" + postId, "Commented on your Post.");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateCommentCount() {
        mProcessComment = true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessComment) {
                    String comments = "" + dataSnapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue("" + newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();

                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.profile_image).into(commentPic);

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.profile_image).into(commentPic);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/mm/yyyy hh:mm aa", calendar).toString();

                    postTitle.setText(pTitle);
                    postDescription.setText(pDescr);
                    postLikes.setText(pLikes + " Likes");
                    postTime.setText(pTime);
                    postComments.setText(commentCount + " Comments");

                    name.setText(hisName);

                    if (pImage.equals("noImage")) {
                        postPic.setVisibility(View.GONE);

                    } else {
                        postPic.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).placeholder(R.drawable.default_image).into(postPic);

                        } catch (Exception e) {

                        }
                    }

                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.default_image).into(userPic);

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.profile_image).into(userPic);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myEmail = user.getEmail();
            myUid = user.getUid();

        } else {
            startActivity(new Intent(this, LoginUserActivity.class));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.searchMenu).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoutMenu) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
