package yogesh.com.AdapterAndModels;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import yogesh.com.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {

    private Context context;
    private List<ModelComments> commentsList;
    private String myUid, postId;

    public CommentsAdapter(Context context, List<ModelComments> commentsList, String myUid, String postId) {
        this.context = context;
        this.commentsList = commentsList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String uid = commentsList.get(position).getUid();
        String name = commentsList.get(position).getuName();
        String email = commentsList.get(position).getuEmail();
        String image = commentsList.get(position).getuDp();
        final String cid = commentsList.get(position).getCommentId();
        final String comment = commentsList.get(position).getComment();
        String timeStamp = commentsList.get(position).getTimstamp();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
//        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String pTime = DateFormat.format("dd/mm/yyyy hh:mm aa", calendar).toString();

        holder.mname.setText(name);
        holder.mcomment.setText(comment);
        holder.mtime.setText(pTime);

        try {
            Picasso.get().load(image).placeholder(R.drawable.default_image).into(holder.mprofile);

        } catch (Exception e) {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUid.equals(uid)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure you want to delete this comment?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();

                } else {
                    Toast.makeText(context, "Cannot Delete other's Comment", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = "" + dataSnapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue("" + newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        private ImageView mprofile;
        private TextView mname, mcomment, mtime;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mprofile = itemView.findViewById(R.id.profileImageView);
            mname = itemView.findViewById(R.id.nameTextView);
            mcomment = itemView.findViewById(R.id.commentTextView);
            mtime = itemView.findViewById(R.id.timeTextView);

        }
    }
}
