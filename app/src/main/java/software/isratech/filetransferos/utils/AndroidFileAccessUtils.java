package software.isratech.filetransferos.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AndroidFileAccessUtils {

    /**
     * https://stackoverflow.com/a/62180319
     */
    @NonNull
    public static String getNameFromContentUri(@NonNull final ContentResolver contentResolver, @NonNull final Uri contentUri) {
        try (final Cursor returnCursor = contentResolver.query(contentUri, null, null, null, null)) {
            int nameColumnIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            return returnCursor.getString(nameColumnIndex);
        }
    }

    /**
     * Get a file's size
     *
     * @param contentResolver - the system's content resolver
     * @param uri             - the uri of the file
     * @return the file size
     */
    public static long getFileSizeFromUri(
            @NonNull final ContentResolver contentResolver,
            @NonNull final Uri uri
    ) {
        try(Cursor returnCursor = contentResolver.query(uri, null, null, null, null)) {
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            return returnCursor.getLong(sizeIndex);
        }
    }

    /** Get a file's human readable size
     *
     * @param fileSize - the file's size in bytes
     * @return a human readable file size */
    public static String getHumanReadableFileSize(long fileSize) {
        if (-1000 < fileSize && fileSize < 1000) {
            return fileSize + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (fileSize <= -999_950 || fileSize >= 999_950) {
            fileSize /= 1000;
            ci.next();
        }
        return String.format(Locale.ENGLISH, "%.1f %cB", fileSize / 1000.0, ci.current());
    }
}
