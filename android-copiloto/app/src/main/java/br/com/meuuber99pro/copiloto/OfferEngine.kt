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
     * Leitura preliminar de PRINT escolhido pelo usuário, não leitura de outros apps.
     * Só aceita UM valor principal e EXATAMENTE DUAS etapas com distância e duração.
     * Adicionais, preços unitários e textos ambíguos não entram no valor ofertado.
     * O resultado deve ser conferido pelo motorista antes da classificação.
     */
    fun parseScreenshotText(text: String): Offer? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size > 120 || lines.isEmpty()) return null

        val ancillary = Regex(
            """(?:/|por\s+)(?:km|h|hora)\b|adicional|b[oô]nus|gorjeta|taxa\s+extra""",
            RegexOption.IGNORE_CASE
        )
        val fareOptions = lines.flatMap { line ->
            if (ancillary.containsMatchIn(line)) emptyList()
            else currency.findAll(line).mapNotNull { number(it.groupValues[1]) }.toList()
        }
        // Não adivinhar qual é o pagamento caso apareçam vários valores válidos.
        if (fareOptions.size != 1) return null
        val fare = fareOptions.single()

        val distance = Regex(
            """(\d+(?:[.,]\d+)?)\s*(km|m)\b""", RegexOption.IGNORE_CASE
        )
        val minutes = Regex(
            """(\d+(?:[.,]\d+)?)\s*min(?:utos?)?\b""", RegexOption.IGNORE_CASE
        )
        fun getKm(line: String): Double? {
            val matches = distance.findAll(line).toList()
            if (matches.size != 1) return null
            val (num, unit) = matches.single().destructured
            val value = num.replace(",", ".").toDoubleOrNull() ?: return null
            return if (unit.equals("m", ignoreCase = true)) value / 1000 else value
        }
        fun getMinutes(line: String): Double? {
            val matches = minutes.findAll(line).toList()
            if (matches.size != 1) return null
            return matches.single().groupValues[1].replace(",", ".").toDoubleOrNull()
        }
        data class Leg(val km: Double, val min: Double)
        val legs = mutableListOf<Leg>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            // Não usar linhas com valores de dinheiro como etapas do percurso.
            if (line.contains("R$", ignoreCase = true)) { i++; continue }
            val km = getKm(line)
            val time = getMinutes(line)
            if (km != null && time != null) {
                legs.add(Leg(km, time))
                i++
                continue
            }
            // OCR às vezes separa "4 min" e "825 m" em linhas adjacentes.
            if ((km != null) != (time != null) && i + 1 < lines.size) {
                val next = lines[i + 1]
                if (!next.contains("R$", ignoreCase = true)) {
                    val km2 = getKm(next)
                    val time2 = getMinutes(next)
                    if (km == null && time != null && km2 != null && time2 == null) {
                        legs.add(Leg(km2, time)); i += 2; continue
                    }
                    if (km != null && time == null && km2 == null && time2 != null) {
                        legs.add(Leg(km, time2)); i += 2; continue
                    }
                }
            }
            i++
        }
        // Ordem visual comum das ofertas: busca primeiro, viagem depois.
        // Se houver outras medidas na tela, não fazer suposição.
        if (legs.size != 2) return null
        return Offer(fare, legs[0].km, legs[1].km, legs[0].min, legs[1].min)
            .takeIf { isValid(it) }
    }

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
