package net.nickchen450.profilephotosample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ALBUM_IMAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 12;

    private AppCompatImageView imageView;
    private Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.img_profile);

        // 拍照
        findViewById(R.id.btn_pic_and_cut).setOnClickListener(picListener);
        findViewById(R.id.btn_album_and_cut).setOnClickListener(albumListener);
    }

    private View.OnClickListener picListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (checkPermission())
                dispatchTakePictureIntent();
            else
                requestPermission();

        }
    };

    private View.OnClickListener albumListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dispatchTakeAlbumIntent();

        }
    };

    private void dispatchTakePictureIntent() {
        picUri = FileUtils.createImageUri(this); // convert path to Uri

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void dispatchTakeAlbumIntent() {
        Intent album = new Intent(Intent.ACTION_GET_CONTENT);
        album.setType("image/*");
        album.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(album, REQUEST_ALBUM_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    cropImage(this, picUri, 500, 500, 1, 1);
                    break;
                }
                case REQUEST_ALBUM_IMAGE: {
                    if (data != null && data.getData() != null) {
                        cropImage(this, data.getData(),500, 500, 1, 1);
                    }
                    break;
                }
                case UCrop.REQUEST_CROP: {
                    if (data != null) {
                        Uri outputUri = UCrop.getOutput(data);
                        if (outputUri != null) {
                            Glide.with(MainActivity.this)
                                    .load(outputUri)
                                    .into(imageView);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchTakePictureIntent();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "請同意使用權限，才可使用此功能", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
    }

    private void cropImage(Activity activity, Uri imageUri, int width, int height, float x, float y) {
        if (imageUri == null)
            return;
        try {
            Uri outUri = Uri.fromFile(FileUtils.createImageFile(activity));
            UCrop.Options options = new UCrop.Options();
            options.setActiveWidgetColor(ContextCompat.getColor(activity, R.color.colorAccent));
            options.setToolbarColor(ContextCompat.getColor(activity, R.color.colorAccent));
            options.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorAccent));
            options.setHideBottomControls(true);
            options.setToolbarWidgetColor(ContextCompat.getColor(activity, android.R.color.white));
            UCrop crop = UCrop.of(imageUri, outUri)
                    .withMaxResultSize(width, height)
                    .withOptions(options)
                    .withAspectRatio(x, y);
            Intent intent = crop.getIntent(activity);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.startActivityForResult(intent, UCrop.REQUEST_CROP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
