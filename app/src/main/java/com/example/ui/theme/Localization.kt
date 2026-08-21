package com.example.ui.theme

enum class AppLanguage(val code: String, val labelAr: String, val labelEn: String) {
    ARABIC("ar", "العربية (Arabic)", "Arabic (العربية)"),
    ENGLISH("en", "الإنجليزية (English)", "English (الإنجليزية)");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ARABIC
        }
    }
}

object AppStrings {

    fun appTitle(lang: AppLanguage): String = "DEUTSCH AR"

    // --- Navigation Tabs ---
    fun tabDictionary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "القاموس"
        AppLanguage.ENGLISH -> "Dictionary"
    }

    fun tabHistory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "السجل"
        AppLanguage.ENGLISH -> "History"
    }

    fun tabCheatSheet(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الملخص"
        AppLanguage.ENGLISH -> "Cheat Sheet"
    }

    fun tabSettings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإعدادات"
        AppLanguage.ENGLISH -> "Settings"
    }

    // --- Search Screen ---
    fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ابحث عن كلمة، فعل، أو جملة..."
        AppLanguage.ENGLISH -> "Search for a word, verb, or sentence..."
    }

    fun searchButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "بحث"
        AppLanguage.ENGLISH -> "Search"
    }

    fun clearQuery(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مسح"
        AppLanguage.ENGLISH -> "Clear"
    }

    fun langAuto(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تلقائي"
        AppLanguage.ENGLISH -> "Auto"
    }

    fun langGerman(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ألماني"
        AppLanguage.ENGLISH -> "German"
    }

    fun langArabic(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "عربي"
        AppLanguage.ENGLISH -> "Arabic"
    }

    fun langEnglish(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إنجليزي"
        AppLanguage.ENGLISH -> "English"
    }

    fun searchPlaceholderForLang(sourceLang: String, appLang: AppLanguage): String = when (sourceLang) {
        "de" -> when (appLang) {
            AppLanguage.ARABIC -> "اكتب كلمة أو جملة ألمانية (Deutsch)..."
            AppLanguage.ENGLISH -> "Type a German word or sentence..."
        }
        "en" -> when (appLang) {
            AppLanguage.ARABIC -> "اكتب كلمة أو جملة إنجليزية (English)..."
            AppLanguage.ENGLISH -> "Type an English word or sentence..."
        }
        "ar" -> when (appLang) {
            AppLanguage.ARABIC -> "اكتب كلمة أو جملة عربية..."
            AppLanguage.ENGLISH -> "Type an Arabic word or sentence..."
        }
        else -> searchPlaceholder(appLang)
    }

    fun translationTargetHint(sourceLang: String, appLang: AppLanguage): String = when (sourceLang) {
        "de" -> when (appLang) {
            AppLanguage.ARABIC -> "يترجم إلى: 🇪🇬 عربي + 🇬🇧 إنجليزي"
            AppLanguage.ENGLISH -> "Translates to: 🇪🇬 Arabic + 🇬🇧 English"
        }
        "en" -> when (appLang) {
            AppLanguage.ARABIC -> "يترجم إلى: 🇩🇪 ألماني + 🇪🇬 عربي"
            AppLanguage.ENGLISH -> "Translates to: 🇩🇪 German + 🇪🇬 Arabic"
        }
        "ar" -> when (appLang) {
            AppLanguage.ARABIC -> "يترجم إلى: 🇩🇪 ألماني + 🇬🇧 إنجليزي"
            AppLanguage.ENGLISH -> "Translates to: 🇩🇪 German + 🇬🇧 English"
        }
        else -> ""
    }

    fun emptyStateTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "القاموس والترجمة الفورية"
        AppLanguage.ENGLISH -> "Dictionary & Instant Translation"
    }

    fun emptyStateDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اكتب أي كلمة، فعل، أو جملة بالألمانية أو العربية أو الإنجليزية في شريط البحث أعلاه واضغط على زر البحث للترجمة والتحليل الشامل."
        AppLanguage.ENGLISH -> "Type any German, Arabic, or English word, verb, or sentence above and tap search for instant translation and grammar analysis."
    }

    fun noResultsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "لم يتم العثور على نتائج"
        AppLanguage.ENGLISH -> "No results found"
    }

    fun noResultsDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تأكد من كتابة الكلمة بشكل صحيح، أو جرّب البحث بصيغة أخرى."
        AppLanguage.ENGLISH -> "Check your spelling or try searching with another form or word."
    }

    fun searchingTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جاري البحث والترجمة..."
        AppLanguage.ENGLISH -> "Searching and translating..."
    }

    fun suggestionsHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اقتراحات مشابهة"
        AppLanguage.ENGLISH -> "Similar Suggestions"
    }

    fun instantTranslationHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الترجمة الفورية"
        AppLanguage.ENGLISH -> "Instant Translation"
    }

    fun originalTextLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "النص الأصلي"
        AppLanguage.ENGLISH -> "Original Text"
    }

    fun translationLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الترجمة"
        AppLanguage.ENGLISH -> "Translation"
    }

    fun sectionMeaning(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المعنى والترجمة"
        AppLanguage.ENGLISH -> "Meaning & Translation"
    }

    fun sectionDeclension(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جدول الإعراب والحالات"
        AppLanguage.ENGLISH -> "Declension & Cases"
    }

    fun sectionConjugation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تصريف الأفعال والأزمنة"
        AppLanguage.ENGLISH -> "Verb Conjugations & Tenses"
    }

    fun sectionExamples(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أمثلة وسياق الاستخدام"
        AppLanguage.ENGLISH -> "Examples & Context"
    }

    fun pluralLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الجمع"
        AppLanguage.ENGLISH -> "Plural"
    }

    fun singularLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المفرد"
        AppLanguage.ENGLISH -> "Singular"
    }

    fun levelLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المستوى"
        AppLanguage.ENGLISH -> "Level"
    }

    fun listenAudio(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "استماع للنطق"
        AppLanguage.ENGLISH -> "Listen Pronunciation"
    }

    fun copyText(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "نسخ"
        AppLanguage.ENGLISH -> "Copy"
    }

    fun copiedToClipboard(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم النسخ إلى الحافظة"
        AppLanguage.ENGLISH -> "Copied to clipboard"
    }

    fun saveWord(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حفظ"
        AppLanguage.ENGLISH -> "Save"
    }

    fun savedWord(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم الحفظ"
        AppLanguage.ENGLISH -> "Saved"
    }

    // Declension cases
    fun caseNominativ(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الرفع (Nominativ)"
        AppLanguage.ENGLISH -> "Nominative (Nominativ)"
    }

    fun caseAkkusativ(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "النصب (Akkusativ)"
        AppLanguage.ENGLISH -> "Accusative (Akkusativ)"
    }

    fun caseDativ(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الجر (Dativ)"
        AppLanguage.ENGLISH -> "Dative (Dativ)"
    }

    fun caseGenitiv(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإضافة (Genitiv)"
        AppLanguage.ENGLISH -> "Genitive (Genitiv)"
    }

    // Tenses
    fun tensePresent(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المضارع (Präsens)"
        AppLanguage.ENGLISH -> "Present (Präsens)"
    }

    fun tenseSimplePast(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الماضي البسيط (Präteritum)"
        AppLanguage.ENGLISH -> "Simple Past (Präteritum)"
    }

    fun tensePerfect(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الماضي التام (Perfekt)"
        AppLanguage.ENGLISH -> "Perfect (Perfekt)"
    }

    fun auxiliaryVerbLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الفعل المساعد"
        AppLanguage.ENGLISH -> "Auxiliary Verb"
    }

    fun imperativeLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "صيغة الأمر (Imperativ)"
        AppLanguage.ENGLISH -> "Imperative (Imperativ)"
    }

    // Missing Model Download Alert
    fun missingModelAlertTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "⚠️ تنبيه: حزم الترجمة غير متنزلة!"
        AppLanguage.ENGLISH -> "⚠️ Alert: Translation packages not downloaded!"
    }

    fun missingModelAlertSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أنت تبحث الآن، ولكن حزم الترجمة غير متنزلة! يُرجى تنزيل الحزم حتى تترجم جميع الكلمات والجمل بدقة كاملة وبدون اتصال بالإنترنت."
        AppLanguage.ENGLISH -> "You are searching, but translation packages are missing! Please download packages to translate all words and sentences accurately offline."
    }

    fun downloadModelsNowBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تنزيل الحزم الآن"
        AppLanguage.ENGLISH -> "Download Packages Now"
    }

    fun requiredDownloadBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مطلوب تحميل"
        AppLanguage.ENGLISH -> "Download Required"
    }

    // --- History Screen ---
    fun historyTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "سجل البحث والكلمات المحفوظة"
        AppLanguage.ENGLISH -> "Search History & Saved"
    }

    fun historySearchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "بحث في السجل..."
        AppLanguage.ENGLISH -> "Search in history..."
    }

    fun historyFilterAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الكل"
        AppLanguage.ENGLISH -> "All"
    }

    fun historyFilterSaved(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المحفوظات ⭐"
        AppLanguage.ENGLISH -> "Saved ⭐"
    }

    fun historyFilterDictionary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مفردات القاموس"
        AppLanguage.ENGLISH -> "Dictionary Words"
    }

    fun historyFilterTranslation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ترجمة فورية"
        AppLanguage.ENGLISH -> "Instant Translations"
    }

    fun clearHistoryBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مسح السجل"
        AppLanguage.ENGLISH -> "Clear History"
    }

    fun clearHistoryDialogTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تأكيد مسح السجل"
        AppLanguage.ENGLISH -> "Confirm Clear History"
    }

    fun clearHistoryDialogMsg(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "هل أنت متأكد من مسح جميع عناصر سجل البحث؟"
        AppLanguage.ENGLISH -> "Are you sure you want to delete all search history items?"
    }

    fun confirmDelete(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مسح"
        AppLanguage.ENGLISH -> "Clear"
    }

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إلغاء"
        AppLanguage.ENGLISH -> "Cancel"
    }

    fun historyEmptyTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "سجل البحث فارغ"
        AppLanguage.ENGLISH -> "Search history is empty"
    }

    fun historyEmptyDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الكلمات والجمل التي تبحث عنها ستظهر هنا للرجوع إليها لاحقاً."
        AppLanguage.ENGLISH -> "Words and sentences you search for will appear here for later reference."
    }

    // --- Settings Screen ---
    fun settingsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإعدادات"
        AppLanguage.ENGLISH -> "Settings"
    }

    fun settingsSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إدارة لغة التطبيق وموديلات الترجمة"
        AppLanguage.ENGLISH -> "Manage app language and translation models"
    }

    fun appLanguageSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "لغة واجهة التطبيق"
        AppLanguage.ENGLISH -> "App Interface Language"
    }

    fun appLanguageSectionDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اختر اللغة المفضلة لعرض نصوص وأزرار التطبيق"
        AppLanguage.ENGLISH -> "Choose preferred language for app interface & menus"
    }

    fun offlineModelsSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "موديلات الترجمة دون اتصال"
        AppLanguage.ENGLISH -> "Offline Translation Models"
    }

    fun offlineModelsSectionDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحميل الحزم للترجمة بدون اتصال بالإنترنت"
        AppLanguage.ENGLISH -> "Download language packages for offline translation"
    }

    fun masterDownloadTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حزم الترجمة دون اتصال (ML Kit)"
        AppLanguage.ENGLISH -> "Offline Translation Packages (ML Kit)"
    }

    fun downloadAllModelsBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحميل جميع الحزم (~90 MB)"
        AppLanguage.ENGLISH -> "Download All Models (~90 MB)"
    }

    fun allModelsDownloadedStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جميع الحزم محملة وجاهزة للعمل أوفلاين"
        AppLanguage.ENGLISH -> "All packages downloaded and ready offline"
    }

    fun availableLanguagePackages(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حزم اللغات المتوفرة"
        AppLanguage.ENGLISH -> "Available Language Packages"
    }

    fun modelDownloadedStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "محمل وجاهز"
        AppLanguage.ENGLISH -> "Downloaded & Ready"
    }

    fun modelNotDownloadedStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "غير محمل"
        AppLanguage.ENGLISH -> "Not Downloaded"
    }

    fun modelDownloadingStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جاري التحميل..."
        AppLanguage.ENGLISH -> "Downloading..."
    }

    fun downloadBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تنزيل"
        AppLanguage.ENGLISH -> "Download"
    }

    fun deleteBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حذف"
        AppLanguage.ENGLISH -> "Delete"
    }

    fun wifiOnlyTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التحميل عبر Wi-Fi فقط"
        AppLanguage.ENGLISH -> "Download via Wi-Fi only"
    }

    fun wifiOnlyDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "توفير بيانات الهاتف الخلوي أثناء التنزيل"
        AppLanguage.ENGLISH -> "Save mobile cellular data during downloads"
    }

    fun refreshStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحديث الحالة"
        AppLanguage.ENGLISH -> "Refresh Status"
    }

    fun aboutAppTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "عن التطبيق"
        AppLanguage.ENGLISH -> "About App"
    }

    fun aboutAppDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "قاموس ألماني-عربي متكامل ومترجم فوري دقيق يعمل بدون إنترنت بالكامل."
        AppLanguage.ENGLISH -> "Comprehensive German-Arabic dictionary & instant translator working 100% offline."
    }

    fun appVersion(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإصدار 1.0.0 • يعمل أوفلاين بالكامل"
        AppLanguage.ENGLISH -> "Version 1.0.0 • Fully Offline Capable"
    }
}
