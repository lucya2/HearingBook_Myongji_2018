package com.example.hee.hearingbook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import butterknife.OnClick;
import gony.com.hearingbook.R;
import gony.com.hearingbook.common.CommonData;
import gony.com.hearingbook.utils.FirebaseDatabaseUtils;

public class LoginActivity extends BaseActivity implements FirebaseDatabaseUtils.CallbackListener {

    final static private Logger L = LoggerFactory.getLogger(LoginActivity.class);

    // kakao login 자원
    private SessionCallback callback;

    // google&firebase login 자원
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    // firebase database 자원
    private FirebaseDatabaseUtils firebaseDatabaseUtils;

    // class member 자원
    private String userImage = "";

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);
        L.info("LoginActivity onViewReady CALL");
        setLayout();
        setProcess();
    }

    /**
     * layout init
     */
    private void setLayout() {
    }

    /**
     * process init
     */
    private void setProcess() {
        // firebase database init
        if (firebaseDatabaseUtils == null) {
            firebaseDatabaseUtils = new FirebaseDatabaseUtils();
        }
        firebaseDatabaseUtils.setCallbackListener(this);

        // kakao auth call back init
        setConfigKakaoLogin();

        // google&firebase auth init
        setConfigGoogleLogin();
    }

    /**
     * click event init
     */
    @OnClick({R.id.google_login})
    void onClickListener(View view) {
        switch (view.getId()) {
            case R.id.google_login:
                executeSignUpGoogle();
                break;
        }
    }

    /** ///////////////////////////// kakao login ///////////////////////////// */
    /**
     * kakao login Init
     */
    private void setConfigKakaoLogin() {
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
    }
    /**
     * kakao auth session call back inner class
     */
    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.requestMe(new MeResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(), "kakao login 에 실패 하였습니다.(because onFailure)", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(), "kakao login 에 실패 하였습니다.(because onSessionClosed)", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onNotSignedUp() {
                    Toast.makeText(getApplicationContext(), "kakao login 에 실패 하였습니다.(because onNotSignedUp)", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onSuccess(UserProfile userProfile) {
                    // 사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공
                    // 기존 회원인지 여부를 체크하기 위해 firebase database를 연계한다.
                    CommonData.getInstance().setLoginID(Long.toString(userProfile.getId()));
                    if (userProfile.getProfileImagePath() != null) {
                        userImage = userProfile.getProfileImagePath();
                    }
                    firebaseDatabaseUtils.select(CommonData.getInstance().USER_TABLE, "userID", CommonData.getInstance().getLoginID(), LoginActivity.this);
                }
            });
        }
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
//            Toast.makeText(getApplicationContext(), "kakao 계정 logout 되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    /** ///////////////////////////// google&firebase login ///////////////////////////// */
    /**
     * google&firebase login Init
     */
    private void setConfigGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
    }
    /**
     * google login start
     */
    private void executeSignUpGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getLastSignedInAccount(this);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class); // google 로그인 성공
                firebaseAuthWithGoogle(account);    // firebase 로그인 수행
            } catch (ApiException e) { // google 로그인 실패
                Toast.makeText(this, "Google login에 실패 하였습니다.(because " + e.getLocalizedMessage() + ")", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * firebase login start
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(getClass().toString(), "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // firebase login 성공
                            Log.d(getClass().toString(), "firebaseAuth:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            L.debug("UserProfile", firebaseUser.getEmail() + "");
                            CommonData.getInstance().setLoginID(firebaseUser.getEmail());
                            if (firebaseUser.getPhotoUrl() != null) {
                                userImage = firebaseUser.getPhotoUrl().toString();
                            }
                            // 기존 회원인지 여부를 체크하기 위해 firebase database를 연계한다.
                            firebaseDatabaseUtils.select(CommonData.getInstance().USER_TABLE, "userID", CommonData.getInstance().getLoginID(), LoginActivity.this);
                        } else {
                            // firebase login 실패
                            Toast.makeText(getApplicationContext(), "Google login에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onFireBaseCallBack(HashMap<String, Object> data, String TABLE_NAME) {
        CommonData.getInstance().setLogin(true);
        if (data == null) { // 최초 회원
            // 회원 가입을 수행한다.
            HashMap<String, Object> signUserData = new HashMap<>();
            signUserData.put("userID", CommonData.getInstance().getLoginID());
            signUserData.put("point", "0");
            signUserData.put("userImage", userImage);
            CommonData.getInstance().setPoint("0");
            CommonData.getInstance().setAccountImageUrl(userImage);
            firebaseDatabaseUtils.insert(CommonData.getInstance().USER_TABLE, signUserData);
        } else { // 기존 회원
            for (Object o : data.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                HashMap<String, String> detailSubData = (HashMap<String, String>) entry.getValue();
                CommonData.getInstance().setLoginID(detailSubData.get("userID"));
                CommonData.getInstance().setPoint(detailSubData.get("point"));
                CommonData.getInstance().setAccountImageUrl(detailSubData.get("userImage"));
            }
        }
        // 메인 화면으로 복귀
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
