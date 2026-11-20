package com.mmsa.records;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

/** الإحصائيات: بلاطات العدّادات وتوزيع الرتب بأشرطة نسبية */
public class StatsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    private void build() {
        Db db = Db.get(this);
        int total = db.count();
        int mil = db.countType(Employee.TYPE_MIL);
        int med = db.countType(Employee.TYPE_MED);
        int civ = db.countType(Employee.TYPE_CIV);
        Employee last = db.lastAdded();

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

        // ===== بلاطات النظرة العامة (2×2) =====
        inner.addView(th.sectionHeader(this, "stats",
                Lang.t("نظرة عامة", "Overview", "Vue d'ensemble", "Обзор")), matchM(10));
        LinearLayout g1 = new LinearLayout(this);
        g1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMargins(dp(3), 0, dp(3), 0);
        g1.addView(statTile("people", Lang.t("إجمالي الموظفين", "Total employees", "Total des employés", "Всего сотрудников"), String.valueOf(total), true), tp);
        g1.addView(statTile("badge", Lang.t("العسكريون", "Military", "Militaires", "Военные"), String.valueOf(mil), false), tp);
        inner.addView(g1, matchM(6));
        LinearLayout g2 = new LinearLayout(this);
        g2.setOrientation(LinearLayout.HORIZONTAL);
        g2.addView(statTile("shield", Lang.t("الطبيون والطبيب المساعدون", "Medical & paramedical", "Médical et paramédical", "Медицинские"), String.valueOf(med), false), tp);
        g2.addView(statTile("person", Lang.t("المدنيون", "Civilians", "Civils", "Гражданские"), String.valueOf(civ), false), tp);
        inner.addView(g2, matchM(6));
        LinearLayout g3 = new LinearLayout(this);
        g3.setOrientation(LinearLayout.HORIZONTAL);
        g3.addView(statTile("plus", Lang.t("آخر إضافة", "Last added", "Dernier ajout", "Последняя запись"), last != null
                ? last.displayValue("fullName", true) : "—", false), tp);
        inner.addView(g3, matchM(12));

        // ===== توزيع الرتب بأشرطة =====
        LinearLayout c2 = th.card(this, 20, 16);
        inner.addView(c2, matchM(12));
        c2.addView(th.sectionHeader(this, "badge",
                Lang.t("توزيع الرتب", "Rank distribution", "Distribution des grades", "Распределение званий")), matchM(10));
        LinkedHashMap<String, Integer> ranks = db.rankCounts();
        if (ranks.isEmpty()) {
            c2.addView(th.text(this, Lang.t("لا توجد سجلات بعد", "No records yet", "Aucun enregistrement", "Записей пока нет"), 13, th.sub, false), match());
        } else {
            int max = 0;
            for (int v : ranks.values()) max = Math.max(max, v);
            for (Map.Entry<String, Integer> en : ranks.entrySet()) {
                c2.addView(rankBar(en.getKey(), en.getValue(), max));
            }
        }

        page(Lang.t("الإحصائيات", "Statistics", "Statistiques", "Статистика"), body, null);
    }

    /** بلاطة إحصاء: أيقونة + قيمة كبيرة + تسمية */
    private LinearLayout statTile(String icon, String label, String value, boolean highlight) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        int bgc = highlight ? th.primary : th.fieldBg;
        l.setBackground(th.box(this, 18, bgc, highlight ? 0 : th.line, 1f));
        l.setPadding(dp(10), dp(14), dp(10), dp(14));
        int iconColor = highlight ? th.onPrimary : th.primary;
        int labelColor = highlight ? th.onPrimary : th.sub;
        int valueColor = highlight ? th.onPrimary : th.text;

        ImageView iv = new ImageView(this);
        iv.setImageDrawable(th.icon(this, icon, iconColor, 20));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        l.addView(iv, new LinearLayout.LayoutParams(dp(24), dp(24)));

        TextView v = th.text(this, value, 17, valueColor, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(0, dp(6), 0, 0);
        v.setSingleLine(false);
        l.addView(v, match());

        TextView t = th.text(this, label, 11.5f, labelColor, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(2), 0, 0);
        l.addView(t, match());
        return l;
    }

    /** صف رتبة: الاسم + العدد + شريط نسبي */
    private LinearLayout rankBar(String rank, int count, int max) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(6), 0, dp(6));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView name = th.text(this, rank, 14, th.text, false);
        top.addView(name, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView n = th.text(this, String.valueOf(count), 14, th.accent, true);
        top.addView(n, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        wrap.addView(top, match());

        LinearLayout barRow = new LinearLayout(this);
        barRow.setOrientation(LinearLayout.HORIZONTAL);
        barRow.setPadding(0, dp(4), 0, 0);
        ViewFill fill = new ViewFill(this, th.gradient(this, 6, th.primary, th.primaryDark));
        ViewFill rest = new ViewFill(this, th.box(this, 6, th.fieldBg, 0, 0));
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0, dp(7),
                Math.max(count, 1f));
        fp.leftMargin = dp(2);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, dp(7),
                Math.max(max - count, 0.01f));
        rp.leftMargin = dp(2);
        barRow.addView(fill.v, fp);
        barRow.addView(rest.v, rp);
        wrap.addView(barRow, match());
        return wrap;
    }

    /** غلاف بسيط لعرض بملف خلفية */
    private static class ViewFill {
        final android.view.View v;

        ViewFill(StatsActivity act, android.graphics.drawable.Drawable d) {
            v = new android.view.View(act);
            v.setBackground(d);
        }
    }
}
