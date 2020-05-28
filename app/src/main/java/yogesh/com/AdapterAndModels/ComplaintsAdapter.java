package yogesh.com.AdapterAndModels;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import yogesh.com.R;

public class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.HolderComplaints> {

    private Context context;
    public ArrayList<ModelsComplaintList> complaintList;

    private FirebaseAuth mAuth;

    public ComplaintsAdapter(Context context, ArrayList<ModelsComplaintList> complaintList) {
        this.context = context;
        this.complaintList = complaintList;

        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComplaints onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_complaint_list, parent, false);

        return new HolderComplaints(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComplaints holder, int position) {
       final ModelsComplaintList modelsComplaintList = complaintList.get(position);
        String id = modelsComplaintList.getComplaintId();
        String uid = modelsComplaintList.getUid();
        String fullname = modelsComplaintList.getFullName();
        String phoneNumber = modelsComplaintList.getPhoneNumber();
        String department = modelsComplaintList.getGovtDepartment();
        String complaintImage = modelsComplaintList.getComplaintImage();
        String complaintDesc = modelsComplaintList.getComplaintDesc();
        String compliantTime = modelsComplaintList.getCompliantTime();
        String latitude = modelsComplaintList.getLatitude();
        String longitude = modelsComplaintList.getLongitude();
        String complaintAddress = modelsComplaintList.getComplaintAddress();
        String status = modelsComplaintList.getStatus();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(compliantTime));
        String pTime = DateFormat.format("dd/mm/yyyy hh:mm aa", calendar).toString();

        holder.complaintId.setText(id);
        holder.department.setText(department);
        holder.time.setText(pTime);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    class HolderComplaints extends RecyclerView.ViewHolder {

        private TextView complaintId, department, time;

        public HolderComplaints(@NonNull View itemView) {
            super(itemView);

            complaintId = itemView.findViewById(R.id.IDTextView);
            department = itemView.findViewById(R.id.departmentTextView);
            time = itemView.findViewById(R.id.timeTextView);

        }
    }
}
