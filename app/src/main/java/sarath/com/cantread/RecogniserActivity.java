package sarath.com.cantread;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.PathUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Objects;

import sarath.com.cantread.view_model.OCRViewModel;
import sarath.com.cantread.view_model.UserViewModel;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RecogniserActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String DEBUG_TAG = "Debug";
    private final static String ERROR_TAG = "Error";

    private final static int RESULT_LOAD_IMAGE = 1;
    private final static int RESULT_CAPTURE_IMAGE = 0;

    private EditText analysedTextView;
    private Snackbar snackbar;
    private FloatingActionMenu menu;

    private Bitmap bitmap;
    private GoogleSignInAccount account;

    private UserViewModel userViewModel;
    private OCRViewModel ocrViewModel;

    private SharedPreferences preferences;

    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            account = GoogleSignIn.getLastSignedInAccount(this);
            userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
            ocrViewModel = ViewModelProviders.of(this).get(OCRViewModel.class);
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }catch(NullPointerException e) {
            account = null;
        }

        setContentView(R.layout.activity_recogniser);

        analysedTextView = findViewById(R.id.analysedText);
        snackbar  = Snackbar.make(findViewById(R.id.rootLayout), "Processing", Snackbar.LENGTH_INDEFINITE);
        menu = findViewById(R.id.menu);

        com.github.clans.fab.FloatingActionButton cameraFab = findViewById(R.id.cameraFab);
        com.github.clans.fab.FloatingActionButton galleryFab = findViewById(R.id.galleryFab);

        cameraFab.setOnClickListener(this);
        galleryFab.setOnClickListener(this);
        analysedTextView.setEnabled(false);

        if(getIntent().getStringExtra("data") != null) {
            analysedTextView.setText(getIntent().getStringExtra("data"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recogniser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save:
                if(analysedTextView.getText().length() != 0) {
                    if(isNetworkAvailable()) {
                        addDataToDatabase(bitmap);
                    } else {
                        Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "No text found!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.share:
                if(analysedTextView.getText().length() != 0) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, analysedTextView.getText());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(this, "No text found!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.cameraFab) {
            menu.close(true);
            try {
                if (ActivityCompat.checkSelfPermission(RecogniserActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RecogniserActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, RESULT_CAPTURE_IMAGE);
                } else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, RESULT_CAPTURE_IMAGE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(v.getId() == R.id.galleryFab) {
            menu.close(true);
            if (ActivityCompat.checkSelfPermission(RecogniserActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RecogniserActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_LOAD_IMAGE);
            } else {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(Objects.requireNonNull(selectedImage),
                    filePathColumn, null, null, null);
            Objects.requireNonNull(cursor).moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            bitmap = BitmapFactory.decodeFile(picturePath);

            timestamp = System.currentTimeMillis();
            Log.w("Timestamp", String.valueOf(timestamp));
            processImage(bitmap);

        } else if (requestCode == RESULT_CAPTURE_IMAGE && resultCode == RESULT_OK && null != data) {
            Bundle extras = data.getExtras();
            try {
                bitmap = (Bitmap) Objects.requireNonNull(extras).get("data");
            } catch (Exception e) {
                Log.e(ERROR_TAG, "Cannot recieve data from camera!");
            }

            timestamp = System.currentTimeMillis();
            if(bitmap != null) {
                processImage(bitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                } else {
                    Toast.makeText(this, "Permission required!", Toast.LENGTH_SHORT).show();
                }
                break;

            case RESULT_CAPTURE_IMAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, RESULT_CAPTURE_IMAGE);
                    }
                } else {
                    Toast.makeText(this, "Permission required!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void processImage(Bitmap bitmap) {
        snackbar.show();

        FirebaseVisionImage image = ocrViewModel.createFirebaseVisionImage(bitmap);

        ocrViewModel.process(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                snackbar.dismiss();
                analysedTextView.setText(firebaseVisionText.getText());
                analysedTextView.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(ERROR_TAG, "Cannot recognize langauge " + e.getMessage());
                Toast.makeText(RecogniserActivity.this, "Cannot recognize language!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addDataToDatabase(final Bitmap bitmap) {
        DatabaseReference reference = userViewModel.checkForImageExistence(account, timestamp);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() <= 0) {
                    insertImageIntoDatabase(bitmap);
                } else {
                    Toast.makeText(RecogniserActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RecogniserActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertImageIntoDatabase(Bitmap bitmap) {
        final String url;

        if(preferences.getBoolean("local", true)) {
            url = userViewModel.saveImageToMemory(bitmap, getContentResolver(), timestamp);
        } else {
            url = null;
        }

        Toast.makeText(this, "Uploading to database, please wait......", Toast.LENGTH_LONG).show();

        UploadTask uploadTask = userViewModel.saveImageToDatabase(bitmap, account, timestamp);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(ERROR_TAG, "Cannot upload to database " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(DEBUG_TAG, "Finished!");

                Task<Uri> uri = userViewModel.getUploadedUrl();
                uri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        insertUserDataIntoDatabase(url, uri.toString());
                    }
                });
            }
        });
    }

    private void insertUserDataIntoDatabase(String url, String downloadUrl) {
        userViewModel.saveToDatabase(url, account, downloadUrl, analysedTextView.getText().toString(), timestamp);
        Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
