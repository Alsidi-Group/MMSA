package com.mmsa.records;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** معاينة كشف الطباعة A4 + حفظ PDF في الهاتف + المشاركة + الطباعة المحصّنة */
public class PrintPreviewActivity extends BaseActivity {

    private static final int REQ_SAVE_PDF = 61;

    private int id = -1;
    private File pdfFile;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        id = getIntent().getIntExtra("id", -1);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView loading = th.text(this,
                Lang.t("جارٍ تجهيز كشف الطباعة…", "Preparing the print sheet…",
                        "Préparation de la fiche…", "Подготовка листа…"), 14, th.sub, false);
        loading.setGravity(Gravity.CENTER);
        loading.setPadding(0, dp(24), 0, dp(8));
        ProgressBar pb = new ProgressBar(this);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(40), dp(40));
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        body.addView(loading, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        body.addView(pb, pp);

        page(Lang.t("وضع الطباعة A4", "A4 print mode", "Mode d'impression A4", "Режим печати A4"), body, null);
        prepare();
    }

    private void prepare() {
        new Thread(() -> {
            try {
                Employee e = Db.get(this).get(id);
                if (e == null) {
                    runOnUiThread(() -> {
                        toast(Lang.t("السجل غير موجود", "Record not found",
                                "Enregistrement introuvable", "Запись не найдена"));
                        finish();
                    });
                    return;
                }
                pdfFile = PrintSheet.build(this, e);
                runOnUiThread(() -> showPreview(e));
            } catch (final Exception ex) {
                runOnUiThread(() -> {
                    toast(Lang.t("تعذر تجهيز الكشف: ", "Could not build the sheet: ",
                            "Échec de la préparation : ", "Не удалось подготовить: ")
                            + ex.getMessage());
                    finish();
                });
            }
        }).start();
    }

    private void showPreview(Employee e) {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(12), dp(12), dp(12), dp(12));

        ScrollView sc = new ScrollView(this);
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(4), dp(4), dp(4), dp(12));
        sc.addView(inner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        body.addView(sc, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView head = th.text(this,
                Lang.t("معاينة كشف الطباعة — مقاس A4 للسجل: ", "A4 print preview for record: ",
                        "Aperçu de la fiche A4 pour : ", "Предварительный просмотр A4: ")
                        + e.displayValue("fullName", false), 13, th.primary, true);
        head.setGravity(Gravity.CENTER);
        inner.addView(head, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        inner.setPadding(dp(4), dp(10), dp(4), dp(12));

        LinearLayout paper = new LinearLayout(this);
        paper.setOrientation(LinearLayout.VERTICAL);
        paper.setBackgroundColor(Color.WHITE);
        paper.setElevation(dp(6));
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        int pw = Math.min(Math.min(dm.widthPixels, dm.heightPixels) - dp(32), 720);
        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(pdfFile,
                    ParcelFileDescriptor.MODE_READ_ONLY);
            android.graphics.pdf.PdfRenderer renderer = new android.graphics.pdf.PdfRenderer(fd);
            android.graphics.pdf.PdfRenderer.Page page = renderer.openPage(0);
            float ratio = (float) page.getHeight() / page.getWidth();
            Bitmap bm = Bitmap.createBitmap(pw, Math.round(pw * ratio), Bitmap.Config.ARGB_8888);
            bm.eraseColor(0xFFFFFFFF);
            page.render(bm, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            renderer.close();
            fd.close();
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(bm);
            iv.setAdjustViewBounds(true);
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ip.topMargin = dp(8);
            inner.addView(iv, ip);
        } catch (Exception ex) {
            TextView err = th.text(this,
                    Lang.t("تعذر عرض المعاينة: ", "Preview failed: ", "Échec de l'aperçu : ",
                            "Ошибка предпросмотра: ") + ex.getMessage(), 13, th.danger, false);
            inner.addView(err);
        }

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setBackgroundColor(th.card);
        bar.setElevation(dp(8));
        bar.setPadding(dp(12), dp(10), dp(12), dp(10));

        // حفظ PDF في الهاتف (اختيار الموقع عبر النظام)
        Button save = th.bigButton(this,
                Lang.t("حفظ PDF في الهاتف", "Save PDF to phone", "Enregistrer le PDF",
                        "Сохранить PDF на телефон"), "export", true);
        save.setOnClickListener(v -> savePdfTo());
        bar.addView(save, matchM(6));

        // حفظ في التنزيلات + مشاركة
        Button share = th.bigButton(this,
                Lang.t("حفظ في التنزيلات ومشاركة", "Save to Downloads & share",
                        "Enregistrer et partager", "В загрузки и поделиться"), "export", false);
        share.setOnClickListener(v -> {
            try {
                MiniFileProvider.saveToDownloads(this, pdfFile, "application/pdf");
                MiniFileProvider.share(this, pdfFile,
                        Lang.t("كشف بيانات الموظف", "Employee data sheet",
                                "Fiche de données de l'employé", "Лист данных сотрудника"));
            } catch (Exception ex) {
                toast(Lang.t("تعذر الحفظ: ", "Save failed: ", "Échec : ", "Ошибка: ") + ex.getMessage());
            }
        });
        bar.addView(share, matchM(6));

        // طباعة عبر النظام — محصّنة بالكامل: أي فشل يصبح رسالة وليس انهياراً
        Button print = th.button(this,
                Lang.t("طباعة عبر نظام الأندرويد", "Print via Android", "Imprimer via Android",
                        "Печать через Android"), false);
        print.setOnClickListener(v -> doPrint());
        bar.addView(print, match());

        page(Lang.t("وضع الطباعة A4", "A4 print mode", "Mode d'impression A4", "Режим печати A4"), body, bar);
    }

    /** اختيار مكان حفظ PDF عبر نافذة النظام (SAF) */
    private void savePdfTo() {
        try {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/pdf");
            i.putExtra(Intent.EXTRA_TITLE,
                    "MMSA_record_" + id + "_" + System.currentTimeMillis() + ".pdf");
            startActivityForResult(i, REQ_SAVE_PDF);
        } catch (Exception ex) {
            toast(Lang.t("تعذر فتح نافذة الحفظ: ", "Could not open the save dialog: ",
                    "Échec de la fenêtre d'enregistrement : ", "Не удалось открыть окно сохранения: ")
                    + ex.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != REQ_SAVE_PDF || res != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try (FileInputStream in = new FileInputStream(pdfFile);
             ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
             FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
            MiniFileProvider.copy(in, out);
            out.flush();
            toast(Lang.t("تم حفظ ملف PDF بنجاح", "PDF saved successfully",
                    "PDF enregistré avec succès", "PDF успешно сохранён"));
        } catch (Exception ex) {
            toast(Lang.t("تعذر حفظ الملف: ", "Could not save the file: ",
                    "Échec de l'enregistrement : ", "Не удалось сохранить файл: ") + ex.getMessage());
        }
    }

    /** الطباعة عبر PrintManager — كل شيء داخل حماية: أي خلل يظهر رسالة ولا يُسقط التطبيق */
    private void doPrint() {
        try {
            if (pdfFile == null || !pdfFile.exists()) {
                toast(Lang.t("ملف الكشف غير جاهز بعد", "The sheet file is not ready yet",
                        "Le fichier n'est pas prêt", "Файл ещё не готов"));
                return;
            }
            PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            if (pm == null) {
                toast(Lang.t("خدمة الطباعة غير متوفرة على هذا الجهاز — استخدم حفظ PDF",
                        "Print service not available on this device — use Save PDF instead",
                        "Service d'impression indisponible — utilisez Enregistrer le PDF",
                        "Служба печати недоступна — используйте сохранение PDF"));
                return;
            }
            PrintAttributes attrs = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .build();
            final File f = pdfFile;
            pm.print("MMSA_record_" + id, new PrintDocumentAdapter() {
                @Override
                public void onLayout(PrintAttributes oldA, PrintAttributes newA,
                                     android.os.CancellationSignal cs, LayoutResultCallback cb,
                                     Bundle extras) {
                    try {
                        if (cs != null && cs.isCanceled()) {
                            cb.onLayoutCancelled();
                            return;
                        }
                        PrintDocumentInfo info = new PrintDocumentInfo.Builder(f.getName())
                                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                .setPageCount(1)
                                .build();
                        cb.onLayoutFinished(info, false);
                    } catch (Exception ex) {
                        try { cb.onLayoutFailed(ex.getMessage()); } catch (Exception ignored) {}
                    }
                }

                @Override
                public void onWrite(PageRange[] pages, ParcelFileDescriptor dest,
                                    android.os.CancellationSignal cs, WriteResultCallback cb) {
                    try {
                        FileInputStream in = new FileInputStream(f);
                        FileOutputStream out = new ParcelFileDescriptor.AutoCloseOutputStream(dest);
                        MiniFileProvider.copy(in, out);
                        cb.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    } catch (Exception ex) {
                        try { cb.onWriteFailed(ex.getMessage()); } catch (Exception ignored) {}
                    }
                }
            }, attrs);
        } catch (Exception ex) {
            // أي انهيار في منظومة الطباعة يتحول إلى رسالة + اقتراح البديل الآمن
            toast(Lang.t("تعذرت الطباعة على هذا الجهاز — استخدم «حفظ PDF» ثم اطبعه من أي قارئ PDF",
                            "Printing failed on this device — use Save PDF then print from any PDF viewer",
                            "Échec de l'impression — utilisez Enregistrer le PDF puis imprimez-le",
                            "Ошибка печати — сохраните PDF и распечатайте из любого просмотрщика"),
                    true);
        }
    }

    private void toast(String s, boolean longer) {
        android.widget.Toast.makeText(this, s,
                longer ? android.widget.Toast.LENGTH_LONG : android.widget.Toast.LENGTH_SHORT).show();
    }
}
