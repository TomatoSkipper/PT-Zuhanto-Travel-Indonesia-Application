package com.example.appproject

import com.example.appproject.data.remote.extractPriceByTargetClass
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTargetClassPriceExtraction() {
        val baseUrl = "https://www.zuhantotravelindo.id"
        val jsUrl = "$baseUrl/assets/index-BKa6SZ3a.js"
        val jsContent = Jsoup.connect(jsUrl).ignoreContentType(true).userAgent("Mozilla/5.0").timeout(10000).execute().body()

        // 1. Bromo Sunrise Package
        val bromoUrl = "$baseUrl/bromo-sunrise-package"
        val (bromoPrice, bromoTiers) = extractPriceByTargetClass(bromoUrl, null, jsContent)
        assertEquals(extractPriceByTargetClass(bromoUrl, null, jsContent).first, bromoPrice)
        assertEquals(5, bromoTiers.size)
        assertEquals(1625000, bromoTiers[0].pricePerPaxInt)

        // 2. Ijen Blue Fire Package
        val ijenUrl = "$baseUrl/ijen-blue-fire-package"
        val (ijenPrice, ijenTiers) = extractPriceByTargetClass(ijenUrl, null, jsContent)
        assertEquals("IDR 3,450,000 / couple", ijenPrice)
        assertEquals(5, ijenTiers.size)
        assertEquals(1725000, ijenTiers[0].pricePerPaxInt)

        // 3. Jogja Dieng Package
        val jogjaDiengUrl = "$baseUrl/jogja-dieng-package"
        val (jogjaDiengPrice, jogjaDiengTiers) = extractPriceByTargetClass(
            jogjaDiengUrl,
            null,
            jsContent
        )
        assertEquals("IDR 3,555,000 / couple", jogjaDiengPrice)
        assertEquals(5, jogjaDiengTiers.size)
        assertEquals(1777500, jogjaDiengTiers[0].pricePerPaxInt)

        // 4. Tumpak Sewu Trip
        val tumpakUrl = "$baseUrl/tumpak-sewu-package"
        val (tumpakPrice, tumpakTiers) = extractPriceByTargetClass(tumpakUrl, null, jsContent)
        assertEquals("IDR 385,000 - IDR 445,000 / person", tumpakPrice)
        assertEquals(2, tumpakTiers.size)
        assertEquals(385000, tumpakTiers[0].pricePerPaxInt)
        assertEquals(445000, tumpakTiers[1].pricePerPaxInt)

        // 5. Mount Rinjani Trekking 2026
        val rinjaniUrl = "$baseUrl/mount-rinjani-trekking"
        val (rinjaniPrice, rinjaniTiers) = extractPriceByTargetClass(rinjaniUrl, null, jsContent)
        assertEquals("IDR 5,300,000 / person", rinjaniPrice)
        assertEquals(1, rinjaniTiers.size)
        assertEquals(5300000, rinjaniTiers[0].pricePerPaxInt)

        // 6. Trip Yogyakarta 2D1N
        val yogyakartaUrl = "$baseUrl/yogyakarta-trip"
        val (yogyakartaPrice, yogyakartaTiers) = extractPriceByTargetClass(
            yogyakartaUrl,
            null,
            jsContent
        )
        assertEquals("IDR 975,000 / person", yogyakartaPrice)
        assertEquals(1, yogyakartaTiers.size)
        assertEquals(975000, yogyakartaTiers[0].pricePerPaxInt)

        // 7. Mount Bromo
        val mountBromoUrl = "$baseUrl/mount-bromo-package"
        val (mountBromoPrice, mountBromoTiers) = extractPriceByTargetClass(
            mountBromoUrl,
            null,
            jsContent
        )
        assertEquals("IDR 1,850,000 / person", mountBromoPrice)
        assertEquals(1, mountBromoTiers.size)
        assertEquals(1850000, mountBromoTiers[0].pricePerPaxInt)

        //8. Borobudur Temple
        val borobudurUrl = "$baseUrl/borobudur-temple-package"
        val (borobudurPrice, borobudurTiers) = extractPriceByTargetClass(
            borobudurUrl,
            null,
            jsContent
        )
        assertEquals("IDR 1,450,000 / person", borobudurPrice)
        assertEquals(1, borobudurTiers.size)
        assertEquals(1450000, borobudurTiers[0].pricePerPaxInt)

        //9. Ijen Crater
        val ijenCraterUrl = "$baseUrl/ijen-crater-package"
        val (ijenCraterPrice, ijenCraterTiers) = extractPriceByTargetClass(
            ijenCraterUrl,
            null,
            jsContent
        )
        assertEquals("IDR 1,850,000 / person", ijenCraterPrice)
        assertEquals(1, ijenCraterTiers.size)
        assertEquals(1850000, ijenCraterTiers[0].pricePerPaxInt)

        //10. Bali
        val baliUrl = "$baseUrl/bali-package"
        val (baliPrice, baliTiers) = extractPriceByTargetClass(baliUrl, null, jsContent)
        assertEquals("IDR 2,750,000 / person", baliPrice)
        assertEquals(1, baliTiers.size)
        assertEquals(2750000, baliTiers[0].pricePerPaxInt)

        //11. Labuan Bajo
        val labuanBajoUrl = "$baseUrl/labuan-bajo-package"
        val (labuanBajoPrice, labuanBajoTiers) = extractPriceByTargetClass(
            labuanBajoUrl,
            null,
            jsContent
        )
        assertEquals("IDR 4,950,000 / person", labuanBajoPrice)
        assertEquals(1, labuanBajoTiers.size)
        assertEquals(4950000, labuanBajoTiers[0].pricePerPaxInt)

        //12. Raja Ampat
        val rajaAmpatUrl = "$baseUrl/raja-ampat-package"
        val (rajaAmpatPrice, rajaAmpatTiers) = extractPriceByTargetClass(
            rajaAmpatUrl,
            null,
            jsContent
        )
        assertEquals("IDR 7,950,000 / person", rajaAmpatPrice)
        assertEquals(1, rajaAmpatTiers.size)
        assertEquals(7950000, rajaAmpatTiers[0].pricePerPaxInt)

        // . Unlisted Packages -> default price to 0
        val unlistedUrl = "$baseUrl/malang-batu-package"
        val (unlistedPrice, unlistedTiers) = extractPriceByTargetClass(unlistedUrl, null, jsContent)
        assertEquals("IDR 0", unlistedPrice)
        assertTrue(unlistedTiers.isEmpty())
    }
}