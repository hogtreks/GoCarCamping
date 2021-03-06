package com.example.GoAutoCamping;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Recommend_ggd extends Fragment implements Recommend_detail_filiterdialog.InputSelected{

    public static final int DATEPICKER_FRAGMENT=1;
    View view;
    ListView listView;
    ArrayList<RecommendDTO> dtos;
    ArrayList<CommunityDTO> dtos2;
    ArrayList<CommunityDTO> dtos3;

    Context context;

    //??????????????????
    RecyclerView recyclerView;
    List<Recommend_hotDTO> models;
    Recommend_Adapter adapter;

    //??????
    Recommend_detail_filiterdialog dlg;
    FloatingActionButton filterbtn;

    public boolean[] checked = {false, false, false, false, false, false, false, false, false, false, false};
    String[] checkingName = { "???", "???", "??????", "??????", "?????????", "??????", "?????????", "?????????", "?????????", "??????", "??????"};
    HorizontalScrollView chipLayout;
    ChipGroup chipGroup;
    TextView noFilter, noFilterResultTV;
    ImageView noFilterResultImg;

    //??????????????????
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;
    private DatabaseReference ReviewsReference;

    CommunityDTO communityDTO;

    public static Recommend_ggd newInstance(){
        return new Recommend_ggd();
    }

    @Override
    public void clearAll() {
        clearChip();
    }

    @Override
    public void sendBoolenArray(boolean[] ch, String[] chName) {

        ArrayList<String> checkName = new ArrayList<String>();
        ArrayList<String> checkFilterName = new ArrayList<String>();

        for(int i=0; i <ch.length; i++){
            checked[i] = ch[i];

        }

        for(int i=0; i<checked.length; i++){
            Log.d("???????????????",  i + "?????? " + checked[i]);

            if(checked[i])
                checkName.add(checkingName[i]);
            //checkFilterName.add(checkingFilter[i]);
        }

        clearChip();
        if(checkChipGroup())
            clearChip();
        addChipView(checkName);
        filterLoad(checked);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.recommend_ggd, container, false);

        //????????? ?????????
        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(true);

        ActionBar mainTB = ((MainActivity) getActivity()).getSupportActionBar();

        noFilterResultTV = view.findViewById(R.id.text_none);
        noFilterResultImg = view.findViewById(R.id.image_none);

        //????????????
        listView = view.findViewById(R.id.list_recomend);

        load();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecommendDTO dto = (RecommendDTO) parent.getItemAtPosition(position);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fragment = new Recommend_detail();

                Bundle bundle = new Bundle();
                bundle.putString("placeName", "place_ggd");
                bundle.putString("placeId", dto.getRecommendId());

                fragment.setArguments(bundle);
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_right, R.anim.enter_from_right, R.anim.exit_from_right);
                transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
            }
        });


        //??????
        dlg = new Recommend_detail_filiterdialog();
        filterbtn = view.findViewById(R.id.filter);

        filterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dlg.setTargetFragment(Recommend_ggd.this, 1);
                dlg.show(getActivity().getSupportFragmentManager(), "tag");
            }
        });

        //???
        chipLayout = view.findViewById(R.id.chip_Layout);
        chipGroup = view.findViewById(R.id.chip_group);

        noFilter = view.findViewById(R.id.filter_text);


        //??????????????????
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        loadCommunity();

        return view;
    }

    //???????????? ????????? ????????????
    public void loadCommunity(){
        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos2 = new ArrayList<>();
        dtos3 = new ArrayList<>();

        //??????????????? ?????? ????????? ???????????? - onComplete??? ???????????? Success??? ????????? ?????? ?????? ?????? ????????? ??????
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    Log.d("?????????2", "?????????2");
                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for(DocumentSnapshot d : list){
                        communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("?????????", "?????????");
                        dtos2.add(communityDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                    }

                    Log.d("?????????", dtos2.size()+"");

                    models = new ArrayList<>();
                    //TODO ????????? ????????? 5??? ????????? 5???????????????
                    int i =0;
                    for (int j = 0; j<dtos2.size(); j++) {
                        if(dtos2.get(j).getCommunityAddress2().contains("?????????")) {
                            models.add(new Recommend_hotDTO(dtos2.get(j).getCommunityImage(), dtos2.get(j).getCommunityAddress()));
                            dtos3.add(dtos2.get(j));
                            i++;
                        }
                        if( i == 4)
                            break;
                    }

                    //????????? ?????? ??? setAdapter
                    adapter = new Recommend_Adapter(models, context);
                    recyclerView.setAdapter(adapter);

                    adapter.setOnItemClickListener(new Recommend_Adapter.Recommend_OnItemClickListener() {
                        @Override
                        public void onItemClick(Recommend_Adapter.ItemViewHolder holder, View view, int pos) {
                            openCommuDetail(pos);
                        }
                    });
                }
                else {
                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });
    }

    public void openCommuDetail(int pos) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment fragment = new Community_detail(pos);

        Bundle bundle = new Bundle();
        bundle.putString("postId", dtos3.get(pos).getCommunityId());
        bundle.putString("rec", "rec");
        bundle.putString("?????????", "?????????");

        fragment.setArguments(bundle);
        transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
    }

    //???????????? ????????? ????????????
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        //?????? ?????? ????????? - ?????? ???
        Firestore.collection("places").document("place_ggd").collection("innerPlaces")
                .orderBy("RecommendStar", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for(DocumentSnapshot d : list){
                        RecommendDTO recommendDTO = d.toObject(RecommendDTO.class);
                        recommendDTO.setRecommendId(d.getId());
                        dtos.add(recommendDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                    }
                    //????????? ?????? ??? setAdapter
                    Recommend_detail_adapter adapter = new Recommend_detail_adapter(context, dtos);
                    listView.setAdapter(adapter);
                }
                else {
                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });
    }



    //????????????
    public void filterLoad(boolean[] ch){

        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        //?????? ?????? ????????? - ?????? ??? ????????????
        Firestore.collection("places").document("place_ggd").collection("innerPlaces")
                .orderBy("RecommendStar", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){



                    //???????????? ????????? ??????
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //???????????? ??????????????? ???????????? ????????? ????????????
                    for(DocumentSnapshot d : list){
                        RecommendDTO recommendDTO = d.toObject(RecommendDTO.class);
                        recommendDTO.setRecommendId(d.getId());

                        boolean check = false;

                        //?????? ????????????
                        for(int i = 0; i < 11; i++){
                            if(ch[i] && recommendDTO.getRecommendFilter().get(i)){
                                check = true;
                            }
                        }

                        if(check){
                            dtos.add(recommendDTO); //??? ????????? ???????????? ???????????? ????????? ???????????? ???????????? ????????? ??????
                        }
                    }
                    if(dtos.size() == 0){
                        noFilterResultTV.setVisibility(View.VISIBLE);
                        noFilterResultImg.setVisibility(View.VISIBLE);
                    }
                    else{
                        noFilterResultTV.setVisibility(View.INVISIBLE);
                        noFilterResultImg.setVisibility(View.INVISIBLE);
                    }

                    //????????? ?????? ??? setAdapter
                    Recommend_detail_adapter adapter = new Recommend_detail_adapter(context, dtos);
                    listView.setAdapter(adapter);
                }
                else {


                    Log.d("??? ??????", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("?????????", "");
            }
        });

    }

    //??? ?????? ??????
    public boolean checkChipGroup(){
        boolean tf = false;
        int i = chipGroup.getChildCount();
        Log.d("???????????? ?????????????", i + "??? ??????");

        if(i == 0)
            tf = true;

        return tf;

    }

    //??? ??? ??????
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
            noFilter.setVisibility(View.INVISIBLE);

        }
    }

    //??? ?????????
    public void clearChip(){
        int count = chipGroup.getChildCount();

        Log.d("???????", chipGroup.getChildCount()+ "???");

        for(int i = count-1; i > -1; i--){
            Log.d("?????? ??????????", i + "???");
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chipGroup.removeView(chip);
        }
        noFilter.setVisibility(View.VISIBLE);
    }

    //??? ??????
    public void removeChip(Chip chip){

        if(chip == null) return;

        //??? ?????? ???????????? ???????????? ?????? - ??? ?????? ??? ????????? ??????
        for(int i = 0; i < checkingName.length; i++){
            if(checkingName[i].equals(chip.getText()))
                checked[i] = false;
        }

        chipGroup.removeView(chip);

        //??? ????????? ????????? ????????? - ?????? ????????? ?????? ??????
        if(checkChipGroup()){
            noFilter.setVisibility(View.VISIBLE);
            load();
        }
        //??? ????????? ???????????? - ???????????? ??? ????????? ?????? ??????
        else{
            filterLoad(checked);
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(1,true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(false);
    }


}