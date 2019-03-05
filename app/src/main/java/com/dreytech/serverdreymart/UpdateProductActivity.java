package com.dreytech.serverdreymart;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dreytech.serverdreymart.Model.Category;
import com.dreytech.serverdreymart.Retrofit.IDreyMarketAPI;
import com.dreytech.serverdreymart.Utils.Common;
import com.dreytech.serverdreymart.Utils.ProgressRequestBody;
import com.dreytech.serverdreymart.Utils.UploadCallback;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

public class UpdateProductActivity extends AppCompatActivity implements UploadCallback {

    private static final int PICK_FILE_REQUEST = 1111;
    MaterialSpinner spinner_menu;
    ImageView img_browser;
    EditText edt_name, edt_price;
    Button btn_update, btn_delete;

    IDreyMarketAPI mService;

    CompositeDisposable compositeDisposable;

    Uri selected_uri = null;
    String uploaded_img_path="", selected_category="";

    HashMap<String, String > menu_data_for_get_key = new HashMap<>();
    HashMap<String, String > menu_data_for_get_value = new HashMap<>();

    List<String> menu_data = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        if (Common.currentBarang !=null)
        {
            uploaded_img_path = Common.currentBarang.Link;
            selected_category = Common.currentBarang.MenuId;
        }

        mService = Common.getAPI();

        compositeDisposable = new CompositeDisposable();

        selected_category = Common.currentBarang.MenuId;

        btn_delete = (Button)findViewById(R.id.btn_delete);
        btn_update= (Button)findViewById(R.id.btn_update);
        edt_name = (EditText) findViewById(R.id.edt_barang_name);
        edt_price = (EditText) findViewById(R.id.edt_barang_price);
        img_browser = (ImageView) findViewById(R.id.img_browser);

        spinner_menu = (MaterialSpinner)findViewById(R.id.spinner_menu_id);

        setSpinnerMenu();

        img_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),"Pilih gambar"),
                        PICK_FILE_REQUEST);
            }
        });

        spinner_menu.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selected_category = menu_data_for_get_key.get(menu_data.get(position));

            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCategory();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCategory();
            }
        });

        setSpinnerMenu();

        setProductInfo();

    }

    private void setProductInfo() {
        if (Common.currentBarang != null)
        {
            edt_name.setText(Common.currentBarang.Name);
            edt_price.setText(Common.currentBarang.Price);

            Picasso.with(this).load(Common.currentBarang.Link).into(img_browser);

            spinner_menu.setSelectedIndex(menu_data.indexOf(menu_data_for_get_value.get(Common.currentCategory.getID())));
        }
    }

    private void deleteCategory() {
        compositeDisposable.add(mService.deleteProduct(Common.currentBarang.ID)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
                       @Override
                       public void accept(String s) throws Exception {
                           Toast.makeText(UpdateProductActivity.this, s, Toast.LENGTH_SHORT).show();
                           finish();
                       }
                   }, new Consumer<Throwable>() {
                       @Override
                       public void accept(Throwable throwable) throws Exception {
                           Toast.makeText(UpdateProductActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                           finish();
                       }
                   }
        ));
    }

    private void updateCategory() {
        compositeDisposable.add(mService.updateProduct(Common.currentCategory.ID,
                edt_name.getText().toString(),
                uploaded_img_path,
                edt_price.getText().toString(),
                selected_category)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Toast.makeText(UpdateProductActivity.this, s, Toast.LENGTH_SHORT).show();
                finish();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(UpdateProductActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        })
        );
    }

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
                                    Toast.makeText(UpdateProductActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }

    }


    private void setSpinnerMenu() {
        for (Category category : Common.menuList)
        {
            menu_data_for_get_key.put(category.getName(),category.getID());
            menu_data_for_get_value.put(category.getID(),category.getName());

            menu_data.add(category.getName());
        }
        spinner_menu.setItems(menu_data);
    }

    @Override
    public void onProgressUpdate(int pertantage) {

    }
}
