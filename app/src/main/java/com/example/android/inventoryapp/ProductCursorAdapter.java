package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.icu.text.NumberFormat;
import android.icu.util.Currency;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.id;
import static android.R.attr.name;

/**
 * Created by Steven on 8/03/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    Context mContext;
    Uri mUri;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        //find Views
        TextView tvProductName = (TextView) view.findViewById(R.id.textview_product_name);
        TextView tvProductPrice = (TextView) view.findViewById(R.id.textview_price);
        final TextView tvProductStock = (TextView) view.findViewById(R.id.textview_stock);
        Button sellButton = (Button) view.findViewById(R.id.sell_item);

        //find values
        String name = cursor.getString(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
        int price = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        int stock = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        // make int final to pass it to the onCLickListener for the button view
        final int id = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));
        double mPrice = price / 100.0;
        //set values to the views
        tvProductName.setText(name);
        tvProductPrice.setText(mContext.getResources().getString(R.string.price,
                java.text.NumberFormat.getCurrencyInstance().format(mPrice)));
        tvProductStock.setText(stock + " in stock !");


        View.OnClickListener myClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String [] selectionArgs = new String[] {String.valueOf(id)};
                Cursor c = mContext.getContentResolver().query(ProductContract.ProductEntry.CONTENT_URI,
                        null, ProductContract.ProductEntry._ID +"=?",selectionArgs,null);
                c.moveToFirst();
                int quantity = c.getInt(c.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
                if (quantity > 0) {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    context.getContentResolver().update(ProductContract.ProductEntry.CONTENT_URI, values,
                            ProductContract.ProductEntry._ID+"=?", selectionArgs);
                    Toast.makeText(context, context.getResources().getString(R.string.item_sold), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.no_stock), Toast.LENGTH_SHORT).show();
                }
                c.close();
            }

        };

        sellButton.setOnClickListener(myClickListener);

    }
}


