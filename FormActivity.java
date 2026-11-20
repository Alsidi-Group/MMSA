package com.mmsa.records;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

/** نموذج الإدخال/التعديل: أقسام بعناوين وأيقونات + خانات مقيّدة + تحقق الرقم الوطني + سجل التغييرات */
public class FormActivity extends BaseActivity {

    private Spinner sType, sIdType, sRank, sQual, sTech, sSpec, sGrade, sAllow;
    private EditText eNum, fNat, fName, fUnit, fJoin, fAppoint, fDecree, fProm, fDue,
            fBank, fBranch, fAccount, fSalary;
    private int editId = -1;
    private boolean listenersOn = false;
    private String pickPh; // عنصر تمهيدي للقوائم الاختيارية — يُحفظ فارغاً

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        editId = getIntent().getIntExtra("id", -1);

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(16), dp(16), dp(16), dp(24));
        sc.addView(inner, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(sc, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setBackgroundColor(th.card);
        bar.setElevation(dp(10));
        bar.setPadding(dp(16), dp(12), dp(16), dp(12));
        Button save = th.bigButton(this,
                editId < 0
                        ? Lang.t("حفظ في قاعدة البيانات", "Save to database", "Enregistrer", "Сохранить в базу")
                        : Lang.t("حفظ التعديلات", "Save changes", "Enregistrer les modifications", "Сохранить изменения"),
                "check", true);
        save.setOnClickListener(v -> save());
        bar.addView(save, match());

        page(editId < 0
                ? Lang.t("إدخال بيانات موظف جديد", "New employee record",
                "Nouvel enregistrement", "Новая запись сотрудника")
                : Lang.t("تعديل بيانات الموظف", "Edit employee record",
                "Modifier l'enregistrement", "Изменение записи"), body, bar);

        // ===== قسم الصفة ونوع الرقم =====
        LinearLayout c1 = th.card(this, 20, 16);
        inner.addView(c1, matchM(12));
        c1.addView(th.sectionHeader(this, "person",
                Lang.t("الصفة ونوع الرقم", "Category & number type",
                        "Catégorie et type de numéro", "Категория и тип номера")), matchM(10));
        c1.addView(th.label(this, Lang.t("الصفة", "Category", "Catégorie", "Категория")), match());
        sType = th.spinner(this, new String[]{
                Lang.t("عسكري", "Military", "Militaire", "Военный"),
                Lang.t("مدني", "Civilian", "Civil", "Гражданский"),
                Employee.TYPE_MED_FULL + " (" + Lang.t("طبي", "Medical", "Médical", "Медицинский") + ")"});
        c1.addView(th.spinnerIconed(this, sType, "person"), matchM(8));
        c1.addView(th.label(this, Lang.t("نوع الرقم", "Number type", "Type de numéro", "Тип номера")), match());
        sIdType = th.spinner(this, new String[]{
                Employee.labelUi("finNumber"), Employee.labelUi("milNumber")});
        c1.addView(th.spinnerIconed(this, sIdType, "badge"), matchM(8));
        c1.addView(th.label(this, Lang.t("الرقم (أحد النوعين فقط)",
                "Number (one of the two only)", "Numéro (un seul des deux)", "Номер (только один)")), match());
        eNum = th.field(this, Employee.K_NUM,
                Lang.t("أرقام فقط", "Digits only", "Chiffres uniquement", "Только цифры"), "badge");
        c1.addView(eNum, match());

        // ===== قسم الرتبة =====
        LinearLayout c2 = th.card(this, 20, 16);
        inner.addView(c2, matchM(12));
        c2.addView(th.sectionHeader(this, "badge",
                Lang.t("الرتبة", "Rank", "Grade", "Звание")), matchM(10));
        c2.addView(th.label(this, Lang.t("الرتبة — تتغير القائمة حسب الصفة",
                "Rank — the list changes by category",
                "Grade — la liste change selon la catégorie",
                "Звание — список зависит от категории")), match());
        sRank = th.spinner(this, Employee.RANKS_MIL);
        c2.addView(th.spinnerIconed(this, sRank, "star"), match());

        // ===== قسم البيانات الأساسية =====
        LinearLayout c3 = th.card(this, 20, 16);
        inner.addView(c3, matchM(12));
        c3.addView(th.sectionHeader(this, "doc",
                Lang.t("البيانات الأساسية", "Basic data", "Données de base", "Основные данные")), matchM(10));
        fNat = addField(c3,
                Lang.t("الرقم الوطني (أرقام فقط — 12 رقم)", "National no. (digits only — 12 digits)",
                        "N° national (12 chiffres)", "Национальный номер (12 цифр)"),
                Employee.K_NUM, Lang.t("أرقام فقط", "Digits only", "Chiffres uniquement", "Только цифры"), "id");
        fName = addField(c3,
                Lang.t("الاسم الرباعي (نص فقط)", "Full name (text only)",
                        "Nom complet (texte)", "Полное имя (текст)"),
                Employee.K_TEXT, Lang.t("أربعة أسماء", "Four names", "Quatre noms", "Четыре имени"), "person");
        fUnit = addField(c3,
                Lang.t("الجهة التابع لها (نص فقط)", "Assigned entity (text only)",
                        "Affectation (texte)", "Подразделение (текст)"),
                Employee.K_TEXT, Lang.t("مثال: المستشفى العسكري", "e.g. Military hospital",
                        "ex. Hôpital militaire", "напр. Военный госпиталь"), "building");

        // ===== قسم المواعيد والقرارات =====
        LinearLayout c4 = th.card(this, 20, 16);
        inner.addView(c4, matchM(12));
        c4.addView(th.sectionHeader(this, "calendar",
                Lang.t("المواعيد والقرارات", "Dates & decrees", "Dates et décrets", "Даты и приказы")), matchM(10));
        fJoin = addField(c4,
                Lang.t("تاريخ الالتحاق بالوحدة التابع لها الآن (تاريخ فقط)",
                        "Joining date (date only)", "Date d'affectation (date)", "Дата присоединения (дата)"),
                Employee.K_DATE, Lang.t("اضغط لاختيار التاريخ — ضغط مطوّل للمسح",
                        "Tap to pick a date — long-press to clear",
                        "Touchez pour choisir — appui long pour effacer",
                        "Нажмите для выбора — долгое нажатие очистит"), "calendar");
        fAppoint = addField(c4,
                Lang.t("تاريخ التعيين (تاريخ فقط)", "Appointment date (date only)",
                        "Date de nomination (date)", "Дата назначения (дата)"),
                Employee.K_DATE, Lang.t("اضغط لاختيار التاريخ — ضغط مطوّل للمسح",
                        "Tap to pick a date — long-press to clear",
                        "Touchez pour choisir — appui long pour effacer",
                        "Нажмите для выбора — долгое нажатие очистит"), "calendar");
        fDecree = addField(c4,
                Lang.t("رقم قرار التعيين (أرقام فقط)", "Appointment decree no. (digits only)",
                        "N° décret de nomination", "№ приказа о назначении"),
                Employee.K_NUM, Lang.t("أرقام فقط", "Digits only", "Chiffres uniquement", "Только цифры"), "doc");
        fProm = addField(c4,
                Lang.t("تاريخ آخر قرار ترقية (تاريخ فقط)", "Last promotion date (date only)",
                        "Dernière promotion (date)", "Дата последнего повышения (дата)"),
                Employee.K_DATE, Lang.t("اضغط لاختيار التاريخ — ضغط مطوّل للمسح",
                        "Tap to pick a date — long-press to clear",
                        "Touchez pour choisir — appui long pour effacer",
                        "Нажмите для выбора — долгое нажатие очистит"), "calendar");
        fDue = addField(c4,
                Lang.t("تاريخ الاستحقاق (تاريخ فقط)", "Due date (date only)",
                        "Date d'échéance (date)", "Дата истечения (дата)"),
                Employee.K_DATE, Lang.t("اضغط لاختيار التاريخ — ضغط مطوّل للمسح",
                        "Tap to pick a date — long-press to clear",
                        "Touchez pour choisir — appui long pour effacer",
                        "Нажмите для выбора — долгое нажатие очистит"), "calendar");

        // ===== قسم المؤهلات والتخصص والدرجة والبيانات المالية =====
        LinearLayout c5 = th.card(this, 20, 16);
        inner.addView(c5, matchM(12));
        c5.addView(th.sectionHeader(this, "money",
                Lang.t("المؤهلات والتخصص والدرجة والبيانات المالية",
                        "Qualifications, specialty, grade & financial data",
                        "Qualifications, spécialité, grade et données financières",
                        "Квалификации, специальность, ступень и финансы")), matchM(10));

        pickPh = Lang.t("— اختر من القائمة —", "— Choose from the list —",
                "— Choisir dans la liste —", "— Выберите из списка —");

        c5.addView(th.label(this, Employee.labelUi("qualification")), match());
        sQual = th.spinner(this, withPlaceholder(Employee.QUALIFICATIONS));
        c5.addView(th.spinnerIconed(this, sQual, "doc"), matchM(8));
        c5.addView(th.label(this, Employee.labelUi("techQual")), match());
        sTech = th.spinner(this, withPlaceholder(Employee.TECH_QUALS));
        c5.addView(th.spinnerIconed(this, sTech, "doc"), matchM(8));
        c5.addView(th.label(this, Employee.labelUi("specialty")), match());
        sSpec = th.spinner(this, withPlaceholder(Employee.SPECIALTIES));
        c5.addView(th.spinnerIconed(this, sSpec, "doc"), matchM(8));
        c5.addView(th.label(this, Employee.labelUi("grade")), match());
        sGrade = th.spinner(this, withPlaceholder(Employee.GRADES));
        c5.addView(th.spinnerIconed(this, sGrade, "star"), matchM(8));
        c5.addView(th.label(this, Employee.labelUi("annualAllowance")), match());
        sAllow = th.spinner(this, withPlaceholder(Employee.ALLOWANCES));
        c5.addView(th.spinnerIconed(this, sAllow, "star"), matchM(8));
        fBank = addField(c5,
                Lang.t("اسم المصرف (نص فقط)", "Bank name (text only)",
                        "Nom de la banque (texte)", "Название банка (текст)"),
                Employee.K_TEXT, Lang.t("مثال: مصرف الجمهورية", "e.g. Republic Bank",
                        "ex. Banque de la République", "напр. Республиканский банк"), "bank");
        fBranch = addField(c5,
                Lang.t("فرع المصرف (نص فقط)", "Bank branch (text only)",
                        "Agence bancaire (texte)", "Отделение банка (текст)"),
                Employee.K_TEXT, Lang.t("مثال: فرع طرابلس", "e.g. Tripoli branch",
                        "ex. Agence de Tripoli", "напр. отделение Триполи"), "folder");
        fAccount = addField(c5,
                Lang.t("رقم الحساب المصرفي (أرقام فقط)", "Bank account no. (digits only)",
                        "N° de compte bancaire", "Номер банковского счёта"),
                Employee.K_NUM, Lang.t("أرقام فقط", "Digits only", "Chiffres uniquement", "Только цифры"), "money");
        fSalary = addField(c5,
                Lang.t("قيمة المرتب الشهري (أرقام أو نص)", "Monthly salary (digits or text)",
                        "Salaire mensuel", "Месячная зарплата"),
                Employee.K_FREE, Lang.t("مثال: 1200", "e.g. 1200", "ex. 1200", "напр. 1200"), "money");

        sType.setOnItemSelectedListener(typeSel);
        sIdType.setOnItemSelectedListener(idSel);

        if (editId >= 0) prefill();
        listenersOn = true;

        // مدقق الرقم الوطني: نافذة تنبيه عند التوقف بقيمة ≠ 12 خانة
        Theme.attachNationalValidator(this, fNat);
    }

    private EditText addField(LinearLayout c, String label, int kind, String hint, String icon) {
        c.addView(th.label(this, label), match());
        EditText f = th.field(this, kind, hint, icon);
        c.addView(f, matchM(8));
        return f;
    }

    private final AdapterView.OnItemSelectedListener typeSel =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                    if (!listenersOn) return;
                    rebuildRanks(null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> p) {}
            };

    private final AdapterView.OnItemSelectedListener idSel =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                    // لا تغيير إضافي — النوع يحدد عند الحفظ
                }

                @Override
                public void onNothingSelected(AdapterView<?> p) {}
            };

    /** بناء قائمة الرتب حسب الصفة مع الحفاظ على الاختيار الحالي إن وجد */
    /** قائمة مع عنصر تمهيدي فارغ في الأول (حتى لا تُحفظ قيمة بدون اختيار واعٍ) */
    private String[] withPlaceholder(String[] items) {
        String[] out = new String[items.length + 1];
        out[0] = pickPh;
        System.arraycopy(items, 0, out, 1, items.length);
        return out;
    }

    /** تعبئة قائمة منسدلة من قائمة جاهزة مع الحفاظ على قيمة مخزنة إن لم تكن ضمن القائمة */
    private void fillSpinner(Spinner s, String[] items, String current) {
        if (current == null) current = "";
        current = current.trim();
        String[] list = items;
        if (!current.isEmpty()) {
            boolean found = false;
            for (String it : items) if (it.equals(current)) { found = true; break; }
            if (!found) {
                list = new String[items.length + 1];
                System.arraycopy(items, 0, list, 0, items.length);
                list[items.length] = current;
            }
        }
        s.setAdapter(th.spinnerAdapter(this, list));
        if (!current.isEmpty()) {
            for (int i = 0; i < list.length; i++) {
                if (list[i].equals(current)) {
                    s.setSelection(i);
                    break;
                }
            }
        }
    }

    /** قيمة القائمة الحالية — العنصر التمهيدي يُحفظ فارغاً */
    private String sel(Spinner s) {
        String v = s.getSelectedItem() != null ? s.getSelectedItem().toString() : "";
        return v.equals(pickPh) ? "" : v;
    }

    private void rebuildRanks(String want) {
        int pos = sType.getSelectedItemPosition();
        String[] ranks = pos == 2 ? Employee.RANKS_MED
                : pos == 1 ? Employee.RANKS_CIV : Employee.RANKS_MIL;
        String current = want != null ? want
                : (sRank.getSelectedItem() != null ? sRank.getSelectedItem().toString() : null);
        ArrayAdapter<String> ad = th.spinnerAdapter(this, ranks);
        sRank.setAdapter(ad);
        if (current != null) {
            for (int i = 0; i < ranks.length; i++) {
                if (ranks[i].equals(current)) {
                    sRank.setSelection(i);
                    break;
                }
            }
        }
    }

    private void prefill() {
        Employee e = Db.get(this).get(editId);
        if (e == null) {
            toast(Lang.t("السجل غير موجود", "Record not found",
                    "Enregistrement introuvable", "Запись не найдена"));
            finish();
            return;
        }
        // الصفة: 0 عسكري / 1 مدني / 2 طبي — القيم المخزنة بالعربية القياسية
        String t = e.val("empType");
        sType.setSelection(t.equals(Employee.TYPE_CIV) ? 1
                : t.equals(Employee.TYPE_MED) ? 2 : 0);
        rebuildRanks(e.val("rank"));
        if (!e.val("finNumber").isEmpty()) sIdType.setSelection(0);
        else if (!e.val("milNumber").isEmpty()) sIdType.setSelection(1);
        eNum.setText(e.val("finNumber").isEmpty() ? e.val("milNumber") : e.val("finNumber"));
        fNat.setText(e.val("nationalId"));
        fName.setText(e.val("fullName"));
        fUnit.setText(e.val("unit"));
        setDate(fJoin, e.val("joinDate"));
        setDate(fAppoint, e.val("appointDate"));
        fDecree.setText(e.val("appointDecree"));
        setDate(fProm, e.val("lastPromotion"));
        setDate(fDue, e.val("dueDate"));
        fillSpinner(sQual, withPlaceholder(Employee.QUALIFICATIONS), e.val("qualification"));
        fillSpinner(sTech, withPlaceholder(Employee.TECH_QUALS), e.val("techQual"));
        fillSpinner(sSpec, withPlaceholder(Employee.SPECIALTIES), e.val("specialty"));
        fillSpinner(sGrade, withPlaceholder(Employee.GRADES), e.val("grade"));
        fillSpinner(sAllow, withPlaceholder(Employee.ALLOWANCES), e.val("annualAllowance"));
        fBank.setText(e.val("bankName"));
        fBranch.setText(e.val("bankBranch"));
        fAccount.setText(e.val("bankAccount"));
        fSalary.setText(e.val("salary"));
    }

    private void setDate(EditText f, String iso) {
        if (iso == null || iso.isEmpty()) return;
        f.setTag(iso);
        f.setText(Employee.isoToDisp(iso));
    }

    private String num(EditText f) {
        return Employee.digitsOnly(f.getText().toString());
    }

    private String txt(EditText f) {
        return f.getText().toString().trim();
    }

    private void save() {
        // تحقق الرقم الوطني: 12 خانة بالضبط عند الإدخال
        String nat = num(fNat);
        if (!nat.isEmpty() && nat.length() != 12) {
            dlg()
                    .setTitle(Lang.t("تعذّر الحفظ", "Cannot save", "Échec de l'enregistrement", "Не удалось сохранить"))
                    .setMessage(Lang.t("يرجى اعادة التأكد من الرقم الوطني 12 رقم",
                            "Please re-check the national number — it must be 12 digits",
                            "Veuillez revérifier le numéro national — 12 chiffres requis",
                            "Проверьте национальный номер — он должен состоять из 12 цифр"))
                    .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                    .show();
            return;
        }

        Employee e = new Employee();
        e.id = editId;
        // الصفة تُخزّن بالقيمة العربية القياسية في قاعدة البيانات (0 عسكري / 1 مدني / 2 طبي)
        int tp = sType.getSelectedItemPosition();
        e.d.put("empType", tp == 1 ? Employee.TYPE_CIV
                : tp == 2 ? Employee.TYPE_MED : Employee.TYPE_MIL);
        boolean fin = sIdType.getSelectedItemPosition() == 0;
        String n = num(eNum);
        e.d.put("finNumber", fin ? n : "");
        e.d.put("milNumber", fin ? "" : n);
        e.d.put("rank", sRank.getSelectedItem() != null ? sRank.getSelectedItem().toString() : "");
        e.d.put("nationalId", nat);
        e.d.put("fullName", txt(fName));
        e.d.put("unit", txt(fUnit));
        e.d.put("joinDate", fJoin.getTag() != null ? fJoin.getTag().toString() : "");
        e.d.put("appointDate", fAppoint.getTag() != null ? fAppoint.getTag().toString() : "");
        e.d.put("appointDecree", num(fDecree));
        e.d.put("lastPromotion", fProm.getTag() != null ? fProm.getTag().toString() : "");
        e.d.put("dueDate", fDue.getTag() != null ? fDue.getTag().toString() : "");
        e.d.put("grade", sel(sGrade));
        e.d.put("qualification", sel(sQual));
        e.d.put("techQual", sel(sTech));
        e.d.put("specialty", sel(sSpec));
        e.d.put("annualAllowance", sel(sAllow));
        e.d.put("bankName", txt(fBank));
        e.d.put("bankBranch", txt(fBranch));
        e.d.put("bankAccount", num(fAccount));
        e.d.put("salary", txt(fSalary));

        // من يحمل صفة عسكرية: رقمه يذهب إلى الرقم العسكري لا إلى الرقم الوظيفي (المالي)
        Employee.applyMilitaryNumber(e);

        String err = Db.get(this).validate(e);
        if (err != null) {
            dlg()
                    .setTitle(Lang.t("تعذّر الحفظ", "Cannot save", "Échec de l'enregistrement", "Не удалось сохранить"))
                    .setMessage(err)
                    .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                    .show();
            return;
        }
        Employee before = editId >= 0 ? Db.get(this).get(editId) : null;
        boolean ok = editId < 0 ? Db.get(this).insert(e) : Db.get(this).update(e);
        if (ok) {
            // تسجيل التغيير في سجل التغييرات (يظهر عند إعادة فتح التطبيق)
            if (editId < 0) {
                Db.get(this).recordChange("add", e, null, e);
            } else if (before != null) {
                Db.get(this).recordChange("edit", e, before, e);
            }
            toast(editId < 0
                    ? Lang.t("تم حفظ الموظف بنجاح", "Employee saved successfully",
                    "Employé enregistré avec succès", "Сотрудник сохранён")
                    : Lang.t("تم حفظ التعديلات بنجاح", "Changes saved successfully",
                    "Modifications enregistrées", "Изменения сохранены"));
            finish();
        } else {
            dlg()
                    .setTitle(Lang.t("تعذّر الحفظ", "Cannot save", "Échec de l'enregistrement", "Не удалось сохранить"))
                    .setMessage(Lang.t("الرقم مكرر في قاعدة البيانات — مفاتيح عدم التكرار: الرقم الوطني أو المالي أو العسكري",
                            "The number already exists in the database — unique keys: national, financial or military number",
                            "Le numéro existe déjà dans la base — clés uniques : national, financier ou militaire",
                            "Номер уже есть в базе — уникальные ключи: национальный, финансовый или военный"))
                    .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                    .show();
        }
    }
}
