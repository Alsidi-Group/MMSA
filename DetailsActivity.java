package com.mmsa.records;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** عرض سجل موظف بالتفصيل الكامل مع أدوات التعديل والحذف والطباعة والتصدير */
public class DetailsActivity extends BaseActivity {

    private int id = -1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        id = getIntent().getIntExtra("id", -1);
        page(Lang.t("تفاصيل الموظف", "Employee details", "Détails de l'employé", "Данные сотрудника"),
                emptyBody(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    private LinearLayout emptyBody() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        return body;
    }

    /** أيقونة كل بند حسب مفتاحه */
    private String iconOf(String key) {
        switch (key) {
            case "finNumber":
            case "milNumber":
                return "badge";
            case "rank":
            case "grade":
            case "annualAllowance":
                return "star";
            case "qualification":
            case "techQual":
            case "specialty":
                return "doc";
            case "nationalId":
                return "id";
            case "fullName":
                return "person";
            case "unit":
                return "building";
            case "appointDecree":
                return "doc";
            case "bankName":
            case "bankBranch":
                return "bank";
            case "bankAccount":
            case "salary":
                return "money";
            default:
                return "calendar"; // كل الحقول التاريخية
        }
    }

    /** بناء الشاشة كاملة من قاعدة البيانات (يُعاد البناء بعد التعديل) */
    private void build() {
        Employee e = Db.get(this).get(id);
        if (e == null) {
            toast(Lang.t("السجل غير موجود", "Record not found",
                    "Enregistrement introuvable", "Запись не найдена"));
            finish();
            return;
        }

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(16), dp(16), dp(16), dp(24));
        sc.addView(inner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(sc, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ===== ترويسة الموظف =====
        LinearLayout hero = th.card(this, 22, 16);
        inner.addView(hero, matchM(12));
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        ImageView av = new ImageView(this);
        av.setImageDrawable(th.icon(this, "person", th.onPrimary, 26));
        av.setScaleType(ImageView.ScaleType.FIT_CENTER);
        av.setBackground(th.gradient(this, 22, th.primary, th.primaryDark));
        av.setPadding(dp(11), dp(11), dp(11), dp(11));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp(52), dp(52));
        ap.rightMargin = dp(12);
        head.addView(av, ap);
        LinearLayout ht = new LinearLayout(this);
        ht.setOrientation(LinearLayout.VERTICAL);
        TextView name = th.text(this, e.displayValue("fullName", false), 17, th.text, true);
        ht.addView(name, matchM(2));
        LinearLayout chipsRow = new LinearLayout(this);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        TextView rankChip = th.chip(this, e.displayValue("rank", true), th.fieldBg, th.primary);
        TextView typeChip = th.chip(this, e.displayValue("empType", true), th.fieldBg, th.sub);
        LinearLayout.LayoutParams cp1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cp1.leftMargin = dp(6);
        chipsRow.addView(rankChip);
        chipsRow.addView(typeChip, cp1);
        ht.addView(chipsRow, match());
        head.addView(ht, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        hero.addView(head, matchM(10));

        // ===== بيانات الموظف بترتيب الشيت =====
        LinearLayout card = th.card(this, 20, 16);
        inner.addView(card, matchM(12));
        card.addView(th.sectionHeader(this, "doc",
                Lang.t("بيانات الموظف", "Employee data", "Données de l'employé", "Данные сотрудника")), matchM(6));
        addRow(card, "id", Employee.labelUi("empType"), e.displayValue("empType", true));
        for (String[] pair : Employee.LABELS) {
            addRow(card, pair[0], Employee.labelUi(pair[0]), e.displayValue(pair[0], true));
        }

        // ===== أزرار الأدوات =====
        LinearLayout c1 = th.card(this, 20, 16);
        inner.addView(c1, matchM(12));
        c1.addView(th.sectionHeader(this, "tools",
                Lang.t("أدوات السجل", "Record tools", "Outils d'enregistrement", "Инструменты записи")), matchM(10));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        Button edit = th.bigButton(this,
                Lang.t("تعديل البيانات", "Edit record", "Modifier des données", "Изменить данные"), "edit", true);
        Button del = th.button(this,
                Lang.t("حذف السجل", "Delete record", "Supprimer", "Удалить"), false);
        del.setTextColor(th.danger);
        row1.addView(edit, half());
        row1.addView(del, half());
        edit.setOnClickListener(v -> {
            Intent i = new Intent(this, FormActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        });
        del.setOnClickListener(v -> confirm(Lang.t("حذف السجل", "Delete record",
                        "Supprimer l'enregistrement", "Удалить запись"),
                Lang.t("سيتم حذف بيانات الموظف «", "Employee record «",
                        "L'enregistrement de l'employé « ", "Запись сотрудника «")
                        + e.displayValue("fullName", false)
                        + Lang.t("» نهائياً. هل تريد المتابعة؟",
                        "» will be deleted permanently. Continue?",
                        "» sera supprimé définitivement. Continuer ?",
                        "» будет удалена навсегда. Продолжить?"),
                () -> {
                    Db.get(this).recordChange("delete", e, e, null);
                    Db.get(this).delete(id);
                    toast(Lang.t("تم حذف السجل", "Record deleted",
                            "Enregistrement supprimé", "Запись удалена"));
                    finish();
                }));
        c1.addView(row1, matchM(10));

        Button print = th.bigButton(this,
                Lang.t("طباعة كشف بيانات الموظف (A4)", "Print employee sheet (A4)",
                        "Imprimer la fiche (A4)", "Печать листа сотрудника (A4)"), "print", true);
        print.setOnClickListener(v -> {
            Intent i = new Intent(this, PrintPreviewActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        });
        c1.addView(print, matchM(10));

        Button export = th.bigButton(this,
                Lang.t("تصدير هذا السجل إلى Excel (XLSX)", "Export this record to Excel (XLSX)",
                        "Exporter vers Excel (XLSX)", "Экспортировать в Excel (XLSX)"), "export", false);
        export.setOnClickListener(v -> exportOne(e));
        c1.addView(export, match());

        page(Lang.t("تفاصيل الموظف", "Employee details", "Détails de l'employé", "Данные сотрудника"),
                body, null);
    }

    /** صف معلومة: أيقونة بند + التسمية يميناً والقيمة يساراً */
    private void addRow(LinearLayout card, String key, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(th.box(this, 12, th.fieldBg, th.line, 1f));
        int p = dp(10);
        row.setPadding(p, p, p, p);

        ImageView iv = new ImageView(this);
        iv.setImageDrawable(th.icon(this, iconOf(key), th.primary, 15));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(18), dp(18));
        ip.rightMargin = dp(8);
        row.addView(iv, ip);

        TextView l = th.text(this, label, 12, th.sub, true);
        row.addView(l, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView v = th.text(this, value, 14, th.text, false);
        v.setGravity(Gravity.LEFT);
        row.addView(v, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams rp = matchM(6);
        card.addView(row, rp);
    }

    /** تصدير سجل واحد إلى ملف Excel وحفظه/مشاركته */
    private void exportOne(final Employee e) {
        toast(Lang.t("جارٍ تجهيز الملف…", "Preparing file…", "Préparation du fichier…", "Подготовка файла…"));
        new Thread(() -> {
            try {
                File dir = MiniFileProvider.exportsDir(this);
                File f = new File(dir, "MMSA_record_" + e.id + "_" + System.currentTimeMillis() + ".xlsx");
                List<String[]> rows = new ArrayList<>();
                rows.add(e.exportRow());
                SheetIO.writeXlsx(f, Employee.EXPORT_HEADERS, rows);
                runOnUiThread(() -> MiniFileProvider.exportAndShare(this, f,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            } catch (Exception ex) {
                runOnUiThread(() -> toast(Lang.t("تعذر التصدير: ", "Export failed: ",
                        "Échec de l'export : ", "Ошибка экспорта: ") + ex.getMessage()));
            }
        }).start();
    }
}
