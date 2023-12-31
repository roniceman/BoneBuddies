package com.example.bonebuddies;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Utils {

    public static Uri saveBitmapToInternalStorage(Context context, Bitmap bitmap, String fileName) throws IOException {
        File directory = context.getFilesDir();
        File file = new File(directory, fileName);

        try (FileOutputStream stream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }

        return Uri.fromFile(file);
    }

    public static Bitmap loadBitmapFromInternalStorage(Context context, String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir("images", Context.MODE_PRIVATE);
        File file = new File(directory, fileName);

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}
