package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.icu.util.Currency;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Steven on 8/03/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //find Views
        TextView tvProductName = (TextView) view.findViewById(R.id.textview_product_name);
        TextView tvProductPrice = (TextView) view.findViewById(R.id.textview_price);
        TextView tvProductStock = (TextView) view.findViewById(R.id.textview_stock);

        //find values
        String name = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
        int price = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        int stock = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));

        //set values to the views
        tvProductName.setText(name);
        tvProductPrice.setText(price + "");
        tvProductStock.setText(stock + " in stock !");

    }
}
