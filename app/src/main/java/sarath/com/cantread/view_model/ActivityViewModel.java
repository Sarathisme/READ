package sarath.com.cantread.view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;


public class ActivityViewModel extends ViewModel {

    public ActivityViewModel() {}

    private static final DatabaseReference DATA_REF =
            FirebaseDatabase.getInstance().getReference("data");

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(DATA_REF);

    @NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }

    public void setStarredValue(String userID, long timestamp, boolean starred) {
        DATA_REF.child(userID).child(String.valueOf(timestamp)).child("starred").setValue(starred);
    }

    public void deleteActivity(String userID, long timestamp) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/");
        storageReference.child(userID + "_" + String.valueOf(timestamp) + ".png").delete();

        DATA_REF.child(userID).child(String.valueOf(timestamp)).removeValue();
    }
}


