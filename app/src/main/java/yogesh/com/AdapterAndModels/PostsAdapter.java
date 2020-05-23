package yogesh.com.AdapterAndModels;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import yogesh.com.Activity.AddPostActivity;
import yogesh.com.Activity.OthersProfileActivity;
import yogesh.com.Activity.PostDetailsActivity;
import yogesh.com.R;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    private Context context;
    private List<ModelPost> postList;
    private String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    private boolean mProcessLike = false;

    public PostsAdapter(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        final String pId = postList.get(position).getpId();
        final String pTitle = postList.get(position).getpTitle();
        final String pDescription = postList.get(position).getpDescr();
        final String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
//        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/mm/yyyy hh:mm aa", calendar).toString();


        holder.userName.setText(uName);
        holder.postTime.setText(pTime);
        holder.postTitle.setText(pTitle);
        holder.postDescription.setText(pDescription);
        holder.postLikes.setText(pLikes + " Likes");
        holder.postComments.setText(pComments + " Comments");

        setLikes(holder, pId);

        try {
            Picasso.get().load(uDp).placeholder(R.drawable.default_image).into(holder.userPictureImage);

        } catch (Exception e) {

        }

        if (pImage.equals("noImage")) {
            holder.postImage.setVisibility(View.GONE);

        } else {
            holder.postImage.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).placeholder(R.drawable.default_image).into(holder.postImage);

            } catch (Exception e) {

            }
        }

        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreButton, uid, myUid, pId, pImage);
            }
        });

        holder.mlikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;

                final String postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLike) {
                            if (dataSnapshot.child(postIde).hasChild(myUid)) {
                                postsRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;

                            } else {
                                postsRef.child(postIde).child("pLikes").setValue("" + (pLikes + 1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;

                                addToHisnotifications(""+ uid, "" + pId, "Liked your post.");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        holder.mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        holder.mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.postImage.getDrawable();
                if (bitmapDrawable == null) {
                    shareTextOnly(pTitle, pDescription);

                } else {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);

                }
            }
        });

        holder.mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
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
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription;

        Uri uri = saveImageToShare(bitmap);

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "yogesh.com.fileprovider", file);

        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)) {
                    holder.mlikeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_icon, 0, 0, 0);
                    holder.mlikeButton.setText("Liked");

                } else {
                    holder.mlikeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
                    holder.mlikeButton.setText("Like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showMoreOptions(ImageButton moreButton, String uid, String myUid, final String pId, final String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreButton, Gravity.END);

        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Details");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    beginDelete(pId, pImage);

                } else if (id == 1) {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

                } else if (id == 2) {
                    Intent intent = new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);

                }

                return false;
            }
        });
        popupMenu.show();

    }


    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")) {
            deleteWithoutImage(pId);

        } else {
            deleteWithImage(pId, pImage);

        }

    }

    private void deleteWithImage(final String pId, String pImage) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting Post...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting Post...");

        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {


        private ImageView userPictureImage, postImage;
        private TextView userName, postTime, postTitle, postDescription, postLikes, postComments;
        private ImageButton moreButton;
        private Button mlikeButton, mCommentButton, mShareButton;
        private LinearLayout mProfileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            userPictureImage = itemView.findViewById(R.id.userPicImageView);
            postImage = itemView.findViewById(R.id.postImageView);
            userName = itemView.findViewById(R.id.userNameTextView);
            postTime = itemView.findViewById(R.id.postTimeTextView);
            postTitle = itemView.findViewById(R.id.postTitleTextView);
            postDescription = itemView.findViewById(R.id.postDescriptionTextView);
            postLikes = itemView.findViewById(R.id.postLikesTextView);
            postComments = itemView.findViewById(R.id.postCommentsTextView);
            moreButton = itemView.findViewById(R.id.moreButton);
            mlikeButton = itemView.findViewById(R.id.likeButton);
            mCommentButton = itemView.findViewById(R.id.commentButton);
            mShareButton = itemView.findViewById(R.id.shareButton);
            mProfileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
