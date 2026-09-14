package com.swasthyasathi.app.data.model

data class CoolingShelter(
    val id: String,
    val name: String,
    val category: String, // "cooling", "water", "clinic"
    val distanceMeters: Int,
    val address: String,
    val tempC: Float,
    val status: String,
    val amenities: List<String>,
    val verifiedTime: String,
    val doctorOnSite: String? = null,
    val relX: Float = 0.5f, // Map coordinate offset 0.0f to 1.0f
    val relY: Float = 0.5f,
    val canopyPct: Int = 82,
    val shadeReductionC: Float = 5.2f,
    val lat: Double = 28.6328,
    val lon: Double = 77.2197
)

data class HydrationLogEntry(
    val id: Long = System.currentTimeMillis(),
    val type: String, // "water", "ors"
    val amountMl: Int,
    val timestamp: String
)

val DEFAULT_COOLING_SHELTERS = listOf(
    CoolingShelter(
        id = "hub-04",
        name = "NDMC Climate Relief Hub 04",
        category = "cooling",
        distanceMeters = 320,
        address = "Connaught Place Block-B Inner Circle",
        tempC = 25f,
        status = "OPEN",
        amenities = listOf("8 Free Recliners", "Cold Misting Fans", "Free ORS Packets", "Phone Charge Point"),
        verifiedTime = "2 mins ago",
        relX = 0.72f,
        relY = 0.28f,
        canopyPct = 84,
        shadeReductionC = 5.5f,
        lat = 28.6335,
        lon = 77.2210
    ),
    CoolingShelter(
        id = "piao-01",
        name = "Piao Jal Sewa Dispenser",
        category = "water",
        distanceMeters = 180,
        address = "Hanuman Mandir Outer Arcade",
        tempC = 14f,
        status = "ACTIVE",
        amenities = listOf("100% RO Purified (TDS 68)", "4 Food-Grade Steel Taps", "Free Bottle Refills", "Wheelchair Ramp"),
        verifiedTime = "Tested Pure 13:40",
        relX = 0.35f,
        relY = 0.38f,
        canopyPct = 88,
        shadeReductionC = 6.0f,
        lat = 28.6315,
        lon = 77.2160
    ),
    CoolingShelter(
        id = "clinic-01",
        name = "Janpath Mohalla Clinic",
        category = "clinic",
        distanceMeters = 550,
        address = "Janpath Lane near Metro Gate 2",
        tempC = 23f,
        status = "DR ON SITE",
        amenities = listOf("Central Air Conditioned Ward", "Heatstroke Saline IV Supplies", "Instant Vitals Check (BP/SpO2)", "Free Paracetamol"),
        verifiedTime = "Live On Site",
        doctorOnSite = "Dr. Kavita Sharma, MD",
        relX = 0.65f,
        relY = 0.76f,
        canopyPct = 78,
        shadeReductionC = 4.8f,
        lat = 28.6275,
        lon = 77.2185
    ),
    CoolingShelter(
        id = "hub-07",
        name = "Palika Shaded Transit Lounge",
        category = "cooling",
        distanceMeters = 680,
        address = "Palika Underground Metro Concourse",
        tempC = 24f,
        status = "OPEN",
        amenities = listOf("Continuous Shaded Canopy", "Industrial Misting Fans", "Chilled Electrolyte Station"),
        verifiedTime = "10 mins ago",
        relX = 0.22f,
        relY = 0.68f,
        canopyPct = 90,
        shadeReductionC = 6.5f,
        lat = 28.6295,
        lon = 77.2140
    )
)
