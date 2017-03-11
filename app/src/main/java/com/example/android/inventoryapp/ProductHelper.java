package com.example.android.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.R.attr.version;

/**
 * Created by Steven on 7/03/2017.
 */


public class ProductHelper extends SQLiteOpenHelper {

    //name and version of the database
    public static final String DATABASE_NAME = "products";
    public static final int DATABASE_VERSION = 1;

    // String to create a new table
    public static final String SQL_CREATE_TABLE = "CREATE TABLE " +
            ProductContract.ProductEntry.TAB_NAME + " ( " +
            ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
            ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, " +
            ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
            + ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL );";

    //string to delete a table
    public static final String SQL_DELETE_TABLE = "DELETE TABLE IF EXISTS " +
            ProductContract.ProductEntry.TAB_NAME;


    public ProductHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
}
