package br.com.meuuber99pro.copiloto

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Tela inicial do protótipo. Consentimento é obrigatório antes de analisar qualquer oferta.
 * A ativação do serviço de acessibilidade acontece exclusivamente nas configurações do Android.
 */
class MainActivity : Activity() {
    private val background = Color.rgb(8, 17, 12)
    private val foreground = Color.rgb(233, 249, 237)
    private val green = Color.rgb(55, 220, 115)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background
        window.navigationBarColor = background
        if (BuildConfig.FLAVOR == "demo") {
            exibirDemoSemPermissoes()
            return
        }

        val prefs = getSharedPreferences("copiloto", MODE_PRIVATE)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 42, 32, 32)
            setBackgroundColor(this@MainActivity.background)
        }
        fun label(text: String, size: Float = 17f, color: Int = foreground): TextView =
            TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                setPadding(0, 0, 0, 24)
            }
        fun button(text: String): Button = Button(this).apply {
            this.text = text
            setTextColor(Color.BLACK)
            setBackgroundColor(green)
            isAllCaps = false
        }

        container.addView(label("Meu Uber/99 Pro", 28f, green))
        container.addView(label("Copiloto Android — protótipo experimental", 18f))
        container.addView(label(
            "Objetivo: avisar CORRIDA BOA ou CORRIDA RUIM por cima da Uber e da 99, " +
            "sem tocar em aceitar ou recusar. A análise usa R$ 40/h de faturamento bruto, " +
            "R$ 25/h e R$ 1/km de sobra, com combustível, desgaste e reserva do financiamento."
        ))
        container.addView(label(
            "Privacidade: se você ativar o recurso, o Android permitirá que este " +
            "serviço leia textos exibidos nas telas de ofertas da Uber Driver e da 99 Motorista " +
            "(valor, distância e tempo). O processamento é local. " +
            "Não enviamos, gravamos nem compartilhamos os textos e não lemos outros apps. " +
            "O recurso pode não reconhecer ofertas se o formato das telas mudar.", 15f
        ))

        val openSettings = button("Abrir configurações de Acessibilidade")
        val consent = CheckBox(this).apply {
            text = "Autorizo a leitura dos textos das ofertas da Uber e 99 para análise local."
            textSize = 15f
            setTextColor(this@MainActivity.foreground)
            isChecked = prefs.getBoolean("consentimento", false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("consentimento", isChecked).apply()
                openSettings.isEnabled = isChecked
            }
        }
        container.addView(consent)
        openSettings.isEnabled = consent.isChecked
        openSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        container.addView(openSettings)
        container.addView(label(
            "Nas configurações do Android, procure 'Analisar ofertas Uber e 99'. " +
            "Você pode desativar esse acesso a qualquer momento. " +
            "O Waze não será lido nem controlado.", 14f
        ))

        val preview = label(
            "Teste local ainda não executado. Ele serve apenas para ver a aparência " +
            "do resultado; não comprova leitura automática.", 16f
        ).apply {
            gravity = Gravity.CENTER
            setPadding(16, 24, 16, 24)
            setBackgroundColor(Color.rgb(20, 45, 30))
        }
        val demoButton = button("Testar cálculo com corrida fictícia")
        demoButton.setOnClickListener {
            val example = OfferEngine.Offer(30.0, 2.0, 8.0, 5.0, 25.0)
            val result = OfferEngine.evaluate(example)
            preview.text = if (result == null) {
                "ANÁLISE INCOMPLETA"
            } else {
                "DEMONSTRAÇÃO — " + result.label + "\n" +
                    OfferEngine.brl(result.grossPerHour) + "/h bruto\n" +
                    OfferEngine.brl(result.netPerHour) + "/h após custos"
            }
            preview.setTextColor(if (result?.good == true) green else Color.rgb(255, 110, 110))
        }
        container.addView(demoButton)
        container.addView(preview)

        container.addView(label(
            "IMPORTANTE: Ainda precisa de teste real com ofertas da Uber/99 " +
            "e instalação de um APK compilado. Não utilize para tomar decisões " +
            "enquanto dirige. As permissões não são ativadas automaticamente.", 14f
        ))

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(container)
        })
    }
    /**
     * Variante de demonstração SEM AccessibilityService, leitura de outras telas,
     * janela sobre outros aplicativos, rede ou permissão sensível. 
     */
    private fun exibirDemoSemPermissoes() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 42, 32, 32)
            setBackgroundColor(background)
        }
        fun texto(msg: String, tamanho: Float = 16f, cor: Int = foreground) =
            TextView(this).apply {
                text = msg
                textSize = tamanho
                setTextColor(cor)
                setPadding(0, 0, 0, 18)
            }
        container.addView(texto("Meu Uber/99 Pro", 28f, green))
        container.addView(texto("DEMONSTRAÇÃO SEM ACESSO ÀS TELAS", 18f, green))
        container.addView(texto(
            "Esta versão permite testar os cálculos com valores digitados. " +
                "Não lê a Uber ou a 99, não abre janelas sobre outros aplicativos " +
                "e não solicita acesso às informações do celular. " +
                "A função automática continua em estudo, separadamente.", 15f
        ))
        val campos = mutableListOf<EditText>()
        fun campo(rotulo: String, exemplo: String) {
            container.addView(texto(rotulo, 14f))
            val input = EditText(this).apply {
                setText(exemplo)
                inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
                setTextColor(foreground)
                setHintTextColor(Color.LTGRAY)
                setSingleLine(true)
                setSelectAllOnFocus(true)
            }
            container.addView(input)
            campos.add(input)
        }
        campo("Valor oferecido (R$)", "30")
        campo("Distância até o passageiro (km)", "2")
        campo("Distância da viagem (km)", "8")
        campo("Tempo até o passageiro (min)", "5")
        campo("Tempo da viagem (min)", "25")
        val resultado = texto("Toque em Analisar para fazer uma simulação.", 18f)
        val analisar = Button(this).apply {
            text = "Analisar corrida de teste"
            isAllCaps = false
            setBackgroundColor(green)
            setTextColor(Color.BLACK)
        }
        analisar.setOnClickListener {
            val numeros = campos.map {
                it.text.toString().trim().replace(",", ".").toDoubleOrNull()
            }
            val oferta = if (numeros.all { it != null }) {
                OfferEngine.Offer(
                    fare = numeros[0]!!,
                    pickupKm = numeros[1]!!,
                    tripKm = numeros[2]!!,
                    pickupMinutes = numeros[3]!!,
                    tripMinutes = numeros[4]!!
                )
            } else null
            val avaliacao = oferta?.let { OfferEngine.evaluate(it) }
            resultado.text = if (avaliacao == null) {
                "Confira os valores. A oferta precisa ter preço, distância e tempo válidos."
            } else {
                avaliacao.label + "\n" +
                    OfferEngine.brl(avaliacao.grossPerHour) + "/h de faturamento bruto\n" +
                    OfferEngine.brl(avaliacao.netPerHour) + "/h após os custos\n" +
                    OfferEngine.brl(avaliacao.netPerKm) + "/km após os custos"
            }
            val destaque = if (avaliacao?.good == true) green else
                Color.rgb(255, 110, 110)
            resultado.setTextColor(destaque)
            resultado.setPadding(16, 22, 16, 22)
            resultado.background = GradientDrawable().apply {
                setColor(Color.rgb(16, 32, 20))
                cornerRadius = 16f
                setStroke(3, destaque)
            }
        }
        container.addView(analisar)
        container.addView(resultado)
        container.addView(texto(
            "Referência inicial: R$ 40/h brutos, R$ 25/h de sobra e R$ 1/km " +
                "de sobra, considerando etanol, desgaste e rateio do financiamento " +
                "do Sandero. Esses valores são provisórios.", 14f
        ))
        container.addView(texto(
            "Esta é uma demonstração de cálculo, não um aplicativo de leitura automática. " +
                "Use somente com o veículo parado. Não tente desativar o Play Protect " +
                "para instalar esta ou qualquer outra versão.", 13f
        ))
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(container)
        })
    }

}
