package com.hindivideo.translator

import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.hindivideo.translator.pipeline.AndroidTtsEngine
import com.hindivideo.translator.pipeline.ModularTranslationPipeline
import com.hindivideo.translator.pipeline.SubtitleCue
import com.hindivideo.translator.ui.VideoPlayerScreen
import com.hindivideo.translator.ui.theme.HindiVideoTranslatorTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var ttsEngine: AndroidTtsEngine
    private lateinit var translationPipeline: ModularTranslationPipeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Android TextToSpeech engine with Hindi locale
        ttsEngine = AndroidTtsEngine(this) { success ->
            if (!success) {
                Toast.makeText(
                    this,
                    "Hindi voice not installed on device. Please download Hindi voice data in Android Settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Initialize modular translation pipeline
        translationPipeline = ModularTranslationPipeline(ttsEngine)

        setContent {
            HindiVideoTranslatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
                    var subtitles by remember { mutableStateOf(getSampleHindiSubtitles()) }
                    var isTranslating by remember { mutableStateOf(false) }

                    // Local video selection launcher using Android's system Photo & Video Picker
                    val videoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let {
                            selectedVideoUri = it
                            Toast.makeText(this, "वीडियो लोड हो गया (Video Loaded)", Toast.LENGTH_SHORT).show()
                        }
                    }

                    VideoPlayerScreen(
                        videoUri = selectedVideoUri,
                        subtitles = subtitles,
                        isTranslating = isTranslating,
                        ttsEngine = ttsEngine,
                        onSelectVideoClick = {
                            videoPickerLauncher.launch("video/*")
                        },
                        onTranslateClick = {
                            lifecycleScope.launch {
                                isTranslating = true
                                try {
                                    val newSubtitles = translationPipeline.translateCurrentVideo(
                                        videoUri = selectedVideoUri,
                                        existingCues = subtitles
                                    )
                                    subtitles = newSubtitles
                                    Toast.makeText(
                                        this@MainActivity,
                                        "हिंदी अनुवाद पूरा हुआ! (Translation Complete)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "अनुवाद त्रुटि: ${e.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isTranslating = false
                                }
                            }
                        },
                        onUpdateSubtitles = { updated ->
                            subtitles = updated
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsEngine.shutdown()
    }

    private fun getSampleHindiSubtitles(): List<SubtitleCue> {
        return listOf(
            SubtitleCue(
                id = 1,
                startTime = 0.5f,
                endTime = 4.0f,
                hindiText = "नमस्ते! हिंदी वीडियो अनुवादक में आपका स्वागत है।",
                originalText = "Hello! Welcome to Hindi Video Translator."
            ),
            SubtitleCue(
                id = 2,
                startTime = 4.2f,
                endTime = 8.5f,
                hindiText = "आप अपने फोन स्टोरेज से कोई भी स्थानीय वीडियो चुन सकते हैं।",
                originalText = "You can select any local video from your phone storage."
            ),
            SubtitleCue(
                id = 3,
                startTime = 8.8f,
                endTime = 13.5f,
                hindiText = "यह ऐप समकालिक उपशीर्षक और हिंदी डबिंग प्रदान करता है।",
                originalText = "This app provides synchronized subtitles and Hindi dubbing."
            )
        )
    }
}