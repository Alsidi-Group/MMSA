package com.mmsa.records;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * نظام اللغات: العربية 🇱🇾 / English 🇬🇧 / Français 🇫🇷 / Русский 🇷🇺
 * الترجمة تُكتب مباشرة في مكان النص عبر t(عربي, إنجليزي, فرنسي, روسي) —
 * واللغة تُحفظ في التفضيلات وتُطبَّق على كل الشاشات مع اتجاه RTL للعربية فقط.
 */
public class Lang {

    /** أسماء اللغات وعلاماتها كما تظهر في قائمة حول التطبيق */
    public static final String[][] LANGS = {
            {"العربية", "\uD83C\uDDF1\uD83C\uDDFE"},   // 🇱🇾
            {"English", "\uD83C\uDDEC\uD83C\uDDE7"},   // 🇬🇧
            {"Français", "\uD83C\uDDEB\uD83C\uDDF7"},  // 🇫🇷
            {"Русский", "\uD83C\uDDF7\uD83C\uDDFA"},   // 🇷🇺
    };

    public static final String[] CODES = {"ar", "en", "fr", "ru"};

    /** فهرس اللغة الحالي — يُضبط عند إنشاء كل شاشة */
    public static int idx = 0;

    public static int index(Context c) {
        int i = c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).getInt("lang", 0);
        if (i < 0 || i > 3) i = 0;
        return i;
    }

    public static void save(Context c, int i) {
        if (i < 0 || i > 3) i = 0;
        c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).edit().putInt("lang", i).apply();
        idx = i;
        Theme.bumpUi(c); // إعادة بناء كل الشاشات المفتوحة باللغة الجديدة
    }

    /** ترجمة مباشرة: t("نص عربي", "English text", "Texte français", "Русский текст") */
    public static String t(String ar, String en, String fr, String ru) {
        switch (idx) {
            case 1: return en;
            case 2: return fr;
            case 3: return ru;
            default: return ar;
        }
    }

    /** هل اللغة الحالية عربية (اتجاه RTL)؟ */
    public static boolean rtl() {
        return idx == 0;
    }

    /** غلاف السياق بلغة محددة (يُستخدم في attachBaseContext) */
    public static Context wrap(Context c, int i) {
        Locale loc = new Locale(CODES[Math.max(0, Math.min(3, i))]);
        Configuration cfg = new Configuration(c.getResources().getConfiguration());
        cfg.setLocale(loc);
        cfg.setLayoutDirection(loc);
        return c.createConfigurationContext(cfg);
    }
}
