package com.example.GoAutoCamping;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;


public class Supplies_Detail extends Fragment {

    Context context;
    private CommentAdapter mAdapter;
    private DatabaseReference CommentsReference;
    private DatabaseReference CommentNumReference;
    EditText commentEdit;
    MaterialButton postComment;
    TextView name, des;
    ImageView image;
    FloatingActionButton likeBtn, storeBtn;

    CoordinatorLayout coordinatorLayout;

    RecyclerView recyclerView;

    //??????????????????
    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    ArrayList<SuppliesDTO> dtos;

    //?????? ??????
    String userID, userNickName, userProfile;

    //??? ??????
    String suppliesKind, postKey;
    String storeLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.supplies_detail, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView = view.findViewById(R.id.comment_recycler);
        commentEdit = view.findViewById(R.id.fieldCommentText);
        postComment = view.findViewById(R.id.buttonPostComment);

        image = view.findViewById(R.id.image_supply);
        name = view.findViewById(R.id.name_supplies);
        des = view.findViewById(R.id.des_supplies);

        likeBtn = view.findViewById(R.id.likebtn_supply);
        storeBtn = view.findViewById(R.id.storebtn_supply);

        coordinatorLayout = view.findViewById(R.id.snackbar_line);

        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(true);

        Bundle bundle = getArguments();

        if(bundle != null){
            suppliesKind = bundle.getString("supplyKind");
            postKey = bundle.getString("postId");

            //?????? ??????????????????
            CommentsReference = FirebaseDatabase.getInstance().getReference().child("Supply").child(suppliesKind).child(postKey);
            load();

        }

        //?????????
        likeBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean ch = checkLogin();
                if(ch){
                    addlike();
                }
            }
        });

        //?????? ???
        storeBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //??????????????? ????????????
                MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                dialog.title(null, "?????? ?????????");
                dialog.message(null, "?????? ????????? ??????????????? ?????????????????????????", null);
                //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
                dialog.positiveButton(null, "???", materialDialog -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeLink));
                    startActivity(intent);
                    return null;
                });
                dialog.negativeButton(null, "?????????", materialDialog -> {
                    dialog.dismiss();
                    return null;
                });
                dialog.show();

            }
        });

        //??????
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CommentAdapter(getContext(), CommentsReference);
        mAdapter.setOnItemLongClickListener(new CommentAdapter.OnItemLongClickEventListener() {
            @Override
            public void onItemLongClick(View a_view, int a_position, List<String> commentIds, List<CommentDTO> commentDTOS) {
                CommentDTO commentDTO = commentDTOS.get(a_position);
                String commentId = commentIds.get(a_position);

                //?????????????????? ????????? ????????? ????????????
                if(commentDTO.userId_comment.equals(email)){
                    MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "?????? ??????");
                    dialog.message(null, "????????? ?????????????????????????", null);
                    dialog.positiveButton(null, "???", materialDialog -> {
                        Snackbar.make(coordinatorLayout, "????????? ?????????????????????!", Snackbar.LENGTH_SHORT).show();
                        onDeleteContent(commentId);
                        return null;
                    });
                    dialog.negativeButton(null, "?????????", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                }
                else{
                    Snackbar.make(coordinatorLayout, "????????? ???????????? ????????????", Snackbar.LENGTH_SHORT).show();

                }
            }
        });

        recyclerView.setAdapter(mAdapter);
        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ch = checkLogin();
                if(ch){
                    postComment();
                }
            }
        });

        return view;
    }

    public boolean checkLogin(){
        boolean ch = false;

        if(email != null){
            ch = true;
            return ch;
        }
        else{
            //??????????????? ????????????
            MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
            dialog.title(null, "????????? ??????");
            dialog.message(null, "???????????? ????????? ???????????????. \n????????? ????????????.", null);
            //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
            dialog.positiveButton(null, "??????", materialDialog -> {
                dialog.dismiss();
                return null;
            });
            dialog.show();
        }
        return ch;

    }


    //???????????? ????????? ????????????
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        //?????????????????? ?????? ??????
        dtos = new ArrayList<>();

        DocumentReference docRef = Firestore.collection("supplies").document(suppliesKind).collection("posts").document(postKey);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //????????? ????????? ????????????
                SuppliesDTO suppliesDTO = documentSnapshot.toObject(SuppliesDTO.class);

                String memoText = suppliesDTO.getPost_memo();
                memoText = memoText.replace("enter", "\n");

                name.setText(suppliesDTO.getPost_name());
                des.setText(memoText);

                Glide.with(getContext())
                        .load(suppliesDTO.getPost_Image())
                        .into(image);

                storeLink = suppliesDTO.getPost_link();
            }
        });


    }

    //????????? ?????? ????????? ??????
    public void checklike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("supplies").document(suppliesKind).collection("posts").document(postKey);

        //???????????? ?????? ??????????????? ???????????????
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("post_likeUser");

                if(group.contains(email)){
                    likeBtn.setImageResource(R.drawable.like_full);
                }
                else{
                    likeBtn.setImageResource(R.drawable.like);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    //????????? ??????
    public void addlike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("supplies").document(suppliesKind).collection("posts").document(postKey);

        //???????????? ?????? ??????????????? ???????????????
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("post_likeUser");

                if(group.contains(email)){
                    documentReference.update("post_likeUser", FieldValue.arrayRemove(email));
                    likeBtn.setImageResource(R.drawable.like);

                }
                else{
                    documentReference.update("post_likeUser", FieldValue.arrayUnion(email));
                    likeBtn.setImageResource(R.drawable.like_full);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        //????????? ????????? ?????? ????????? ??? ???????????????
        Firestore.runTransaction(new Transaction.Function<Double>() {
            @Nullable
            @Override
            public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(documentReference);
                ArrayList<String> group = (ArrayList<String>) snapshot.get("post_likeUser");

                Double likenum = snapshot.getDouble("post_like");

                if(group.contains(email) && likenum > 0){
                    likenum = likenum - 1;
                    transaction.update(documentReference, "post_like", likenum);
                }
                else{
                    likenum = likenum + 1;
                    transaction.update(documentReference, "post_like", likenum);
                }

                return likenum;
            }
        }).addOnSuccessListener(new OnSuccessListener<Double>() {
            @Override
            public void onSuccess(Double integer) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }


    //?????? ??????
    private void postComment() {
        // ?????? ?????? ?????????
        String commentText = commentEdit.getText().toString();

        if(!TextUtils.isEmpty(commentText)){
            InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(commentEdit.getWindowToken(), 0);

            CommentDTO comment = new CommentDTO(email, userProfile, userNickName, commentText);

            // ?????? ????????????
            CommentsReference.push().setValue(comment);

            // ????????? ?????? ????????????
            commentEdit.setText(null);
        }
        else{
            InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(commentEdit.getWindowToken(), 0);

            Snackbar.make(coordinatorLayout, "????????? ??????????????????", Snackbar.LENGTH_SHORT).show();
        }


    }

    //?????? ??????
    private void onDeleteContent(String item)
    {
        CommentsReference.child(item).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("error: "+e.getMessage());
            }
        });
    }


    //?????? ?????? ??????
    public abstract class OnSingleClickListener implements View.OnClickListener{
        //?????? ?????? ?????? ?????? ?????? ( ?????? ?????? ????????? ?????? ?????? ?????? )
        private static final long MIN_CLICK_INTERVAL = 1000; //1sec
        private long mLastClickTime = 0;
        public abstract void onSingleClick(View v);
        @Override public final void onClick(View v) {
            long currentClickTime = SystemClock.uptimeMillis();
            long elapsedTime = currentClickTime - mLastClickTime;
            mLastClickTime = currentClickTime;
            // ???????????? ?????? ??????
            if (elapsedTime > MIN_CLICK_INTERVAL) {
                onSingleClick(v);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        checklike();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();

            //????????? ??????????????????
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //????????? ????????? ????????????
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);
                    userNickName = userDTO.getUserNickname();
                    userProfile = userDTO.getUserProfile();
                }
            });
        }


        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(1,true);
        }
    }
}