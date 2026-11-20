package com.mmsa.records;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** فحص التحديثات: تلقائي عند الاتصال + يدوي، ونافذة «ما الجديد» للإصدار الجديد فقط */
public class Updater {

    /** رابط ملف التحديثات على مستودع المشروع الرسمي */
    public static final String UPDATE_URL =
            "https://raw.githubusercontent.com/Alsidi-Group/MMSA/main/update.json";

    public static final int VERSION_CODE = 9;
    public static final String VERSION_NAME = "1.4.2";

    public static class Info {
        public String versionName;
        public int versionCode;
        public String url;
        public String notes;
    }

    private static final String PREF = "mmsa";
    private static final String KEY_LAST = "lastPromptedCode";
    private static final String KEY_LAST_CHECK = "lastAutoCheckMs";
    /** المهلة بين فحصين تلقائيين: 4 ساعات */
    private static final long CHECK_INTERVAL_MS = 4L * 60 * 60 * 1000;

    /** هل الجهاز متصل فعلاً بالإنترنت؟ */
    public static boolean online(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            android.net.Network nw = cm.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities cap = cm.getNetworkCapabilities(nw);
            return cap != null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            return false;
        }
    }

    public static Info fetch() throws Exception {
        HttpURLConnection cn = (HttpURLConnection) new URL(UPDATE_URL).openConnection();
        cn.setConnectTimeout(10000);
        cn.setReadTimeout(10000);
        cn.setRequestProperty("Cache-Control", "no-cache");
        int code = cn.getResponseCode();
        InputStream in = code >= 400 ? cn.getErrorStream() : cn.getInputStream();
        if (in == null) throw new Exception("تعذر الاتصال بالخادم (HTTP " + code + ")");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        in.close();
        JSONObject o = new JSONObject(b.toString("UTF-8"));
        Info i = new Info();
        i.versionName = o.optString("versionName", "1.0.0");
        i.versionCode = o.optInt("versionCode", 1);
        i.url = o.optString("url", "");
        i.notes = o.optString("notes", "");
        return i;
    }

    /** مقارنة إصدارات مثل 1.0.0 مع 1.10.0 */
    public static int cmp(String a, String b) {
        String[] A = a.trim().split("\\.");
        String[] B = b.trim().split("\\.");
        for (int i = 0; i < Math.max(A.length, B.length); i++) {
            int x = i < A.length ? parse(A[i]) : 0;
            int y = i < B.length ? parse(B[i]) : 0;
            if (x != y) return Integer.compare(x, y);
        }
        return 0;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean isNewer(Info i) {
        return i.versionCode > VERSION_CODE || cmp(i.versionName, VERSION_NAME) > 0;
    }

    /** فحص تلقائي صامت عند فتح التطبيق: النافذة تظهر مرة واحدة لكل إصدار جديد فقط */
    public static void autoCheck(final Activity act) {
        if (!online(act)) return;
        act.getSharedPreferences(PREF, 0).edit()
                .putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
        new Thread(() -> {
            try {
                final Info i = fetch();
                if (isNewer(i)) {
                    SharedPreferences sp = act.getSharedPreferences(PREF, 0);
                    if (sp.getInt(KEY_LAST, 0) < i.versionCode) {
                        sp.edit().putInt(KEY_LAST, i.versionCode).apply();
                        act.runOnUiThread(() -> showNewVersion(act, i));
                    }
                }
            } catch (Exception ignored) {
                // فحص صامت — لا رسائل خطأ في الخلفية
            }
        }).start();
    }

    /** فحص تلقائي بشرط المهلة: لا يكرر الفحص قبل مرور 4 ساعات */
    public static void autoCheckIfDue(final Activity act) {
        long last = act.getSharedPreferences(PREF, 0).getLong(KEY_LAST_CHECK, 0L);
        if (System.currentTimeMillis() - last >= CHECK_INTERVAL_MS) autoCheck(act);
    }

    /**
     * مستقبِل حالة الاتصال: يبحث عن التحديث تلقائياً لحظة عودة الإنترنت
     * أثناء تشغيل التطبيق (يُسجَّل وقت تشغيل الواجهة ويُلغى عند إيقافها).
     */
    public static android.content.BroadcastReceiver connectivityReceiver(final Activity act) {
        return new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context c, android.content.Intent i) {
                if (online(act)) autoCheckIfDue(act);
            }
        };
    }

    /** فحص يدوي من زر «البحث عن تحديثات» — يعرض النتيجة دائماً */
    public static void check(final Activity act) {
        new Thread(() -> {
            try {
                final Info i = fetch();
                act.runOnUiThread(() -> {
                    if (isNewer(i)) {
                        showNewVersion(act, i);
                    } else {
                        Theme.dialog(act)
                                .setTitle(Lang.t("البحث عن تحديثات", "Check for updates",
                                        "Rechercher des mises à jour", "Проверка обновлений"))
                                .setMessage(Lang.t("أنت تستخدم أحدث إصدار (", "You are using the latest version (",
                                        "Vous utilisez la dernière version (", "У вас последняя версия (")
                                        + VERSION_NAME + ") — "
                                        + Lang.t("لا توجد تحديثات جديدة حالياً.",
                                        "no new updates available.",
                                        "aucune mise à jour disponible.",
                                        "обновлений нет."))
                                .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                                .show();
                    }
                });
            } catch (Exception e) {
                final String msg = e.getMessage();
                act.runOnUiThread(() -> Theme.dialog(act)
                        .setTitle(Lang.t("تعذر الوصول لخدمة التحديثات", "Update service unreachable",
                                "Service de mise à jour inaccessible", "Сервис обновлений недоступен"))
                        .setMessage((msg == null ? Lang.t("خطأ غير معروف", "Unknown error",
                                "Erreur inconnue", "Неизвестная ошибка") : msg)
                                + Lang.t("\n\nتأكد من اتصال الهاتف بالإنترنت.",
                                "\n\nMake sure the phone is connected to the internet.",
                                "\n\nVérifiez la connexion internet du téléphone.",
                                "\n\nПроверьте подключение телефона к интернету."))
                        .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                        .show());
            }
        }).start();
    }

    /** النافذة المنبثقة: «يوجد إصدار جديد … ما الجديد في هذا الإصدار» — بيانات الإصدار الجديد فقط */
    public static void showNewVersion(final Activity act, final Info i) {
        Theme th = Theme.of(act);
        int dp16 = Theme.dp(act, 16);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        int p = Theme.dp(act, 20);
        root.setPadding(p, Theme.dp(act, 16), p, Theme.dp(act, 4));

        // ترويسة: أيقونة داخل مربع متدرج + النصان
        LinearLayout head = new LinearLayout(act);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        ImageView iv = new ImageView(act);
        iv.setImageDrawable(th.icon(act, "update", th.onPrimary, 22));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackground(th.gradient(act, 14, th.primary, th.primaryDark));
        iv.setPadding(Theme.dp(act, 9), Theme.dp(act, 9), Theme.dp(act, 9), Theme.dp(act, 9));
        head.addView(iv, new LinearLayout.LayoutParams(Theme.dp(act, 40), Theme.dp(act, 40)));
        LinearLayout ht = new LinearLayout(act);
        ht.setOrientation(LinearLayout.VERTICAL);
        TextView t1 = th.text(act, Lang.t("يوجد إصدار جديد (", "A new version is available (",
                "Une nouvelle version est disponible (", "Доступна новая версия (")
                + i.versionName + ")", 16.5f, th.primary, true);
        TextView t2 = th.text(act, Lang.t("ما الجديد في هذا الإصدار؟", "What's new in this version?",
                "Quoi de neuf dans cette version ?", "Что нового в этой версии?"), 12.5f, th.sub, false);
        ht.addView(t1);
        ht.addView(t2);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hp.rightMargin = Theme.dp(act, 12);
        head.addView(ht, hp);
        root.addView(head, match());

        TextView ln = th.text(act, Lang.t("أصدارك الحالي: ", "Your current version: ",
                "Votre version actuelle : ", "Ваша текущая версия: ")
                + VERSION_NAME + " (" + VERSION_CODE + ")",
                12, th.sub, false);
        ln.setPadding(0, Theme.dp(act, 8), 0, Theme.dp(act, 4));
        root.addView(ln, match());

        // عناصر «ما الجديد» من ملاحظات update.json
        LinearLayout items = new LinearLayout(act);
        items.setOrientation(LinearLayout.VERTICAL);
        int added = addNoteItems(act, th, items, i.notes);
        if (added == 0) {
            addNoteItem(act, th, items, Lang.t("تحسينات عامة", "General improvements",
                    "Améliorations générales", "Общие улучшения"),
                    Lang.t("تحسينات عامة وإصلاحات في هذا الإصدار.",
                            "General improvements and fixes in this version.",
                            "Améliorations et corrections dans cette version.",
                            "Общие улучшения и исправления в этой версии."), "check");
        }
        root.addView(items, match());

        ScrollView sc = new ScrollView(act);
        sc.addView(root);

        // زرا التنزيل واللاحقاً
        LinearLayout btns = new LinearLayout(act);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setPadding(Theme.dp(act, 16), Theme.dp(act, 8), Theme.dp(act, 16), Theme.dp(act, 8));
        Button dl = th.bigButton(act, Lang.t("تنزيل الإصدار الجديد", "Download the new version",
                "Télécharger la nouvelle version", "Скачать новую версию"), "export", true);
        dl.setOnClickListener(v -> {
            try {
                act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(i.url)));
            } catch (Exception ignored) {}
        });
        btns.addView(dl, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button later = th.button(act, Lang.t("لاحقاً", "Later", "Plus tard", "Позже"), false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Theme.dp(act, 8);
        btns.addView(later, lp);

        LinearLayout outer = new LinearLayout(act);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.addView(sc, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        outer.addView(btns, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Theme.dialog(act)
                .setTitle(Lang.t("التحديثات", "Updates", "Mises à jour", "Обновления"))
                .setView(outer)
                .setPositiveButton(Lang.t("إغلاق", "Close", "Fermer", "Закрыть"), null)
                .show();
    }

    private static LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /** تحليل ملاحظات الإصدار: كل سطر يبدأ بـ «•» عنوان، وما بعده وصف البند */
    private static int addNoteItems(Activity act, Theme th, LinearLayout box, String notes) {
        if (notes == null || notes.trim().isEmpty()) return 0;
        String[] lines = notes.replace("\r", "").split("\n");
        String head = null, body = new String();
        int count = 0;
        for (int i = 0; i <= lines.length; i++) {
            String ln = i < lines.length ? lines[i].trim() : "";
            boolean isHead = ln.startsWith("•") || ln.startsWith("*");
            if ((isHead || i == lines.length) && head != null) {
                String icon = head.contains("إضافة") || head.contains("اضافة") ? "plus"
                        : head.contains("أمن") || head.contains("امن") ? "shield"
                        : head.contains("تعديل") ? "edit"
                        : head.contains("إصلاح") || head.contains("اصلاح") ? "tools" : "check";
                addNoteItem(act, th, box, head, body.trim(), icon);
                count++;
                body = new String();
            }
            if (isHead) head = ln.replaceFirst("^[•*]\\s*", "").replace(":", "").trim();
            else if (!ln.isEmpty()) body = body.isEmpty() ? ln : body + "\n" + ln;
        }
        return count;
    }

    private static void addNoteItem(Activity act, Theme th, LinearLayout box,
                                    String head, String body, String icon) {
        LinearLayout row = new LinearLayout(act);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int pad = Theme.dp(act, 10);
        row.setPadding(0, pad, 0, pad);

        ImageView iv = new ImageView(act);
        iv.setImageDrawable(th.icon(act, icon, th.onPrimary, 16));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackground(th.box(act, 10, th.primary, 0, 0));
        iv.setPadding(Theme.dp(act, 7), Theme.dp(act, 7), Theme.dp(act, 7), Theme.dp(act, 7));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                Theme.dp(act, 30), Theme.dp(act, 30));
        ip.rightMargin = Theme.dp(act, 10);
        row.addView(iv, ip);

        LinearLayout tx = new LinearLayout(act);
        tx.setOrientation(LinearLayout.VERTICAL);
        TextView h = th.text(act, head, 14, th.primary, true);
        tx.addView(h);
        if (body != null && !body.isEmpty()) {
            TextView b = th.text(act, body, 12.5f, th.sub, false);
            tx.addView(b, match());
        }
        row.addView(tx, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        box.addView(row, match());
    }
}
