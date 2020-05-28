package yogesh.com.AdapterAndModels;

public class ModelsComplaintList {

    private String complaintId, uid, fullName, phoneNumber, govtDepartment, complaintImage, complaintDesc, compliantTime, latitude, longitude, complaintAddress, Status;

    public ModelsComplaintList() {
    }

    public ModelsComplaintList(String complaintId, String uid, String fullName,
                               String phoneNumber, String govtDepartment, String complaintImage,
                               String complaintDesc, String compliantTime, String latitude,
                               String longitude, String complaintAddress, String status) {
        this.complaintId = complaintId;
        this.uid = uid;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.govtDepartment = govtDepartment;
        this.complaintImage = complaintImage;
        this.complaintDesc = complaintDesc;
        this.compliantTime = compliantTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.complaintAddress = complaintAddress;
        Status = status;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGovtDepartment() {
        return govtDepartment;
    }

    public void setGovtDepartment(String govtDepartment) {
        this.govtDepartment = govtDepartment;
    }

    public String getComplaintImage() {
        return complaintImage;
    }

    public void setComplaintImage(String complaintImage) {
        this.complaintImage = complaintImage;
    }

    public String getComplaintDesc() {
        return complaintDesc;
    }

    public void setComplaintDesc(String complaintDesc) {
        this.complaintDesc = complaintDesc;
    }

    public String getCompliantTime() {
        return compliantTime;
    }

    public void setCompliantTime(String compliantTime) {
        this.compliantTime = compliantTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getComplaintAddress() {
        return complaintAddress;
    }

    public void setComplaintAddress(String complaintAddress) {
        this.complaintAddress = complaintAddress;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
