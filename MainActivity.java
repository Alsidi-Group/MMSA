package com.mmsa.records;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * الواجهة الرئيسية: شعار أكبر + اسم الإدارة بالعريض وتحته منظومة البيانات + 4 خانات متناسقة +
 * أزرار منفصلة (بحث / إضافة / تعديل) + بحث تلقائي عند التوقف عن الكتابة + سجل التغييرات عند الفتح.
 */
public class MainActivity extends BaseActivity {

    private EditText fName, fNat, fNum;
    private Spinner sIdType;
    private LinearLayout resultsBox;
    private TextView resTitle, vAll, vMil, vMed, vCiv;
    private Button searchBtn;
    private ArrayList<Employee> results = new ArrayList<>();
    private boolean autoChecked = false;
    private boolean changesShown = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        inner.setPadding(pad, pad, pad, dp(20));
        sc.addView(inner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(sc, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ===== البطاقة الرئيسية: شعار أكبر وأوضح + اسم الإدارة بالعريض + منظومة البيانات =====
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setBackground(th.gradient(this, 26, th.primary, th.primaryDark));
        hero.setPadding(pad, dp(26), pad, dp(22));
        hero.setElevation(dp(5));

        ImageView logo = new ImageView(this);
        Bitmap lg = Theme.loadLogo(this);
        logo.setImageBitmap(lg);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        GradientDrawable disc = new GradientDrawable();
        disc.setShape(GradientDrawable.OVAL);
        disc.setColor(0xFFFFFFFF);
        logo.setBackground(disc);
        int pp = dp(8);
        logo.setPadding(pp, pp, pp, pp);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(124), dp(124));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        hero.addView(logo, lp);

        TextView org = th.text(this,
                Lang.t("إدارة الخدمات الطبية العسكرية",
                        "Military Medical Services Administration",
                        "Administration des services médicaux militaires",
                        "Управление военно-медицинской службой"),
                19, th.onPrimary, true);
        org.setGravity(Gravity.CENTER);
        org.setPadding(0, dp(14), 0, dp(2));
        hero.addView(org, match());

        TextView sys = th.text(this,
                Lang.t("منظومة البيانات", "Data Management System",
                        "Système de gestion des données", "Система управления данными"),
                14.5f, Theme.blend(th.onPrimary, th.primary, 0.20f), false);
        sys.setGravity(Gravity.CENTER);
        sys.setPadding(0, dp(4), 0, 0);
        hero.addView(sys, match());

        inner.addView(hero, matchM(12));

        // ===== شرائح الإحصاء =====
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cp.setMargins(dp(3), 0, dp(3), 0);
        vAll = addStatChip(chips, "people",
                Lang.t("الكل", "All", "Tous", "Все"), "0", cp);
        vMil = addStatChip(chips, "badge",
                Lang.t("عسكري", "Military", "Militaire", "Военные"), "0", cp);
        vMed = addStatChip(chips, "shield",
                Lang.t("طبي", "Medical", "Médical", "Медицинские"), "0", cp);
        vCiv = addStatChip(chips, "person",
                Lang.t("مدني", "Civilian", "Civil", "Гражданские"), "0", cp);
        inner.addView(chips, matchM(12));

        // ===== بطاقة البحث =====
        LinearLayout card = th.card(this, 22, 16);
        inner.addView(card, matchM(12));
        card.addView(th.sectionHeader(this, "search",
                Lang.t("البحث عن موظف (خانة واحدة فقط)", "Search employee (one field only)",
                        "Rechercher (un seul champ)", "Поиск (одно поле)")), matchM(10));

        card.addView(th.label(this,
                Lang.t("الاسم الرباعي", "Full name", "Nom complet", "Полное имя")), match());
        fName = th.field(this, Employee.K_TEXT,
                Lang.t("اكتب الاسم أو جزءاً منه", "Type the name or a part of it",
                        "Tapez le nom ou une partie", "Введите имя или его часть"), "person");
        card.addView(fName, matchM(8));

        card.addView(th.label(this,
                Lang.t("الرقم الوطني (أرقام فقط — 12 رقم)", "National no. (12 digits)",
                        "N° national (12 chiffres)", "Национальный номер (12 цифр)")), match());
        fNat = th.field(this, Employee.K_NUM,
                Lang.t("أرقام فقط", "Digits only", "Chiffres uniquement", "Только цифры"), "id");
        card.addView(fNat, matchM(8));

        card.addView(th.label(this,
                Lang.t("رقم الوظيفي (المالي) أو الرقم العسكري",
                        "Job (financial) no. or military no.",
                        "N° d'emploi (financier) ou militaire",
                        "Рабочий (финансовый) или военный номер")), match());
        LinearLayout numRow = new LinearLayout(this);
        numRow.setOrientation(LinearLayout.HORIZONTAL);
        numRow.setGravity(Gravity.CENTER_VERTICAL);
        sIdType = th.spinner(this, new String[]{Employee.ID_FIN, Employee.ID_MIL});
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.42f);
        sp.setMarginEnd(dp(8));
        numRow.addView(th.spinnerIconed(this, sIdType, "badge"), sp);
        fNum = th.field(this, Employee.K_NUM,
                Lang.t("أرقام فقط", "Digits only", "Chiffres uniquement", "Только цифры"), "badge");
        numRow.addView(fNum, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.58f));
        card.addView(numRow, matchM(12));

        LinearLayout br = new LinearLayout(this);
        br.setOrientation(LinearLayout.HORIZONTAL);
        searchBtn = th.bigButton(this,
                Lang.t("بحث", "Search", "Rechercher", "Искать"), "search", true);
        Button clear = th.button(this, Lang.t("مسح", "Clear", "Effacer", "Очистить"), false);
        br.addView(searchBtn, half());
        br.addView(clear, half());
        searchBtn.setOnClickListener(v -> doSearch());
        clear.setOnClickListener(v -> {
            fName.setText("");
            fNat.setText("");
            fNum.setText("");
            results.clear();
            renderResults();
        });
        card.addView(br, match());

        // البحث الذكي: زر البحث يظهر فعّالاً ويبحث تلقائياً بمجرد التوقف عن الكتابة
        attachSmartSearch(fName, () -> {
            String s = fName.getText().toString().trim();
            if (s.length() >= 2 && singleField()) runSearch(null, null, s);
        });
        Theme.attachNationalValidator(this, fNat);
        attachSmartSearch(fNat, () -> {
            String s = Employee.digitsOnly(fNat.getText().toString());
            if (s.length() == 12 && singleField()) runSearch("nationalId", null, null);
        });
        attachSmartSearch(fNum, () -> {
            String s = Employee.digitsOnly(fNum.getText().toString());
            if (s.length() >= 3 && singleField()) runSearch(null, s, null);
        });

        // ===== نتائج البحث =====
        resTitle = th.text(this, "", 13.5f, th.text, true);
        resTitle.setPadding(dp(4), dp(6), dp(4), dp(8));
        resultsBox = new LinearLayout(this);
        resultsBox.setOrientation(LinearLayout.VERTICAL);
        inner.addView(resTitle, match());
        inner.addView(resultsBox, match());
        renderResults();

        // ===== الأزرار المنفصلة: إضافة بيانات جديدة + تعديل البيانات =====
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setBackgroundColor(th.card);
        bar.setElevation(dp(10));
        int bp = dp(14);
        bar.setPadding(bp, dp(12), bp, dp(12));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        Button add = th.bigButton(this,
                Lang.t("إضافة بيانات جديدة", "Add new record", "Ajouter des données", "Добавить данные"),
                "plus", true);
        Button editB = th.bigButton(this,
                Lang.t("تعديل البيانات", "Edit record", "Modifier des données", "Изменить данные"),
                "edit", false);
        row1.addView(add, half());
        row1.addView(editB, half());
        add.setOnClickListener(v ->
                startActivity(new Intent(this, FormActivity.class)));
        editB.setOnClickListener(v -> editDialog());
        bar.addView(row1, matchM(10));

        // ===== الخانات الأربع بنفس الحجم والتنسيق =====
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(dp(3), 0, dp(3), 0);
        row2.addView(th.tileButton(this,
                Lang.t("قاعدة البيانات", "Database", "Base de données", "База данных"), "list",
                () -> startActivity(new Intent(this, ListActivity.class))), tp);
        row2.addView(th.tileButton(this,
                Lang.t("الإحصائيات", "Statistics", "Statistiques", "Статистика"), "stats",
                () -> startActivity(new Intent(this, StatsActivity.class))), tp);
        row2.addView(th.tileButton(this,
                Lang.t("إدارة البيانات", "Data management", "Gestion des données", "Управление данными"), "tools",
                () -> startActivity(new Intent(this, ToolsActivity.class))), tp);
        row2.addView(th.tileButton(this,
                Lang.t("حول التطبيق", "About", "À propos", "О приложении"), "info",
                () -> startActivity(new Intent(this, AboutActivity.class))), tp);
        bar.addView(row2, matchM(10));

        Button print = th.bigButton(this,
                Lang.t("الطباعة A4", "Print A4", "Impression A4", "Печать A4"), "print", false);
        print.setOnClickListener(v -> printTool());
        bar.addView(print, match());

        page(Lang.t("الصفحة الرئيسية", "Home", "Accueil", "Главная страница"), body, bar, false);
    }

    /** شريحة إحصاء بأيقونة وقيمة رقمية — تُعيد مرجع القيمة للتحديث لاحقاً */
    private TextView addStatChip(LinearLayout parent, String icon, String title, String val,
                                 LinearLayout.LayoutParams params) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setBackground(th.box(this, 16, th.fieldBg, th.line, 1f));
        l.setPadding(dp(6), dp(10), dp(6), dp(10));
        ImageView iv = new ImageView(this);
        iv.setImageDrawable(th.icon(this, icon, th.primary, 18));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        l.addView(iv, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView t = th.text(this, title, 11.5f, th.sub, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(4), 0, 0);
        l.addView(t, match());
        TextView v = th.text(this, val, 15, th.text, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(0, dp(2), 0, 0);
        l.addView(v, match());
        parent.addView(l, params);
        return v;
    }

    // ---------- البحث الذكي ----------

    /** يظهر زر البحث فعّالاً عند التوقف عن الكتابة، مع تنفيذ عمل البحث تلقائياً */
    private void attachSmartSearch(EditText e, Runnable onStopped) {
        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                searchBtn.setAlpha(0.45f); // يظهر فعّالاً بمجرد التوقف عن الكتابة
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        e.addTextChangedListener(new Theme.StopTyping(800, () -> {
            searchBtn.setAlpha(1f);
            onStopped.run();
        }));
    }

    /** هل خانة واحدة فقط مشغولة؟ */
    private boolean singleField() {
        int n = 0;
        if (!fName.getText().toString().trim().isEmpty()) n++;
        if (!Employee.digitsOnly(fNat.getText().toString()).isEmpty()) n++;
        if (!Employee.digitsOnly(fNum.getText().toString()).isEmpty()) n++;
        return n == 1;
    }

    private void runSearch(String natCol, String num, String name) {
        Db db = Db.get(this);
        if (name != null) results = db.searchName(name);
        else if (natCol != null)
            results = db.searchByCol("nationalId", Employee.digitsOnly(fNat.getText().toString()));
        else
            results = db.searchByCol(sIdType.getSelectedItemPosition() == 0
                    ? "finNumber" : "milNumber", num);
        renderResults();
    }

    /** البحث اليدوي بزر البحث */
    private void doSearch() {
        String name = fName.getText().toString().trim();
        String nat = Employee.digitsOnly(fNat.getText().toString());
        String num = Employee.digitsOnly(fNum.getText().toString());
        int filled = 0;
        if (!name.isEmpty()) filled++;
        if (!nat.isEmpty()) filled++;
        if (!num.isEmpty()) filled++;
        if (filled == 0) {
            toast(Lang.t("اكتب بيانات البحث في خانة واحدة", "Type your search in one field",
                    "Tapez la recherche dans un seul champ", "Введите данные поиска в одно поле"));
            return;
        }
        if (filled > 1) {
            toast(Lang.t("يمكن البحث في خانة واحدة فقط — امسح الخانات الأخرى",
                    "Only one search field allowed — clear the others",
                    "Un seul champ autorisé — effacez les autres",
                    "Только одно поле — очистите остальные"));
            return;
        }
        if (!nat.isEmpty() && nat.length() != 12) {
            Theme.dialog(this)
                    .setTitle(Lang.t("تنبيه", "Warning", "Attention", "Внимание"))
                    .setMessage(Lang.t("يرجى اعادة التأكد من الرقم الوطني 12 رقم",
                            "Please re-check the national number — it must be 12 digits",
                            "Veuillez revérifier le numéro national — 12 chiffres requis",
                            "Проверьте национальный номер — он должен состоять из 12 цифр"))
                    .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                    .show();
            return;
        }
        runSearch(nat.isEmpty() ? null : "nationalId",
                nat.isEmpty() ? num : null, name.isEmpty() ? null : name);
    }

    // ---------- زر تعديل البيانات: إيجاد السجل ثم فتح نموذج التعديل ----------

    private void editDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = dp(20);
        box.setPadding(p, dp(10), p, 0);
        Spinner sp = th.spinner(this, new String[]{
                Employee.labelUi("nationalId"), Employee.labelUi("finNumber"),
                Employee.labelUi("milNumber")});
        box.addView(sp, matchM(8));
        final EditText in = th.field(this, Employee.K_NUM,
                Lang.t("أرقام فقط", "Digits only", "Chiffres uniquement", "Только цифры"), "badge");
        box.addView(in, match());

        dlg()
                .setTitle(Lang.t("تعديل البيانات — أدخل رقم السجل",
                        "Edit record — enter its number",
                        "Modifier — entrez le numéro",
                        "Изменить — введите номер"))
                .setView(box)
                .setPositiveButton(Lang.t("بحث", "Search", "Rechercher", "Найти"), (d, w) -> {
                    String v = Employee.digitsOnly(in.getText().toString());
                    if (v.isEmpty()) {
                        toast(Lang.t("أدخل الرقم أولاً", "Enter a number first",
                                "Entrez d'abord un numéro", "Сначала введите номер"));
                        return;
                    }
                    int which = sp.getSelectedItemPosition();
                    if (which == 0 && v.length() != 12) {
                        Theme.dialog(this)
                                .setTitle(Lang.t("تنبيه", "Warning", "Attention", "Внимание"))
                                .setMessage(Lang.t("يرجى اعادة التأكد من الرقم الوطني 12 رقم",
                                        "Please re-check the national number — it must be 12 digits",
                                        "Veuillez revérifier le numéro national — 12 chiffres requis",
                                        "Проверьте национальный номер — он должен состоять из 12 цифр"))
                                .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                                .show();
                        return;
                    }
                    String col = which == 0 ? "nationalId"
                            : which == 1 ? "finNumber" : "milNumber";
                    ArrayList<Employee> res = Db.get(this).searchByCol(col, v);
                    if (res.size() == 1) {
                        Intent i = new Intent(this, FormActivity.class);
                        i.putExtra("id", res.get(0).id);
                        startActivity(i);
                    } else if (res.isEmpty()) {
                        toast(Lang.t("لا يوجد سجل بهذا الرقم", "No record with this number",
                                "Aucun enregistrement avec ce numéro", "Нет записи с этим номером"));
                    } else {
                        toast(Lang.t("عدة نتائج — اختر السجل من قاعدة البيانات",
                                        "Multiple results — pick from the database",
                                        "Plusieurs résultats — choisissez dans la base",
                                        "Несколько результатов — выберите в базе данных")
                                + " (" + res.size() + ")");
                    }
                })
                .setNegativeButton(Lang.t("إلغاء", "Cancel", "Annuler", "Отмена"), null)
                .show();
    }

    // ---------- سجل التغييرات عند إعادة فتح التطبيق ----------

    /** مستقبِل عودة الاتصال بالإنترنت: فحص التحديث تلقائياً أثناء تشغيل التطبيق */
    private android.content.BroadcastReceiver connRx;

    @Override
    protected void onStart() {
        super.onStart();
        connRx = Updater.connectivityReceiver(this);
        android.content.IntentFilter f =
                new android.content.IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        if (android.os.Build.VERSION.SDK_INT >= 26)
            registerReceiver(connRx, f, Context.RECEIVER_NOT_EXPORTED);
        else
            registerReceiver(connRx, f);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connRx != null) {
            try {
                unregisterReceiver(connRx);
            } catch (Exception ignored) {
            }
            connRx = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCounts();
        if (!autoChecked) {
            autoChecked = true;
            Updater.autoCheck(this);
        }
        showChangeLog();
    }

    /** نافذة تفصيلية بما أُضيف/عُدّل/حُذف منذ آخر فتح: القديم بالأزرق والجديد بالأحمر + تابع الآن */
    private void showChangeLog() {
        if (changesShown) return;
        changesShown = true;
        final ArrayList<Db.ChangeEntry> list = Db.get(this).unseenChanges();
        if (list.isEmpty()) return;
        Db.get(this).markChangesSeen();

        int blue = th.night ? 0xFF7EB3FF : 0xFF1D4ED8;
        int red = th.night ? 0xFFFF7B72 : 0xFFC62828;
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        box.setPadding(p, dp(8), p, dp(4));

        int shown = Math.min(list.size(), 10);
        for (int i = 0; i < shown; i++) {
            Db.ChangeEntry en = list.get(i);
            LinearLayout c = th.card(this, 16, 12);
            box.addView(c, matchM(10));

            // ترويسة التغيير
            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            ImageView iv = new ImageView(this);
            String ic = en.action.equals("delete") ? "trash"
                    : en.action.equals("edit") ? "edit"
                    : en.action.equals("import") ? "import" : "plus";
            iv.setImageDrawable(th.icon(this, ic, th.onPrimary, 15));
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setBackground(th.gradient(this, 10, th.primary, th.primaryDark));
            iv.setPadding(dp(7), dp(7), dp(7), dp(7));
            head.addView(iv, new LinearLayout.LayoutParams(dp(30), dp(30)));
            LinearLayout ht = new LinearLayout(this);
            ht.setOrientation(LinearLayout.VERTICAL);
            String act = en.action.equals("delete")
                    ? Lang.t("حذف سجل", "Deleted record", "Enregistrement supprimé", "Запись удалена")
                    : en.action.equals("edit")
                    ? Lang.t("تعديل بيانات", "Edited record", "Enregistrement modifié", "Запись изменена")
                    : en.action.equals("import")
                    ? Lang.t("استيراد", "Import", "Importation", "Импорт")
                    : Lang.t("إضافة سجل جديد", "New record", "Nouvel enregistrement", "Новая запись");
            TextView h1 = th.text(this, act, 14, th.primary, true);
            ht.addView(h1);
            TextView h2 = th.text(this, f.format(new Date(en.ts)), 11, th.sub, false);
            ht.addView(h2);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            hp.setMarginEnd(dp(8));
            hp.setMarginStart(dp(6));
            head.addView(ht, hp);
            c.addView(head, matchM(6));

            if (!en.empName.isEmpty()) {
                c.addView(th.text(this, en.empName, 13.5f, th.text, true), matchM(6));
            }

            if (en.action.equals("import")) {
                c.addView(th.text(this,
                        Lang.t("تمت إضافة سجلات جديدة إلى قاعدة البيانات",
                                "New records were added to the database",
                                "De nouveaux enregistrements ont été ajoutés",
                                "Новые записи добавлены в базу данных"),
                        12.5f, th.sub, false), match());
                continue;
            }

            // تفاصيل الحقول: القيمة القديمة بالأزرق والجديدة بالأحمر
            Employee before = en.before, after = en.after;
            if (en.action.equals("edit") && before != null && after != null) {
                for (String col : Employee.COLS) {
                    String o = before.val(col), n = after.val(col);
                    if (col.endsWith("Date")) { o = Employee.isoToDisp(o); n = Employee.isoToDisp(n); }
                    if (!o.equals(n)) {
                        c.addView(diffRow(col, o, Lang.t("قبل", "Before", "Avant", "До"), blue), matchM(3));
                        c.addView(diffRow(col, n, Lang.t("بعد", "After", "Après", "После"), red), match());
                    }
                }
            } else if (en.action.equals("add") && after != null) {
                for (String col : Employee.COLS) {
                    String n = after.val(col);
                    if (n.isEmpty()) continue;
                    if (col.endsWith("Date")) n = Employee.isoToDisp(n);
                    c.addView(diffRow(col, n, Lang.t("أُضيف", "Added", "Ajouté", "Добавлено"), red), match());
                }
            } else if (en.action.equals("delete") && before != null) {
                for (String col : Employee.COLS) {
                    String o = before.val(col);
                    if (o.isEmpty()) continue;
                    if (col.endsWith("Date")) o = Employee.isoToDisp(o);
                    c.addView(diffRow(col, o, Lang.t("حُذف", "Deleted", "Supprimé", "Удалено"), blue), match());
                }
            }
        }
        if (list.size() > shown) {
            box.addView(th.text(this,
                    Lang.t("+ تغييرات أخرى لم تُعرض", "+ More changes not shown",
                            "+ D'autres changements non affichés", "+ Другие изменения не показаны"),
                    12, th.sub, false), match());
        }

        ScrollView sc = new ScrollView(this);
        sc.addView(box);

        final int[] target = {-1};
        for (Db.ChangeEntry en : list) {
            if ((en.action.equals("edit") || en.action.equals("add")) && en.empId > 0) {
                target[0] = en.empId;
                break;
            }
        }

        AlertDialog d = dlg()
                .setTitle(Lang.t("ما الجديد منذ آخر زيارة؟", "What changed since your last visit?",
                        "Quoi de neuf depuis la dernière visite ?", "Что изменилось с последнего визита?"))
                .setView(sc)
                .setPositiveButton(Lang.t("تابع الآن", "Open now", "Ouvrir maintenant", "Открыть сейчас"),
                        (dg, w) -> {
                            if (target[0] > 0) {
                                Intent i = new Intent(this, DetailsActivity.class);
                                i.putExtra("id", target[0]);
                                startActivity(i);
                            } else {
                                toast(Lang.t("السجل المحذوف غير متاح للعرض",
                                        "The deleted record is not available",
                                        "L'enregistrement supprimé n'est pas disponible",
                                        "Удалённая запись недоступна"));
                            }
                        })
                .setNegativeButton(Lang.t("إغلاق", "Close", "Fermer", "Закрыть"), null)
                .show();
        // زر التابع معطل إذا لا يوجد هدف صالح
        if (target[0] < 0) d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    /** سطر فرق: تسمية الحقل + قيمته بلون القديم/الجديد */
    private LinearLayout diffRow(String col, String value, String tag, int color) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setBackground(th.box(this, 10, th.fieldBg, 0, 0));
        int p = dp(8);
        r.setPadding(p, p, p, p);
        TextView l = th.text(this, Employee.labelUi(col), 11.5f, th.sub, true);
        l.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.62f));
        r.addView(l);
        TextView v = th.text(this, tag + ": " + (value.isEmpty() ? "—" : value), 12.5f, color, true);
        v.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.38f));
        r.addView(v);
        return r;
    }

    private void refreshCounts() {
        Db db = Db.get(this);
        int n = db.count();
        int mil = db.countType(Employee.TYPE_MIL);
        int med = db.countType(Employee.TYPE_MED);
        int civ = db.countType(Employee.TYPE_CIV);
        if (vAll != null) vAll.setText(String.valueOf(n));
        if (vMil != null) vMil.setText(String.valueOf(mil));
        if (vMed != null) vMed.setText(String.valueOf(med));
        if (vCiv != null) vCiv.setText(String.valueOf(civ));
    }

    private void renderResults() {
        resultsBox.removeAllViews();
        resTitle.setText(results.isEmpty()
                ? Lang.t("نتائج البحث", "Search results", "Résultats de recherche", "Результаты поиска")
                : Lang.t("نتائج البحث", "Search results", "Résultats de recherche", "Результаты поиска")
                + " (" + results.size() + ")");
        if (results.isEmpty()) {
            TextView t = th.text(this,
                    Lang.t("لا توجد نتائج — استخدم زر البحث بالأعلى، أو أضف موظفاً جديداً",
                            "No results — use the search button above, or add a new employee",
                            "Aucun résultat — utilisez le bouton de recherche ci-dessus",
                            "Нет результатов — используйте поиск выше или добавьте сотрудника"),
                    13, th.sub, false);
            t.setPadding(dp(4), dp(8), dp(4), dp(8));
            resultsBox.addView(t);
            return;
        }
        for (final Employee e : results) {
            resultsBox.addView(employeeCard(e));
        }
    }

    /** بطاقة موظف: شارة دائرية + الاسم + شريحة الرتبة + الأرقام */
    private LinearLayout employeeCard(Employee e) {
        LinearLayout row = th.card(this, 18, 12);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        ImageView av = new ImageView(this);
        av.setImageDrawable(th.icon(this, "person", th.onPrimary, 22));
        av.setScaleType(ImageView.ScaleType.FIT_CENTER);
        av.setBackground(th.gradient(this, 20, th.primary, th.primaryDark));
        av.setPadding(dp(9), dp(9), dp(9), dp(9));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp(44), dp(44));
        ap.setMarginEnd(dp(10));
        row.addView(av, ap);

        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        TextView name = th.text(this, e.displayValue("fullName", false), 15, th.text, true);
        tx.addView(name, matchM(2));
        String nums = "";
        if (!e.val("nationalId").isEmpty())
            nums += Lang.t("الوطني", "Nat.", "Nat.", "Нац.") + ": " + e.val("nationalId");
        if (!e.val("finNumber").isEmpty())
            nums += (nums.isEmpty() ? "" : " • ") + Lang.t("المالي", "Fin.", "Fin.", "Фин.") + ": " + e.val("finNumber");
        if (!e.val("milNumber").isEmpty())
            nums += (nums.isEmpty() ? "" : " • ") + Lang.t("العسكري", "Mil.", "Mil.", "Воен.") + ": " + e.val("milNumber");
        TextView sub = th.text(this, nums.isEmpty() ? "—" : nums, 12, th.sub, false);
        tx.addView(sub, match());
        row.addView(tx, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView rank = th.chip(this, e.displayValue("rank", true), th.fieldBg, th.primary);
        row.addView(rank, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        row.setOnClickListener(v -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra("id", e.id);
            startActivity(i);
        });
        return row;
    }

    /** أداة العرض في وضع الطباعة A4: نتيجة واحدة → عرض مباشر، وإلا اختر من القائمة */
    private void printTool() {
        if (results.size() == 1) {
            Intent i = new Intent(this, PrintPreviewActivity.class);
            i.putExtra("id", results.get(0).id);
            startActivity(i);
        } else {
            Intent i = new Intent(this, ListActivity.class);
            i.putExtra("pick", true);
            startActivity(i);
        }
    }
}
