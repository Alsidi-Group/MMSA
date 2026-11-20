package com.mmsa.records;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/** قاعدة البيانات: كل الموظفين مع التصفية والفرز (تصاعدي/تنازلي) ووضع اختيار للطباعة A4 */
public class ListActivity extends BaseActivity {

    private ListView lv;
    private TextView countView;
    private EditText filter;
    private Spinner sortSp;
    private Button sortBtn;
    private ArrayList<Employee> data = new ArrayList<>();
    private EmpAdapter ad;
    private boolean pick = false;
    private boolean desc = false;

    /** أعمدة الفرز: نص العرض + اسم العمود (مسموح به فقط في Db.SORTABLE) */
    private static final String[][] SORT_FIELDS = {
            {"id", "id"}, {"fullName", "fullName"}, {"finNumber", "finNumber"},
            {"milNumber", "milNumber"}, {"rank", "rank"}, {"nationalId", "nationalId"},
            {"bankName", "bankName"}, {"bankBranch", "bankBranch"}, {"unit", "unit"},
            {"appointDate", "appointDate"}, {"lastPromotion", "lastPromotion"},
            {"appointDecree", "appointDecree"}
    };

    private String[] sortLabels() {
        return new String[]{
                Employee.labelUi("id") + Lang.t(" (التسلسل)", " (Serial)", " (Série)", " (Порядковый)"),
                Lang.t("حروف الاسم أبجدياً", "Name alphabetically", "Nom alphabétique", "Имя по алфавиту"),
                Lang.t("رقم المالي", "Financial no.", "N° financier", "Финансовый номер"),
                Lang.t("رقم العسكري", "Military no.", "N° militaire", "Военный номер"),
                Lang.t("الرتبة", "Rank", "Grade", "Звание"),
                Lang.t("الرقم الوطني", "National no.", "N° national", "Национальный номер"),
                Lang.t("اسم المصرف", "Bank name", "Nom de la banque", "Название банка"),
                Lang.t("فرع المصرف", "Bank branch", "Agence bancaire", "Отделение банка"),
                Lang.t("الجهة التابع لها", "Assigned entity", "Affectation", "Подразделение"),
                Lang.t("تاريخ التعيين", "Appointment date", "Date de nomination", "Дата назначения"),
                Lang.t("تاريخ آخر ترقية", "Last promotion date", "Dernière promotion", "Дата повышения"),
                Lang.t("رقم قرار التعيين", "Appointment decree no.", "N° décret de nomination", "№ приказа о назначении")
        };
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        pick = getIntent().getBooleanExtra("pick", false);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(12);

        LinearLayout top = th.card(this, 20, 14);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.setMargins(pad, pad, pad, dp(8));
        body.addView(top, tp);
        countView = th.text(this, "", 14, th.primary, true);
        top.addView(countView, matchM(6));
        if (pick) {
            TextView hint = th.text(this,
                    Lang.t("اختر الموظف لعرض كشف الطباعة A4",
                            "Pick an employee for the A4 print sheet",
                            "Choisissez un employé pour l'impression A4",
                            "Выберите сотрудника для печати A4"),
                    12.5f, th.sub, false);
            top.addView(hint, matchM(6));
        }

        LinearLayout frow = new LinearLayout(this);
        frow.setOrientation(LinearLayout.HORIZONTAL);
        filter = th.field(this, Employee.K_FREE,
                Lang.t("تصفية: اسم أو رقم أو رتبة", "Filter: name, number or rank",
                        "Filtre : nom, numéro ou grade", "Фильтр: имя, номер или звание"), "search");
        frow.addView(filter, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button go = th.bigButton(this, Lang.t("تصفية", "Filter", "Filtrer", "Фильтр"), "search", true);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gp.setMarginStart(dp(8));
        frow.addView(go, gp);
        go.setOnClickListener(v -> load(filter.getText().toString().trim()));
        top.addView(frow, matchM(8));

        // صف الفرز: قائمة الحقول + زر الاتجاه (من الأكبر للأصغر أو العكس)
        LinearLayout srow = new LinearLayout(this);
        srow.setOrientation(LinearLayout.HORIZONTAL);
        srow.setGravity(Gravity.CENTER_VERTICAL);
        sortSp = th.spinner(this, sortLabels());
        sortSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                load(filter.getText().toString().trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });
        srow.addView(th.spinnerIconed(this, sortSp, "sort"),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        sortBtn = th.button(this,
                desc ? Lang.t("تنازلي ↓", "Desc ↓", "Desc ↓", "По убыванию ↓")
                        : Lang.t("تصاعدي ↑", "Asc ↑", "Asc ↑", "По возрастанию ↑"), false);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sbp.setMarginStart(dp(8));
        sortBtn.setOnClickListener(v -> {
            desc = !desc;
            sortBtn.setText(desc ? Lang.t("تنازلي ↓", "Desc ↓", "Desc ↓", "По убыванию ↓")
                    : Lang.t("تصاعدي ↑", "Asc ↑", "Asc ↑", "По возрастанию ↑"));
            load(filter.getText().toString().trim());
        });
        srow.addView(sortBtn, sbp);
        top.addView(srow, match());

        lv = new ListView(this);
        lv.setDivider(new GradientDrawable());
        lv.setDividerHeight(dp(6));
        lv.setPadding(pad, dp(4), pad, dp(12));
        lv.setClipToPadding(false);
        ad = new EmpAdapter();
        lv.setAdapter(ad);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Employee e = data.get(position);
            if (pick) {
                Intent i = new Intent(this, PrintPreviewActivity.class);
                i.putExtra("id", e.id);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(this, DetailsActivity.class);
                i.putExtra("id", e.id);
                startActivity(i);
            }
        });
        body.addView(lv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        page(pick ? Lang.t("اختيار موظف للطباعة", "Pick employee to print",
                "Choisir un employé à imprimer", "Выбор сотрудника для печати")
                : Lang.t("قاعدة البيانات", "Database", "Base de données", "База данных"), body, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(filter != null && filter.getText() != null
                ? filter.getText().toString().trim() : "");
    }

    private void load(String q) {
        String col = SORT_FIELDS[Math.max(0,
                sortSp != null ? sortSp.getSelectedItemPosition() : 0)][1];
        data = (q == null || q.isEmpty())
                ? Db.get(this).allSorted(col, desc)
                : Db.get(this).searchAll(q);
        countView.setText(pick
                ? Lang.t("الموظفون", "Employees", "Employés", "Сотрудники") + ": " + data.size()
                : Lang.t("كل الموظفين", "All employees", "Tous les employés", "Все сотрудники")
                + ": " + data.size()
                + (q.isEmpty() ? "" : " — " + Lang.t("نتائج التصفية", "filtered", "filtrés", "отфильтровано")));
        ad.notifyDataSetChanged();
        if (data.isEmpty()) {
            TextView t = new TextView(this);
            t.setText(q.isEmpty()
                    ? Lang.t("لا توجد سجلات بعد — أضف موظفاً من الشاشة الرئيسية",
                            "No records yet — add an employee from the home screen",
                            "Aucun enregistrement — ajoutez un employé",
                            "Записей пока нет — добавьте сотрудника")
                    : Lang.t("لا توجد نتائج مطابقة للتصفية", "No matching results",
                            "Aucun résultat correspondant", "Нет подходящих результатов"));
            t.setTextColor(th.sub);
            t.setTextSize(14);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, dp(40), 0, 0);
            lv.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() { return 0; }
                @Override
                public Object getItem(int i) { return null; }
                @Override
                public long getItemId(int i) { return 0; }
                @Override
                public View getView(int i, View v, ViewGroup p) { return t; }
            });
        } else {
            lv.setAdapter(ad);
            ad.notifyDataSetChanged();
        }
    }

    private class EmpAdapter extends BaseAdapter {
        @Override
        public int getCount() { return data.size(); }

        @Override
        public Object getItem(int i) { return data.get(i); }

        @Override
        public long getItemId(int i) { return data.get(i).id; }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            Employee e = data.get(i);
            LinearLayout row = th.card(ListActivity.this, 18, 12);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            ImageView av = new ImageView(ListActivity.this);
            av.setImageDrawable(th.icon(ListActivity.this, "person", th.onPrimary, 22));
            av.setScaleType(ImageView.ScaleType.FIT_CENTER);
            av.setBackground(th.gradient(ListActivity.this, 20, th.primary, th.primaryDark));
            av.setPadding(dp(9), dp(9), dp(9), dp(9));
            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp(44), dp(44));
            ap.setMarginEnd(dp(10));
            row.addView(av, ap);

            LinearLayout tx = new LinearLayout(ListActivity.this);
            tx.setOrientation(LinearLayout.VERTICAL);
            TextView name = th.text(ListActivity.this,
                    e.displayValue("fullName", false), 15, th.text, true);
            tx.addView(name, matchM(2));
            String nums = "";
            if (!e.val("nationalId").isEmpty())
                nums += Lang.t("الوطني", "Nat.", "Nat.", "Нац.") + ": " + e.val("nationalId");
            if (!e.val("finNumber").isEmpty())
                nums += (nums.isEmpty() ? "" : " • ") + Lang.t("المالي", "Fin.", "Fin.", "Фин.")
                        + ": " + e.val("finNumber");
            if (!e.val("milNumber").isEmpty())
                nums += (nums.isEmpty() ? "" : " • ") + Lang.t("العسكري", "Mil.", "Mil.", "Воен.")
                        + ": " + e.val("milNumber");
            TextView sub = th.text(ListActivity.this, nums.isEmpty() ? "—" : nums, 12, th.sub, false);
            tx.addView(sub, match());
            row.addView(tx, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView rank = th.chip(ListActivity.this, e.displayValue("rank", true),
                    th.fieldBg, th.primary);
            row.addView(rank, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return row;
        }
    }
}
