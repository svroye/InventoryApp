package com.example.android.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Steven on 7/03/2017.
 */


public class ProductProvider extends ContentProvider {


    // Uri matcher code for the products table
    private static final int PRODUCT = 100;
    //Uri matcher code for a single product in the products table
    private static final int PRODUCT_ID = 101;

    //UriMatcher to compare a Uri with the above constants
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //add content to the Uri matcher
    static {
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCT);
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private ProductHelper mProductHelper;

    @Override
    public boolean onCreate() {
        mProductHelper = new ProductHelper(getContext());
        return false;
    }

    //to read data from the database
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mProductHelper.getReadableDatabase();
        Cursor c;
        int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                c = db.query(ProductContract.ProductEntry.TAB_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = db.query(ProductContract.ProductEntry.TAB_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri !!!!! : " + uri);

        }
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("No valid Uri : " + uri);
        }
    }

    //insert new row in the table
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mProductHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                getContext().getContentResolver().notifyChange(uri,null);
                long newId = db.insert(ProductContract.ProductEntry.TAB_NAME,null,values);
                return ContentUris.withAppendedId(uri,newId);
            default: throw new IllegalArgumentException("No valid Uri : " + uri);
        }

    }

    //delete from the database
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = mUriMatcher.match(uri);
        SQLiteDatabase db = mProductHelper.getWritableDatabase();
        switch (match) {
            case PRODUCT:
                getContext().getContentResolver().notifyChange(uri,null);
                return db.delete(ProductContract.ProductEntry.TAB_NAME,selection,selectionArgs);
            case PRODUCT_ID:
                getContext().getContentResolver().notifyChange(uri,null);
                selection = ProductContract.ProductEntry._ID +"=?";
                selectionArgs= new String[]{ String.valueOf(ContentUris.parseId(uri))};
                return db.delete(ProductContract.ProductEntry.TAB_NAME,selection,selectionArgs);
            default:
                throw new IllegalStateException("No valid Uri : " + uri);
        }
    }

    //update row(s) of the database
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = mUriMatcher.match(uri);
        SQLiteDatabase db = mProductHelper.getWritableDatabase();
        switch (match) {
            case PRODUCT:
                getContext().getContentResolver().notifyChange(uri,null);
                return db.update(ProductContract.ProductEntry.TAB_NAME,values,selection,selectionArgs);
            case PRODUCT_ID:
                getContext().getContentResolver().notifyChange(uri,null);
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri)) };
                return db.update(ProductContract.ProductEntry.TAB_NAME,values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Invalid Uri : " + uri);
        }
    }
}
