package com.mmsa.records;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * إدارة البيانات: تصدير واستيراد Excel / SQL / CSV من وإلى الهاتف + النسخ الاحتياطي.
 * الاستيراد يقبل أي ملف من الأنواع الثلاثة ويجاهل المكرر، والقيم غير الموجودة تبقى فارغة.
 */
public class ToolsActivity extends BaseActivity {

    private static final int REQ_IMPORT = 41;
    private static final int REQ_BACKUP = 42;
    private static final int REQ_RESTORE = 43;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(14), dp(14), dp(14), dp(24));
        sc.addView(inner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(sc, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ===== التصدير إلى الهاتف =====
        LinearLayout c1 = th.card(this, 20, 16);
        inner.addView(c1, matchM(12));
        c1.addView(th.sectionHeader(this, "export",
                Lang.t("تصدير البيانات إلى الهاتف", "Export to phone",
                        "Exportation vers le téléphone", "Экспорт на телефон")), matchM(8));
        c1.addView(th.text(this,
                Lang.t("تصدير جميع الموظفين إلى ملف Excel أو SQL أو CSV بترتيب الشيت، وحفظه في مجلد التنزيلات Downloads/MMSA مع فتح خيارات المشاركة.",
                        "Export all employees to Excel, SQL or CSV and save to Downloads/MMSA with sharing options.",
                        "Exportez tous les employés vers Excel, SQL ou CSV et enregistrez dans Downloads/MMSA.",
                        "Экспортируйте всех сотрудников в Excel, SQL или CSV и сохраните в Downloads/MMSA."),
                12.5f, th.sub, false), matchM(10));
        LinearLayout exRow = new LinearLayout(this);
        Button xlsx = th.bigButton(this, "Excel", "export", true);
        Button sql = th.bigButton(this, "SQL", "export", false);
        Button csv = th.bigButton(this, "CSV", "export", false);
        exRow.addView(xlsx, half());
        exRow.addView(sql, half());
        exRow.addView(csv, half());
        xlsx.setOnClickListener(v -> exportAll("xlsx"));
        sql.setOnClickListener(v -> exportAll("sql"));
        csv.setOnClickListener(v -> exportAll("csv"));
        c1.addView(exRow, match());

        // ===== الاستيراد من الهاتف =====
        LinearLayout c2 = th.card(this, 20, 16);
        inner.addView(c2, matchM(12));
        c2.addView(th.sectionHeader(this, "import",
                Lang.t("استيراد من الهاتف", "Import from phone",
                        "Importation du téléphone", "Импорт с телефона")), matchM(8));
        c2.addView(th.text(this,
                Lang.t("استيراد الموظفين من أي ملف Excel أو SQL أو CSV حتى لو لم يُصدَّر من التطبيق، وإضافتها إلى قاعدة بيانات التطبيق. يتعرّف الاستيراد الذكي على عناوين الأعمدة تلقائياً بالعربية بأي صيغة أو بالإنجليزية أو الفرنسية أو الروسية، ويكتشف صف العناوين والورقة المناسبة بنفسه، ويوحّد القيم القياسية (الصفة والرتبة والدرجة والعلاوات والمؤهلات) لتطابق نصوص التطبيق، ويحوّل تواريخ Excel التسلسلية تلقائياً. القيم الفارغة تبقى فارغاً والمكرر يُتجاهل تلقائياً.",
                        "Import employees from any Excel, SQL or CSV file — even one not exported from this app. Smart import recognizes column headers automatically (Arabic in any form, English, French or Russian), detects the header row and the right sheet by itself, maps standard values (category, rank, grade, increments, qualifications) to the app's exact texts, and converts Excel serial dates automatically. Missing values stay empty and duplicates are skipped.",
                        "Importez des employés depuis n'importe quel fichier Excel, SQL ou CSV. L'import intelligent reconnaît les en-têtes (arabe, anglais, français ou russe), détecte la ligne d'en-têtes et la bonne feuille, aligne les valeurs standard sur les textes de l'application et convertit les dates Excel. Les valeurs manquantes restent vides et les doublons sont ignorés.",
                        "Импорт сотрудников из любого файла Excel, SQL или CSV. Умный импорт распознаёт заголовки (арабский, английский, французский, русский), сам находит строку заголовков и нужный лист, приводит стандартные значения к текстам приложения и преобразует серийные даты Excel. Пустые значения остаются пустыми, дубликаты пропускаются."),
                12.5f, th.sub, false), matchM(10));
        Button imp = th.bigButton(this,
                Lang.t("اختيار ملف واستيراد (Excel / SQL / CSV)",
                        "Pick a file and import (Excel / SQL / CSV)",
                        "Choisir un fichier et importer (Excel / SQL / CSV)",
                        "Выбрать файл и импортировать (Excel / SQL / CSV)"),
                "import", true);
        imp.setOnClickListener(v -> pickImport());
        c2.addView(imp, match());

        // ===== النسخ الاحتياطي =====
        LinearLayout c3 = th.card(this, 20, 16);
        inner.addView(c3, matchM(12));
        c3.addView(th.sectionHeader(this, "backup",
                Lang.t("النسخ الاحتياطي", "Backup", "Sauvegarde", "Резервная копия")), matchM(8));
        c3.addView(th.text(this,
                Lang.t("نسخة كاملة من قاعدة البيانات (SQLite) داخل ذاكرة الهاتف — احفظها في مكان آمن ويمكن استعادتها على أي هاتف.",
                        "A full database backup (SQLite) — save it somewhere safe and restore it on any phone.",
                        "Une sauvegarde complète de la base (SQLite) — conservez-la en lieu sûr.",
                        "Полная резервная копия базы (SQLite) — сохраните в надёжном месте."),
                12.5f, th.sub, false), matchM(10));
        Button bak = th.bigButton(this,
                Lang.t("إنشاء نسخة احتياطية", "Create backup", "Créer une sauvegarde", "Создать копию"),
                "backup", true);
        bak.setOnClickListener(v -> pickBackup());
        c3.addView(bak, matchM(6));
        Button res = th.bigButton(this,
                Lang.t("استعادة نسخة احتياطية", "Restore backup", "Restaurer", "Восстановить"),
                "restore", false);
        res.setOnClickListener(v -> pickRestore());
        c3.addView(res, match());

        page(Lang.t("إدارة البيانات", "Data management", "Gestion des données", "Управление данными"),
                body, null);
    }

    private String stamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date(System.currentTimeMillis()));
    }

    /** تصدير بالصيغة المطلوبة: xlsx / sql / csv */
    private void exportAll(String kind) {
        toast(Lang.t("جارٍ التصدير…", "Exporting…", "Exportation…", "Экспорт…"));
        new Thread(() -> {
            try {
                List<Employee> all = Db.get(this).all();
                List<String[]> rows = new ArrayList<>();
                for (Employee e : all) rows.add(e.exportRow());
                File dir = MiniFileProvider.exportsDir(this);
                File f;
                String mime;
                if (kind.equals("xlsx")) {
                    f = new File(dir, "MMSA_employees_" + stamp() + ".xlsx");
                    SheetIO.writeXlsx(f, Employee.EXPORT_HEADERS, rows);
                    mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                } else if (kind.equals("sql")) {
                    f = new File(dir, "MMSA_employees_" + stamp() + ".sql");
                    SqlIO.writeSql(f, all);
                    mime = "application/sql";
                } else {
                    f = new File(dir, "MMSA_employees_" + stamp() + ".csv");
                    SheetIO.writeCsv(f, Employee.EXPORT_HEADERS, rows);
                    mime = "text/csv";
                }
                File ff = f;
                String mm = mime;
                runOnUiThread(() -> MiniFileProvider.exportAndShare(this, ff, mm));
            } catch (Exception ex) {
                runOnUiThread(() -> toast(Lang.t("تعذر التصدير: ", "Export failed: ",
                        "Échec de l'export : ", "Ошибка экспорта: ") + ex.getMessage()));
            }
        }).start();
    }

    private void pickImport() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel", "text/csv", "text/comma-separated-values",
                "application/sql", "text/plain", "application/octet-stream"});
        startActivityForResult(i, REQ_IMPORT);
    }

    private void pickBackup() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_TITLE, "MMSA_backup_" + stamp() + ".db");
        startActivityForResult(i, REQ_BACKUP);
    }

    private void pickRestore() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i, REQ_RESTORE);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (data == null || data.getData() == null || res != RESULT_OK) return;
        Uri uri = data.getData();
        if (req == REQ_IMPORT) doImport(uri);
        else if (req == REQ_BACKUP) doBackup(uri);
        else if (req == REQ_RESTORE) confirmRestore(uri);
    }

    private String fileName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {}
        return "";
    }

    private void doImport(Uri uri) {
        toast(Lang.t("جارٍ قراءة الملف…", "Reading file…", "Lecture du fichier…", "Чтение файла…"));
        new Thread(() -> {
            try {
                String name = fileName(uri).toLowerCase();
                List<String[]> rows;
                boolean fromSql = false;
                try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
                    if (name.endsWith(".sql")) {
                        // ملف SQL: تحليل عبارات INSERT وإخراجها بترتيب أعمدة الشيت
                        SqlIO.SqlRows sq = SqlIO.readSql(in);
                        rows = sq.rows;
                        fromSql = true;
                        if (rows.isEmpty()) {
                            final String msg = sq.foundColumns
                                    ? Lang.t("لم يُعثر على صفوف صالحة داخل ملف SQL — تأكد من عبارات INSERT.",
                                            "No valid rows found in the SQL file — check the INSERT statements.",
                                            "Aucune ligne valide dans le fichier SQL — vérifiez les instructions INSERT.",
                                            "В SQL-файле нет допустимых строк — проверьте INSERT.")
                                    : Lang.t("لم يُعثر على عبارات INSERT داخل ملف SQL.",
                                            "No INSERT statements found in the SQL file.",
                                            "Aucune instruction INSERT dans le fichier SQL.",
                                            "В SQL-файле нет INSERT-выражений.");
                            runOnUiThread(() -> Theme.dialog(this)
                                    .setTitle(Lang.t("تعذر الاستيراد", "Import failed",
                                            "Échec de l'import", "Ошибка импорта"))
                                    .setMessage(msg)
                                    .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                                    .show());
                            return;
                        }
                    } else if (name.endsWith(".csv")) {
                        rows = SheetIO.readCsv(in);
                    } else {
                        try {
                            // كل الأوراق — يختار تلقائياً الورقة الأقرب لجدول الموظفين
                            rows = bestSheet(SheetIO.readXlsxAll(in));
                        } catch (Exception x) {
                            try (java.io.InputStream in2 = getContentResolver().openInputStream(uri)) {
                                rows = SheetIO.readCsv(in2);
                            } catch (Exception x2) {
                                try (java.io.InputStream in3 = getContentResolver().openInputStream(uri)) {
                                    SqlIO.SqlRows sq = SqlIO.readSql(in3);
                                    rows = sq.rows;
                                    fromSql = true;
                                }
                            }
                        }
                    }
                }
                ArrayList<Employee> candidates = fromSql ? mapSqlRows(rows) : mapRows(rows);
                int added = 0, dup = 0, bad = 0;
                String errMsg = null;
                if (candidates == null) {
                    errMsg = Lang.t("لم يتم التعرف على عناوين الأعمدة في هذا الملف. الاستيراد الذكي يقبل العناوين بالعربية بأي صيغة (الاسم الرباعي، الرقم الوطني، الرقم المالي، الرقم العسكري…) أو بالإنجليزية أو الفرنسية أو الروسية، ويتحمل وجود عنوان تقرير قبل صف العناوين. تأكد أن الملف يحتوي صفاً واضحاً لأسماء الأعمدة، وعموداً للاسم الرباعي، ورقماً واحداً على الأقل (مالي أو عسكري أو وطني).",
                            "Column headers not recognized in this file. Smart import accepts headers in Arabic in any form (Full name, National no., Job no., Military no.…) or in English, French or Russian, and tolerates a report title above the header row. Make sure the file has a clear header row with a full-name column and at least one number column (job/military/national).",
                            "En-têtes non reconnus. L'import intelligent accepte les en-têtes en arabe (nom complet, n° national, n° d'emploi, n° militaire…) ou en anglais, français ou russe, et tolère un titre au-dessus. Le fichier doit contenir une ligne d'en-têtes claire avec un nom complet et au moins un numéro.",
                            "Заголовки не распознаны. Умный импорт принимает заголовки на арабском (полное имя, национальный номер, рабочий номер, военный номер…), английском, французском или русском, и допускает титульную строку. В файле должна быть строка заголовков с полным именем и хотя бы одним номером.");
                } else {
                    int[] r = Db.get(this).importRows(candidates);
                    added = r[0]; dup = r[1]; bad = r[2];
                }
                final String fErr = errMsg;
                final int fa = added, fd = dup, fb = bad;
                runOnUiThread(() -> {
                    if (fErr != null) {
                        Theme.dialog(this)
                                .setTitle(Lang.t("تعذر الاستيراد", "Import failed",
                                        "Échec de l'import", "Ошибка импорта"))
                                .setMessage(fErr)
                                .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                                .show();
                    } else {
                        Theme.dialog(this)
                                .setTitle(Lang.t("نتيجة الاستيراد", "Import result",
                                        "Résultat de l'import", "Результат импорта"))
                                .setMessage(Lang.t("أُضيف", "Added", "Ajoutés", "Добавлено") + ": " + fa + "\n"
                                        + Lang.t("تجاهُل (مكرر)", "Skipped (duplicates)", "Ignorés (doublons)", "Пропущено (дубликаты)") + ": " + fd
                                        + "\n" + Lang.t("أسطر غير صالحة", "Invalid rows", "Lignes invalides", "Недопустимые строки") + ": " + fb)
                                .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                                .show();
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(Lang.t("تعذر الاستيراد: ", "Import failed: ",
                        "Échec de l'import : ", "Ошибка импорта: ") + ex.getMessage()));
            }
        }).start();
    }

    /** مطابقة عناوين الأعمدة بأي لغة/صيغة مع المفاتيح القياسية */
    private static String matchKey(String header) {
        return Employee.matchHeader(header);
    }

    /** عدد العناوين المعروفة في صف — لاختيار صف العناوين وأفضل ورقة */
    private static int headerScore(String[] row) {
        if (row == null) return 0;
        int score = 0;
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (String cell : row) {
            String k = matchKey(cell);
            if (k != null && !k.equals("id") && seen.add(k)) score++;
        }
        return score;
    }

    /** اختيار الورقة الأقرب لجدول الموظفين من ملف متعدد الأوراق */
    private static List<String[]> bestSheet(List<List<String[]>> sheets) {
        if (sheets == null || sheets.isEmpty()) return null;
        List<String[]> best = null;
        int bestScore = -1;
        for (List<String[]> sh : sheets) {
            if (sh == null || sh.isEmpty()) continue;
            int score = 0;
            for (int i = 0; i < Math.min(sh.size(), 30); i++) {
                int s = headerScore(sh.get(i));
                if (s > score) score = s;
            }
            if (score > bestScore) { bestScore = score; best = sh; }
        }
        return best;
    }

    /** تطبيع القيم حسب نوع العمود: أرقام فقط للأرقام، وتاريخ ISO للتواريخ،
     *  وتاريخ Excel التسلسلي رقمياً يتحول لتاريخ، والقيم القياسية توحّد نصوصها */
    private static String normalize(String key, String v) {
        if (v == null) return "";
        v = v.trim();
        if (v.isEmpty()) return "";
        if (key.equals("finNumber") || key.equals("milNumber") || key.equals("nationalId")
                || key.equals("bankAccount")) {
            return Employee.digitsOnly(v);
        }
        if (key.endsWith("Date")) {
            // خلية تاريخ في Excel مخزنة كرقم تسلسلي (مثل 43831) — تحويلها لتاريخ
            String serial = Employee.excelSerialToIso(v);
            if (!serial.equals(v)) return serial;
            return Employee.anyToIso(v);
        }
        // القيم القياسية تُوحَّد لتطابق نصوص التطبيق، والنصوص الحرة تبقى كما هي
        return Employee.canonicalValue(key, v);
    }

    /** صفوف Excel/CSV → موظفون — يكتشف صف العناوين تلقائياً حتى لو سبقه عنوان تقرير */
    private ArrayList<Employee> mapRows(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) return null;
        // صف العناوين: الصف الأعلى مطابقةً ضمن أول 30 صفاً (يتحمل عنوان تقرير قبله)
        int hdr = -1, bestScore = 0;
        for (int i = 0; i < Math.min(rows.size(), 30); i++) {
            int s = headerScore(rows.get(i));
            if (s > bestScore) { bestScore = s; hdr = i; }
        }
        if (hdr < 0 || bestScore < 3) return null;
        String[] header = rows.get(hdr);
        java.util.HashSet<String> assigned = new java.util.HashSet<>();
        int[] keyIdx = new int[header.length];
        for (int i = 0; i < header.length; i++) {
            String k = matchKey(header[i]);
            keyIdx[i] = k == null ? -1 : indexOfKey(k);
            // أول عمود يفوز إن تكرر نفس المفتاح في عمودين
            if (keyIdx[i] >= 0 && !assigned.add(Employee.EXPORT_KEYS[keyIdx[i]])) keyIdx[i] = -1;
        }
        // كشف عمود الرتب العسكرية من قيمه إن كان عنوانه غير معروف أو معروضاً خاطئاً
        remapRankColumn(rows, hdr, keyIdx);
        ArrayList<Employee> out = new ArrayList<>();
        for (int r = hdr + 1; r < rows.size(); r++) {
            String[] row = rows.get(r);
            if (row == null) continue;
            boolean anyVal = false;
            for (String cell : row) if (cell != null && !cell.trim().isEmpty()) { anyVal = true; break; }
            if (!anyVal) continue;
            Employee e = new Employee();
            e.id = -1;
            for (int i = 0; i < header.length && i < row.length; i++) {
                int k = keyIdx[i];
                if (k < 0) continue;
                String key = Employee.EXPORT_KEYS[k];
                if (key.equals("id")) continue;
                String v = normalize(key, row[i] == null ? "" : row[i]);
                if (v.isEmpty()) continue; // القيمة الفارغة تبقى فارغة
                e.d.put(key, v);
            }
            if (e.val("empType").isEmpty())
                e.d.put("empType", e.val("milNumber").isEmpty()
                        ? Employee.TYPE_CIV : Employee.TYPE_MIL);
            applyMilitary(e);
            out.add(e);
        }
        return out;
    }

    /** إن احتوى عمود على رتب عسكرية (جندي، عريف، ر.ع.و…) فاعتبره عمود الرتبة:
     *  يتجاوز العرض الخاطئ لعنوانه كرقم مالي أو مجهول، لكن لا يمس عمود الاسم أو الوطني */
    private void remapRankColumn(List<String[]> rows, int hdr, int[] keyIdx) {
        int rankIdx = indexOfKey("rank");
        if (rankIdx < 0 || keyIdx.length == 0) return;
        int nCols = 0;
        for (String[] row : rows) if (row != null) nCols = Math.max(nCols, row.length);
        int best = -1, bestHits = 1; // يلزم رتبتان على الأقل في العينة
        for (int col = 0; col < nCols; col++) {
            int hits = 0;
            for (int r = hdr + 1; r < Math.min(rows.size(), hdr + 51); r++) {
                String[] row = rows.get(r);
                if (row != null && col < row.length && Employee.isMilitaryRank(row[col])) hits++;
            }
            if (hits > bestHits) { bestHits = hits; best = col; }
        }
        if (best < 0) return;
        int cur = best < keyIdx.length ? keyIdx[best] : -1;
        if (cur == rankIdx) return; // عنوانه عُرف صحيحاً كرتبة
        if (cur < 0 || Employee.EXPORT_KEYS[cur].equals("finNumber")
                || Employee.EXPORT_KEYS[cur].equals("milNumber")
                || Employee.EXPORT_KEYS[cur].equals("id"))
            keyIdx[best] = rankIdx;
    }

    /** من يحمل صفة عسكرية (أو رتبة عسكرية): الصفة = عسكري ورقمه في الخانة العسكرية */
    private static void applyMilitary(Employee e) {
        Employee.applyMilitaryNumber(e);
    }

    /** صفوف SQL (بترتيب أعمدة الشيت) → موظفون */
    private ArrayList<Employee> mapSqlRows(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) return null;
        ArrayList<Employee> out = new ArrayList<>();
        for (String[] row : rows) {
            if (row == null) continue;
            boolean anyVal = false;
            for (String cell : row) if (cell != null && !cell.trim().isEmpty()) { anyVal = true; break; }
            if (!anyVal) continue;
            Employee e = new Employee();
            e.id = -1;
            for (int i = 0; i < Employee.EXPORT_KEYS.length && i < row.length; i++) {
                String key = Employee.EXPORT_KEYS[i];
                if (key.equals("id")) continue;
                String v = normalize(key, row[i]);
                if (v.isEmpty()) continue;
                e.d.put(key, v);
            }
            if (e.val("empType").isEmpty())
                e.d.put("empType", e.val("milNumber").isEmpty()
                        ? Employee.TYPE_CIV : Employee.TYPE_MIL);
            applyMilitary(e);
            out.add(e);
        }
        return out;
    }

    private static int indexOfKey(String key) {
        for (int i = 0; i < Employee.EXPORT_KEYS.length; i++)
            if (Employee.EXPORT_KEYS[i].equals(key)) return i;
        return -1;
    }

    private void doBackup(Uri uri) {
        android.os.ParcelFileDescriptor pfd = null;
        try {
            Db.get(this).checkpoint();
            pfd = getContentResolver().openFileDescriptor(uri, "w");
            try (FileInputStream in = new FileInputStream(Db.get(this).dbPath())) {
                try (FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                    MiniFileProvider.copy(in, out);
                }
            }
            toast(Lang.t("تم إنشاء النسخة الاحتياطية بنجاح", "Backup created successfully",
                    "Sauvegarde créée avec succès", "Резервная копия создана"));
        } catch (Exception ex) {
            toast(Lang.t("تعذر الحفظ: ", "Save failed: ", "Échec de l'enregistrement : ",
                    "Ошибка сохранения: ") + ex.getMessage());
        } finally {
            if (pfd != null) {
                try { pfd.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void confirmRestore(Uri uri) {
        confirm(Lang.t("استعادة نسخة احتياطية", "Restore backup", "Restaurer la sauvegarde", "Восстановить копию"),
                Lang.t("سيتم استبدال جميع البيانات الحالية بمحتوى النسخة المختارة. هل تريد المتابعة؟",
                        "All current data will be replaced by the selected backup. Continue?",
                        "Toutes les données actuelles seront remplacées. Continuer ?",
                        "Все текущие данные будут заменены выбранной копией. Продолжить?"),
                () -> {
                    try {
                        File dbf = new File(Db.get(this).dbPath());
                        Db.reset(); // إغلاق القاعدة قبل الاستبدال
                        byte[] buf = new byte[8192];
                        try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                             FileOutputStream out = new FileOutputStream(dbf)) {
                            int n;
                            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                        }
                        new File(dbf.getAbsolutePath() + "-wal").delete();
                        new File(dbf.getAbsolutePath() + "-shm").delete();
                        toast(Lang.t("تمت الاستعادة — إعادة تشغيل التطبيق",
                                "Restored — restarting the app",
                                "Restauré — redémarrage de l'application",
                                "Восстановлено — перезапуск приложения"));
                        Intent i = new Intent(this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finishAffinity();
                    } catch (Exception ex) {
                        toast(Lang.t("تعذرت الاستعادة: ", "Restore failed: ",
                                "Échec de la restauration : ", "Ошибка восстановления: ") + ex.getMessage());
                    }
                });
    }
}
