package com.dreytech.serverdreymart.Utils;

import com.dreytech.serverdreymart.Model.Barang;
import com.dreytech.serverdreymart.Model.Category;
import com.dreytech.serverdreymart.Model.Order;
import com.dreytech.serverdreymart.Retrofit.FCMRetrofitClient;
import com.dreytech.serverdreymart.Retrofit.IDreyMarketAPI;
import com.dreytech.serverdreymart.Retrofit.RetrofitClient;
import com.dreytech.serverdreymart.Services.IFCMServices;

import java.util.ArrayList;
import java.util.List;

public class Common {
    public static final String BASE_URL = "http://192.168.43.10/dreymarket/";
    private static final String FCM_URL = "https://fcm.googleapis.com/";

    public static Barang currentBarang;
    public static Category currentCategory;
    public static Order currentOrder;

    public static List<Category> menuList = new ArrayList<>();

    public static IDreyMarketAPI getAPI()
    {
        return RetrofitClient.getClient(BASE_URL).create(IDreyMarketAPI.class);
    }
    public static IFCMServices getFCMAPI()
    {
        return FCMRetrofitClient.getClient(FCM_URL).create(IFCMServices.class);
    }

    public static String convertCodeToStatus(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "Placed";
            case 1:
                return "Sedang diproses";
            case 2:
                return "Dalam perjalanan";
            case 3:
                return "Sampai ditujuan";
            case -1:
                return "Dibatalkan";
            default:
                return "Pemesanan gagal";
        }
    }
}
