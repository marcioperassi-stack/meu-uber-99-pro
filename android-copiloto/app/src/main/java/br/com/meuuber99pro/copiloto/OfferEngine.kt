package br.com.meuuber99pro.copiloto

import java.util.Locale

/**
 * Motor de cálculo INDEPENDENTE da leitura da tela.
 * Metas fixas de faturamento bruto: R$ 1,70/km E R$ 40/h.
 * Nenhum dado de corrida é salvo ou enviado.
 */
object OfferEngine {
    data class Offer(
        val fare: Double,
        val pickupKm: Double,
        val tripKm: Double,
        val pickupMinutes: Double,
        val tripMinutes: Double
    )

    data class Assessment(
        val good: Boolean,
        val grossPerHour: Double,
        val grossPerKm: Double
    ) {
        val label: String get() = if (good) "CORRIDA BOA" else "CORRIDA RUIM"
    }

    private val currency = Regex("""R\$\s*(\d{1,3}(?:\.\d{3})*,\d{2})""")
    private val kmPattern = Regex("""(\d+(?:[.,]\d+)?)\s*km\b""", RegexOption.IGNORE_CASE)
    private val minutesPattern = Regex("""(\d+(?:[.,]\d+)?)\s*min(?:utos?)?\b""", RegexOption.IGNORE_CASE)

    private fun number(raw: String): Double? =
        raw.replace(".", "").replace(",", ".").toDoubleOrNull()

    /**
     * Reconhece apenas um formato rotulado:
     * "R$ 30,00\nBusca 2 km 5 min\nViagem 8 km 25 min".
     * Ofertas com estrutura desconhecida retornam null (nunca inventar um veredito).
     * É necessário ampliar/validar com telas reais das plataformas.
     */
    fun parseVisibleText(text: String): Offer? {
        val fares = currency.findAll(text)
            .mapNotNull { number(it.groupValues[1]) }
            .distinct().toList()
        if (fares.size != 1) return null

        val lines = text.lines().map { it.trim() }
        val pickup = lines.firstOrNull {
            it.contains(Regex("""buscar|busca|embarque|passageiro""", RegexOption.IGNORE_CASE))
        } ?: return null
        val trip = lines.firstOrNull {
            it.contains(Regex("""viagem|trajeto""", RegexOption.IGNORE_CASE))
        } ?: return null

        fun distanceAndTime(line: String): Pair<Double, Double>? {
            val km = kmPattern.find(line)?.groupValues?.get(1)
                ?.replace(",", ".")?.toDoubleOrNull() ?: return null
            val minutes = minutesPattern.find(line)?.groupValues?.get(1)
                ?.replace(",", ".")?.toDoubleOrNull() ?: return null
            return km to minutes
        }

        val (pickupKm, pickupMin) = distanceAndTime(pickup) ?: return null
        val (tripKm, tripMin) = distanceAndTime(trip) ?: return null
        val offer = Offer(fares.single(), pickupKm, tripKm, pickupMin, tripMin)
        return if (isValid(offer)) offer else null
    }


    /**
     * Diagnóstico agregado. Não contém texto original do print, nomes ou endereços.
     * Quando não houver exatamente 1 preço principal, 2 distâncias e 2 tempos,
     * NÃO escolher arbitrariamente uma rota.
     */
    data class ScreenshotReading(
        val offer: Offer?,
        val fareCount: Int,
        val distanceCount: Int,
        val timeCount: Int
    )

    fun inspectScreenshotText(text: String): ScreenshotReading {
        // OCR pode interpretar "R$" como "RS" ou separar "R$" e "19,40" por linha.
        val normalized = text.replace(
            Regex("""(?i)\bR\s*[${'\s*(?=\d)""")
        ) { "R$ " }
        val lines = normalized.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size > 160 || lines.isEmpty()) return ScreenshotReading(null, 0, 0, 0)

        val money = Regex(
            """R[$S]\s*(\d{1,4}(?:\.\d{3})*[.,]\d{2})""",
            RegexOption.IGNORE_CASE
        )
        val rate = Regex(
            """^\s*(?:/|por\s+)(?:km|h|hora)\b""", RegexOption.IGNORE_CASE
        )
        val extraAfter = Regex(
            """^\s*(?:adicional|b[oô]nus|gorjeta|taxa\s+extra|extra)\b""",
            RegexOption.IGNORE_CASE
        )
        val extraBefore = Regex(
            """(?:adicional|b[oô]nus|gorjeta|taxa\s+extra|extra)\s*(?:de|:)?\s*$""",
            RegexOption.IGNORE_CASE
        )

        fun moneyValue(raw: String): Double? {
            // Vírgula: R$ 1.234,56; ponto OCR: R$ 19.40
            val numeric = if (raw.contains(',')) {
                raw.replace(".", "").replace(",", ".")
            } else raw
            return numeric.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
        }

        val fares = lines.flatMap { line ->
            money.findAll(line).mapNotNull { match ->
                val before = line.substring(0, match.range.first).takeLast(30)
                val after = line.substring(match.range.last + 1)
                // Ignorar apenas o valor acessório, NÃO a linha inteira.
                if (rate.containsMatchIn(after) ||
                    extraAfter.containsMatchIn(after) ||
                    extraBefore.containsMatchIn(before)
                ) null
                else moneyValue(match.groupValues[1])
            }.toList()
        }

        val distance = Regex(
            """(\d+(?:[.,]\d+)?)\s*(km|m)\b""", RegexOption.IGNORE_CASE
        )
        val minutes = Regex(
            """(\d+(?:[.,]\d+)?)\s*min(?:utos?)?\b""", RegexOption.IGNORE_CASE
        )
        val kmValues = lines.flatMap { line ->
            distance.findAll(line).mapNotNull {
                val number = it.groupValues[1].replace(",", ".").toDoubleOrNull()
                if (number == null || !number.isFinite() || number < 0) null
                else if (it.groupValues[2].equals("m", ignoreCase = true)) number / 1000
                else number
            }.toList()
        }
        val minuteValues = lines.flatMap { line ->
            minutes.findAll(line).mapNotNull {
                it.groupValues[1].replace(",", ".").toDoubleOrNull()
                    ?.takeIf { n -> n.isFinite() && n >= 0 }
            }.toList()
        }

        // A ordem das medidas na tela costuma ser busca e, em seguida, viagem.
        // A exigência de contagens exatas evita associar pontos intermediários.
        val offer = if (fares.size == 1 && kmValues.size == 2 && minuteValues.size == 2) {
            Offer(fares.single(), kmValues[0], kmValues[1], minuteValues[0], minuteValues[1])
                .takeIf { isValid(it) }
        } else null
        return ScreenshotReading(offer, fares.size, kmValues.size, minuteValues.size)
    }

    fun parseScreenshotText(text: String): Offer? = inspectScreenshotText(text).offer

    private fun isValid(o: Offer): Boolean =
        listOf(o.fare, o.pickupKm, o.tripKm, o.pickupMinutes, o.tripMinutes)
            .all { it.isFinite() && it >= 0 } &&
        o.fare > 0 && (o.pickupKm + o.tripKm) > 0 &&
        (o.pickupMinutes + o.tripMinutes) > 0 &&
        (o.pickupMinutes + o.tripMinutes) <= 240

    const val MIN_GROSS_PER_KM = 1.70
    const val MIN_GROSS_PER_HOUR = 40.0

    /** Analisa apenas o faturamento bruto incluindo busca e viagem, sem deduzir custos. */
    fun evaluate(offer: Offer): Assessment? {
        if (!isValid(offer)) return null
        val totalKm = offer.pickupKm + offer.tripKm
        val totalHours = (offer.pickupMinutes + offer.tripMinutes) / 60.0
        val grossHourly = offer.fare / totalHours
        val grossKm = offer.fare / totalKm
        return Assessment(
            good = grossHourly + 1e-9 >= MIN_GROSS_PER_HOUR &&
                grossKm + 1e-9 >= MIN_GROSS_PER_KM,
            grossPerHour = grossHourly,
            grossPerKm = grossKm
        )
    }

    fun brl(value: Double): String =
        String.format(Locale("pt", "BR"), "R$ %.2f", value)
}
}S]\s*(?=\d)""")
        ) { "R$ " }
        val lines = normalized.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size > 160 || lines.isEmpty()) return ScreenshotReading(null, 0, 0, 0)

        val money = Regex(
            """R[${'\s*(\d{1,4}(?:\.\d{3})*[.,]\d{2})""",
            RegexOption.IGNORE_CASE
        )
        val rate = Regex(
            """^\s*(?:/|por\s+)(?:km|h|hora)\b""", RegexOption.IGNORE_CASE
        )
        val extraAfter = Regex(
            """^\s*(?:adicional|b[oô]nus|gorjeta|taxa\s+extra|extra)\b""",
            RegexOption.IGNORE_CASE
        )
        val extraBefore = Regex(
            """(?:adicional|b[oô]nus|gorjeta|taxa\s+extra|extra)\s*(?:de|:)?\s*$""",
            RegexOption.IGNORE_CASE
        )

        fun moneyValue(raw: String): Double? {
            // Vírgula: R$ 1.234,56; ponto OCR: R$ 19.40
            val numeric = if (raw.contains(',')) {
                raw.replace(".", "").replace(",", ".")
            } else raw
            return numeric.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
        }

        val fares = lines.flatMap { line ->
            money.findAll(line).mapNotNull { match ->
                val before = line.substring(0, match.range.first).takeLast(30)
                val after = line.substring(match.range.last + 1)
                // Ignorar apenas o valor acessório, NÃO a linha inteira.
                if (rate.containsMatchIn(after) ||
                    extraAfter.containsMatchIn(after) ||
                    extraBefore.containsMatchIn(before)
                ) null
                else moneyValue(match.groupValues[1])
            }.toList()
        }

        val distance = Regex(
            """(\d+(?:[.,]\d+)?)\s*(km|m)\b""", RegexOption.IGNORE_CASE
        )
        val minutes = Regex(
            """(\d+(?:[.,]\d+)?)\s*min(?:utos?)?\b""", RegexOption.IGNORE_CASE
        )
        val kmValues = lines.flatMap { line ->
            distance.findAll(line).mapNotNull {
                val number = it.groupValues[1].replace(",", ".").toDoubleOrNull()
                if (number == null || !number.isFinite() || number < 0) null
                else if (it.groupValues[2].equals("m", ignoreCase = true)) number / 1000
                else number
            }.toList()
        }
        val minuteValues = lines.flatMap { line ->
            minutes.findAll(line).mapNotNull {
                it.groupValues[1].replace(",", ".").toDoubleOrNull()
                    ?.takeIf { n -> n.isFinite() && n >= 0 }
            }.toList()
        }

        // A ordem das medidas na tela costuma ser busca e, em seguida, viagem.
        // A exigência de contagens exatas evita associar pontos intermediários.
        val offer = if (fares.size == 1 && kmValues.size == 2 && minuteValues.size == 2) {
            Offer(fares.single(), kmValues[0], kmValues[1], minuteValues[0], minuteValues[1])
                .takeIf { isValid(it) }
        } else null
        return ScreenshotReading(offer, fares.size, kmValues.size, minuteValues.size)
    }

    fun parseScreenshotText(text: String): Offer? = inspectScreenshotText(text).offer

    private fun isValid(o: Offer): Boolean =
        listOf(o.fare, o.pickupKm, o.tripKm, o.pickupMinutes, o.tripMinutes)
            .all { it.isFinite() && it >= 0 } &&
        o.fare > 0 && (o.pickupKm + o.tripKm) > 0 &&
        (o.pickupMinutes + o.tripMinutes) > 0 &&
        (o.pickupMinutes + o.tripMinutes) <= 240

    const val MIN_GROSS_PER_KM = 1.70
    const val MIN_GROSS_PER_HOUR = 40.0

    /** Analisa apenas o faturamento bruto incluindo busca e viagem, sem deduzir custos. */
    fun evaluate(offer: Offer): Assessment? {
        if (!isValid(offer)) return null
        val totalKm = offer.pickupKm + offer.tripKm
        val totalHours = (offer.pickupMinutes + offer.tripMinutes) / 60.0
        val grossHourly = offer.fare / totalHours
        val grossKm = offer.fare / totalKm
        return Assessment(
            good = grossHourly + 1e-9 >= MIN_GROSS_PER_HOUR &&
                grossKm + 1e-9 >= MIN_GROSS_PER_KM,
            grossPerHour = grossHourly,
            grossPerKm = grossKm
        )
    }

    fun brl(value: Double): String =
        String.format(Locale("pt", "BR"), "R$ %.2f", value)
}
}S]\s*(\d{1,4}(?:\.\d{3})*[.,]\d{2})""",
            RegexOption.IGNORE_CASE
        )
        val rate = Regex(
            """^\s*(?:/|por\s+)(?:km|h|hora)\b""", RegexOption.IGNORE_CASE
        )
        val extraAfter = Regex(
            """^\s*(?:adicional|b[oô]nus|gorjeta|taxa\s+extra|extra)\b""",
            RegexOption.IGNORE_CASE
        )
        val extraBefore = Regex(
            """(?:adicional|b[oô]nus|gorjeta|taxa\s+extra|extra)\s*(?:de|:)?\s*$""",
            RegexOption.IGNORE_CASE
        )

        fun moneyValue(raw: String): Double? {
            // Vírgula: R$ 1.234,56; ponto OCR: R$ 19.40
            val numeric = if (raw.contains(',')) {
                raw.replace(".", "").replace(",", ".")
            } else raw
            return numeric.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
        }

        val fares = lines.flatMap { line ->
            money.findAll(line).mapNotNull { match ->
                val before = line.substring(0, match.range.first).takeLast(30)
                val after = line.substring(match.range.last + 1)
                // Ignorar apenas o valor acessório, NÃO a linha inteira.
                if (rate.containsMatchIn(after) ||
                    extraAfter.containsMatchIn(after) ||
                    extraBefore.containsMatchIn(before)
                ) null
                else moneyValue(match.groupValues[1])
            }.toList()
        }

        val distance = Regex(
            """(\d+(?:[.,]\d+)?)\s*(km|m)\b""", RegexOption.IGNORE_CASE
        )
        val minutes = Regex(
            """(\d+(?:[.,]\d+)?)\s*min(?:utos?)?\b""", RegexOption.IGNORE_CASE
        )
        val kmValues = lines.flatMap { line ->
            distance.findAll(line).mapNotNull {
                val number = it.groupValues[1].replace(",", ".").toDoubleOrNull()
                if (number == null || !number.isFinite() || number < 0) null
                else if (it.groupValues[2].equals("m", ignoreCase = true)) number / 1000
                else number
            }.toList()
        }
        val minuteValues = lines.flatMap { line ->
            minutes.findAll(line).mapNotNull {
                it.groupValues[1].replace(",", ".").toDoubleOrNull()
                    ?.takeIf { n -> n.isFinite() && n >= 0 }
            }.toList()
        }

        // A ordem das medidas na tela costuma ser busca e, em seguida, viagem.
        // A exigência de contagens exatas evita associar pontos intermediários.
        val offer = if (fares.size == 1 && kmValues.size == 2 && minuteValues.size == 2) {
            Offer(fares.single(), kmValues[0], kmValues[1], minuteValues[0], minuteValues[1])
                .takeIf { isValid(it) }
        } else null
        return ScreenshotReading(offer, fares.size, kmValues.size, minuteValues.size)
    }

    fun parseScreenshotText(text: String): Offer? = inspectScreenshotText(text).offer

    private fun isValid(o: Offer): Boolean =
        listOf(o.fare, o.pickupKm, o.tripKm, o.pickupMinutes, o.tripMinutes)
            .all { it.isFinite() && it >= 0 } &&
        o.fare > 0 && (o.pickupKm + o.tripKm) > 0 &&
        (o.pickupMinutes + o.tripMinutes) > 0 &&
        (o.pickupMinutes + o.tripMinutes) <= 240

    const val MIN_GROSS_PER_KM = 1.70
    const val MIN_GROSS_PER_HOUR = 40.0

    /** Analisa apenas o faturamento bruto incluindo busca e viagem, sem deduzir custos. */
    fun evaluate(offer: Offer): Assessment? {
        if (!isValid(offer)) return null
        val totalKm = offer.pickupKm + offer.tripKm
        val totalHours = (offer.pickupMinutes + offer.tripMinutes) / 60.0
        val grossHourly = offer.fare / totalHours
        val grossKm = offer.fare / totalKm
        return Assessment(
            good = grossHourly + 1e-9 >= MIN_GROSS_PER_HOUR &&
                grossKm + 1e-9 >= MIN_GROSS_PER_KM,
            grossPerHour = grossHourly,
            grossPerKm = grossKm
        )
    }

    fun brl(value: Double): String =
        String.format(Locale("pt", "BR"), "R$ %.2f", value)
}
