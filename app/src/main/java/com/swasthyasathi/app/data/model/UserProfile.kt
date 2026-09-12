package com.swasthyasathi.app.data.model

data class UserProfile(
    val firstName: String = "Aarav",
    val ageGroup: String = "18-40", // under18, 18-40, 41-60, 60+
    val location: String = "New Delhi, Delhi NCR",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val outdoorActivityLevel: String = "high", // low, moderate, high
    val sensitivities: List<String> = listOf("heat"), // respiratory, cardiovascular, heat, none
    val emergencyContactName: String = "Rajesh Sharma",
    val emergencyContactPhone: String = "+91 98765 43210"
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
            ageGroup = "18-40",
            outdoorActivityLevel = "high",
            sensitivities = listOf("heat"),
            location = "New Delhi, Delhi NCR",
            latitude = 28.6139,
            longitude = 77.2090,
            emergencyContactName = "Anita Sharma",
            emergencyContactPhone = "+91 98111 22334"
        )
    ),
    DemoPreset(
        id = "senior",
        label = "Senior Citizen (Cardio & Heat)",
        profile = UserProfile(
            firstName = "Devendra",
            ageGroup = "60+",
            outdoorActivityLevel = "low",
            sensitivities = listOf("cardiovascular", "heat"),
            location = "Jaipur, Rajasthan",
            latitude = 26.9124,
            longitude = 75.7873,
            emergencyContactName = "Sanjay Verma",
            emergencyContactPhone = "+91 98222 33445"
        )
    ),
    DemoPreset(
        id = "asthma",
        label = "Asthma / Respiratory Sensitive",
        profile = UserProfile(
            firstName = "Pooja",
            ageGroup = "18-40",
            outdoorActivityLevel = "moderate",
            sensitivities = listOf("respiratory"),
            location = "Kolkata, West Bengal",
            latitude = 22.5726,
            longitude = 88.3639,
            emergencyContactName = "Meera Sen",
            emergencyContactPhone = "+91 98333 44556"
        )
    ),
    DemoPreset(
        id = "student",
        label = "Baseline Student",
        profile = UserProfile(
            firstName = "Kabir",
            ageGroup = "under18",
            outdoorActivityLevel = "moderate",
            sensitivities = listOf("none"),
            location = "Bengaluru, Karnataka",
            latitude = 12.9716,
            longitude = 77.5946,
            emergencyContactName = "Dr. Sunita Rao",
            emergencyContactPhone = "+91 98444 55667"
        )
    )
)
