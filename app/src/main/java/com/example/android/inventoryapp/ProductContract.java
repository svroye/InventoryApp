package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.os.Build.ID;

/**
 * Created by Steven on 7/03/2017.
 */

public final class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    private ProductContract() {

    }


    public static final class ProductEntry implements BaseColumns {
        //name of the table
        public static final String TAB_NAME = "products";
        //column header for id
        public static final String _ID = BaseColumns._ID;
        //column header for product name
        public static final String COLUMN_PRODUCT_NAME = "Name";
        //column header for the quantity
        public static final String COLUMN_PRODUCT_QUANTITY = "Quantity";
        //column header for the price
        public static final String COLUMN_PRODUCT_PRICE = "Price";
        //column header for the image
        public static final String COLUMN_PRODUCT_IMAGE = "Image";

        //Content Uri for the table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    }
}
