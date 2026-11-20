# سياسة الخصوصية — منظومة البيانات (MMSA)

**آخر تحديث: نوفمبر 2026 — الإصدار 1.4.0**

هذه السياسة توضح كيفية تعامل تطبيق «منظومة البيانات» مع البيانات، وهي مطابقة لمتطلبات سياسات نشر Google Play وتوافق مع فحص Google Play Protect.

## 1. تخزين البيانات — محلي بالكامل

تعمل المنظومة بالكامل داخل هاتفك. تُحفظ بيانات الموظفين (الاسم، الأرقام، الرتب، الجهة، البيانات المالية والمصرفية، المؤهلات) في قاعدة بيانات محلية **SQLite** مشفرة **AES-256** داخل ذاكرة الهاتف نفسه. **لا يوجد أي خادم، ولا تُرسل بياناتك إلى أي جهة خارجية مهما كان نوعها.**

## 2. لا جمع ولا مشاركة ولا تتبع

- التطبيق **لا يجمع** أي بيانات شخصية ولا تحليلات استخدام.
- لا يحتوي على إعلانات، ولا أدوات تتبع، ولا SDK خارجية.
- **لا يشارك** أي بيانات مع أي طرف ثالث.

## 3. الأذونات المستخدمة (إذنان فقط)

| الإذن | الغرض |
| --- | --- |
| `INTERNET` | فحص وجود إصدار جديد من التطبيق عبر ملف تحديثات على GitHub — فقط |
| `ACCESS_NETWORK_STATE` | التحقق من وجود اتصال قبل محاولة الفحص |

لا يطلب التطبيق أي إذن آخر (لا جهات اتصال، لا موقع، لا كاميرا، لا تخزين)، ولا يعمل في الخلفية.

## 4. النسخ الاحتياطي والاستيراد

تصدير البيانات (Excel / SQL / CSV / نسخة احتياطية) واستيرادها يحدثان **بأمرك المباشر** وعبر ملفاتك أنت فقط، ولا يمر أي ملف عبر أي خادم.

## 5. حذف البيانات

إزالة التطبيق من الهاتف تحذف كل البيانات نهائياً وبشكل كامل. يمكنك أيضاً تصدير نسخة احتياطية والاحتفاظ بها بنفسك.

## 6. الأمان

- التطبيق موقّع رقمياً بمفتاح مطوّر خاص، ومتوافق مع فحص **Google Play Protect**.
- لا يطلب أي إذن «خطير» حسب تصنيف أندرويد، ولا يستخدم اتصالاً غير مشفر (cleartext ممنوع في البيان).

## 7. الأطفال

التطبيق ليس موجهاً للأطفال دون 13 عاماً (أداة إدارية لموظفي القطاع)، ومع ذلك لا يجمع أي بيانات من أي مستخدم.

## 8. التواصل

لأي استفسار حول الخصوصية تواصل مع مطوّر المنظومة عبر بيانات الترخيص الموجودة داخل التطبيق (شاشة «حول التطبيق»).

---

# Privacy Policy — MMSA Data System (English summary)

**Last updated: November 2026 — version 1.4.0**

- **Fully local:** all employee data is stored in an AES-256 encrypted local SQLite database on the device. There is no server; data is never sent anywhere.
- **No collection, no sharing, no tracking:** no ads, no analytics, no third-party SDKs, no personal data collection.
- **Two permissions only:** `INTERNET` and `ACCESS_NETWORK_STATE`, used solely to check for new app versions on GitHub. Never used to send data.
- **Backup/import** runs only on your explicit action with your own files.
- **Deletion:** uninstalling removes all data permanently.
- **Security:** digitally signed, compliant with Google Play Protect, no dangerous permissions, no background work, no cleartext traffic.
- **Not directed at children under 13** (an internal administrative tool), and collects no data from any user anyway.
- **Contact:** the developer via the license details inside the app (About screen).
