/*
    Part of this code is taken from https://github.com/scoute-dich/browser
    which is published under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    <http://www.gnu.org/licenses/>.
 */

package org.woheller69.audiometry;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class Backup {
    public static final int PERMISSION_REQUEST_CODE = 123;

    public static boolean checkPermissionStorage (Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestPermission(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_warning_amber_black_24dp);
        builder.setTitle("Permission needed");
        builder.setMessage("hEARtest needs access to external storage. Please accept permission and try again.");
        builder.setPositiveButton(R.string.dialog_OK_button, (dialog, which) -> {
            dialog.cancel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",activity.getPackageName())));
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivity(intent);
                }
            } else {
                //below android 11
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) {

        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                    throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
                }
                String[] children = sourceLocation.list();
                for (String aChildren : Objects.requireNonNull(children)) {
                    copyDirectory(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
                }
            } else {
                // make sure the directory we plan to store the recording in exists
                File directory = targetLocation.getParentFile();
                if (directory != null && !directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Cannot create dir " + directory.getAbsolutePath());
                }

                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);
                // Copy the bits from InputStream to OutputStream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
