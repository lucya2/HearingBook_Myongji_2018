package gony.com.hearingbook.adapter;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import gony.com.hearingbook.HearingBookApplication;
import gony.com.hearingbook.R;
import gony.com.hearingbook.activities.BookDetailPage;



public class ContentsAdapter extends RecyclerView.Adapter<ContentsAdapter.ViewHolder> {

    private ArrayList<ContentsRecyclerViewItem> mContentsRecyclerViewItems;
    private View v;
    /**
     * 생성자
     */
    public ContentsAdapter(ArrayList<ContentsRecyclerViewItem> contentsRecyclerViewItems) {
        this.mContentsRecyclerViewItems = contentsRecyclerViewItems;
    }

    @Override
    public ContentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contents, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContentsAdapter.ViewHolder holder, final int position) {
        final ContentsRecyclerViewItem mContentsRecyclerViewItem = mContentsRecyclerViewItems.get(position);
        if (mContentsRecyclerViewItem != null) {
            holder.bookContentsItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContentsRecyclerViewItem contentsRecyclerViewItem = mContentsRecyclerViewItems.get(position);
                    Intent intent = new Intent(HearingBookApplication.getCurrentActivity(), BookDetailPage.class);
                    intent.putExtra("contentsItem", contentsRecyclerViewItem);
                    HearingBookApplication.getCurrentActivity().startActivity(intent);
                }
            });
            // data parser
            holder.bookContentsName.setText(mContentsRecyclerViewItem.getBookContentsName());
        }
    }

    @Override
    public int getItemCount() {
        return this.mContentsRecyclerViewItems.size();
    }

    /** item layout 불러오기 **/
    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout bookContentsItemLayout;
        TextView bookContentsName;

        public ViewHolder(View v) {
            super(v);
            bookContentsItemLayout = v.findViewById(R.id.book_contents_item_layout);
            bookContentsName = v.findViewById(R.id.book_contents_name);
        }
    }

    // 아이템 데이터 추가 method
    public void addItem(String bookContentsName, String bookContentsUniqID) {
        ContentsRecyclerViewItem item = new ContentsRecyclerViewItem();
        item.setBookContentsName(bookContentsName);
        item.setBookContentsUniqID(bookContentsUniqID);

        mContentsRecyclerViewItems.add(item);
    }
}
