package com.app.wor;

import static android.os.Build.VERSION.SDK_INT;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.wor.databinding.ActivityMainBinding;
import com.app.wor.models.EntryModelClass;
import com.app.wor.models.EntryModelClass3;
import com.app.wor.models.ProductModelClass;
import com.app.wor.models.StoreModelClass;
import com.app.wor.models.UsersModelClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 104;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int PICK_IMAGE = 102;
    ActivityMainBinding binding;
    StoreModelClass model;
    String question1, question2;
    List<String> selectList;
    List<ProductModelClass> productsList;
    ProductsListAdapter adapter1;
    AlertDialog alertDialog;
    private Uri ImageUri;
    StorageReference mStorageRef;
    private StorageTask mUploadTask;
    List<String> urlListQ4;
    List<String> urlListQ5;

    List<String> listQ4;
    List<String> listQ5;
    boolean flag = false, first = false, typeFlag = false;
    UsersModelClass userModel;
    DatabaseReference databaseReference;
    int pos = 0;
    SharedPreferences sharedPreferences, sharedPref;
    SharedPreferences.Editor editor,editor2;
    DatabaseHelper db;
    boolean layout1=false,layout2=false,layout3=false,layout4=false,layout5=false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main,null);

        databaseReference = FirebaseDatabase.getInstance().getReference("Entries");
        databaseReference.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference("Pictures/");
        selectList = new ArrayList<>();
        productsList = new ArrayList<>();
        urlListQ4 = new ArrayList<>();
        urlListQ5 = new ArrayList<>();
        listQ4 = new ArrayList<>();
        listQ5 = new ArrayList<>();
        db = new DatabaseHelper(MainActivity.this);

        sharedPreferences = getSharedPreferences("LoginData",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        sharedPref = getSharedPreferences("LoginState",MODE_PRIVATE);
        editor2 = sharedPref.edit();

        editor2.putBoolean("login",true);
        editor2.apply();

        String json = sharedPreferences.getString("json","");
        Gson gson = new Gson();
        if(!json.isEmpty()){
            Type type = new TypeToken<UsersModelClass>(){

            }.getType();
            userModel = gson.fromJson(json,type);
            typeFlag = true;

            Toast.makeText(this, "user: "+userModel.getUsername(), Toast.LENGTH_SHORT).show();
        }else {
            typeFlag = false;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,SplashActivity.combineList);

        binding.spnStores.setAdapter(adapter);

        binding.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String westCode = binding.edtSearch.getText().toString().trim();
                if(westCode.isEmpty()){
                    binding.edtSearch.setError("Required");
                    binding.edtSearch.requestFocus();
                    return;
                }
                if(SplashActivity.storesList.size()>0){
                    for(StoreModelClass modelClass : SplashActivity.storesList){
                        if(westCode.equals(modelClass.getWestCode())){
                            model = modelClass;
                            binding.tvWestCode.setText("West Code: "+model.getWestCode());
                            binding.tvCustId.setText("Customer id: "+model.getCustomerId());
                            binding.tvStreet.setText("Street: "+model.getStreet());
                            binding.tvCity.setText("City: "+model.getCity());
                            binding.tvChain.setText("Chain: "+model.getChain());

                            pos++;
                            first = true;
                            break;
                        }
                    }
                    binding.spnStores.setSelection(pos);
                }else {
                    Toast.makeText(MainActivity.this, "No stores added yet!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.spnStores.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = SplashActivity.combineList.get(position);
                if(!str.equals("Select store")){
                    model = SplashActivity.storesList.get(position-1);
                    binding.tvWestCode.setText("West Code: "+model.getWestCode());
                    binding.tvCustId.setText("Customer id: "+model.getCustomerId());
                    binding.tvStreet.setText("Street: "+model.getStreet());
                    binding.tvCity.setText("City: "+model.getCity());
                    binding.tvChain.setText("Chain: "+model.getChain());

                    first = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}});

        binding.btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!first){
                    Toast.makeText(MainActivity.this, "No store selected!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                layout1 = true;
                binding.layout1.setVisibility(View.GONE);
                binding.layout2.setVisibility(View.VISIBLE);
            }
        });
        binding.btnPrev1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layout1.setVisibility(View.VISIBLE);
                binding.layout2.setVisibility(View.GONE);
            }
        });
        binding.btnNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.rdb1.isChecked()){
                    question1 = binding.rdb1.getText().toString();
                }else if(binding.rdb2.isChecked()){
                    question1 = binding.rdb2.getText().toString();
                }else {
                    Toast.makeText(MainActivity.this, "Please choose your answer!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(binding.rdb3.isChecked()){
                    question2 = binding.rdb3.getText().toString();
                }else if(binding.rdb4.isChecked()){
                    question2 = binding.rdb4.getText().toString();
                }else {
                    Toast.makeText(MainActivity.this, "Please choose your answer!", Toast.LENGTH_SHORT).show();
                    return;
                }

                layout1 = false;
                layout2 = true;
                binding.layout2.setVisibility(View.GONE);
                binding.layout3.setVisibility(View.VISIBLE);

                binding.recyclerProducts.setHasFixedSize(true);
                binding.recyclerProducts.setNestedScrollingEnabled(false);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                binding.recyclerProducts.setLayoutManager(linearLayoutManager);

                binding.recyclerProducts.setAdapter(null);
                adapter1 = new ProductsListAdapter(MainActivity.this,SplashActivity.productsList);
                binding.recyclerProducts.setAdapter(adapter1);
            }
        });
        binding.btnPrev2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layout2.setVisibility(View.VISIBLE);
                binding.layout3.setVisibility(View.GONE);
            }
        });
        binding.edtSearch2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }});

        binding.btnNext3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(selectList.isEmpty()){
//                    Toast.makeText(MainActivity.this, "No product selected!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                productsList.clear();
                for(ProductModelClass modelClass : SplashActivity.productsList){
                    if(selectList.contains(modelClass.getId())){
                        productsList.add(modelClass);
                    }
                }
                Toast.makeText(MainActivity.this, selectList.size()+" products selected", Toast.LENGTH_SHORT).show();
                binding.layout4.setVisibility(View.VISIBLE);
                binding.layout3.setVisibility(View.GONE);

                layout2 = false;
                layout3 = true;

                binding.recyclerQ4.setHasFixedSize(true);
                binding.recyclerQ4.setNestedScrollingEnabled(false);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this,2);
                binding.recyclerQ4.setLayoutManager(gridLayoutManager);
            }
        });
        binding.btnPrev3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layout4.setVisibility(View.GONE);
                binding.layout3.setVisibility(View.VISIBLE);
            }
        });
        binding.imgSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence arr[] = new CharSequence[]{
                        "TAKE A PHOTO",
                        "CHOOSE FROM LIBRARY",
                        "CANCEL"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select");
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                                    return;
                                }
                            }

//                            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                            startActivityForResult(intent, CAMERA_REQUEST_CODE);
                            captureCameraImage();
                        }
                        if(i==1) {
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
//                                    return;
//                                }
//                            }
                            if(!checkPermission()){
                                requestPermission();
                            }else {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                startActivityForResult(intent, PICK_IMAGE);
                            }

                        }
                        if(i == 2){
                            alertDialog.dismiss();
                        }
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        binding.btnNext4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(isConnectionAvailable(MainActivity.this)){
//                    if(urlListQ4.isEmpty()){
//                        Toast.makeText(MainActivity.this, "No photos added!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }else {
//                    if(listQ4.isEmpty()){
//                        Toast.makeText(MainActivity.this, "No photos added!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
                binding.layout5.setVisibility(View.VISIBLE);
                binding.layout4.setVisibility(View.GONE);

                layout3 = false;
                layout4 = true;

                binding.recyclerQ5.setHasFixedSize(true);
                binding.recyclerQ5.setNestedScrollingEnabled(false);
                GridLayoutManager gridLayoutManager2 = new GridLayoutManager(MainActivity.this,2);
                binding.recyclerQ5.setLayoutManager(gridLayoutManager2);
            }
        });
        binding.btnPrev4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layout5.setVisibility(View.GONE);
                binding.layout4.setVisibility(View.VISIBLE);
            }
        });

        binding.imgSelect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence arr[] = new CharSequence[]{
                        "TAKE A PHOTO",
                        "CHOOSE FROM LIBRARY",
                        "CANCEL"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select");
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                                    return;
                                }
                            }
                            flag = true;
//                            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                            startActivityForResult(intent, CAMERA_REQUEST_CODE);
                            captureCameraImage();
                        }
                        if(i==1) {

//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
//                                    return;
//                                }
//                            }
                            if(!checkPermission()){
                                requestPermission();
                            }else {
                                flag = true;
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                startActivityForResult(intent, PICK_IMAGE);
                            }
                        }
                        if(i == 2){
                            alertDialog.dismiss();
                        }
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
            }
        });
        binding.btnNext5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(isConnectionAvailable(MainActivity.this)){
//                    if(urlListQ5.isEmpty()){
//                        Toast.makeText(MainActivity.this, "No photo added!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }else {
//                    if(listQ5.isEmpty()){
//                        Toast.makeText(MainActivity.this, "No photos added!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
                binding.layout5.setVisibility(View.GONE);
                binding.layout6.setVisibility(View.VISIBLE);

                layout4 = false;
                layout5 = true;

                binding.tvWestCode2.setText("West Code: "+model.getWestCode());
                binding.tvCustId2.setText("Customer id: "+model.getCustomerId());
                binding.tvStreet2.setText("Street: "+model.getStreet());
                binding.tvCity2.setText("City: "+model.getCity());
                binding.tvChain2.setText("Chain: "+model.getChain());
                binding.tvQ1.setText(question1);
                binding.tvQ2.setText(question2);
                if(productsList.size()>0){
                    String str="";
                    for(ProductModelClass modelClass : productsList){
                        str+=modelClass.getBrandName()+", ";
                    }
                    int len = str.length();
                    str = str.substring(0,len-2);
                    binding.tvProdList.setText(str);
                }else {
                    binding.tvProdList.setText("No products selected");
                }
                if(isConnectionAvailable(MainActivity.this)){
                    binding.tvQ4Size.setText(urlListQ4.size()+" PHOTOS ARE SELECTED TO UPLOAD");
                    binding.tvQ5Size.setText(urlListQ5.size()+" PHOTOS ARE SELECTED TO UPLOAD");
                }else {
                    binding.tvQ4Size.setText(listQ4.size()+" PHOTOS ARE SELECTED TO UPLOAD");
                    binding.tvQ5Size.setText(listQ5.size()+" PHOTOS ARE SELECTED TO UPLOAD");
                }
            }
        });
        binding.btnPrev5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layout6.setVisibility(View.GONE);
                binding.layout5.setVisibility(View.VISIBLE);
            }
        });
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnectionAvailable(MainActivity.this)){
                    String cDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                    String id = databaseReference.push().getKey();
                    EntryModelClass entryModel = new EntryModelClass(id,cDate,userModel.getId(),userModel.getUsername(),model,
                            question1,question2,productsList,urlListQ4,urlListQ5);
                    databaseReference.child(id).setValue(entryModel);
                    Toast.makeText(MainActivity.this, "New entry added successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }else {
                    String cDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                    String id = databaseReference.push().getKey();
                    Gson gson = new Gson();
                    String store = gson.toJson(model);
                    String products = gson.toJson(productsList);
                    String q4List = gson.toJson(listQ4);
                    String q5List = gson.toJson(listQ5);

                    if(db.addEntry(id,cDate,userModel.getId(),userModel.getUsername(),store,question1,question2,products,q4List,q5List)){
                        Toast.makeText(MainActivity.this, "New entry added successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }else {
                        Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        binding.imgSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SyncDataActivity.class);
                startActivity(intent);
            }
        });
        binding.imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clear();
                editor.apply();
                editor2.clear();
                editor2.apply();

                Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
//        if (SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 109);
//                return;
//            }
//        }
    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        List<ProductModelClass> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (ProductModelClass s : SplashActivity.productsList) {
            //if the existing elements contains the search input
            if (s.getBrandName().toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filterdNames.add(s);
            }
        }

        try {
            adapter1.filterList(filterdNames);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGE);
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 108 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

//                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, CAMERA_REQUEST_CODE);
                captureCameraImage();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if(isConnectionAvailable(MainActivity.this)){
                Bitmap photo = (Bitmap) data.getExtras().get("data");
//                ImageUri = getImageUri(photo);
                binding.recyclerQ4.setAdapter(null);
                uploadBitmap(photo);
//                uploadImages(ImageUri);
            }else {
                if (data.getData() != null) {
                    ImageUri = data.getData();
                    String imagePath = copyImageToAppFolder(ImageUri);
                    if(!flag){
                        listQ4.add(imagePath);
                        abc2();
                    }else {
                        listQ5.add(imagePath);
                        abc2();
                    }
                }
                else {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
//                        ImageUri = getImageUri(imageBitmap);
//                        String imagePath = copyImageToAppFolder(ImageUri);
                        String imagePath = copyImageToAppFolder2(imageBitmap);

                        if(!flag){
                            listQ4.add(imagePath);
                            abc2();
                        }else {
                            listQ5.add(imagePath);
                            abc2();
                        }
                    }
                }
            }
        }

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            if(isConnectionAvailable(MainActivity.this)){
                if (data.getClipData() != null) {

                    int countClipData = data.getClipData().getItemCount();
                    int currentImageSlect = 0;

                    while (currentImageSlect < countClipData) {
                        ImageUri = data.getClipData().getItemAt(currentImageSlect).getUri();
                        binding.recyclerQ4.setAdapter(null);
                        uploadImages(ImageUri);
                        currentImageSlect = currentImageSlect + 1;
                    }
                } else {
                    ImageUri = data.getData();
                    binding.recyclerQ4.setAdapter(null);
                    uploadImages(ImageUri);
                }
            } else {
                if (data.getClipData() != null) {
                    int countClipData = data.getClipData().getItemCount();
                    int currentImageSlect = 0;
                    while (currentImageSlect < countClipData){
                        if(!flag){
                            ImageUri = data.getClipData().getItemAt(currentImageSlect).getUri();
                            String imagePath = copyImageToAppFolder(ImageUri);
                            listQ4.add(imagePath);
                            currentImageSlect = currentImageSlect + 1;
                        }else{
                            ImageUri = data.getClipData().getItemAt(currentImageSlect).getUri();
                            String imagePath = copyImageToAppFolder(ImageUri);
                            listQ5.add(imagePath);
                            currentImageSlect = currentImageSlect + 1;
                        }
                    }
                    abc2();
                } else {
                    if(!flag){
                        ImageUri = data.getData();
                        String imagePath = copyImageToAppFolder(ImageUri);
                        listQ4.add(imagePath);
                        abc2();
                    }else{
                        ImageUri = data.getData();
                        String imagePath = copyImageToAppFolder(ImageUri);
                        listQ5.add(imagePath);
                        abc2();
                    }
                }
            }
        }
    }

    ProgressDialog mProgressDialog;
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Uploading photos please wait...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    File photoFile = null;
    public void  captureCameraImage()
    {

        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
        }

// Continue only if the File was successfully created
        if (photoFile != null) {
            // Create an Intent to capture a photo using the camera API
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Set the output file URI to the file created earlier
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.app.wor.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            // Start the camera activity
            cameraLauncher.launch(takePictureIntent);
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name using a timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File cacheDir = getCacheDir();

        // Create the file object
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                cacheDir        /* directory */
        );

        // Return the file object
        return imageFile;
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Get the high-resolution image from the file

                    Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                    Bitmap photo=getBimapFromPath(bitmap,photoFile.getAbsolutePath());
                    if(photo==null)
                        return;
                    if(isConnectionAvailable(MainActivity.this)){
//                ImageUri = getImageUri(photo);
                        binding.recyclerQ4.setAdapter(null);
                        uploadBitmap(photo);
//                uploadImages(ImageUri);
                    }else {
                        String imagePath = copyImageToAppFolder2(photo);

                        if(!flag){
                            listQ4.add(imagePath);
                            abc2();
                        }else {
                            listQ5.add(imagePath);
                            abc2();
                        }
                    }
                }
            });

    private Bitmap getBimapFromPath(Bitmap bitmap,String absolutePath) {
        // Create an ExifInterface instance to read the metadata

        try {
            ExifInterface exif = new ExifInterface(absolutePath);

// Read the orientation information from the metadata
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

// Rotate the bitmap according to the orientation information
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch (Exception e)
        {
            return null;
        }



    }

    // Launch the camera intent
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private String copyImageToAppFolder(Uri imageUri) {
        String imagePath = null;
        try {
            // Get the input stream of the image
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            // Create a file in the app folder with a unique name
            File appFolder = getExternalFilesDir(null);
            String filename = "image_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(appFolder, filename);
            imagePath = outputFile.getAbsolutePath();

            // Copy the image to the file in the app folder
            OutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[2048];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    private  String getExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver() ;
        MimeTypeMap mime = MimeTypeMap.getSingleton() ;
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImages(Uri mImageUri) {
        showProgressDialog();
        final StorageReference fileref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(mImageUri));
        mUploadTask = fileref.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            if(!flag){
                                urlListQ4.add(uri.toString());
                                hideProgressDialog();
                                abc();
                            }else {
                                urlListQ5.add(uri.toString());
                                hideProgressDialog();
                                abc();
                            }
                        } catch (Exception ex ){
                            Toast.makeText(getApplicationContext()  , "err" + ex.toString() , Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                hideProgressDialog();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            }
        });
    }

    private void uploadBitmap(Bitmap bitmap) {
        showProgressDialog();
        byte[] data;
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, boas);
        data = boas.toByteArray();

        final StorageReference fileref = mStorageRef.child(System.currentTimeMillis()+"");
        UploadTask uploadTask = fileref.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(!flag){
                            urlListQ4.add(uri.toString());
                            hideProgressDialog();
                            abc();
                        }else {
                            urlListQ5.add(uri.toString());
                            hideProgressDialog();
                            abc();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                hideProgressDialog();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            }
        });
    }

    public void abc(){
        if(!flag){
            ItemListAdapter imageAdapter = new ItemListAdapter(MainActivity.this, urlListQ4);
            binding.recyclerQ4.setAdapter(imageAdapter);
            binding.tvPhotos1.setText(urlListQ4.size()+" Photos");
        }else {
            ItemListAdapter2 imageAdapter = new ItemListAdapter2(MainActivity.this, urlListQ5);
            binding.recyclerQ5.setAdapter(imageAdapter);
            binding.tvPhotos2.setText(urlListQ5.size()+" Photos");
            flag = false;
        }
    }

    public void abc2(){
        if(!flag){
            ItemListAdapter3 imageAdapter = new ItemListAdapter3(MainActivity.this, listQ4);
            binding.recyclerQ4.setAdapter(imageAdapter);
            binding.tvPhotos1.setText(listQ4.size()+" Photos");
        }else {
            ItemListAdapter4 imageAdapter = new ItemListAdapter4(MainActivity.this, listQ5);
            binding.recyclerQ5.setAdapter(imageAdapter);
            binding.tvPhotos2.setText(listQ5.size()+" Photos");
            flag = false;
        }
    }

    public class ProductsListAdapter extends RecyclerView.Adapter<ProductsListAdapter.ImageViewHolder> {
        private Context mcontext;
        private List<ProductModelClass> muploadList;

        public ProductsListAdapter(Context context, List<ProductModelClass> uploadList) {
            mcontext = context;
            muploadList = uploadList;
        }

        @Override
        public ProductsListAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.products_list_layout, parent, false);
            return (new ProductsListAdapter.ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ProductsListAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final ProductModelClass model = muploadList.get(position);

            holder.tvProduct.setText(model.getBrandName());
            holder.tvCategory.setText(model.getCategory());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectList.contains(model.getId())){
                        selectList.remove(model.getId());
                        holder.cbSelect.setChecked(false);
                    }else {
                        selectList.add(model.getId());
                        holder.cbSelect.setChecked(true);
                    }
                }
            });
            holder.cbSelect.setChecked(false);
            if(selectList.contains(model.getId())){
                holder.cbSelect.setChecked(true);
            }
//            holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @RequiresApi(api = Build.VERSION_CODES.N)
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if(isChecked){
//                        if(!selectList.contains(model.getId())){
//                            selectList.add(model.getId());
//                            Toast.makeText(mcontext, "size: "+selectList.size(), Toast.LENGTH_SHORT).show();
//                        }
//                    }else {
////                        selectList.removeIf(prod -> prod.getId().equals(model.getId()));
////                        Toast.makeText(mcontext, "size: "+selectList.size(), Toast.LENGTH_SHORT).show();
//                        selectList.remove(model.getId());
//                        Toast.makeText(mcontext, "size: "+selectList.size(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public TextView tvProduct;
            public TextView tvCategory;
            public CheckBox cbSelect;

            public ImageViewHolder(View itemView) {
                super(itemView);

                tvProduct = itemView.findViewById(R.id.tvProduct);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                cbSelect = itemView.findViewById(R.id.cbSelect);

            }
        }
        @SuppressLint("NotifyDataSetChanged")
        public void filterList(List<ProductModelClass> searchList) {
            this.muploadList = searchList;
            notifyDataSetChanged();
        }
    }

    public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ImageViewHolder> {
        private Context mcontext;
        private List<String> muploadList;

        public ItemListAdapter(Context context, List<String> uploadList) {
            mcontext = context;
            muploadList = uploadList;

        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.image_layout, parent, false);
            return (new ImageViewHolder(v));
        }

        int dpToPix(int dp) {
            return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final String uploadCurrent = muploadList.get(position);

            Picasso.with(mcontext).load(uploadCurrent).resize(dpToPix(105), dpToPix(105)).placeholder(R.drawable.holder).into(holder.itemPic);

            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseStorage mStorage;
                    mStorage = FirebaseStorage.getInstance();
                    StorageReference imgRef = mStorage.getReferenceFromUrl(uploadCurrent);
                    imgRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onSuccess(Void aVoid) {
                            urlListQ4.remove(position);
                            muploadList.remove(uploadCurrent);
                            notifyDataSetChanged();

                            binding.tvPhotos1.setText(urlListQ4.size()+" Photos");
                        }
                    });
                }
            });

        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageView itemPic;
            public ImageView imgDelete;

            public ImageViewHolder(View itemView) {
                super(itemView);

                itemPic = itemView.findViewById(R.id.itemPic);
                imgDelete = itemView.findViewById(R.id.imgDelete);

            }
        }
    }

    public class ItemListAdapter2 extends RecyclerView.Adapter<ItemListAdapter2.ImageViewHolder> {
        private Context mcontext;
        private List<String> muploadList;

        public ItemListAdapter2(Context context, List<String> uploadList) {
            mcontext = context;
            muploadList = uploadList;

        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.image_layout, parent, false);
            return (new ImageViewHolder(v));
        }

        int dpToPix(int dp) {
            return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final String uploadCurrent = muploadList.get(position);

            Picasso.with(mcontext).load(uploadCurrent).resize(dpToPix(105), dpToPix(105)).placeholder(R.drawable.holder).into(holder.itemPic);

            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseStorage mStorage;
                    mStorage = FirebaseStorage.getInstance();
                    StorageReference imgRef = mStorage.getReferenceFromUrl(uploadCurrent);
                    imgRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onSuccess(Void aVoid) {
                            urlListQ5.remove(position);
                            muploadList.remove(uploadCurrent);
                            notifyDataSetChanged();

                            binding.tvPhotos2.setText(urlListQ5.size()+" Photos");
                        }
                    });
                }
            });

        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageView itemPic;
            public ImageView imgDelete;

            public ImageViewHolder(View itemView) {
                super(itemView);

                itemPic = itemView.findViewById(R.id.itemPic);
                imgDelete = itemView.findViewById(R.id.imgDelete);

            }
        }
    }

    public class ItemListAdapter3 extends RecyclerView.Adapter<ItemListAdapter3.ImageViewHolder> {
        private Context mcontext;
        private List<String> muploadList;

        public ItemListAdapter3(Context context, List<String> uploadList) {
            mcontext = context;
            muploadList = uploadList;

        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.image_layout, parent, false);
            return (new ImageViewHolder(v));
        }

        int dpToPix(int dp) {
            return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final String uploadCurrent = muploadList.get(position);

//            Picasso.with(mcontext).load(uploadCurrent).resize(dpToPix(105), dpToPix(105)).placeholder(R.drawable.holder).into(holder.itemPic);
            holder.itemPic.setImageURI(Uri.parse(uploadCurrent));

            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listQ4.remove(position);
                    muploadList.remove(uploadCurrent);
                    notifyDataSetChanged();

                    binding.tvPhotos1.setText(listQ4.size()+" Photos");
                }
            });

        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageView itemPic;
            public ImageView imgDelete;

            public ImageViewHolder(View itemView) {
                super(itemView);

                itemPic = itemView.findViewById(R.id.itemPic);
                imgDelete = itemView.findViewById(R.id.imgDelete);

            }
        }

//    public void deletItems(List<Upload> user_selection) {
//        for(Upload upload : user_selection){
//            Toast.makeText(mcontext, ""+upload.getItemName(), Toast.LENGTH_SHORT).show();
//        }
//        notifyDataSetChanged();
//    }

    }

    public class ItemListAdapter4 extends RecyclerView.Adapter<ItemListAdapter4.ImageViewHolder> {
        private Context mcontext;
        private List<String> muploadList;

        public ItemListAdapter4(Context context, List<String> uploadList) {
            mcontext = context;
            muploadList = uploadList;

        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.image_layout, parent, false);
            return (new ImageViewHolder(v));
        }

        int dpToPix(int dp) {
            return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final String uploadCurrent = muploadList.get(position);

//            Picasso.with(mcontext).load(uploadCurrent).resize(dpToPix(105), dpToPix(105)).placeholder(R.drawable.holder).into(holder.itemPic);
            holder.itemPic.setImageURI(Uri.parse(uploadCurrent));

            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listQ5.remove(position);
                    muploadList.remove(uploadCurrent);
                    notifyDataSetChanged();

                    binding.tvPhotos1.setText(listQ5.size()+" Photos");
                }
            });

        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageView itemPic;
            public ImageView imgDelete;

            public ImageViewHolder(View itemView) {
                super(itemView);

                itemPic = itemView.findViewById(R.id.itemPic);
                imgDelete = itemView.findViewById(R.id.imgDelete);

            }
        }

//    public void deletItems(List<Upload> user_selection) {
//        for(Upload upload : user_selection){
//            Toast.makeText(mcontext, ""+upload.getItemName(), Toast.LENGTH_SHORT).show();
//        }
//        notifyDataSetChanged();
//    }

    }

    @Override
    public void onBackPressed() {
        if(layout1){
            binding.layout2.setVisibility(View.GONE);
            binding.layout1.setVisibility(View.VISIBLE);
            layout1 = false;
        }else if(layout2){
            binding.layout3.setVisibility(View.GONE);
            binding.layout2.setVisibility(View.VISIBLE);
            layout2 = false;
            layout1 = true;
        }else if(layout3){
            binding.layout4.setVisibility(View.GONE);
            binding.layout3.setVisibility(View.VISIBLE);
            layout3 = false;
            layout2 = true;
        }else if(layout4){
            binding.layout5.setVisibility(View.GONE);
            binding.layout4.setVisibility(View.VISIBLE);
            layout4 = false;
            layout3 = true;
        }else if(layout5){
            binding.layout6.setVisibility(View.GONE);
            binding.layout5.setVisibility(View.VISIBLE);
            layout5 = false;
            layout4 = true;
        }else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }

    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    private Uri getImageUri(Bitmap imageBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, System.currentTimeMillis()+"", null);
        return Uri.parse(path);
    }

    private String copyImageToAppFolder2(Bitmap bitmap) {
        String imagePath = null;
        try {
            String fileName = "image_" + System.currentTimeMillis() + ".png";
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            File imageFile = new File(getFilesDir(), fileName);
            imagePath = imageFile.getAbsolutePath();

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

}