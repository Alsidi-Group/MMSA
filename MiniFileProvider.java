package com.mmsa.records;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** مزوّد ملفات داخلي للمشاركة (بديل FileProvider بدون مكتبات خارجية) + أدوات حفظ ومشاركة */
public class MiniFileProvider extends ContentProvider {

    public static final String AUTH = "com.mmsa.records.files";

    @Override
    public boolean onCreate() {
        return true;
    }

    private File fileFor(Uri u) {
        Context c = getContext();
        File exports = new File(c.getCacheDir(), "exports");
        // فقط ملفات مباشرة داخل مجلد التصدير (منع أي اجتياز مسار أو مجلدات فرعية)
        String name = u.getLastPathSegment();
        if (name == null || name.contains("/") || name.contains("\\") || name.contains(".."))
            throw new SecurityException("مسار غير مسموح");
        File f = new File(exports, name);
        if (!f.getParentFile().equals(exports))
            throw new SecurityException("مسار غير مسموح");
        return f;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri u, String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(fileFor(u), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public Cursor query(Uri u, String[] proj, String sel, String[] args, String sort) {
        File f = fileFor(u);
        if (proj == null) proj = new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        MatrixCursor mc = new MatrixCursor(proj);
        Object[] row = new Object[proj.length];
        for (int i = 0; i < proj.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(proj[i])) row[i] = f.getName();
            else if (OpenableColumns.SIZE.equals(proj[i])) row[i] = f.length();
            else row[i] = null;
        }
        mc.addRow(row);
        return mc;
    }

    @Override
    public String getType(Uri u) {
        String n = fileFor(u).getName().toLowerCase();
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".xlsx")) return "vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (n.endsWith(".csv")) return "text/csv";
        return "application/octet-stream";
    }

    @Override
    public Uri insert(Uri u, ContentValues v) { return null; }

    @Override
    public int delete(Uri u, String s, String[] a) { return 0; }

    @Override
    public int update(Uri u, ContentValues v, String s, String[] a) { return 0; }

    // ---------- أدوات المشاركة والحفظ ----------

    public static File exportsDir(Context c) {
        File d = new File(c.getCacheDir(), "exports");
        d.mkdirs();
        return d;
    }

    public static Uri uriFor(File f) {
        return Uri.parse("content://" + AUTH + "/" + f.getName());
    }

    /** فتح مربع المشاركة لأي ملف داخل مجلد التصدير */
    public static void share(Context act, File f, String title) {
        Uri u = uriFor(f);
        Intent s = new Intent(Intent.ACTION_SEND);
        s.setType(act.getContentResolver().getType(u));
        s.putExtra(Intent.EXTRA_STREAM, u);
        s.putExtra(Intent.EXTRA_TITLE, title);
        s.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        act.startActivity(Intent.createChooser(s, title));
    }

    /** حفظ نسخة داخل مجلد التنزيلات Downloads/MMSA (أندرويد 10 وما فوق) */
    public static void saveToDownloads(Context c, File f, String mime) {
        if (android.os.Build.VERSION.SDK_INT < 29) return;
        try {
            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, f.getName());
            cv.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime);
            cv.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOWNLOADS + "/MMSA");
            Uri u = c.getContentResolver().insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            if (u != null) {
                OutputStream os = c.getContentResolver().openOutputStream(u);
                if (os != null) {
                    copy(new FileInputStream(f), os);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        out.close();
    }

    /** تصدير + حفظ + مشاركة في خطوة واحدة */
    public static void exportAndShare(Context c, File f, String mime) {
        saveToDownloads(c, f, mime);
        share(c, f, f.getName());
    }
}
