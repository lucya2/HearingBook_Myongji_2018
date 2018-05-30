package com.example.hee.hearingbook.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.OnClick;
import gony.com.hearingbook.R;
import gony.com.hearingbook.common.CommonData;
import gony.com.hearingbook.utils.GonSoftwareUtils;

public class MypageActivity extends BaseActivity {

    final static private Logger L = LoggerFactory.getLogger(MypageActivity.class);
    private final int REQUEST_PROFILE_IMAGE = 1;

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.profile_image)
    ImageView profileImage;
    @BindView(R.id.point)
    TextView point;

    @Override
    protected int getContentView() {
        return R.layout.activity_mypage;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);
        L.info("MypageActivity onViewReady CALL");
        setLayout();
        setProcess();
    }

    private void setLayout() {
        title.setText(getResources().getString(R.string.mypage));
    }

    private void setProcess() {
        if (!CommonData.getInstance().getAccountImageUrl().equals("")) {
            GonSoftwareUtils.getInstance().glideCropCircleForContext(this, CommonData.getInstance().getAccountImageUrl(), null, profileImage);
        } else {
            GonSoftwareUtils.getInstance().glideCropCircleForContext(this, "", getResources().getDrawable(R.drawable.default_profile_8e44ad), profileImage);
        }
        point.setText(String.format("%s%s", CommonData.getInstance().getPoint(), getResources().getString(R.string.point)));
    }

    /**
     * kakao logout method
     */
    private void kakaoLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                CommonData.getInstance().setLogin(false);
                CommonData.getInstance().setLoginID("");
                CommonData.getInstance().setPoint("");
                CommonData.getInstance().setAccountImageUrl("");
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * google logout method
     */
    private void googleLogout() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();
        // Google sign out
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //로그아웃 성공 후 하고싶은 내용 코딩 ~
                    }
                });
    }

    @OnClick({R.id.back_btn, R.id.profile_image, R.id.logout, R.id.favorit, R.id.shoppin_basket, R.id.purchase_history, R.id.buy_point_text})
    void onClickListener(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.back_btn:
                // 메인 화면으로 복귀
                intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.profile_image:
                intent = new Intent(getApplicationContext(), ProfileImageChangeActivity.class);
                startActivityForResult(intent, REQUEST_PROFILE_IMAGE);
                break;
            case R.id.logout:
                kakaoLogout();
                googleLogout();
                break;
            case R.id.favorit:
                intent = new Intent(getApplicationContext(), FavoritActivity.class);
                startActivity(intent);
                break;
            case R.id.shoppin_basket:
                intent = new Intent(getApplicationContext(), ShoppingBasketActivity.class);
                startActivity(intent);
                break;
            case R.id.purchase_history:
                intent = new Intent(getApplicationContext(), PurchaseHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.buy_point_text:
                intent = new Intent(getApplicationContext(), BuyPointActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // 프로필 이미지 변경 후 call back
                case REQUEST_PROFILE_IMAGE:
                    GonSoftwareUtils.getInstance().glideCropCircleForContext(this, CommonData.getInstance().getAccountImageUrl(), null, profileImage);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 마이페이지>포인트구매 후 복귀 시 point 갱신
        point.setText(String.format("%s%s", CommonData.getInstance().getPoint(), getResources().getString(R.string.point)));
    }
}

