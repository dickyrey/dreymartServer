package com.dreytech.serverdreymart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.dreytech.serverdreymart.Adapter.OrderDetailAdapter;
import com.dreytech.serverdreymart.Model.DataMessage;
import com.dreytech.serverdreymart.Model.MyResponse;
import com.dreytech.serverdreymart.Model.Order;
import com.dreytech.serverdreymart.Model.Token;
import com.dreytech.serverdreymart.Retrofit.IDreyMarketAPI;
import com.dreytech.serverdreymart.Services.IFCMServices;
import com.dreytech.serverdreymart.Utils.Common;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewOrderDetail extends AppCompatActivity {

    TextView txt_order_id, txt_order_price, txt_order_comment, txt_order_address;
    Spinner spinner_order_status;
    RecyclerView recycler_order_detail;

    String[] spinner_source = new String[]{
            "Dibatalkan", // Index 0
            "Ditempatkan", // 1
            "Diproses", // 2
            "Dalam Perjalanan", //3
            "Telah Sampai" //4

    };
    Toolbar toolbar;

    IDreyMarketAPI mService;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getAPI();


        txt_order_id = (TextView)findViewById(R.id.txt_order_id);
        txt_order_price = (TextView)findViewById(R.id.txt_order_price);
        txt_order_comment = (TextView)findViewById(R.id.txt_order_comment);
        txt_order_address = (TextView)findViewById(R.id.txt_order_address);

        spinner_order_status = (Spinner)findViewById(R.id.spinner_order_status);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                spinner_source);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_order_status.setAdapter(spinnerArrayAdapter);


        recycler_order_detail = (RecyclerView)findViewById(R.id.recycler_order_detail);
        recycler_order_detail.setLayoutManager(new LinearLayoutManager(this));
        recycler_order_detail.setAdapter(new OrderDetailAdapter(this));

        //set Data
        txt_order_id.setText(new StringBuilder("#")
        .append(Common.currentOrder.getOrderId()));
        txt_order_price.setText(new StringBuilder("Rp.").append(Common.currentOrder.getOrderPrice()));
        txt_order_address.setText(Common.currentOrder.getOrderAddress());
        txt_order_comment.setText(Common.currentOrder.getOrderComment());

        setSpinnerSelectedBaseOnOrderStatus();

    }

    private void setSpinnerSelectedBaseOnOrderStatus() {
        switch (Common.currentOrder.getOrderStatus())
        {
            case -1:
                spinner_order_status.setSelection(0); // Dibatalkan
                break;
            case 0:
                spinner_order_status.setSelection(1); // Ditempatkan
                break;
            case 1:
                spinner_order_status.setSelection(2); // Diproses
                break;
            case 2:
                spinner_order_status.setSelection(3); // Dalam Perjalanan
                break;
            case 3:
                spinner_order_status.setSelection(4); // Telah Sampai
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_detail,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save_order_detail)
            saveUpdateOrder();
        return true;
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void saveUpdateOrder() {
        final int order_status = spinner_order_status.getSelectedItemPosition()-1;
        compositeDisposable.add(mService.updateOrderStatus(Common.currentOrder.getUserPhone(),
                Common.currentOrder.getOrderId(),
                order_status)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
            @Override
                public void accept(String s) throws Exception {

                sendOrderUpdateNotification(Common.currentOrder, order_status);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.d("ERROR",""+throwable.getMessage());
            }
        }));
    }

    private void sendOrderUpdateNotification(final Order currentOrder, final int order_status) {

        mService.getToken(currentOrder.getUserPhone(),"0")
                .enqueue(new Callback<Token>() {
                    @Override
                    public void onResponse(Call<Token> call, Response<Token> response) {

                        Token userToken = response.body();


                        Map<String,String> dataSend = new HashMap<>();
                        dataSend.put("title", "Your order has been update");
                        dataSend.put("message", "Order #"+currentOrder.getOrderId()+" telah diperbarui menjadi "+Common.convertCodeToStatus(order_status));

                        DataMessage dataMessage = new DataMessage();
                        dataMessage.to = userToken.getToken();
                        dataMessage.setData(dataSend);


                        IFCMServices mFCMService = Common.getFCMAPI();
                        mFCMService.sendNotification(dataMessage)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                        if (response.code() == 200) {
                                            if (response.body().success == 1) {
                                                Toast.makeText(ViewOrderDetail.this, "Pesanan telah diperbarui!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(ViewOrderDetail.this, "Gagal mengirim notif", Toast.LENGTH_SHORT).show();
                                            }


                                        }
                                    }


                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                        Toast.makeText(ViewOrderDetail.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }

                    @Override
                    public void onFailure(Call<Token> call, Throwable t) {
                        Toast.makeText(ViewOrderDetail.this, "failer"+t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
