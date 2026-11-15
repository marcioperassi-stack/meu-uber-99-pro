package br.com.meuuber99pro.copiloto

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

/**
 * Protótipo de leitura local. Não clica, não aceita, não recusa, não captura a tela,
 * não salva conteúdos e não utiliza rede. Lê apenas nós de texto acessíveis.
 */
class OfferReaderService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val allowedPackages = setOf("com.ubercab.driver", "com.app99.driver")
    private var overlay: View? = null
    private var lastSignature = ""
    private var lastShownAt = 0L
    private val dismiss = Runnable { hideOverlay() }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val prefs = getSharedPreferences("copiloto", MODE_PRIVATE)
        if (!prefs.getBoolean("consentimento", false)) {
            hideOverlay()
            return
        }
        if (event.packageName?.toString() !in allowedPackages) {
            hideOverlay()
            return
        }
        val root = rootInActiveWindow ?: return
        val lines = mutableListOf<String>()
        collectText(root, lines)
        val offer = OfferEngine.parseVisibleText(lines.joinToString("\n")) ?: return
        val result = OfferEngine.evaluate(offer) ?: return
        val signature = listOf(offer.fare, offer.pickupKm, offer.tripKm,
            offer.pickupMinutes, offer.tripMinutes).joinToString("|")
        val now = SystemClock.elapsedRealtime()
        if (signature == lastSignature && now - lastShownAt < 5_000L) return
        lastSignature = signature
        lastShownAt = now
        showOverlay(result)
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableList<String>) {
        if (out.size >= 100) return
        val value = (node.text ?: node.contentDescription)?.toString()?.trim()
        if (!value.isNullOrBlank()) out.add(value.take(200))
        for (index in 0 until node.childCount) {
            if (out.size >= 100) break
            node.getChild(index)?.let { collectText(it, out) }
        }
    }

    private fun showOverlay(result: OfferEngine.Assessment) {
        hideOverlay()
        val color = if (result.good) Color.rgb(65, 229, 126) else Color.rgb(255, 100, 113)
        val title = result.label
        val details = OfferEngine.brl(result.grossPerHour) + "/h bruto · " +
            OfferEngine.brl(result.netPerHour) + "/h após custos"

        val box = TextView(this).apply {
            text = title + "\n" + details
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(26, 14, 26, 14)
            background = GradientDrawable().apply {
                setColor(Color.rgb(8, 17, 12))
                cornerRadius = 22f
                setStroke(5, color)
            }
            contentDescription = title + ". " + details
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 100
        }
        try {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.addView(box, params)
            overlay = box
            handler.postDelayed(dismiss, 8_000L)
        } catch (_: Exception) {
            overlay = null
        }
    }

    private fun hideOverlay() {
        handler.removeCallbacks(dismiss)
        overlay?.let { view ->
            try {
                (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(view)
            } catch (_: Exception) {}
        }
        overlay = null
    }

    override fun onInterrupt() = hideOverlay()

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }
}
