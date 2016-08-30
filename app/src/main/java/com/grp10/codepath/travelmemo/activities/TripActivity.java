package com.grp10.codepath.travelmemo.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.grp10.codepath.travelmemo.R;
import com.grp10.codepath.travelmemo.data.DemoImages;
import com.grp10.codepath.travelmemo.firebase.FirebaseUtil;
import com.grp10.codepath.travelmemo.firebase.Trip;
import com.grp10.codepath.travelmemo.firebase.User;
import com.grp10.codepath.travelmemo.fragments.OverlapFragment;
import com.grp10.codepath.travelmemo.interfaces.FragmentLifecycle;
import com.grp10.codepath.travelmemo.utils.Constants;
import com.grp10.codepath.travelmemo.view.CustomToolbar;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Badgeable;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

public class TripActivity extends AppCompatActivity {

    private static final String TAG = Constants.TAG;

    @BindView(R.id.pager_container)
    PagerContainer pagerContainer;

    @BindView(R.id.pager_container_friends)
    PagerContainer pagerContainerFriend;

    //    @BindView(R.id.viewpager)
    ViewPager viewPager;
    ViewPager viewPagerFriends;

//    @BindView(R.id.layout_1)
//    View row1;
//
//    @BindView(R.id.layout_2)
//    View row2;


    @BindView(R.id.toolbar)
    CustomToolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private Drawer result;
    private MyFragmentPagerAdapter pagerAdapter;
    private MyFragmentPagerAdapter pagerFriendsAdapter;

    private DatabaseReference mFirebaseDatabaseReference;

    private String mUsername;
    private String mUserId;
    private List<Trip> tripList;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

//        pagerContainer = (PagerContainer) row1.findViewById(R.id.pager_container);
//        pagerContainerFriend = (PagerContainer) row2.findViewById(R.id.pager_container);
        // Initialize Current User
        mUsername = FirebaseUtil.getCurrentUserName();
        mUserId = FirebaseUtil.getCurrentUserId();
        
        getMaterialDrawerMenu();
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        setupCarousal();
        setupFriendsCarousal();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTripDialog();
            }
        });
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
//        Constants.colorizeToolbar(toolbar,R.color.colorPrimary,this);
    }

    private void addInvitedTripListener() {
        mFirebaseDatabaseReference.child("user-trips").child(mUserId).child("trips").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"++++ OnChildAdded");
//                getUserTrips();
                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                Trip trip = parseTripDetails(map);
                if(tripList == null)
                    tripList = new ArrayList<Trip>();
                tripList.add(trip);

                Log.d(TAG,"Total Trips == " + tripList.size());

                refreshCarousalView();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"++++ OnChildChanged");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG,"++++ onChildRemoved");
                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                String id = null;
                Log.d(TAG, "removing maps  == " + map);
                id = (String) map.get("id");

                Iterator<Trip> iterator = tripList.iterator();
                while (iterator.hasNext()){
                    Trip trip = iterator.next();
                    if(trip.getId().equals(id))
                        iterator.remove();
                }

                Log.d(TAG,"Total Trips == " + tripList.size());

                refreshCarousalView();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"++++ onChildMoved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"++++ onCancelled");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserTrips();
//        addInvitedTripListener();
    }

    private void getUserTrips() {
        Log.d(TAG,"userId name == " + mUserId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data == " + dataSnapshot.toString());

//                        if(tripList == null)
                tripList = new ArrayList<>();

                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>) dataSnapshot1.getValue();

                        for (String key : map.keySet()) {
                            Log.d(TAG, "maps  == " + map.get(key));
                            HashMap<String, Object> map2 = map.get(key);
                            Trip trip = parseTripDetails(map2);
                            if (trip != null)
                                tripList.add(trip);
                        }
                    }

                } else {          // for fixing a crash when you delete the last trip and create a new one.
                    tripList.clear();
                }
                Log.d(TAG, "Total Trips == " + tripList.size());
                Collections.sort(tripList);
                refreshCarousalView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mFirebaseDatabaseReference.child("user-trips").child(mUserId)
                .addValueEventListener(valueEventListener);

    }

    private Trip parseTripDetails(HashMap<String, Object> map2) {
        Trip trip = new Trip();
        trip.setName((String) map2.get("name"));
        trip.setId((String) map2.get("id"));
        if (tripList.contains(trip)) {
            Log.d(TAG, "List aleadry contains the trip : " + trip.getName());
            return null;
        } else
        {
            trip.setIsFavorite((Boolean) map2.get("isFavorite"));
            trip.setDescription((String) map2.get("description"));
            trip.setStartDate((Long) map2.get("startDate"));

            User owner = new User();
            HashMap<String, String> mapOwners = (HashMap<String, String>) map2.get("owner");
            owner.setName(mapOwners.get("name"));
            owner.setUid(mapOwners.get("uid"));
            trip.setOwner(owner);

            List<User> travellers = new ArrayList<User>();
//                                    List<HashMap<String, String>> listTravellers = (List<HashMap<String, String>>) map2.get("Travellers");
//                                    for (HashMap<String, String> members : listTravellers) {
//                                        User member = new User();
//                                        member.setName(members.get("name"));
//                                        member.setUid(members.get("uid"));
//                                        travellers.add(member);
//                                    }
            trip.setTravellers(travellers);
            Log.d(TAG, "Trip name == " + trip.getName());
            Log.d(TAG, "Trip id == " + trip.getId());
            Log.d(TAG, "Trip owner == " + trip.getOwner());
            Log.d(TAG, "Trip travelers == " + trip.getTravellers());
            return trip;
        }
    }

    public void refreshCarousalView() {
        if(pagerAdapter != null) {
            pagerAdapter.setTripList(tripList);
            pagerAdapter.notifyDataSetChanged();
            Log.d(TAG,"+++ PagerAdapter count == " + pagerAdapter.getCount());

        }else {
            updateCarousalView();
        }
        if(pagerFriendsAdapter != null) {
            pagerFriendsAdapter.setTripList(tripList);
            pagerFriendsAdapter.notifyDataSetChanged();
            Log.d(TAG,"+++ PagerAdapter count == " + pagerFriendsAdapter.getCount());

        }else {
            updateFriendsCarousalView();
        }
    }

    private void updateCarousalView() {

        if(pagerAdapter == null) {
            pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(),tripList);
        }
        Log.d(TAG,"+++ PagerAdapter count == " + pagerAdapter.getCount());
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
        viewPager.setAdapter(pagerAdapter);

        if(pagerAdapter.getCount() > 0) {
            //Manually setting the first View to be elevated
            viewPager.post(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
//                    if(fragment != null)
//                        ViewCompat.setElevation(fragment.getView(), 8.0f);
                }
            });
        }

    }

    private void updateFriendsCarousalView() {

        if(pagerFriendsAdapter == null) {
            pagerFriendsAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), tripList);
        }

        Log.d(TAG,"+++ pagerFriendsAdapter count == " + pagerAdapter.getCount());

        viewPagerFriends.setOffscreenPageLimit(pagerFriendsAdapter.getCount());
        viewPagerFriends.setAdapter(pagerFriendsAdapter);

        if(pagerFriendsAdapter.getCount() > 0) {
            //Manually setting the first View to be elevated
            viewPagerFriends.post(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = (Fragment) viewPagerFriends.getAdapter().instantiateItem(viewPagerFriends, 0);
//                    ViewCompat.setElevation(fragment.getView(), 8.0f);
                }
            });
        }

    }
    public void getMaterialDrawerMenu(){
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(mUsername).withEmail(FirebaseUtil.getCurrentUserEmail())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        //create the drawer and remember the `Drawer` result object
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.HOME).withIcon(GoogleMaterial.Icon.gmd_home),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.print).withIcon(GoogleMaterial.Icon.gmd_print),
                        new SecondaryDrawerItem().withName(R.string.friends).withIcon(GoogleMaterial.Icon.gmd_nature_people),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.profile).withIcon(GoogleMaterial.Icon.gmd_verified_user),
                        new SecondaryDrawerItem().withName(R.string.SETTINGS).withIcon(GoogleMaterial.Icon.gmd_settings),
                        new SecondaryDrawerItem().withName(R.string.LOGOUT).withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        if (drawerItem != null) {
                            if (drawerItem instanceof Nameable) {
                                if(((Nameable) drawerItem).getName().getText(TripActivity.this).equals(getString(R.string.LOGOUT))){
                                    FirebaseAuth.getInstance().signOut() ;
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user == null) {
                                        Toast.makeText(TripActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                                        getSharedPreferences("UserKey", MODE_PRIVATE).edit().clear().apply();
                                        startActivity(new Intent(TripActivity.this, SignInActivity.class));
                                        finish();
                                    }
                                }
                            }

                            if (drawerItem instanceof Badgeable) {
                                Badgeable badgeable = (Badgeable) drawerItem;
                                if (badgeable.getBadge() != null) {
                                    //note don't do this if your badge contains a "+"
                                    //only use toString() if you set the test as String
                                    int badge = Integer.valueOf(badgeable.getBadge().toString());
                                    if (badge > 0) {
                                        badgeable.withBadge(String.valueOf(badge - 1));
                                        result.updateItem(drawerItem);
                                    }
                                }
                            }
                        }

                        return false;
                    }
                })
                .build();

    }
    private void setupCarousal() {

        viewPager = pagerContainer.getViewPager();

        //Set this to have a carousal effect
        new CoverFlow.Builder().with(viewPager)
                .scale(0.3f)
                .pagerMargin(getResources().getDimensionPixelSize(R.dimen.overlap_pager_margin))
                .spaceSize(0f)
                .build();

        // Enable this for a linear page view
//        viewPager.setPageMargin(30);

        viewPager.setClipChildren(false);
//        pagerContainer.setOverlapEnabled(true);
        viewPager.addOnPageChangeListener(pageChangeListener);

    }

    private void setupFriendsCarousal() {

        viewPagerFriends = pagerContainerFriend.getViewPager();

        //Set this to have a carousal effect
        new CoverFlow.Builder().with(viewPagerFriends)
                .scale(0.3f)
                .pagerMargin(getResources().getDimensionPixelSize(R.dimen.overlap_pager_margin))
                .spaceSize(0f)
                .build();

        // Enable this for a linear page view
//        viewPagerFriends.setPageMargin(30);

        viewPagerFriends.setClipChildren(false);
//        pagerContainer.setOverlapEnabled(true);
        viewPagerFriends.addOnPageChangeListener(friendPageChangeListener);

    }
    private void createTripDialog() {
        View messageView = LayoutInflater.from(TripActivity.this).
                inflate(R.layout.create_trip_dialog, null);
        // Create alert dialog builder
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set create_trip_dialog.xml to AlertDialog builder
        alertDialogBuilder.setView(messageView).setCancelable(false).setTitle("Create a new trip");
        alertDialogBuilder.setPositiveButton("Ok",null).setNegativeButton("Cancel",null);
        // Create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button btnPostive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                btnPostive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String tripName = ((TextInputLayout) alertDialog.findViewById(R.id.tripName)).getEditText().
                                getText().toString();
                        String description = ((TextInputLayout) alertDialog.findViewById(R.id.description)).getEditText().
                                getText().toString();
                        if("".equals(tripName)){
                            TextView txtErr = (TextView) alertDialog.findViewById(R.id.errorMsg);
                            txtErr.setText("Enter a trip name...");
                            return;
                        }else {
                            createNewTrip(tripName,description);
                            dialogInterface.dismiss();
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogInterface.dismiss();
                    }
                });
            }
        });

        alertDialog.show();

    }

    private void createNewTrip(final String tripName, final String description) {

        User owner = new User(mUsername, null, mUserId,FirebaseUtil.getCurrentUserEmail() );
        final String tripKey = mFirebaseDatabaseReference.child("trips").push().getKey();

        Log.d(Constants.TAG,"trip id == " + tripKey);
//            DatabaseReference newPostRef = postRef.push();
        Trip trip = new Trip(tripKey,owner, tripName, description);
//        String userId = getSharedPreferences("UserKey",MODE_PRIVATE).getString("userId",null);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/trips/" + tripKey, trip.toMap());
        childUpdates.put("/user-trips/" + mUserId + "/" + tripKey, trip.toMap());

        // Create a new trip
        mFirebaseDatabaseReference.child("trips").child(tripKey).setValue(trip.toMap());
        // Now create a new trip associated with the user.

        mFirebaseDatabaseReference.child("user-trips").child(mUserId).child("trips").child(tripKey).setValue(trip.toMap(), new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null || databaseError.getCode() >= 0) {
                    Log.d(Constants.TAG,"created a new trip id == ");

                    Intent viewTripIntent = new Intent(TripActivity.this, ViewTripActivity.class);
                    viewTripIntent.putExtra(Constants.TRIP_ID, tripKey);
                    viewTripIntent.putExtra(Constants.TRIP_NAME, tripName);
                    viewTripIntent.putExtra(Constants.DESCRIPTION, description);
                    viewTripIntent.putExtra(Constants.NEW_TRIP, true);
                    startActivity(viewTripIntent);
                }else{
                    Log.d(Constants.TAG,"error on creating a new trip id == " + databaseError.getMessage());

                }
            }
        });
    }

    public void setToolbar(Integer color) {
        toolbar.setItemColor(color);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Trip> mTripList;
        public MyFragmentPagerAdapter(FragmentManager fm, List<Trip> tripList) {
            super(fm);
            mTripList = tripList;
        }

        public void setTripList(List<Trip> tripList){
            mTripList = tripList;
        }

        @Override
        public Fragment getItem(int position) {
            if(mTripList.size() > 0) {
                Trip trip = mTripList.get(position);
                return OverlapFragment.newInstance(DemoImages.covers[position % 6], trip.getName(), trip.getDescription(), trip.getId());
            }else{
                return new OverlapFragment();
            }
        }

        @Override
        public int getCount() {
            return mTripList.size();
        }
    }


    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        int currentPosition = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            FragmentLifecycle fragmentToShow = (FragmentLifecycle)pagerAdapter.getItem(position);
            fragmentToShow.onResumeFragment(TripActivity.this);

            /// If we have deleted more items than current position
            if(pagerAdapter.getCount() <= currentPosition){
                currentPosition = pagerAdapter.getCount()-1;
            }
            FragmentLifecycle fragmentToHide = (FragmentLifecycle)pagerAdapter.getItem(currentPosition);
            fragmentToHide.onPauseFragment();

            currentPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private ViewPager.OnPageChangeListener friendPageChangeListener = new ViewPager.OnPageChangeListener() {
        int currentPosition = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            FragmentLifecycle fragmentToShow = (FragmentLifecycle)pagerFriendsAdapter.getItem(position);
            fragmentToShow.onResumeFragment(TripActivity.this);

            FragmentLifecycle fragmentToHide = (FragmentLifecycle)pagerFriendsAdapter.getItem(currentPosition);
            fragmentToHide.onPauseFragment();

            currentPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"Removing db event listener " + valueEventListener);
        if(valueEventListener != null)
            mFirebaseDatabaseReference.removeEventListener(valueEventListener);
    }
}
