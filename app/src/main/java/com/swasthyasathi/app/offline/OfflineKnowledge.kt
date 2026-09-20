package com.swasthyasathi.app.offline

object OfflineKnowledge {

    data class EmergencyAdvisory(
        val category: String,
        val english: String,
        val hindi: String,
        val bengali: String,
        val instantAction: String
    )

    private val advisories = listOf(
        EmergencyAdvisory(
            category = "heat_stroke",
            english = "Heat Stroke & Extreme Heat Exhaustion: Move immediately to a shaded or air-conditioned area. Loosen tight clothing, apply cold water compresses to neck and forehead, and sip electrolyte ORS solution slowly.",
            hindi = "हीट स्ट्रोक और अत्यधिक गर्मी: तुरंत छायादार या वातानुकूलित स्थान पर जाएं। तंग कपड़ों को ढीला करें, गर्दन और माथे पर ठंडा पानी लगाएं और धीरे-धीरे ओआरएस का घोल पीएं।",
            bengali = "হিট স্ট্রোক এবং চরম গরমের চাপ: অবিলম্বে ছায়াযুক্ত বা এয়ার-কন্ডিশনযুক্ত জায়গায় যান। পরনের পোশাক আলগা করুন, ঘাড়ে ও কপালে ঠান্ডা জল দিন এবং আস্তে আস্তে ওআরএস জল পান করুন।",
            instantAction = "Move to shade • Apply cold water compress • Drink ORS electrolyte"
        ),
        EmergencyAdvisory(
            category = "dehydration",
            english = "Severe Dehydration Risk: Ingest 400 mL electrolyte fluid or Oral Rehydration Solution. Avoid plain unmineralized tap water during extreme sweating. Rest for 20 minutes.",
            hindi = "गंभीर डिहाइड्रेशन का खतरा: 400 मिलीलीटर इलेक्ट्रोलाइट घोल या ओआरएस पीएं। बहुत पसीना आने पर सादा पानी पीने के बजाय ओआरएस लें। 20 मिनट विश्राम करें।",
            bengali = "চরম ডিহাইড্রেশনের ঝুঁকি: ৪০০ মিলি ইলেক্ট্রোলাইট জল বা ওআরএস পান করুন। অতিরিক্ত ঘাম হলে সাধারণ জলের পরিবর্তে ওআরএস নিন এবং ২০ মিনিট বিশ্রাম করুন।",
            instantAction = "Drink 400ml ORS • Rest 20 mins in cool area"
        ),
        EmergencyAdvisory(
            category = "aqi_respiratory",
            english = "Hazardous Air Quality & Respiratory Distress: Wear an N95 respirator mask outdoors. Seal indoor windows and activate air filtration. Avoid open-air exercise.",
            hindi = "खतरनाक वायु प्रदूषण और सांस की समस्या: बाहर जाते समय N95 मास्क पहनें। घर की खिड़कियां बंद रखें और इनडोर एयर प्यूरीफायर चलाएं। खुले में व्यायाम न करें।",
            bengali = "বিপজ্জনক বায়ুদূষণ ও শ্বাসকষ্ট: বাইরে বেরোলে N95 মাস্ক ব্যবহার করুন। ঘরের জানালা বন্ধ রাখুন এবং ইনডোর এয়ার ফিল্টার চালু রাখুন। খোলা বাতাসে ব্যায়াম করবেন না।",
            instantAction = "Wear N95 Mask • Stay Indoors • Use prescribed inhaler if asthmatic"
        ),
        EmergencyAdvisory(
            category = "fall_detection",
            english = "Fall Detected Protocol: Stay still for 30 seconds to assess for spinal or limb trauma. If uninjured, slowly rise using wall support. Automated SOS signal queued for dispatch.",
            hindi = "फॉल डिटेक्शन प्रोटोकॉल: चोट की जांच के लिए 30 सेकंड तक शांत रहें। यदि आप सुरक्षित हैं, तो धीरे-धीरे उठें। स्वचालित एसओएस सिग्नल कतारबद्ध है।",
            bengali = "ফল সনাক্তকরণ প্রোটোকল: কোনো আঘাত লেগেছে কিনা তা পরীক্ষা করতে ৩০ সেকেন্ড স্থির থাকুন। নিরাপদ বোধ করলে আস্তে আস্তে উঠুন। স্বয়ংক্রিয় এসওএস কিউ করা হয়েছে।",
            instantAction = "Assess trauma • Rise slowly • SOS notification dispatched"
        )
    )

    fun getOfflineAdvisory(query: String, language: String): String {
        val q = query.lowercase()
        val advisory = when {
            q.contains("dizzy") || q.contains("headache") || q.contains("heat") || q.contains("चक्कर") || q.contains("गर्मी") || q.contains("মাথা") ->
                advisories.find { it.category == "heat_stroke" }
            q.contains("water") || q.contains("dehydrat") || q.contains("पानी") || q.contains("प्याज") || q.contains("জল") ->
                advisories.find { it.category == "dehydration" }
            q.contains("breath") || q.contains("smog") || q.contains("pollution") || q.contains("सांस") || q.contains("দূষণ") ->
                advisories.find { it.category == "aqi_respiratory" }
            q.contains("fall") || q.contains("chot") || q.contains("गिर") || q.contains("পড়ে") ->
                advisories.find { it.category == "fall_detection" }
            else -> advisories[0]
        } ?: advisories[0]

        return when (language.lowercase()) {
            "hi" -> "ℹ️ **ऑफलाइन मोड क्लीनिकल सलाह**:\n${advisory.hindi}\n\n⚡ **त्वरित कदम**: ${advisory.instantAction}"
            "bn" -> "ℹ️ **অফলাইন মোড ক্লিনিকাল পরামর্শ**:\n${advisory.bengali}\n\n⚡ **জরুরী পদক্ষেপ**: ${advisory.instantAction}"
            else -> "ℹ️ **Offline Clinical Advisory**:\n${advisory.english}\n\n⚡ **Instant Action**: ${advisory.instantAction}"
        }
    }
}
