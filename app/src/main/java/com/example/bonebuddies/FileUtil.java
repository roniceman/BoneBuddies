package com.example.bonebuddies;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    public static File saveBitmapToFile(Bitmap bitmap, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static File getTempFile() {
        File directory = new File(Environment.getExternalStorageDirectory(), "YourAppDirectoryName");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, "temp_image.jpg");
    }
}
