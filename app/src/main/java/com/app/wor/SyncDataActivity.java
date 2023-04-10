package com.app.wor;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.app.wor.models.EntryModelClass;
import com.app.wor.models.EntryModelClass2;
import com.app.wor.models.EntryModelClass3;
import com.app.wor.models.ProductModelClass;
import com.app.wor.models.StoreModelClass;
import com.app.wor.models.UsersModelClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyncDataActivity extends BaseActivity {

    DatabaseReference databaseReference, databaseReference2, databaseReference3;
    Button btnUpload, btnClear, btnDownload;
    ImageView img, img2;
    DatabaseHelper db;
    StorageReference mStorageRef ;
    private StorageTask mUploadTask;
    boolean flag1 = false, flag2 = false;
    List<EntryModelClass3> list;
    List<String> urlListQ4;
    List<String> urlListQ5;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_data);

        databaseReference = FirebaseDatabase.getInstance().getReference("Stores");
        databaseReference2 = FirebaseDatabase.getInstance().getReference("Products");
        databaseReference3 = FirebaseDatabase.getInstance().getReference("Entries");
        mStorageRef = FirebaseStorage.getInstance().getReference("Pictures/");
        db = new DatabaseHelper(this);
        list = new ArrayList<>();
        urlListQ4 = new ArrayList<>();
        urlListQ5 = new ArrayList<>();

        img = findViewById(R.id.img);
        img2 = findViewById(R.id.img2);
        btnUpload = findViewById(R.id.btnUpload);
        btnClear = findViewById(R.id.btnClear);
        btnDownload = findViewById(R.id.btnDownload);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });
        btnClear.setVisibility(View.GONE);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.emptyEntries();
                Toast.makeText(getApplicationContext(), "Entries deleted from offline database", Toast.LENGTH_SHORT).show();
            }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadData();
            }
        });
        if (SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                return;
            }
        }
    }

    private void uploadData() {
        list.clear();
        Gson gson = new Gson();
        List<EntryModelClass2> dbList = db.getAllEntries();
        if(dbList.size()<1){
            Toast.makeText(this, "No entries in offline database!", Toast.LENGTH_SHORT).show();
            return;
        }

        for(EntryModelClass2 model : dbList){
            Type type = new TypeToken<StoreModelClass>(){}.getType();
            Type type2 = new TypeToken<List<ProductModelClass>>(){}.getType();
            Type type3 = new TypeToken<List<String>>(){}.getType();
            Type type4 = new TypeToken<List<String>>(){}.getType();

            StoreModelClass store = gson.fromJson(model.getStore(),type);
            List<ProductModelClass> productsList = gson.fromJson(model.getProductsList(),type2);

            List<String> listQ4 = gson.fromJson(model.getUrlListQ4(),type3);
            List<String> listQ5 = gson.fromJson(model.getUrlListQ5(),type4);

            EntryModelClass3 entryModel = new EntryModelClass3(model.getId(),model.getDate(),model.getUserId(),model.getUsername(),
                    store,model.getQuestion1(),model.getQuestion2(),productsList,listQ4,listQ5);

            list.add(entryModel);
        }
//        Toast.makeText(this, list.size()+"", Toast.LENGTH_SHORT).show();
//        img.setImageURI(Uri.parse(list.get(0).getUrlListQ4().get(0)));
//        img2.setImageURI(Uri.parse(list.get(0).getUrlListQ5().get(0)));

        for(EntryModelClass3 model : list){
            EntryModelClass entryModel = new EntryModelClass(model.getId(),model.getDate(),model.getUserId(),model.getUsername(),
                    model.getStore(),model.getQuestion1(),model.getQuestion2(),model.getProductsList(),urlListQ4,urlListQ5);
            databaseReference3.child(model.getId()).setValue(entryModel);

        }
        for(EntryModelClass3 model : list){
            List<String> list = new ArrayList<>();
            for(String uri : model.getUrlListQ4()){
                uploadImagesQ4(uri,model,list);
            }
        }
        for(EntryModelClass3 model : list){
            List<String> list = new ArrayList<>();
            for(String uri : model.getUrlListQ5()){
                uploadImagesQ5(uri,model,list);
            }
        }
        db.emptyEntries();
        Toast.makeText(this, "Entry uploaded", Toast.LENGTH_SHORT).show();

    }

    private void downloadData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                db.emptyStoresTable();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    StoreModelClass modelClass = snapshot1.getValue(StoreModelClass.class);
                    if(db.addStore(modelClass)){
                        flag1 = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                db.emptyProductsTable();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    ProductModelClass modelClass = snapshot1.getValue(ProductModelClass.class);
                    if(db.addProduct(modelClass)){
                        flag2 = true;
                    }
                }
                if(flag1 && flag2){
                    Toast.makeText(SyncDataActivity.this, "Data is downloaded in offline database", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(SyncDataActivity.this, "No data found in online database!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});
    }

    private  String getExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver() ;
        MimeTypeMap mime = MimeTypeMap.getSingleton() ;
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImagesQ4(String uri, EntryModelClass3 model, List<String> list ) {
        showProgressDialog("Uploading..");
        String filePath = getExternalStoragePathFromUri(Uri.parse(uri));

        File file = new File(filePath); // Step 2
        Uri fileUri = Uri.fromFile(file); // Step 3

        final StorageReference fileref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(fileUri));
        mUploadTask = fileref.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            list.add(uri.toString());
                            databaseReference3.child(model.getId()).child("urlListQ4").setValue(list);
                            hideProgressDialog();
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

    String getExternalStoragePathFromUri(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        String externalStoragePath = null;

        if (documentFile != null) {
            File file = new File(documentFile.getUri().getPath());
            externalStoragePath = file.getAbsolutePath();
        }

        return externalStoragePath;
    }

    private void uploadImagesQ5(String uri, EntryModelClass3 model, List<String> list) {
        showProgressDialog("Uploading..");

        String filePath = getExternalStoragePathFromUri(Uri.parse(uri));

        File file = new File(filePath); // Step 2
        Uri fileUri = Uri.fromFile(file); // Step 3

        final StorageReference fileref = mStorageRef.child(System.currentTimeMillis() + "." + getExtension(fileUri));
        mUploadTask = fileref.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            list.add(uri.toString());
                            databaseReference3.child(model.getId()).child("urlListQ5").setValue(list);
                            hideProgressDialog();
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
}