package com.mmsa.records;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** أساس كل الشاشات: لغة الواجهة + اتجاه RTL/LTR + الألوان + هيكل الصفحة مع زر الرجوع أعلى اليمين */
public abstract class BaseActivity extends Activity {

    protected Theme th;
    private long uiEpochAtCreate = -1;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Lang.wrap(base, Lang.index(base)));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Lang.idx = Lang.index(this);
        th = Theme.of(this);
        uiEpochAtCreate = Theme.uiEpoch(this);
        getWindow().getDecorView().setLayoutDirection(
                Lang.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        getWindow().setBackgroundDrawable(new GradientDrawable() {{
            setColor(th.bg);
        }});
        applyBars();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // تغيّر الثيم أو الوضع أو اللغة أثناء غياب هذه الشاشة؟ أعد بناءها بالكامل
        if (uiEpochAtCreate != Theme.uiEpoch(this)) {
            uiEpochAtCreate = Theme.uiEpoch(this);
            recreate();
            return;
        }
        th = Theme.of(this); // تحديث الألوان بعد تبديل الثيم أو الوضع
        applyBars();
    }

    private void applyBars() {
        getWindow().setStatusBarColor(th.primaryDark);
        getWindow().setNavigationBarColor(th.bg);
    }

    protected int dp(float v) {
        return Theme.dp(this, v);
    }

    /** باني نوافذ منبثقة بنفس ثيم التطبيق (فاتح/داكن تلقائياً) وبخط التطبيق */
    protected AlertDialog.Builder dlg() {
        return Theme.dialog(this);
    }

    protected LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    protected LinearLayout.LayoutParams matchM(int bottomDp) {
        LinearLayout.LayoutParams l = match();
        l.bottomMargin = dp(bottomDp);
        return l;
    }

    protected LinearLayout.LayoutParams half() {
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        l.setMargins(dp(4), 0, dp(4), 0);
        return l;
    }

    protected void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    protected void confirm(String title, String msg, final Runnable onYes) {
        dlg()
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(Lang.t("تأكيد", "Confirm", "Confirmer", "Подтвердить"),
                        (d, w) -> onYes.run())
                .setNegativeButton(Lang.t("إلغاء", "Cancel", "Annuler", "Отмена"), null)
                .show();
    }

    /** هيكل الصفحة: شريط علوي (زر رجوع أعلى اليمين + شعار + عنوان) + جسم + شريط سفلي اختياري */
    public LinearLayout page(String title, View body, View bottomBar) {
        return page(title, body, bottomBar, true);
    }

    /** نسخة مع تحكم بإظهار زر الرجوع (الواجهة الرئيسية بدون رجوع) */
    public LinearLayout page(String title, View body, View bottomBar, boolean withBack) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(th.bg);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(th.primary);
        header.setPadding(dp(16), dp(10), dp(16), dp(10));
        header.setElevation(dp(4));

        // زر الرجوع أعلى الصفحة على اليمين (بداية الاتجاه RTL)
        if (withBack) {
            ImageView back = new ImageView(this);
            back.setImageDrawable(th.icon(this, "back", th.onPrimary, 22));
            back.setScaleType(ImageView.ScaleType.FIT_CENTER);
            back.setPadding(dp(6), dp(6), dp(6), dp(6));
            back.setClickable(true);
            back.setContentDescription(Lang.t("رجوع", "Back", "Retour", "Назад"));
            back.setOnClickListener(v -> finish());
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(36), dp(36));
            bp.setMarginEnd(dp(6));
            header.addView(back, bp);
        }

        ImageView iv = new ImageView(this);
        Bitmap lg = Theme.loadLogo(this);
        iv.setImageBitmap(lg);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(34), dp(34));
        ip.setMarginEnd(dp(12));
        header.addView(iv, ip);

        TextView t = th.text(this, title, 18, th.onPrimary, true);
        header.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        root.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));
        root.addView(body, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        if (bottomBar != null) {
            root.addView(bottomBar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        root.setFitsSystemWindows(true); // الشريط العلوي/السفلي لا يغطي المحتوى (targetSdk 35)
        setContentView(root);
        return root;
    }
}
