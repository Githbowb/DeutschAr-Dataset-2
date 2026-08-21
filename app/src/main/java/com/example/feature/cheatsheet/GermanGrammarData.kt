package com.example.feature.cheatsheet

object GermanGrammarData {

    val categories = listOf(
        CheatSheetCategory("ALL", "الكل", "Alle", "📚"),
        CheatSheetCategory("GENDER", "الجنس النحوي", "Genus / Artikel", "🏷️"),
        CheatSheetCategory("CASES", "الحالات الأربع", "Die 4 Kasus", "⚖️"),
        CheatSheetCategory("WORD_ORDER", "ترتيب الجملة", "Satzbau & Verben", "🧩")
    )

    val rules: List<GrammarRule> = listOf(
        // 1. GENDER (Genus / Artikel)
        GrammarRule(
            id = "rule_gender",
            titleAr = "الجنس النحوي وأدوات التعريف والتنكير",
            titleDe = "Genus und Artikel (der, die, das)",
            summaryAr = "في الألماني كل اسم له جنس نحوي (مذكر، مؤنث، محايد) لازم يتحفظ مع الأداة الخاصة به لأن الجنس النحوي ملوش علاقة بالجنس البيولوجي.",
            category = "GENDER",
            explanations = listOf(
                GrammarExplanation(
                    heading = "أنواع الأسماء وأدوات التعريف",
                    content = "الألماني يحتوي على 3 أجناس للأسماء في حالة المفرد:\n" +
                            "• der (مذكر - Maskulinum): مثل der Mann (الرجل), der Tisch (الترابيزة).\n" +
                            "• die (مؤنث - Femininum): مثل die Frau (المرأة), die Sonne (الشمس).\n" +
                            "• das (محايد - Neutrum): مثل das Kind (الطفل), das Buch (الكتاب).\n" +
                            "• die (الجمع - Plural): لكل الأسماء في الجمع بلا استثناء."
                ),
                GrammarExplanation(
                    heading = "أدوات التنكير والنفي (المفرد)",
                    content = "• المذكر: ein (نكرة) / kein (نفي النكرة)\n" +
                            "• المؤنث: eine (نكرة) / keine (نفي النكرة)\n" +
                            "• المحايد: ein (نكرة) / kein (نفي النكرة)\n" +
                            "• الجمع: لا توجد أداة نكرة (نقول: Bücher)، ولكن في النفي نستخدم keine Bücher."
                ),
                GrammarExplanation(
                    heading = "نهايات مشهورة بتساعدك تعرف الأداة بسهولة",
                    content = "• der (مذكر): الكلمات المنتهية بـ -er, -ling, -or, -ist, -ismus، وأيام الأسبوع والشهور وفصول السنة.\n" +
                            "• die (مؤنث): الكلمات المنتهية بـ -ung, -heit, -keit, -schaft, -tion, -tät, -ie, -e (بنسبة 90%).\n" +
                            "• das (محايد): الكلمات المنتهية بـ -chen, -lein, -um, -ment, -ma, -o، وتصغير الكلمات، والمصادر المستعملة كأسماء (das Essen, das Trinken)."
                )
            ),
            tables = listOf(
                GrammarTable(
                    title = "جدول أدوات التعريف والتنكير والنفي في حالة الرفع (Nominativ)",
                    headers = listOf("الجنس", "معرفة (Bestimmt)", "نكرة (Unbestimmt)", "نفي النكرة (Negation)"),
                    rows = listOf(
                        listOf("مذكر (Maskulin)", "der", "ein", "kein"),
                        listOf("مؤنث (Feminin)", "die", "eine", "keine"),
                        listOf("محايد (Neutral)", "das", "ein", "kein"),
                        listOf("جمع (Plural)", "die", "— (بدون نكرة)", "keine")
                    )
                )
            ),
            examples = listOf(
                GrammarExample("Der Tisch ist neu.", "الترابيزة جديدة.", "كلمة Tisch في الألماني مذكر der مع إنها في العربي مؤنث (ترابيزة)."),
                GrammarExample("Das Mädchen liest ein Buch.", "البنت تقرأ كتاباً.", "كلمة Mädchen محايد das لأنها منتهية بـ -chen للتصغير."),
                GrammarExample("Die Hoffnung stirbt zuletzt.", "الأمل يموت آخراً.", "كلمة Hoffnung مؤنث die لأنها منتهية بـ -ung.")
            ),
            importantTips = listOf(
                "💡 سر التفوق: متسميش الكلمة لوحدها أبداً! متقولش Tisch = ترابيزة، احفظها دايماً 'der Tisch'.",
                "💡 متقيسش على العربي! الشمس في الألماني مؤنث (die Sonne) والقمر مذكر (der Mond).",
                "💡 كل الكلمات المصغرة بـ -chen أو -lein بتاخد das حتى لو كانت شخص (das Mädchen)."
            )
        ),

        // 2. CASES (Die 4 Kasus)
        GrammarRule(
            id = "rule_cases_overview",
            titleAr = "الحالات الإعرابية الأربع (Nominativ, Akkusativ, Dativ, Genitiv)",
            titleDe = "Die vier Fälle im Deutschen",
            summaryAr = "الحالات الإعرابية بتحدد وظيفة الاسم في الجملة (فاعل، مفعول مباشر، مفعول غير مباشر، ملكية) وبناءً عليها بتتغير أداة التعريف والتنكير وصفات الملكية.",
            category = "CASES",
            explanations = listOf(
                GrammarExplanation(
                    heading = "1. حالة الرفع (Nominativ) - الفاعل",
                    content = "• مين اللي عمل الفعل؟ (Wer / Was?)\n" +
                            "• هو الشكل الأساسي للاسم كما في القاموس.\n" +
                            "• بييجي كمان بعد أفعال زي: sein (يكون), werden (يصبح), bleiben (يبقى)."
                ),
                GrammarExplanation(
                    heading = "2. حالة النصب (Akkusativ) - المفعول به المباشر",
                    content = "• وقع عليه فعل الفاعل مباشرة (Wen / Was?)\n" +
                            "• القاعدة الذهبية: التغيير الوحيد بيحصل في المذكر المفرد بس (der تتحول لـ den / ein تتحول لـ einen).\n" +
                            "• المؤنث والمحايد والجمع بيفضلوا زي ما هم في الـ Nominativ تماماً!\n" +
                            "• حروف جر تطلب دائماً Akkusativ: für, durch, gegen, ohne, um, bis."
                ),
                GrammarExplanation(
                    heading = "3. حالة المجرور / القابل (Dativ) - المفعول غير المباشر / المستفيد",
                    content = "• لمين أو مع مين؟ (Wem?)\n" +
                            "• التغييرات في كل الأجناس:\n" +
                            "  - المذكر والمحايد: der/das يتحولوا لـ dem (ein -> einem).\n" +
                            "  - المؤنث: die تتحول لـ der (eine -> einer).\n" +
                            "  - الجمع: die تتحول لـ den + بنضيف حرف n لآخر الاسم لو مش آخره n أو s (den Kindern).\n" +
                            "• حروف جر تطلب دائماً Dativ: aus, bei, mit, nach, seit, von, zu, gegenüber."
                ),
                GrammarExplanation(
                    heading = "4. حالة الإضافة / المجرور بالملكية (Genitiv) - المضاف إليه",
                    content = "• لمن هذا الشيء؟ (Wessen?)\n" +
                            "• التغييرات:\n" +
                            "  - المذكر والمحايد: der/das يتحولوا لـ des + بنزود (s أو es) لآخر الاسم نفسه (des Mannes, des Kindes).\n" +
                            "  - المؤنث والجمع: die تتحول لـ der بدون إضافة نهايات للاسم.\n" +
                            "• حروف جر مشهورة للـ Genitiv: wegen (بسبب), während (أثناء), trotz (بالرغم من), statt (بدلاً من)."
                )
            ),
            tables = listOf(
                GrammarTable(
                    title = "الجدول الشامل لأدوات التعريف في الحالات الأربع (Definite Articles)",
                    headers = listOf("الحالة (Fall)", "مذكر (Maskulin)", "محايد (Neutral)", "مؤنث (Feminin)", "جمع (Plural)"),
                    rows = listOf(
                        listOf("Nominativ (فاعل)", "der", "das", "die", "die"),
                        listOf("Akkusativ (مفعول به)", "den", "das", "die", "die"),
                        listOf("Dativ (مجرور/غير مباشر)", "dem", "dem", "der", "den (+n)"),
                        listOf("Genitiv (ملكية/إضافة)", "des (+s/es)", "des (+s/es)", "der", "der")
                    )
                ),
                GrammarTable(
                    title = "جدول أدوات التنكير في الحالات الأربع (Indefinite Articles)",
                    headers = listOf("الحالة (Fall)", "مذكر (Maskulin)", "محايد (Neutral)", "مؤنث (Feminin)", "جمع (Plural)"),
                    rows = listOf(
                        listOf("Nominativ", "ein", "ein", "eine", "keine"),
                        listOf("Akkusativ", "einen", "ein", "eine", "keine"),
                        listOf("Dativ", "einem", "einem", "einer", "keinen (+n)"),
                        listOf("Genitiv", "eines (+s/es)", "eines (+s/es)", "einer", "keiner")
                    )
                )
            ),
            examples = listOf(
                GrammarExample(
                    german = "Der Vater (Nom) gibt dem Sohn (Dat) den Apfel (Akk).",
                    arabic = "الأب يعطي الابن التفاحة.",
                    explanation = "الأب فاعل (der Vater)، الابن هو المستفيد غير المباشر (dem Sohn - داتيف)، والتفاحة مفعول مباشر مذكر اتحولت لـ den Apfel (أكوزاتيف)."
                ),
                GrammarExample(
                    german = "Das Auto des Lehrers steht hier.",
                    arabic = "عربية المدرس واقفة هنا.",
                    explanation = "عربية المدرس: Lehrer مذكر في الجينيتيف أخد des وزودنا s في آخره (des Lehrers)."
                ),
                GrammarExample(
                    german = "Ich fahre mit dem Bus zur Schule.",
                    arabic = "أنا بروح بالأتوبيس للمدرسة.",
                    explanation = "بعد حرف الجر mit بييجي دايماً داتيف، Bus مذكر أخد dem Bus."
                )
            ),
            importantTips = listOf(
                "💡 مفتاح الأكوزاتيف: احفظ إنه بيغير 'المذكر بس' (der -> den). المحايد والمؤنث والجمع زي الفاعل بالظبط.",
                "💡 مفتاح الداتيف: المذكر والمحايد بياخدوا dem (تخيلهم توأم)، المؤنث بيتحول لـ der!",
                "💡 ترتيب الضمائر مع فعل بياخد مفعولين: لو الاتنين ضمائر -> الأكوزاتيف قبل الداتيف (Ich gebe es ihm)."
            )
        ),

        // 3. WORD ORDER & VERB POSITION (Satzbau)
        GrammarRule(
            id = "rule_word_order",
            titleAr = "ترتيب عناصر الجملة وموقع الفعل (Satzbau)",
            titleDe = "Satzstellung und Verben (Hauptsatz & Nebensatz)",
            summaryAr = "أهم قانون في اللغة الألمانية هو موقع الفعل المصرف: في الجملة الرئيسية دايماً رقم 2، وفي الجملة الفرعية في آخر الجملة تماماً.",
            category = "WORD_ORDER",
            explanations = listOf(
                GrammarExplanation(
                    heading = "1. الجملة الخبرية العادية (Hauptsatz) - قاعدة الفعل في المركز الثاني (V2)",
                    content = "• الفعل المصرف (Konjugiertes Verb) **لازم** يجي في الموضع رقم 2 في الجملة.\n" +
                            "• 'الموضع 2' مش معناه الكلمة التانية، معناه العنصر أو التركيب التاني.\n" +
                            "• لو بدأت بالفاعل: (Ich lerne heute Deutsch).\n" +
                            "• لو بدأت بالظرف أو الزمان (Inversion): الفعل بيفضل رقم 2 والفاعل بيرجع رقم 3 (Heute lerne ich Deutsch)."
                ),
                GrammarExplanation(
                    heading = "2. الفعلين في جملة واحدة (قوس الجملة - Satzklammer)",
                    content = "إذا كان في الجملة فعل مساعد أو ناقص (Modalverb) أو زمن تام (Perfekt) أو فعل منفصل (Trennbares Verb):\n" +
                            "• الفعل المصرف بيحتل الموضع 2.\n" +
                            "• الفعل التاني (المصدر Infinitiv أو التصريف التالت Partizip II أو البادئة المنفصلة) بيروح **في آخر الجملة خالص**."
                ),
                GrammarExplanation(
                    heading = "3. الجملة الجانبية / الفرعية (Nebensatz) - الفعل في النهاية",
                    content = "مع الروابط التي تبدأ جملة جانبية (مثل: weil, dass, wenn, ob, obwohl, da, als):\n" +
                            "• الفعل المصرف بيطرد لآخر الجملة تماماً.\n" +
                            "• مثال: Ich lerne Deutsch, **weil** ich in Deutschland studieren **möchte**."
                ),
                GrammarExplanation(
                    heading = "4. ترتيب الظروف في الجملة (قاعدة TeKaMoLo)",
                    content = "لو عندك كذا ظرف في الجملة رتبهم كالآتي:\n" +
                            "1. **Te**mporal (الزمان - Wann?)\n" +
                            "2. **Ka**usal (السبب - Warum?)\n" +
                            "3. **Mo**dal (الكيفية/الطريقة - Wie?)\n" +
                            "4. **Lo**kal (المكان - Wo / Wohin?)"
                )
            ),
            tables = listOf(
                GrammarTable(
                    title = "مقارنة الروابط: روابط لا تغير الترتيب (ADUSO) مقابل روابط ترسل الفعل للآخر",
                    headers = listOf("النوع", "أشهر الروابط", "تأثيرها على الفعل المصرف", "مثال"),
                    rows = listOf(
                        listOf("روابط صفرية (ADUSO)", "aber (لكن), denn (لأن), und (و), sondern (بل), oder (أو)", "الفعل يظل في المركز 2 كما هو", "Ich gehe, denn ich habe Zeit."),
                        listOf("روابط جانبية (Nebensatz)", "weil (لأن), dass (أن), wenn (إذا/لو), obwohl (رغم أن), als (عندما)", "الفعل المصرف يذهب لآخر الجملة تماماً", "Ich gehe, weil ich Zeit habe."),
                        listOf("ظروف ربط (Konjunktionaladverbien)", "deshalb (لذلك), trotzdem (مع ذلك), dann (ثم), sonst (وإلا)", "الفعل يأتي بعدها فوراً في الموضع 2", "Ich habe Zeit, deshalb gehe ich.")
                    )
                )
            ),
            examples = listOf(
                GrammarExample(
                    german = "Morgen **fahre** ich mit dem Zug nach Berlin.",
                    arabic = "بكرة هسافر بالقطار لبرلين.",
                    explanation = "بدأنا بالزمان (Morgen - عنصر 1)، الفعل جه رقم 2 (fahre)، والفاعل جه رقم 3 (ich)."
                ),
                GrammarExample(
                    german = "Ich **muss** jeden Tag fleißig Deutsch **lernen**.",
                    arabic = "يجب أن أذاكر ألماني باجتهاد كل يوم.",
                    explanation = "الفعل الناقص muss في الموضع 2، والمصدر lernen راح في آخر الجملة خالص (Satzklammer)."
                ),
                GrammarExample(
                    german = "Er **steht** jeden Morgen um 6 Uhr **auf**.",
                    arabic = "هو يصحى كل يوم الصبح الساعة 6.",
                    explanation = "الفعل aufstehen منفصل: الأصل steht في الموضع 2 والبادئة auf في آخر الجملة."
                )
            ),
            importantTips = listOf(
                "💡 احفظ كلمة 'ADUSO': دي 5 روابط بتعتبر في الموضع صفر ومش بتغير موقع الفعل (Aber, Denn, Und, Sondern, Oder).",
                "💡 الفرق بين denn و weil: الاتنين معناهم 'لأن'، بس denn الفعل بعدها بيجي رقم 2، وweil بتشوط الفعل لآخر الجملة!",
                "💡 قاعدة TeKaMoLo: رتب ظروفك (زمان -> سبب -> طريقة -> مكان) عشان جملتك تطلع ألمانية فصحى طبيعية."
            )
        )
    )
}
