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
