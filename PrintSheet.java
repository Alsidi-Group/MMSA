package com.mmsa.records;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** توليد كشف بيانات الموظف في ورقة A4 (PDF) بالشعار والاسم ونص السرية */
public class PrintSheet {

    private static final int W = 595, H = 842; // A4 بالنقاط
    private static final int GREEN = 0xFF14402A;
    private static final int GOLD = 0xFFC9A227;
    private static final int DARKRED = 0xFF8B1E1E;
    private static final int SUB = 0xFF5A6B5D;
    private static final int LINE = 0xFF9AA89A;
    private static final int LABELBG = 0xFFEAF0EA;

    public static File build(Context c, Employee e) throws IOException {
        File dir = new File(c.getCacheDir(), "exports");
        dir.mkdirs();
        File out = new File(dir, "MMSA_report_" + e.id + "_" + System.currentTimeMillis() + ".pdf");
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(W, H, 1).create();
        PdfDocument.Page page = doc.startPage(info);
        drawPage(c, page.getCanvas(), e);
        doc.finishPage(page);
        FileOutputStream f = new FileOutputStream(out);
        doc.writeTo(f);
        f.close();
        doc.close();
        return out;
    }

    private static void drawPage(Context c, Canvas cv, Employee e) {
        cv.drawColor(0xFFFFFFFF);

        // الشعار في الأعلى وتحته اسم الإدارة
        android.graphics.Bitmap logo = Theme.loadLogo(c);
        int ls = 84;
        if (logo != null) {
            cv.drawBitmap(logo, null,
                    new android.graphics.Rect(W / 2 - ls / 2, 26, W / 2 + ls / 2, 26 + ls), null);
        }
        Paint title = paint(19, GREEN, true);
        title.setTextAlign(Paint.Align.CENTER);
        cv.drawText("إدارة الخدمات الطبية العسكرية", W / 2f, 132, title);

        Paint goldLine = new Paint();
        goldLine.setColor(GOLD);
        goldLine.setStrokeWidth(2.2f);
        cv.drawLine(150, 144, 445, 144, goldLine);

        Paint sub = paint(12, SUB, false);
        sub.setTextAlign(Paint.Align.CENTER);
        cv.drawText("منظومة البيانات — كشف بيانات موظف", W / 2f, 163, sub);

        Paint meta = paint(9, SUB, false);
        meta.setTextAlign(Paint.Align.RIGHT);
        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                .format(new Date(System.currentTimeMillis()));
        cv.drawText("تاريخ الطباعة: " + dateStr, W - 20, 181, meta);
        meta.setTextAlign(Paint.Align.LEFT);
        cv.drawText("النظام: " + e.displayValue("empType", false), 20, 181, meta);

        // جدول البيانات حسب تسلسل الشيت
        int rowsCount = Employee.LABELS.length + 1; // + الصفة
        float top = 196, bottom = 768;
        float rowH = (bottom - top) / rowsCount;
        float labRight = 575, labLeft = 395, valLeft = 20, valRight = 395;

        Paint labelP = paint(10.5f, GREEN, true);
        Paint valP = paint(11, 0xFF1A1A1A, false);
        Paint border = new Paint();
        border.setColor(LINE);
        border.setStrokeWidth(0.8f);
        border.setStyle(Paint.Style.STROKE);
        Paint labelBg = new Paint();
        labelBg.setColor(LABELBG);

        String[][] rows = new String[rowsCount][2];
        rows[0][0] = "الصفة";
        rows[0][1] = e.displayValue("empType", true);
        for (int i = 0; i < Employee.LABELS.length; i++) {
            rows[i + 1][0] = Employee.LABELS[i][1];
            rows[i + 1][1] = e.displayValue(Employee.LABELS[i][0], true);
        }

        for (int i = 0; i < rowsCount; i++) {
            float y = top + i * rowH;
            cv.drawRect(labLeft, y, labRight, y + rowH, labelBg);
            drawVCentered(cv, labelP, rows[i][0], labRight - 10, y, rowH, labRight - labLeft - 20, Paint.Align.RIGHT);
            drawVCentered(cv, valP, rows[i][1], valRight - 10, y, rowH, valRight - valLeft - 20, Paint.Align.RIGHT);
            cv.drawRect(labLeft, y, labRight, y + rowH, border);
            cv.drawRect(valLeft, y, valRight, y + rowH, border);
        }

        // نص السرية أسفل الورقة: العنوان بخط عريض جداً وتحته تنبيه المشاركة
        Paint warn = paint(14, DARKRED, true);
        warn.setTextAlign(Paint.Align.CENTER);
        cv.drawText("(( سري جداً للغاية ))", W / 2f, 806, warn);

        Paint warn2 = paint(11, DARKRED, true);
        warn2.setTextAlign(Paint.Align.CENTER);
        cv.drawText("عدم مشاركة هذه البيانات مع الآخرين ⚠️", W / 2f, 828, warn2);
    }

    private static Paint paint(float size, int color, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setColor(color);
        p.setTypeface(bold ? Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                : Typeface.SANS_SERIF);
        if (bold) p.setFakeBoldText(true);
        return p;
    }

    private static void drawVCentered(Canvas cv, Paint p, String s, float x, float rowTop,
                                      float rowH, float maxWidth, Paint.Align align) {
        if (s == null || s.isEmpty()) return;
        android.text.TextPaint tp = new android.text.TextPaint(p);
        tp.setTextAlign(align);
        float base = rowTop + rowH / 2 - (p.ascent() + p.descent()) / 2;
        if (p.measureText(s) <= maxWidth) {
            cv.drawText(s, x, base, tp);
            return;
        }
        // سطران: كسر عند مسافة قريبة من المنتصف (للنصوص العربية الطويلة)
        int mid = s.length() / 2;
        int br = s.lastIndexOf(' ', mid);
        if (br <= 0) br = mid;
        String l1 = s.substring(0, br).trim();
        String l2 = s.substring(br).trim();
        if (tp.measureText(l2) > maxWidth) {
            l2 = TextUtils.ellipsize(l2, tp, maxWidth, TextUtils.TruncateAt.END).toString();
        }
        float lh = (p.ascent() + p.descent()) / 2;
        cv.drawText(l1, x, base - lh / 2 - 1, tp);
        cv.drawText(l2, x, base + lh / 2 + 1, tp);
    }

    /** مثلث تحذير ذهبي بعلامة تعجب — بديل الرمز ⚠ في ملف PDF */
    private static void drawTriangle(Canvas cv, float cx, float cy, float size) {
        Path path = new Path();
        float h = size * 0.9f;
        path.moveTo(cx, cy - h / 2);
        path.lineTo(cx - size / 2, cy + h / 2);
        path.lineTo(cx + size / 2, cy + h / 2);
        path.close();
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(GOLD);
        cv.drawPath(path, fill);
        Paint bang = paint(9, 0xFF3A2B00, true);
        bang.setTextAlign(Paint.Align.CENTER);
        cv.drawText("!", cx, cy + 3.5f, bang);
    }
}
