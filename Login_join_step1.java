package com.example.GoAutoCamping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class Login_join_step1 extends AppCompatActivity {

    CoordinatorLayout snackbar;
    TextInputLayout phoneNumL_join, codeL_join;
    TextInputEditText phoneNum_join, code_join;

    MaterialButton btnSendcode, btnNext;
    Button btnCheckPhone, btnResendCode;

    boolean phoneVerified = false;
    boolean phoneCheck = false;
    String phone = "";
    String name = "";
    String code = "";

    //??????????????????
    private FirebaseFirestore Firestore;
    private FirebaseAuth FireAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_join_step1);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FireAuth = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();

        snackbar = findViewById(R.id.snackbar_line);
        phoneNumL_join = findViewById(R.id.phonenumLayout_join);
        codeL_join = findViewById(R.id.verificationCodeLayout_join);
        phoneNum_join = findViewById(R.id.phonenumText_join);
        code_join = findViewById(R.id.verificationCode_join);

        btnCheckPhone = findViewById(R.id.btnPhoneNumCheck);
        btnSendcode = findViewById(R.id.btnSendCode_join);
        btnResendCode = findViewById(R.id.btnResendCode);
        btnNext = findViewById(R.id.btnVerifyCode_join);


        btnCheckPhone.setVisibility(View.INVISIBLE);
        btnCheckPhone.setClickable(false);

        btnCheckPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){

                                if(document.get("userPhone").toString().equals(phoneNum_join.getText().toString())){ //???????????? ???????????? ????????????

                                    Log.d("????????? ??????????", "???????????????");

                                    phoneNumL_join.setError("?????? ???????????? ?????????????????????");
                                    phoneNumL_join.setErrorEnabled(true);

                                    return;
                                }
                                else{
                                    phoneCheck = true;
                                    phoneNumL_join.setErrorEnabled(false);
                                }
                            }
                        } else {
                            Log.d("err", "no user");
                        }
                    }
                });
            }
        });


        //????????????
        phoneNum_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = phoneNum_join.getText().toString();
                if(text.equals("")){
                    phoneNumL_join.setError("?????? ???????????????");
                }else{
                    phoneNumL_join.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = phoneNum_join.getText().toString();
                if(text.equals("")){
                    phoneNumL_join.setError("?????? ???????????????");
                }else{
                    btnCheckPhone.performClick();
                }
            }
        });

        //????????????
        code_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = code_join.getText().toString();
                if(text.equals("")){
                    codeL_join.setError("?????? ???????????????");
                }else{
                    codeL_join.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        //????????? ?????? ?????? ?????? ??????
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d("TAG", "onVerificationCompleted:" + credential);
                Log.d("?????? ??????", "??????????????????.");

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                } else if (e instanceof FirebaseTooManyRequestsException) {

                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d("TAG", "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;
            }

        };

        //???????????? ?????????
        btnSendcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = phoneNum_join.getText().toString();

                if(!TextUtils.isEmpty(phone) && phoneNumL_join.getError() == null ){
                    codeL_join.setVisibility(View.VISIBLE);
                    btnResendCode.setVisibility(View.VISIBLE);
                    startPhoneNumberVerification(phone);
                }
                else{
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    phoneNum_join.requestFocus();
                    imm.showSoftInput(phoneNum_join, InputMethodManager.SHOW_IMPLICIT);
                }

            }
        });

        //???????????? ?????? ?????????
        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = phoneNum_join.getText().toString();
                if(!TextUtils.isEmpty(phone)){
                    resendVerificationCode(phone, mResendToken);
                }
                else{
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    phoneNum_join.requestFocus();
                    imm.showSoftInput(phoneNum_join, InputMethodManager.SHOW_IMPLICIT);
                }

            }
        });

        //?????????????????? ???????????? - ???????????? ?????? ?????????
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    code = code_join.getText().toString();

                    //???????????? ????????????
                    verifyPhoneNumberWithCode(mVerificationId, code);
                }
            }
        });
    }


    //??? ??????
    private boolean validateForm() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        //????????????
        String phonenum = phoneNum_join.getText().toString();
        //??????
        String code = code_join.getText().toString();

        if(TextUtils.isEmpty(phonenum)){
            valid = false;
            phoneNum_join.requestFocus();
            imm.showSoftInput(phoneNum_join, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(phoneNumL_join.getError() != null){
            valid = false;
            phoneNum_join.requestFocus();
            imm.showSoftInput(phoneNum_join, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(TextUtils.isEmpty(code)){
            valid = false;
            code_join.requestFocus();
            imm.showSoftInput(code_join, InputMethodManager.SHOW_IMPLICIT);
        }
        else{
            //mBinding.fieldPassword.setError(null);
        }

        return valid;
    }


    //???????????? ????????????
    private void startPhoneNumberVerification(String phoneNumber) {
        String phonenum = "+82"+phoneNumber;
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FireAuth)
                        .setPhoneNumber(phonenum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);   //???????????????

        Snackbar.make(snackbar, "??????????????? ?????????????????????", Snackbar.LENGTH_LONG).show();

    }

    //???????????? ???????????????
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        String phonenum = "+82"+phoneNumber;
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FireAuth)
                        .setPhoneNumber(phonenum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        Snackbar.make(snackbar, "??????????????? ????????????????????????", Snackbar.LENGTH_LONG).show();
    }

    //??????????????? ?????????????????????
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    //TODO ???????????? ??? ???????????? ???????????? ?????? ????????????
    //??????????????? ????????? - ?????? ?????? ?????? ??????
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {


        FireAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("TAG", "signInWithCredential:success");
                            phoneVerified = true;
                            checkPhoneVerification(phoneVerified);

                        } else {

                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Snackbar.make(snackbar, "????????? ?????????????????????.", Snackbar.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            }
                        }
                    }
                });
    }

    //?????? ?????? ??? ??????
    private void checkPhoneVerification(boolean phoneVerified){
        if(phoneVerified){
            //????????????
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("?????? ???????????????", "?????? ?????? ??????");
                            }
                        }
                    });

            //????????? ????????????

            Intent intent = new Intent(Login_join_step1.this, Login_join_step2.class);
            intent.putExtra("phoneNum", phone);
            startActivity(intent);
            finish();

        }
        else{
            //???????????? ????????? ??????
            Snackbar.make(snackbar, "???????????? ??????", Snackbar.LENGTH_SHORT).show();
        }
    }

}
