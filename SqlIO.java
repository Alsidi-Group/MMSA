package com.mmsa.records;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * قراءة وكتابة ملفات SQL Dump — Java خالص.
 * التصدير: CREATE TABLE + INSERT INTO.
 * الاستيراد: تحليل عبارات INSERT مع دعم الأعمدة المسماة وتجاوز الأعمدة غير المعروفة،
 * والقيم غير الموجودة تبقى فارغة كما هي.
 */
public class SqlIO {

    // ---------- كتابة SQL ----------

    public static void writeSql(File out, List<Employee> rows) throws IOException {
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8);
        w.write("-- MMSA employees SQL dump\n");
        w.write("-- Encoding: UTF-8\n\n");
        w.write("CREATE TABLE IF NOT EXISTS employees (\n"
                + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "  empType TEXT,\n  finNumber TEXT,\n  milNumber TEXT,\n  rank TEXT,\n"
                + "  nationalId TEXT,\n  fullName TEXT,\n  unit TEXT,\n  joinDate TEXT,\n"
                + "  appointDate TEXT,\n  appointDecree TEXT,\n  lastPromotion TEXT,\n"
                + "  dueDate TEXT,\n  grade TEXT,\n  bankName TEXT,\n  bankBranch TEXT,\n"
                + "  bankAccount TEXT,\n  salary TEXT,\n  createdAt INTEGER,\n  updatedAt INTEGER\n);\n\n");
        for (Employee e : rows) {
            StringBuilder b = new StringBuilder("INSERT INTO employees (");
            for (int i = 0; i < Employee.COLS.length; i++) {
                if (i > 0) b.append(", ");
                b.append(Employee.COLS[i]);
            }
            b.append(") VALUES (");
            for (int i = 0; i < Employee.COLS.length; i++) {
                if (i > 0) b.append(", ");
                b.append(q(e.val(Employee.COLS[i])));
            }
            b.append(");\n");
            w.write(b.toString());
        }
        w.close();
    }

    private static String q(String v) {
        if (v == null) return "NULL";
        return "'" + v.replace("'", "''") + "'";
    }

    // ---------- قراءة SQL ----------

    /** نتيجة تحليل ملف SQL: صفوف القيم mapped على مفاتيح Employee + عدد العبارات */
    public static class SqlRows {
        public List<String[]> rows = new ArrayList<>();
        public int statements = 0;
        public boolean foundColumns = false;
    }

    /** قراءة ملف SQL وتحويل عبارات INSERT إلى صفوف نصية بترتيب EXPORT_KEYS */
    public static SqlRows readSql(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder all = new StringBuilder();
        String ln;
        while ((ln = r.readLine()) != null) all.append(ln).append('\n');
        r.close();
        String sql = all.toString();
        if (sql.length() > 20_000_000) throw new IOException("ملف كبير جداً");

        SqlRows res = new SqlRows();
        int pos = 0;
        while (true) {
            int idx = indexOfInsert(sql, pos);
            if (idx < 0) break;
            pos = idx + 6; // طول كلمة INSERT
            int p = skipWs(sql, pos);
            if (startsWithWord(sql, p, "INTO")) { p += 4; p = skipWs(sql, p); }
            // اسم الجدول
            StringBuilder table = new StringBuilder();
            while (p < sql.length() && (Character.isLetterOrDigit(sql.charAt(p))
                    || sql.charAt(p) == '_' || sql.charAt(p) == '`' || sql.charAt(p) == '"')) {
                table.append(sql.charAt(p));
                p++;
            }
            String tname = table.toString().replace("`", "").replace("\"", "").toLowerCase();
            // نقبل أي جدول موظفين حتى لو صدر من نظام آخر بأسماء مختلفة
            boolean ourTable = tname.contains("employees") || tname.contains("employee")
                    || tname.contains("mmsa") || tname.contains("staff")
                    || tname.contains("personnel") || tname.contains("موظف");
            // قائمة الأعمدة إن وجدت
            List<String> cols = new ArrayList<>();
            p = skipWs(sql, p);
            if (p < sql.length() && sql.charAt(p) == '(') {
                int close = findCloseParen(sql, p);
                if (close < 0) break;
                String colList = sql.substring(p + 1, close);
                for (String c : colList.split(",")) {
                    String cc = c.trim().replace("`", "").replace("\"", "").toLowerCase();
                    if (!cc.isEmpty()) cols.add(cc);
                }
                if (!cols.isEmpty()) res.foundColumns = true;
                p = close + 1;
            }
            // كلمة VALUES ثم التuples
            p = skipWs(sql, p);
            if (!startsWithWord(sql, p, "VALUES") && !startsWithWord(sql, p, "VALUE")) {
                continue;
            }
            p += 6;
            p = skipWs(sql, p);
            while (p < sql.length() && sql.charAt(p) == '(') {
                int close = findCloseParen(sql, p);
                if (close < 0) break;
                List<String> vals = parseTuple(sql, p + 1, close);
                res.statements++;
                if (ourTable) {
                    String[] row = mapToExport(vals, cols);
                    if (row != null) res.rows.add(row);
                }
                p = close + 1;
                p = skipWs(sql, p);
                if (p < sql.length() && sql.charAt(p) == ',') { p++; p = skipWs(sql, p); }
                else break;
            }
            p = skipToSemi(sql, p);
        }
        return res;
    }

    /** الترتيب القياسي لأعمدة جدولنا (بلا قائمة أعمدة مسماة) */
    private static final String[] STD = {"id", "empType", "finNumber", "milNumber", "rank",
            "nationalId", "fullName", "unit", "joinDate", "appointDate", "appointDecree",
            "lastPromotion", "dueDate", "grade", "qualification", "techQual",
            "specialty", "annualAllowance", "bankName", "bankBranch",
            "bankAccount", "salary", "createdAt", "updatedAt"};

    /** تحويل قيم الـtuple إلى صف بترتيب EXPORT_KEYS، أو null إن لم يطابق */
    private static String[] mapToExport(List<String> vals, List<String> cols) {
        java.util.HashMap<String, String> m = new java.util.HashMap<>();
        if (cols != null && !cols.isEmpty()) {
            // مطابقة كل عمود باسمه: camelCase أو snake_case أو عربي أو إنجليزي/فرنسي/روسي
            for (int i = 0; i < Math.min(cols.size(), vals.size()); i++) {
                String key = colKey(cols.get(i));
                if (key != null && !m.containsKey(key)) m.put(key, vals.get(i));
            }
        } else if (vals.size() == STD.length) {
            // بلا أعمدة مسماة وبالطول القياسي فقط: افترض ترتيب جدولنا
            for (int i = 0; i < STD.length; i++) m.put(STD[i], vals.get(i));
        } else {
            return null; // ملف خارجي بلا قائمة أعمدة وبطول غير معروف — لا نخمّن
        }
        String[] out = new String[Employee.EXPORT_KEYS.length];
        boolean any = false;
        for (int i = 0; i < Employee.EXPORT_KEYS.length; i++) {
            String k = Employee.EXPORT_KEYS[i];
            String v = m.get(k);
            out[i] = v == null ? "" : v;
            if (!out[i].isEmpty()) any = true;
        }
        return any ? out : null;
    }

    /** مفتاح عمود SQL: أسماؤنا القياسية (camel/snake) ثم مطابقة ذكية بأي لغة */
    private static String colKey(String col) {
        if (col == null) return null;
        String c = col.replace('`', ' ').replace('"', ' ')
                .replace('_', ' ').replace('.', ' ').replace('-', ' ')
                .replaceAll("\\s+", " ").trim();
        if (c.isEmpty()) return null;
        String joined = c.replace(" ", "").toLowerCase();
        for (String k : Employee.EXPORT_KEYS)
            if (k.toLowerCase().equals(joined)) return k;
        if (joined.equals("createdat") || joined.equals("updatedat")) return null;
        return Employee.matchHeader(c);
    }

    private static int indexOfInsert(String s, int from) {
        return s.toUpperCase().indexOf("INSERT", from);
    }

    private static int skipWs(String s, int p) {
        while (p < s.length() && Character.isWhitespace(s.charAt(p))) p++;
        return p;
    }

    private static boolean startsWithWord(String s, int p, String word) {
        if (p + word.length() > s.length()) return false;
        if (!s.substring(p, p + word.length()).equalsIgnoreCase(word)) return false;
        int nx = p + word.length();
        return nx >= s.length() || !Character.isLetterOrDigit(s.charAt(nx));
    }

    /** نهاية القوس الذي يبدأ عند open مع تجاوز النصوص المقتبسة */
    private static int findCloseParen(String s, int open) {
        boolean sq = false, dq = false;
        for (int i = open + 1; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (sq) {
                if (ch == '\\') i++; // MySQL: \' المحرف التالي متحوّل
                else if (ch == '\'') {
                    // '' تعني اقتباساً واحداً متحوّلاً
                    if (i + 1 < s.length() && s.charAt(i + 1) == '\'') i++;
                    else sq = false;
                }
            } else if (dq) {
                if (ch == '\\') i++;
                else if (ch == '"') dq = false;
            } else if (ch == '\'') sq = true;
            else if (ch == '"') dq = true;
            else if (ch == '(') return findCloseParen(s, i);
            else if (ch == ')') return i;
        }
        return -1;
    }

    /** تحليل محتويات الـtuple بين القوسين إلى قيم نصية (مع إزالة المسافات ومعالجة NULL) */
    private static List<String> parseTuple(String s, int start, int close) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean sq = false, dq = false;
        int i = start;
        while (i < close) {
            char ch = s.charAt(i);
            if (sq) {
                if (ch == '\\' && i + 1 < close) { cur.append(s.charAt(i + 1)); i++; } // \' أو \\ أو \n
                else if (ch == '\'' && i + 1 < close && s.charAt(i + 1) == '\'') { cur.append('\''); i++; }
                else if (ch == '\'') sq = false;
                else cur.append(ch);
            } else if (dq) {
                if (ch == '\\' && i + 1 < close) { cur.append(s.charAt(i + 1)); i++; }
                else if (ch == '"' && i + 1 < close && s.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else if (ch == '"') dq = false;
                else cur.append(ch);
            } else if (ch == '\'') sq = true;
            else if (ch == '"') dq = true;
            else if (ch == ',') {
                out.add(cleanVal(cur.toString(), sq, dq));
                cur.setLength(0);
            }
            else cur.append(ch);
            i++;
        }
        out.add(cleanVal(cur.toString(), sq, dq));
        return out;
    }

    /** قيمة نظيفة: إزالة المسافات وتحويل NULL النصي إلى فارغ */
    private static String cleanVal(String v, boolean sq, boolean dq) {
        String t = v.trim();
        if (!sq && !dq && t.equalsIgnoreCase("NULL")) return "";
        return t;
    }

    private static int skipToSemi(String s, int p) {
        boolean sq = false, dq = false;
        while (p < s.length()) {
            char ch = s.charAt(p);
            if (sq) { if (ch == '\'') sq = false; }
            else if (dq) { if (ch == '"') dq = false; }
            else if (ch == '\'') sq = true;
            else if (ch == '"') dq = true;
            else if (ch == ';') return p + 1;
            p++;
        }
        return p;
    }
}
