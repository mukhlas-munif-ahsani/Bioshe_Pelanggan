package com.munifahsan.biosheapp.ui.pageHome;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.munifahsan.biosheapp.R;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PromoAdapter extends SliderViewAdapter<PromoAdapter.Holder> {

    private List<PromoModel> mData;
    private OnListItemCliked onListItemCliked;

    public void setOnListItemCliked(OnListItemCliked onListItemCliked) {
        this.onListItemCliked = onListItemCliked;
    }

    public PromoAdapter() {
        this.mData = new ArrayList<>();
    }

    public void addCardItem(PromoModel item) {
        mData.add(item);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_promo, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder viewHolder, int position) {
        //viewHolder.imageView.setText(mData.get(position).getNama());
        viewHolder.id = mData.get(position).getId();

        Picasso.get()
                .load(mData.get(position).getnImage())
                .placeholder(R. drawable.black_transparent)
                .into(viewHolder.imageView);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public class Holder extends SliderViewAdapter.ViewHolder {

        ImageView imageView;
        CardView cardView;

        String id;
        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_promo);
            cardView = itemView.findViewById(R.id.cardPromo);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onListItemCliked.onItemCliked(id, getItemPosition(itemView));
                }
            });
        }

    }

    public interface OnListItemCliked {
        void onItemCliked(String id, int position);
    }

}
