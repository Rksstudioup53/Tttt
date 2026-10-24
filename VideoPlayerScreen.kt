package com.hindivideo.translator.ui

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hindivideo.translator.pipeline.AndroidTtsEngine
import com.hindivideo.translator.pipeline.SubtitleCue
import kotlinx.coroutines.delay

enum class AudioTrackMode {
    ORIGINAL_AUDIO,     // मूल ऑडियो (Original sound from video)
    HINDI_SUBTITLES,    // हिंदी उपशीर्षक (Subtitles only, video sound normal)
    HINDI_DUBBED_AUDIO  // हिंदी डब ऑडियो (Android TTS Hindi dub with audio ducking)
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    videoUri: Uri?,
    subtitles: List<SubtitleCue>,
    isTranslating: Boolean,
    ttsEngine: AndroidTtsEngine,
    onSelectVideoClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onUpdateSubtitles: (List<SubtitleCue>) -> Unit
) {
    val context = LocalContext.current
    var audioMode by remember { mutableStateOf(AudioTrackMode.HINDI_DUBBED_AUDIO) }
    var currentPlaybackPosition by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var activeSubtitle by remember { mutableStateOf<SubtitleCue?>(null) }
    var lastSpokenCueId by remember { mutableIntStateOf(-1) }

    // ExoPlayer lifecycle instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(videoUri) {
        if (videoUri != null) {
            val mediaItem = MediaItem.fromUri(videoUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
        onDispose {
            exoPlayer.release()
            ttsEngine.stop()
        }
    }

    // Audio mode ducking & volume control logic
    LaunchedEffect(audioMode) {
        when (audioMode) {
            AudioTrackMode.ORIGINAL_AUDIO -> {
                exoPlayer.volume = 1.0f
                ttsEngine.stop()
            }
            AudioTrackMode.HINDI_SUBTITLES -> {
                exoPlayer.volume = 1.0f
                ttsEngine.stop()
            }
            AudioTrackMode.HINDI_DUBBED_AUDIO -> {
                // Audio ducking: lower original video volume so Hindi TTS speech is crisp & clear
                exoPlayer.volume = 0.15f
            }
        }
    }

    // Playback time polling loop for subtitle sync and TTS dubbing
    LaunchedEffect(exoPlayer, subtitles, audioMode) {
        while (true) {
            if (exoPlayer.isPlaying) {
                val currentSeconds = exoPlayer.currentPosition / 1000f
                currentPlaybackPosition = currentSeconds
                isPlaying = true

                // Match current subtitle cue
                val matchingCue = subtitles.firstOrNull {
                    currentSeconds >= it.startTime && currentSeconds <= it.endTime
                }
                activeSubtitle = matchingCue

                // If in Hindi Dubbed Audio mode, speak newly encountered subtitle
                if (audioMode == AudioTrackMode.HINDI_DUBBED_AUDIO && matchingCue != null) {
                    if (matchingCue.id != lastSpokenCueId) {
                        lastSpokenCueId = matchingCue.id
                        ttsEngine.speak(matchingCue.hindiText)
                    }
                } else if (matchingCue == null) {
                    // Reset cue tracker when in between subtitles
                    lastSpokenCueId = -1
                }
            } else {
                isPlaying = false
            }
            delay(100)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "हिंदी वीडियो अनुवादक",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Android Native (ExoPlayer + Compose)",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ExoPlayer Video View Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (videoUri != null) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Subtitle Overlay (When enabled)
                if (audioMode != AudioTrackMode.ORIGINAL_AUDIO && activeSubtitle != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 28.dp, start = 16.dp, end = 16.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = activeSubtitle!!.hindiText,
                            color = Color(0xFFFDE047), // Soft yellow for subtitles
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "स्थानीय वीडियो चुनने के लिए नीचे क्लिक करें",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean Hindi Action Buttons (As explicitly requested by user)
        Text(
            text = "क्रियाएं और नियंत्रण (Actions & Controls)",
            color = Color(0xFFCBD5E1),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Primary Buttons: Select Video & Translate to Hindi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSelectVideoClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("वीडियो चुनें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onTranslateClick,
                enabled = !isTranslating,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("हिंदी में अनुवाद करें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Audio Track Mode Switcher (Original Audio, Hindi Subtitles, Hindi Dubbed Audio)
        Text(
            text = "ऑडियो और उपशीर्षक मोड (Audio & Subtitle Mode):",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = audioMode == AudioTrackMode.ORIGINAL_AUDIO,
                onClick = { audioMode = AudioTrackMode.ORIGINAL_AUDIO },
                label = { Text("मूल ऑडियो", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )

            FilterChip(
                selected = audioMode == AudioTrackMode.HINDI_SUBTITLES,
                onClick = { audioMode = AudioTrackMode.HINDI_SUBTITLES },
                label = { Text("हिंदी उपशीर्षक", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )

            FilterChip(
                selected = audioMode == AudioTrackMode.HINDI_DUBBED_AUDIO,
                onClick = { audioMode = AudioTrackMode.HINDI_DUBBED_AUDIO },
                label = { Text("हिंदी डब ऑडियो", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Synchronized Subtitle Timeline List
        Text(
            text = "समकालिक उपशीर्षक टाइमलाइन (${subtitles.size} खंड)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(subtitles) { cue ->
                val isCurrent = activeSubtitle?.id == cue.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrent) Color(0xFF334155) else Color.Transparent)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${String.format("%.1f", cue.startTime)}s - ${String.format("%.1f", cue.endTime)}s",
                            color = if (isCurrent) Color(0xFFF97316) else Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = cue.hindiText,
                            color = if (isCurrent) Color.White else Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    IconButton(
                        onClick = { ttsEngine.speak(cue.hindiText) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Test Hindi Speech",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}