package com.example.appproject.data.remote

import android.content.Context
import android.util.Log
import com.example.appproject.R
import com.example.appproject.data.model.ItineraryDay
import com.example.appproject.data.model.ItineraryStep
import com.example.appproject.data.model.PricingTier
import com.example.appproject.data.model.TARGET_PRICE_CONFIGS
import com.example.appproject.data.model.TripPromo
import com.example.appproject.data.model.safeAnnotatedStringHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

const val PROMO_CACHE_PREFS = "promo_trips_cache_prefs"
const val KEY_CACHED_TRIPS = "cached_trips_json"
const val KEY_LAST_SYNC_TIME = "last_synced_time"

fun resolveTripImageRes(tripName: String, fallbackId: Int = R.drawable.background_dashboard): Int {
    val lower = tripName.lowercase(Locale.ROOT)
    return when {
        lower.contains("bromo midnight") -> R.drawable.bromo_midnight_trip
        lower.contains("bromo sunrise") -> R.drawable.bromo_sunrises
        lower.contains("tumpak") || lower.contains("sewu") -> R.drawable.tumpak_sewu
        lower.contains("ijen") -> R.drawable.ijen_crater_explore
        lower.contains("yogyakarta") -> R.drawable.borobudur_temple_explore
        lower.contains("rinjani") -> R.drawable.mount_rinjani
        lower.contains("lembang") || lower.contains("bandung") -> R.drawable.bandung
        lower.contains("golf") || lower.contains("bogor") -> R.drawable.bogor_golf
        lower.contains("malang") || lower.contains("batu") -> R.drawable.malang
        lower.contains("jogja") -> R.drawable.jogja
        else -> if (fallbackId > 0 && fallbackId != R.drawable.background_dashboard) fallbackId else R.drawable.background_dashboard
    }
}

fun savePromoTripsToCache(context: Context, trips: List<TripPromo>) {
    try {
        val prefs = context.getSharedPreferences(PROMO_CACHE_PREFS, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (trip in trips) {
            val obj = JSONObject().apply {
                put("name", trip.name)
                put("description", trip.description)
                put("price", trip.price)
                put("imageRes", trip.imageRes)
                put("subtext", trip.subtext)
                put("badge", trip.badge)
                put("duration", trip.duration)
                put("transport", trip.transport)
                put("hotel", trip.hotel)

                val effectiveItin = trip.itineraries ?: getDefaultItinerariesForTrip(trip.name)
                val itinArray = JSONArray()
                if (effectiveItin != null) {
                    for (day in effectiveItin) {
                        val dayObj = JSONObject().apply {
                            put("dayTitle", day.dayTitle)
                            val stepArray = JSONArray()
                            for (step in day.steps) {
                                val stepObj = JSONObject().apply {
                                    put("time", step.time)
                                    put("activity", step.activity)
                                }
                                stepArray.put(stepObj)
                            }
                            put("steps", stepArray)
                        }
                        itinArray.put(dayObj)
                    }
                }
                put("itineraries", itinArray)

                val tierArray = JSONArray()
                for (tier in trip.pricingTiers) {
                    val tObj = JSONObject().apply {
                        put("paxCount", tier.paxCount)
                        put("paxLabel", tier.paxLabel)
                        put("pricePerPaxInt", tier.pricePerPaxInt)
                        put("priceFormatted", tier.priceFormatted)
                    }
                    tierArray.put(tObj)
                }
                put("pricingTiers", tierArray)

                val incArray = JSONArray()
                for (inc in trip.inclusions) {
                    incArray.put(inc)
                }
                put("inclusions", incArray)

                val excArray = JSONArray()
                for (exc in trip.exclusions) {
                    excArray.put(exc)
                }
                put("exclusions", excArray)

                val datesArray = JSONArray()
                for (date in trip.availableDates) {
                    datesArray.put(date)
                }
                put("availableDates", datesArray)
            }
            jsonArray.put(obj)
        }
        val currentTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
        prefs.edit()
            .putString(KEY_CACHED_TRIPS, jsonArray.toString())
            .putString(KEY_LAST_SYNC_TIME, currentTime)
            .apply()
    } catch (e: Exception) {
        Log.e("PromoSync", "Failed to save trips to cache", e)
    }
}

fun getDefaultItinerariesForTrip(tripName: String, context: Context? = null): List<ItineraryDay>? {
    val lower = tripName.lowercase(Locale.ROOT)
    val matched = getDefaultWebsitePromoTrips(context).firstOrNull { defaultTrip ->
        val defLower = defaultTrip.name.lowercase(Locale.ROOT)
        when {
            lower.contains("bromo midnight") && (defLower.contains("bromo midnight") || defLower.contains("bromo")) -> true
            lower.contains("bromo sunrise") && (defLower.contains("bromo sunrise") || defLower.contains("sunrise")) -> true
            (lower.contains("tumpak") || lower.contains("sewu")) && (defLower.contains("tumpak") || defLower.contains("sewu")) -> true
            lower.contains("ijen") && defLower.contains("ijen") -> true
            (lower.contains("dieng") || lower.contains("jogja dieng")) && (defLower.contains("dieng") || defLower.contains("jogja")) -> true
            lower.contains("rinjani") && defLower.contains("rinjani") -> true
            (lower.contains("yogyakarta") || lower.contains("jogja")) && (defLower.contains("yogyakarta") || defLower.contains("jogja")) -> true
            else -> defLower == lower
        }
    }
    return matched?.itineraries
}

fun getDefaultWebsitePromoTrips(context: Context? = null): List<TripPromo> {
    fun getString(resId: Int, defaultStr: String): String {
        return context?.getString(resId) ?: defaultStr
    }

    return listOf(
        TripPromo(
            name = getString(R.string.pkg_bromo_sunrise, "Bromo Sunrise Package"),
            description = getString(
                R.string.pkg_bromo_sunrise_desc,
                "2 Days 1 Night – Complete Private Trip"
            ),
            price = "",
            imageRes = R.drawable.bromo_sunrises,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_bromo_sunrise_subtext,
                    "PT Zuhanto Travel Indonesia offers a private journey to Mount Bromo, featuring its iconic sunrise, vast sand sea, and breathtaking mountain scenery. Designed for comfort, flexibility, and private groups."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = getString(R.string.duration_2d1n, "2 Days 1 Night"),
            transport = "Innova / HiAce",
            participant = getString(R.string.pax_2_5_6_10, "2 - 5 person / 6-10 person"),
            hotel = getString(R.string.hotel_based_on_package, "Based on package or request"),
            itineraries = listOf(
                ItineraryDay(
                    getString(
                        R.string.itin_bromo_day1_title,
                        "DAY 1: Departure & Transfer to Bromo"
                    ),
                    listOf(
                        ItineraryStep(
                            "21.00",
                            getString(
                                R.string.itin_bromo_day1_s1,
                                "Meeting Point at Surabaya / Malang"
                            )
                        ),
                        ItineraryStep(
                            "21.30",
                            getString(
                                R.string.itin_bromo_day1_s2,
                                "Briefing & Overnight Drive to Bromo Area"
                            )
                        )
                    )
                ),
                ItineraryDay(
                    getString(R.string.itin_bromo_day2_title, "DAY 2: Sunrise Bromo & Crater Tour"),
                    listOf(
                        ItineraryStep(
                            "03.00",
                            getString(
                                R.string.itin_bromo_day2_s1,
                                "Jeep 4x4 Transfer to Penanjakan / Kingkong Hill"
                            )
                        ),
                        ItineraryStep(
                            "05.00",
                            getString(R.string.itin_bromo_day2_s2, "Witness Magical Bromo Sunrise")
                        ),
                        ItineraryStep(
                            "06.30",
                            getString(
                                R.string.itin_bromo_day2_s3,
                                "Explore Bromo Crater, Pasir Berbisik & Widodaren Hill"
                            )
                        ),
                        ItineraryStep(
                            "09.00",
                            getString(R.string.itin_bromo_day2_s4, "Breakfast at Local Restaurant")
                        ),
                        ItineraryStep(
                            "12.00",
                            getString(
                                R.string.itin_bromo_day2_s5,
                                "Return Transfer to Surabaya / Malang"
                            )
                        )
                    )
                )
            ),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_tumpak_sewu_title, "Tumpak Sewu Package"),
            description = getString(
                R.string.pkg_tumpak_sewu_desc_full,
                "One Day Waterfall Adventure Trip"
            ),
            price = "",
            imageRes = R.drawable.tumpak_sewu,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_tumpak_sewu_subtext_full,
                    "Explore Indonesia's Niagara in a full day of adventure. Challenging trekking, epic views, and unforgettable experiences at Tumpak Sewu Waterfall."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = getString(R.string.duration_1d_19h, "1 Day (±19 Hours)"),
            transport = "TBA",
            participant = getString(R.string.participant_min_5, "Minimum 5 pax"),
            hotel = getString(R.string.hotel_without_daytrip, "Without Hotel (Day Trip)"),
            itineraries = listOf(
                ItineraryDay(
                    getString(
                        R.string.itin_tumpak_day1_title,
                        "DAY 1: One Day Waterfall Adventure"
                    ),
                    listOf(
                        ItineraryStep(
                            "01.00",
                            getString(
                                R.string.itin_tumpak_day1_s1,
                                "Drive from Surabaya to Lumajang"
                            )
                        ),
                        ItineraryStep(
                            "04.30",
                            getString(
                                R.string.itin_tumpak_day1_s2,
                                "Arrive at Tumpak Sewu & Sunrise Golden Hour"
                            )
                        ),
                        ItineraryStep(
                            "07.00",
                            getString(
                                R.string.itin_tumpak_day1_s3,
                                "Trekking Down to Bottom Waterfall"
                            )
                        ),
                        ItineraryStep(
                            "08.30",
                            getString(
                                R.string.itin_tumpak_day1_s4,
                                "Explore Tebing Nirwana & Telaga Biru"
                            )
                        ),
                        ItineraryStep(
                            "13.00",
                            getString(R.string.itin_tumpak_day1_s5, "Lunch & Return Trip")
                        )
                    )
                )
            ),
            pricingTiers = emptyList(),
            inclusions = listOf(
                getString(R.string.inc_transport_ac, "Transport AC"),
                getString(R.string.inc_driver_bbm, "Driver + BBM"),
                getString(R.string.inc_tickets, "Tickets"),
                getString(R.string.inc_local_guide, "Local Guide"),
                getString(R.string.inc_mineral_water, "Mineral Water"),
                getString(R.string.inc_lunch_pack_b, "Lunch (Pack B)"),
                getString(R.string.inc_documentation, "Documentation"),
                getString(R.string.inc_first_aid, "First Aid")
            ),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_ijen_bluefire, "Ijen Blue Fire Package"),
            description = getString(
                R.string.pkg_ijen_bluefire_desc_full,
                "2 Days 1 Night – Night Trekking & Crater Lake"
            ),
            price = "",
            imageRes = R.drawable.ijencrater,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_ijen_bluefire_subtext_full,
                    "PT Zuhanto Travel Indonesia offers a private journey to Ijen Crater, where you can witness the rare natural phenomenon of Blue Fire and enjoy breathtaking volcanic scenery."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = getString(R.string.duration_2d1n, "2 Days 1 Night"),
            transport = "Innova / HiAce",
            participant = getString(R.string.pax_2_5_6_10_persons, "2-5 persons / 6-10 persons"),
            hotel = getString(R.string.hotel_based_on_package, "Based on package or request"),
            itineraries = listOf(
                ItineraryDay(
                    getString(R.string.itin_ijen_day1_title, "DAY 1: Departure to Banyuwangi"),
                    listOf(
                        ItineraryStep(
                            "21.00",
                            getString(
                                R.string.itin_ijen_day1_s1,
                                "Pick-up at Surabaya / Malang / Banyuwangi"
                            )
                        ),
                        ItineraryStep(
                            "23.30",
                            getString(R.string.itin_ijen_day1_s2, "Drive to Paltuding Base Camp")
                        )
                    )
                ),
                ItineraryDay(
                    getString(
                        R.string.itin_ijen_day2_title,
                        "DAY 2: Blue Fire Trekking & Crater Lake"
                    ),
                    listOf(
                        ItineraryStep(
                            "01.00",
                            getString(
                                R.string.itin_ijen_day2_s1,
                                "Safety Briefing & Trekking Preparation"
                            )
                        ),
                        ItineraryStep(
                            "02.00",
                            getString(
                                R.string.itin_ijen_day2_s2,
                                "Ascent to Crater Rim & Witness Rare Blue Fire"
                            )
                        ),
                        ItineraryStep(
                            "06.00",
                            getString(
                                R.string.itin_ijen_day2_s3,
                                "Turquoise Acid Crater Lake View at Sunrise"
                            )
                        ),
                        ItineraryStep(
                            "09.00",
                            getString(
                                R.string.itin_ijen_day2_s4,
                                "Descend to Base Camp & Breakfast"
                            )
                        ),
                        ItineraryStep(
                            "10.00",
                            getString(R.string.itin_ijen_day2_s5, "Return Transfer")
                        )
                    )
                )
            ),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_jogja_dieng, "Jogja Dieng Package"),
            description = getString(
                R.string.pkg_jogja_dieng_desc_full,
                "2 Days 1 Night – Cultural & Nature Trip"
            ),
            price = "",
            imageRes = R.drawable.jogja,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_jogja_dieng_subtext_full,
                    "Enjoy a private journey from Jogja to Dieng, designed for comfort and flexibility. Ideal for couples or groups seeking a unique nature experience in Central Java."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = getString(R.string.duration_2d1n, "2 Days 1 Night"),
            transport = "Innova / HiAce",
            participant = getString(R.string.pax_2_5_6_10_persons, "2-5 persons / 6-10 persons"),
            hotel = getString(R.string.hotel_based_on_package, "Based on package or request"),
            itineraries = listOf(
                ItineraryDay(
                    getString(R.string.itin_jogja_day1_title, "DAY 1: Dieng Plateau Tour"),
                    listOf(
                        ItineraryStep(
                            "02.00",
                            getString(R.string.itin_jogja_day1_s1, "Pick up point Jogja / Dieng")
                        ),
                        ItineraryStep(
                            "03.00",
                            getString(
                                R.string.itin_jogja_day1_s2,
                                "Dieng Tour (Arjuna Temple & Sikidang Crater)"
                            )
                        ),
                        ItineraryStep(
                            "14.00",
                            getString(R.string.itin_jogja_day1_s3, "Finish Tour & Check-in Hotel")
                        )
                    )
                ),
                ItineraryDay(
                    getString(R.string.itin_jogja_day2_title, "DAY 2: Cultural Tour & Shopping"),
                    listOf(
                        ItineraryStep(
                            "08.00",
                            getString(R.string.itin_jogja_day2_s1, "Heritage Temple Exploration")
                        ),
                        ItineraryStep(
                            "13.00",
                            getString(R.string.itin_jogja_day2_s2, "Malioboro Souvenir Shopping")
                        ),
                        ItineraryStep(
                            "16.00",
                            getString(R.string.itin_jogja_day2_s3, "Transfer Out")
                        )
                    )
                )
            ),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_mount_rinjani_title, "Mount Rinjani Trekking"),
            description = getString(
                R.string.pkg_mount_rinjani_desc_full,
                "7D6N Ultimate Mountain Expedition"
            ),
            price = "",
            imageRes = R.drawable.mount_rinjani,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_mount_rinjani_subtext_full,
                    "Mount Rinjani (3,726m) is a complete ecosystem of adventure. This 7-day program is designed to give you the full summit experience without rushing."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = getString(R.string.duration_one_day, "1 Day"),
            participant = getString(R.string.participant_no_limit, "No Limit"),
            transport = "TBA",
            hotel = getString(R.string.hotel_without_camping, "Without Hotel (Camping)"),
            itineraries = listOf(
                ItineraryDay(
                    getString(
                        R.string.itin_rinjani_day1_title,
                        "Day 01: Arrival Lombok – Sembalun Village"
                    ),
                    listOf(
                        ItineraryStep(
                            "12.00",
                            getString(R.string.itin_rinjani_day1_s1, "Airport Pick-up")
                        ),
                        ItineraryStep(
                            "14.00",
                            getString(
                                R.string.itin_rinjani_day1_s2,
                                "Drive to Sembalun Village (1,156m)"
                            )
                        ),
                        ItineraryStep(
                            "17.00",
                            getString(
                                R.string.itin_rinjani_day1_s3,
                                "Check-in Homestay in Sembalun"
                            )
                        )
                    )
                ),
                ItineraryDay(
                    getString(
                        R.string.itin_rinjani_day2_title,
                        "Day 02: Sembalun Gate – Sembalun Crater Rim"
                    ),
                    listOf(
                        ItineraryStep(
                            "08.00",
                            getString(R.string.itin_rinjani_day2_s1, "National Park Registration")
                        ),
                        ItineraryStep(
                            "08.30",
                            getString(
                                R.string.itin_rinjani_day2_s2,
                                "Start Trekking from Sembalun Gate"
                            )
                        ),
                        ItineraryStep(
                            "17.00",
                            getString(
                                R.string.itin_rinjani_day2_s3,
                                "Arrive at Sembalun Crater Rim (2,639m Campsite)"
                            )
                        )
                    )
                ),
                ItineraryDay(
                    getString(
                        R.string.itin_rinjani_day3_title,
                        "Day 03: Summit Attack (3,726m) – Segara Anak Lake"
                    ),
                    listOf(
                        ItineraryStep(
                            "02.30",
                            getString(R.string.itin_rinjani_day3_s1, "Start Summit Attack")
                        ),
                        ItineraryStep(
                            "06.00",
                            getString(
                                R.string.itin_rinjani_day3_s2,
                                "Reach Summit of Mount Rinjani (3,726m) - Sunrise"
                            )
                        ),
                        ItineraryStep(
                            "13.00",
                            getString(
                                R.string.itin_rinjani_day3_s3,
                                "Segara Anak Lake & Hot Springs"
                            )
                        )
                    )
                ),
                ItineraryDay(
                    getString(
                        R.string.itin_rinjani_day4_title,
                        "Day 04: Segara Anak Lake – Senaru Crater Rim"
                    ),
                    listOf(
                        ItineraryStep(
                            "08.00",
                            getString(
                                R.string.itin_rinjani_day4_s1,
                                "Enjoy Segara Anak Lake & Fishing"
                            )
                        ),
                        ItineraryStep(
                            "13.00",
                            getString(
                                R.string.itin_rinjani_day4_s2,
                                "Trek Up to Senaru Crater Rim (2,641m)"
                            )
                        ),
                        ItineraryStep(
                            "16.00",
                            getString(
                                R.string.itin_rinjani_day4_s3,
                                "Campsite Check-in & Sunset View"
                            )
                        )
                    )
                ),
                ItineraryDay(
                    getString(
                        R.string.itin_rinjani_day5_title,
                        "Day 05: Senaru Crater Rim – Senaru Village – Senggigi"
                    ),
                    listOf(
                        ItineraryStep(
                            "07.00",
                            getString(R.string.itin_rinjani_day5_s1, "Descend to Senaru Village")
                        ),
                        ItineraryStep(
                            "14.30",
                            getString(
                                R.string.itin_rinjani_day5_s2,
                                "Transfer to Senggigi Beach Hotel"
                            )
                        ),
                        ItineraryStep(
                            "17.00",
                            getString(R.string.itin_rinjani_day5_s3, "Check-in Hotel in Senggigi")
                        )
                    )
                ),
                ItineraryDay(
                    getString(
                        R.string.itin_rinjani_day6_title,
                        "Day 06: Waterfall Tour & Relaxation"
                    ),
                    listOf(
                        ItineraryStep(
                            "10.30",
                            getString(R.string.itin_rinjani_day6_s1, "Visit Sendang Gile Waterfall")
                        ),
                        ItineraryStep(
                            "11.30",
                            getString(R.string.itin_rinjani_day6_s2, "Trek to Tiu Kelep Waterfall")
                        ),
                        ItineraryStep(
                            "15.00",
                            getString(R.string.itin_rinjani_day6_s3, "Return to Senggigi Hotel")
                        )
                    )
                ),
                ItineraryDay(
                    getString(R.string.itin_rinjani_day7_title, "Day 07: Departure"),
                    listOf(
                        ItineraryStep(
                            "TBA",
                            getString(
                                R.string.itin_rinjani_day7_s1,
                                "Check-out & Transfer to Lombok Airport"
                            )
                        )
                    )
                )
            ),
            pricingTiers = emptyList(),
            inclusions = listOf(
                getString(R.string.inc_airport_lombok, "Airport Pickup & Drop off (Lombok)"),
                getString(R.string.inc_transport_ac_full, "Transportation (AC)"),
                getString(
                    R.string.inc_accom_sembalun_senggigi,
                    "Accommodation at Sembalun & Senggigi"
                ),
                getString(R.string.inc_guide_porters, "Trekking Guide & Porters"),
                getString(R.string.inc_camping_equip, "Camping Equipment"),
                getString(R.string.inc_meals_water, "Meals & Mineral Water")
            ),
            exclusions = listOf(
                getString(R.string.exc_tips_guide, "Tips for Guide & Porter"),
                getString(R.string.exc_personal_gear, "Personal Gear"),
                getString(R.string.exc_extra_porter, "Extra Porter"),
                getString(R.string.exc_travel_insurance, "Travel Insurance"),
                getString(R.string.exc_flight_tickets, "Flight Tickets")
            )
        ),
        TripPromo(
            name = getString(R.string.pkg_yogyakarta_title, "Yogyakarta Trip"),
            description = getString(
                R.string.pkg_yogyakarta_desc_full,
                "2D1N Culture, Heritage & Merapi Adventure"
            ),
            price = "",
            imageRes = R.drawable.borobudur_temple_explore,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_yogyakarta_subtext_full,
                    "Explore Yogyakarta's rich culture, Malioboro street, and exciting Merapi Jeep adventure with complete facilities."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = getString(R.string.duration_2d1n, "2 Days 1 Night"),
            transport = getString(R.string.transport_medium_bus, "Medium Bus"),
            participant = getString(R.string.participant_min_30, "Minimum 30 pax"),
            hotel = "TBA",
            itineraries = listOf(
                ItineraryDay(
                    getString(
                        R.string.itin_yogya_day1_title,
                        "DAY 1: Surabaya - Yogyakarta Fun Trip"
                    ),
                    listOf(
                        ItineraryStep(
                            "05.00",
                            getString(R.string.itin_yogya_day1_s1, "Meeting Point (Surabaya)")
                        ),
                        ItineraryStep(
                            "05.30",
                            getString(R.string.itin_yogya_day1_s2, "Trip to Yogyakarta")
                        ),
                        ItineraryStep("11.00", getString(R.string.itin_yogya_day1_s3, "Lunch")),
                        ItineraryStep(
                            "13.00",
                            getString(R.string.itin_yogya_day1_s4, "Heha Sky View")
                        ),
                        ItineraryStep(
                            "17.00",
                            getString(R.string.itin_yogya_day1_s5, "Malioboro (Free Time)")
                        ),
                        ItineraryStep(
                            "19.00",
                            getString(R.string.itin_yogya_day1_s6, "Dinner & Check-in")
                        )
                    )
                ),
                ItineraryDay(
                    getString(R.string.itin_yogya_day2_title, "DAY 2: Merapi Adventure - Return"),
                    listOf(
                        ItineraryStep(
                            "07.00",
                            getString(R.string.itin_yogya_day2_s1, "Breakfast & Check-out")
                        ),
                        ItineraryStep(
                            "08.30",
                            getString(R.string.itin_yogya_day2_s2, "Lava Tour Merapi (Jeep 4x4)")
                        ),
                        ItineraryStep(
                            "11.00",
                            getString(R.string.itin_yogya_day2_s3, "Kopi Merapi")
                        ),
                        ItineraryStep("12.30", getString(R.string.itin_yogya_day2_s4, "Lunch")),
                        ItineraryStep(
                            "13.30",
                            getString(R.string.itin_yogya_day2_s5, "Souvenir Center")
                        ),
                        ItineraryStep(
                            "15.00",
                            getString(R.string.itin_yogya_day2_s6, "Return Trip")
                        ),
                        ItineraryStep(
                            "21.00",
                            getString(R.string.itin_yogya_day2_s7, "Arrive in Surabaya")
                        )
                    )
                )
            ),
            pricingTiers = emptyList(),
            inclusions = listOf(
                getString(R.string.inc_medium_bus, "Medium Bus"),
                getString(R.string.inc_drive_co, "Driver + Co"),
                getString(R.string.inc_bbm_toll, "BBM & Toll"),
                getString(R.string.inc_meals, "Meals"),
                getString(R.string.inc_snack_box, "Snack Box"),
                getString(R.string.inc_tour_guide, "Tour Guide"),
                getString(R.string.inc_documentation, "Documentation"),
                getString(R.string.inc_banner, "Banner"),
                getString(R.string.inc_fun_games, "Fun Games"),
                getString(R.string.inc_jeep_lava_tour, "Jeep Lava Tour"),
                getString(R.string.inc_tickets, "Tickets"),
                getString(R.string.inc_souvenir_shopping, "Souvenir Shopping")
            ),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_bromo_midnight, "Bromo Midnight Trip"),
            description = getString(
                R.string.pkg_bromo_midnight_desc,
                "Fast & Flexible Midnight Tour"
            ),
            price = "",
            imageRes = R.drawable.bromo_midnight_trip,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_bromo_midnight_subtext,
                    "Overnight journey to witness the magical Bromo sunrise without hotel stay."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = "",
            transport = "",
            participant = "",
            hotel = "",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_malang_batu, "Paket Wisata Malang - Batu"),
            description = getString(R.string.pkg_malang_batu_desc, "Family & Group City Tour"),
            price = "",
            imageRes = R.drawable.malang,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_malang_batu_subtext,
                    "Explore apple orchards, theme parks, and cool mountain weather in Malang and Batu."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = "",
            transport = "",
            participant = "",
            hotel = "",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_lembang_bandung, "Paket A Destinasi Lembang - Bandung"),
            description = getString(R.string.pkg_lembang_bandung_desc, "Nature & Volcano Tour"),
            price = "",
            imageRes = R.drawable.bandung,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_lembang_bandung_subtext,
                    "Discover floating markets, Tangkuban Perahu crater, and scenic tea plantations in Lembang Bandung."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = "",
            transport = "",
            participant = "",
            hotel = "",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.pkg_jakarta_bogor_golf, "Jakarta - Bogor Golf Package"),
            description = getString(
                R.string.pkg_jakarta_bogor_golf_desc,
                "Premium Golf Experience"
            ),
            price = "",
            imageRes = R.drawable.bogor_golf,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.pkg_jakarta_bogor_golf_subtext,
                    "Exclusive golfing days at top-rated golf courses in Jakarta and Bogor with full service."
                )
            ),
            badge = getString(R.string.promo_package, "PROMO PACKAGE"),
            duration = "",
            transport = "",
            participant = "",
            hotel = "",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.mount_bromo, "Mount Bromo"),
            description = getString(R.string.mount_bromo_iconic_sunrise, "Iconic Sunrise"),
            price = "",
            imageRes = R.drawable.mountbromo,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.mount_bromo_subtext_full,
                    "Witness the magical sunrise and whispering sands landscape with private jeep service. <br><br><b>*Prices may adjust based on number of participants and travel period.</b>"
                )
            ),
            badge = getString(R.string.custom_trip, "Custom Trip Package"),
            duration = "TBD",
            transport = "TBD",
            participant = getString(R.string.minimum_one, "Minimum 1 pax"),
            hotel = "TBD",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.borobudur_temple, "Borobudur Temple"),
            description = getString(R.string.borobudur_world_heritage, "World Heritage"),
            price = "",
            imageRes = R.drawable.borobudurtemple,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.borobudur_subtext_full,
                    "Explore the magnificence of the world's largest Buddhist temple and enjoy the special atmosphere of Yogyakarta. <br><br><b>*Prices may adjust based on number of participants and travel period.</b>"
                )
            ),
            badge = getString(R.string.custom_trip, "Custom Trip Package"),
            duration = "TBD",
            transport = "TBD",
            participant = getString(R.string.minimum_one, "Minimum 1 pax"),
            hotel = "TBD",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.ijen_crater, "Ijen Crater"),
            description = getString(R.string.ijen_eternal_blue_fire, "Eternal Blue Fire"),
            price = "",
            imageRes = R.drawable.ijencrater,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.ijen_subtext_full,
                    "Night adventure witnessing the rare blue fire phenomenon and stunning turquoise crater lake. <br><br><b>*Prices may adjust based on number of participants and travel period.</b>"
                )
            ),
            badge = getString(R.string.custom_trip, "Custom Trip Package"),
            duration = "TBD",
            transport = "TBD",
            participant = getString(R.string.minimum_one, "Minimum 1 pax"),
            hotel = "TBD",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.bali, "Bali"),
            description = getString(R.string.bali_island_of_gods, "Island of Gods"),
            price = "",
            imageRes = R.drawable.bali,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.bali_subtext_full,
                    "Private villas, beautiful beaches, and cultural richness in a relaxing private holiday package. <br><br><b>*Prices may adjust based on number of participants and travel period.</b>"
                )
            ),
            badge = getString(R.string.custom_trip, "Custom Trip Package"),
            duration = "TBD",
            transport = "TBD",
            participant = getString(R.string.minimum_one, "Minimum 1 pax"),
            hotel = "TBD",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.labuan_bajo, "Labuan Bajo"),
            description = getString(R.string.labuan_gateway_komodo, "Gateway to Komodo"),
            price = "",
            imageRes = R.drawable.labuan_bajo_explore,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.labuan_subtext_full,
                    "Private sailing, pink beaches, and encounters with prepaid dragons in their natural habitat. <br><br><b>*Prices may adjust based on number of participants and travel period.</b>"
                )
            ),
            badge = getString(R.string.custom_trip, "Custom Trip Package"),
            duration = "TBD",
            transport = "TBD",
            participant = getString(R.string.minimum_one, "Minimum 1 pax"),
            hotel = "TBD",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        ),
        TripPromo(
            name = getString(R.string.raja_ampat, "Raja Ampat"),
            description = getString(R.string.raja_eastern_paradise, "Eastern Paradise"),
            price = "",
            imageRes = R.drawable.raja_ampat_explore,
            subtext = safeAnnotatedStringHtml(
                getString(
                    R.string.raja_subtext_full,
                    "Stunning karst archipelagos and unrivaled underwater richness in Papua. <br><br><b>*Prices may adjust based on number of participants and travel period.</b>"
                )
            ),
            badge = getString(R.string.custom_trip, "Custom Trip Package"),
            duration = "TBD",
            transport = "TBD",
            participant = getString(R.string.minimum_one, "Minimum 1 pax"),
            hotel = "TBD",
            itineraries = emptyList(),
            pricingTiers = emptyList(),
            inclusions = emptyList(),
            exclusions = emptyList()
        )
    )
}

fun getScopedJsTextForPackage(jsContent: String, pkgPath: String): String {
    val routePattern = Pattern.compile("""path:"${Pattern.quote(pkgPath)}",element:o\.jsx\((\w+),""")
    val routeMatcher = routePattern.matcher(jsContent)
    val compName = if (routeMatcher.find()) routeMatcher.group(1) else null

    if (compName != null) {
        val compDecl = "$compName="
        val idx = jsContent.indexOf(compDecl)
        if (idx != -1) {
            val start = idx
            val end = (idx + 8000).coerceAtMost(jsContent.length)
            return jsContent.substring(start, end)
        }
    }
    return ""
}

fun extractPriceByTargetClass(
    targetUrl: String,
    doc: Document?,
    jsContent: String,
    context: Context? = null
): Pair<String, List<PricingTier>> {
    val getString: (Int, String) -> String = { id, fallback ->
        context?.getString(id) ?: fallback
    }

    val config = TARGET_PRICE_CONFIGS.entries.find { (url, cfg) ->
        targetUrl.equals(url, ignoreCase = true) || targetUrl.endsWith(cfg.urlPath, ignoreCase = true)
    }?.value

    val extractedPairs = mutableListOf<Pair<String, String>>()

    if (config != null) {
        if (doc != null) {
            val elems = try {
                val classSelector = if (config.classA.contains(" ")) "." + config.classA.replace(" ", ".") else "." + config.classA
                doc.select(classSelector)
            } catch (_: Exception) {
                emptyList()
            }.ifEmpty {
                doc.getElementsByAttributeValue("class", config.classA)
            }
            if (config.keyword != null) {
                for (elem in elems) {
                    val parentText = elem.parent()?.text() ?: ""
                    val ownText = elem.text()
                    if (parentText.contains(config.keyword, ignoreCase = true) || ownText.contains(config.keyword, ignoreCase = true)) {
                        extractedPairs.add(Pair(getString(R.string.price, "Price"), ownText.trim()))
                        break
                    }
                }
            } else {
                val elemsB = if (config.classB != null) doc.getElementsByAttributeValue("class", config.classB) else emptyList()
                if (elems.isNotEmpty() && elemsB.isNotEmpty()) {
                    for (i in 0 until minOf(elems.size, elemsB.size)) {
                        extractedPairs.add(Pair(elems[i].text().trim(), elemsB[i].text().trim()))
                    }
                } else if (elems.isNotEmpty() && config.classB == null) {
                    for (elem in elems) {
                        extractedPairs.add(Pair("Price", elem.text().trim()))
                    }
                }
            }
        }

        if (extractedPairs.isEmpty() && jsContent.isNotEmpty()) {
            if (config.keyword != null) {
                val kwRegex = Pattern.compile("""name:"[^"]*?${Pattern.quote(config.keyword)}[^"]*?"[^}]*?price:"([^"]+)"""", Pattern.CASE_INSENSITIVE)
                val matcher = kwRegex.matcher(jsContent)
                if (matcher.find()) {
                    matcher.group(1)?.let {
                        extractedPairs.add(Pair(getString(R.string.price, "Price"), it.replace("\\", "").trim()))
                    }
                }
            }
            if (extractedPairs.isEmpty()) {
                val scopedJs = getScopedJsTextForPackage(jsContent, config.urlPath)
                if (scopedJs.isNotEmpty()) {
                    if (config.classB != null) {
                        val regexA = Pattern.compile("""className:"${Pattern.quote(config.classA)}"[^}]*?children:(?:\["|")?([^"\}]+)""")
                        val regexB = Pattern.compile("""className:"${Pattern.quote(config.classB)}"[^}]*?children:(?:\["|")?([^"\}]+)""")

                        val matcherA = regexA.matcher(scopedJs)
                        val matcherB = regexB.matcher(scopedJs)

                        val listA = mutableListOf<String>()
                        val listB = mutableListOf<String>()

                        while (matcherA.find()) {
                            listA.add(matcherA.group(1)?.replace("\\", "")?.trim() ?: "")
                        }
                        while (matcherB.find()) {
                            listB.add(matcherB.group(1)?.replace("\\", "")?.trim() ?: "")
                        }

                        if (listA.isNotEmpty() && listB.isNotEmpty()) {
                            for (i in 0 until minOf(listA.size, listB.size)) {
                                extractedPairs.add(Pair(listA[i], listB[i]))
                            }
                        } else if (listA.isNotEmpty()) {
                            extractedPairs.add(Pair(getString(R.string.basic_package_A, "Package A"), listA[0]))
                            if (listB.isNotEmpty()) {
                                extractedPairs.add(Pair(getString(R.string.complete_package_B, "Package B"), listB[0]))
                            }
                        }
                    } else {
                        val regex = Pattern.compile("""className:"${Pattern.quote(config.classA)}"[^}]*?children:(?:\["|")?([^"\}]+)""")
                        val matcher = regex.matcher(scopedJs)
                        while (matcher.find()) {
                            matcher.group(1)?.let {
                                extractedPairs.add(Pair(getString(R.string.price, "Price"), it.replace("\\", "").trim()))
                            }
                        }
                    }
                }
            }
        }
    }

    if (extractedPairs.isEmpty()) {
        return Pair("IDR 0", emptyList())
    }

    if (config?.isPaxCategory == true) {
        val tiers = mutableListOf<PricingTier>()
        var overallPrice = ""

        for ((paxLabel, rawPrice) in extractedPairs) {
            val paxInt = paxLabel.filter { it.isDigit() }.toIntOrNull() ?: 2
            val digitsOnly = rawPrice.filter { it.isDigit() }
            var priceInt = digitsOnly.toIntOrNull() ?: 0

            if (rawPrice.contains(".") && !rawPrice.contains(",") && digitsOnly.length <= 6) {
                priceInt *= 1000
            }

            if (priceInt > 0) {
                val isCouple = paxInt == 2 || paxLabel.contains("Couple", ignoreCase = true) || paxLabel.contains(getString(
                    R.string.couple, "couple"), ignoreCase = true) || paxLabel.contains("2 Pax", ignoreCase = true)
                val perPaxInt = if (isCouple && priceInt > 3000000) priceInt / 2 else priceInt
                val couplePrice = if (isCouple) (if (priceInt > 3000000) priceInt else perPaxInt * 2) else 0

                val formattedTier = if (isCouple) {
                    "IDR " + String.format(Locale.US, "%,d", couplePrice) + " / " + getString(
                        R.string.couple, "couple")
                } else {
                    "IDR " + String.format(Locale.US, "%,d", perPaxInt) + " / " + getString(
                        R.string.pax, "pax")
                }

                tiers.add(
                    PricingTier(
                        paxCount = paxInt,
                        paxLabel = paxLabel,
                        pricePerPaxInt = perPaxInt,
                        priceFormatted = formattedTier
                    )
                )

                if (overallPrice.isEmpty()) {
                    overallPrice = if (isCouple) {
                        "IDR " + String.format(Locale.US, "%,d", couplePrice) + " / " + getString(
                            R.string.couple, "couple")
                    } else {
                        "IDR " + String.format(Locale.US, "%,d", priceInt) + " / " + getString(
                            R.string.person, "person")
                    }
                }
            }
        }

        return Pair(if (overallPrice.isNotEmpty()) overallPrice else "IDR 0", tiers)
    } else {
        val tiers = mutableListOf<PricingTier>()
        var overallPrice = ""

        if (config?.classB != null && extractedPairs.isNotEmpty()) {
            val first = extractedPairs[0]
            val digitsA = first.first.filter { it.isDigit() }
            val digitsB = first.second.filter { it.isDigit() }

            var pkgAPriceInt = digitsA.toIntOrNull() ?: 0
            if (first.first.contains(".") && digitsA.length <= 4) pkgAPriceInt *= 1000

            var pkgBPriceInt = digitsB.toIntOrNull() ?: 0
            if (first.second.contains(".") && digitsB.length <= 4) pkgBPriceInt *= 1000

            if (pkgAPriceInt > 0) {
                tiers.add(
                    PricingTier(
                        paxCount = 1,
                        paxLabel = getString(R.string.basic_package_A, "Package A (Standard)"),
                        pricePerPaxInt = pkgAPriceInt,
                        priceFormatted = "IDR " + String.format(Locale.US, "%,d", pkgAPriceInt) + " / " + getString(R.string.pax, "pax")
                    )
                )
            }
            if (pkgBPriceInt > 0) {
                tiers.add(
                    PricingTier(
                        paxCount = 2,
                        paxLabel = getString(R.string.complete_package_B, "Package B (Premium)"),
                        pricePerPaxInt = pkgBPriceInt,
                        priceFormatted = "IDR " + String.format(Locale.US, "%,d", pkgBPriceInt) + " / " + getString(R.string.pax, "pax")
                    )
                )
            }

            val strA = "IDR " + String.format(Locale.US, "%,d", pkgAPriceInt)
            val strB = "IDR " + String.format(Locale.US, "%,d", pkgBPriceInt)
            overallPrice = "$strA - $strB / " + getString(R.string.person, "person")
        } else if (extractedPairs.isNotEmpty()) {
            val raw = extractedPairs[0].second.ifEmpty { extractedPairs[0].first }
            val digits = raw.filter { it.isDigit() }
            var priceInt = digits.toIntOrNull() ?: 0
            if (raw.contains(".") && !raw.contains(",") && digits.length <= 4) priceInt *= 1000

            if (priceInt > 0) {
                overallPrice = "IDR " + String.format(Locale.US, "%,d", priceInt) + " / " + getString(
                    R.string.person, "person")
                tiers.add(
                    PricingTier(
                        paxCount = 1,
                        paxLabel = getString(R.string.one_pax, "1 Pax"),
                        pricePerPaxInt = priceInt,
                        priceFormatted = "IDR " + String.format(Locale.US, "%,d", priceInt) + " / " + getString(R.string.pax, "pax")
                    )
                )
            } else {
                overallPrice = "IDR 0"
            }
        }

        return Pair(if (overallPrice.isNotEmpty()) overallPrice else "IDR 0", tiers)
    }
}

fun getSavedPromoTrips(context: Context): List<TripPromo> {
    val defaultTrips = getDefaultWebsitePromoTrips(context)
    val prefs = context.getSharedPreferences(PROMO_CACHE_PREFS, Context.MODE_PRIVATE)
    val cachedJson = prefs.getString(KEY_CACHED_TRIPS, null)
    if (!cachedJson.isNullOrEmpty()) {
        try {
            val jsonArray = JSONArray(cachedJson)
            val cachedMap = mutableMapOf<String, Triple<String, List<PricingTier>, List<String>>>()
            val cachedList = mutableListOf<Triple<String, List<PricingTier>, List<String>>>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val nameStr = obj.optString("name")
                val price = obj.optString("price")
                val tierList = mutableListOf<PricingTier>()
                val tierArray = obj.optJSONArray("pricingTiers")
                if (tierArray != null) {
                    for (j in 0 until tierArray.length()) {
                        val tObj = tierArray.getJSONObject(j)
                        tierList.add(
                            PricingTier(
                                paxCount = tObj.optInt("paxCount", 2),
                                paxLabel = tObj.optString("paxLabel"),
                                pricePerPaxInt = tObj.optInt("pricePerPaxInt", 1850000),
                                priceFormatted = tObj.optString("priceFormatted")
                            )
                        )
                    }
                }
                val datesList = mutableListOf<String>()
                val datesArray = obj.optJSONArray("availableDates")
                if (datesArray != null) {
                    for (j in 0 until datesArray.length()) {
                        datesList.add(datesArray.getString(j))
                    }
                }
                val triple = Triple(price, tierList as List<PricingTier>, datesList as List<String>)
                cachedList.add(triple)
                if (nameStr.isNotBlank()) {
                    cachedMap[nameStr.lowercase(Locale.ROOT)] = triple
                }
            }

            return defaultTrips.mapIndexed { index, defaultTrip ->
                val tripNameLower = defaultTrip.name.lowercase(Locale.ROOT)
                val cachedEntry = cachedMap[tripNameLower]
                    ?: cachedMap.entries.find { (k, _) ->
                        ((tripNameLower.contains("ijen crater") || tripNameLower.contains("kawah ijen")) && (k.contains("ijen crater") || k.contains("kawah ijen"))) ||
                        ((tripNameLower.contains("mount bromo") || tripNameLower.contains("gunung bromo")) && (k.contains("mount bromo") || k.contains("gunung bromo"))) ||
                        (tripNameLower.contains("borobudur") && k.contains("borobudur")) ||
                        (tripNameLower.contains("bali") && k.contains("bali")) ||
                        (tripNameLower.contains("labuan") && k.contains("labuan")) ||
                        (tripNameLower.contains("raja") && k.contains("raja"))
                    }?.value
                    ?: cachedList.getOrNull(index)

                var finalPrice = cachedEntry?.first?.takeIf { it.isNotBlank() && it != "IDR 0" } ?: defaultTrip.price
                var finalTiers = if (cachedEntry != null && cachedEntry.second.isNotEmpty()) {
                    cachedEntry.second
                } else {
                    defaultTrip.pricingTiers
                }
                val finalDates = if (cachedEntry != null && cachedEntry.third.isNotEmpty()) {
                    cachedEntry.third
                } else {
                    defaultTrip.availableDates
                }

                val isIjenCrater = tripNameLower.contains("ijen crater") || tripNameLower.contains("kawah ijen")
                if (isIjenCrater && (finalPrice.contains("couple", ignoreCase = true) || finalPrice.contains("3.450.000"))) {
                    finalPrice = defaultTrip.price
                    finalTiers = defaultTrip.pricingTiers
                }

                val isYogyakarta = tripNameLower.contains("yogyakarta") || tripNameLower.contains("jogja")
                if (isYogyakarta && (finalPrice.contains("couple", ignoreCase = true) || finalPrice.contains("3.555.000"))) {
                    finalPrice = defaultTrip.price
                    finalTiers = defaultTrip.pricingTiers
                }

                defaultTrip.copy(
                    price = finalPrice,
                    pricingTiers = finalTiers,
                    availableDates = finalDates
                )
            }
        } catch (e: Exception) {
            Log.e("PromoSync", "Error parsing cached promo trip prices", e)
        }
    }
    return defaultTrips
}

suspend fun FetchPromoTripsFromWebsite(context: Context): List<TripPromo> = withContext(Dispatchers.IO) {
    try {
        val baseUrl = "https://www.zuhantotravelindo.id"
        val doc = try {
            Jsoup.connect(baseUrl).userAgent("Mozilla/5.0").timeout(8000).get()
        } catch (_: Exception) {
            null
        }

        var jsContent = ""
        if (doc != null) {
            val scriptElements = doc.select("script[src]")
            var jsBundleUrl: String? = null
            for (element in scriptElements) {
                val src = element.attr("src")
                if (src.contains("index-") && src.endsWith(".js")) {
                    jsBundleUrl = if (src.startsWith("http")) src else "$baseUrl$src"
                    break
                }
            }
            if (jsBundleUrl == null) {
                jsBundleUrl = "$baseUrl/assets/index-BKa6SZ3a.js"
            }
            try {
                jsContent = Jsoup.connect(jsBundleUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0")
                    .timeout(8000)
                    .execute()
                    .body()
            } catch (e: Exception) {
                Log.e("PromoSync", "Error fetching JS bundle for live price sync", e)
            }
        }

        val defaultTrips = getDefaultWebsitePromoTrips(context)
        val updatedTrips = coroutineScope {
            defaultTrips.map { defaultTrip ->
                async {
                    val cleanTripName = defaultTrip.name.lowercase(Locale.ROOT)
                    val matchingConfig = TARGET_PRICE_CONFIGS.entries.find { (key, cfg) ->
                        val cleanKey = key.lowercase(Locale.ROOT)
                        val cleanUrlPath = cfg.urlPath.removePrefix("/").replace("-", " ").lowercase(Locale.ROOT)
                        when {
                            cleanTripName.contains("bromo sunrise") -> cleanUrlPath.contains("bromo sunrise")
                            cleanTripName.contains("mount bromo") || cleanTripName.contains("gunung bromo") -> cleanUrlPath.contains("mount bromo") || cleanKey.contains("mount_bromo")
                            cleanTripName.contains("ijen blue fire") || cleanTripName.contains("blue fire") -> cleanUrlPath.contains("ijen blue fire")
                            cleanTripName.contains("ijen crater") || cleanTripName.contains("kawah ijen") -> cleanUrlPath.contains("ijen crater") || cleanKey.contains("ijen_crater")
                            cleanTripName.contains("tumpak") -> cleanUrlPath.contains("tumpak")
                            cleanTripName.contains("dieng") -> cleanUrlPath.contains("dieng")
                            cleanTripName.contains("yogyakarta") -> cleanUrlPath.contains("yogyakarta") || cleanKey.contains("yogyakarta-trip")
                            cleanTripName.contains("jogja") && !cleanTripName.contains("dieng") -> cleanUrlPath.contains("yogyakarta") || cleanKey.contains("yogyakarta-trip")
                            cleanTripName.contains("rinjan") -> cleanUrlPath.contains("rinjani")
                            cleanTripName.contains("borobudur") || cleanTripName.contains("candi borobudur") -> cleanUrlPath.contains("borobudur") || cleanKey.contains("borobudur_temple")
                            cleanTripName.contains("bali") -> cleanUrlPath.contains("bali")
                            cleanTripName.contains("labuan") -> cleanUrlPath.contains("labuan")
                            cleanTripName.contains("raja") -> cleanUrlPath.contains("raja")
                            else -> cleanTripName.equals(cleanUrlPath, ignoreCase = true) || (cleanTripName.length > 5 && cleanUrlPath.length > 5 && (cleanTripName.contains(cleanUrlPath) || cleanUrlPath.contains(cleanTripName)))
                        }
                    }

                    if (matchingConfig == null) {
                        defaultTrip
                    } else {
                        val targetUrl = matchingConfig.key
                        val packageDoc = if (doc != null) {
                            try {
                                Jsoup.connect(targetUrl).userAgent("Mozilla/5.0").timeout(5000).get()
                            } catch (_: Exception) { null }
                        } else null

                        val (websitePrice, targetTiers) = extractPriceByTargetClass(targetUrl, packageDoc, jsContent, context)
                        var finalPrice = if (websitePrice.isNotBlank() && websitePrice != "IDR 0") websitePrice else defaultTrip.price
                        var finalTiers = if (websitePrice.isNotBlank() && websitePrice != "IDR 0" && targetTiers.isNotEmpty()) targetTiers else defaultTrip.pricingTiers

                        val isIjenCrater = cleanTripName.contains("ijen crater") || cleanTripName.contains("kawah ijen")
                        if (isIjenCrater && (finalPrice.contains("couple", ignoreCase = true) || finalPrice.contains("3.450.000"))) {
                            finalPrice = if (defaultTrip.price.isNotBlank()) defaultTrip.price else "IDR 1,850,000 / Person"
                            finalTiers = defaultTrip.pricingTiers
                        }

                        val isYogyakarta = cleanTripName.contains("yogyakarta") || (cleanTripName.contains("jogja") && !cleanTripName.contains("dieng"))
                        if (isYogyakarta && (finalPrice.contains("couple", ignoreCase = true) || finalPrice.contains("3.555.000"))) {
                            finalPrice = if (defaultTrip.price.isNotBlank()) defaultTrip.price else "IDR 975,000 / Person"
                            finalTiers = defaultTrip.pricingTiers
                        }

                        defaultTrip.copy(price = finalPrice, pricingTiers = finalTiers)
                    }
                }
            }.awaitAll()
        }

        if (updatedTrips.isNotEmpty()) {
            savePromoTripsToCache(context, updatedTrips)
            return@withContext updatedTrips
        }
    } catch (e: Exception) {
        Log.e("PromoSync", "Failed to fetch live prices from website", e)
    }
    return@withContext getSavedPromoTrips(context)
}
