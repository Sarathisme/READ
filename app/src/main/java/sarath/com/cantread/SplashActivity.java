package sarath.com.cantread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Error";
    private static final int RC_SIGN_IN = 100;

    private SignInButton googleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        } catch (NullPointerException e) {
            Log.v(TAG, String.valueOf(e));
        }

        googleSignInButton = findViewById(R.id.googleSignInButton);
        try {
            TextView textView = (TextView) googleSignInButton.getChildAt(0);
            textView.setText(getResources().getString(R.string.sign_in_text));
        } catch (NullPointerException e) {
            Log.e(TAG, "Cannot set text to google button");
        }
        googleSignInButton.setOnClickListener(this);

        try {
            Intent intent = getIntent();
            if(intent.getBooleanExtra("logout", false)) {
                mGoogleSignInClient.signOut();
            }
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.googleSignInButton:
                if(isNetworkAvailable()) {
                    signIn();
                } else {
                    Toast.makeText(this, "No internet connected!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            updateUI(account);
        }
    }

    public void updateUI(GoogleSignInAccount account) {
        if(account != null) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            Toast.makeText(getApplicationContext(), "Welcome, " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No internet or slow connectivity!", Toast.LENGTH_SHORT).show();
        }
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
        super.onBackPressed();
    }
}
