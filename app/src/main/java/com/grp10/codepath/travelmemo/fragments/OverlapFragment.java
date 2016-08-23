package com.grp10.codepath.travelmemo.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grp10.codepath.travelmemo.R;
import com.grp10.codepath.travelmemo.activities.TripActivity;
import com.grp10.codepath.travelmemo.activities.ViewTripActivity;
import com.grp10.codepath.travelmemo.asynctasks.ComputeDominantColorTask;
import com.grp10.codepath.travelmemo.interfaces.DominantColor;
import com.grp10.codepath.travelmemo.interfaces.FragmentLifecycle;
import com.grp10.codepath.travelmemo.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverlapFragment extends Fragment implements DominantColor,FragmentLifecycle{


    @BindView(R.id.image_cover)
    ImageView coverImageView;

    @BindView(R.id.content_layout)
    RelativeLayout layout;

    @BindView(R.id.cardview)
    CardView cardView;

    @BindView(R.id.txtTripName)
    TextView txtTripName;

    @BindView(R.id.txtDate)
    TextView txtDate;

    @BindView(R.id.txtDesc)
    TextView txtDesc;

    Integer color;

    public OverlapFragment() {
        // Required empty public constructor
    }

    int resourceId;
    String tripId;
    String tripName;
    String tripDesc;
    static final String ARG_RES_ID = "ARG_RES_ID";

    public static OverlapFragment newInstance(int resourceId, String name, String description, String tripId) {
        OverlapFragment overlapFragment = new OverlapFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_RES_ID, resourceId);
        bundle.putString(Constants.TRIP_ID, tripId);
        bundle.putString(Constants.TRIP_NAME, name);
        bundle.putString(Constants.DESCRIPTION, description);

        overlapFragment.setArguments(bundle);
        return overlapFragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resourceId = getArguments().getInt(ARG_RES_ID);
        tripId = getArguments().getString(Constants.TRIP_ID);
        tripName = getArguments().getString(Constants.TRIP_NAME);
        tripDesc = getArguments().getString(Constants.DESCRIPTION);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trip_images, container, false);
        ButterKnife.bind(this,rootView);

        Glide.with(getActivity()).load(resourceId).into(coverImageView);
        txtTripName.setText(tripName);
        txtDesc.setText(tripDesc);
        SharedPreferences prefs = getActivity().getSharedPreferences("Colors", Context.MODE_PRIVATE);
        if(!prefs.contains(resourceId+"")) {
            Log.d(Constants.TAG,"we dont have Dominant color ==" + color);

            ComputeDominantColorTask computeDominantColorTask = new ComputeDominantColorTask(getActivity(), this);
            computeDominantColorTask.execute(resourceId);
        }else{
            color = prefs.getInt(resourceId + "",-1);
            cardView.setBackgroundColor(color);
        }
        coverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent viewTripIntent = new Intent(getContext(), ViewTripActivity.class);
                viewTripIntent.putExtra(Constants.TRIP_NAME, tripName);
                viewTripIntent.putExtra(Constants.TRIP_ID, tripId);
                viewTripIntent.putExtra(Constants.DESCRIPTION, tripDesc);
                viewTripIntent.putExtra(Constants.NEW_TRIP, false);
                getContext().startActivity(viewTripIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onDominantColorComputed(Integer color) {

        Log.d(Constants.TAG,"afterExec : Dominant color ==" + color);
        layout.setBackgroundColor(color);
        cardView.setBackgroundColor(color);
        SharedPreferences prefs = getActivity().getSharedPreferences("Colors", Context.MODE_PRIVATE);
        prefs.edit().putInt(resourceId + "",color).apply();

    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment(AppCompatActivity activity) {
        int color =  activity.getSharedPreferences("Colors", Context.MODE_PRIVATE).getInt(resourceId+"",-1);
        Log.d(Constants.TAG,"+++Resume fragment= " + color);
        ((TripActivity)activity).setToolbar(color);
    }
}
