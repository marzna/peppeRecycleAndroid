package com.example.pepperecycle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MultipartBody;

public class PhotoManager implements Runnable {

    String photoPath;
    Bitmap mRGBA;
    String photoName;

    public PhotoManager(Bitmap mRGBA, String photoName) {
        this.mRGBA = mRGBA;
        this.photoName = photoName;
    }

    public void run() {
        savePhoto(mRGBA);

    }

    private void savePhoto(Bitmap bmp) { //https://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
        File pictureFile = getOutputMediaFile();

        if (pictureFile == null) {
            Log.d("Classif","Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile, false); //il "false" dovrebbe permettere di sovrascrivere l'immagine, se esiste
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Classif", "Impossibile salvare l'immagine. " + e.getMessage());
        } catch (IOException e) {
            Log.d("Classif", "Impossibile accedere al file: " + e.getMessage());
        }
        photoPath = "storage/emulated/0/DCIM/" + photoName; //pictureFile.toString(); ///storage/emulated/0/DCIM/PhotoPepper0.jpg

        Log.e("CLASSIF","SavePhoto eseguita");
    }

    //Create a File for saving an image or video
    private  File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + photoName);
        return mediaFile;
    }

    private void loadPhoto(ImageView imageViewPepperPhoto) {
        File imgFile = new File(photoPath);

        if( imgFile.exists() ) {
            //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Bitmap myBitmap = BitmapFactory.decodeFile(photoPath);
            //TODO DECOMMENTA SE LO TOGLI DALL'ONCREATE ImageView myImage = (ImageView) findViewById(R.id.imageViewPepperPhoto);

            imageViewPepperPhoto.setImageBitmap(myBitmap);
            Log.d("Classif", "Immagine caricata dal path: " + photoPath);
        } else {
            Log.d("Classif", "Non è stata trovata nessuna immagine nel path." + photoPath);
        }
    }


    //Elimina tutta la cartella di PeppeRecycle contenente le foto scattate.
    private boolean deleteDir(File dir) { //File dir = new File(imagesDir);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(dir);
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
