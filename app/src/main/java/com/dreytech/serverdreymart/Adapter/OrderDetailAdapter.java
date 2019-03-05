package com.dreytech.serverdreymart.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dreytech.serverdreymart.Model.Cart;
import com.dreytech.serverdreymart.R;
import com.dreytech.serverdreymart.Utils.Common;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    Context context;
    List<Cart> itemList;

    public OrderDetailAdapter(Context context) {
        this.context = context;
        this.itemList = new Gson().fromJson(Common.currentOrder.getOrderDetail(), new TypeToken<List<Cart>>(){}.getType());

    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_detail_layout,viewGroup,false);
        return new OrderDetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder orderDetailViewHolder, int i) {

        orderDetailViewHolder.txt_barang_amount.setText(""+itemList.get(i).getAmount());
        orderDetailViewHolder.txt_barang_name.setText(new StringBuilder(itemList.get(i).getName()));
        orderDetailViewHolder.txt_size.setText(itemList.get(i).getSize() == 0?"Size M":"Size L");
        orderDetailViewHolder.txt_sugar_ice.setText(new StringBuilder("Sugar: ").append(itemList.get(i).getSugar())
        .append(", Ice: ").append(itemList.get(i).getIce()));

        if (itemList.get(i).getToppingExtras() != null &&
                !itemList.get(i).getToppingExtras().isEmpty()) {

            String topping_format = itemList.get(i).getToppingExtras().replaceAll("\\n", ",");
            topping_format = topping_format.substring(0, topping_format.length() - 1);
            orderDetailViewHolder.txt_topping.setText(topping_format);
        }
        else
        {
            orderDetailViewHolder.txt_topping.setText("None");
        }

        Picasso.with(context).load(itemList.get(i).getLink()).into(orderDetailViewHolder.img_order_item);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class OrderDetailViewHolder extends RecyclerView.ViewHolder{

        ImageView img_order_item;
        TextView txt_barang_name, txt_barang_amount, txt_sugar_ice, txt_size, txt_topping;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);

            img_order_item = (ImageView)itemView.findViewById(R.id.img_order_item);

            txt_barang_name = (TextView)itemView.findViewById(R.id.txt_barang_name);
            txt_barang_amount= (TextView)itemView.findViewById(R.id.txt_barang_amount);
            txt_sugar_ice = (TextView)itemView.findViewById(R.id.txt_sugar_ice);
            txt_size = (TextView)itemView.findViewById(R.id.txt_size);
            txt_topping = (TextView)itemView.findViewById(R.id.txt_topping);

        }
    }
}
