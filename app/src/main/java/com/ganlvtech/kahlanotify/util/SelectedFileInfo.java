package com.ganlvtech.kahlanotify.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

public class SelectedFileInfo {
    public String displayName;
    public byte[] bytes;
    public int width;
    public int height;
    private Context mContext;
    private Uri mUri;

    public SelectedFileInfo(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                byte[] buf = new byte[4096];
                InputStream inputStream = contentResolver.openInputStream(uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    byteArrayOutputStream.write(buf, 0, len);
                }
                bytes = byteArrayOutputStream.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void decodeImage() {
        try {
            ParcelFileDescriptor parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(mUri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                width = image.getWidth();
                height = image.getHeight();
                parcelFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
