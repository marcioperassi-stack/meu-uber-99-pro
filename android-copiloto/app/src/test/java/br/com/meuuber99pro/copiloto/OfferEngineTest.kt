package br.com.meuuber99pro.copiloto

import org.junit.Assert.*
import org.junit.Test

class OfferEngineTest {
    @Test fun printComMetrosBonusEValorPorKmNaoConfundeTarifa() {
        val texto = """
            UberX
            R$ 19,40
            R$ 1,65/km
            4 min (825 m)
            12 min (10,9 km)
            R$ 3,00 adicional
        """.trimIndent()
        val offer = OfferEngine.parseScreenshotText(texto)
        assertNotNull(offer)
        assertEquals(19.40, offer!!.fare, 0.0001)
        assertEquals(0.825, offer.pickupKm, 0.0001)
        assertEquals(10.9, offer.tripKm, 0.0001)
        assertEquals(4.0, offer.pickupMinutes, 0.0001)
        assertEquals(12.0, offer.tripMinutes, 0.0001)
        val result = OfferEngine.evaluate(offer)!!
        assertFalse(result.good)
        assertEquals(72.75, result.grossPerHour, 0.0001)
        assertEquals(19.4 / 11.725, result.grossPerKm, 0.0001)
    }

    @Test fun printComMedidasEmLinhasSeparadas() {
        val offer = OfferEngine.parseScreenshotText(
            "R$ 30,00\n5 min\n2 km\n25 min\n8 km"
        )
        assertNotNull(offer)
        assertEquals(2.0, offer!!.pickupKm, 0.0001)
        assertEquals(8.0, offer.tripKm, 0.0001)
        assertEquals(5.0, offer.pickupMinutes, 0.0001)
        assertTrue(OfferEngine.evaluate(offer)!!.good)
    }

    @Test fun doisValoresPrincipaisSemRotulosSaoAmbiguos() {
        assertNull(OfferEngine.parseScreenshotText(
            "R$ 19,40\nR$ 18,90\n4 min 825 m\n12 min 10,9 km"
        ))
    }

    @Test fun tresEtapasNaoPermitemAdvinhar() {
        assertNull(OfferEngine.parseScreenshotText(
            "R$ 30,00\n4 min 825 m\n12 min 10,9 km\n3 min 1 km"
        ))
    }

    @Test fun naoAceitaTextoSemValor() {
        assertNull(OfferEngine.parseScreenshotText(
            "4 min 825 m\n12 min 10,9 km"
        ))
    }

    @Test fun precoPorKmNaoSubstituiValorDaCorrida() {
        assertNull(OfferEngine.parseScreenshotText(
            "R$ 1,70/km\n4 min 825 m\n12 min 10,9 km"
        ))
    }

    @Test fun metaExataAceitaComDuasCondicoes() {
        val offer = OfferEngine.Offer(20.0, 2.0, 8.0, 5.0, 25.0)
        val result = OfferEngine.evaluate(offer)!!
        assertTrue(result.good)
        assertEquals(2.0, result.grossPerKm, 0.0001)
        assertEquals(40.0, result.grossPerHour, 0.0001)
    }

    @Test fun apenasUmaMetaCumpridaNaoBasta() {
        assertFalse(OfferEngine.evaluate(
            OfferEngine.Offer(17.0, 2.0, 8.0, 5.0, 25.0)
        )!!.good)
        assertFalse(OfferEngine.evaluate(
            OfferEngine.Offer(20.0, 2.0, 10.0, 5.0, 20.0)
        )!!.good)
    }

    @Test fun precoInsuficienteReprova() {
        assertFalse(OfferEngine.evaluate(
            OfferEngine.Offer(15.0, 2.0, 8.0, 5.0, 25.0)
        )!!.good)
    }

    @Test fun ofertaInvalidaNaoAvalia() {
        assertNull(OfferEngine.evaluate(
            OfferEngine.Offer(30.0, 0.0, 0.0, 0.0, 0.0)
        ))
    }
    @Test fun valorPrincipalEValorPorKmNaMesmaLinha() {
        val o = OfferEngine.parseScreenshotText(
            "R$ 19,40  R$ 1,65/km\n4 min (825 m)\n12 min (10,9 km)"
        )
        assertNotNull(o)
        assertEquals(19.40, o!!.fare, 0.0001)
        assertEquals(0.825, o.pickupKm, 0.0001)
    }

    @Test fun moedaSeparadaEntreLinhas() {
        val o = OfferEngine.parseScreenshotText(
            "R$\n19,40\n4 min\n825 m\n12 min\n10,9 km"
        )
        assertNotNull(o)
        assertEquals(19.40, o!!.fare, 0.0001)
    }

    @Test fun moedaOCRConfundeCifraoComS() {
        val o = OfferEngine.parseScreenshotText(
            "RS 19,40\n4 min 825 m\n12 min 10,9 km"
        )
        assertNotNull(o)
        assertEquals(19.40, o!!.fare, 0.0001)
    }

    @Test fun enderecoEntreTempoEDistanciaNaoInvalida() {
        val o = OfferEngine.parseScreenshotText(
            "R$ 19,40\n4 min\nRua Exemplo\n825 m\n" +
                "12 min\nAvenida Exemplo\n10,9 km"
        )
        assertNotNull(o)
        assertEquals(4.0, o!!.pickupMinutes, 0.0001)
        assertEquals(10.9, o.tripKm, 0.0001)
    }

    @Test fun relatorioDeOCRMostraApenasContagem() {
        val result = OfferEngine.inspectScreenshotText(
            "R$ 19,40\n4 min\n825 m\n12 min"
        )
        assertNull(result.offer)
        assertEquals(1, result.fareCount)
        assertEquals(1, result.distanceCount)
        assertEquals(2, result.timeCount)
    }

    @Test fun taxaExtraNaMesmaLinhaDaTarifaNaoApagaValorPrincipal() {
        val o = OfferEngine.parseScreenshotText(
            "R$ 19,40  R$ 3,00 adicional\n4 min 825 m\n12 min 10,9 km"
        )
        assertNotNull(o)
        assertEquals(19.40, o!!.fare, 0.0001)
    }

    @Test fun muitosValoresAmbiguosNaoClassificam() {
        val o = OfferEngine.parseScreenshotText(
            "R$ 19,40  R$ 20,40\n4 min 825 m\n12 min 10,9 km"
        )
        assertNull(o)
    }

}
