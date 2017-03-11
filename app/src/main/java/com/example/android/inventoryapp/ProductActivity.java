package com.example.android.inventoryapp;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.R.attr.name;
import static com.example.android.inventoryapp.R.id.save;


/**
 * the following methods were found on the Udacity forum and were created by member
 * crlsndrsjmnz :  takePicture, createImageFile, getAlbumDir, onActivityResult,
 * getBitmapFromUri
 */

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    //LOG tag of this class
    public final String LOG_TAG = ProductActivity.class.getSimpleName();

    //prefix and suffix for creating an image file
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    //Uri pointing to the storage file of the image taken with the camera
    private Uri mUri;

    //fileprovider authority
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    //directory of the camera
    private static final String CAMERA_DIR = "/dcim/";

    private static final int PICK_IMAGE_REQUEST = 0;

    private boolean isGalleryPicture = false;

    // Uri of the image location if the Activity was opened from an already existing product (i.e.
    // to edit the product)
    Uri imageUri;

    //Defines whether the activity was opened to add new product or to edit an existing product
    public boolean productAlreadyExisted = false;

    //Views ilmportant to show info to the user
    EditText tvName;
    EditText tvPrice;
    EditText tvQuantity;
    ImageView ivPicture;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    //Bitmap (=image)
    Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        Intent intent = getIntent();
        Uri intentUri = intent.getData();

        //see whether the activity was opened to add a new product or to edit an existing one
        if (intentUri == null) {
            setTitle(getResources().getString(R.string.new_product));
        } else {
            //edit product case
            productAlreadyExisted = true;
            setTitle(getResources().getString(R.string.edit_product));
            //load the data from the database of the product
            getSupportLoaderManager().initLoader(1, null, this);
        }

        tvName = (EditText) findViewById(R.id.name_textview);
        tvPrice = (EditText) findViewById(R.id.price_textview);
        tvQuantity = (EditText) findViewById(R.id.quantity_textview);
        ivPicture = (ImageView) findViewById(R.id.image_product);

        //set clickListener such that user can take a picture when clicking on the symbol
        ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }


    private void takePicture() {
        //intent to take picture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            //create a new file
            File f = createImageFile();
            Log.d(LOG_TAG, "File: " + f.getAbsolutePath());

            //get uri for the file
            mUri = FileProvider.getUriForFile(
                    this, FILE_PROVIDER_AUTHORITY, f);

            //Add uri to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);

            // Solution taken from http://stackoverflow.com/a/18332000/3346625
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = new File(Environment.getExternalStorageDirectory()
                    + CAMERA_DIR
                    + getString(R.string.app_name));

            Log.d(LOG_TAG, "Dir: " + storageDir);

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d(LOG_TAG, "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                //Set image to the textview
                mBitmap = getBitmapFromUri(mUri);
                ivPicture.setImageBitmap(mBitmap);

                isGalleryPicture = true;
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(LOG_TAG, "Uri: " + mUri.toString());

            //Set image to the textview
            mBitmap = getBitmapFromUri(mUri);
            ivPicture.setImageBitmap(mBitmap);
            TextView newPicture = (TextView) findViewById(R.id.new_picture_tv);
            newPicture.setText("");

            isGalleryPicture = false;
        }
    }


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


    //create the menu options; in this activity, the delete and edit buttons are set to invisible,
    //only the save button is important
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem deleteItem = menu.findItem(R.id.delete);
        deleteItem.setVisible(false);
        MenuItem editItem = menu.findItem(R.id.edit);
        editItem.setVisible(false);
        return true;
    }

    //specify what should happen when a menu item is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == save) {
            addProduct();
        }
        return super.onOptionsItemSelected(item);
    }

    //method to specify what should happen when the save button is pressed, i.e. add product
    public void addProduct() {
        //get the values from the Views
        String name = tvName.getText().toString().trim();
        String mPrice = tvPrice.getText().toString().trim();
        String mQuantity = tvQuantity.getText().toString().trim();
        //set the Uri either to the one created after taking an image or the one from the existing product
        Uri newUri = mUri;
        if (productAlreadyExisted && mUri == null) {
            newUri = imageUri;
        }

        //check whether all the fields have input; if not, a Toast message is shown to tell the user
        // that the product was not saved and we return to the previous activity
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(String.valueOf(mPrice)) ||
                TextUtils.isEmpty(String.valueOf(mQuantity)) || newUri == null) {
            Toast.makeText(this, getResources().getString(R.string.error_adding_product), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // all fields had an input. Distinction is made between an already existig product, in which case
        // the database needs to be updated, and a new product, in which case the insert method for the
        //database will be called
        if (productAlreadyExisted) {
            long id = ContentUris.parseId(getIntent().getData());
            String[] selectionArgs = new String[]{String.valueOf(id)};
            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, name);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, Integer.parseInt(mPrice));
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, Integer.parseInt(mQuantity));
            if (mUri == null) {
                values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(imageUri));
            } else {
                values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(mUri));
            }
            getContentResolver().update(ProductContract.ProductEntry.CONTENT_URI, values,
                    ProductContract.ProductEntry._ID + "=?", selectionArgs);

        } else {
            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, name);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, Integer.parseInt(mPrice));
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, Integer.parseInt(mQuantity));
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(mUri));
            getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
        }


        Toast.makeText(this, getResources().getString(R.string.product_saved), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, getIntent().getData(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        String name = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
        int price = data.getInt(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        int quantity = data.getInt(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        String uri = data.getString(data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE));
        imageUri = Uri.parse(uri);
        Bitmap image = getBitmapFromUri(imageUri);

        tvName.setText(name);
        tvPrice.setText(price + "");
        tvQuantity.setText(quantity + "");
        ivPicture.setImageBitmap(image);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        tvName.setText("");
        tvPrice.setText("");
        tvQuantity.setText("");
        ivPicture.setImageResource(R.drawable.photo);
    }
}
