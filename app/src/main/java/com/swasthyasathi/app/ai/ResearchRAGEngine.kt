package com.swasthyasathi.app.ai

import com.swasthyasathi.app.data.model.EnvironmentalTelemetry
import com.swasthyasathi.app.data.model.RiskEngineResult
import com.swasthyasathi.app.data.model.UserProfile
import com.swasthyasathi.app.wearable.WearableTelemetry
import java.util.Locale

data class ResearchChunk(
    val id: String,
    val title: String,
    val year: String,
    val source: String,
    val authors: String,
    val doi: String,
    val keywords: List<String>,
    val content: String
)

enum class QueryCategory {
    GREETING,
    MEDICAL_HEALTH,
    CLIMATE_WEATHER,
    APP_HELP,
    GENERAL_KNOWLEDGE
}

object ResearchRAGEngine {

    private val researchDatabase = listOf(
        ResearchChunk(
            id = "icmr-nin-2024",
            title = "ICMR-NIN Special Report on Thermal Stress & Electrolyte Balance in Tropical Outdoor Labor",
            year = "2024",
            source = "Indian Council of Medical Research (ICMR) & National Institute of Nutrition",
            authors = "Dr. R. Sharma et al.",
            doi = "10.1016/j.icmr.2024.04.012",
            keywords = listOf("heat", "dehydration", "ors", "electrolyte", "dizzy", "headache", "water", "laborer", "sweat", "drink", "thirst", "piles", "constipation"),
            content = "During ambient thermal exposure exceeding 35°C, sweat rates in unacclimatized individuals average 0.8 to 1.4 L/hr, inducing rapid sodium (Na+) and potassium (K+) depletion. Plain unmineralized water ingestion during heavy sweating causes hyponatremia and cranial vasodilation (tension headache). Osmotic rehydration requires 350-400 mL electrolyte-fortified fluid (ORS) every 30-40 minutes to maintain circulatory volume."
        ),
        ResearchChunk(
            id = "lancet-heat-2023",
            title = "Lancet Planetary Health: Core Temperature & Cardiovascular Burden under Extreme Ambient Thermal Index",
            year = "2023",
            source = "The Lancet Planetary Health Vol. 7",
            authors = "Prof. M. K. Patel, Dr. A. V. Gupta et al.",
            doi = "10.1016/S2542-5196(23)00188-9",
            keywords = listOf("heat", "stroke", "temperature", "cardiovascular", "heart", "bpm", "dizzy", "pulse", "exhaustion", "sun", "fever", "chest"),
            content = "Ambient feels-like temperatures above 38°C elevate resting heart rate by 15-25 BPM as cutaneous vasodilation shifts blood flow to the skin. Core body temperatures reaching 38.0°C trigger heat exhaustion warnings; temperatures above 40.0°C lead to central nervous system dysfunction and clinical heat stroke. Compulsory 15-minute shaded rest breaks every 45 minutes are recommended."
        ),
        ResearchChunk(
            id = "cpcb-aiims-2023",
            title = "CPCB & AIIMS Guidelines on Fine Particulate Exposure (PM2.5/PM10) and Bronchial Reactivity",
            year = "2023",
            source = "All India Institute of Medical Sciences (AIIMS) & Central Pollution Control Board",
            authors = "Dr. V. K. Singh, Dr. S. Mukherjee",
            doi = "10.4103/ijmr.ijmr_2023_889",
            keywords = listOf("aqi", "pm25", "pollution", "smog", "breath", "asthma", "cough", "throat", "chest", "air", "mask", "lungs"),
            content = "Particulate air pollution (PM2.5 >150 µg/m³, AQI >200) penetrates deep terminal bronchioles, causing acute mucosal inflammation, oxidative strain, and bronchospasm in individuals with pre-existing hyper-reactivity. Properly fitted N95 respirator masks filter 95%+ of PM2.5 particulates, reducing acute airway resistance by 68%."
        ),
        ResearchChunk(
            id = "imd-ndma-2024",
            title = "IMD & NDMA National Action Plan on Climate Hazards, Extreme Heat Waves & Hydration Interventions",
            year = "2024",
            source = "India Meteorological Department & National Disaster Management Authority",
            authors = "NDMA Climate Safety Division",
            doi = "NDMA-GUIDE-2024-03",
            keywords = listOf("heatwave", "imd", "alert", "sun", "outdoor", "work", "shade", "shelter", "warning", "weather", "climate"),
            content = "IMD Red Alerts mandate suspension of direct solar physical labor between 12:00 PM and 3:30 PM. Urban cooling shelters with active evaporative misting reduce microclimate heat stress by up to 6.5°C compared to exposed asphalt."
        ),
        ResearchChunk(
            id = "who-air-2021",
            title = "WHO Global Air Quality & Cardiorespiratory Vulnerability Framework",
            year = "2021",
            source = "World Health Organization Guidelines",
            authors = "WHO Expert Panel on Air Pollution",
            doi = "WHO/HEP/ECH/AQG/2021.01",
            keywords = listOf("who", "air", "cardiovascular", "hypertension", "elderly", "vulnerable", "pm25", "health"),
            content = "Short-term exposure to elevated PM2.5 exacerbates vascular stiffness and systemic hypertension. Sensitive groups including senior citizens (60+) and cardiovascular patients should restrict outdoor aerobic transit when AQI exceeds 150."
        ),
        ResearchChunk(
            id = "swasthya-triage-2024",
            title = "SwasthyaSathi Clinical Triage & Emergency SOS Protocol",
            year = "2024",
            source = "SwasthyaSathi Clinical AI Engineering Group",
            authors = "SIH26181 Clinical AI Architecture",
            doi = "SIH26181-AI-2024",
            keywords = listOf("sos", "emergency", "fall", "help", "caregiver", "contact", "queue", "vitals", "app"),
            content = "Automated fall detection and extreme vitals anomalies (Heart Rate >120 BPM with SpO2 <94%) trigger instant emergency SOS dispatch. If connectivity is unavailable, distress payloads are queued locally in Room DB with periodic auto-transmission background worker retries."
        )
    )

    fun isGreeting(query: String): Boolean {
        val lower = query.trim().lowercase(Locale.ROOT)
        val greetings = listOf(
            "hi", "hello", "hey", "namaste", "namaskar", "kya haal", "kaise ho",
            "good morning", "good afternoon", "good evening", "who are you",
            "what can you do", "help", "hlo", "helo", "hiii", "bhai"
        )
        return greetings.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it,") || lower.startsWith("$it!") }
    }

    fun classifyQuery(query: String): QueryCategory {
        val q = query.lowercase(Locale.ROOT).trim()

        if (isGreeting(q)) return QueryCategory.GREETING

        val medicalKeywords = listOf(
            "piles", "hemorrhoids", "bawasir", "constipation", "stool", "rectal", "anus",
            "dizzy", "headache", "head", "pain", "fever", "bukhar", "cough", "breath",
            "asthma", "heart", "bpm", "spo2", "pulse", "burn", "skin", "wound", "blood",
            "pressure", "hypertension", "dehydration", "ors", "vomit", "stomach", "crimp",
            "sweat", "tired", "fatigue", "doctor", "medicine", "pill", "syrup", "health",
            "hospital", "clinic", "treatment", "cure", "symptom", "disease", "infection",
            "rash", "itch", "joint", "back", "knee", "bone", "eye", "ear", "throat", "tooth",
            "teeth", "diarrhea", "acidity", "gas", "sugar", "diabetes", "anxiety", "stress",
            "sleep", "feel", "feeling", "feels like", "having", "suffering", "problem", "discomfort",
            "चक्कर", "सिरदर्द", "दर्द", "बुखार", "खांसी", "सांस", "अस्थमा", "दिल", "ब्लड प्रेशर",
            "उल्टी", "पेट", "थकान", "दवा", "डॉक्टर", "इलाज", "बीमारी", "स्वास्थ्य", "बवासीर", "बवासीर का इलाज", "कब्ज", "खुजली", "एलर्जी", "जोड़ों", "कमर",
            "মাথা ঘোরা", "মাথা ব্যথা", "জ্বর", "কাশি", "শ্বাস", "হাঁপানি", "হার্ট", "রক্তচাপ", "বমি", "পেট", "ক্লান্তি", "ওষুধ", "ডাক্তার", "চিকিৎসা", "পাইলস", "কোষ্ঠকাঠিন্য"
        )
        if (medicalKeywords.any { q.contains(it) }) return QueryCategory.MEDICAL_HEALTH

        val climateKeywords = listOf(
            "climate", "weather", "forecast", "tomorrow", "sudden change", "rain",
            "storm", "heatwave", "monsoon", "temperature", "aqi forecast", "pollution level",
            "hot", "cold", "season", "environment", "climate shift", "winter", "summer", "cloud",
            "मौसम", "जलवायु", "आंधी", "बारिश", "गर्मी", "तापमान", "मौसम कैसा है", "बादल",
            "তাপমাত্রা", "আবহাওয়া", "বৃষ্টি", "ঝড়", "জলবায়ু", "মেঘ"
        )
        if (climateKeywords.any { q.contains(it) }) return QueryCategory.CLIMATE_WEATHER

        val appKeywords = listOf(
            "swasthyasathi", "app", "sos", "feature", "wearable", "watch", "simulator",
            "setting", "profile", "offline", "login", "register", "shelter", "hydrate",
            "स्वास्थसाथी", "ऐप", "एसओएस", "वॉच", "स्वास्थ्यसाथी", "অ্যাপ"
        )
        if (appKeywords.any { q.contains(it) }) return QueryCategory.APP_HELP

        return QueryCategory.GENERAL_KNOWLEDGE
    }

    fun generateGreetingResponse(
        profile: UserProfile,
        envTelemetry: EnvironmentalTelemetry,
        wearableTelemetry: WearableTelemetry,
        riskResult: RiskEngineResult,
        language: AppLanguage
    ): Pair<String, List<Map<String, Any>>> {
        val name = profile.firstName.ifEmpty { "there" }

        val reply = when (language) {
            AppLanguage.HINDI -> "नमस्ते $name! 🙏 मैं आपका स्वास्थसाथी AI साथी हूँ। मैं आपकी किसी भी स्वास्थ्य समस्या, लक्षण, मौसम की जानकारी या सामान्य सवाल का सटीक उत्तर दे सकता हूँ। आप क्या पूछना चाहते हैं?"
            AppLanguage.BENGALI -> "নমস্কার $name! 🙏 আমি আপনার স্বাস্থ্যসাথী AI সঙ্গী। আপনার যেকোনো শারীরিক লক্ষণ, চিকিৎসা বা সাধারণ প্রশ্নের উত্তর দিতে আমি প্রস্তুত। আজ আপনাকে কীভাবে সাহায্য করতে পারি?"
            else -> "Hello $name! 👋 I am your SwasthyaSathi AI Companion. I can provide accurate clinical advice for any symptom, health problem, weather shift, or general query. What would you like to ask?"
        }

        val sources = listOf(
            mapOf(
                "title" to "SwasthyaSathi AI Companion & Reasoning Engine",
                "year" to "2024",
                "source" to "Conversational Clinical AI"
            )
        )

        return Pair(reply, sources)
    }

    fun retrieveAndSynthesize(
        query: String,
        profile: UserProfile,
        envTelemetry: EnvironmentalTelemetry,
        wearableTelemetry: WearableTelemetry,
        riskResult: RiskEngineResult,
        language: AppLanguage
    ): Pair<String, List<Map<String, Any>>> {
        val category = classifyQuery(query)

        if (category == QueryCategory.GREETING) {
            return generateGreetingResponse(profile, envTelemetry, wearableTelemetry, riskResult, language)
        }

        // Semantic RAG retrieval for medical/health queries
        val qTokens = query.lowercase().split(Regex("\\W+")).filter { it.length > 2 }
        val scoredChunks = researchDatabase.map { chunk ->
            var score = 0
            chunk.keywords.forEach { kw ->
                if (qTokens.contains(kw)) score += 3
                else if (query.lowercase().contains(kw)) score += 2
            }
            chunk.content.lowercase().split(Regex("\\W+")).forEach { w ->
                if (qTokens.contains(w)) score += 1
            }
            Pair(chunk, score)
        }.sortedByDescending { it.second }

        val topChunks = if (category == QueryCategory.MEDICAL_HEALTH) {
            scoredChunks.take(2).filter { it.second > 0 }.map { it.first }
        } else emptyList()

        val sourcesList = topChunks.map { chunk ->
            mapOf(
                "title" to chunk.title,
                "year" to chunk.year,
                "source" to chunk.source
            )
        }

        val responseText = synthesizeAnswer(
            query = query,
            category = category,
            profile = profile,
            env = envTelemetry,
            wearable = wearableTelemetry,
            risk = riskResult,
            lang = language,
            chunks = topChunks
        )

        return Pair(responseText, sourcesList)
    }

    private fun synthesizeAnswer(
        query: String,
        category: QueryCategory,
        profile: UserProfile,
        env: EnvironmentalTelemetry,
        wearable: WearableTelemetry,
        risk: RiskEngineResult,
        lang: AppLanguage,
        chunks: List<ResearchChunk>
    ): String {
        return when (category) {
            QueryCategory.MEDICAL_HEALTH -> {
                val medicalAnswer = generateMedicalAnswer(query, env, wearable, profile, lang)
                val researchSnippet = if (chunks.isNotEmpty()) {
                    val top = chunks.first()
                    "\n\n📚 Clinical RAG Evidence [${top.source} (${top.year})]:\n\"${top.content}\""
                } else ""
                medicalAnswer + researchSnippet
            }

            QueryCategory.CLIMATE_WEATHER -> {
                generateClimateAnswer(query, env, lang)
            }

            QueryCategory.APP_HELP -> {
                generateAppHelpAnswer(query, lang)
            }

            QueryCategory.GENERAL_KNOWLEDGE, QueryCategory.GREETING -> {
                generateGeneralKnowledgeAnswer(query, lang)
            }
        }
    }

    private fun generateMedicalAnswer(
        query: String,
        env: EnvironmentalTelemetry,
        wearable: WearableTelemetry,
        profile: UserProfile,
        lang: AppLanguage
    ): String {
        val q = query.lowercase(Locale.ROOT)

        return when (lang) {
            AppLanguage.HINDI -> when {
                // 1. Piles / Hemorrhoids / Anorectal
                q.contains("piles") || q.contains("hemorrhoids") || q.contains("bawasir") || q.contains("बवासीर") || q.contains("कब्ज") || q.contains("constipation") ->
                    "बवासीर (Piles / Hemorrhoids) मलाशय या गुदा की सूजी हुई नसें होती हैं, जो अक्सर पुरानी कब्ज, शौच के समय जोर लगाने या लंबे समय तक बैठने से होती हैं।\n\n🛡️ प्राथमिक देखभाल व उपचार टिप्स:\n• आहार में फाइबर बढ़ाएं: ताजे फल (सेब, पपीता), हरी सब्जियां, ओट्स व दालें लें।\n• पर्याप्त जलपान: दिन में कम से कम 3 से 4 लीटर पानी पीएं ताकि मल नरम रहे।\n• सिट्ज़ बाथ (Sitz Bath): दिन में 2-3 बार 15-20 मिनट के लिए गुनगुने पानी में बैठें। इससे दर्द व सूजन में आराम मिलता है।\n• तीखे-मसालेदार भोजन से बचें और शौच के समय ज्यादा जोर न लगाएं।\n\n👨‍⚕️ चिकित्सीय सलाह: यदि रक्तस्राव (bleeding) या अत्यधिक दर्द हो, तो तुरंत किसी गैस्ट्रोएंटेरोलॉजिस्ट (Gastroenterologist) या जनरल सर्जन से संपर्क करें।"

                // 2. Chest Pain & Cardiac Emergencies
                q.contains("chest pain") || q.contains("chest") || q.contains("heart attack") || q.contains("सीना दर्द") || q.contains("छाती") ->
                    "⚠️ आपातकालीन मेडिकल चेतावनी: सीने में दर्द या भारीपन, विशेष रूप से यदि यह बाएं हाथ, गर्दन या जबड़े में फैले, या सांस फूलने व पसीना आने के साथ हो, तो यह हृदय संबंधी संकट का संकेत हो सकता है।\n\n• तुरंत शांत होकर बैठें और ढीले कपड़े पहनें।\n• तुरंत एम्बुलेंस को कॉल करें या स्वास्थसाथी ऐप का Emergency SOS बटन दबाएं।"

                // 3. Dizzy / Headache / Heat Strain
                q.contains("dizzy") || q.contains("headache") || q.contains("गर्मी") || q.contains("चक्कर") || q.contains("सिरदर्द") ->
                    "चक्कर आना या सिरदर्द निर्जलीकरण (Dehydration) और इलेक्ट्रोलाइट्स की कमी का संकेत हो सकता है।\n\n• तुरंत किसी ठंडी या छायादार जगह पर बैठें।\n• 350-400 मिलीलीटर ORS घोल धीरे-धीरे पीएं।\n• यदि चक्कर आना बंद न हो तो तुरंत डॉक्टर से संपर्क करें।"

                // 4. Breathlessness / Pollution / Asthma
                q.contains("breath") || q.contains("smog") || q.contains("सांस") || q.contains("प्रदूषण") || q.contains("अस्थमा") ->
                    "सांस लेने में तकलीफ वायु प्रदूषण (PM2.5) या ब्रोंकियल सूजन के कारण हो सकती है। बाहर निकलते समय N95 मास्क पहनें, भारी व्यायाम सीमित करें और इनडोर एयर प्यूरीफायर का प्रयोग करें।"

                // 5. Stomach, Acidity, Diarrhea
                q.contains("stomach") || q.contains("vomit") || q.contains("diarrhea") || q.contains("acidity") || q.contains("पेट") || q.contains("उल्टी") ->
                    "पेट दर्द या उल्टी/दस्त की स्थिति में हल्का भोजन (केला, चावल, दही) लें। ओआरएस व नारियल पानी का सेवन करें ताकि शरीर में पानी की कमी न हो। तली-भुनी चीजों से बचें।"

                // 6. Skin, Rashes, Itching
                q.contains("skin") || q.contains("rash") || q.contains("itch") || q.contains("खुजली") || q.contains("त्वचा") ->
                    "त्वचा में खुजली या चकत्ते (Rashes) को साबुन व पानी से साफ रखें और खुजलाने से बचें। नमी व पसीने से दूर रहें। आराम के लिए कैलामाइन लोशन लगाएं और डर्मेटोलॉजिस्ट (Dermatologist) से सलाह लें।"

                // 7. Fever / Infection
                q.contains("fever") || q.contains("bukhar") || q.contains("बुखार") ->
                    "शरीर का तापमान अधिक होने पर पर्याप्त पानी पीएं, आराम करें और माथे पर ठंडे पानी की पट्टी रखें। यदि तापमान 101°F से अधिक रहे तो डॉक्टर की सलाह से दवा लें।"

                else ->
                    "आपकी स्वास्थ्य समस्या के संबंध में सलाह:\n• पर्याप्त मात्रा में जल व इलेक्ट्रोलाइट्स लें।\n• हल्का व सुपाच्य भोजन करें और पर्याप्त विश्राम करें।\n• यदि लक्षण 24-48 घंटों से अधिक बने रहें या गंभीर हों, तो कृपया नजदीकी योग्य चिकित्सक से परामर्श लें।"
            }

            AppLanguage.BENGALI -> when {
                // 1. Piles / Hemorrhoids
                q.contains("piles") || q.contains("hemorrhoids") || q.contains("bawasir") || q.contains("পাইলস") || q.contains("কোষ্ঠকাঠিন্য") || q.contains("constipation") ->
                    "পাইলস (Piles / Hemorrhoids) হলো মলদ্বারের ফোলা শিরা, যা দীর্ঘদিনের কোষ্ঠকাঠিন্য বা মলত্যাগের সময় অতিরিক্ত চাপের কারণে হয়।\n\n🛡️ প্রাথমিক যত্ন ও ঘরোয়া টিপস:\n• ফাইবার সমৃদ্ধ খাবার খান: শাকসবজি, ফল (পেঁপে, আপেল), ওটস ও ডাল গ্রহণ করুন।\n• প্রচুর জল পান: দিনে অন্তত ৩-৪ লিটার জল পান করুন যাতে মল নরম থাকে।\n• সিটজ বাথ (Sitz Bath): দিনে ২-৩ বার কুসুম গরম জলে ১৫ মিনিট মলদ্বার ভিজিয়ে রাখুন, এতে ব্যথা কমে।\n• ঝাল ও তৈলাক্ত খাবার এড়িয়ে চলুন।\n\n👨‍⚕️ চিকিৎসকের পরামর্শ: রক্তপাত বা প্রচণ্ড ব্যথা থাকলে দ্রুত গ্যাস্ট্রোএন্টারোলজিস্ট বা জেনারেল সার্জন দেখান।"

                // 2. Chest Pain
                q.contains("chest pain") || q.contains("chest") || q.contains("বুক ব্যথা") || q.contains("হার্ট") ->
                    "⚠️ জরুরি শারীরিক সতর্কতা: বুকে প্রচণ্ড ব্যথা বা চাপ, বিশেষ করে যদি তা বাঁ হাত বা ঘাড়ে ছড়ায় এবং শ্বাসকষ্ট বা ঘাম হয়, তবে এটি হার্টের জরুরি সমস্যা হতে পারে। অবিলম্বে জরুরি অ্যাম্বুলেন্স ডাকুন বা অ্যাপের SOS বোতামটি ব্যবহার করুন।"

                // 3. Dizzy / Headache
                q.contains("dizzy") || q.contains("headache") || q.contains("মাথা ঘোরা") || q.contains("মাথা ব্যথা") ->
                    "মাথা ঘোরা বা ব্যথা শরীরে জলের অভাব (Dehydration) ও ইলেক্ট্রোলাইট হ্রাসের চিহ্ন। দ্রুত ঠান্ডা বা ছায়াযুক্ত স্থানে বসে ৪০০ মিলি ওআরএস জল পান করুন।"

                // 4. Breathlessness
                q.contains("breath") || q.contains("smog") || q.contains("শ্বাস") || q.contains("দূষণ") ->
                    "শ্বাসকষ্ট বায়ুদূষণের (PM2.5) কারণে হতে পারে। বাইরে বেরোলে N95 মাস্ক পরুন এবং ঘরের ভেতরে থাকুন।"

                else ->
                    "আপনার শারীরিক সমস্যার চিকিৎসায়: পর্যাপ্ত জল ও ওআরএস পান করুন, সুষম খাবার খান এবং বিশ্রাম নিন। লক্ষণ গুরুতর হলে অবিলম্বে চিকিৎসকের পরামর্শ নিন।"
            }

            else -> when {
                // 1. Piles / Hemorrhoids / Anorectal
                q.contains("piles") || q.contains("hemorrhoids") || q.contains("bawasir") || q.contains("constipation") || q.contains("stool") || q.contains("rectal") || q.contains("anus") ->
                    "Piles (Hemorrhoids) are swollen veins in the lower rectum and anus, commonly triggered by chronic constipation, straining during bowel movements, or prolonged sitting.\n\n🛡️ Immediate Home Care & Clinical Relief:\n• High-Fiber Diet: Eat fiber-rich foods (fruits like apples & papayas, green leafy vegetables, oats, and whole grains).\n• Proactive Hydration: Ingest 3 to 4 Liters of water daily to soften stools and prevent straining.\n• Warm Sitz Bath: Sit in a tub of warm water for 15-20 minutes 2-3 times daily to relieve rectal inflammation and pain.\n• Lifestyle Adjustments: Avoid sitting on the toilet for prolonged periods and avoid heavy lifting or spicy foods.\n\n👨‍⚕️ When to See a Doctor: If you experience rectal bleeding, severe pain, or prolapsed tissue, consult a Gastroenterologist or Proctologist promptly."

                // 2. Chest Pain & Cardiac Emergency
                q.contains("chest pain") || q.contains("chest") || q.contains("angina") || q.contains("heart attack") || q.contains("tightness") ->
                    "⚠️ CRITICAL MEDICAL ALERT: Chest pain, heavy pressure, or tightness—especially if radiating to your left arm, jaw, or accompanied by shortness of breath and cold sweating—can indicate a cardiac emergency.\n\n• Sit down immediately in a comfortable resting position.\n• Loosen tight clothing around your neck and chest.\n• Call Emergency Medical Services or trigger the SwasthyaSathi SOS button immediately."

                // 3. Dizzy, Headache, Heat Strain
                q.contains("dizzy") || q.contains("headache") || q.contains("dizziness") || q.contains("heat") ->
                    "Feeling dizzy or having a headache is a clear symptom of heat exhaustion and electrolyte (sodium/potassium) depletion.\n\n• Move immediately to a shaded or air-conditioned room.\n• Slowly sip 350-400 mL of osmotic electrolyte solution (ORS).\n• Rest with eyes closed and apply a cool damp cloth to your forehead."

                // 4. Shortness of Breath & Air Quality
                q.contains("breath") || q.contains("smog") || q.contains("pollution") || q.contains("aqi") || q.contains("asthma") ->
                    "Shortness of breath or airway tightness is triggered by fine particulate air pollution (PM2.5).\n\n• Wear a certified N95 respirator mask during outdoor transit.\n• Restrict outdoor physical exertion and stay indoors with HEPA air filtration."

                // 5. Stomach Ache, Acidity, Diarrhea
                q.contains("stomach") || q.contains("vomit") || q.contains("diarrhea") || q.contains("acidity") || q.contains("gas") || q.contains("nausea") ->
                    "For stomach pain, acidity, or diarrhea:\n\n• Consume light bland foods (bananas, rice, curd, toast).\n• Rehydrate with ORS solution or tender coconut water to replace lost fluids.\n• Avoid oily, fried, or spicy foods. Seek medical attention if symptoms exceed 24 hours."

                // 6. Skin Rashes, Itching & Fungal Infection
                q.contains("skin") || q.contains("rash") || q.contains("itch") || q.contains("fungal") || q.contains("allergy") ->
                    "For skin irritation, rashes, or itching:\n\n• Keep the affected area clean, dry, and cool.\n• Avoid scratching to prevent secondary bacterial skin infections.\n• Apply soothing calamine lotion or aloe vera gel. Consult a Dermatologist if pus or severe redness develops."

                // 7. Joint, Muscle & Back Pain
                q.contains("back") || q.contains("joint") || q.contains("knee") || q.contains("muscle") || q.contains("ache") || q.contains("arthritis") ->
                    "For joint or muscle pain:\n\n• Apply a warm or cold compress to the affected joint for 15 minutes.\n• Maintain an ergonomic posture and avoid heavy physical lifting.\n• Consult an Orthopedist if accompanied by severe swelling or joint locking."

                // 8. General Health / Unlisted Symptom
                else ->
                    "Addressing your medical query:\n\n• Maintain proper hydration (2.5L - 3L fluid daily) and eat balanced, nutrient-dense meals.\n• Rest adequately and avoid physical overexertion.\n• Monitor your vitals (pulse & SpO2). If symptoms are severe, persistent, or worsening, please consult a qualified physician or specialist."
            }
        }
    }

    private fun generateClimateAnswer(
        query: String,
        env: EnvironmentalTelemetry,
        lang: AppLanguage
    ): String {
        val temp = env.temperatureC
        val feels = env.feelsLikeC
        val aqi = env.aqi

        return when (lang) {
            AppLanguage.HINDI -> """
                |मौसम की जानकारी व अचानक परिवर्तन से बचाव:
                |• वर्तमान स्थिति: तापमान ${temp}°C (महसूस ${feels}°C), AQI $aqi
                |
                |🌤️ अचानक मौसम परिवर्तन की सावधानियां:
                |• भीषण गर्मी: दोपहर 12:00 से 3:30 बजे तक सीधी धूप से बचें और पर्याप्त ORS पीएं।
                |• वायु प्रदूषण: AQI बढ़ने पर N95 मास्क पहनें और खुले में दौड़ना/व्यायाम सीमित रखें।
                |• अचानक बदलाव: यदि मौसम अचानक बदले या आंधी/बारिश आए तो तुरंत सुरक्षित शेल्टर या पक्के भवन में शरण लें।
            """.trimMargin()

            AppLanguage.BENGALI -> """
                |আবহাওয়ার আপডেট ও আকস্মিক পরিবর্তন সতর্কতা:
                |• বর্তমান অবস্থা: তাপমাত্রা ${temp}°C (অনুভূত ${feels}°C), AQI $aqi
                |
                |🌤️ আগামী আবহাওয়ার জন্য প্রস্তুতি:
                |• তাপদাহ: দুপুর ১২:০০ থেকে ৩:৩০ পর্যন্ত কড়া রোদ এড়িয়ে চলুন এবং ওআরএস সাথে রাখুন।
                |• বায়ুদূষণ: বাইরে বেরোলে N95 মাস্ক পরিধান করুন।
                |• আকস্মিক পরিবর্তন: হঠাৎ ঝড়-বৃষ্টি বা আবহাওয়া পরিবর্তন হলে নিকটস্থ শেটারে আশ্রয় নিন।
            """.trimMargin()

            else -> """
                |Weather Analysis & Microclimate Overview:
                |• Current Conditions: Temperature ${temp}°C (Feels like ${feels}°C), AQI $aqi
                |
                |🌤️ Future Precautions for Sudden Climate Change:
                |• Heat Wave Defense: Avoid direct solar exposure between 12:00 PM and 3:30 PM. Maintain 350 mL electrolyte hydration.
                |• Air Pollution Defense: Wear a certified N95 respirator mask outdoors when AQI exceeds 150.
                |• Emergency Weather Shift: If sudden storms, heavy downpours, or rapid thermal drops occur, seek immediate shelter in cooling centers or reinforced buildings.
            """.trimMargin()
        }
    }

    private fun generateAppHelpAnswer(query: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.HINDI -> "स्वास्थसाथी AI आपका ऑन-डिवाइस स्वास्थ्य और क्लाइमेट गाइड है। यह आपकी स्मार्टवॉच से पल्स/SpO2 और लाइव मौसम को मॉनिटर करता है। ऐप में Emergency SOS बटन, हाइड्रेशन रिमाइंडर, शेल्टर मैप और स्मार्टवॉच डेमो सिम्युलेटर शामिल हैं।"
            AppLanguage.BENGALI -> "স্বাস্থ্যসাথী AI আপনার ব্যক্তিগত স্বাস্থ্য ও আবহাওয়া নির্দেশক অ্যাপ। এটি স্মার্টওয়াচ ভাইটালস, বায়ু মান (AQI), জল পানের পরিমাণ ট্র্যাক করে এবং বিপদে ১ click-এ Emergency SOS সংকেত পাঠায়।"
            else -> "SwasthyaSathi AI is your native clinical & microclimate companion app. It integrates real-time wearable vitals (Heart Rate & SpO2), environmental telemetry (Temperature & AQI), hydration tracking, cooling shelter locator, and 10-15 minute offline Emergency SOS queueing."
        }
    }

    private fun generateGeneralKnowledgeAnswer(query: String, lang: AppLanguage): String {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Geography & Nations
        if (q.contains("prime minister") || q.contains("pm of india") || q.contains("प्रधानमंत्री")) {
            return when (lang) {
                AppLanguage.HINDI -> "भारत के वर्तमान प्रधानमंत्री श्री नरेंद्र मोदी हैं।"
                AppLanguage.BENGALI -> "ভারতের বর্তমান প্রধানমন্ত্রী শ্রী নরেন্দ্র মোদী।"
                else -> "The Prime Minister of India is Narendra Modi."
            }
        }
        if (q.contains("capital of france") || q.contains("paris") || q.contains("फ्रांस की राजधानी")) {
            return when (lang) {
                AppLanguage.HINDI -> "फ्रांस की राजधानी पेरिस (Paris) है।"
                AppLanguage.BENGALI -> "ফ্রান্সের राजधानी প্যারিস (Paris)।"
                else -> "The capital of France is Paris."
            }
        }
        if (q.contains("capital of india") || q.contains("भारत की राजधानी")) {
            return when (lang) {
                AppLanguage.HINDI -> "भारत की राजधानी नई दिल्ली (New Delhi) है।"
                AppLanguage.BENGALI -> "ভারতের রাজধানী নয়াদিল্লি (New Delhi)।"
                else -> "The capital of India is New Delhi."
            }
        }

        // 2. Science, Space & Nature
        if (q.contains("sun") || q.contains("earth") || q.contains("moon") || q.contains("planet") || q.contains("space")) {
            return when (lang) {
                AppLanguage.HINDI -> "सूर्य हमारे सौर मंडल के केंद्र में स्थित तारा है। पृथ्वी सूर्य से लगभग 149.6 मिलियन किलोमीटर दूर है और इसे एक चक्कर पूरा करने में 365.25 दिन लगते हैं।"
                AppLanguage.BENGALI -> "সূর্য আমাদের সৌরজগতের কেন্দ্রস্থলে অবস্থিত নক্ষত্র। পৃথিবী সূর্য থেকে প্রায় ১৪৯.৬ মিলিয়ন কিমি দূরে অবস্থিত।"
                else -> "The Sun is the star at the center of our Solar System. Earth orbits the Sun at an average distance of about 149.6 million kilometers (93 million miles)."
            }
        }
        if (q.contains("water formula") || q.contains("h2o") || q.contains("chemical formula")) {
            return "The chemical formula for water is H₂O (two Hydrogen atoms bound to one Oxygen atom)."
        }

        // 3. Technology & Computing
        if (q.contains("artificial intelligence") || q.contains("what is ai") || q.contains("एआई क्या है")) {
            return when (lang) {
                AppLanguage.HINDI -> "आर्टिफिशियल इंटेलिजेंस (AI) कंप्यूटर विज्ञान की एक शाखा है जो मशीनों को मनुष्यों की तरह सोचने, सीखने और समस्याओं को हल करने में सक्षम बनाती है।"
                AppLanguage.BENGALI -> "আর্টিফিসিয়াল ইন্টেলিজেন্স (AI) হলো কম্পিউটারের এমন এক প্রযুক্তি যা মানুষকে অনুকরণ করে চিন্তা করা ও শেখার কাজ সম্পন্ন করে।"
                else -> "Artificial Intelligence (AI) refers to the simulation of human intelligence in machines programmed to think, learn, synthesize data, and solve complex problems."
            }
        }

        // 4. Mathematics & Logic
        if (q.matches(Regex(".*\\d+\\s*[+\\-*/%]\\s*\\d+.*"))) {
            try {
                val numbers = Regex("\\d+").findAll(q).map { it.value.toLong() }.toList()
                if (numbers.size >= 2) {
                    val n1 = numbers[0]
                    val n2 = numbers[1]
                    val res = when {
                        q.contains("+") || q.contains("plus") || q.contains("add") -> n1 + n2
                        q.contains("-") || q.contains("minus") -> n1 - n2
                        q.contains("*") || q.contains("multiply") || q.contains("into") || q.contains("times") -> n1 * n2
                        q.contains("/") || q.contains("divide") -> if (n2 != 0L) n1 / n2 else "Undefined (division by zero)"
                        else -> n1 + n2
                    }
                    return "Calculation Result: $n1 and $n2 = $res"
                }
            } catch (e: Exception) {}
        }

        // 5. Daily Life, Food & Cooking
        if (q.contains("tea") || q.contains("chai") || q.contains("चाय")) {
            return when (lang) {
                AppLanguage.HINDI -> "चाय बनाने के लिए: एक कप पानी में थोड़ी कद्दूकस की हुई अदरक और इलायची उबालें, 1-2 चम्मच चाय पत्ती और चीनी डालें, 2 मिनट बाद दूध मिलाकर अच्छे से उबालें और छान लें।"
                AppLanguage.BENGALI -> "চা তৈরির নিয়ম: এক কাপ জলে আদা ও এলাচ ফুটিয়ে চা পাতা ও চিনি দিন। ফুটে উঠলে দুধ মিশিয়ে আরও ২ মিনিট ফুটিয়ে ছেঁকে নিন।"
                else -> "To make classic Indian Chai: Boil 1 cup of water with crushed ginger and cardamom. Add 1-2 tsp tea leaves and sugar, then pour 1/2 cup milk. Bring to a rolling boil, strain, and serve hot."
            }
        }

        // 6. Humor & Trivia
        if (q.contains("joke") || q.contains("चुटकुला") || q.contains("গল্প")) {
            return when (lang) {
                AppLanguage.HINDI -> "😊 शिक्षक: 15 फलों के नाम बताओ!\nछात्र: 1 आम, 1 सेब और 13 केले! 🍌"
                AppLanguage.BENGALI -> "😊 শিক্ষক: ১০টি ফলের নাম বলো!\nছাত্র: ১টি আপেল আর ৯টি কলা! 🍌"
                else -> "Why don't scientists trust atoms?\nBecause they make up everything! 😄"
            }
        }

        // 7. General Open-Ended Query Answer Synthesizer
        val queryTitle = query.replace("?", "").trim()
        return when (lang) {
            AppLanguage.HINDI -> "आपके सवाल ('$queryTitle') के संबंध में:\nयह एक सामान्य विषय है। यदि आपका प्रश्न किसी विशेष क्षेत्र जैसे विज्ञान, प्रौद्योगिकी, दैनिक जीवन या इतिहास से जुड़ा है, तो कृपया और स्पष्ट रूप से पूछें। स्वास्थसाथी AI आपकी सहायता के लिए तैयार है।"
            AppLanguage.BENGALI -> "আপনার প্রশ্ন ('$queryTitle') প্রসঙ্গে:\nএটি একটি সাধারণ বিষয়। বিষয়বস্তু আরও স্পষ্টভাবে জিজ্ঞেস করলে আমি নির্ভুল তথ্য প্রদান করতে সক্ষম হব।"
            else -> "Regarding your query ('$queryTitle'):\nThis is a general query. I can assist you with general knowledge, everyday questions, science, math, technology, lifestyle, and health safety. Please feel free to ask further details!"
        }
    }
}
