package com.superacm.demo.player.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PlayerFileUtil {
    /**
     * 将文件保存到 MediaStore（相册/视频），并通知媒体库刷新。
     * @param context Context
     * @param srcFile 源文件（应用私有目录）
     * @param mimeType 文件类型（如 "video/mp4"、"image/jpeg"）
     * @param relativeDir 目标相册目录（如 Environment.DIRECTORY_MOVIES、Environment.DIRECTORY_PICTURES）
     * @return 新文件的 Uri
     * @throws IOException 拷贝失败时抛出
     */
    public static Uri saveFileToMediaStore(Context context, File srcFile, String mimeType, String relativeDir) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, srcFile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir);

        ContentResolver resolver = context.getContentResolver();
        Uri collection = mimeType.startsWith("video/") ?
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) :
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        Uri uri = resolver.insert(collection, values);
        if (uri == null) throw new IOException("Failed to create MediaStore entry");

        try (OutputStream out = resolver.openOutputStream(uri);
             InputStream in = new FileInputStream(srcFile)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        // 通知媒体库刷新
        MediaScannerConnection.scanFile(context, new String[]{srcFile.getAbsolutePath()}, new String[]{mimeType}, null);
        return uri;
    }
} 