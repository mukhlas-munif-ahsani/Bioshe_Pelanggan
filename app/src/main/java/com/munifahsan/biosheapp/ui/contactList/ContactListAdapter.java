package com.munifahsan.biosheapp.ui.contactList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.munifahsan.biosheapp.R;
import com.munifahsan.biosheapp.domain.User;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.Holder> implements Filterable {

    private List<User> mModelList;
    private List<User> mModelListFull;
    private OnListItemCliked onListItemCliked;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public void setOnListItemCliked(OnListItemCliked onListItemCliked) {
        this.onListItemCliked = onListItemCliked;
    }

    public void setListModels(List<User> mModelList) {
        this.mModelList = mModelList;
        mModelListFull = new ArrayList<>(mModelList);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.id = mModelList.get(position).getId();
        holder.nama.setText(mModelList.get(position).getNama());

        if (mModelList.get(position).getId().equals(auth.getCurrentUser().getUid())) {
            holder.linItem.setVisibility(View.GONE);
        }

        if (mModelList.get(position).getLevel().equals("SALES")) {
            holder.outlet.setText(mModelList.get(position).getLevel());
        }

        if (mModelList.get(position).getLevel().equals("CUSTOMER")) {
            holder.outlet.setText(mModelList.get(position).getNamaOutlet());
        }

        //Log.d("SALES", "ID SALES : "+idSales + " " + " " + "SALES ID :"+salesId);
//
//        if (!mModelList.get(position).getId().equals(mModelList.get(position).getSalesId())){
//            holder.linItem.setVisibility(View.GONE);
//         }

        Picasso.get()
                .load(mModelList.get(position).getPhoto_url())
                .placeholder(R.drawable.black_transparent)
                .into(holder.image);
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

        TextView nama;
        ImageView image;
        TextView outlet;
        LinearLayout card;
        LinearLayout linItem;

        String id;
        String jenis;
        boolean isPremium;

        public Holder(@NonNull View itemView) {
            super(itemView);

            nama = itemView.findViewById(R.id.namaUser);
            image = itemView.findViewById(R.id.circleImageView_photo_addContact);
            card = itemView.findViewById(R.id.layout_addContact);
            outlet = itemView.findViewById(R.id.namaOutlet);
            linItem = itemView.findViewById(R.id.linItem);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    onListItemCliked.onItemCliked(id, position);
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
            List<User> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(mModelListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (User item : mModelListFull) {
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
        return harga.substring(0, harga.length() - 3);
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
}
