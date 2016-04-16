package com.scanlibrary;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhansi on 04/04/15.
 */
public class PickImageFragment extends Fragment {

    private View view;
    private ImageButton cameraButton;
    private ImageButton galleryButton;
    private Uri fileUri;
    private IScanner scanner;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.pick_image_fragment, null);
        init();
        return view;
    }

    private void init() {
        cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new CameraButtonClickListener());
        galleryButton = (ImageButton) view.findViewById(R.id.selectButton);
        galleryButton.setOnClickListener(new GalleryClickListener());
        if (isIntentPreferenceSet()) {
            handleIntentPreference();
        }
    }

    private void handleIntentPreference() {
        int preference = getIntentPreference();
        if (preference == ScanConstants.OPEN_CAMERA) {
            //openCamera();
            checkPermissionandopenCamera();
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            //openMediaContent();
            checkPermissionsAndOpenMediaContent();
        }
    }

    private boolean isIntentPreferenceSet() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        return preference != 0;
    }

    private int getIntentPreference() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        return preference;
    }


    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            checkPermissionandopenCamera();

        }
    }


    private void checkPermissionandopenCamera(){
        if (IScannerPermissions.isSDK_M()) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission((Activity) scanner, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                openCamera();

            }else{
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity) scanner,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        IScannerPermissions.ISP_GROUP_CAMERA_AND_READ_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }else{
            openCamera();
        }
    }

    private void checkPermissionsAndOpenMediaContent(){
        if (IScannerPermissions.isSDK_M()) {
            // Here, thisActivity is the current activity
            if ((ContextCompat.checkSelfPermission((Activity) scanner, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission((Activity) scanner, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

                openMediaContent();

            }else{
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity) scanner,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        IScannerPermissions.ISP_READ_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }else{
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case IScannerPermissions.ISP_GROUP_CAMERA_AND_READ_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openCamera();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ((Activity) scanner).finish();
                }
                return;
            }
            case IScannerPermissions.ISP_READ_WRITE_EXTERNAL_STORAGE:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openMediaContent();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ((Activity) scanner).finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class GalleryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            //openMediaContent();
            checkPermissionsAndOpenMediaContent();
        }
    }

    public void openMediaContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
    }

    public void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = createImageFile();
        file.getParentFile().mkdirs();
        fileUri = Uri.fromFile(file);
        if (file != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(cameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE);
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(ScanConstants.IMAGE_PATH, "IMG_" + timeStamp + ".jpg");
        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);
        Bitmap bitmap = null;
        if (resultCode == Activity.RESULT_OK) {
            try {
                switch (requestCode) {
                    case ScanConstants.START_CAMERA_REQUEST_CODE:
                        bitmap = getBitmap(fileUri);
                        break;

                    case ScanConstants.PICKFILE_REQUEST_CODE:
                        bitmap = getBitmap(data.getData());
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bitmap != null) {
            postImagePick(bitmap);
        }
    }

    protected void postImagePick(Bitmap bitmap) {
        Uri uri = Utils.getUri(getActivity(), bitmap);
        bitmap.recycle();
        scanner.onBitmapSelect(uri);
    }

    private Bitmap getBitmap(Uri selectedimg) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor = getActivity().getContentResolver().openAssetFileDescriptor(selectedimg, "r");
        Bitmap original = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
        return original;
    }
}