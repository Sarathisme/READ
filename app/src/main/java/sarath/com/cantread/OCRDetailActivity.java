package sarath.com.cantread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import sarath.com.cantread.R;

public class OCRDetailActivity extends AppCompatActivity {

    private static final String ERROR_TAG = "OCRActivity Error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inside your activity (if you did not enable transitions in your theme)
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // Set an exit transition
        getWindow().setExitTransition(new Explode());

        setContentView(R.layout.activity_ocrdetail);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch(NullPointerException e) {
            Log.e(ERROR_TAG, "Cannot get action bar!");
        }

        TextView ocrDateView = findViewById(R.id.ocrDetailActivityDateTextView);
        TextView ocrTextView = findViewById(R.id.ocrDetailActivityTextView);

        try {
            Intent intent = getIntent();
            ocrDateView.setText(intent.getStringExtra("date"));
            ocrTextView.setText(intent.getStringExtra("text"));
            Picasso.get().load(intent.getStringExtra("imageUrl")).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    findViewById(R.id.toolbar_layout).setBackground(new BitmapDrawable(getResources(), bitmap));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }catch (NullPointerException e) {
            Log.e(ERROR_TAG, "Cannot retrieve data");
        }
    }
}
