package sarath.com.cantread.view_model;

import android.arch.lifecycle.ViewModel;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

import sarath.com.cantread.model.User;

public class UserViewModel extends ViewModel {

    private final static String ERROR_TAG = "ViewModel Error";
    private final static String DEBUG_TAG = "ViewModel Debug";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("data");
    private StorageReference imageStorageReference;


    private String url = null;

    public DatabaseReference checkForImageExistence(GoogleSignInAccount account, final long timestamp) {
        return databaseReference.child(Objects.requireNonNull(account.getId())).child(String.valueOf(timestamp));
    }

    public String saveImageToMemory(Bitmap bitmap, ContentResolver contentResolver, long timestamp) {
        try {
            url = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Hello", "Can't Read?");
            Log.d("Memory", url);
        } catch (Exception e) {
            Log.e(ERROR_TAG, "Cannot write image into memory" + e.getMessage());
        }
        return url;
    }

    public UploadTask saveImageToDatabase(Bitmap bitmap, GoogleSignInAccount account, long timestamp) {
        String time = String.valueOf(timestamp);
        String path = account.getId() + "_" + time + ".png";

        UploadTask uploadTask;

        imageStorageReference = FirebaseStorage.getInstance().getReference("images/" + path);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            uploadTask = imageStorageReference.putBytes(data);
        } catch (Exception e) {
            Log.e(ERROR_TAG, e.getMessage());
            uploadTask = null;
        }

        return uploadTask;
    }

    public Task<Uri> getUploadedUrl() {
        return imageStorageReference.getDownloadUrl();
    }

    public void saveToDatabase(String url, final GoogleSignInAccount account, String downloadUrl, String text, final long timestamp) {
        final User user = new User(account.getId(), url, downloadUrl, timestamp, text, false, account.getDisplayName());
        try {
            insertIntoDatabase(user, account);
        }catch (Exception e) {
            Log.e(ERROR_TAG, "Cannot insert data into database " + e.getMessage());
        }
    }

    private void insertIntoDatabase(User user, GoogleSignInAccount account) {
        databaseReference.child(Objects.requireNonNull(account.getId())).child(String.valueOf(user.getTimestamp())).setValue(user);
    }
}
