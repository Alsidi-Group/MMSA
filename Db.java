package com.mmsa.records;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/** قاعدة البيانات المحلية SQLite داخل ذاكرة الهاتف + منع التكرار بالمفاتيح */
public class Db extends SQLiteOpenHelper {

    public static final String DB_NAME = "mmsa.db";
    private static Db inst;

    public static synchronized Db get(Context c) {
        if (inst == null) inst = new Db(c.getApplicationContext());
        return inst;
    }

    /** إغلاق القاعدة قبل الاستعادة */
    public static synchronized void reset() {
        if (inst != null) {
            try { inst.close(); } catch (Exception ignored) {}
            inst = null;
        }
    }

    private Db(Context c) {
        super(c, DB_NAME, null, 5);
        ctx = c.getApplicationContext();
    }

    private final Context ctx;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE employees ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "empType TEXT, finNumber TEXT, milNumber TEXT, rank TEXT, nationalId TEXT, "
                + "fullName TEXT, unit TEXT, joinDate TEXT, appointDate TEXT, appointDecree TEXT, "
                + "lastPromotion TEXT, dueDate TEXT, grade TEXT, "
                + "qualification TEXT, techQual TEXT, specialty TEXT, annualAllowance TEXT, "
                + "bankName TEXT, bankBranch TEXT, "
                + "bankAccount TEXT, salary TEXT, createdAt INTEGER, updatedAt INTEGER)");
        db.execSQL(CREATE_CHANGELOG);
    }

    private static final String CREATE_CHANGELOG =
            "CREATE TABLE IF NOT EXISTS change_log ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "action TEXT, empId INTEGER, empName TEXT, "
                    + "beforeJson TEXT, afterJson TEXT, ts INTEGER, seen INTEGER DEFAULT 0)";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL(CREATE_CHANGELOG);
        // ترحيل 1.2.0 → 1.3.0: أعمدة المؤهلات والتخصص والعلاوات السنوية
        String[] addCols = {
                "ALTER TABLE employees ADD COLUMN qualification TEXT",
                "ALTER TABLE employees ADD COLUMN techQual TEXT",
                "ALTER TABLE employees ADD COLUMN specialty TEXT",
                "ALTER TABLE employees ADD COLUMN annualAllowance TEXT"
        };
        for (String sql : addCols) {
            try {
                db.execSQL(sql);
            } catch (Exception ignored) { // العمود موجود مسبقاً
            }
        }
        // ترحيل 1.3.1 → 1.4.0: تحويل مسميات الرتب المخزنة إلى الصيغ المختصرة
        // (رئيس عرفة وحدة ← ر.ع.و، رئيس عرفة سرية ← ر.ع.س، نائب عريف ← ن.ع)
        for (String[] m : Employee.RANK_MIGRATIONS) {
            try {
                db.execSQL("UPDATE employees SET rank=? WHERE rank=?",
                        new Object[]{m[1], m[0]});
            } catch (Exception ignored) {
            }
        }
        // ترحيل 1.4.0 → 1.4.1: من يحمل صفة عسكرية ينتقل رقمه
        // من خانة الرقم الوظيفي (المالي) إلى خانة الرقم العسكري إن كانت فارغة
        db.execSQL("UPDATE employees SET milNumber=finNumber, finNumber='' "
                        + "WHERE empType=? AND (milNumber IS NULL OR milNumber='') "
                        + "AND finNumber IS NOT NULL AND finNumber != ''",
                new Object[]{Employee.TYPE_MIL});
    }

    public String dbPath() {
        return ctx.getDatabasePath(DB_NAME).getAbsolutePath();
    }

    /** دمج محتوى WAL في الملف الرئيسي قبل النسخ الاحتياطي */
    public void checkpoint() {
        try {
            getWritableDatabase().execSQL("PRAGMA wal_checkpoint(FULL)");
        } catch (Exception ignored) {}
    }

    private ContentValues cv(Employee e) {
        ContentValues v = new ContentValues();
        for (String col : Employee.COLS) v.put(col, e.val(col));
        return v;
    }

    private static boolean empty(Employee e, String k) {
        return e.val(k).trim().isEmpty();
    }

    /** التحقق من الإلزامية ومنع التكرار عبر (الرقم الوطني / المالي / العسكري). يُعيد رسالة خطأ أو null */
    public String validate(Employee e) {
        if (empty(e, "fullName"))
            return Lang.t("الاسم الرباعي إلزامي", "Full name is required",
                    "Le nom complet est obligatoire", "Полное имя обязательно");
        if (empty(e, "rank"))
            return Lang.t("الرتبة إلزامية", "Rank is required",
                    "Le grade est obligatoire", "Звание обязательно");
        if (empty(e, "finNumber") && empty(e, "milNumber") && empty(e, "nationalId"))
            return Lang.t("أدخل أحد مفاتيح عدم التكرار: الرقم الوطني أو رقم الوظيفي (المالي) أو الرقم العسكري",
                    "Enter one unique key: national number, job (financial) number or military number",
                    "Entrez une clé unique : numéro national, d'emploi (financier) ou militaire",
                    "Введите один уникальный ключ: национальный, рабочий (финансовый) или военный номер");
        return findDup(e);
    }

    /** البحث عن تكرار في المفاتيح الثلاثة مع استثناء السجل الحالي */
    public String findDup(Employee e) {
        String[] keys = {"nationalId", "finNumber", "milNumber"};
        SQLiteDatabase db = getReadableDatabase();
        for (String col : keys) {
            String v = e.val(col).trim();
            if (v.isEmpty()) continue;
            Cursor c = db.query("employees", new String[]{"fullName"},
                    col + "=? AND id<>?", new String[]{v, String.valueOf(e.id)},
                    null, null, null);
            String err = null;
            if (c.moveToFirst()) {
                err = Employee.labelUi(col) + " («" + v + "») "
                        + Lang.t("مسجّل مسبقاً للموظف: ", "is already registered for employee: ",
                        "est déjà enregistré pour : ", "уже зарегистрирован для: ")
                        + c.getString(0);
            }
            c.close();
            if (err != null) return err;
        }
        return null;
    }

    public boolean insert(Employee e) {
        if (findDup(e) != null) return false;
        long now = System.currentTimeMillis();
        e.createdAt = now;
        e.updatedAt = now;
        ContentValues v = cv(e);
        v.put("createdAt", now);
        v.put("updatedAt", now);
        long r = getWritableDatabase().insert("employees", null, v);
        if (r > 0) e.id = (int) r;
        return r > 0;
    }

    public boolean update(Employee e) {
        if (findDup(e) != null) return false;
        e.updatedAt = System.currentTimeMillis();
        ContentValues v = cv(e);
        v.put("updatedAt", e.updatedAt);
        return getWritableDatabase().update("employees", v, "id=?",
                new String[]{String.valueOf(e.id)}) > 0;
    }

    public int delete(int id) {
        return getWritableDatabase().delete("employees", "id=?",
                new String[]{String.valueOf(id)});
    }

    private static final String SEL =
            "SELECT id," + joinCols() + ",createdAt,updatedAt FROM employees";

    private static String joinCols() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < Employee.COLS.length; i++) {
            if (i > 0) b.append(",");
            b.append(Employee.COLS[i]);
        }
        return b.toString();
    }

    private Employee fromCursor(Cursor c) {
        Employee e = new Employee();
        e.id = c.getInt(0);
        for (int i = 0; i < Employee.COLS.length; i++) {
            String v = c.getString(i + 1);
            e.d.put(Employee.COLS[i], v == null ? "" : v);
        }
        e.createdAt = c.getLong(Employee.COLS.length + 1);
        e.updatedAt = c.getLong(Employee.COLS.length + 2);
        return e;
    }

    public Employee get(int id) {
        Cursor c = getReadableDatabase().rawQuery(SEL + " WHERE id=?",
                new String[]{String.valueOf(id)});
        Employee e = null;
        if (c.moveToFirst()) e = fromCursor(c);
        c.close();
        return e;
    }

    public ArrayList<Employee> all() {
        Cursor c = getReadableDatabase().rawQuery(SEL + " ORDER BY fullName", null);
        ArrayList<Employee> out = new ArrayList<>();
        while (c.moveToNext()) out.add(fromCursor(c));
        c.close();
        return out;
    }

    public ArrayList<Employee> searchName(String q) {
        Cursor c = getReadableDatabase().rawQuery(SEL + " WHERE fullName LIKE ? ORDER BY fullName",
                new String[]{"%" + q + "%"});
        ArrayList<Employee> out = new ArrayList<>();
        while (c.moveToNext()) out.add(fromCursor(c));
        c.close();
        return out;
    }

    /** أعمدة البحث المسموح بها فقط (حماية من حقن اسم العمود) */
    private static final java.util.Set<String> SEARCHABLE = new java.util.HashSet<>(
            java.util.Arrays.asList("nationalId", "finNumber", "milNumber"));

    /** مطابقة على عمود محدد (nationalId / finNumber / milNumber فقط):
     *  مطابقة تامة أولاً، وإلا مطابقة جزئية لإصلاح فروق التخزين والمسافات */
    public ArrayList<Employee> searchByCol(String col, String v) {
        ArrayList<Employee> out = new ArrayList<>();
        if (!SEARCHABLE.contains(col)) return out; // رفض أي عمود غير مسموح
        String val = v.trim();
        Cursor c = getReadableDatabase().rawQuery(
                SEL + " WHERE " + col + "=? OR " + col + " LIKE ? ORDER BY fullName",
                new String[]{val, "%" + val + "%"});
        while (c.moveToNext()) out.add(fromCursor(c));
        c.close();
        return out;
    }

    /** بحث عام في الاسم/الأرقام/الرتبة (للتصفية في عرض الكل) */
    public ArrayList<Employee> searchAll(String q) {
        String like = "%" + q + "%";
        Cursor c = getReadableDatabase().rawQuery(SEL
                        + " WHERE fullName LIKE ? OR nationalId LIKE ? OR finNumber LIKE ? OR milNumber LIKE ? OR rank LIKE ?"
                        + " ORDER BY fullName",
                new String[]{like, like, like, like, like});
        ArrayList<Employee> out = new ArrayList<>();
        while (c.moveToNext()) out.add(fromCursor(c));
        c.close();
        return out;
    }

    public int count() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM employees", null);
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public int countType(String t) {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM employees WHERE empType=?",
                new String[]{t});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public LinkedHashMap<String, Integer> rankCounts() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT rank, COUNT(*) FROM employees GROUP BY rank ORDER BY COUNT(*) DESC", null);
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        while (c.moveToNext()) {
            String r = c.getString(0);
            if (r == null || r.trim().isEmpty())
                r = Lang.t("بدون رتبة", "No rank", "Sans grade", "Без звания");
            out.put(r, c.getInt(1));
        }
        c.close();
        return out;
    }

    public Employee lastAdded() {
        Cursor c = getReadableDatabase().rawQuery(SEL + " ORDER BY id DESC LIMIT 1", null);
        Employee e = null;
        if (c.moveToFirst()) e = fromCursor(c);
        c.close();
        return e;
    }

    /** استيراد دفعة سجلات مع تجاهل المكرر. يُعيد [مضاف، مكرر، غير صالح] */
    public int[] importRows(ArrayList<Employee> list) {
        int added = 0, dup = 0, bad = 0;
        for (Employee e : list) {
            if (empty(e, "fullName")) { bad++; continue; }
            if (empty(e, "finNumber") && empty(e, "milNumber") && empty(e, "nationalId")) { bad++; continue; }
            if (findDup(e) != null) { dup++; continue; }
            if (insert(e)) added++; else dup++;
        }
        if (added > 0) {
            // سجل واحد موحد لعملية الاستيراد بدل سجل لكل صف
            Employee s = new Employee();
            s.id = -1;
            s.d.put("fullName", Lang.t("استيراد دفعة سجلات", "Batch import", "Importation groupée", "Пакетный импорт")
                    + " (" + added + ")");
            recordChange("import", s, null, null);
        }
        return new int[]{added, dup, bad};
    }

    // ---------- سجل التغييرات: ما أُضيف/عُدّل/حُذف يظهر عند إعادة فتح التطبيق ----------

    /** تسجيل تغيير واحد في جدول change_log (غير مقروء حتى يعرضه التطبيق) */
    public void recordChange(String action, Employee e, Employee before, Employee after) {
        try {
            ContentValues v = new ContentValues();
            v.put("action", action);
            v.put("empId", e.id);
            v.put("empName", e.val("fullName"));
            v.put("beforeJson", before != null ? before.toJson() : null);
            v.put("afterJson", after != null ? after.toJson() : null);
            v.put("ts", System.currentTimeMillis());
            v.put("seen", 0);
            getWritableDatabase().insert("change_log", null, v);
        } catch (Exception ignored) {}
    }

    public static class ChangeEntry {
        public long id;
        public String action;      // add / edit / delete
        public int empId;
        public String empName;
        public String beforeJson, afterJson;
        public long ts;
        public Employee before, after;
    }

    /** كل التغييرات غير المقروءة (الأحدث أولاً) */
    public ArrayList<ChangeEntry> unseenChanges() {
        ArrayList<ChangeEntry> out = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,action,empId,empName,beforeJson,afterJson,ts FROM change_log "
                        + "WHERE seen=0 ORDER BY id DESC LIMIT 50", null);
        while (c.moveToNext()) {
            ChangeEntry en = new ChangeEntry();
            en.id = c.getLong(0);
            en.action = c.getString(1);
            en.empId = c.getInt(2);
            en.empName = c.getString(3) == null ? "" : c.getString(3);
            en.beforeJson = c.getString(4);
            en.afterJson = c.getString(5);
            en.ts = c.getLong(6);
            en.before = Employee.fromJson(en.beforeJson);
            en.after = Employee.fromJson(en.afterJson);
            out.add(en);
        }
        c.close();
        return out;
    }

    /** تعليم كل التغييرات كمقروءة بعد عرضها */
    public void markChangesSeen() {
        try {
            getWritableDatabase().execSQL("UPDATE change_log SET seen=1 WHERE seen=0");
        } catch (Exception ignored) {}
    }

    // ---------- الفرز ----------

    /** أعمدة الفرز المسموح بها فقط (حماية من حقن اسم العمود) */
    public static final java.util.Set<String> SORTABLE = new java.util.HashSet<>(java.util.Arrays.asList(
            "id", "fullName", "finNumber", "milNumber", "rank", "nationalId", "bankName",
            "bankBranch", "unit", "appointDate", "lastPromotion", "appointDecree"));

    /** كل الموظفين بترتيب مخصص (عمود + تصاعدي/تنازلي) */
    public ArrayList<Employee> allSorted(String col, boolean desc) {
        if (!SORTABLE.contains(col)) col = "fullName";
        String dir = desc ? "DESC" : "ASC";
        Cursor c = getReadableDatabase().rawQuery(
                SEL + " ORDER BY " + col + " COLLATE NOCASE " + dir + ", id " + dir, null);
        ArrayList<Employee> out = new ArrayList<>();
        while (c.moveToNext()) out.add(fromCursor(c));
        c.close();
        return out;
    }
}
