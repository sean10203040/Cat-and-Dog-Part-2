package com.seanfarmer.catdog

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val worker = Executors.newSingleThreadExecutor()
    private var classifier: CatDogClassifier? = null
    private var ready = false
    private var busy = false
    private var selectedUri: Uri? = null
    private var previewBitmap: Bitmap? = null
    private lateinit var preview: ImageView
    private lateinit var status: TextView
    private lateinit var dog: TextView
    private lateinit var cat: TextView
    private lateinit var choose: Button
    private lateinit var progress: ProgressBar

    private val picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            try {
                contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { /* Some providers only grant temporary access. */ }
            if (ready) classifyPhoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedUri = savedInstanceState?.getString("photo_uri")?.let(Uri::parse)
        buildUi()
        worker.execute {
            try {
                classifier = CatDogClassifier(applicationContext)
                runOnUiThread {
                    if (!isDestroyed && !isFinishing) {
                        ready = true
                        status.text = "Choose a photo of a cat or dog."
                        choose.isEnabled = true
                        selectedUri?.let(::classifyPhoto)
                    }
                }
            } catch (error: Exception) {
                runOnUiThread {
                    if (!isDestroyed && !isFinishing) {
                        status.text = "Model could not load. Add your Part 1 cat_dog.tflite file to app/src/main/assets and rebuild.\n\n${error.message}"
                    }
                }
            }
        }
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(32))
            setBackgroundColor(Color.rgb(246, 249, 251))
        }
        // Respect system bars on Android 15+, including edge-to-edge enforcement.
        scroll.setOnApplyWindowInsetsListener { view, insets ->
            @Suppress("DEPRECATION")
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        fun text(value: String, size: Float) = TextView(this).apply {
            text = value; textSize = size; setTextColor(Color.rgb(25, 52, 75))
            setPadding(0, dp(8), 0, dp(8))
        }
        body.addView(text("Cat or Dog?", 30f).apply { setTypeface(null, Typeface.BOLD) })
        body.addView(text("Select a photo to see both predictions.", 16f))
        preview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Selected photo"
            setBackgroundColor(Color.WHITE)
        }
        body.addView(preview, LinearLayout.LayoutParams(-1, dp(260)))
        choose = Button(this).apply {
            text = "Choose photo"; isEnabled = false
            setOnClickListener {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
        body.addView(choose, LinearLayout.LayoutParams(-1, -2))
        progress = ProgressBar(this).apply { visibility = View.GONE }
        body.addView(progress, LinearLayout.LayoutParams(dp(40), dp(40)).apply { gravity = Gravity.CENTER_HORIZONTAL })
        dog = text("Dog --%", 26f)
        cat = text("Cat --%", 26f)
        body.addView(dog); body.addView(cat)
        status = text("Loading model...", 15f).apply {
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        body.addView(status)
        body.addView(text("These are model predictions, not guaranteed identifications. Use photos containing one cat or one dog.", 13f))
        scroll.addView(body); setContentView(scroll)
    }

    private fun classifyPhoto(uri: Uri) {
        if (busy || !ready) return
        busy = true; choose.isEnabled = false; progress.visibility = View.VISIBLE
        dog.text = "Dog --%"; cat.text = "Cat --%"; status.text = "Classifying..."
        // Clear stale preview so a new error is never paired with an old prediction.
        preview.setImageDrawable(null); previewBitmap?.recycle(); previewBitmap = null
        worker.execute {
            var bitmap: Bitmap? = null
            try {
                val source = ImageDecoder.createSource(contentResolver, uri)
                val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                    // Bound memory for large phone photos; ImageDecoder handles orientation.
                    val ratio = max(info.size.width, info.size.height) / 2048f
                    if (ratio > 1f) decoder.setTargetSize(
                        max(1, (info.size.width / ratio).roundToInt()),
                        max(1, (info.size.height / ratio).roundToInt()))
                }
                bitmap = decoded
                val start = System.nanoTime()
                val result = checkNotNull(classifier).classify(decoded)
                val elapsedMs = (System.nanoTime() - start) / 1_000_000
                runOnUiThread {
                    if (isDestroyed || isFinishing) { decoded.recycle(); return@runOnUiThread }
                    previewBitmap = decoded; preview.setImageBitmap(decoded)
                    dog.text = String.format(Locale.US, "Dog %.1f%%", result.first)
                    cat.text = String.format(Locale.US, "Cat %.1f%%", result.second)
                    status.text = "${if (result.first >= result.second) "Dog" else "Cat"} is the higher prediction. Processed in ${elapsedMs} ms."
                    busy = false; choose.isEnabled = true; progress.visibility = View.GONE
                }
            } catch (error: Exception) {
                bitmap?.recycle()
                runOnUiThread {
                    if (!isDestroyed && !isFinishing) {
                        busy = false; choose.isEnabled = true; progress.visibility = View.GONE
                        status.text = "Could not classify this photo. Try another image.\n${error.message}"
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selectedUri?.let { outState.putString("photo_uri", it.toString()) }
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        // Queue cleanup after pending inference; Interpreter is not thread-safe.
        worker.execute { classifier?.close(); classifier = null }
        worker.shutdown()
        preview.setImageDrawable(null); previewBitmap?.recycle(); previewBitmap = null
        super.onDestroy()
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()
}
