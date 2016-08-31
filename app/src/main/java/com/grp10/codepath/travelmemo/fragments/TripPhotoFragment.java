package com.grp10.codepath.travelmemo.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grp10.codepath.travelmemo.R;
import com.grp10.codepath.travelmemo.activities.KickflipActivity;
import com.grp10.codepath.travelmemo.activities.ViewPhotoActivity;
import com.grp10.codepath.travelmemo.firebase.Memo;
import com.grp10.codepath.travelmemo.utils.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by traviswkim on 8/18/16.
 */
public class TripPhotoFragment extends Fragment {
    static final String ARG_TRIP_ID = "ARG_TRIP_ID";
    private DatabaseReference mFbDBReference;
    private String tripId;
    StaggeredGridLayoutManager layoutManager;
    @BindView(R.id.rvTripPhotos) RecyclerView rvTripPhotos;

    private Context mContext;
    private FirebaseRecyclerAdapter<Memo, PhotoViewHolder> adapter;
    private ArrayList<Memo> memoList;

    public TripPhotoFragment() {

    }

    public static TripPhotoFragment newInstance(Context context, String tripId) {
        TripPhotoFragment tripPhotoFragment = new TripPhotoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TRIP_ID, tripId);
        tripPhotoFragment.setArguments(bundle);
        return tripPhotoFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFbDBReference = FirebaseDatabase.getInstance().getReference();
        tripId = getArguments().getString(ARG_TRIP_ID);
//        tripId = "-KPinKfmgOsZFl-55mNN";
        mFbDBReference = mFbDBReference.child("trips").child(tripId).child("Memos");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_photos, parent, false);
        ButterKnife.bind(this, v);

        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);
        rvTripPhotos.hasFixedSize();
        rvTripPhotos.setLayoutManager(layoutManager);
        rvTripPhotos.setHasFixedSize(true);
        // Required to clear image when the view is recycled
// See  : https://github.com/bumptech/glide/issues/710
//model - Memo{owner=akshat, type='photo', media_url='https://firebasestorage.googleapis.com/v0/b/travelmemo-1de8a.appspot.com/o/fufu%2Fcom.google.android.gms.internal.zzafu%40fc82d7e%2F20082016170329.jpg?alt=media&token=a0b80a34-222d-4b05-b1e6-a40904a50dc1'}
        adapter = new FirebaseRecyclerAdapter<Memo, PhotoViewHolder>(Memo.class, R.layout.item_trip_photo, PhotoViewHolder.class, mFbDBReference) {

           @Override
           public void onViewRecycled(PhotoViewHolder holder) {
               super.onViewRecycled(holder);
               // Required to clear image when the view is recycled
               // See  : https://github.com/bumptech/glide/issues/710
               Glide.clear(holder.tripPhoto);
           }

           @Override
           protected void populateViewHolder(PhotoViewHolder viewHolder, Memo model, int position) {
               //model - Memo{owner=akshat, type='photo', media_url='https://firebasestorage.googleapis.com/v0/b/travelmemo-1de8a.appspot.com/o/fufu%2Fcom.google.android.gms.internal.zzafu%40fc82d7e%2F20082016170329.jpg?alt=media&token=a0b80a34-222d-4b05-b1e6-a40904a50dc1'}
               if("video".equals(model.getType()) || model.getType().equals("photo")){
                   final String pictureString = model.getMediaUrl();
                   if(mContext != null) {
                       if ("video".equals(model.getType())) {
                           Glide.with(mContext).load(model.getThumbnail_url()).diskCacheStrategy(DiskCacheStrategy.ALL)
                                   .fitCenter().into(viewHolder.tripPhoto);
                           viewHolder.tripText.setText(model.getText());
                           Log.d(Constants.TAG, "Media URL == " + pictureString);
                           viewHolder.tripPhoto.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View view) {
                                   Intent i = new Intent(mContext, KickflipActivity.class);
                                   i.setData(Uri.parse(pictureString));
                                   mContext.startActivity(i);
                               }
                           });
                       } else {
                           Glide.with(mContext).load(pictureString).diskCacheStrategy(DiskCacheStrategy.ALL)
                                   .fitCenter().into(viewHolder.tripPhoto);
                           viewHolder.tripText.setText(model.getText());
                           Log.d(Constants.TAG, "Media URL == " + pictureString);

                           viewHolder.tripPhoto.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View view) {
                                   Intent i = new Intent(mContext, ViewPhotoActivity.class);
                                   i.putParcelableArrayListExtra("Photos", memoList);
                                   mContext.startActivity(i);
                               }
                           });
                       }

                   }
               }
           }
       };

        // Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                layoutManager.smoothScrollToPosition(rvTripPhotos, null, adapter.getItemCount());
            }
        });


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        rvTripPhotos.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.cleanup();
        mContext = null;
    }

    public void setMemoList(ArrayList<Memo> listMemos) {
        this.memoList = listMemos;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.ivTripPhoto) ImageView tripPhoto;
        @BindView(R.id.tvTripText) TextView tripText;
        public PhotoViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }


    }
}
