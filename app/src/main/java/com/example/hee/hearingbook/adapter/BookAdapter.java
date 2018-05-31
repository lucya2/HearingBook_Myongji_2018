package gony.com.hearingbook.adapter;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import gony.com.hearingbook.HearingBookApplication;
import gony.com.hearingbook.R;
import gony.com.hearingbook.activities.BookInfoPage;
import gony.com.hearingbook.activities.FavoritActivity;
import gony.com.hearingbook.utils.GonSoftwareUtils;


public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private ArrayList<BookRecyclerViewItem> mBookList;
    private View v;
    /**
     * 생성자
     */
    public BookAdapter(ArrayList<BookRecyclerViewItem> bookList) {
        this.mBookList = bookList;
    }

    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_content, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BookAdapter.ViewHolder holder, final int position) {
        final BookRecyclerViewItem mBookItem = mBookList.get(position);
        if (mBookItem != null) {
            holder.bookInfoItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BookRecyclerViewItem bookRecyclerViewItem = mBookList.get(position);
                    Intent intent = new Intent(HearingBookApplication.getCurrentActivity(), BookInfoPage.class);
                    intent.putExtra("bookItem", bookRecyclerViewItem);
                    HearingBookApplication.getCurrentActivity().startActivity(intent);
                }
            });
            // data parser
            GonSoftwareUtils.getInstance().glideCenterCropForContext(v.getContext(), mBookItem.getBookImage(), holder.bookImage);
            holder.bookName.setText(mBookItem.getBookName());
            holder.bookAuthor.setText(mBookItem.getBookAuthor());
        }
    }

    @Override
    public int getItemCount() {
        return this.mBookList.size();
    }

    /** item layout 불러오기 **/
    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout bookInfoItemLayout;
        ImageView bookImage;
        TextView bookName;
        TextView bookAuthor;

        public ViewHolder(View v) {
            super(v);
            bookInfoItemLayout = v.findViewById(R.id.book_info_item_layout);
            bookImage = v.findViewById(R.id.book_image);
            bookName = v.findViewById(R.id.book_name);
            bookAuthor = v.findViewById(R.id.book_author);
        }
    }

    // 아이템 데이터 추가 method
    public void addItem(String bookAuthor, String bookGenre, String bookImage, String bookName, String bookPoint,
                        int bookSellerCount, String bookSummary, ArrayList<HashMap<String, String>> bookContents, String bookUniqID) {
        BookRecyclerViewItem item = new BookRecyclerViewItem();
        item.setBookAuthor(bookAuthor);
        item.setBookGenre(bookGenre);
        item.setBookImage(bookImage);
        item.setBookName(bookName);
        item.setBookPoint(bookPoint);
        item.setBookSellerCount(bookSellerCount);
        item.setBookSummary(bookSummary);
        item.setBookContents(bookContents);
        item.setBookUniqID(bookUniqID);

        mBookList.add(item);
    }
}
