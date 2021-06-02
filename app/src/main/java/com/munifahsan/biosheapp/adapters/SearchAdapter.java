package com.munifahsan.biosheapp.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.munifahsan.biosheapp.R;
import com.munifahsan.biosheapp.domain.Product;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Holder> implements Filterable {

    private List<Product> mModelList;
    private List<Product> mModelListFull;
    private OnListItemCliked onListItemCliked;
    private OnAddItemCliked onAddItemCliked;

    public void setOnListItemCliked(OnListItemCliked onListItemCliked) {
        this.onListItemCliked = onListItemCliked;
    }
    public void setOnAddItemCliked(OnAddItemCliked onAddItemCliked) {
        this.onAddItemCliked = onAddItemCliked;
    }

    public void setListModels(List<Product> mModelList) {
        this.mModelList = mModelList;
        mModelListFull = new ArrayList<>(mModelList);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_2, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.id = mModelList.get(position).getId();
        holder.namaPrdocut.setText(mModelList.get(position).getNama());

        Picasso.get()
                .load(mModelList.get(position).getThumbnail())
                .placeholder(R.drawable.black_transparent)
                .into(holder.image);

        holder.disconTxt.setText(String.valueOf(mModelList.get(position).getDiskon()));

        if (mModelList.get(position).getDiskon() == 0) {
            holder.linDiscon.setVisibility(View.GONE);
        } else {
            holder.linDiscon.setVisibility(View.VISIBLE);
        }

        holder.priceDisconTxt.setText(rupiahFormat(mModelList.get(position).getHarga()));
        holder.priceDisconTxt.setPaintFlags(holder.priceDisconTxt.getPaintFlags() / Paint.STRIKE_THRU_TEXT_FLAG);

        int dis = mModelList.get(position).getHarga() * mModelList.get(position).getDiskon() / 100;
        holder.priceTxt.setText(rupiahFormat(mModelList.get(position).getHarga() - dis));

        if (mModelList.get(position).getDiskon() == 0){
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)holder.priceTxt.getLayoutParams();
            layoutParams.bottomMargin = 40;
            holder.priceTxt.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        if (mModelList == null) {
            return 0;
        } else {
            return mModelList.size();
        }
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView namaPrdocut;
        ImageView image;
        TextView disconTxt;
        LinearLayout linDiscon;
        TextView priceDisconTxt;
        CardView addKeranjangBtn;
        CardView product;
        TextView priceTxt;

        String id;
        String jenis;
        boolean isPremium;

        public Holder(@NonNull View itemView) {
            super(itemView);

            namaPrdocut = itemView.findViewById(R.id.productName);
            image = itemView.findViewById(R.id.thumbnailImage);
            disconTxt = itemView.findViewById(R.id.disconTxt);
            linDiscon = itemView.findViewById(R.id.linDiscon);
            priceDisconTxt = itemView.findViewById(R.id.priceDisconTxt);
            addKeranjangBtn = itemView.findViewById(R.id.keranjangBtn);
            image = itemView.findViewById(R.id.thumbnailImage);
            product = itemView.findViewById(R.id.product);
            priceTxt = itemView.findViewById(R.id.priceTxt);

            product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    onListItemCliked.onItemCliked(id, position);
                }
            });

            addKeranjangBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAddItemCliked.onItemCliked(id, getAdapterPosition(), view);
                }
            });
        }

    }

    @Override
    public Filter getFilter() {
        return listFilter;
    }

    private Filter listFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Product> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(mModelListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Product item : mModelListFull) {
                    if (item.getNama().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mModelList.clear();
            mModelList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    private String rupiahFormat(int number) {
        NumberFormat kursIndonesia = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp;
        formatRp = new DecimalFormatSymbols();
        formatRp.setCurrencySymbol("Rp ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');
        ((DecimalFormat) kursIndonesia).setDecimalFormatSymbols(formatRp);
        String harga = kursIndonesia.format(number);
        return harga.replace(",00", " ");
    }

    public static String getTimeDate(Date timestamp) {
        try {
            //Date netDate = (timestamp);
            SimpleDateFormat sfd = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sfd.format(timestamp);
        } catch (Exception e) {
            return "date";
        }
    }

    public interface OnListItemCliked {
        void onItemCliked(String id, int position);
    }

    public interface OnAddItemCliked {
        void onItemCliked(String id, int position, View view);
    }
}
