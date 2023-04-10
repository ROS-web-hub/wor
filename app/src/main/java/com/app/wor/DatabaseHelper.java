package com.app.wor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.app.wor.models.EntryModelClass;
import com.app.wor.models.EntryModelClass2;
import com.app.wor.models.ProductModelClass;
import com.app.wor.models.StoreModelClass;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "app_data.db";
    public static final String TABLE_STORES= "stores";
    public static final String TABLE_PRODUCTS= "products";
    public static final String TABLE_ENTRY= "entry";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        //SQLiteDatabase db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table IF NOT EXISTS " + TABLE_ENTRY + "(id TEXT PRIMARY KEY,date TEXT,userId TEXT,username TEXT,store TEXT,question1 TEXT,question2 TEXT,productsList TEXT,urlListQ4 TEXT,urlListQ5 TEXT)");
        db.execSQL("Create Table IF NOT EXISTS " + TABLE_STORES + "(id TEXT PRIMARY KEY,westCode TEXT,customerId TEXT,street TEXT,city TEXT,chain TEXT)");
        db.execSQL("Create Table IF NOT EXISTS " + TABLE_PRODUCTS + "(id TEXT PRIMARY KEY,brandName TEXT,category TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL("DROP TABLE IF EXISTS" + TABLE_BOOKMARKS);
        onCreate(db);
    }

    public boolean addStore(StoreModelClass model){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id",model.getId());
        contentValues.put("westCode",model.getWestCode());
        contentValues.put("customerId",model.getCustomerId());
        contentValues.put("street",model.getStreet());
        contentValues.put("city",model.getCity());
        contentValues.put("chain",model.getChain());
        long isInserted = db.insert(TABLE_STORES,null,contentValues);
        if(isInserted == -1)
            return false;
        else
            return true;
    }

    public List<StoreModelClass> getAllStores(){
        SQLiteDatabase db=this.getReadableDatabase();// get readable database
        //query to get all rows from DATA_TABLE to cursor
        Cursor cursor=db.rawQuery(" select * from "+ TABLE_STORES ,null );
        List<StoreModelClass> dataList=new ArrayList<>(); // list of todo
        if(cursor.moveToFirst()){  // travers cursor
            do {
                StoreModelClass tm=new StoreModelClass(cursor.getString(0),cursor.getString(1),cursor.getString(2),
                        cursor.getString(3),cursor.getString(4),cursor.getString(5));
                dataList.add(tm);// add todo object to list
            }while (cursor.moveToNext());
        }
        return dataList; // return list
    }

    public boolean emptyStoresTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("delete from " + TABLE_STORES ,null);
        if(res.getCount() > 0)
            return true;

        return false;
    }

    public boolean addProduct(ProductModelClass model){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id",model.getId());
        contentValues.put("brandName",model.getBrandName());
        contentValues.put("category",model.getCategory());
        long isInserted = db.insert(TABLE_PRODUCTS,null,contentValues);
        if(isInserted == -1)
            return false;
        else
            return true;
    }

    public List<ProductModelClass> getAllProducts(){

        SQLiteDatabase db=this.getReadableDatabase();// get readable database
        //query to get all rows from DATA_TABLE to cursor
        Cursor cursor=db.rawQuery(" select * from "+ TABLE_PRODUCTS ,null );
        List<ProductModelClass> dataList=new ArrayList<>(); // list of todo
        if(cursor.moveToFirst()){  // travers cursor
            do {
                ProductModelClass tm=new ProductModelClass(cursor.getString(0),cursor.getString(1),cursor.getString(2));
                dataList.add(tm);// add todo object to list
            }while (cursor.moveToNext());
        }
        return dataList; // return list
    }

    public boolean emptyProductsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("delete from " + TABLE_PRODUCTS ,null);
        if(res.getCount() > 0)
            return true;

        return false;
    }

    public boolean addEntry(String id,String date,String userId,String username,String store,String question1,String question2,String productsList,
                            String urlListQ4,String urlListQ5){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id",id);
        contentValues.put("date",date);
        contentValues.put("userId",userId);
        contentValues.put("username",username);
        contentValues.put("store",store);
        contentValues.put("question1",question1);
        contentValues.put("question2",question2);
        contentValues.put("productsList",productsList);
        contentValues.put("urlListQ4",urlListQ4);
        contentValues.put("urlListQ5",urlListQ5);
        long isInserted = db.insert(TABLE_ENTRY,null,contentValues);
        if(isInserted == -1)
            return false;
        else
            return true;
    }

    public List<EntryModelClass2> getAllEntries(){
        SQLiteDatabase db=this.getReadableDatabase();// get readable database
        //query to get all rows from DATA_TABLE to cursor
        Cursor cursor=db.rawQuery(" select * from "+ TABLE_ENTRY ,null );
        List<EntryModelClass2> dataList=new ArrayList<>(); // list of todo
        if(cursor.moveToFirst()){  // travers cursor
            do {
                EntryModelClass2 tm=new EntryModelClass2(cursor.getString(0),cursor.getString(1),cursor.getString(2),
                        cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),
                        cursor.getString(7),cursor.getString(8),cursor.getString(9));
                dataList.add(tm);// add todo object to list
            }while (cursor.moveToNext());
        }
        return dataList; // return list
    }

    public boolean emptyEntries(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("delete from " + TABLE_ENTRY ,null);
        if(res.getCount() > 0)
            return true;

        return false;
    }
}
