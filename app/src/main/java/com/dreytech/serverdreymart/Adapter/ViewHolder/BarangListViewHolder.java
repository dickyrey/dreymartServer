package com.dreytech.serverdreymart.Adapter.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dreytech.serverdreymart.Interface.IItemClickListner;
import com.dreytech.serverdreymart.R;

public class BarangListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView img_product;
    public TextView txt_barang_name, txt_price;

    IItemClickListner itemClickListnener;

    public void setItemClickListnener(IItemClickListner itemClickListnener) {
        this.itemClickListnener = itemClickListnener;
    }

    public BarangListViewHolder(@NonNull View itemView) {
        super(itemView);

        img_product = (ImageView)itemView.findViewById(R.id.img_product);
        txt_barang_name = (TextView)itemView.findViewById(R.id.txt_barang_name);
        txt_price = (TextView)itemView.findViewById(R.id.txt_price);

        itemView.setOnClickListener(this
        );
    }

    @Override
    public void onClick(View v) {
        itemClickListnener.onClick(v, false);
    }
}
