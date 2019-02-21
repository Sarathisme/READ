package sarath.com.cantread.fragment;

import android.Manifest;
import android.app.ActivityOptions;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import sarath.com.cantread.ItemAdapter;
import sarath.com.cantread.OCRDetailActivity;
import sarath.com.cantread.R;
import sarath.com.cantread.SwipeToDeleteCallback;
import sarath.com.cantread.interfaces.RecyclerViewClickListener;
import sarath.com.cantread.model.User;
import sarath.com.cantread.view_model.ActivityViewModel;

import static java.util.Objects.*;

/**
 * A simple {@link Fragment} subclass.
 */

    public class ActivityFragment extends Fragment implements RecyclerViewClickListener {

    private final static String DEBUG_TAG = "ActivityFragment Debug";
    private final static String ERROR_TAG = "ActivityFragment Error";
    private static final int REQUEST_CODE = 1;

    private LiveData<DataSnapshot> liveData;
    private ActivityViewModel viewModel;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private ItemAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    RecyclerViewClickListener mListener;

    private ArrayList<User> mUsers = new ArrayList<>();

    private GoogleSignInAccount account;

    public ActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_activity, container, false);

        // Set Activity title
        try {
            requireNonNull(getActivity()).setTitle("Activity");
        } catch (NullPointerException e) {
            Log.e(ERROR_TAG, "Cannot set activity title");
        }

        // Instantiate progress bar
        progressBar = view.findViewById(R.id.activityProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Instantiate Layout Manager, RecyclerView and Adapter
        try {
            account = GoogleSignIn.getLastSignedInAccount(requireNonNull(getContext()));
        }catch (NullPointerException e) {
            account = null;
        }

        // Setup RecyclerView, Layout Manager and Adapter
        setupRecyclerView(view);

        // Initiate ViewModel and LiveData
        viewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);
        liveData = viewModel.getDataSnapshotLiveData();

        if(isNetworkAvailable()) {
            getUsersFromDatabase();
        } else {
            Toast.makeText(getContext(), "No internet connection!", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void setupRecyclerView(final View view) {
        recyclerView = view.findViewById(R.id.activityRecyclerView);
        mListener = this;

        adapter = new ItemAdapter(mUsers, getContext(), mListener);
        layoutManager = new LinearLayoutManager(getContext());

        // Add layout manager and adapter
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(){
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                User user = mUsers.get(position);
                viewModel.deleteActivity(user.getUserID(), user.getTimestamp());
                mUsers.remove(position);
                try {
                    Objects.requireNonNull(getContext()).getContentResolver().delete(Uri.parse(user.getImageLocalUrl()), null, null);
                }catch (NullPointerException ignored) {

                }
                adapter.notifyItemRemoved(position);

                Snackbar.make(view, "Deleted", Snackbar.LENGTH_SHORT).show();
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void getUsersFromDatabase() {
        liveData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    // Update the UI here with values in the snapshot
                    mUsers.clear();
                    if(account != null) {
                        try {
                            for(DataSnapshot data: dataSnapshot.child(requireNonNull(account.getId())).getChildren()) {
                                User user = data.getValue(User.class);
                                mUsers.add(user);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                            adapter.notifyDataSetChanged();
                        }catch (NullPointerException e) {
                            Log.e(ERROR_TAG, "Account not found!");
                        }
                    }
                }
            }
        });

        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onButtonClick(View v, int position) {
        User user = mUsers.get(position);
        if(user.isStarred()) {
            changeStarredStatus(false, R.drawable.ic_star_border_black_24dp, user, v);
        } else {
            changeStarredStatus(true, R.drawable.ic_star_yellow_24dp, user, v);
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(getActivity(), OCRDetailActivity.class);
        intent.putExtra("imageUrl", mUsers.get(position).getImageCloudUrl());
        intent.putExtra("text", mUsers.get(position).getOcrText());
        intent.putExtra("date", processDate(mUsers.get(position).getTimestamp()));

        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(getActivity(), v, "image");

        startActivity(intent, options.toBundle());
    }

    private String processDate(long raw) {
        String rawDate = new Date(raw).toString();
        String[] dateArray = rawDate.split("GMT");
        return dateArray[0];
    }

    private void changeStarredStatus(boolean starred, int icon, User user, View v) {
        viewModel.setStarredValue(user.getUserID(), user.getTimestamp(), starred);
        user.setStarred(starred);
        ((ImageButton)v).setImageDrawable(Objects.requireNonNull(getContext()).getDrawable(icon));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
