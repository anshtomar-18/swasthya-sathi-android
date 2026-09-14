package com.swasthyasathi.app.data.model

data class UserProfile(
    val firstName: String = "Aarav",
    val lastName: String = "Sharma",
    val exactAge: Int = 28,
    val ageGroup: String = "18-40", // under18, 18-40, 41-60, 60+
    val gender: String = "Male", // Male, Female, Other
    val abhaId: String = "91-4521-8832-1092",
    val location: String = "New Delhi, Delhi NCR",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val outdoorHours: Float = 6.0f, // Daily outdoor work / exposure hours
    val outdoorActivityLevel: String = "high", // low, moderate, high
    val workEnvironment: String = "Outdoor Field / Direct Sun",
    val shiftTimings: String = "Morning to Afternoon (09:00 - 17:00)",
    val diseases: List<String> = listOf("Heat Intolerance"), // Asthma / COPD, Cardiovascular / Hypertension, Type-2 Diabetes, Heat Intolerance, Kidney / Renal Disorder, Chronic Allergies
    val sensitivities: List<String> = listOf("heat"), // internal clinical keys: heat, respiratory, cardiovascular, renal, metabolic, none
    val emergencyContactName: String = "Rajesh Sharma",
    val emergencyContactPhone: String = "+91 98765 43210",
    val emergencyRelation: String = "Spouse / Family",
    val offlineSmsMesh: Boolean = true
)

data class DemoPreset(
    val id: String,
    val label: String,
    val profile: UserProfile
)

val SIH_DEMO_PRESETS = listOf(
    DemoPreset(
        id = "worker",
        label = "Outdoor Laborer (Heat Sensitive)",
        profile = UserProfile(
            firstName = "Ramesh",
            lastName = "Kumar",
            exactAge = 34,
            ageGroup = "18-40",
            gender = "Male",
            abhaId = "91-8823-1102-4491",
            outdoorHours = 8.5f,
            outdoorActivityLevel = "high",
            workEnvironment = "Construction / Heavy Physical",
            shiftTimings = "Full Day Peak Solar (08:30 - 17:30)",
            diseases = listOf("Heat Intolerance"),
            sensitivities = listOf("heat"),
            location = "New Delhi, Delhi NCR",
            latitude = 28.6139,
            longitude = 77.2090,
            emergencyContactName = "Anita Sharma",
            emergencyContactPhone = "+91 98111 22334",
            emergencyRelation = "Spouse"
        )
    ),
    DemoPreset(
        id = "senior",
        label = "Senior Citizen (Cardio & Heat)",
        profile = UserProfile(
            firstName = "Devendra",
            lastName = "Verma",
            exactAge = 67,
            ageGroup = "60+",
            gender = "Male",
            abhaId = "91-1192-3344-7788",
            outdoorHours = 2.0f,
            outdoorActivityLevel = "low",
            workEnvironment = "Indoor / Occasional Transit",
            shiftTimings = "Morning Walking Hours (06:30 - 08:30)",
            diseases = listOf("Cardiovascular / Hypertension", "Heat Intolerance"),
            sensitivities = listOf("cardiovascular", "heat"),
            location = "Jaipur, Rajasthan",
            latitude = 26.9124,
            longitude = 75.7873,
            emergencyContactName = "Sanjay Verma",
            emergencyContactPhone = "+91 98222 33445",
            emergencyRelation = "Son"
        )
    ),
    DemoPreset(
        id = "asthma",
        label = "Asthma / Respiratory Sensitive",
        profile = UserProfile(
            firstName = "Pooja",
            lastName = "Sen",
            exactAge = 26,
            ageGroup = "18-40",
            gender = "Female",
            abhaId = "91-3321-9988-4421",
            outdoorHours = 4.5f,
            outdoorActivityLevel = "moderate",
            workEnvironment = "Urban Commute / Street Transit",
            shiftTimings = "Standard Shift (10:00 - 18:00)",
            diseases = listOf("Asthma / COPD", "Chronic Allergies"),
            sensitivities = listOf("respiratory"),
            location = "Kolkata, West Bengal",
            latitude = 22.5726,
            longitude = 88.3639,
            emergencyContactName = "Meera Sen",
            emergencyContactPhone = "+91 98333 44556",
            emergencyRelation = "Sister"
        )
    ),
    DemoPreset(
        id = "student",
        label = "Baseline Student",
        profile = UserProfile(
            firstName = "Kabir",
            lastName = "Rao",
            exactAge = 20,
            ageGroup = "18-40",
            gender = "Male",
            abhaId = "91-7789-2233-1100",
            outdoorHours = 3.0f,
            outdoorActivityLevel = "moderate",
            workEnvironment = "Campus / Mixed Transit",
            shiftTimings = "College Hours (09:00 - 16:00)",
            diseases = listOf("None"),
            sensitivities = listOf("none"),
            location = "Bengaluru, Karnataka",
            latitude = 12.9716,
            longitude = 77.5946,
            emergencyContactName = "Dr. Sunita Rao",
            emergencyContactPhone = "+91 98444 55667",
            emergencyRelation = "Mother"
        )
    )
)
