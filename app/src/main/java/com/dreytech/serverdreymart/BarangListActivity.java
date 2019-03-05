package com.dreytech.serverdreymart;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dreytech.serverdreymart.Adapter.BarangListAdapter;
import com.dreytech.serverdreymart.Model.Barang;
import com.dreytech.serverdreymart.Retrofit.IDreyMarketAPI;
import com.dreytech.serverdreymart.Utils.Common;
import com.dreytech.serverdreymart.Utils.ProgressRequestBody;
import com.dreytech.serverdreymart.Utils.UploadCallback;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BarangListActivity extends AppCompatActivity implements UploadCallback {

    private static final int PICK_FILE_REQUEST = 1111;
    IDreyMarketAPI mService;
    RecyclerView recycler_barangs;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    FloatingActionButton btn_add;

    ImageView img_browser;
    EditText edt_barang_name, edt_barang_price;

    Uri selected_uri;
    String uploaded_img_path;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == PICK_FILE_REQUEST)
            {
                if (data != null)
                {
                    selected_uri = data.getData();
                    if (selected_uri != null && !selected_uri.getPath().isEmpty())
                    {
                        img_browser.setImageURI(selected_uri);
                        uploadFileToServer();
                    }
                    else
                        Toast.makeText(this, "Tidak bisa mengunggah ke server ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadFileToServer() {
        if (selected_uri != null)
        {
            File file = FileUtils.getFile(this, selected_uri);

            String file_name = new StringBuilder(UUID.randomUUID().toString())
                    .append(FileUtils.getExtension(file.toString())).toString();

            ProgressRequestBody requestFile = new ProgressRequestBody(file, this);

            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file_name, requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadProductFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //Setelah mengunggah, akan mendapatkan nama dan kembali string dari link gambar
                                    uploaded_img_path = new StringBuilder(Common.BASE_URL)
                                            .append("server/product/product_img/")
                                            .append(response.body().toString())
                                            .toString();
                                    Log.d("IMGPath", uploaded_img_path);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(BarangListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barang_list);

        mService = Common.getAPI();

        btn_add = (FloatingActionButton)findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBarangDialog();
            }
        });

        recycler_barangs = (RecyclerView)findViewById(R.id.recycler_barangs);
        recycler_barangs.setLayoutManager(new GridLayoutManager(this,2));
        recycler_barangs.setHasFixedSize(true);

        loadListBarang(Common.currentCategory.getID());
    }

    private void showAddBarangDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Menambah Barang Baru");

        View view = LayoutInflater.from(this).inflate(R.layout.add_new_product_layout,null);

        edt_barang_name = (EditText)view.findViewById(R.id.edt_barang_name);
        edt_barang_price = (EditText)view.findViewById(R.id.edt_barang_price);
        img_browser = (ImageView)view.findViewById(R.id.img_browser);

        //Event
        img_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),"Pilih gambar"),
                        PICK_FILE_REQUEST);
            }
        });

        //Set View
        builder.setView(view);
        builder.setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                uploaded_img_path="";
                selected_uri=null;
            }
        }).setPositiveButton("TAMBAH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (edt_barang_name.getText().toString().isEmpty())
                {
                    Toast.makeText(BarangListActivity.this, "Isi nama barang", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edt_barang_price.getText().toString().isEmpty())
                {
                    Toast.makeText(BarangListActivity.this, "Isi harga barang", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (uploaded_img_path.isEmpty())
                {
                    Toast.makeText(BarangListActivity.this, "Pilih gambar untuk barang", Toast.LENGTH_SHORT).show();
                    return;
                }

                compositeDisposable.add(mService.addNewProduct(edt_barang_name.getText().toString(),
                        uploaded_img_path,
                        edt_barang_price.getText().toString(),
                        Common.currentCategory.ID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(BarangListActivity.this, s, Toast.LENGTH_SHORT).show();
                        loadListBarang(Common.currentCategory.getID());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(BarangListActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));


            }
        }).show();

    }


    private void loadListBarang(String id) {
        compositeDisposable.add(mService.getBarang(id)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<List<Barang>>() {
            @Override
            public void accept(List<Barang> barangs) throws Exception {
                displayBarangList(barangs);
            }
        }));
    }

    private void displayBarangList(List<Barang> barangs) {
        BarangListAdapter adapter = new BarangListAdapter(this,barangs);
        recycler_barangs.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        loadListBarang(Common.currentCategory.getID());
        super.onResume();
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
    public void onProgressUpdate(int pertantage) {

    }
}
