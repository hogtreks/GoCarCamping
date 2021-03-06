package com.example.GoAutoCamping;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapView;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Map extends Fragment implements Map_filterdialog.InputSelected, OnMapReadyCallback {
    final int CAMERA_ZOOM_LEVEL = 13;
    final int MARKER_SIZE = 80;
    final double NORTH_LATITUDE = 38.58742;
    final double SOUTH_LATITUDE = 33.112585;
    final double WEST_LONGITUDE = 124.608107;
    final double EAST_LONGITUDE = 131.872743;
    //implements OnMapReadyCallback,Map_filterdialog.InputSelected
    Map_RecyclerAdapter adapter;
    RecyclerView mapRecyclerView;
    FloatingActionButton mapFab, filterFab, mainFab, locationFab;
    Button listBtn, searchMidBtn;
    Boolean enterFlag = false, openFlag = false; //mapFlag : ?????? ?????? btnFlag : ?????? ?????? ??????
    View list_bs = null, coordinatorLayout = null;
    BottomSheetBehavior list_behav = null;
    CardView cardview;
    EditText searchView;
    Bundle searchArg;
    ConstraintLayout fl;
    FrameLayout sear, totCard;
    ImageButton sBtn;
    String word, recPlaceName, recPlaceId;
    List<Map_placedata> placedata = new ArrayList<>();
    List<Map_placedata> filterData = new ArrayList<>();
    List<Map_placedata> localData = new ArrayList<>();

    MapView sView = null;
    NaverMap naverMap;

    ArrayList<Marker> markerList = new ArrayList<>();
    int maxHeight = 0, minHeight = 0, mapFlag = 0;

    //??????
    public boolean[] checked = {false, false, false, false, false, false, false, false, false, false, false};
    boolean[] checkedLoc = {false, false, false, false, false, false, false, false, false, false};
    String[] checkingName = {"???", "???", "??????", "??????", "?????????", "??????", "?????????", "?????????", "?????????", "??????", "??????"};
    String[] checkedLocName = {"??????", "?????????", "?????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "?????????"};

    HorizontalScrollView chipLayout, bschipLayout;
    ChipGroup chipGroup, bschipGroup;
    Context context;

    //??????????????????
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    ArrayList<RecommendDTO> dtos;
    ArrayList<String> filter_name = new ArrayList<>();
    ArrayList<String> local_name = new ArrayList<>();
    ArrayList<Double> local_lat = new ArrayList<>();
    ArrayList<Double> local_lng = new ArrayList<>();
    Marker detailMarker, locMarker = new Marker(), detMarker = new Marker();

    //?????? ?????? ????????????
    private Home_gpsTracker gpsTracker;
    double latitude = 37.58482502367129, longitude = 126.92520885572567; //??????, ??????

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    // private FusedLocationSource mLocationSource;

    public static Map newInstance() {
        return new Map();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //????????? ?????????
        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(false);

        coordinatorLayout = inflater.inflate(R.layout.map, container, false);
        fl = coordinatorLayout.findViewById(R.id.mapLayout);
        sear = coordinatorLayout.findViewById(R.id.searLayout);

        load();//load(); //?????????????????? ?????? ?????? ?????????

        //??????
        sView = coordinatorLayout.findViewById(R.id.map);
        sView.onCreate(savedInstanceState);
        //mLocationSource = new FusedLocationSource(this,PERMISSIONS_REQUEST_CODE);
        mapFlag = 0;
        sView.getMapAsync(this::onMapReady);
        getCurrentLocation();
        //????????????
        minHeight = ((MainActivity) getActivity()).pointY; //?????? ???????????? ?????? ??????????????? ?????? ???
        list_bs = coordinatorLayout.findViewById(R.id.list_bottomSheet); //??????
        initializeListBottomSheet(); //????????????
        initializeCardView(); //?????????
        settingFab(); //??????


        sBtn = coordinatorLayout.findViewById(R.id.sBtn);
        sBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word = searchView.getText().toString();
                int count = chipGroup.getChildCount();

                if (word.isEmpty() && count == 0) {
                    // ????????? x ?????? x ?????? x
                } else {
                    searchWord(word, checkedLoc, checked);

                }
                searchView.setText("");
            }
        });

        searchView = coordinatorLayout.findViewById(R.id.searchView);
        //todo : ????????? ?????? ??????s
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    word = searchView.getText().toString();
                    int count = chipGroup.getChildCount();

                    if (word.isEmpty() && count == 0) {
                        // ????????? x ?????? x ?????? x
                    } else {
                        searchWord(word, checkedLoc, checked);
                    }
                    searchView.setText("");
                }
                return true;
            }
        });

        return coordinatorLayout;
    }


    //?????????(?????? ?????????)
    private void initializeCardView() {
        cardview = coordinatorLayout.findViewById(R.id.card_view);
        totCard = coordinatorLayout.findViewById(R.id.totCardView);
        cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //????????? ?????? ???????????? ??????
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fragment = new Recommend_detail();

                enterFlag = true;
                Bundle bbundle = new Bundle();
                bbundle.putString("placeName", recPlaceName);
                bbundle.putString("placeId", recPlaceId);

                fragment.setArguments(bbundle);
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.enter_from_right, R.anim.enter_from_right, R.anim.exit_from_right);
                transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
            }
        });

        //???????????? ???????????? ??????
        listBtn = coordinatorLayout.findViewById(R.id.listBtn);
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (placedata == null || placedata.isEmpty() || placedata.size() == 0) {

                } else {
                    //??????
                    totCard.setVisibility(View.GONE);
                    mapFlag = 1;
                    sView.getMapAsync(Map.this::onMapReady);
                    totBsState(true);
                }
            }
        });
        //?????? editText ?????????
        searchMidBtn = coordinatorLayout.findViewById(R.id.searchMidBtn);
        searchMidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totCard.setVisibility(View.GONE);
                list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);
                fabVisibility(true);
                mapFlag = 2;
                sView.getMapAsync(Map.this);
                sear.clearAnimation();

            }
        });
    }

    private void settingFab() {

        if(!placedata.isEmpty()){
            fabVisibility(false);
        }

        filterFab = coordinatorLayout.findViewById(R.id.filterFab); //??????????????????
        locationFab = coordinatorLayout.findViewById(R.id.locaFab); //????????? ?????? ??????
        mapFab = coordinatorLayout.findViewById(R.id.mapFab); //?????? ?????? ?????? ??????
        mainFab = coordinatorLayout.findViewById(R.id.mainFab); //?????? ?????? -> ??????????????? ??????
        mainFab.bringToFront();
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!openFlag) { //???????????????
                    fabListener(false);
                    openFlag = true;
                } else { // ???????????????
                    fabListener(true);
                    openFlag = false;
                }
            }
        });

        Map_searchDialog msd = new Map_searchDialog();

        //?????? ?????? ?????? ??????????????? (->Map_searchDialog)
        mapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabListener(openFlag);
                mapFlag = 0;
                searchArg = new Bundle();
                searchArg.putDouble("currentLatitude", latitude);
                searchArg.putDouble("currentLongitude", longitude);

                sView.getMapAsync(Map.this);
                msd.setArguments(searchArg);
                msd.show(requireActivity().getSupportFragmentManager(), "tag");
            }
        });

        //????????? ??????
        locationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabListener(openFlag);
                getCurrentLocation();
            }
        });

        //?????? ???
        chipLayout = coordinatorLayout.findViewById(R.id.chip_Layout);
        chipGroup = coordinatorLayout.findViewById(R.id.chip_group);
        //?????? ???????????????
        Map_filterdialog dlg = new Map_filterdialog();
        //?????? ???????????? ??????(->Recommend_detail_filterdialog)
        filterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainFab.bringToFront();
                fabListener(openFlag);
                dlg.setTargetFragment(Map.this, 1);
                dlg.show(getActivity().getSupportFragmentManager(), "tag");
            }
        });


    }

    public void getCurrentLocation() {
        gpsTracker = new Home_gpsTracker(getContext());

        //????????? ????????? ?????????(GPS ?????? ??????)
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
            Log.d("??????", latitude + "");
            Log.d("??????", longitude + "");

            if(latitude > NORTH_LATITUDE || latitude < SOUTH_LATITUDE || longitude < WEST_LONGITUDE || longitude > EAST_LONGITUDE) {
                //????????? ??????, ????????? ??????
                Toast.makeText(this.getContext(), " ?????? ????????? ?????? ????????? ?????? ????????? ", Toast.LENGTH_SHORT).show(); //todo: ???????????? ??????
                latitude = 37.58482502367129;
                longitude = 126.92520885572567;
            }
            mapFlag = 0;
            sView.getMapAsync(this::onMapReady);
        }
    }

    public void searchTest() {

        boolean firebaseFlag = false;// false : ?????? ????????? ??????  true : ??????, ?????? ?????? ??????
        if (chipGroup.getChildCount() > 0) {
            for (int i = 0; i < checked.length; i++) {
                //??????, ?????? ????????? ???????????? ??????????????? ??????
                if (checked[i]) firebaseFlag = true;
            }
        }
        if (firebaseFlag) {
            //??????, ?????? ?????? ??????
            for (int i = 0; i < checked.length; i++)
                Log.d("filterTest", "????????? " + i + "??????" + checked[i]);
            //filterLoad(checked);

        }
        //??????, ?????? ?????? ???????????? ??????
    }


    //???????????? ????????? ????????????
    public void load() {
        Firestore = FirebaseFirestore.getInstance();

        // if (!dtos.isEmpty()) dtos.clear();
        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        //?????? ?????? ????????? - ?????? ???
        Firestore.collectionGroup("innerPlaces")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {

                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for (DocumentSnapshot d : list) {
                        RecommendDTO recommendDTO = d.toObject(RecommendDTO.class);
                        recommendDTO.setRecommendId(d.getId());

                        Log.d("??? ?????????????", d.getId());

                        dtos.add(recommendDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                    }

                    // if(!mapDtos.isEmpty()) mapDtos.clear();
                    Log.d("filterTest", "db?????? load ??????" + dtos.size());
                } else {
                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });
        Log.d("filterTest", "db?????? load ??????2  " + dtos.size());


    }

    @Override
    public void clearAll() {
        clearChip();
    }

    //chip boolean, name ?????? ?????????(<- filterdialog)
    @Override
    public void sendBoolenArray(boolean[] ch, String[] chName, boolean[] chLoc, String[] chLocName) {
        //ch, chName : ?????? ?????? ??????, ??????
        //chLoc, chLocName : ???????????? ?????? ??????, ??????
        ArrayList<String> checkName = new ArrayList<String>();

        for (int i = 0; i < ch.length; i++) {
            checked[i] = ch[i];
        }
        for (int i = 0; i < chLoc.length; i++) {
            checkedLoc[i] = chLoc[i];
        }

        for (int i = 0; i < checked.length; i++) {
            Log.d("???????????????", i + "?????? " + checked[i]);

            if (checked[i])
                checkName.add(chName[i]);
        }
        for (int i = 0; i < checkedLoc.length; i++) {
            if (checkedLoc[i]) checkName.add(checkedLocName[i]);
        }

        clearChip();
        if (checkChipGroup())
            clearChip();

        addChipView(checkName);
    }


    //???
    public boolean checkChipGroup() {
        boolean tf = false;
        int i = chipGroup.getChildCount();
        Log.d("???????????? ?????????????", i + "??? ??????");

        clearChip();
        if (i == 0)
            tf = true;

        return tf;

    }

    public void addChipView(ArrayList<String> name) {
        for (int i = 0; i < name.size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.filter_chip_layout, chipGroup, false);
            chip.setText(name.get(i));

            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeChip(chip);
                }
            });

            chipGroup.addView(chip);

            searchTest();
        }
    }

    //??? ?????????
    public void clearChip() {
        int count = chipGroup.getChildCount();

        Log.d("???????", chipGroup.getChildCount() + "???");

        for (int i = count - 1; i > -1; i--) {
            Log.d("?????? ??????????", i + "???");
            Chip chip = (Chip) chipGroup.getChildAt(i);
            removeChip(chip);
        }
    }

    //??? ??????
    public void removeChip(Chip chip) {
        if (chip == null) return;

        for (int i = 0; i < checkingName.length; i++) {
            if (checkingName[i].equals(chip.getText()))
                checked[i] = false;
        }
        for (int i = 0; i < checkedLocName.length; i++) {
            if (checkedLocName[i].equals(chip.getText()))
                checkedLoc[i] = false;
        }

        chipGroup.removeView(chip);

        searchTest();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
    }

    //?????? ?????? db?????? ????????? ??????
    public String changeLocationName(String locationCode) {
        String areacode = "";
        switch (locationCode) {
            case "??????":
                areacode = "place_seoul";
                break;
            case "?????????":
                areacode = "place_ggd";
                break;
            case "?????????":
                areacode = "place_gwd";
                break;
            case "????????????":
                areacode = "place_ccnd";
                break;
            case "????????????":
                areacode = "place_ccbd";
                break;
            case "????????????":
                areacode = "place_gsnd";
                break;
            case "????????????":
                areacode = "place_gsbd";
                break;
            case "????????????":
                areacode = "place_jlnd";
                break;
            case "????????????":
                areacode = "place_jlbd";
                break;
            case "?????????":
                areacode = "place_zzd";
                break;
        }

        Log.d("filterTest", "?????? ??????" + areacode);
        return areacode;
    }

    //?????????
    public void searchWord(String text, boolean[] locationPick, boolean[] facilityPick) {
        //locationPick ?????? boolean???
        //facilityPick  ?????? boolean
        fabVisibility(false);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);//????????? ??? ?????? ??????

        String w = text; //????????? ?????? ??????
        ArrayList<String> location_name = new ArrayList<>(); //true ?????? ???????????? ??????
        ArrayList<String> facility_name = new ArrayList<>(); //true ?????? ?????? ?????? ?????? ??????
        //------------------------????????? ??????------------------------------------------------------------
        boolean facilityFlag = false;
        if (!placedata.isEmpty()) {
            adapter.removeItem();
            placedata.clear();
            filterData.clear();
        }

        if(!localData.isEmpty()){
            localData.clear();
            local_name.clear();
            local_lat.clear();
            local_lat.clear();
        }


        //???????????? ?????????
        if (!filter_name.isEmpty()) {
            int count = bschipGroup.getChildCount();

            Log.d("???????", chipGroup.getChildCount() + "???");
            filter_name.clear();

            for (int i = count - 1; i > -1; i--) {
                Log.d("?????? ??????????", i + "???");
                Chip chip = (Chip) bschipGroup.getChildAt(i);
                bschipGroup.removeView(chip);
            }
        }
        //-------------------------------------------????????? ?????? --------------------------------------
        //-----------------------------------------??? ?????? ?????? ??????--------------------------------------
        //?????? ?????? ??? ????????????
        for (int i = 0; i < locationPick.length; i++) {
            if (locationPick[i]) {
                location_name.add(changeLocationName(checkedLocName[i]));
                filter_name.add(checkedLocName[i]);
            }
        }
        //?????? ?????? ??? ?????? ??????
        for (int i = 0; i < facilityPick.length; i++) {
            if (facilityPick[i]) {
                facilityFlag =true;
                filter_name.add(checkingName[i]);
            }
        }



        String titleName = "", addressName = "", dtosCode = "";

        /*
        if (w.isEmpty() && location_name.size() == 0 && !facilityFlag) {
            //????????? ?????? + ?????? ????????? ?????? ??????
            for (int i = 0; i < dtos.size(); i++)

                filterData.add(new Map_placedata(dtos.get(i).getRecommendTitle(), dtos.get(i).getRecommendAddress(), dtos.get(i).getRecommendLat(), dtos.get(i).getRecommendLng(), dtos.get(i).getRecommendStar(), dtos.get(i).getRecommendAreaCode(), dtos.get(i).getRecommendId(), dtos.get(i).getRecommendLike(), dtos.get(i).getRecommendImage()));
        } else {*/
        for (int i = 0; i < dtos.size(); i++) {

            titleName = dtos.get(i).getRecommendTitle();
            addressName = dtos.get(i).getRecommendAddress();
            dtosCode = dtos.get(i).getRecommendAreaCode();

            boolean addFlag = false, localFlag= false;

            //????????? ??????
            if(!w.isEmpty()){
                //?????? ??????
                if(addressName.contains(w)){
                    //???????????? ???????????? ??????
                    Log.d("addressTest", w);
                    local_name.add(dtos.get(i).getRecommendTitle());
                    local_lat.add(dtos.get(i).getRecommendLat());
                    local_lng.add(dtos.get(i).getRecommendLng());
                    localFlag = true;
                }
                //???????????? ??????
                if(!localFlag && titleName.contains(w)){
                    localFlag = true;
                }
            }

            //?????? ?????? ??????
            for(int k = 0; k < location_name.size(); k++){
                if(location_name.get(k).equals(dtosCode)){
                    addFlag = true;
                }
            }
            //?????? ?????? ?????? ??????
            if(facilityFlag){
                for(int k = 0; k <facilityPick.length; k++){
                    if(facilityPick[k] == dtos.get(i).getRecommendFilter().get(k)) addFlag = true;
                }
            }

            if (localFlag) {
                //????????? o, ?????? o
                localData.add(new Map_placedata(dtos.get(i).getRecommendTitle(), dtos.get(i).getRecommendAddress(), dtos.get(i).getRecommendLat(), dtos.get(i).getRecommendLng(), dtos.get(i).getRecommendStar(), dtos.get(i).getRecommendAreaCode(), dtos.get(i).getRecommendId(), dtos.get(i).getRecommendLike(), dtos.get(i).getRecommendImage()));
            }else if(addFlag){
                //?????????x ?????? o
                filterData.add(new Map_placedata(dtos.get(i).getRecommendTitle(), dtos.get(i).getRecommendAddress(), dtos.get(i).getRecommendLat(), dtos.get(i).getRecommendLng(), dtos.get(i).getRecommendStar(), dtos.get(i).getRecommendAreaCode(), dtos.get(i).getRecommendId(), dtos.get(i).getRecommendLike(), dtos.get(i).getRecommendImage()));
            }
        }

        if(!localData.isEmpty()){
            for(int i = 0; i <localData.size(); i++){
                placedata.add(new Map_placedata(localData.get(i).getName(), localData.get(i).getAdd(), localData.get(i).getLat(), localData.get(i).getLan(), localData.get(i).getRate(), localData.get(i).getRecPlaceName(), localData.get(i).getRecPlaceId(), localData.get(i).getNum(), localData.get(i).getImage()));
            }
        }
        if(!filterData.isEmpty()){
            for(int i = 0; i <filterData.size(); i++){
                placedata.add(new Map_placedata(filterData.get(i).getName(), filterData.get(i).getAdd(), filterData.get(i).getLat(), filterData.get(i).getLan(), filterData.get(i).getRate(), filterData.get(i).getRecPlaceName(), filterData.get(i).getRecPlaceId(), filterData.get(i).getNum(), filterData.get(i).getImage()));
            }
        }

        if (filterData.isEmpty()&&localData.isEmpty()) {
            Toast.makeText(this.getContext(), "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show(); //todo: ???????????? ??????
        } else {
            Log.d("filterTest", "placeData ?????????" + placedata.size());
            //????????? ???????????????
            Animation animation = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.slide_up_animation);
            sear.startAnimation(animation);

            fabVisibility(false); // ???????????? ??????
            totBsState(true); //???????????? ??????
            list_behav.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

            //??????
            mapFlag = 1; // ?????? ?????? ???????????? false : ?????? ?????? ?????? true : ?????? ?????? ?????????
            sView.getMapAsync(this); //?????? ?????????
            //????????????
            setListData(); //?????? ?????? ????????????
            clearChip();

            if (filter_name.isEmpty()) {
                bschipGroup.setVisibility(View.GONE);
                bschipLayout.setVisibility(View.GONE);
            } else {
                bschipGroup.setVisibility(View.VISIBLE);
                bschipLayout.setVisibility(View.VISIBLE);
                for (int i = 0; i < filter_name.size(); i++) {
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.map_chip_layout, bschipGroup, false);
                    chip.setText(filter_name.get(i));
                    bschipGroup.addView(chip);
                }
            }
        }

    }


    //?????? ???????????????
    private void initializeListBottomSheet() {
        list_behav = BottomSheetBehavior.from(list_bs);

        list_behav.setDraggable(false);
        bschipGroup = coordinatorLayout.findViewById(R.id.search_chip_group);
        bschipLayout = coordinatorLayout.findViewById(R.id.search_chip_Layout);
        list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);

        ImageButton openBtn = coordinatorLayout.findViewById(R.id.openBtn);
        ImageButton closeBtn = coordinatorLayout.findViewById(R.id.closeBtn);

        //??????????????????
        mapRecyclerView = coordinatorLayout.findViewById(R.id.mapRecyclerView);
        LinearLayoutManager linearLayoutManager_2 = new LinearLayoutManager(getActivity());
        mapRecyclerView.setLayoutManager(linearLayoutManager_2);

        //???????????? ?????? ?????? //?????? ?????? ?????? ??????
        list_bs.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int getH = getView().getHeight();

                Log.d("height test2", "??????" + getH);
                maxHeight = getH;
                list_bs.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (list_behav.getState()) {
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        list_behav.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        list_behav.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                        break;
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);
                closeBtn.setVisibility(View.INVISIBLE);
                fabVisibility(true);
                clearAll();
                mapFlag = 2;
                sView.getMapAsync(Map.this::onMapReady);
                sear.clearAnimation();
            }
        });

        list_behav.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                //bottomsheet ?????? ???????????? ??????
                if (newState == list_behav.STATE_COLLAPSED) {
                    onSlide(list_bs, 0.07f);
                } else if (newState == list_behav.STATE_HALF_EXPANDED) {
                    onSlide(list_bs, 0.5f);
                    closeBtn.setVisibility(View.VISIBLE);
                } else if (newState == list_behav.STATE_HIDDEN) {
                    onSlide(list_bs, -0.0f);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //bottomsheet ???????????????
                switch (list_behav.getState()) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        setMapPaddingBottom(slideOffset);
                        openBtn.setRotation(360);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        setMapPaddingBottom(slideOffset);
                        openBtn.setRotation(180);
                        break;

                }
            }
        });

    }


    //?????? ?????????
    private void setMapPaddingBottom(Float offset) {
        //From 0.0 min - 1.0 max
        CoordinatorLayout.LayoutParams parmas = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        float maxPaddingBottom = maxHeight;
        if (offset != 0.5f) parmas.setMargins(0, 0, 0, Math.round((offset) * maxPaddingBottom));
        else if (offset == 0.5f)
            parmas.setMargins(0, 0, 0, Math.round((offset) * maxPaddingBottom));
        fl.setLayoutParams(parmas);
    }


    // ?????????????????? ????????? ??????
    public void setListData() {
        adapter = new Map_RecyclerAdapter(context, placedata, localData.size());
        mapRecyclerView.setAdapter(adapter);
        // sView.getMapAsync(Map.this::onMapReady);

        adapter.setOnItemClickListener(new Map_RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                detailMarker = new Marker();
                detailMarker.setPosition(new LatLng(placedata.get(position).getLat(), placedata.get(position).getLan()));
                detailMarker.setCaptionText(placedata.get(position).getName());
                mapFlag = 3;
                sView.getMapAsync(Map.this::onMapReady);
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onMapReady(NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setMapType(NaverMap.MapType.Basic); //????????????
        naverMap.setLayerGroupEnabled(naverMap.LAYER_GROUP_BUILDING, false);//????????????
        final String[] markerTxt = new String[1];

        //naverMap.setLocationSource(mLocationSource);
        //ActivityCompat.requestPermissions(getActivity(),REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);

        //mapFlag 0 : ??? ??????
        //mapFlag 1 : ????????? ??????
        //mapFlag 2 : ?????? ?????????
        //mapFlag 3 : ?????? ??? ?????????, ????????? ????????? ??????
        if (mapFlag == 0) {
            if(locMarker != null){
                locMarker.setMap(null);
            }

            locMarker.setPosition(new LatLng(latitude,longitude));
            locMarker.setIcon(OverlayImage.fromResource(R.drawable.locationicon));
            locMarker.setHeight(MARKER_SIZE + 15);
            locMarker.setWidth(MARKER_SIZE + 15);
            locMarker.setMap(naverMap);

            CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(locMarker.getPosition(), CAMERA_ZOOM_LEVEL);
            naverMap.moveCamera(cameraUpdate);
        } else if (mapFlag == 1) {
            LatLng northEast; //????????? bounds ??????( ?????? ) ????????? ??????, ?????? ?????? ??? ??????
            LatLng southWest; //????????? bounds ??????( ?????? ) ????????? ??????, ?????? ????????? ??? ??????
            double minLat, minLng, maxLat, maxLng;

            if(!local_name.isEmpty()){
                minLat = local_lat.get(0);
                minLng = local_lng.get(0);

                maxLat = local_lat.get(0);
                maxLng = local_lng.get(0);
            }else{
                minLat = placedata.get(0).getLat(); //????????? ??????
                minLng = placedata.get(0).getLan(); //?????? ?????? ??? ??????

                maxLat = placedata.get(0).getLat(); //????????? ??????
                maxLng = placedata.get(0).getLan(); //?????? ????????? ??? ??????
            }


            //?????? ???????????? ?????? ?????? ?????????
            if (markerList != null || markerList.size() != 0) {
                markerList.clear();
            }
            for (int i = 0; i < placedata.size(); i++) {
                Marker marker = new Marker();
                marker.setPosition(new LatLng(placedata.get(i).getLat(), placedata.get(i).getLan()));
                marker.setCaptionText(placedata.get(i).getName());
                marker.setWidth(MARKER_SIZE);
                marker.setHeight(MARKER_SIZE + 40);
                markerList.add(marker);
                if(local_name.isEmpty()) {
                    //?????? ??????
                    if (placedata.get(i).getLat() < minLat) {
                        minLat = placedata.get(i).getLat();
                    }
                    if (placedata.get(i).getLat() > maxLat) {
                        maxLat = placedata.get(i).getLat();
                    }
                    //?????? ??????
                    if (placedata.get(i).getLan() < minLng) {
                        minLng = placedata.get(i).getLan();
                    }
                    if (placedata.get(i).getLan() > maxLng) {
                        maxLng = placedata.get(i).getLan();
                    }
                }
                marker.setMap(naverMap);
                marker.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        Log.d("map test", "?????? ?????????");
                        markerTxt[0] = marker.getCaptionText();

                        markerClick(markerTxt[0], marker.getPosition());
                        return false;
                    }
                });

            }
            if(!local_name.isEmpty()){
                for(int i = 0; i < local_name.size(); i++){
                    if (local_lat.get(i) < minLat) {
                        minLat = local_lat.get(i);
                    }
                    if (local_lat.get(i) > maxLat) {
                        maxLat = local_lat.get(i);
                    }
                    //?????? ??????
                    if (local_lng.get(i) < minLng) {
                        minLng = local_lng.get(i);
                    }
                    if (local_lng.get(i) > maxLng) {
                        maxLng = local_lng.get(i);
                    }
                }
            }
            northEast = new LatLng(maxLat+.1f, minLng+.1f);
            southWest = new LatLng(minLat-.1f, maxLng-.1f);

            CameraUpdate cameraUpdate = CameraUpdate.fitBounds(new LatLngBounds(southWest, northEast), 10, 20, 10, 20);
            naverMap.moveCamera(cameraUpdate);
        } else if (mapFlag == 2) {
            Log.d("?????? ?????????", ""+markerList.size());
            //?????????
            for (int i = 0; i < markerList.size(); i++) {
                markerList.get(i).setMap(null);
            }
            markerList.clear();
            detMarker.setMap(null);
            if(detailMarker != null){
                detailMarker.setMap(null);
            }

        } else if (mapFlag == 3) {
            for (int i = 0; i < markerList.size(); i++) {
                markerList.get(i).setMap(null);
            }
            detailMarker.setWidth(MARKER_SIZE);
            detailMarker.setHeight(MARKER_SIZE + 40);
            detailMarker.setMap(naverMap);
            setBSDetail(detailMarker);
            CameraUpdate cameraUpdate2 = CameraUpdate.scrollAndZoomTo(detailMarker.getPosition(), CAMERA_ZOOM_LEVEL); //????????? ??? ?????? ?????? ??? 1 -> 14 ???????????????
            naverMap.moveCamera(cameraUpdate2);

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void markerClick(String text, LatLng pos){
        for (int i = 0; i < markerList.size(); i++) {
            markerList.get(i).setMap(null);
        }

        detMarker.setPosition(pos);
        detMarker.setCaptionText(text);

        setBSDetail(detMarker);
        detMarker.setMap(naverMap);

        CameraUpdate cameraUpdate2 = CameraUpdate.scrollAndZoomTo(pos, CAMERA_ZOOM_LEVEL); //????????? ??? ?????? ?????? ??? 1 -> 14 ???????????????
        naverMap.moveCamera(cameraUpdate2);
    }

    //????????? ????????? ?????? ?????? cardview
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void setBSDetail(Marker marker) {
        list_bs.setVisibility(View.INVISIBLE);
        totCard.setVisibility(View.VISIBLE);
        list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);
        String name = marker.getCaptionText(); //?????? ?????????

        cardview.setVisibility(View.VISIBLE);
        TextView tvDP = coordinatorLayout.findViewById(R.id.detailTitle); //?????????
        TextView tvDA = coordinatorLayout.findViewById(R.id.detailAdd); //??????
        ImageView ivImg = coordinatorLayout.findViewById(R.id.detailImg); //?????????
        RatingBar rbb = coordinatorLayout.findViewById(R.id.detailStar); //????????? ???
        TextView tvRate = coordinatorLayout.findViewById(R.id.detailNum);
        TextView tvLike = coordinatorLayout.findViewById(R.id.detailLikeNum);
        GradientDrawable drawable = (GradientDrawable) getContext().getDrawable(R.drawable.community_edge);

        //????????? ??????
        ivImg.setBackground(drawable);
        ivImg.setClipToOutline(true);

        for (int i = 0; i < placedata.size(); i++) {
            if (placedata.get(i).getName().equals(name)) {
                tvDP.setText(placedata.get(i).getName());
                tvDA.setText(placedata.get(i).getAdd());
                rbb.setRating((float) placedata.get(i).getRate());
                tvRate.setText(String.valueOf((float) placedata.get(i).getRate()));
                tvLike.setText(String.valueOf(placedata.get(i).getLike()));
                Glide.with(getContext())
                        .load(placedata.get(i).getImage())
                        .into(ivImg);

                recPlaceName = placedata.get(i).getRecPlaceName();
                recPlaceId = placedata.get(i).getRecPlaceId();
            }
        }
    }

    public void totBsState(boolean flag) {
        if (flag) {
            //?????? BS == open
            list_bs.setVisibility(View.VISIBLE);
            list_behav.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            Log.d("1. test", "totBsState false");
            list_bs.setVisibility(View.INVISIBLE);
        }
    }

    private void fabVisibility(boolean fabFlag) {

        if (fabFlag) {
            filterFab.setVisibility(View.VISIBLE);
            mapFab.setVisibility(View.VISIBLE);
            mainFab.setVisibility(View.VISIBLE);
            locationFab.setVisibility(View.VISIBLE);
        } else {
            filterFab.setVisibility(View.GONE);
            mapFab.setVisibility(View.GONE);
            mainFab.setVisibility(View.GONE);
            locationFab.setVisibility(View.GONE);
        }

    }

    //?????? ?????? fab ?????? ???????????????
    private void fabListener(Boolean btnFlag) {
        //btnflag default : false : ???????????? / true : ????????????
        if (btnFlag) {
            //??????
            //ObjectAnimator.ofFloat(mainFab, View.ROTATION, 45f, 0f).start();
            ObjectAnimator.ofFloat(locationFab, "translationY", 0f).start();
            ObjectAnimator.ofFloat(filterFab, "translationY", 0f).start();
            ObjectAnimator.ofFloat(mapFab, "translationY", 0f).start();
            mainFab.bringToFront();
        } else {
            //??????
            ObjectAnimator.ofFloat(locationFab, "translationY", -720f).start();
            ObjectAnimator.ofFloat(filterFab, "translationY", -480f).start();
            ObjectAnimator.ofFloat(mapFab, "translationY", -240f).start();
            //ObjectAnimator.ofFloat(mainFab, View.ROTATION, 0f, 45f).start();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sView.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        sView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        sView.onStop();
    }

    @Override
    public void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        sView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sView.onDestroy();
    }

    //????????????
    @Override
    public void onResume() {
        super.onResume();
        sView.onResume();
        if(enterFlag) {
            mapFlag = 0;
            sView.getMapAsync(Map.this::onMapReady);
            placedata.clear();
        }

        FragmentActivity activity = getActivity();
        if (activity != null) {
            ((MainActivity) activity).setBackBtn(0, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????
            boolean check_result = true;
            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    //naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                    break;
                }
            }
            if (check_result) {
                //?????? ?????? ????????? ??? ??????
                ;
            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(getActivity(), "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {
        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "GPS ?????????");
        dialog.message(null, "GPS ????????? ????????????????????????????", null);
        dialog.positiveButton(null, "?????????", materialDialog -> {
            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            return null;
        });
        dialog.negativeButton(null, "?????????", materialDialog -> {
            dialog.dismiss();
            return null;
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    void checkRunTimePermission() {

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)
            // 3.  ?????? ?????? ????????? ??? ??????
        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.
            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {
                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(getContext(), "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

}

