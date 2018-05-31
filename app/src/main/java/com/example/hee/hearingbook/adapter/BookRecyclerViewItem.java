package gony.com.hearingbook.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;



public class BookRecyclerViewItem implements Serializable {

    private String bookAuthor;                                      // 저자
    private String bookGenre;                                       // 장르
    private String bookImage;                                       // 책이미지
    private String bookName;                                        // 책제목
    private String bookPoint;                                       // 책가격(포인트)
    private int bookSellerCount;                                    // 책판매부수
    private String bookSummary;                                     // 책요약
    private String bookUniqID;                                      // 책ID
    private ArrayList<HashMap<String, String>> bookContents;        // 책목차

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookGenre() {
        return bookGenre;
    }

    public void setBookGenre(String bookGenre) {
        this.bookGenre = bookGenre;
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookPoint() {
        return bookPoint;
    }

    public void setBookPoint(String bookPoint) {
        this.bookPoint = bookPoint;
    }

    public int getBookSellerCount() {
        return bookSellerCount;
    }

    public void setBookSellerCount(int bookSellerCount) {
        this.bookSellerCount = bookSellerCount;
    }

    public String getBookSummary() {
        return bookSummary;
    }

    public void setBookSummary(String bookSummary) {
        this.bookSummary = bookSummary;
    }

    public String getBookUniqID() {
        return bookUniqID;
    }

    public void setBookUniqID(String bookUniqID) {
        this.bookUniqID = bookUniqID;
    }

    public ArrayList<HashMap<String, String>> getBookContents() {
        return bookContents;
    }

    public void setBookContents(ArrayList<HashMap<String, String>> bookContents) {
        this.bookContents = bookContents;
    }
}
