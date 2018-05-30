package com.example.hee.hearingbook.common;

public class CommonData {

    /** ///////////////////// 상수 variable ///////////////////// */
    public final String USER_TABLE = "USER_TABLE";                           // 사용자 테이블
    public final String BOOK_INFO_TABLE = "BOOK_INFO_TABLE";                 // 책 정보 테이블
    public final String PURCHASE_HISTORY_TABLE = "_PURCHASE_HISTORY_TABLE";  // 구매이력 테이블
    public final String SHOPPING_BASKET_TABLE = "_SHOPPING_BASKET_TABLE";    // 장바구니 테이블
    public final String FAVORITES_TABLE = "_FAVORITES_TABLE";                // 즐겨찾기 테이블
    public final String BOOK_TEXT_TABLE = "_BOOK_TEXT_TABLE";                // 책 상세 내용 테이블
    
    /** ///////////////////// bean variable ///////////////////// */
    private boolean isLogin = false;                                        // 로그인 여부
    private String loginID = "";                                            // 로그인 ID
    private String accountImageUrl = "";                                    // 로그인 계정 이미지 url
    private String point = "";                                              // 보유 포인트

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getLoginID() {
        return loginID;
    }

    public void setLoginID(String loginID) {
        loginID = loginID.replace("@","").replace(".", "");
        this.loginID = loginID;
    }

    public String getAccountImageUrl() {
        return accountImageUrl;
    }

    public void setAccountImageUrl(String accountImageUrl) {
        this.accountImageUrl = accountImageUrl;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    // single ton
    private static CommonData instance = null;

    public static synchronized CommonData getInstance(){
        if(null == instance){
            instance = new CommonData();
        }
        return instance;
    }


}
