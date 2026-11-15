package br.com.meuuber99pro.copiloto

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
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

        val prefs = getSharedPreferences("copiloto", MODE_PRIVATE)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 42, 32, 32)
            setBackgroundColor(background)
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
            setTextColor(foreground)
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
}
