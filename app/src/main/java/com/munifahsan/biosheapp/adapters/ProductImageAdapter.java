package com.munifahsan.biosheapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.munifahsan.biosheapp.R;
import com.munifahsan.biosheapp.domain.Images;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductImageAdapter extends SliderViewAdapter<ProductImageAdapter.Holder> {

    private List<Images> mData;

    public ProductImageAdapter() {
        this.mData = new ArrayList<>();
    }

    public void addCardItem(Images item) {
        mData.add(item);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_image, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder viewHolder, int position) {
        //viewHolder.imageView.setText(mData.get(position).getNama());
        Picasso.get()
                .load(mData.get(position).getImage())
                .placeholder(R.drawable.black_transparent)
                .into(viewHolder.imageView);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    static class Holder extends SliderViewAdapter.ViewHolder {

        ImageView imageView;

        String id;
        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_product);
        }

    }

}
