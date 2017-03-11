package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

import static android.R.attr.data;
import static android.R.attr.id;
import static android.R.attr.name;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SENDTO;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;
import static java.lang.Integer.parseInt;

public class ExistingProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //log tag for the activity
    public final String LOG_TAG = ExistingProductActivity.class.getSimpleName();

    //views of the layout xml
    TextView mName;
    ImageView mImage;
    TextView mQuantity;
    TextView mPrice;
    Button sellButton;
    Button orderButton;
    Button confirmOrderButton;

    //uri of the image
    Uri mUri;
    //number of times the order button is pressed
    int numberOrdered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_product);

        mName = (TextView) findViewById(R.id.existing_product_name);
        mImage = (ImageView) findViewById(R.id.existing_product_image);
        mQuantity = (TextView) findViewById(R.id.existing_product_quantity);
        mPrice = (TextView) findViewById(R.id.existing_product_price);
        sellButton = (Button) findViewById(R.id.sell_button);
        orderButton = (Button) findViewById(R.id.order_button);
        confirmOrderButton = (Button) findViewById(R.id.confirm_order);

        //get the intent and the uri of the intent to know which item was pressed and load the data
        //from the database from that item
        Intent intent = getIntent();
        mUri = intent.getData();
        getSupportLoaderManager().initLoader(1, null, this);

        //clickListener for a new order (1item per click)
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderItem();
            }
        });

        //clickListener for a new sell (1 item per click)
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellItem();
            }
        });

        //clickListener for the confirm order (summary of the order, i.e. which product and how
        // many items are ordered. )
        confirmOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmOrder();
            }
        });

    }

    // method after the confirmOrder button was pressed. An email app opens with the summary of the order
    // i.e. the name of the product and the number of item you order
    private void confirmOrder() {
        Intent intent = new Intent(ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        String[] receiver = new String[]{getResources().getString(R.string.supplier_email)};
        intent.putExtra(Intent.EXTRA_EMAIL, receiver);
        String subject = getResources().getString(R.string.mail_subject);
        String name = mName.getText().toString();
        String body = getResources().getString(R.string.mail_body, name, numberOrdered);
        intent.putExtra(EXTRA_SUBJECT, subject);
        intent.putExtra(EXTRA_TEXT, body);
        startActivity(intent);
    }

    // new item ordered, stock increases
    public void orderItem() {
        numberOrdered++;
        long id = ContentUris.parseId(mUri);
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor c = getContentResolver().query(ProductContract.ProductEntry.CONTENT_URI, null,
                ProductContract.ProductEntry._ID + "=?", selectionArgs, null);
        c.moveToFirst();
        int quantity = c.getInt(c.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        quantity++;
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        getContentResolver().update(ProductContract.ProductEntry.CONTENT_URI, values,
                ProductContract.ProductEntry._ID + "=?", selectionArgs);
        mQuantity.setText(quantity + "");
        Toast.makeText(this, getResources().getString(R.string.item_ordered), Toast.LENGTH_SHORT).show();

    }

    // item sold, stock decreases
    public void sellItem() {
        long id = ContentUris.parseId(mUri);
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor c = getContentResolver().query(ProductContract.ProductEntry.CONTENT_URI, null,
                ProductContract.ProductEntry._ID + "=?", selectionArgs, null);
        c.moveToFirst();
        int quantity = c.getInt(c.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        if (quantity > 0) {
            quantity--;
            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            getContentResolver().update(ProductContract.ProductEntry.CONTENT_URI, values,
                    ProductContract.ProductEntry._ID + "=?", selectionArgs);
            mQuantity.setText(quantity + "");
            Toast.makeText(this, getResources().getString(R.string.item_sold), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.no_stock), Toast.LENGTH_SHORT).show();
        }
    }


    //get the image from the Uri
    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    //hide the save button, and only show the edit and delete options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem saveItem = menu.findItem(R.id.save);
        saveItem.setVisible(false);
        return true;
    }


    //specify what should happen when the menu items are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.edit:
                Intent intent = new Intent(this, ProductActivity.class);
                intent.setData(mUri);
                startActivity(intent);
                break;
            case R.id.delete:
                showDeleteConfirmationDialog();
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(getResources().getString(R.string.delete_product));
        b.setPositiveButton(getResources().getString(R.string.positive_delete_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });
        b.setNegativeButton(getResources().getString(R.string.negative_delete_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog ad = b.create();
        ad.show();
    }

    private void deleteProduct() {
        int deleted = getContentResolver().delete(mUri, null, null);
        if (deleted == 1) {
            Toast.makeText(this, getResources().getString(R.string.product_deleted), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.product_not_deleted), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, getIntent().getData(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {

            String name = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
            int quantity = data.getInt(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
            int price = data.getInt(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));
            String uriResource = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE));
            Uri uriImage = Uri.parse(uriResource);
            Bitmap image = getBitmapFromUri(uriImage);

            mName.setText(name);
            mQuantity.setText(quantity + "");
            mPrice.setText(price + "");
            mImage.setImageBitmap(image);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mName.setText("");
        mQuantity.setText("");
        mPrice.setText("");
        mImage.setImageResource(R.drawable.photo);
    }
}
