package com.dreytech.serverdreymart;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.dreytech.serverdreymart.Adapter.OrderViewAdapter;
import com.dreytech.serverdreymart.Model.Order;
import com.dreytech.serverdreymart.Retrofit.IDreyMarketAPI;
import com.dreytech.serverdreymart.Utils.Common;

import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ShowOrderActivity extends AppCompatActivity {


    IDreyMarketAPI mService;
    RecyclerView recycler_order;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order);

        mService = Common.getAPI();

        recycler_order = (RecyclerView) findViewById(R.id.recycler_orders);
        recycler_order.setLayoutManager(new LinearLayoutManager(this));
        recycler_order.setHasFixedSize(true);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.order_new) {
                    loadOrder("0");
                } else if (item.getItemId() == R.id.order_cancel) {
                    loadOrder("-1");
                } else if (item.getItemId() == R.id.order_processing) {
                    loadOrder("1");
                } else if (item.getItemId() == R.id.order_shipping) {
                    loadOrder("2");
                } else if (item.getItemId() == R.id.order_shipped) {
                    loadOrder("3");
                }
                return true;
            }
        });

        loadOrder("0");

    }

    private void loadOrder(String code) {
        compositeDisposable.add(mService.getAllOrder(code)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<List<Order>>() {
                        @Override
                        public void accept(List<Order> orders) throws Exception {
                            displayOrder(orders);
                        }
                    })
        );

    }

    private void displayOrder(List<Order> orders) {
        Collections.reverse(orders);
        OrderViewAdapter adapter = new OrderViewAdapter(this,orders);
        recycler_order.setAdapter(adapter);
    }


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrder("0");
    }
}