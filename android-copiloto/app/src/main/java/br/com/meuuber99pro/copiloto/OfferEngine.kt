package br.com.meuuber99pro.copiloto

import java.time.LocalDate
import java.util.Locale

/**
 * Motor de cálculo INDEPENDENTE da leitura da tela.
 * Valores iniciais iguais aos do analisador web; o APK piloto ainda não sincroniza preferências.
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
        val netPerHour: Double,
        val netPerKm: Double,
        val netAmount: Double
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

    /** Cálculo de referência — preço, consumo, desgaste, financiamento e metas são provisórios. */
    fun evaluate(offer: Offer, daysInMonth: Int = LocalDate.now().lengthOfMonth()): Assessment? {
        if (!isValid(offer) || daysInMonth !in 28..31) return null
        val km = offer.pickupKm + offer.tripKm
        val hours = (offer.pickupMinutes + offer.tripMinutes) / 60.0
        val fuelCost = km / 9.0 * 3.62
        val wearCost = km * 0.25
        val financeReserve = km / (300.0 * daysInMonth) * 2252.0
        val net = offer.fare - fuelCost - wearCost - financeReserve
        val grossHourly = offer.fare / hours
        val netHourly = net / hours
        val netKm = net / km
        return Assessment(
            good = net > 0 && grossHourly >= 40.0 &&
                netHourly >= 25.0 && netKm >= 1.0,
            grossPerHour = grossHourly,
            netPerHour = netHourly,
            netPerKm = netKm,
            netAmount = net
        )
    }

    fun brl(value: Double): String =
        String.format(Locale("pt", "BR"), "R$ %.2f", value)
}
