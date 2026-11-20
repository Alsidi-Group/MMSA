package com.mmsa.records;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/** نموذج بيانات الموظف + تعريفات الحقول والرتب وأدوات التحويل */
public class Employee {
    public int id;
    public HashMap<String, String> d = new HashMap<>();
    public long createdAt, updatedAt;

    // أنواع الإدخال
    public static final int K_NUM = 0;   // أرقام فقط
    public static final int K_TEXT = 1;  // نص فقط
    public static final int K_DATE = 2;  // تاريخ فقط
    public static final int K_FREE = 3;  // أرقام أو نص

    // أعمدة قاعدة البيانات (بنفس ترتيب الشيت)
    public static final String[] COLS = {
            "empType", "finNumber", "milNumber", "rank", "nationalId", "fullName", "unit",
            "joinDate", "appointDate", "appointDecree", "lastPromotion", "dueDate", "grade",
            "qualification", "techQual", "specialty", "annualAllowance",
            "bankName", "bankBranch", "bankAccount", "salary"
    };

    // تسميات العرض/الطباعة/التصدير بترتيب الشيت
    public static final String[][] LABELS = {
            {"finNumber", "رقم الوظيفي (المالي)"},
            {"milNumber", "الرقم العسكري"},
            {"rank", "الرتبة"},
            {"nationalId", "الرقم الوطني"},
            {"fullName", "الاسم الرباعي"},
            {"unit", "الجهة التابع لها"},
            {"joinDate", "تاريخ الالتحاق بالوحدة التابع لها الآن"},
            {"appointDate", "تاريخ التعيين"},
            {"appointDecree", "رقم قرار التعيين"},
            {"lastPromotion", "تاريخ آخر قرار ترقية"},
            {"dueDate", "تاريخ الاستحقاق"},
            {"grade", "الدرجة الحالية"},
            {"qualification", "المؤهل العلمي"},
            {"techQual", "المؤهل الفني"},
            {"specialty", "التخصص"},
            {"annualAllowance", "العلاوات السنوية"},
            {"bankName", "اسم المصرف"},
            {"bankBranch", "فرع المصرف"},
            {"bankAccount", "رقم الحساب المصرفي"},
            {"salary", "قيمة المرتب الشهري"},
    };

    // أعمدة التصدير (التسلسل + الصفة + البنود)
    public static final String[] EXPORT_HEADERS = {
            "التسلسل", "الصفة", "رقم الوظيفي (المالي)", "الرقم العسكري", "الرتبة", "الرقم الوطني",
            "الاسم الرباعي", "الجهة التابع لها", "تاريخ الالتحاق بالوحدة التابع لها الآن", "تاريخ التعيين",
            "رقم قرار التعيين", "تاريخ آخر قرار ترقية", "تاريخ الاستحقاق", "الدرجة الحالية",
            "المؤهل العلمي", "المؤهل الفني", "التخصص", "العلاوات السنوية",
            "اسم المصرف", "فرع المصرف", "رقم الحساب المصرفي", "قيمة المرتب الشهري"
    };
    public static final String[] EXPORT_KEYS = {
            "id", "empType", "finNumber", "milNumber", "rank", "nationalId", "fullName", "unit",
            "joinDate", "appointDate", "appointDecree", "lastPromotion", "dueDate", "grade",
            "qualification", "techQual", "specialty", "annualAllowance",
            "bankName", "bankBranch", "bankAccount", "salary"
    };

    public static final String TYPE_MIL = "عسكري";
    public static final String TYPE_CIV = "مدني";
    public static final String TYPE_MED = "طبي";
    /** الاسم الكامل للصفة الطبية كما وردت في الطلب */
    public static final String TYPE_MED_FULL = "العناصر الطبية والطبية المساعدة";
    public static final String ID_FIN = "رقم الوظيفي (المالي)";
    public static final String ID_MIL = "الرقم العسكري";

    // الرتب العسكرية — الصيغ المختصرة المعتمدة (الإصدار 1.4.0)
    public static final String[] RANKS_MIL = {
            "جندي", "جندي أول", "ن.ع", "عريف", "ر.ع.س", "ر.ع.و",
            "ملازم ثان", "ملازم أول", "نقيب", "مقدم", "عقيد", "عميد", "لواء", "فريق",
            "فريق أول", "مشير"
    };
    // رتب المدنيين
    public static final String[] RANKS_CIV = {"موظف", "موظفة"};
    // العناصر الطبية والطبية المساعدة
    public static final String[] RANKS_MED = {
            "دكتور/ة", "طبيب/ة", "ممرض/ة", "فني تخدير", "فني أشعة", "مساعد طبيب"
    };

    // قائمة المؤهلات العلمية (قائمة منسدلة)
    public static final String[] QUALIFICATIONS = {
            "شهادة اعدادية", "شهادة ثانوية", "شهادة دبلوم متوسط", "شهادة دبلوم عالي",
            "شهادة جامعية", "شهادة مجاستير", "شهادة دكتوراء"
    };
    // قائمة المؤهلات الفنية (قائمة منسدلة)
    public static final String[] TECH_QUALS = {
            "فني تكيف وتبريد", "فني كهرباء", "فني صيانة", "سائق سيارة"
    };
    // قائمة التخصصات الإدارية (قائمة منسدلة)
    public static final String[] SPECIALTIES = {
            "طباعة", "محاسب", "مستشار قانوني", "مهندس IT"
    };
    // قائمة الدرجة الحالية (1 إلى 18)
    public static final String[] GRADES = {
            "1 أولى", "2 ثانية", "3 ثالثة", "4 رابعة", "5 خامسة", "6 سادسة",
            "7 سابعة", "8 ثامنة", "9 تاسعة", "10 عاشرة", "11 أحد عشر", "12 اثنى عشر",
            "13 ثلاثة عشر", "14 رابعة عشر", "15 خمسة عشر", "16 سادسة عشر",
            "17 سابعة عشر", "18 الثامنة عشر"
    };
    // قائمة العلاوات السنوية (قائمة منسدلة)
    public static final String[] ALLOWANCES = {
            "علاوة واحدة", "علاوتان", "3 علاوات", "4 علاوات", "5 علاوات", "6 علاوات",
            "7 علاوات", "8 علاوات", "9 علاوات", "10 علاوات", "11 علاوة", "12 علاوة"
    };

    /** اسم الصفة للعرض حسب اللغة (المخزّن في القاعدة يبقى بالعربية القياسية) */
    public static String typeLabel(String stored) {
        if (stored == null || stored.isEmpty()) return "";
        if (stored.equals(TYPE_MED)) return Lang.t("العناصر الطبية والطبية المساعدة",
                "Medical & paramedical staff", "Personnel médical et paramédical",
                "Медицинский и парамедицинский персонал");
        if (stored.equals(TYPE_CIV)) return Lang.t("مدني", "Civilian", "Civil", "Гражданский");
        if (stored.equals(TYPE_MIL)) return Lang.t("عسكري", "Military", "Militaire", "Военный");
        return stored;
    }

    // ---------- الاستيراد الذكي: مطابقة أي عنوان عمود + توحيد القيم ----------

    /** توحيد نص للمقارنة: أرقام غربية + مسافات واحدة + إزالة التطويل */
    private static String normCmp(String s) {
        if (s == null) return "";
        return toWestern(s).replace('\u0640', ' ').replaceAll("\\s+", " ").trim();
    }

    /** توحيد أقوى: إزالة التشكيل وتوحيد الألف والياء والتاء المربوطة (للمقارنة فقط) */
    private static String loose(String s) {
        String t = normCmp(s);
        StringBuilder b = new StringBuilder();
        for (char ch : t.toCharArray()) {
            if (ch >= '\u064B' && ch <= '\u0652') continue; // تشكيل
            if (ch == 'أ' || ch == 'إ' || ch == 'آ') ch = 'ا';
            if (ch == 'ة') ch = 'ه';
            if (ch == 'ى') ch = 'ي';
            b.append(Character.toLowerCase(ch));
        }
        return b.toString().trim();
    }

    private static boolean in(String s, String... opts) {
        for (String o : opts) if (s.contains(o)) return true;
        return false;
    }

    /**
     * مطابقة ذكية لأي عنوان عمود مع المفاتيح القياسية: عربي بأي صيغة أو
     * إنجليزي/فرنسي/روسي أو snake_case. تعيد مفتاح العمود أو null.
     */
    public static String matchHeader(String header) {
        if (header == null) return null;
        String h = normCmp(header);
        if (h.isEmpty()) return null;
        for (int i = 0; i < EXPORT_HEADERS.length; i++)
            if (EXPORT_HEADERS[i].equals(h)) return EXPORT_KEYS[i];
        String low = h.toLowerCase();
        // من الأدق إلى الأعم — أول تطابق يفوز
        if (in(low, "current grade", "grade actuel", "ступень", "الدرجة")) return "grade";
        if (in(low, "rank", "звание", "الرتبة", "grade", "المسمى العسكري")) return "rank";
        if (in(low, "promotion", "повышение", "ترقية")) return "lastPromotion";
        if (in(low, "decree", "décret", "приказ", "قرار")) return "appointDecree";
        if (in(low, "appointment", "nomination", "назначен", "التعيين")) return "appointDate";
        if (in(low, "due", "échéance", "истечен", "الاستحقاق")) return "dueDate";
        if (in(low, "joining", "присоедин", "الالتحاق", "الاتحاق")) return "joinDate";
        if (in(low, "branch", "agence", "отделение", "فرع")) return "bankBranch";
        if (in(low, "account", "compte", "счёт", "счет", "الحساب")) return "bankAccount";
        if (in(low, "bank name", "banque", "банк", "المصرف", "البنك")) return "bankName";
        if (in(low, "salary", "salaire", "зарплат", "المرتب", "الراتب")) return "salary";
        if (in(low, "technical", "technique", "техническая", "المؤهل الفني", "الفني", "المهني"))
            return "techQual";
        if (in(low, "qualification", "académique", "ученая", "учёная", "المؤهل", "الشهادة", "العلمي"))
            return "qualification";
        if (in(low, "specialty", "spécialité", "специальность", "التخصص")) return "specialty";
        if (in(low, "increment", "avancement", "надбав", "علاو")) return "annualAllowance";
        if (in(low, "national", "identity", "identité", "национал",
                "الوطني", "الشخصي", "القومي", "الهوية", "المدني")) return "nationalId";
        if (in(low, "full name", "fullname", "nom complet", "полное имя", "фамил",
                "الرباعي", "الاسم الكامل", "اسم الموظف", "اسم ولقب")) return "fullName";
        if (in(low, "financial", "financier", "рабочий", "job", "staff no", "employee no",
                "employee id", "المالي", "الوظيفي", "الوظيفة", "كود",
                "رقم الموظف", "رقم الموظفين")) return "finNumber";
        if (in(low, "military", "militaire", "военный", "العسكري", "رقم عسكري")) return "milNumber";
        if (in(low, "category", "catégorie", "категория", "الصفة", "الفئة",
                "emp type", "staff type")) return "empType";
        if (in(low, "entity", "unit", "подраздел",
                "الجهة", "الوحدة", "جهة", "مكان العمل", "القسم")) return "unit";
        if (in(low, "name", "имя", "الاسم")) return "fullName";
        if (low.equals("id") || low.equals("no") || low.equals("#")
                || h.equals("م") || h.equals("التسلسل") || h.equals("رقم")) return "id";
        return null;
    }

    /** كل الرتب (عسكري + مدني + طبي) لتوحيد قيم الرتبة */
    private static String[] ranksAll() {
        String[] all = new String[RANKS_MIL.length + RANKS_CIV.length + RANKS_MED.length];
        int k = 0;
        for (String r : RANKS_MIL) all[k++] = r;
        for (String r : RANKS_CIV) all[k++] = r;
        for (String r : RANKS_MED) all[k++] = r;
        return all;
    }

    /** ترحيل مسميات الرتب القديمة المخزنة إلى الصيغ المختصرة (يستخدمه Db عند الترقية) */
    public static final String[][] RANK_MIGRATIONS = {
            {"رئيس عرفة وحدة", "ر.ع.و"},
            {"رئيس عرفة سرية", "ر.ع.س"},
            {"نائب عريف", "ن.ع"}
    };

    /** صيغ رتب شائعة في الشيتات (بلا نقاط أو بالصيغة القديمة الطويلة) → الصيغة القياسية */
    private static final String[][] RANK_ALIASES = {
            {"رئيسعرفهوحده", "ر.ع.و"}, {"رئيسعرفهسريه", "ر.ع.س"},
            {"نائبعريف", "ن.ع"}, {"ماول", "ملازم أول"},
            {"مثاني", "ملازم ثان"}, {"ملازمثاني", "ملازم ثان"}
    };

    /** توحيد الرتبة: تطابق متسامح + الاختصارات بلا نقاط («ر ع و» ← ر.ع.و، «ن ع» ← ن.ع)
     *  والصيغ القديمة الطويلة («رئيس عرفة وحدة» ← ر.ع.و) و«م أول» ← ملازم أول */
    private static String canonRank(String t) {
        String lt = loose(t);
        for (String r : ranksAll()) if (loose(r).equals(lt)) return r;
        String s = lt.replaceAll("[ .0-9]", "");
        if (!s.isEmpty()) {
            for (String[] a : RANK_ALIASES) if (a[0].equals(s)) return a[1];
            for (String r : ranksAll()) {
                String rs = loose(r).replaceAll("[ .0-9]", "");
                if (rs.equals(s)) return r;
            }
        }
        return t;
    }

    /** هل النص رتبة عسكرية؟ (يُستخدم عند الاستيراد لضبط الصفة ونقل الرقم إلى العسكري) */
    public static boolean isMilitaryRank(String v) {
        String c = canonicalValue("rank", v);
        for (String r : RANKS_MIL) if (r.equals(c)) return true;
        return false;
    }

    /** قاعدة الصفة العسكرية: من يحمل صفة عسكرية (أو رتبة عسكرية) ينتقل رقمه
     *  من خانة الرقم الوظيفي (المالي) إلى خانة الرقم العسكري — إن كانت فارغة */
    public static void applyMilitaryNumber(Employee e) {
        if (!TYPE_MIL.equals(e.val("empType")) && !isMilitaryRank(e.val("rank"))) return;
        e.d.put("empType", TYPE_MIL);
        if (e.val("milNumber").isEmpty() && !e.val("finNumber").isEmpty()) {
            e.d.put("milNumber", e.val("finNumber"));
            e.d.put("finNumber", "");
        }
    }

    /** إرجاع الصيغة القياسية من قائمة جاهزة إن طابقت النص (بمقارنة متسامحة) */
    private static String canonFromList(String t, String[] list) {
        String lt = loose(t);
        for (String item : list) if (loose(item).equals(lt)) return item;
        // محاولة ثانية بعد إزالة الأرقام والمسافات: «أولى» تطابق «1 أولى»
        String lt2 = lt.replaceAll("[0-9 ]", "");
        if (!lt2.isEmpty()) {
            for (String item : list) {
                String li2 = loose(item).replaceAll("[0-9 ]", "");
                if (li2.equals(lt2)) return item;
            }
        }
        return t;
    }

    private static String canonType(String t) {
        String l = loose(t);
        if (l.contains("طبي") || l.contains("المساعده") || l.contains("medic")) return TYPE_MED;
        if (l.contains("مدني") || l.contains("civil")) return TYPE_CIV;
        if (l.contains("عسكري") || l.contains("military") || l.contains("شرطي")) return TYPE_MIL;
        return t;
    }

    private static String canonGrade(String t) {
        String d = digitsOnly(t);
        if (!d.isEmpty() && d.length() <= 2) {
            try {
                int n = Integer.parseInt(d);
                if (n >= 1 && n <= GRADES.length) return GRADES[n - 1];
            } catch (Exception ignored) {}
        }
        return canonFromList(t, GRADES);
    }

    private static String canonAllowance(String t) {
        String d = digitsOnly(t);
        if (!d.isEmpty() && d.length() <= 2) {
            try {
                int n = Integer.parseInt(d);
                if (n >= 1 && n <= ALLOWANCES.length) return ALLOWANCES[n - 1];
            } catch (Exception ignored) {}
        }
        String l = loose(t);
        if (l.contains("علاو")) {
            if (l.contains("واحد")) return ALLOWANCES[0];
            if (l.contains("علاوتان") || l.contains("علاوتين")
                    || l.contains("اثنتان") || l.contains("اثنين")) return ALLOWANCES[1];
        }
        return canonFromList(t, ALLOWANCES);
    }

    /**
     * توحيد قيم الحقول القياسية بحيث يطابق النص المستورد نصوص التطبيق:
     * الصفة والرتبة والدرجة والعلاوات والمؤهلات والتخصص تُحوَّل للقيمة القياسية
     * إن طابقتها بأي صيغة، والنصوص الحرة تبقى كما هي تماماً.
     */
    public static String canonicalValue(String key, String v) {
        if (v == null) return "";
        String t = normCmp(v);
        if (t.isEmpty()) return "";
        switch (key) {
            case "empType": return canonType(t);
            case "rank": return canonRank(t);
            case "grade": return canonGrade(t);
            case "annualAllowance": return canonAllowance(t);
            case "qualification": return canonFromList(t, QUALIFICATIONS);
            case "techQual": return canonFromList(t, TECH_QUALS);
            case "specialty": return canonFromList(t, SPECIALTIES);
            default: return v.trim();
        }
    }

    public String val(String key) {
        String v = d.get(key);
        return v == null ? "" : v;
    }

    public static String labelOf(String key) {
        for (String[] p : LABELS) if (p[0].equals(key)) return p[1];
        return key;
    }

    /** تسمية الحقل حسب اللغة الحالية (لعرض الواجهات فقط — التصدير يبقى بالعربية القياسية) */
    public static String labelUi(String key) {
        if (key.equals("empType"))
            return Lang.t("الصفة", "Category", "Catégorie", "Категория");
        if (key.equals("id"))
            return Lang.t("التسلسل", "#", "N°", "№");
        if (key.equals("finNumber"))
            return Lang.t("رقم الوظيفي (المالي)", "Job no. (Financial)", "N° d'emploi (financier)", "Рабочий номер (финансовый)");
        if (key.equals("milNumber"))
            return Lang.t("الرقم العسكري", "Military no.", "N° militaire", "Военный номер");
        if (key.equals("rank"))
            return Lang.t("الرتبة", "Rank", "Grade", "Звание");
        if (key.equals("nationalId"))
            return Lang.t("الرقم الوطني", "National no.", "N° national", "Национальный номер");
        if (key.equals("fullName"))
            return Lang.t("الاسم الرباعي", "Full name", "Nom complet", "Полное имя");
        if (key.equals("unit"))
            return Lang.t("الجهة التابع لها", "Assigned entity", "Affectation", "Подразделение");
        if (key.equals("joinDate"))
            return Lang.t("تاريخ الالتحاق بالوحدة", "Joining date", "Date d'affectation", "Дата присоединения");
        if (key.equals("appointDate"))
            return Lang.t("تاريخ التعيين", "Appointment date", "Date de nomination", "Дата назначения");
        if (key.equals("appointDecree"))
            return Lang.t("رقم قرار التعيين", "Appointment decree no.", "N° décret de nomination", "№ приказа о назначении");
        if (key.equals("lastPromotion"))
            return Lang.t("تاريخ آخر قرار ترقية", "Last promotion date", "Dernière promotion", "Дата последнего повышения");
        if (key.equals("dueDate"))
            return Lang.t("تاريخ الاستحقاق", "Due date", "Date d'échéance", "Дата истечения");
        if (key.equals("grade"))
            return Lang.t("الدرجة الحالية", "Current grade", "Grade actuel", "Текущая ступень");
        if (key.equals("qualification"))
            return Lang.t("المؤهل العلمي", "Academic qualification", "Qualification académique", "Учёная квалификация");
        if (key.equals("techQual"))
            return Lang.t("المؤهل الفني", "Technical qualification", "Qualification technique", "Техническая квалификация");
        if (key.equals("specialty"))
            return Lang.t("التخصص", "Specialty", "Spécialité", "Специальность");
        if (key.equals("annualAllowance"))
            return Lang.t("العلاوات السنوية", "Annual increments", "Avancements annuels", "Годовые надбавки");
        if (key.equals("bankName"))
            return Lang.t("اسم المصرف", "Bank name", "Nom de la banque", "Название банка");
        if (key.equals("bankBranch"))
            return Lang.t("فرع المصرف", "Bank branch", "Agence bancaire", "Отделение банка");
        if (key.equals("bankAccount"))
            return Lang.t("رقم الحساب المصرفي", "Bank account no.", "N° de compte bancaire", "Номер банковского счёта");
        if (key.equals("salary"))
            return Lang.t("قيمة المرتب الشهري", "Monthly salary", "Salaire mensuel", "Месячная зарплата");
        return labelOf(key);
    }

    public String displayValue(String key, boolean dashForEmpty) {
        String v = val(key);
        if (key.endsWith("Date")) v = isoToDisp(v);
        if (key.equals("empType")) v = typeLabel(v);
        if (dashForEmpty && (v == null || v.trim().isEmpty())) return "—";
        return v == null ? "" : v;
    }

    /** تحويل الأرقام العربية/الفارسية إلى أرقام لاتينية */
    public static String toWestern(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch >= '\u0660' && ch <= '\u0669') b.append((char) (ch - '\u0660' + '0'));
            else if (ch >= '\u06F0' && ch <= '\u06F9') b.append((char) (ch - '\u06F0' + '0'));
            else b.append(ch);
        }
        return b.toString();
    }

    /** أرقام فقط (بعد التعريب) */
    public static String digitsOnly(String s) {
        StringBuilder b = new StringBuilder();
        for (char ch : toWestern(s).toCharArray()) if (ch >= '0' && ch <= '9') b.append(ch);
        return b.toString();
    }

    /** نص فقط: حروف ومسافات وعلامات أساسية، بدون أرقام */
    public static String textOnly(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char ch : toWestern(s).toCharArray()) {
            boolean ok = Character.isLetter(ch) || ch == ' ' || ch == '.' || ch == '-'
                    || ch == '/' || ch == '(' || ch == ')' || ch == '\'' || ch == '\u0640';
            if (ok) b.append(ch);
        }
        return b.toString();
    }

    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DISP = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    /** ISO yyyy-MM-dd إلى dd/MM/yyyy للعرض */
    public static String isoToDisp(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "";
        try {
            Date dt = ISO.parse(iso.trim());
            return DISP.format(dt);
        } catch (ParseException e) {
            return iso.trim();
        }
    }

    /** تحويل ISO yyyy-MM-dd إلى كائن تاريخ */
    public static Date parseIso(String iso) throws ParseException {
        return ISO.parse(iso);
    }

    /** رقم تسلسلي بتاريخ Excel (نطاق 1954..2119) إلى ISO — وإلا يُعاد النص كما هو */
    public static String excelSerialToIso(String v) {
        String d = digitsOnly(v);
        if (d.isEmpty() || !d.equals(v.trim()) || d.length() > 6) return v;
        try {
            long serial = Long.parseLong(d);
            if (serial < 20000L || serial > 80000L) return v;
            java.util.Calendar cal = java.util.Calendar.getInstance(
                    java.util.TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis((serial - 25569L) * 86400000L);
            return String.format(java.util.Locale.US, "%04d-%02d-%02d",
                    cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1,
                    cal.get(java.util.Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            return v;
        }
    }

    /** dd/MM/yyyy أو yyyy-MM-dd أو yyyyMMdd إلى ISO للتخزين، أو "" إن كانت غير صالحة */
    public static String anyToIso(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";
        try {
            Date dt = DISP.parse(s);
            return ISO.format(dt);
        } catch (ParseException ignored) {
        }
        try {
            Date dt = ISO.parse(s);
            return ISO.format(dt);
        } catch (ParseException ignored) {
        }
        if (s.length() == 8 && digitsOnly(s).equals(s)) {
            try {
                SimpleDateFormat comp = new SimpleDateFormat("yyyyMMdd", Locale.US);
                comp.setLenient(false);
                Date dt = comp.parse(s);
                return ISO.format(dt);
            } catch (ParseException ignored) {
            }
        }
        return "";
    }

    /** صف تصدير واحد بترتيب الشيت */
    public String[] exportRow() {
        String[] row = new String[EXPORT_KEYS.length];
        for (int i = 0; i < EXPORT_KEYS.length; i++) {
            String k = EXPORT_KEYS[i];
            if (k.equals("id")) row[i] = String.valueOf(id);
            else if (k.equals("empType")) row[i] = val("empType");
            else row[i] = displayValue(k, false);
        }
        return row;
    }

    /** لقطة JSON لكل الحقول (لسجل التغييرات قبل/بعد) */
    public String toJson() {
        org.json.JSONObject o = new org.json.JSONObject();
        try {
            for (String col : COLS) o.put(col, val(col));
        } catch (Exception ignored) {}
        return o.toString();
    }

    /** استرجاع لقطة من JSON */
    public static Employee fromJson(String j) {
        Employee e = new Employee();
        if (j == null || j.isEmpty()) return e;
        try {
            org.json.JSONObject o = new org.json.JSONObject(j);
            for (String col : COLS) e.d.put(col, o.optString(col, ""));
        } catch (Exception ignored) {}
        return e;
    }
}
