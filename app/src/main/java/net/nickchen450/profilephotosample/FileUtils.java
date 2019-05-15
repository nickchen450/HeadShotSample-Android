package net.nickchen450.profilephotosample;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class FileUtils {


    public static File createImageFile(Context context) throws IOException {
        String fileName = Calendar.getInstance().getTimeInMillis() + ".png";
        File file = new File(context.getCacheDir(), "stayfunImage");
        if (!file.exists())
            file.mkdir();

        file = new File(file, fileName);
        if (!file.exists())
            file.createNewFile();
        return file;
    }

    public static Uri createImageUri(Context context) {
        try {
            return createUri(context, createImageFile(context));
        } catch (IOException e) {
            return null;
        }
    }

    public static Uri createUri(Context context, File file) {
        try {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+ ".provider", file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
