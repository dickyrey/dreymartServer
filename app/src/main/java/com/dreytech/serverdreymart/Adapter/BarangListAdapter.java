package com.dreytech.serverdreymart.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dreytech.serverdreymart.Adapter.ViewHolder.BarangListViewHolder;
import com.dreytech.serverdreymart.Interface.IItemClickListner;
import com.dreytech.serverdreymart.Model.Barang;
import com.dreytech.serverdreymart.R;
import com.dreytech.serverdreymart.UpdateProductActivity;
import com.dreytech.serverdreymart.Utils.Common;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BarangListAdapter extends RecyclerView.Adapter<BarangListViewHolder> {

    Context context;
    List<Barang> barangList;

    public BarangListAdapter(Context context, List<Barang> barangList) {
        this.context = context;
        this.barangList = barangList;
    }

    @NonNull
    @Override
    public BarangListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.barang_item_layout,parent,false);

        return new BarangListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BarangListViewHolder holder, final int position) {
        Picasso.with(context).load(barangList.get(position).Link).into(holder.img_product);
        holder.txt_price.setText(new StringBuilder("Rp.").append(barangList.get(position).Price).toString());
        holder.txt_barang_name.setText(barangList.get(position).Name);

        holder.setItemClickListnener(new IItemClickListner() {
            @Override
            public void onClick(View view, boolean isLongClick) {
                Common.currentBarang = barangList.get(position);
                context.startActivity(new Intent(context, UpdateProductActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return barangList.size();
    }
}
