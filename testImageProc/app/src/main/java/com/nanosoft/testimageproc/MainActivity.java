package com.nanosoft.testimageproc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends AppCompatActivity {

    public static int REQUEST_EXTERNAL_STORAGE = 1;

    public static String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE = 0x11;
   public void requestPermissions() {
       ActivityCompat.requestPermissions(this, PERMISSION_STORAGE, REQUEST_CODE);
   }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]  permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                main();
            } else {
                Log.d("DENIED", "true");
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                System.exit(0);
            }
        }
    }

    public static String sd = Environment.getExternalStorageDirectory().getPath();
    public Bitmap image;
    public int width;
    public int height;
    public String imagePath = "Download/image.jpg";

    public void readImage() {
        Log.d("Image", "Reading");
        File imgFile = new File(sd, imagePath);
        Log.d("FILEPATH:", sd + imagePath);

        if (!imgFile.exists()) {
            try {
                throw new Exception("Path doesn't exist");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.image = getSampledImage(imgFile);
        this.width = image.getWidth();
        this.height = image.getHeight();
        Log.d("Dimensions", "Width: " + Integer.toString(width) + "Height: " + Integer.toString(height));
    }

    public static Bitmap getSampledImage(File imgFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
        options.inSampleSize = calculateInSampleSize(options, 134, 75);
        options.inJustDecodeBounds = false;

        Bitmap b =  BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
        return b;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Status ", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
    }

   public void main() {
       readImage();
       int offset;
       Log.d("Status", "main()");
       for (int i = 0; i < 10; i++) {
           offset = this.determineOffset(ColorOption.BLUE);
           Log.d("offset", Integer.toString(offset));
       }
       Log.d("Status ", "done");
   }

    public enum ColorOption {
        BLUE,
        RED,
    }

    public int determineOffset(ColorOption c) {
        Log.d("FUCK", "DOING SHIT");
        int threshold = 40;
        int blueAverage = this.averageBlue();
        int redAverage = this.averageRed();
       //vector of all tagged pixels...
        Log.d("Farther", "further");

        int xSum = 0;
        int ySum = 0;
        int numPixels = 0;
        if (c == ColorOption.BLUE) {
                Log.d("doing stuff", "for blue");
            //if we're testing for blue
            for (int w = 0; w < width; w++) {
                    for (int h = 0; h < height; h++) {
                        if (Color.blue(image.getPixel(w, h)) >= blueAverage + threshold) {
                            xSum += w;
                            ySum += h;
                            numPixels++;
                        }
                    }
            }
        } else { // if we're testing for red
            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                        if (Color.red(image.getPixel(w, h)) >= redAverage + threshold) {
                           xSum += w;
                            ySum += h;
                            numPixels++;
                        }
                }
            }
        }

        //  telemetry.addData("xSum / pixels.size()", Float.toString(Math.round(xSum / coloredPixels.size())));
        int xAvg = 0;
        int yAvg = 0; // not necessary right now, but may be necessary later for determining distance.
        Log.d("number of pixels", Integer.toString(numPixels));
        if (numPixels != 0) {
            xAvg = Math.round(xSum / numPixels);
            yAvg = Math.round(ySum / numPixels);
        }

        //center x coordinate...
        int centerX = Math.round(width / 2);
        //  telemetry.addData("Image center:", "" + centerX);
        //offset from rounded
        //if value is negative,
        // center is to the right of the center,
        //otherwise, it's on the left.


        // telemetry.addData("Offset is: ", "" + offset);
        Log.d("Determining offset", "done");
        return centerX - xAvg;
    }

    public int averageRed() {
        Log.d("averaging", "red");
        int totalRed = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
              //  Log.d("looping", "true");
                totalRed += Color.red(this.image.getPixel(w, h));
            }
        }
        Log.d("red averaging", "done");
        Log.d("red average is", Integer.toString(Math.round(totalRed/(this.width * this.height))));
        return Math.round(totalRed / (this.width * this.height));
    }

    public int averageBlue() {
        Log.d("averaging", "blue");
        int totalBlue = 0;
        for (int w = 0; w < width; w++) {
           // Log.d("looping", "true");
            for (int h = 0; h < height; h++) {
                totalBlue += Color.blue(image.getPixel(w, h));
            }
        }

        Log.d("blue averaging", "done");
        Log.d("red average is", Integer.toString(Math.round(totalBlue/(this.width * this.height))));
        return Math.round(totalBlue / (this.width * this.height));
    }
}



