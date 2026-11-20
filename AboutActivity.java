package com.mmsa.records;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * حول التطبيق: التعريف + الإصدار (بوابة سرية بالضغط المطوّل) + التحديثات +
 * المظهر (نهاري/ليلي/تلقائي + باقات لونية) + اللغة (4 لغات بأعلامها قائمة تتوسع وتنطوي) + الترخيص المشفر.
 */
public class AboutActivity extends BaseActivity {

    private boolean langOpen = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        build();
        Updater.autoCheck(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (th == null) th = Theme.of(this);
    }

    private void build() {
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

        // ===== بطاقة التعريف =====
        LinearLayout hero = th.card(this, 24, 22);
        inner.addView(hero, matchM(12));

        ImageView logo = new ImageView(this);
        logo.setImageBitmap(Theme.loadLogo(this));
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(104), dp(104));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = dp(4);
        hero.addView(logo, lp);

        TextView name = th.text(this,
                Lang.t("منظومة البيانات", "Data Management System",
                        "Système de gestion des données", "Система управления данными"),
                21, th.primary, true);
        name.setGravity(Gravity.CENTER);
        hero.addView(name, matchM(3));

        TextView dev = th.text(this,
                Lang.t("المطوّر: مروان السيدي — Marwan Alsidi",
                        "Developer: Marwan Alsidi",
                        "Développeur : Marwan Alsidi",
                        "Разработчик: Marwan Alsidi"),
                14, th.text, true);
        dev.setGravity(Gravity.CENTER);
        hero.addView(dev, matchM(2));

        TextView country = th.text(this, Lang.t("دولة ليبيا", "State of Libya", "État de Libye", "Государство Ливия")
                + " \uD83C\uDDF1\uD83C\uDDFE", 14, th.sub, false);
        country.setGravity(Gravity.CENTER);
        hero.addView(country, matchM(6));

        TextView det = th.text(this,
                Lang.t("منظومة البيانات للكادر الوظيفي لإدارة الخدمات الطبية العسكرية",
                        "Employee data system for the Military Medical Services Administration",
                        "Système de données du personnel de l'administration des services médicaux militaires",
                        "Система учёта сотрудников Управления военно-медицинской службы"),
                13.5f, th.sub, false);
        det.setGravity(Gravity.CENTER);
        det.setLineSpacing(dp(3), 1.1f);
        hero.addView(det, matchM(8));

        TextView ver = th.text(this,
                Lang.t("الإصدار", "Version", "Version", "Версия") + " " + Updater.VERSION_NAME
                        + " (" + Updater.VERSION_CODE + ")", 12.5f, th.sub, false);
        ver.setGravity(Gravity.CENTER);
        hero.addView(ver, matchM(0));

        ver.setOnLongClickListener(v -> {
            License.secretGate(this);
            return true;
        });

        // ===== المظهر: الوضع + الباقات اللونية =====
        LinearLayout ct = th.card(this, 20, 16);
        inner.addView(ct, matchM(12));
        ct.addView(th.sectionHeader(this, "palette",
                Lang.t("المظهر — الوضع والألوان", "Appearance — mode & colors",
                        "Apparence — mode et couleurs", "Внешний вид — режим и цвета")), matchM(8));

        TextView modeL = th.label(this, Lang.t("وضع العرض", "Display mode", "Mode d'affichage", "Режим отображения"));
        ct.addView(modeL, match());
        RadioGroup rgMode = new RadioGroup(this);
        int curMode = Theme.mode(this);
        for (int i = 0; i < Theme.MODE_NAMES.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(Theme.MODE_NAMES[i]);
            rb.setTextSize(14);
            rb.setTextColor(th.text);
            rb.setId(300 + i);
            rb.setChecked(i == curMode);
            rgMode.addView(rb);
        }
        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            int m = checkedId - 300;
            Theme.saveMode(this, m);
            th = Theme.of(this);
            getWindow().setStatusBarColor(th.primaryDark);
            toast(Lang.t("تم تطبيق الوضع: ", "Mode applied: ", "Mode appliqué : ", "Режим применён: ")
                    + Theme.MODE_NAMES[m]);
            build();
        });
        ct.addView(rgMode, matchM(10));

        TextView palL = th.label(this, Lang.t("الباقة اللونية (خلفية بألوان متنوعة)",
                "Color palette (varied backgrounds)",
                "Palette de couleurs (fonds variés)", "Цветовая палитра (разные фоны)"));
        ct.addView(palL, match());
        int curPal = Theme.index(this);
        for (int i = 0; i < Theme.THEME_NAMES.length; i++) {
            ct.addView(paletteRow(i, i == curPal));
        }

        // ===== اللغة: قائمة تتوسع وتنطوي بأعلام =====
        LinearLayout cl = th.card(this, 20, 16);
        inner.addView(cl, matchM(12));
        cl.addView(th.sectionHeader(this, "globe",
                Lang.t("اللغة", "Language", "Langue", "Язык")), matchM(8));

        int curLang = Lang.index(this);
        LinearLayout langHead = new LinearLayout(this);
        langHead.setOrientation(LinearLayout.HORIZONTAL);
        langHead.setGravity(Gravity.CENTER_VERTICAL);
        langHead.setClickable(true);
        langHead.setOnClickListener(v -> {
            langOpen = !langOpen;
            build();
        });
        ImageView globe = new ImageView(this);
        globe.setImageDrawable(th.icon(this, "globe", th.onPrimary, 17));
        globe.setScaleType(ImageView.ScaleType.FIT_CENTER);
        globe.setBackground(th.gradient(this, 11, th.primary, th.primaryDark));
        globe.setPadding(dp(8), dp(8), dp(8), dp(8));
        langHead.addView(globe, new LinearLayout.LayoutParams(dp(34), dp(34)));
        TextView curName = th.text(this,
                Lang.LANGS[curLang][0] + " " + Lang.LANGS[curLang][1], 15, th.text, true);
        LinearLayout.LayoutParams cnp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cnp.setMarginStart(dp(10));
        curName.setLayoutParams(cnp);
        langHead.addView(curName);
        TextView chev = th.text(this, langOpen ? "▲" : "▼", 14, th.primary, true);
        langHead.addView(chev, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cl.addView(langHead, match());

        if (langOpen) {
            for (int i = 0; i < Lang.LANGS.length; i++) {
                cl.addView(langRow(i, i == curLang));
            }
        } else {
            cl.addView(th.text(this,
                    Lang.t("اضغط للتوسع واختيار اللغة", "Tap to expand and choose a language",
                            "Touchez pour choisir une langue", "Нажмите, чтобы выбрать язык"),
                    12, th.sub, false), match());
        }

        // ===== التحديثات =====
        LinearLayout cu = th.card(this, 20, 16);
        inner.addView(cu, matchM(12));
        cu.addView(th.sectionHeader(this, "update",
                Lang.t("التحديثات", "Updates", "Mises à jour", "Обновления")), matchM(8));
        cu.addView(th.text(this,
                Lang.t("يبحث التطبيق تلقائياً عن الإصدارات الجديدة عند توفر الاتصال بالإنترنت، وعند وجود إصدار أحدث تظهر نافذة تعرض ما الجديد فيه. يمكنك أيضاً الفحص يدوياً.",
                        "The app checks automatically for new versions when online, and shows a What's New window when a newer version exists. You can also check manually.",
                        "L'application vérifie automatiquement les mises à jour et affiche une fenêtre « Quoi de neuf » si une version plus récente existe. Vérification manuelle possible.",
                        "Приложение автоматически проверяет обновления при подключении к интернету и показывает окно «Что нового». Можно проверить вручную."),
                12.5f, th.sub, false), matchM(10));
        Button upd = th.bigButton(this,
                Lang.t("البحث عن تحديثات", "Check for updates", "Rechercher des mises à jour",
                        "Проверить обновления"), "update", true);
        upd.setOnClickListener(v -> Updater.check(this));
        cu.addView(upd, match());

        // ===== سياسة الخصوصية وحماية Play Protect (متطلب Google Play) =====
        LinearLayout pv = th.card(this, 20, 16);
        inner.addView(pv, matchM(12));
        pv.addView(th.sectionHeader(this, "license",
                Lang.t("سياسة الخصوصية والحماية", "Privacy & protection",
                        "Confidentialité et protection", "Конфиденциальность и защита")), matchM(8));
        pv.addView(th.text(this,
                Lang.t("التطبيق يعمل بالكامل داخل الهاتف: بيانات الموظفين تُخزَّن محلياً مشفرة ولا تُرسل لأي جهة إطلاقاً، ولا يحتوي إعلانات أو تتبعاً. متوافق مع معايير نشر Google Play وفحص Google Play Protect.",
                        "The app works entirely on the phone: employee data is stored locally and encrypted and is never sent anywhere, with no ads or tracking. Compliant with Google Play publishing policies and Google Play Protect.",
                        "L'application fonctionne entièrement sur le téléphone : les données restent locales et chiffrées, sans publicité ni suivi. Conforme aux règles Google Play et à Play Protect.",
                        "Приложение работает полностью на телефоне: данные хранятся локально в зашифрованном виде и никуда не отправляются, без рекламы и отслеживания. Соответствует правилам Google Play и Play Protect."),
                12.5f, th.sub, false), matchM(10));
        Button pvp = th.bigButton(this,
                Lang.t("قراءة سياسة الخصوصية الكاملة", "Read the full privacy policy",
                        "Lire la politique de confidentialité complète",
                        "Прочитать полную политику конфиденциальности"), "license", false);
        pvp.setOnClickListener(v -> License.showPrivacy(this));
        pv.addView(pvp, match());

        // ===== الترخيص المشفر وخاص =====
        LinearLayout lic = th.card(this, 20, 16);
        inner.addView(lic, matchM(12));
        lic.addView(th.sectionHeader(this, "license",
                Lang.t("الترخيص", "License", "Licence", "Лицензия")), matchM(8));
        LinearLayout licRow = new LinearLayout(this);
        licRow.setOrientation(LinearLayout.HORIZONTAL);
        licRow.setGravity(Gravity.CENTER_VERTICAL);
        licRow.setBackground(th.box(this, 14, th.fieldBg, th.line, 1.2f));
        licRow.setPadding(dp(12), dp(12), dp(12), dp(12));
        licRow.setClickable(true);
        licRow.setOnClickListener(v -> License.show(this));
        ImageView licIv = new ImageView(this);
        licIv.setImageDrawable(th.icon(this, "license", th.onPrimary, 18));
        licIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        licIv.setBackground(th.gradient(this, 12, th.primary, th.primaryDark));
        licIv.setPadding(dp(8), dp(8), dp(8), dp(8));
        licRow.addView(licIv, new LinearLayout.LayoutParams(dp(36), dp(36)));
        LinearLayout licTx = new LinearLayout(this);
        licTx.setOrientation(LinearLayout.VERTICAL);
        TextView licT = th.text(this,
                Lang.t("عرض الترخيص المشفر", "View encrypted license",
                        "Voir la licence chiffrée", "Просмотреть лицензию"), 14, th.primary, true);
        TextView licS = th.text(this,
                Lang.t("ترخيص خاص وليس مفتوح المصدر — تحت رعاية وتطوير مروان السيدي",
                        "Private license, not open source — under the care of Marwan Alsidi",
                        "Licence privée, non open source — sous l'égide de Marwan Alsidi",
                        "Частная лицензия — под руководством Marwan Alsidi"),
                11.5f, th.sub, false);
        licTx.addView(licT);
        licTx.addView(licS);
        LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ltp.setMarginEnd(dp(10));
        ltp.setMarginStart(dp(4));
        licRow.addView(licTx, ltp);
        ImageView chevI = new ImageView(this);
        chevI.setImageDrawable(th.icon(this, "key", th.sub, 16));
        licRow.addView(chevI, new LinearLayout.LayoutParams(dp(20), dp(20)));
        lic.addView(licRow, match());

        // ===== تحذير السرية =====
        LinearLayout warn = new LinearLayout(this);
        warn.setOrientation(LinearLayout.HORIZONTAL);
        warn.setGravity(Gravity.CENTER);
        warn.setBackground(th.box(this, 16, th.danger, 0, 0));
        warn.setPadding(dp(14), dp(12), dp(14), dp(12));
        TextView wt = th.text(this,
                Lang.t("هذا التطبيق سري للغاية — عدم مشاركته مع الآخرين ⚠️",
                        "This app is highly confidential — do not share it ⚠️",
                        "Application strictement confidentielle — ne pas la partager ⚠️",
                        "Приложение строго конфиденциально — не передавайте его ⚠️"),
                13.5f, th.onPrimary, true);
        wt.setGravity(Gravity.CENTER);
        warn.addView(wt);
        inner.addView(warn, match());

        page(Lang.t("حول التطبيق", "About", "À propos", "О приложении"), body, null);
    }

    /** صف باقة لونية: دائرة العينة + الاسم + اختيار */
    private LinearLayout paletteRow(final int i, boolean selected) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(6), dp(8), dp(6), dp(8));
        r.setClickable(true);
        r.setOnClickListener(v -> {
            Theme.save(this, i);
            th = Theme.of(this);
            getWindow().setStatusBarColor(th.primaryDark);
            toast(Lang.t("تم تطبيق المظهر: ", "Theme applied: ", "Thème appliqué : ", "Тема применена: ")
                    + Theme.THEME_NAMES[i]
                    + (Theme.isNight(this) ? " 🌙" : " ☀️"));
            build();
        });
        GradientDrawable sw = new GradientDrawable();
        sw.setShape(GradientDrawable.OVAL);
        sw.setColor(Theme.PALETTE_SWATCH[i]);
        sw.setStroke(dp(2), th.line);
        ImageView dot = new ImageView(this);
        dot.setBackground(sw);
        r.addView(dot, new LinearLayout.LayoutParams(dp(26), dp(26)));
        TextView t = th.text(this, Theme.THEME_NAMES[i], 14, th.text, selected);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.setMarginStart(dp(12));
        r.addView(t, tp);
        r.addView(th.text(this, selected ? "✓" : "", 15, th.primary, true),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        return r;
    }

    /** صف لغة: علم + اسم + علامة الاختيار */
    private LinearLayout langRow(final int i, boolean selected) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(8), dp(10), dp(8), dp(10));
        r.setBackground(th.box(this, 12, selected ? th.fieldBg : th.card, th.line, 1f));
        r.setClickable(true);
        r.setOnClickListener(v -> {
            if (i != Lang.index(this)) {
                Lang.save(this, i);
                toast(Lang.t("تم تغيير اللغة", "Language changed", "Langue modifiée", "Язык изменён"));
                // إعادة تشغيل التطبيق لتطبيق اللغة على كل الشاشات
                Intent it = new Intent(this, MainActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
                finishAffinity();
            }
        });
        TextView flag = th.text(this, Lang.LANGS[i][1], 22, th.text, false);
        r.addView(flag, new LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView nm = th.text(this, Lang.LANGS[i][0], 15.5f, th.text, selected);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        np.setMarginStart(dp(6));
        nm.setLayoutParams(np);
        r.addView(nm);
        r.addView(th.text(this, selected ? "✓" : "", 16, th.primary, true),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        return r;
    }
}
