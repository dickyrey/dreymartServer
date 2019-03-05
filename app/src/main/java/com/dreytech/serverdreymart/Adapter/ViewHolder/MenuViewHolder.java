package com.dreytech.serverdreymart.Adapter.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dreytech.serverdreymart.Interface.IItemClickListner;
import com.dreytech.serverdreymart.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

    public ImageView img_product;
    public TextView txt_product;

    IItemClickListner itemClickListnener;


    public void setItemClickListnener(IItemClickListner itemClickListnener) {
        this.itemClickListnener = itemClickListnener;
    }

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        img_product = (ImageView)itemView.findViewById(R.id.img_product);
        txt_product = (TextView)itemView.findViewById(R.id.txt_menu_name);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }

    @Override
    public void onClick(View v) {
        itemClickListnener.onClick(v,false);
    }

    @Override
    public boolean onLongClick(View v) {
        itemClickListnener.onClick(v,true);
        return true;
    }
}
