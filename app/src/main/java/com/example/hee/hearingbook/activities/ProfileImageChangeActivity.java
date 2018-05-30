package com.example.hee.hearingbook.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import gony.com.hearingbook.HearingBookApplication;
import gony.com.hearingbook.R;
import gony.com.hearingbook.common.CommonData;
import gony.com.hearingbook.utils.FirebaseDatabaseUtils;
import gony.com.hearingbook.utils.FirebaseStorageUtils;
import gony.com.hearingbook.utils.GonSoftwareUtils;
import io.reactivex.functions.Consumer;

public class ProfileImageChangeActivity extends BaseActivity implements FirebaseDatabaseUtils.CallbackListener,
        FirebaseStorageUtils.Firebase_Storage_CallbackListener {

    final static private Logger L = LoggerFactory.getLogger(ProfileImageChangeActivity.class);
    // 카메라 연계
    private final static int REQUEST_CAMERA = 1;
    // 갤러리 연계
    private final static int REQUEST_GALLERY = 2;

    // firebase database 객체
    private FirebaseDatabaseUtils firebaseDatabaseUtils;
    // firebase storage 객체
    private FirebaseStorageUtils firebaseStorageUtils;
    // 현재 로그인 유져의 USER_TABLE uniq root key
    private String uniqKey = "";
    // firebase storage upload url
    private String uploadImageUrlforFirebaseStorage = "";
    // 프로필 변경 성공 flag
    private boolean mProfileImageChangeSuccess = false;

    private static Uri mImageUri;
    private static Uri mUriCamera;

    @BindView(R.id.title)
    TextView title;

    @Override
    protected int getContentView() {
        return R.layout.activity_profile_image_change;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);
        L.info("ProfileImageChangeActivity onViewReady CALL");
        setLayout();
        checkPermissions();
    }

    /**
     * layout init
     */
    private void setLayout() {
        title.setText(getResources().getString(R.string.profile_change_image));
    }

    private static final int PERMISSION_REQUEST = 1000;
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // permission 작업
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_CONTACTS)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) {
                            if (!aBoolean) {
                                Toast.makeText(HearingBookApplication.getAppContext(), getResources().getString(R.string.need_access_picture),
                                        Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        }
                    });
        }
    }


    @OnClick({R.id.back_btn, R.id.camera, R.id.gallery})
    void onClickListener(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.camera:
                doPicture(REQUEST_CAMERA);
                break;
            case R.id.gallery:
                doPicture(REQUEST_GALLERY);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if (mProfileImageChangeSuccess) {
            setResult(RESULT_OK, intent);
            finish();
        } else {
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    /**
     * 카메라 || 갤러리 연동
     */
    private void doPicture(int requestCode) {
        Intent intent;
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (Environment.getExternalStorageState().equals("mounted")) {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ContentValues values = new ContentValues();
                    values.put("title", System.currentTimeMillis() + "");
                    values.put("mime_type", "image/jpeg");
                    mImageUri = HearingBookApplication.getAppContext().getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    intent.putExtra("orientation", 0);
                    intent.putExtra("output", mImageUri);
                    startActivityForResult(intent, requestCode);
                }
                break;
            case REQUEST_GALLERY:
                if (Build.VERSION.SDK_INT > 19) {
                    intent = new Intent("android.intent.action.OPEN_DOCUMENT");
                    intent.addCategory("android.intent.category.OPENABLE");
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Choose an image"), requestCode);
                    return;
                }
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction("android.intent.action.PICK");
                startActivityForResult(intent, requestCode);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            switch (requestCode) {
                // 카메라, 갤러리 연동 후 callback
                case REQUEST_CAMERA:
                    if (data == null) {
                        mUriCamera = mImageUri;
                    } else if (data.getData() == null) {
                        mUriCamera = mImageUri;
                    } else {
                        mUriCamera = data.getData();
                    }
                    bitmap = GonSoftwareUtils.getInstance().getImageBitmap(this, mImageUri);
                    break;
                case REQUEST_GALLERY:
                    Bitmap captureBitmap;
                    if (data.getData() == null) { // 일부 폰에서 카메라 촬영 후 Uri 가 null 인 case가 발견(그런 경우에는 bitmap으로 처리하여 Uri 추출)
                        captureBitmap = (Bitmap) data.getExtras().get("data");
                        bitmap = captureBitmap;
                    } else {
                        bitmap = GonSoftwareUtils.getInstance().getImageBitmap(this, data.getData());
                    }
                    break;
            }

            if (bitmap != null) {
                uploadImage(bitmap);
            }
        }
    }

    public void uploadImage(Bitmap bitmap) {
        if (firebaseStorageUtils == null) {
            firebaseStorageUtils = new FirebaseStorageUtils();
            firebaseStorageUtils.setFirebase_storage_callbackListener(this);
        }
        firebaseStorageUtils.uploadImage("images/" + GonSoftwareUtils.getInstance().getUniqKey(), bitmap);
    }

    @Override
    public void onFireBaseStorageCallBack(String uri) {
        uploadImageUrlforFirebaseStorage = uri;
        // 현재 ID의 uniqKey를 추출하기 위한 select 수행
        if (firebaseDatabaseUtils == null) {
            firebaseDatabaseUtils = new FirebaseDatabaseUtils();
            firebaseDatabaseUtils.setCallbackListener(this);
        }
        firebaseDatabaseUtils.select(CommonData.getInstance().USER_TABLE, "userID", CommonData.getInstance().getLoginID(), this);
    }

    @Override
    public void onFireBaseCallBack(HashMap<String, Object> data, String TABLE_NAME) {
        if (data != null) {
            for (Object o : data.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                uniqKey = entry.getKey().toString();
            }

            // 기존 USER_TABLE에 userImage data를 update
            if (firebaseDatabaseUtils == null) {
                firebaseDatabaseUtils = new FirebaseDatabaseUtils();
                firebaseDatabaseUtils.setCallbackListener(this);
            }
            HashMap<String, String> updateData = new HashMap<>();
            updateData.put("userImage", uploadImageUrlforFirebaseStorage);

            firebaseDatabaseUtils.update(CommonData.getInstance().USER_TABLE, uniqKey, updateData);
            CommonData.getInstance().setAccountImageUrl(uploadImageUrlforFirebaseStorage);
            mProfileImageChangeSuccess = true;
            Toast.makeText(this, getResources().getString(R.string.profile_change_image_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.profile_change_image_fail), Toast.LENGTH_SHORT).show();
        }
    }
}
