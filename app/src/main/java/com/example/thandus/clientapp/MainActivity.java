    package com.example.thandus.clientapp;

    import android.app.ProgressDialog;
    import android.content.Context;
    import android.content.Intent;
    import android.database.Cursor;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.Canvas;
    import android.graphics.Color;
    import android.graphics.Paint;
    import android.net.Uri;
    import android.os.Environment;
    import android.os.Handler;
    import android.os.Message;
    import android.provider.MediaStore;
    import android.support.annotation.DrawableRes;
    import android.support.v4.content.FileProvider;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.util.AttributeSet;
    import android.util.Log;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import java.io.File;
    import java.io.IOException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;

    import static android.os.Environment.getExternalStoragePublicDirectory;

    public class MainActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private String mCurrentPhotoPath;
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private  Uri selectedImage = null;
    private ImageView imageView_upload = null;
    private TextView textView_imageComment = null;
    private String imgDecodableString = null;
    private  File imageFile = null;
    private ProgressDialog progress = null;


    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    textView_imageComment = (TextView)findViewById(R.id.textView_imageComment);
    imageView_upload = (ImageView) findViewById(R.id.imageView);
    setInitialImageAndText();
}

protected  void setInitialImageAndText(){
    imageView_upload.setImageResource(R.mipmap.ic_launcher);
    textView_imageComment.setText(getResources().getString(R.string.image_message));
}

public void browseGallery(View v){
    // Create intent to Open Image applications like Gallery, Google Photos
    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    // Start the Intent
    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
}

public void takePictureFromCamera(View v){
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(this, getString(R.string.toast_message_file_error), Toast.LENGTH_LONG)
                    .show();
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            System.out.println("photoFile is NOT NULL!!!!!");
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    else{
        System.out.println("takePictureIntent is NULL!!!!!");
    }
}

private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir =  getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    System.out.println("storageDir==="+storageDir.getAbsolutePath());
    File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
    );

    // Save a file: path for use with ACTION_VIEW intents
    mCurrentPhotoPath = image.getAbsolutePath();
    return image;
}

private void galleryAddPic() {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(mCurrentPhotoPath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    this.sendBroadcast(mediaScanIntent);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    try {
        // When an Image is picked
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                && null != data) {
            // Get the Image from data
            selectedImage = data.getData();
            imageFile = prepareAFile(selectedImage);
            // Set the Image in ImageView after decoding the String
            imageView_upload.setImageURI(selectedImage);
            //imageView_upload.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
            /*mCurrentPhotoPath = selectedImagePath;
            setPic();*/
            textView_imageComment.setText(getString(R.string.image_message_upload));
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //as soon as the camera activity is back with a result
            //write the image file to a gallary
            galleryAddPic();
            imageFile= new File(mCurrentPhotoPath);
            //processing the image to display

            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView_upload.setImageBitmap(imageBitmap);*/

            /*selectedImage = data.getData();
            imageView_upload.setImageURI(selectedImage);*/

            setPic();

            textView_imageComment.setText(getString(R.string.image_message_camera));
        }
        else {
            Toast.makeText(this,getString(R.string.toast_message_image_not_selected),Toast.LENGTH_LONG).show();
        }
    } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(this, getString(R.string.toast_message_selection_error), Toast.LENGTH_LONG)
                .show();
    }
}

private void setPic() {
    // Get the dimensions of the View
    int targetW = imageView_upload.getWidth();
    int targetH = imageView_upload.getHeight();

    // Get the dimensions of the bitmap
    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    bmOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    int photoW = bmOptions.outWidth;
    int photoH = bmOptions.outHeight;

    // Determine how much to scale down the image
    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false;
    bmOptions.inSampleSize = scaleFactor;
    bmOptions.inPurgeable = true;

    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    imageView_upload.setImageBitmap(bitmap);
}

public void submitToServer(View v) {
    //Prepare a file from image Uri
    if (imageFile != null) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {}
        Toast.makeText(MainActivity.this, getString(R.string.toast_message_image_submitted), Toast.LENGTH_SHORT).show();
        setInitialImageAndText();
        imageFile = null;
    }
    else{
        Toast.makeText(this, getString(R.string.toast_message_no_image_to_submit), Toast.LENGTH_SHORT).show();
    }
}

protected File prepareAFile(Uri imageUri){
    //Preparing File to be sent
    String[] filePathColumn = { MediaStore.Images.Media.DATA };
    // Get the cursor
    Cursor cursor = getContentResolver().query(imageUri,
            filePathColumn, null, null, null);
    // Move to first row
    cursor.moveToFirst();

    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
    imgDecodableString = cursor.getString(columnIndex);
    cursor.close();

    File imageFile= new File(imgDecodableString);
    return imageFile;
}
}

