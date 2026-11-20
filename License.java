package com.mmsa.records;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * نظام الترخيص المشفر: يقرأ assets/license.dat (AES-256-GCM) ويحلله داخل التطبيق فقط.
 * الثوابت أدناه مخزّنة موهّاة بـ XOR — لا يوجد أي نص سري صريح في الكود أو داخل APK.
 */
public class License {

    private static final byte[] LIC_MASK = {-80, -64, 1, -37, -24, 45, 0, -88, -1, -67, -11, -29, 100, 12, 36, 73};
    private static final byte[] LIC_KEY = {27, -16, -20, 13, 43, -17, 124, 46, -115, 92, 33, 120, -49, 56, -85, -97, 81, 89, -47, 1, -104, -29, 80, 57, -87, 36, -115, 29, -62, 20, -82, 89};
    private static final byte[] SRC_ENC = {-15, -84, 114, -78, -116, 68, 95, -17, -115, -46, -128, -109, 85, 60, 28, 126, -119, -9};
    private static final byte[] PIN_ENC = {-127, -16, 48, -21, -39, 20, 57, -100};

    /** فك التمويه: XOR مع القناع */
    private static byte[] deobf(byte[] enc, int len) {
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) out[i] = (byte) (enc[i] ^ LIC_MASK[i % LIC_MASK.length]);
        return out;
    }

    /** نص الترخيص بعد فك التشفير من الملف المشفر */
    public static String text(Context c) {
        try (InputStream in = c.getAssets().open("license.dat")) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
            byte[] d = b.toByteArray();
            if (d.length < 24 || !(new String(d, 0, 5, "UTF-8")).equals("MLIC1"))
                throw new Exception("تنسيق غير صالح");
            javax.crypto.SecretKey ks = new javax.crypto.spec.SecretKeySpec(
                    deobf(LIC_KEY, LIC_KEY.length), "AES");
            javax.crypto.Cipher ci = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            ci.init(javax.crypto.Cipher.DECRYPT_MODE, ks,
                    new javax.crypto.spec.GCMParameterSpec(128, d, 5, 12));
            byte[] plain = ci.doFinal(d, 17, d.length - 17);
            return new String(plain, "UTF-8");
        } catch (Exception e) {
            // احتياطي فقط في حال تلف الملف — بلا أي معلومات حساسة
            return "الترخيص مشفر وخاص — تحت رعاية وتطوير مروان السيدي — Marwan Alsidi.\n"
                    + "لا يحق لأحد تعديل المشروع أو إعادة توزيعه إلا بمالك مفتاح المصدر الرسمي.\n"
                    + "جميع الحقوق محفوظة © 2026";
        }
    }

    /** نافذة سياسة الخصوصية الكاملة — متطلب نشر Google Play وحماية Play Protect */
    public static void showPrivacy(Activity act) {
        Theme th = Theme.of(act);
        TextView tv = new TextView(act);
        tv.setText(privacyText(act));
        tv.setTextSize(14);
        tv.setTextColor(th.text);
        tv.setLineSpacing(dp(act, 4), 1.15f);
        int p = dp(act, 20);
        tv.setPadding(p, p, p, p);

        ScrollView sc = new ScrollView(act);
        sc.addView(tv);
        Theme.dialog(act)
                .setTitle(Lang.t("سياسة الخصوصية", "Privacy policy",
                        "Politique de confidentialité", "Политика конфиденциальности"))
                .setView(sc)
                .setPositiveButton(Lang.t("إغلاق", "Close", "Fermer", "Закрыть"), null)
                .show();
    }

    /** نص سياسة الخصوصية (بأربع لغات) */
    private static String privacyText(Activity act) {
        return Lang.t(
                "منظومة البيانات — إدارة البيانات (إصدار 1.4.0)\n\n"
                + "1) تخزين البيانات: تعمل المنظومة بالكامل داخل هاتفك. تُحفظ بيانات الموظفين في قاعدة بيانات محلية (SQLite) مشفرة AES-256 داخل ذاكرة الهاتف، ولا تُرسل إلى أي خادم أو جهة خارجية مهما كان.\n\n"
                + "2) لا نجمع ولا نشارك: لا يحتوي التطبيق على إعلانات أو أدوات تتبع أو تحليلات، ولا يجمع أي بيانات شخصية، ولا يشارك أي بيانات مع أي طرف ثالث.\n\n"
                + "3) الأذونات المستخدمة:\n"
                + "• الإنترنت (INTERNET) و حالة الشبكة (ACCESS_NETWORK_STATE): لفحص التوفر على إصدار جديد من التطبيق فقط — لا تُستخدم لإرسال أي بيانات.\n\n"
                + "4) النسخ الاحتياطي والاستيراد: تصدير أو استيراد الملفات (Excel / SQL / CSV) يحدث بأمرك المباشر وعبر ملفاتك أنت فقط.\n\n"
                + "5) حذف البيانات: إزالة التطبيق من الهاتف تحذف كل البيانات نهائياً؛ ويمكنك أيضاً استخدام النسخ الاحتياطي للاحتفاظ بنسخة لديك.\n\n"
                + "6) الأمان: التطبيق موقّع رقمياً ومتوافق مع فحص Google Play Protect، ولا يطلب أي إذن خطير ولا يعمل في الخلفية.\n\n"
                + "7) التواصل: لأي استفسار حول الخصوصية تواصل مع مطوّر المنظومة عبر بيانات الترخيص داخل التطبيق.\n\n"
                + "آخر تحديث لهذه السياسة: نوفمبر 2026",
                "MMSA Data System — Data Administration (version 1.4.0)\n\n"
                + "1) Data storage: the system works entirely on your phone. Employee data is stored in a local (SQLite) database encrypted with AES-256 inside the device memory, and is never sent to any server or external party.\n\n"
                + "2) No collection, no sharing: the app contains no ads, trackers or analytics, collects no personal data, and shares nothing with third parties.\n\n"
                + "3) Permissions used:\n"
                + "• Internet (INTERNET) and network state (ACCESS_NETWORK_STATE): only to check whether a new app version is available — never used to send any data.\n\n"
                + "4) Backup & import: exporting or importing files (Excel / SQL / CSV) happens only on your direct command and with your own files.\n\n"
                + "5) Data deletion: uninstalling the app permanently deletes all data; you can also keep a backup copy you exported.\n\n"
                + "6) Security: the app is digitally signed and compliant with Google Play Protect, requests no dangerous permissions and runs nothing in the background.\n\n"
                + "7) Contact: for any privacy question contact the system developer via the license details inside the app.\n\n"
                + "Policy last updated: November 2026",
                "Système de données MMSA — Administration des données (version 1.4.0)\n\n"
                + "1) Stockage : le système fonctionne entièrement sur votre téléphone. Les données des employés sont enregistrées dans une base SQLite locale chiffrée AES-256 dans la mémoire de l'appareil, et ne sont jamais envoyées à un serveur ou à un tiers.\n\n"
                + "2) Aucune collecte, aucun partage : pas de publicité, pas de traqueurs, pas d'analyse, aucune donnée personnelle collectée, aucun partage avec des tiers.\n\n"
                + "3) Autorisations utilisées :\n"
                + "• Internet (INTERNET) et état du réseau (ACCESS_NETWORK_STATE) : uniquement pour vérifier la disponibilité d'une nouvelle version — jamais pour envoyer des données.\n\n"
                + "4) Sauvegarde et import : l'export ou l'import de fichiers (Excel / SQL / CSV) ne se fait que sur votre commande directe avec vos propres fichiers.\n\n"
                + "5) Suppression : désinstaller l'application supprime définitivement toutes les données ; vous pouvez garder une copie de sauvegarde exportée.\n\n"
                + "6) Sécurité : application signée numériquement, conforme à Google Play Protect, sans autorisations dangereuses ni traitement en arrière-plan.\n\n"
                + "7) Contact : pour toute question de confidentialité, contactez le développeur via les informations de licence dans l'application.\n\n"
                + "Dernière mise à jour : novembre 2026",
                "Система данных MMSA — управление данными (версия 1.4.0)\n\n"
                + "1) Хранение данных: система работает полностью на вашем телефоне. Данные сотрудников хранятся в локальной базе SQLite, зашифрованной AES-256, в памяти устройства и никогда не отправляются на серверы или третьим лицам.\n\n"
                + "2) Без сбора и передачи: в приложении нет рекламы, трекеров и аналитики, оно не собирает персональные данные и ничего не передаёт третьим сторонам.\n\n"
                + "3) Используемые разрешения:\n"
                + "• Интернет (INTERNET) и состояние сети (ACCESS_NETWORK_STATE): только для проверки наличия новой версии приложения — никогда для передачи данных.\n\n"
                + "4) Резервные копии и импорт: экспорт или импорт файлов (Excel / SQL / CSV) происходит только по вашей команде и с вашими файлами.\n\n"
                + "5) Удаление данных: удаление приложения полностью стирает все данные; можно также сохранить экспортированную резервную копию.\n\n"
                + "6) Безопасность: приложение подписано цифровой подписью, соответствует Google Play Protect, не запрашивает опасные разрешения и не работает в фоне.\n\n"
                + "7) Контакты: по вопросам конфиденциальности свяжитесь с разработчиком через данные лицензии в приложении.\n\n"
                + "Последнее обновление политики: ноябрь 2026");
    }

    /** نافذة الترخيص المنبثقة (مشفّرة وخاص) */
    public static void show(Activity act) {
        Theme th = Theme.of(act);
        TextView tv = new TextView(act);
        tv.setText(text(act));
        tv.setTextSize(14);
        tv.setTextColor(th.text);
        tv.setLineSpacing(dp(act, 4), 1.15f);
        int p = dp(act, 20);
        tv.setPadding(p, p, p, p);

        ScrollView sc = new ScrollView(act);
        sc.addView(tv);
        Theme.dialog(act)
                .setTitle(Lang.t("الترخيص — مشفر وخاص", "License — encrypted & private",
                        "Licence — chiffrée et privée", "Лицензия — зашифрованная и частная"))
                .setView(sc)
                .setPositiveButton(Lang.t("إغلاق", "Close", "Fermer", "Закрыть"), null)
                .show();
    }

    /** مقارنة الرقم السري بمقارنة زمنية ثابتة (بدون تسريب التوقيت) */
    public static boolean checkPin(String s) {
        byte[] want = deobf(PIN_ENC, PIN_ENC.length);
        byte[] got = (s == null ? "" : s.trim()).getBytes(StandardCharsets.UTF_8);
        boolean ok = want.length == got.length;
        int m = Math.min(want.length, got.length);
        for (int i = 0; i < m; i++) ok &= want[i] == got[i];
        for (int i = m; i < want.length; i++) ok &= want[i] == want[i]; // ثبات الزمن
        return ok;
    }

    /** مفتاح المصدر — يُطلق فقط بعد الرقم السري، ولا يظهر في أي واجهة عادية */
    public static String sourceKey() {
        return new String(deobf(SRC_ENC, SRC_ENC.length), StandardCharsets.UTF_8);
    }

    /** بوابة خفية: ضغط مطوّل على رقم الإصدار في «حول التطبيق» */
    public static void secretGate(Activity act) {
        Theme th = Theme.of(act);
        final EditText in = new EditText(act);
        in.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        in.setHint(Lang.t("الرقم السري", "Secret PIN", "Code secret", "Секретный PIN"));
        in.setTextSize(15);
        in.setTextColor(th.text);
        in.setHintTextColor(th.sub);
        in.setBackground(th.box(act, 14, th.fieldBg, th.line, 1.2f));
        int p = dp(act, 14);
        in.setPadding(p, p, p, p);

        LinearLayoutWrap wrap = new LinearLayoutWrap(act, in, th);
        Theme.dialog(act)
                .setTitle(Lang.t("منطقة محمية", "Protected area", "Zone protégée", "Защищённая зона"))
                .setMessage(Lang.t("أدخل الرقم السري الخاص بمطوّر المنظومة لفتح مفتاح المصدر",
                        "Enter the developer secret PIN to unlock the source key",
                        "Entrez le code secret du développeur pour débloquer la clé source",
                        "Введите секретный PIN-код разработчика"))
                .setView(wrap.root)
                .setPositiveButton(Lang.t("فتح", "Unlock", "Débloquer", "Открыть"), (d, w) -> {
                    if (checkPin(in.getText().toString())) {
                        Theme.dialog(act)
                                .setTitle(Lang.t("مفتاح المصدر — سري للغاية",
                                        "Source key — highly confidential",
                                        "Clé source — strictement confidentielle",
                                        "Ключ исходника — строго конфиденциально"))
                                .setMessage(Lang.t("المفتاح: ", "The key: ", "La clé : ", "Ключ: ")
                                        + sourceKey()
                                        + Lang.t("\n\nهذا المفتاح يعطي حق التعديل على المشروع وبنائه، ولا يُشارك مع أي جهة.",
                                        "\n\nThis key grants modification and build rights and must not be shared with anyone.",
                                        "\n\nCette clé donne le droit de modification et ne doit être partagée avec personne.",
                                        "\n\nЭтот ключ даёт право на изменение проекта и не подлежит передаче третьим лицам."))
                                .setPositiveButton(Lang.t("إغلاق", "Close", "Fermer", "Закрыть"), null)
                                .show();
                    } else {
                        android.widget.Toast.makeText(act,
                                Lang.t("الرقم السري غير صحيح", "Wrong PIN",
                                        "Code incorrect", "Неверный PIN"),
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(Lang.t("إلغاء", "Cancel", "Annuler", "Отмена"), null)
                .show();
    }

    /** غلاف بسيط لحقل الإدخال داخل الحوار */
    private static class LinearLayoutWrap {
        final ScrollView root;

        LinearLayoutWrap(Activity act, EditText in, Theme th) {
            android.widget.LinearLayout l = new android.widget.LinearLayout(act);
            l.setOrientation(android.widget.LinearLayout.VERTICAL);
            int pad = dp(act, 22);
            l.setPadding(pad, dp(act, 8), pad, 0);
            l.addView(in, new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
            root = new ScrollView(act);
            root.addView(l);
        }
    }

    private static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }
}
