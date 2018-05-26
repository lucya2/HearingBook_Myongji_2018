package com.example.hee.hearingbook.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import com.example.hee.hearingbook.R;
import com.example.hee.hearingbook.adapter.BookAdapter;
import com.example.hee.hearingbook.adapter.BookRecyclerViewItem;
import com.example.hee.hearingbook.common.CommonData;
import com.example.hee.hearingbook.utils.FirebaseDatabaseUtils;
import com.example.hee.hearingbook.utils.GonSoftwareUtils;

public class MainActivity extends BaseActivity implements FirebaseDatabaseUtils.CallbackListener {

    final static private Logger L = LoggerFactory.getLogger(MainActivity.class);
    private final static long FINISH_INTERVAL_TIME = 2000;
    private long  backPressedTime = 0;
    // 로그인 연계
    private final static int REQUEST_LOGIN = 1;
    // 마이페이지 연계
    private final static int REQUEST_MYPAGE = 2;

    // google login check variable
    private FirebaseAuth mAuth;

    // firebase database 자원
    private FirebaseDatabaseUtils firebaseDatabaseUtils;

    // class member 자원
    private String userImage = "";

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.non_login_layout)
    ConstraintLayout nonLoginLayout;
    @BindView(R.id.success_login_layout)
    ConstraintLayout successLoginLayout;
    @BindView(R.id.profile_image)
    ImageView profileImage;
    @BindView(R.id.point)
    TextView point;
    @BindView(R.id.best_seller_recycler_view)
    RecyclerView bestSellerRecyclerView;
    private BookAdapter bookAdapter;
    private ArrayList<BookRecyclerViewItem> bookRecyclerViewItems = new ArrayList<BookRecyclerViewItem>();

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);
        L.info("MainActivity onViewReady CALL");
        setLayout();
        setProcess();
    }

    /**
     * layout Init method
     */
    private void setLayout() {
        title.setText(getResources().getString(R.string.app_name_title));
        bestSellerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bestSellerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        bookAdapter = new BookAdapter(bookRecyclerViewItems);
        bestSellerRecyclerView.setAdapter(bookAdapter);
    }

    /**
     * process Init method
     */
    private void setProcess() {
        // kakao login session 여부 check
        kakaoIsSession();
    }

    @OnClick({R.id.navigation_menu, R.id.drawer_close_btn, R.id.login, R.id.search_book, /*R.id.radio,*/ R.id.mypage, R.id.buy_point_btn,
            R.id.best_seller_all_view})
    void onClickListener(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.navigation_menu:      // 상단 toolbar navigation menu 버튼
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.drawer_close_btn:     // drawer close 버튼
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.login:                // drawer login 버튼
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, REQUEST_LOGIN);
                break;
            case R.id.search_book:          // drawer 도서 검색 메뉴 버튼
                intent = new Intent(getApplicationContext(), SearchBookActivity.class);
                startActivity(intent);
                break;
            /*case R.id.radio:                // drawer 라디오 메뉴 버튼
                intent = new Intent(getApplicationContext(), RadioActivity.class);
                startActivity(intent);
                break;*/
            case R.id.mypage:               // drawer 마이페이지 메뉴 버튼
                if (CommonData.getInstance().isLogin()) {
                    intent = new Intent(getApplicationContext(), MypageActivity.class);
                    startActivityForResult(intent, REQUEST_MYPAGE);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.not_login), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buy_point_btn:        // 포인트 충전 버튼
                intent = new Intent(getApplicationContext(), BuyPointActivity.class);
                startActivity(intent);
                break;
            case R.id.best_seller_all_view:  // 전체 보기 버튼
                intent = new Intent(getApplicationContext(), BestSellerFullActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            long tempTime        = System.currentTimeMillis();
            long intervalTime    = tempTime - backPressedTime;
            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), "\"뒤로\" 버튼을 한 번 더 누르시면 종료 됩니다." , Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // 마이페이지>포인트구매 후 복귀 시 point 갱신
        int numberOfSpaces = 2;
        point.setText(String.format("%s%" + numberOfSpaces + "s", CommonData.getInstance().getPoint(), getResources().getString(R.string.point)));
        // firebase database init
        if (firebaseDatabaseUtils == null) {
            firebaseDatabaseUtils = new FirebaseDatabaseUtils();
        }
        firebaseDatabaseUtils.setCallbackListener(this);

        // book_info_table select for best seller recyclerview
        if(bookRecyclerViewItems.size() > 0) {
            bookRecyclerViewItems.clear();
            bookAdapter.notifyDataSetChanged();
        }
        firebaseDatabaseUtils.select(CommonData.getInstance().BOOK_INFO_TABLE, "", "", this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // 로그인 화면 연동 후 callback
                case REQUEST_LOGIN:
                case REQUEST_MYPAGE:
                    loginUpdateUI();
                    break;
            }
        }
    }

    /**
     * 기존에 kakao auth session 여부 체크(있는 경우에 자동 로그인 수행)
     */
    public void kakaoIsSession() {
        // 유저의 기존 session 정보를 받아오는 함수
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                L.debug("onFailure ");
                googleIsSession();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                L.debug("onSessionClosed ");
                googleIsSession();
            }

            @Override
            public void onNotSignedUp() {
                L.debug("onNotSignedUp ");
                googleIsSession();
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                CommonData.getInstance().setLoginID(Long.toString(userProfile.getId()));
                if (userProfile.getProfileImagePath() != null) {
                    userImage = userProfile.getProfileImagePath();
                }
                // 기존 회원인지 여부를 체크하기 위해 firebase database를 연계한다.
                firebaseDatabaseUtils.select(CommonData.getInstance().USER_TABLE, "userID", CommonData.getInstance().getLoginID(), MainActivity.this);
            }
        });
    }

    /**
     * 기존에 google auth session 여부 체크(있는 경우에 자동 로그인 수행)
     */
    public void googleIsSession() {
        if (mAuth == null) mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            CommonData.getInstance().setLoginID(firebaseUser.getEmail());
            if (firebaseUser.getPhotoUrl() != null) {
                userImage = firebaseUser.getPhotoUrl().toString();
            }
            // 기존 회원인지 여부를 체크하기 위해 firebase database를 연계한다.
            firebaseDatabaseUtils.select(CommonData.getInstance().USER_TABLE, "userID", CommonData.getInstance().getLoginID(), MainActivity.this);
        }
    }

    @Override
    public void onFireBaseCallBack(HashMap<String, Object> data, String TABLE_NAME) {
        if (TABLE_NAME.equals(CommonData.getInstance().USER_TABLE)) {
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
                    HashMap<String, Object> detailSubData = (HashMap<String, Object>) entry.getValue();
                    CommonData.getInstance().setLoginID((String)detailSubData.get("userID"));
                    CommonData.getInstance().setPoint((String)detailSubData.get("point"));
                    CommonData.getInstance().setAccountImageUrl((String)detailSubData.get("userImage"));
                }
            }

            loginUpdateUI();
        } else if (TABLE_NAME.equals(CommonData.getInstance().BOOK_INFO_TABLE)) {
            if (data != null) {
                for (Object o : data.entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    HashMap<String, Object> detailSubData = (HashMap<String, Object>) entry.getValue();
                    bookAdapter.addItem((String)detailSubData.get("bookAuthor"), (String)detailSubData.get("bookGenre"),
                            (String)detailSubData.get("bookImage"), (String)detailSubData.get("bookName"),
                            (String)detailSubData.get("bookPoint"), Integer.parseInt((String)detailSubData.get("bookSellerCount")),
                            (String)detailSubData.get("bookSummary"), (ArrayList<HashMap<String, String>>)detailSubData.get("bookContents"),
                            (String)detailSubData.get("bookUniqID"));
                }
                sortingRanking();
            }
        }
    }

    /**
     * 로그인 여부에 따라 UI 변경(drawer navigation)
     */
    private void loginUpdateUI() {
        if (CommonData.getInstance().isLogin()) {
            nonLoginLayout.setVisibility(View.GONE);
            successLoginLayout.setVisibility(View.VISIBLE);
            if (!CommonData.getInstance().getAccountImageUrl().equals("")) {
                GonSoftwareUtils.getInstance().glideCropCircleForContext(this, CommonData.getInstance().getAccountImageUrl(), null, profileImage);
            } else {
                GonSoftwareUtils.getInstance().glideCropCircleForContext(this, "", getResources().getDrawable(R.drawable.default_profile), profileImage);
            }
            int numberOfSpaces = 2;
            point.setText(String.format("%s%" + numberOfSpaces + "s", CommonData.getInstance().getPoint(), getResources().getString(R.string.point)));
        } else {
            nonLoginLayout.setVisibility(View.VISIBLE);
            successLoginLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 베스트셀러 sorting method
     */
    private void sortingRanking() {
        Comparator<BookRecyclerViewItem> noDesc = new Comparator<BookRecyclerViewItem>() {
            @Override
            public int compare(BookRecyclerViewItem item1, BookRecyclerViewItem item2) {
                return (item2.getBookSellerCount() - item1.getBookSellerCount());
            }
        };

        Collections.sort(bookRecyclerViewItems, noDesc);
        bookAdapter.notifyDataSetChanged();
    }
}
