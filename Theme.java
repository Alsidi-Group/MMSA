package com.mmsa.records;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * نظام التصميم UI/UX:
 * 6 باقات لونية × 3 أوضاع (نهاري / ليلي / تلقائي حسب النظام) + خط Tajawal الأنيق +
 * مصنع مكوّنات + أيقونات مرسومة + حقول مقيّدة بأيقونات + نوافذ منبثقة بنفس الثيم تلقائياً.
 */
public class Theme {

    public static final String[] THEME_NAMES = {
            "عسكري أخضر", "كحلي رسمي", "رملي صحراوي", "بحري تركواز", "بنفسجي ملكي", "نبيذي ملكي",
            "أزرق سماوي", "وردي هادئ", "زيتوني", "رمادي فولاذي"
    };

    /** لون عينة كل باقة لعرضها في قائمة الثيمات */
    public static final int[] PALETTE_SWATCH = {
            0xFF1F5C3D, 0xFF1F3A5F, 0xFF9C7A3C, 0xFF0E7490, 0xFF5B3A8F, 0xFF8C2B2B,
            0xFF0B72B9, 0xFFB4426C, 0xFF6B7A2F, 0xFF4A5568
    };

    public static final int MODE_DAY = 0, MODE_NIGHT = 1, MODE_AUTO = 2;
    public static final String[] MODE_NAMES = {"نهاري ☀️", "ليلي 🌙", "تلقائي حسب النظام 🔄"};

    public int primary, primaryDark, accent, bg, card, text, sub, onPrimary, danger, fieldBg, line;
    public boolean night;

    // ---------- الخط الأنيق (Tajawal) ----------

    public static Typeface FONT_R, FONT_M, FONT_B;
    private static boolean fontsInit = false;

    public static void initFonts(Context c) {
        if (fontsInit) return;
        fontsInit = true;
        if (Build.VERSION.SDK_INT >= 26) {
            try { FONT_R = c.getResources().getFont(R.font.tajawal_regular); } catch (Exception ignored) {}
            try { FONT_M = c.getResources().getFont(R.font.tajawal_medium); } catch (Exception ignored) {}
            try { FONT_B = c.getResources().getFont(R.font.tajawal_bold); } catch (Exception ignored) {}
        }
        if (FONT_R == null) FONT_R = Typeface.create("sans-serif", Typeface.NORMAL);
        if (FONT_M == null) FONT_M = Typeface.create("sans-serif", Typeface.NORMAL);
        if (FONT_B == null) FONT_B = Typeface.create("sans-serif", Typeface.BOLD);
    }

    public static Typeface font(Context c, boolean bold) {
        initFonts(c);
        return bold ? FONT_B : FONT_M;
    }

    public static Typeface fontRegular(Context c) {
        initFonts(c);
        return FONT_R;
    }

    private void set(int p, boolean n) {
        night = n;
        if (p == 0) { // عسكري أخضر
            primary = n ? 0xFF2E7D4F : 0xFF1F5C3D; primaryDark = n ? 0xFF1B5E38 : 0xFF14402A;
            bg = n ? 0xFF121713 : 0xFFF1F4EE; card = n ? 0xFF1D231E : 0xFFFFFFFF;
            text = n ? 0xFFEDEDED : 0xFF17251C; sub = n ? 0xFFA3AFA5 : 0xFF5A6B5D;
            fieldBg = n ? 0xFF232A24 : 0xFFF7F9F4; line = n ? 0xFF35403A : 0xFFD7DFD6;
        } else if (p == 1) { // كحلي رسمي
            primary = n ? 0xFF3E6699 : 0xFF1F3A5F; primaryDark = n ? 0xFF27436B : 0xFF152840;
            bg = n ? 0xFF11151B : 0xFFF0F3F8; card = n ? 0xFF1B2129 : 0xFFFFFFFF;
            text = n ? 0xFFE8E8E8 : 0xFF141E2C; sub = n ? 0xFF9FAABA : 0xFF5A6B80;
            fieldBg = n ? 0xFF212833 : 0xFFF6F8FB; line = n ? 0xFF333D4A : 0xFFD5DCE6;
        } else if (p == 2) { // رملي صحراوي
            primary = n ? 0xFFB08B4D : 0xFF9C7A3C; primaryDark = n ? 0xFF7A6236 : 0xFF6E5628;
            bg = n ? 0xFF171410 : 0xFFF8F3E9; card = n ? 0xFF232018 : 0xFFFFFFFF;
            text = n ? 0xFFEDE7DB : 0xFF2A2416; sub = n ? 0xFFABA18D : 0xFF7A6B4E;
            fieldBg = n ? 0xFF28231A : 0xFFFBF7EE; line = n ? 0xFF3B352A : 0xFFE2D8C2;
        } else if (p == 3) { // بحري تركواز
            primary = n ? 0xFF2E9CB8 : 0xFF0E7490; primaryDark = n ? 0xFF1D6E85 : 0xFF0A5568;
            bg = n ? 0xFF0F171A : 0xFFEFF6F7; card = n ? 0xFF192226 : 0xFFFFFFFF;
            text = n ? 0xFFE5EDEF : 0xFF12262B; sub = n ? 0xFF93A8AE : 0xFF5A7A80;
            fieldBg = n ? 0xFF1E2A30 : 0xFFF4FAFA; line = n ? 0xFF2F4046 : 0xFFD3E3E5;
        } else if (p == 4) { // بنفسجي ملكي
            primary = n ? 0xFF7A50B8 : 0xFF5B3A8F; primaryDark = n ? 0xFF5C3A94 : 0xFF43276E;
            bg = n ? 0xFF14111B : 0xFFF3F1F8; card = n ? 0xFF1E1A28 : 0xFFFFFFFF;
            text = n ? 0xFFEAE7F0 : 0xFF1E1730; sub = n ? 0xFFA49BB8 : 0xFF6B5F85;
            fieldBg = n ? 0xFF251F31 : 0xFFF8F6FC; line = n ? 0xFF39304A : 0xFFDDD8EA;
        } else if (p == 5) { // نبيذي ملكي
            primary = n ? 0xFFB04040 : 0xFF8C2B2B; primaryDark = n ? 0xFF8C2F2F : 0xFF6B1F1F;
            bg = n ? 0xFF171010 : 0xFFF8F1F0; card = n ? 0xFF231919 : 0xFFFFFFFF;
            text = n ? 0xFFEFE7E7 : 0xFF2B1414; sub = n ? 0xFFB19A9A : 0xFF7D5A5A;
            fieldBg = n ? 0xFF2A1E1E : 0xFFFBF6F5; line = n ? 0xFF413030 : 0xFFE7D5D3;
        } else if (p == 6) { // أزرق سماوي
            primary = n ? 0xFF2E93D6 : 0xFF0B72B9; primaryDark = n ? 0xFF1D6EA6 : 0xFF085A95;
            bg = n ? 0xFF0F151B : 0xFFEAF4FC; card = n ? 0xFF19202A : 0xFFFFFFFF;
            text = n ? 0xFFE7EDF3 : 0xFF10222E; sub = n ? 0xFF98A7B5 : 0xFF5A7080;
            fieldBg = n ? 0xFF1E2733 : 0xFFF2F9FE; line = n ? 0xFF303D4B : 0xFFCFE2EF;
        } else if (p == 7) { // وردي هادئ
            primary = n ? 0xFFC9638A : 0xFFB4426C; primaryDark = n ? 0xFF9E4B6E : 0xFF8C3153;
            bg = n ? 0xFF181114 : 0xFFFBF0F4; card = n ? 0xFF241B1F : 0xFFFFFFFF;
            text = n ? 0xFFEFE6EA : 0xFF2C161E; sub = n ? 0xFFB49BA6 : 0xFF83616E;
            fieldBg = n ? 0xFF2A2126 : 0xFFFEF5F8; line = n ? 0xFF413239 : 0xFFEFD5DF;
        } else if (p == 8) { // زيتوني
            primary = n ? 0xFF8A9B3D : 0xFF6B7A2F; primaryDark = n ? 0xFF6B7A2F : 0xFF525F23;
            bg = n ? 0xFF14160E : 0xFFF3F6E9; card = n ? 0xFF1F2317 : 0xFFFFFFFF;
            text = n ? 0xFFEBEDDF : 0xFF232615; sub = n ? 0xFFA6AC8F : 0xFF6D7350;
            fieldBg = n ? 0xFF252B1B : 0xFFF9FBF1; line = n ? 0xFF394027 : 0xFFDFE6C8;
        } else if (p == 9) { // رمادي فولاذي
            primary = n ? 0xFF64748B : 0xFF4A5568; primaryDark = n ? 0xFF4B5768 : 0xFF363F4E;
            bg = n ? 0xFF121417 : 0xFFEFF1F4; card = n ? 0xFF1C1F24 : 0xFFFFFFFF;
            text = n ? 0xFFE8EAEE : 0xFF1A2026; sub = n ? 0xFFA0A8B3 : 0xFF64707D;
            fieldBg = n ? 0xFF23272D : 0xFFF6F8FA; line = n ? 0xFF363B44 : 0xFFD9DEE5;
        } else { // احتياطي: الباقة الأولى
            primary = n ? 0xFF2E7D4F : 0xFF1F5C3D; primaryDark = n ? 0xFF1B5E38 : 0xFF14402A;
            bg = n ? 0xFF121713 : 0xFFF1F4EE; card = n ? 0xFF1D231E : 0xFFFFFFFF;
            text = n ? 0xFFEDEDED : 0xFF17251C; sub = n ? 0xFFA3AFA5 : 0xFF5A6B5D;
            fieldBg = n ? 0xFF232A24 : 0xFFF7F9F4; line = n ? 0xFF35403A : 0xFFD7DFD6;
        }
        accent = 0xFFC9A227;
        onPrimary = 0xFFFFFFFF;
        danger = n ? 0xFFEF5350 : 0xFFB3261E;
    }

    public static int index(Context c) {
        int i = c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).getInt("theme", 0);
        if (i < 0 || i > THEME_NAMES.length - 1) i = 0;
        return i;
    }

    public static void save(Context c, int i) {
        if (i < 0 || i > THEME_NAMES.length - 1) i = 0;
        c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).edit().putInt("theme", i).apply();
        bumpUi(c); // إجبار كل الشاشات المفتوحة على إعادة البناء بالثيم الجديد
    }

    public static int mode(Context c) {
        int m = c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).getInt("mode", MODE_DAY);
        if (m < 0 || m > MODE_AUTO) m = MODE_DAY;
        return m;
    }

    public static void saveMode(Context c, int m) {
        if (m < 0 || m > MODE_AUTO) m = MODE_DAY;
        c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).edit().putInt("mode", m).apply();
        bumpUi(c);
    }

    // ---------- نبضة الواجهة: إعادة بناء كل الشاشات عند تغيير الثيم/اللغة ----------

    /** طابع زمني لآخر تغيير في المظهر أو اللغة — يقارنه كل شاشة عند العودة */
    public static void bumpUi(Context c) {
        c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).edit()
                .putLong("uiEpoch", System.currentTimeMillis()).apply();
    }

    public static long uiEpoch(Context c) {
        return c.getSharedPreferences("mmsa", Context.MODE_PRIVATE).getLong("uiEpoch", 0L);
    }

    /** هل النظام حالياً في الوضع الليلي؟ */
    public static boolean systemNight(Context c) {
        return (c.getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    /** الحسم النهائي: ليلي فقط إذا اختار المستخدم الليلي أو التلقائي والنظام ليلي */
    public static boolean isNight(Context c) {
        int m = mode(c);
        return m == MODE_NIGHT || (m == MODE_AUTO && systemNight(c));
    }

    public static Theme of(Context c) {
        Theme t = new Theme();
        t.set(index(c), isNight(c));
        initFonts(c);
        return t;
    }

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    // ---------- النوافذ المنبثقة بنفس الثيم (فاتحة/داكنة تلقائياً) ----------

    public static int dialogStyle(Context c) {
        return isNight(c) ? R.style.MMSA_Dialog_Dark : R.style.MMSA_Dialog_Light;
    }

    /** سياق بنفس ثيم النوافذ المنبثقة (لمنتقي التاريخ مثلاً) */
    public static Context dialogCtx(Context c) {
        return new ContextThemeWrapper(c, dialogStyle(c));
    }

    /** باني نوافذ منبثقة بخلفية بنفس الثيم (لا أبيض إجباري) وبخط التطبيق */
    public static AlertDialog.Builder dialog(Context c) {
        return new AlertDialog.Builder(dialogCtx(c));
    }

    // ---------- البحث الذكي: توقف عن الكتابة → تنفيذ ----------

    /** مستمع "توقف عن الكتابة": ينفّذ العمل بعد انتهاء الكتابة بفترة سكون */
    public static class StopTyping implements TextWatcher {
        private final Handler h = new Handler(Looper.getMainLooper());
        private final long delay;
        private final Runnable onStop;
        private Runnable pending;
        private boolean self = false;

        public StopTyping(long delayMs, Runnable onStop) {
            this.delay = delayMs;
            this.onStop = onStop;
        }

        public void setSelf(boolean v) { self = v; }

        @Override
        public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (self) return;
            if (pending != null) h.removeCallbacks(pending);
            pending = onStop;
            h.postDelayed(pending, delay);
        }
    }

    /** مدقق الرقم الوطني: عند التوقف بقيمة غير فارغة وبطول ≠ 12 → نافذة تنبيه فورية */
    public static void attachNationalValidator(final Activity act, final EditText e) {
        e.addTextChangedListener(new StopTyping(900, () -> {
            String s = Employee.digitsOnly(e.getText().toString());
            if (!s.isEmpty() && s.length() != 12) {
                dialog(act)
                        .setTitle(Lang.t("تنبيه", "Warning", "Attention", "Внимание"))
                        .setMessage(Lang.t("يرجى اعادة التأكد من الرقم الوطني 12 رقم",
                                "Please re-check the national number — it must be 12 digits",
                                "Veuillez revérifier le numéro national — 12 chiffres requis",
                                "Проверьте национальный номер — он должен состоять из 12 цифр"))
                        .setPositiveButton(Lang.t("حسناً", "OK", "OK", "OK"), null)
                        .show();
            }
        }));
    }

    // ---------- نصوص ----------

    public TextView text(Context c, String s, float sizeSp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sizeSp);
        t.setTextColor(color);
        initFonts(c);
        t.setTypeface(bold ? FONT_B : FONT_R);
        return t;
    }

    public TextView label(Context c, String s) {
        TextView t = text(c, s, 13, sub, true);
        t.setPadding(dp(c, 2), dp(c, 10), dp(c, 2), dp(c, 4));
        return t;
    }

    // ---------- أشكال وخلفيات ----------

    public GradientDrawable box(Context c, float radiusDp, int fill, int strokeColor, float strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(dp(c, radiusDp));
        g.setColor(fill);
        if (strokeColor != 0) g.setStroke(Math.max(1, dp(c, strokeDp)), strokeColor);
        return g;
    }

    /** خلفية متدرجة للأزرار الرئيسية والرأس */
    public GradientDrawable gradient(Context c, float radiusDp, int c1, int c2) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{c1, blend(c1, c2, 0.45f), c2});
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static int blend(int a, int b, float f) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg2 = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * f), g2 = Math.round(ag + (bg2 - ag) * f),
                bl = Math.round(ab + (bb - ab) * f);
        return 0xFF000000 | (r << 16) | (g2 << 8) | bl;
    }

    public LinearLayout card(Context c, float radiusDp) {
        return card(c, radiusDp, 14);
    }

    public LinearLayout card(Context c, float radiusDp, int paddingDp) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackground(box(c, radiusDp, card, line, 1f));
        int p = dp(c, paddingDp);
        l.setPadding(p, p, p, p);
        l.setElevation(dp(c, 3));
        return l;
    }

    /** شريحة pill صغيرة */
    public TextView chip(Context c, String s, int bg, int fg) {
        TextView t = text(c, s, 12, fg, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(box(c, 18, bg, 0, 0));
        t.setPadding(dp(c, 12), dp(c, 5), dp(c, 12), dp(c, 5));
        return t;
    }

    /** عنوان قسم مع أيقونة داخل مربع ملوّن */
    public LinearLayout sectionHeader(Context c, String icon, String title) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);

        ImageView box = new ImageView(c);
        box.setImageDrawable(icon(c, icon, onPrimary, 18));
        box.setScaleType(ImageView.ScaleType.FIT_CENTER);
        box.setBackground(gradient(c, 12, primary, primaryDark));
        box.setPadding(dp(c, 8), dp(c, 8), dp(c, 8), dp(c, 8));
        l.addView(box, new LinearLayout.LayoutParams(dp(c, 34), dp(c, 34)));

        TextView t = text(c, title, 15.5f, primary, true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.setMarginEnd(dp(c, 10));
        tp.setMarginStart(dp(c, 4));
        l.addView(t, tp);

        View ln = new View(c);
        ln.setBackgroundColor(line);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(c, 1.4f), 1f);
        l.addView(ln, lp);
        return l;
    }

    // ---------- أيقونات مرسومة بالكود (بدون مكتبات) ----------

    public android.graphics.drawable.Drawable icon(Context c, String k, int color, int sizeDp) {
        int s = dp(c, sizeDp);
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1.8f, s * 0.075f));
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        glyph(cv, p, s, k);
        return new BitmapDrawable(c.getResources(), b);
    }

    private static float u(int s, double v) { return (float) (v * s / 24f); }

    private void glyph(Canvas cv, Paint p, int s, String k) {
        Paint fp = new Paint(p);
        fp.setStyle(Paint.Style.FILL);
        switch (k) {
            case "person": {
                cv.drawCircle(u(s, 12), u(s, 8), u(s, 4), p);
                cv.drawArc(new RectF(u(s, 4.5f), u(s, 13), u(s, 19.5f), u(s, 27)), 180, 180, false, p);
                break;
            }
            case "people": {
                cv.drawCircle(u(s, 9), u(s, 8), u(s, 3.5f), p);
                cv.drawArc(new RectF(u(s, 2.5f), u(s, 13.5f), u(s, 15.5f), u(s, 25)), 180, 180, false, p);
                cv.drawCircle(u(s, 17), u(s, 9.5f), u(s, 2.8f), p);
                cv.drawArc(new RectF(u(s, 13), u(s, 14.5f), u(s, 23), u(s, 23)), 190, 140, false, p);
                break;
            }
            case "id": {
                cv.drawRoundRect(new RectF(u(s, 2.5f), u(s, 5), u(s, 21.5f), u(s, 19)), u(s, 2.5f), u(s, 2.5f), p);
                cv.drawCircle(u(s, 8), u(s, 10.5f), u(s, 2.2f), p);
                cv.drawLine(u(s, 11.5f), u(s, 9.5f), u(s, 19), u(s, 9.5f), p);
                cv.drawLine(u(s, 11.5f), u(s, 13.5f), u(s, 17), u(s, 13.5f), p);
                cv.drawLine(u(s, 5), u(s, 15.5f), u(s, 11), u(s, 15.5f), p);
                break;
            }
            case "badge": star(cv, p, u(s, 12), u(s, 12.5f), u(s, 8.2f), u(s, 3.6f), 5);
                break;
            case "calendar": {
                cv.drawRoundRect(new RectF(u(s, 3.5f), u(s, 5.5f), u(s, 20.5f), u(s, 19.5)), u(s, 2.5f), u(s, 2.5f), p);
                cv.drawLine(u(s, 3.5f), u(s, 10), u(s, 20.5f), u(s, 10), p);
                cv.drawLine(u(s, 7.5f), u(s, 3.5f), u(s, 7.5f), u(s, 7), p);
                cv.drawLine(u(s, 16.5f), u(s, 3.5f), u(s, 16.5f), u(s, 7), p);
                cv.drawCircle(u(s, 9), u(s, 14.5), u(s, 1.1), fp);
                cv.drawCircle(u(s, 15), u(s, 14.5), u(s, 1.1), fp);
                break;
            }
            case "bank": {
                Path roof = new Path();
                roof.moveTo(u(s, 12), u(s, 3.5f));
                roof.lineTo(u(s, 21), u(s, 8.5f));
                roof.lineTo(u(s, 3), u(s, 8.5f));
                roof.close();
                cv.drawPath(roof, p);
                cv.drawLine(u(s, 6), u(s, 11.5f), u(s, 6), u(s, 17.5f), p);
                cv.drawLine(u(s, 12), u(s, 11.5f), u(s, 12), u(s, 17.5f), p);
                cv.drawLine(u(s, 18), u(s, 11.5f), u(s, 18), u(s, 17.5f), p);
                cv.drawLine(u(s, 3.5f), u(s, 20), u(s, 20.5f), u(s, 20), p);
                break;
            }
            case "money": {
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 9), p);
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 5), p);
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 1.6f), fp);
                break;
            }
            case "home": {
                Path r = new Path();
                r.moveTo(u(s, 3.5f), u(s, 12));
                r.lineTo(u(s, 12), u(s, 4));
                r.lineTo(u(s, 20.5f), u(s, 12));
                cv.drawPath(r, p);
                cv.drawRoundRect(new RectF(u(s, 6), u(s, 11.5f), u(s, 18), u(s, 20)), u(s, 1.5f), u(s, 1.5f), p);
                break;
            }
            case "doc": {
                Path pg = new Path();
                pg.moveTo(u(s, 5.5f), u(s, 3.5f));
                pg.lineTo(u(s, 14), u(s, 3.5f));
                pg.lineTo(u(s, 19), u(s, 8.5f));
                pg.lineTo(u(s, 19), u(s, 20.5f));
                pg.lineTo(u(s, 5.5f), u(s, 20.5f));
                pg.close();
                cv.drawPath(pg, p);
                cv.drawLine(u(s, 14), u(s, 3.5f), u(s, 14), u(s, 8.5f), p);
                cv.drawLine(u(s, 8.5f), u(s, 12.5f), u(s, 15.5f), u(s, 12.5f), p);
                cv.drawLine(u(s, 8.5f), u(s, 16), u(s, 15.5f), u(s, 16), p);
                break;
            }
            case "star": star(cv, p, u(s, 12), u(s, 12.5f), u(s, 9), u(s, 4), 5);
                break;
            case "search": {
                cv.drawCircle(u(s, 10.5f), u(s, 10.5f), u(s, 6.5f), p);
                cv.drawLine(u(s, 15.5f), u(s, 15.5f), u(s, 20.5f), u(s, 20.5f), p);
                break;
            }
            case "back": {
                // سهم الرجوع — اتجاهه حسب لغة الواجهة (يمين للعربية RTL، يسار للاتينية)
                Path a = new Path();
                if (Lang.rtl()) {
                    a.moveTo(u(s, 9), u(s, 5));
                    a.lineTo(u(s, 15.5f), u(s, 12));
                    a.lineTo(u(s, 9), u(s, 19));
                } else {
                    a.moveTo(u(s, 15), u(s, 5));
                    a.lineTo(u(s, 8.5f), u(s, 12));
                    a.lineTo(u(s, 15), u(s, 19));
                }
                cv.drawPath(a, p);
                break;
            }
            case "sort": {
                cv.drawLine(u(s, 5), u(s, 6), u(s, 19), u(s, 6), p);
                cv.drawLine(u(s, 5), u(s, 11), u(s, 15.5f), u(s, 11), p);
                cv.drawLine(u(s, 5), u(s, 16), u(s, 12), u(s, 16), p);
                Path a = new Path();
                a.moveTo(u(s, 17.5f), u(s, 13));
                a.lineTo(u(s, 20.5f), u(s, 17));
                a.lineTo(u(s, 14.5f), u(s, 17));
                cv.drawPath(a, p);
                fp.setColor(p.getColor());
                cv.drawPath(a, fp);
                break;
            }
            case "list": {
                cv.drawLine(u(s, 9), u(s, 6), u(s, 20), u(s, 6), p);
                cv.drawLine(u(s, 9), u(s, 12), u(s, 20), u(s, 12), p);
                cv.drawLine(u(s, 9), u(s, 18), u(s, 20), u(s, 18), p);
                cv.drawCircle(u(s, 4.5f), u(s, 6), u(s, 1.4f), fp);
                cv.drawCircle(u(s, 4.5f), u(s, 12), u(s, 1.4f), fp);
                cv.drawCircle(u(s, 4.5f), u(s, 18), u(s, 1.4f), fp);
                break;
            }
            case "stats": {
                cv.drawLine(u(s, 3.5f), u(s, 20), u(s, 20.5f), u(s, 20), p);
                cv.drawLine(u(s, 6), u(s, 20), u(s, 6), u(s, 13), p);
                cv.drawLine(u(s, 12), u(s, 20), u(s, 12), u(s, 7), p);
                cv.drawLine(u(s, 18), u(s, 20), u(s, 18), u(s, 15), p);
                break;
            }
            case "tools": {
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 8.5f), p);
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 3.5f), p);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    cv.drawLine(u(s, 12) + (float) Math.cos(a) * u(s, 8.5f),
                            u(s, 12) + (float) Math.sin(a) * u(s, 8.5f),
                            u(s, 12) + (float) Math.cos(a) * u(s, 11.2f),
                            u(s, 12) + (float) Math.sin(a) * u(s, 11.2f), p);
                }
                break;
            }
            case "print": {
                cv.drawRoundRect(new RectF(u(s, 4), u(s, 8.5f), u(s, 20), u(s, 17)), u(s, 2), u(s, 2), p);
                cv.drawRect(new RectF(u(s, 7), u(s, 4), u(s, 17), u(s, 9)), p);
                cv.drawRect(new RectF(u(s, 7.5f), u(s, 13), u(s, 16.5f), u(s, 19.5f)), p);
                cv.drawCircle(u(s, 17), u(s, 11), u(s, 1), fp);
                break;
            }
            case "export": {
                cv.drawRoundRect(new RectF(u(s, 4), u(s, 11), u(s, 20), u(s, 20)), u(s, 2), u(s, 2), p);
                cv.drawLine(u(s, 12), u(s, 3.5f), u(s, 12), u(s, 13.5f), p);
                Path h = new Path();
                h.moveTo(u(s, 8.5f), u(s, 7));
                h.lineTo(u(s, 12), u(s, 3.5f));
                h.lineTo(u(s, 15.5f), u(s, 7));
                cv.drawPath(h, p);
                break;
            }
            case "import": {
                cv.drawRoundRect(new RectF(u(s, 4), u(s, 11), u(s, 20), u(s, 20)), u(s, 2), u(s, 2), p);
                cv.drawLine(u(s, 12), u(s, 3.5f), u(s, 12), u(s, 13.5f), p);
                Path h = new Path();
                h.moveTo(u(s, 8.5f), u(s, 10));
                h.lineTo(u(s, 12), u(s, 13.5f));
                h.lineTo(u(s, 15.5f), u(s, 10));
                cv.drawPath(h, p);
                break;
            }
            case "backup": {
                cv.drawOval(new RectF(u(s, 5), u(s, 4), u(s, 19), u(s, 9.5f)), p);
                cv.drawLine(u(s, 5), u(s, 6.75f), u(s, 5), u(s, 16.5f), p);
                cv.drawLine(u(s, 19), u(s, 6.75f), u(s, 19), u(s, 16.5f), p);
                cv.drawArc(new RectF(u(s, 5), u(s, 14), u(s, 19), u(s, 19.5)), 0, 180, false, p);
                cv.drawLine(u(s, 12), u(s, 9.5f), u(s, 12), u(s, 13.5f), p);
                break;
            }
            case "restore": {
                cv.drawOval(new RectF(u(s, 5), u(s, 12.5f), u(s, 19), u(s, 18)), p);
                cv.drawLine(u(s, 5), u(s, 7.5f), u(s, 5), u(s, 15.25f), p);
                cv.drawLine(u(s, 19), u(s, 7.5f), u(s, 19), u(s, 15.25f), p);
                cv.drawArc(new RectF(u(s, 5), u(s, 4.5f), u(s, 19), u(s, 10)), 0, 180, false, p);
                cv.drawLine(u(s, 12), u(s, 4.5f), u(s, 12), u(s, 9), p);
                Path h = new Path();
                h.moveTo(u(s, 9), u(s, 7));
                h.lineTo(u(s, 12), u(s, 10));
                h.lineTo(u(s, 15), u(s, 7));
                cv.drawPath(h, p);
                break;
            }
            case "info": {
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 9), p);
                cv.drawCircle(u(s, 12), u(s, 7.8f), u(s, 1.4f), fp);
                cv.drawLine(u(s, 12), u(s, 11), u(s, 12), u(s, 17), p);
                break;
            }
            case "shield": {
                Path sh = new Path();
                sh.moveTo(u(s, 12), u(s, 3.5f));
                sh.lineTo(u(s, 20), u(s, 6.5f));
                sh.lineTo(u(s, 19), u(s, 14));
                sh.quadTo(u(s, 17), u(s, 19), u(s, 12), u(s, 21));
                sh.quadTo(u(s, 7), u(s, 19), u(s, 5), u(s, 14));
                sh.lineTo(u(s, 4), u(s, 6.5f));
                sh.close();
                cv.drawPath(sh, p);
                Path ch = new Path();
                ch.moveTo(u(s, 8.5f), u(s, 12));
                ch.lineTo(u(s, 11), u(s, 14.8f));
                ch.lineTo(u(s, 15.5f), u(s, 9.5f));
                cv.drawPath(ch, p);
                break;
            }
            case "update": {
                cv.drawArc(new RectF(u(s, 4.5f), u(s, 4.5f), u(s, 19.5f), u(s, 19.5f)), 30, 285, false, p);
                Path h = new Path();
                h.moveTo(u(s, 17.5f), u(s, 3.5f));
                h.lineTo(u(s, 18.5f), u(s, 8.5f));
                h.lineTo(u(s, 13.5f), u(s, 9.5f));
                cv.drawPath(h, p);
                break;
            }
            case "license": {
                Path pg = new Path();
                pg.moveTo(u(s, 5), u(s, 3.5f));
                pg.lineTo(u(s, 19), u(s, 3.5f));
                pg.lineTo(u(s, 19), u(s, 16.5f));
                pg.lineTo(u(s, 15), u(s, 16.5f));
                pg.lineTo(u(s, 15), u(s, 21.5f));
                pg.lineTo(u(s, 11.5f), u(s, 18.5f));
                pg.lineTo(u(s, 8), u(s, 21.5f));
                pg.lineTo(u(s, 8), u(s, 16.5f));
                pg.lineTo(u(s, 5), u(s, 16.5f));
                pg.close();
                cv.drawPath(pg, p);
                star(cv, p, u(s, 12), u(s, 9), u(s, 3), u(s, 1.3f), 5);
                break;
            }
            case "warning": {
                Path tr = new Path();
                tr.moveTo(u(s, 12), u(s, 3.5f));
                tr.lineTo(u(s, 21.5f), u(s, 20));
                tr.lineTo(u(s, 2.5f), u(s, 20));
                tr.close();
                cv.drawPath(tr, p);
                cv.drawLine(u(s, 12), u(s, 9.5f), u(s, 12), u(s, 14.5f), p);
                cv.drawCircle(u(s, 12), u(s, 17.2f), u(s, 1.3f), fp);
                break;
            }
            case "plus": {
                cv.drawLine(u(s, 12), u(s, 4), u(s, 12), u(s, 20), p);
                cv.drawLine(u(s, 4), u(s, 12), u(s, 20), u(s, 12), p);
                break;
            }
            case "edit": {
                Path pn = new Path();
                pn.moveTo(u(s, 4.5f), u(s, 19.5f));
                pn.lineTo(u(s, 5.5f), u(s, 15.5f));
                pn.lineTo(u(s, 16), u(s, 5));
                pn.quadTo(u(s, 17.5f), u(s, 3.5f), u(s, 19), u(s, 5));
                pn.quadTo(u(s, 20.5f), u(s, 6.5f), u(s, 19), u(s, 8));
                pn.lineTo(u(s, 8.5f), u(s, 18.5f));
                pn.close();
                cv.drawPath(pn, p);
                cv.drawLine(u(s, 14.6f), u(s, 6.4f), u(s, 17.6f), u(s, 9.4f), p);
                break;
            }
            case "trash": {
                cv.drawRoundRect(new RectF(u(s, 5), u(s, 7), u(s, 19), u(s, 20.5f)), u(s, 2), u(s, 2), p);
                cv.drawLine(u(s, 3), u(s, 7), u(s, 21), u(s, 7), p);
                cv.drawLine(u(s, 9), u(s, 4), u(s, 15), u(s, 4), p);
                cv.drawLine(u(s, 10), u(s, 11), u(s, 10), u(s, 16.5f), p);
                cv.drawLine(u(s, 14), u(s, 11), u(s, 14), u(s, 16.5f), p);
                break;
            }
            case "check": {
                Path ch = new Path();
                ch.moveTo(u(s, 4.5f), u(s, 12.5f));
                ch.lineTo(u(s, 10), u(s, 18));
                ch.lineTo(u(s, 19.5f), u(s, 6));
                cv.drawPath(ch, p);
                break;
            }
            case "lock": {
                cv.drawRoundRect(new RectF(u(s, 5.5f), u(s, 11), u(s, 18.5f), u(s, 20.5f)), u(s, 2.5f), u(s, 2.5f), p);
                cv.drawArc(new RectF(u(s, 8), u(s, 3.5f), u(s, 16), u(s, 13)), 180, 180, false, p);
                cv.drawCircle(u(s, 12), u(s, 15.5f), u(s, 1.5f), fp);
                break;
            }
            case "key": {
                cv.drawCircle(u(s, 8), u(s, 9.5f), u(s, 4), p);
                cv.drawLine(u(s, 11), u(s, 12.5f), u(s, 19.5f), u(s, 21), p);
                cv.drawLine(u(s, 16.5f), u(s, 18), u(s, 18.5f), u(s, 16), p);
                cv.drawLine(u(s, 18.5f), u(s, 20), u(s, 20.5f), u(s, 18), p);
                break;
            }
            case "building": {
                cv.drawRoundRect(new RectF(u(s, 5), u(s, 3.5f), u(s, 19), u(s, 20.5f)), u(s, 1.5f), u(s, 1.5f), p);
                cv.drawCircle(u(s, 9), u(s, 8), u(s, 1.1f), fp);
                cv.drawCircle(u(s, 15), u(s, 8), u(s, 1.1f), fp);
                cv.drawCircle(u(s, 9), u(s, 13), u(s, 1.1f), fp);
                cv.drawCircle(u(s, 15), u(s, 13), u(s, 1.1f), fp);
                cv.drawLine(u(s, 2.5f), u(s, 20.5f), u(s, 21.5f), u(s, 20.5f), p);
                break;
            }
            case "globe": {
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 8.5f), p);
                cv.drawOval(new RectF(u(s, 8), u(s, 3.5f), u(s, 16), u(s, 20.5f)), p);
                cv.drawLine(u(s, 3.5f), u(s, 12), u(s, 20.5f), u(s, 12), p);
                break;
            }
            case "phone": {
                cv.drawRoundRect(new RectF(u(s, 7), u(s, 3), u(s, 17), u(s, 21)), u(s, 2.5f), u(s, 2.5f), p);
                cv.drawLine(u(s, 10), u(s, 18), u(s, 14), u(s, 18), p);
                break;
            }
            case "folder": {
                Path fo = new Path();
                fo.moveTo(u(s, 3), u(s, 19.5f));
                fo.lineTo(u(s, 3), u(s, 6));
                fo.lineTo(u(s, 9.5f), u(s, 6));
                fo.lineTo(u(s, 11.5f), u(s, 8.5f));
                fo.lineTo(u(s, 21), u(s, 8.5f));
                fo.lineTo(u(s, 21), u(s, 19.5f));
                fo.close();
                cv.drawPath(fo, p);
                break;
            }
            case "history": {
                cv.drawArc(new RectF(u(s, 4), u(s, 4), u(s, 20), u(s, 20)), 90, 290, false, p);
                Path h = new Path();
                h.moveTo(u(s, 4.5f), u(s, 5.5f));
                h.lineTo(u(s, 4.5f), u(s, 10.5f));
                h.lineTo(u(s, 9.5f), u(s, 10.5f));
                cv.drawPath(h, p);
                cv.drawLine(u(s, 12), u(s, 8), u(s, 12), u(s, 12.5f), p);
                cv.drawLine(u(s, 12), u(s, 12.5f), u(s, 15.5f), u(s, 14.5f), p);
                break;
            }
            case "palette": {
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 9), p);
                cv.drawCircle(u(s, 9), u(s, 9), u(s, 1.3f), fp);
                cv.drawCircle(u(s, 15), u(s, 9), u(s, 1.3f), fp);
                cv.drawCircle(u(s, 9), u(s, 15), u(s, 1.3f), fp);
                cv.drawCircle(u(s, 15), u(s, 15), u(s, 1.3f), fp);
                break;
            }
            default: { // نقطة افتراضية
                cv.drawCircle(u(s, 12), u(s, 12), u(s, 5), fp);
            }
        }
    }

    private void star(Canvas cv, Paint p, float cx, float cy, float rOut, float rIn, int points) {
        Path st = new Path();
        for (int i = 0; i < points * 2; i++) {
            double a = Math.PI * i / points - Math.PI / 2;
            float r = (i % 2 == 0) ? rOut : rIn;
            float x = cx + (float) Math.cos(a) * r;
            float y = cy + (float) Math.sin(a) * r;
            if (i == 0) st.moveTo(x, y); else st.lineTo(x, y);
        }
        st.close();
        cv.drawPath(st, p);
    }

    // ---------- أزرار ----------

    public Button button(Context c, String s, boolean filled) {
        Button b = new Button(c);
        b.setText(s);
        b.setTextSize(15);
        b.setAllCaps(false);
        initFonts(c);
        b.setTypeface(FONT_M);
        b.setTextColor(filled ? onPrimary : primary);
        b.setBackground(box(c, 16, filled ? primary : card, filled ? 0 : primary, 1.4f));
        b.setPadding(dp(c, 10), dp(c, 13), dp(c, 10), dp(c, 13));
        b.setElevation(dp(c, filled ? 3 : 1));
        return b;
    }

    /** زر رئيسي متدرج بأيقونة */
    public Button bigButton(Context c, String s, String icon, boolean filled) {
        Button b = button(c, s, filled);
        b.setTextSize(15.5f);
        if (filled) b.setBackground(gradient(c, 18, primary, primaryDark));
        b.setCompoundDrawablePadding(dp(c, 10));
        b.setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon(c, icon, filled ? onPrimary : primary, 18), null, null, null);
        return b;
    }

    /** زر شبحي بأيقونة (مربع شبكي) */
    public LinearLayout tileButton(Context c, String s, String icon, Runnable onClick) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setBackground(box(c, 16, fieldBg, line, 1f));
        l.setPadding(dp(c, 6), dp(c, 14), dp(c, 6), dp(c, 14));
        ImageView iv = new ImageView(c);
        iv.setImageDrawable(icon(c, icon, primary, 22));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        l.addView(iv, new LinearLayout.LayoutParams(dp(c, 26), dp(c, 26)));
        TextView t = text(c, s, 12.5f, text, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(c, 7), 0, 0);
        l.addView(t, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        l.setOnClickListener(v -> { if (onClick != null) onClick.run(); });
        l.setClickable(true);
        return l;
    }

    // ---------- حقول الإدخال ----------

    /** حقل إدخال مقيّد حسب النوع مع أيقونة وحدود تركيز */
    public EditText field(Context c, int kind, String hint) {
        return field(c, kind, hint, null);
    }

    public EditText field(Context c, int kind, String hint, String iconKind) {
        final EditText e = new EditText(c);
        e.setHint(hint);
        e.setHintTextColor(sub);
        e.setTextColor(text);
        e.setTextSize(15);
        initFonts(c);
        e.setTypeface(FONT_R);
        e.setBackground(box(c, 14, fieldBg, line, 1.2f));
        int p = dp(c, 14);
        e.setPadding(p, p, p, p);
        if (iconKind != null) {
            e.setCompoundDrawablePadding(dp(c, 10));
            e.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    icon(c, iconKind, sub, 19), null, null, null);
        }
        e.setOnFocusChangeListener((v, has) -> e.setBackground(
                box(c, 14, fieldBg, has ? primary : line, has ? 1.8f : 1.2f)));
        if (kind == Employee.K_NUM) {
            e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            e.addTextChangedListener(new FilterWatcher(e, true));
        } else if (kind == Employee.K_TEXT) {
            e.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            e.addTextChangedListener(new FilterWatcher(e, false));
        } else { // حر أو تاريخ
            e.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        if (kind == Employee.K_DATE) attachDatePicker(c, e);
        return e;
    }

    /** محوّل قوائم منسدلة بخط التطبيق وألوان الثيم — والقائمة المنبثقة تتبع الثيم ليلي/نهاري */
    public ArrayAdapter<String> spinnerAdapter(Context c, String[] items) {
        final Theme T = this;
        ArrayAdapter<String> ad = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTextColor(text);
                v.setTextSize(15);
                v.setTypeface(fontRegular(c));
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getDropDownView(position, convertView, parent);
                // صفوف القائمة المنسدلة بلون الثيم — لا أبيض في الوضع الليلي
                v.setTextColor(text);
                v.setTextSize(15);
                v.setTypeface(fontRegular(c));
                v.setBackground(T.box(c, 8, fieldBg, 0, 0));
                int pv = T.dp(c, 12);
                v.setPadding(pv, pv, pv, pv);
                return v;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return ad;
    }

    /** قائمة منسدلة من قائمة جاهزة مع الحفاظ على قيمة مخزنة إن لم تكن ضمن القائمة */
    public Spinner spinnerWith(Context c, String[] items, String current) {
        String[] list = items;
        if (current != null && !current.trim().isEmpty()) {
            boolean found = false;
            for (String it : items) if (it.equals(current)) { found = true; break; }
            if (!found) {
                list = new String[items.length + 1];
                System.arraycopy(items, 0, list, 0, items.length);
                list[items.length] = current; // القيمة القديمة تبقى للاختيار
            }
        }
        Spinner s = spinner(c, list);
        if (current != null && !current.trim().isEmpty()) {
            for (int i = 0; i < list.length; i++) {
                if (list[i].equals(current)) {
                    s.setSelection(i);
                    break;
                }
            }
        }
        return s;
    }

    /** قائمة منسدلة مع أيقونة على يمينها */
    public LinearLayout spinnerIconed(Context c, Spinner sp, String iconKind) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        ImageView iv = new ImageView(c);
        iv.setImageDrawable(icon(c, iconKind, sub, 19));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(c, 22), dp(c, 22));
        ip.setMarginEnd(dp(c, 10));
        l.addView(iv, ip);
        l.addView(sp, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return l;
    }

    /** حقل تاريخ: منتقي تقويم فقط (تاريخ فقط) — الضغط المطوّل يمسح */
    private static void attachDatePicker(final Context c, final EditText e) {
        e.setFocusable(false);
        e.setClickable(true);
        e.setCursorVisible(false);
        e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                String iso = (String) e.getTag();
                if (iso != null) {
                    try {
                        cal.setTime(Employee.parseIso(iso));
                    } catch (Exception ignored) {}
                }
                DatePickerDialog dlg = new DatePickerDialog(dialogCtx(c),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(android.widget.DatePicker dp, int y, int m, int day) {
                                Calendar cc = Calendar.getInstance();
                                cc.set(y, m, day);
                                SimpleDateFormat isoF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                SimpleDateFormat dispF = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                e.setTag(isoF.format(cc.getTime()));
                                e.setText(dispF.format(cc.getTime()));
                            }
                        },
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dlg.show();
            }
        });
        e.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                e.setTag(null);
                e.setText("");
                return true;
            }
        });
    }

    /** مرشّح الإدخال الحي: أرقام فقط (مع تحويل الأرقام العربية) أو نص فقط */
    public static class FilterWatcher implements TextWatcher {
        private final EditText box;
        private final boolean digits;
        private boolean self = false;

        public FilterWatcher(EditText box, boolean digits) {
            this.box = box;
            this.digits = digits;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

        @Override
        public void onTextChanged(CharSequence s, int a, int b, int c) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (self) return;
            String clean = digits ? Employee.digitsOnly(s.toString()) : Employee.textOnly(s.toString());
            if (!clean.equals(s.toString())) {
                self = true;
                s.replace(0, s.length(), clean);
                self = false;
            }
            try {
                box.setSelection(box.length());
            } catch (Exception ignored) {}
        }
    }

    public Spinner spinner(Context c, String[] items) {
        Spinner s = new Spinner(c);
        // خلفية نافذة القائمة المنسدلة بلون الثيم — داكنة في وضع الليل وواضحة دائماً
        try {
            GradientDrawable pb = new GradientDrawable();
            pb.setColor(card);
            pb.setCornerRadius(dp(c, 12));
            s.setPopupBackgroundDrawable(pb);
        } catch (Exception ignored) {}
        s.setAdapter(spinnerAdapter(c, items));
        s.setBackground(box(c, 14, fieldBg, line, 1.2f));
        int p = dp(c, 12);
        s.setPadding(p, p, p, p);
        return s;
    }

    /** شعار الإدارة: ملف مستورد من المستخدم إن وجد، وإلا الشعار المدمج */
    public static Bitmap loadLogo(Context c) {
        File f = new File(c.getFilesDir(), "logo.png");
        Bitmap b = null;
        if (f.exists()) b = BitmapFactory.decodeFile(f.getAbsolutePath());
        if (b == null) b = BitmapFactory.decodeResource(c.getResources(), R.drawable.logo);
        return b;
    }

    public static boolean customLogoExists(Context c) {
        return new File(c.getFilesDir(), "logo.png").exists();
    }
}
