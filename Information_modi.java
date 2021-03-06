package com.example.GoAutoCamping;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Information_modi extends AppCompatActivity implements Information_modi_bottomsheet.BottomSheetListener {

    int REQUEST_IMAGE_CODE = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;
    ImageView profile_edit;

    TextInputLayout nameL_edit, nicknameL_edit ,idL_edit, passwdL_edit, passwdOkL_edit, birthdayL_edit, phoneNumL_edit;
    TextInputEditText name_edit, nickname_edit, id_edit, passwd_edit, passwdOk_edit, birthday_edit, phoneNum_edit;
    Button btnIdCheck, btnEditModi;

    boolean emailCheck = false;
    boolean passwdChange = false;
    String postId, address, content, id, image, nickName, profile, uploadTime;
    UserDTO exUserDTO;

    //??????????????????
    private FirebaseStorage storage;
    private FirebaseFirestore Firestore;
    private FirebaseDatabase FireDatabase;
    private DatabaseReference mCommentsComReference;
    private DatabaseReference mReviewsReference;
    private DatabaseReference mCommentsSupReference;
    private FirebaseAuth user;
    private String imageUrl="";
    private String email;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_modi);

        //????????????
        Toolbar toolbar = findViewById(R.id.modiTB);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //?????? ????????? ??????
        user = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        FireDatabase = FirebaseDatabase.getInstance();

        mCommentsComReference = FireDatabase.getReference().child("Community");
        mReviewsReference = FireDatabase.getReference().child("Places");
        mCommentsSupReference = FireDatabase.getReference().child("Supply");

        profile_edit = findViewById(R.id.profileImg_modi);

        name_edit = findViewById(R.id.nameText_modi);
        nickname_edit = findViewById(R.id.nicknameText_modi);
        idL_edit = findViewById(R.id.idLayout_modi);
        passwdL_edit = findViewById(R.id.passwdLayout_modi);
        passwdOkL_edit = findViewById(R.id.passwdOkLayout_modi);
        birthdayL_edit = findViewById(R.id.birthdayLayout_modi);
        phoneNumL_edit = findViewById(R.id.phonenumLayout_modi);

        nameL_edit = findViewById(R.id.nameLayout_modi);
        nicknameL_edit = findViewById(R.id.nickNameLayout_modi);
        id_edit = findViewById(R.id.idText_modi);
        passwd_edit = findViewById(R.id.passwdText_modi);
        passwdOk_edit = findViewById(R.id.passwdOkText_modi);
        birthday_edit = findViewById(R.id.birthdayText_modi);
        phoneNum_edit = findViewById(R.id.phonenumText_modi);

        btnIdCheck = findViewById(R.id.btnidcheck);
        btnEditModi = findViewById(R.id.btnEdit);

        name_edit.setClickable(false);
        nameL_edit.setClickable(false);
        nickname_edit.setClickable(false);
        nicknameL_edit.setClickable(false);

        btnIdCheck.setVisibility(View.INVISIBLE);
        btnIdCheck.setClickable(false);

        firstLoad();

        //?????? ???????????? ?????? ?????? ??????
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        } else {
        }

        //???????????? ?????? ??? ????????? ??????
        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);
            }
        });


        //???????????? ??????
        passwd_edit.addTextChangedListener(new TextWatcher() {
            @Override// ????????? ????????? ??????
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override //????????? ??????????????? ??????
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = passwd_edit.getText().toString();
                if(text.equals("")){
                    passwdL_edit.setError("?????? ???????????????");
                }
                else if(text.length() < 6){
                    passwdL_edit.setError("6??? ?????? ??????????????????");
                }
                else{
                    passwdL_edit.setErrorEnabled(false);
                }
            }
            @Override//????????? ?????? ?????? ??????
            public void afterTextChanged(Editable s) {
            }
        });

        //???????????? ??????
        passwdOk_edit.addTextChangedListener(new TextWatcher() {
            String text = null;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                text = passwdOk_edit.getText().toString();
                if(text.equals("")){
                    passwdOkL_edit.setError("?????? ???????????????.");
                }else{
                    passwdOkL_edit.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String pw = passwd_edit.getText().toString();
                if(!text.equals(pw)){
                    passwdOkL_edit.setError("??????????????? ?????? ????????????.");
                }else{
                    passwdOkL_edit.setErrorEnabled(false);
                }
            }
        });

        //?????? ????????? ????????????
        birthday_edit.setClickable(false);
        birthday_edit.setFocusable(false);
        //??????????????? ??????????????? ??????
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(birthdayL_edit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        birthdayL_edit.setOnClickListener(birthBottom);
        birthday_edit.setOnClickListener(birthBottom);

        btnEditModi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    if(!passwd_edit.getText().toString().equals(exUserDTO.getUserPasswd()))
                        passwdChange = true;
                    String birth = birthday_edit.getText().toString();
                    if(TextUtils.isEmpty(birth)){
                        birthday_edit.performClick();
                    } else {
                        String birthdate = birthday_edit.getText().toString();
                        String phoneNum = phoneNum_edit.getText().toString();
                        String passwd = passwd_edit.getText().toString();
                        updateUser(passwd, birthdate, phoneNum, imageUrl);
                    }
                }
            }
        });


    }

    //??????????????? ???????????? ????????? ??????????????? ??????.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CODE) {
            try {
                Uri image = data.getData();
                try {
                    //imageValid = true;
                    imageUrl = getRealPathFromUri(image);

                    RequestOptions cropOptions = new RequestOptions();
                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .apply(cropOptions.optionalCircleCrop())
                            .into(profile_edit);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    //??????????????? ?????????.
    private String getRealPathFromUri(Uri uri)
    {
        String[] proj=  {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this,uri,proj,null,null,null);
        Cursor cursor = cursorLoader.loadInBackground();

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return  url;
    }


    public void updateUser(String passwd, String birth, String phone, String uri){
        Firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPassword = passwd;

        //????????? ?????? ?????? ???
        if(imageUrl.equals("")){


            UserDTO userDTO = new UserDTO();
            userDTO.setUserId(exUserDTO.getUserId());
            userDTO.setUserName(exUserDTO.getUserName());
            userDTO.setUserNickname(exUserDTO.getUserNickname());
            userDTO.setUserProfile(exUserDTO.getUserProfile());
            userDTO.setUserBirth(birth);
            userDTO.setUserPhone(phone);
            userDTO.setUserPosts(exUserDTO.getUserPosts());
            userDTO.setUserFavorite(exUserDTO.getUserFavorite());
            if(passwdChange)
                userDTO.setUserPasswd(passwd);
            else{
                userDTO.setUserPasswd(exUserDTO.getUserPasswd());
            }
            //????????? ?????? ????????????
            Firestore.collection("users").document(email).set(userDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            //??????????????? ???????????? ?????? ???????????? ????????????
            if(passwdChange)
            {
                try{
                    //???????????? ????????????
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("???????????? ??????", "User password updated.");
                                    }
                                }
                            });
                }
                catch (Exception e){
                    Log.d("?????? ?????? ??????", "?????? ??????");
                    e.printStackTrace();
                }
            }



        }
        else{   //????????? ?????? ?????????
            try {
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();

                Uri file = Uri.fromFile(new File(uri));
                final StorageReference riversRef = storageRef.child("images/" + file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return riversRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            //????????????????????? ?????????????????? ?????????
                            @SuppressWarnings("VisibleForTests")
                            Uri downloadUrl = task.getResult();

                            Firestore = FirebaseFirestore.getInstance();
                            FireDatabase = FirebaseDatabase.getInstance();

                            UserDTO userDTO = new UserDTO();
                            userDTO.setUserId(exUserDTO.getUserId());
                            userDTO.setUserName(exUserDTO.getUserName());
                            userDTO.setUserNickname(exUserDTO.getUserNickname());
                            userDTO.setUserProfile(downloadUrl.toString());
                            userDTO.setUserBirth(birth);
                            userDTO.setUserPhone(phone);
                            userDTO.setUserPosts(exUserDTO.getUserPosts());
                            userDTO.setUserFavorite(exUserDTO.getUserFavorite());

                            if(passwdChange)
                                userDTO.setUserPasswd(passwd);
                            else{
                                userDTO.setUserPasswd(exUserDTO.getUserPasswd());
                            }

                            UpdateUserStore(email, userDTO);

                            //????????? ????????? ?????? ?????? ????????????
                            UpdateRdb(email, downloadUrl.toString());

                            //??????????????? ???????????? ?????? ???????????? ????????????
                            if(passwdChange)
                            {
                                try{
                                    //???????????? ????????????
                                    user.updatePassword(newPassword)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("???????????? ??????", "User password updated.");
                                                    }
                                                }
                                            });
                                }
                                catch (Exception e){
                                    Log.d("?????? ?????? ??????", "?????? ??????");
                                    e.printStackTrace();
                                }
                            }

                        } else {
                        }
                    }
                });

            } catch (Exception e) { }
        }
    }




    //???????????? bottomSheet ???????????????
    View.OnClickListener birthBottom = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Information_modi_bottomsheet bsd = new Information_modi_bottomsheet();
            bsd.show(getSupportFragmentManager(),"birth_bottom_sheet");
        }
    };


    //birthBottomSheet ?????? ????????? ???????????? ??? ????????? setText
    @Override
    public void setDate(int year, int month, int date) {
        birthday_edit.setText(year + "??? "+ month + "??? " + date +"???");
    }

    //????????? ?????? ????????????
    private void UpdateUserStore(String email, UserDTO userDTO){
        Firestore.collection("users").document(email).set(userDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    //RDB ?????? - ????????? ????????? ?????? ??? ?????? ??????
    private void UpdateRdb(String email, String changedUrl){

        //???????????? ??????
        mCommentsComReference.orderByChild("userId_comment").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot d : dataSnapshot.getChildren()) {

                    for (DataSnapshot dd : d.getChildren()) {

                        CommentDTO commentDTO = dd.getValue(CommentDTO.class);
                        if(commentDTO.userId_comment.equals(email)){
                            //??????
                            commentDTO.userProfile_comment = changedUrl;
                            dd.getRef().setValue(commentDTO);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //?????? ??????
        mCommentsSupReference.orderByChild("userId_comment").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {

                    for (DataSnapshot dd : d.getChildren()) {

                        for(DataSnapshot ddd : dd.getChildren()){
                            CommentDTO commentDTO = ddd.getValue(CommentDTO.class);

                            if(commentDTO.userId_comment.equals(email)){
                                //??????
                                commentDTO.userProfile_comment = changedUrl;
                                ddd.getRef().setValue(commentDTO);
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //?????? ??????
        mReviewsReference.orderByChild("recommendReviewProfile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {

                    for (DataSnapshot dd : d.getChildren()) {

                        Recommend_reviewDTO reviewDTO = dd.getValue(Recommend_reviewDTO.class);
                        if(reviewDTO.getRecommendReviewId().equals(email)){
                            //??????
                            reviewDTO.setRecommendReviewProfile(changedUrl);
                            dd.getRef().setValue(reviewDTO);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //???????????? ????????? ????????? ?????? ?????????
        Firestore.collection("communication")
                .whereEqualTo("communityUserNickName", exUserDTO.getUserNickname())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                CommunityDTO communityDTO = document.toObject(CommunityDTO.class);

                                communityDTO.setCommunityUserProfile(changedUrl);

                                Firestore.collection("communication")
                                        .document(document.getId())
                                        .set(communityDTO)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("???????????? ?????? ??????", "?????????");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("???????????? ?????? ??????", "?????????");
                                    }
                                });
                            }
                        } else {

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        
                    }
                });
    }


    //??? ??????
    private boolean validateForm() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        String password = passwd_edit.getText().toString();
        String passwdOk = passwdOk_edit.getText().toString();
        String phonenum = phoneNum_edit.getText().toString();


        //????????????
        if (TextUtils.isEmpty(password)) {
            valid = false;
            passwd_edit.requestFocus();
            imm.showSoftInput(passwd_edit, InputMethodManager.SHOW_IMPLICIT);
        }
        //???????????? ??????
        else if(TextUtils.isEmpty(passwdOk)){
            valid = false;
            passwdOk_edit.requestFocus();
            imm.showSoftInput(passwdOk_edit, InputMethodManager.SHOW_IMPLICIT);
        } else if(passwdOkL_edit.getError() != null){
            valid = false;
            passwdOk_edit.requestFocus();
            imm.showSoftInput(passwdOk_edit, InputMethodManager.SHOW_IMPLICIT);
        }
        //????????????
        else if(TextUtils.isEmpty(phonenum)){
            valid = false;
            phoneNum_edit.requestFocus();
            imm.showSoftInput(phoneNum_edit, InputMethodManager.SHOW_IMPLICIT);
        }

        return valid;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //?????? ???????????? ??????
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void firstLoad(){
        FirebaseUser currentUser = user.getCurrentUser();
        if(currentUser != null){
            email = currentUser.getEmail();

            //????????? ??????????????????
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //????????? ?????? ????????????
                    exUserDTO = documentSnapshot.toObject(UserDTO.class);
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);

                    name_edit.setText(userDTO.getUserName());
                    nickname_edit.setText(userDTO.getUserNickname());
                    passwd_edit.setText(userDTO.getUserPasswd());
                    id_edit.setText(userDTO.getUserId());
                    phoneNum_edit.setText(userDTO.getUserPhone());
                    birthday_edit.setText(userDTO.getUserBirth());

                    //????????? ????????? ?????? ????????????
                    RequestOptions cropOptions = new RequestOptions();
                    String url = userDTO.getUserProfile();
                    Glide.with(getApplicationContext())
                            .load(url)
                            .apply(cropOptions.optionalCircleCrop())
                            .into(profile_edit);
                }
            });
        }
    }

}
