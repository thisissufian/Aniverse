package com.aniverse.app.ui.screens.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.util.Rational
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.local.AppPreferences
import com.aniverse.app.data.model.Anime
import com.aniverse.app.data.model.Episode
import com.aniverse.app.data.model.ServerItem
import com.aniverse.app.data.model.SkipInterval
import com.aniverse.app.data.remote.AnikotoResolver
import com.aniverse.app.ui.theme.*
import com.aniverse.app.util.PipController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ENHANCED_BRIDGE_JS: String = """
!function(){
    "use strict";
    try {
        Object.defineProperty(document, 'pictureInPictureEnabled', {
            get: function() { return true; },
            configurable: true
        });
        if (window.HTMLVideoElement && !HTMLVideoElement.prototype.__aniversePipPatched) {
            HTMLVideoElement.prototype.__aniversePipPatched = true;
            HTMLVideoElement.prototype.requestPictureInPicture = function() {
                try {
                    window.parent.postMessage({ megaCommand: 'pip' }, '*');
                } catch(e) {}
                return Promise.resolve();
            };
        }
    } catch(e) {}

    function isPipTarget(el) {
        if (!el || !el.closest) return false;
        return !!el.closest(".jw-icon-pip, .jw-svg-icon-pip, .jw-icon-floating, .jw-svg-icon-floating, [aria-label*='Picture in Picture'], [aria-label*='Picture in picture'], [aria-label*='PiP'], [aria-label*='pip'], [aria-label*='Floating'], [aria-label*='floating'], [aria-label*='Mini'], [aria-label*='mini'], .art-control-pip, [data-index='pip'], button[title*='Picture in Picture'], button[title*='PiP'], button[title*='Mini']");
    }

    document.addEventListener("click", function(e) {
        if (isPipTarget(e.target)) {
            e.preventDefault();
            e.stopPropagation();
            if (e.stopImmediatePropagation) e.stopImmediatePropagation();
            try {
                window.parent.postMessage({ megaCommand: 'pip' }, '*');
            } catch(err) {}
        }
    }, true);

    if(!window.__megaEmbedFsBound&&(window.__megaEmbedFsBound=!0,window.parent!==window&&(e=navigator.userAgent||"",t=/iP(ad|hone|od)/.test(e)||-1!==e.indexOf("Mac")&&navigator.maxTouchPoints>1,n=/AppleWebKit/.test(e)&&!/Chrome|CriOS|Chromium|Edg|OPR|Android/.test(e),!t&&!n))){var e,t,n,r=!1,o=!1,a=0,i=0,c=!1;window.addEventListener("message",function(e){if(e.source===window.parent){var t=function(e){if(null==e)return null;if("object"==typeof e)return e;if("string"==typeof e)try{return JSON.parse(e)}catch(e){return null}return null}(e.data);t&&"boolean"==typeof t.megaFullscreenState&&w(t.megaFullscreenState)}}),s({megaCommand:"getFullscreenState"})}function l(e){if(!e||"string"!=typeof e)return!1;try{return-1!==new URL(e).hostname.toLowerCase().indexOf("anikoto")}catch(e){return!1}}function u(){if("function"!=typeof jwplayer)return null;try{var e=jwplayer("megaplay-player");if(e&&"function"==typeof e.getContainer)return e;if((e=jwplayer())&&"function"==typeof e.getContainer)return e}catch(e){}return null}function s(e){try{window.parent.postMessage(e,"*")}catch(e){}}function f(e){o=!!e,function(){if(!document.getElementById("mega-fs-icon-style")){var e=document.createElement("style");e.id="mega-fs-icon-style",e.textContent=".jwplayer.mega-parent-fs .jw-svg-icon-fullscreen-on{display:none!important}.jwplayer.mega-parent-fs .jw-svg-icon-fullscreen-off{display:block!important}",(document.head||document.documentElement).appendChild(e)}}();var t=function(){var e=u(),t=null;try{t=e&&e.getContainer&&e.getContainer()}catch(e){}return t||(t=document.getElementById("megaplay-player")),t}();t&&t.classList.toggle("mega-parent-fs",o);var n=u();try{n&&"function"==typeof n.resize&&n.resize()}catch(e){}}function d(){var e=u();if(e&&!e.__megaFsApiPatched){e.__megaFsApiPatched=!0;var t=e.setFullscreen,n=e.getFullscreen;"function"==typeof t&&(e.setFullscreen=function(){return r?e:t.apply(e,arguments)}),"function"==typeof n&&(e.getFullscreen=function(){return r?o:n.call(e)});try{"function"!=typeof e.on||e.__megaFsEvtBound||(e.__megaFsEvtBound=!0,e.on("ready",function(){r&&f(o)}))}catch(e){}}}function p(e){return!(!e||!e.closest)&&!!e.closest(".jw-icon-fullscreen, .jw-svg-icon-fullscreen, .jw-svg-icon-fullscreen-on, .jw-svg-icon-fullscreen-off, [aria-label='Fullscreen'], [aria-label='Exit Fullscreen'], [aria-label='Exit fullscreen']")}function g(e){return!(!e||!e.closest)&&(!p(e)&&!function(e){return!(!e||!e.closest||!e.closest(".jw-controls, .jw-controlbar, .jw-button-container, .jw-settings-menu, .jw-skip, .jw-tooltip, .botright, #overlay-control-megaplay a"))}(e)&&!!e.closest("#megaplay-player, .jwplayer, .jw-wrapper, .jw-media, .jw-overlays, .jw-display, video, canvas"))}function m(){var e;(e=Date.now())-a<450||(a=e,0)||(f(!o),s({megaCommand:"toggleFullscreen"}))}function y(e){e.preventDefault(),e.stopPropagation(),e.stopImmediatePropagation&&e.stopImmediatePropagation()}function w(e){r=!0,d(),c||(c=!0,document.addEventListener("click",function(e){if(r)return p(e.target)?(y(e),void m()):void(g(e.target)&&2===e.detail&&(y(e),m()))},!0),document.addEventListener("dblclick",function(e){r&&g(e.target)&&y(e)},!0),document.addEventListener("pointerup",function(e){if(r&&"mouse"!==e.pointerType&&g(e.target)){var t=Date.now();if(t-i<320)return y(e),i=0,void m();i=t}},!0),document.addEventListener("keydown",function(e){if(r){var t=e.target;t&&-1!==["INPUT","TEXTAREA","SELECT"].indexOf(t.tagName)||"f"!==e.key&&"F"!==e.key||e.repeat||(e.preventDefault(),e.stopImmediatePropagation(),m())}},!0)),f(e),setTimeout(d,500),setTimeout(d,2e3)}
}();
"""

class WebAppInterface(
    private val onToggleFullscreen: () -> Unit,
    private val isCurrentFullscreen: () -> Boolean,
    private val onPlaybackChanged: (Boolean) -> Unit,
    private val onTimeUpdate: (Long, Long) -> Unit = { _, _ -> },
    private val onTriggerMiniPlayer: () -> Unit = {},
    private val canTriggerMiniPlayer: () -> Boolean = { true }
) {
    @JavascriptInterface
    fun toggleFullscreen() {
        onToggleFullscreen()
    }

    @JavascriptInterface
    fun isFullscreen(): Boolean {
        return isCurrentFullscreen()
    }

    @JavascriptInterface
    fun setPlaybackState(isPlaying: Boolean) {
        onPlaybackChanged(isPlaying)
    }

    @JavascriptInterface
    fun onTimeUpdate(positionSec: Float, durationSec: Float) {
        onTimeUpdate((positionSec * 1000).toLong(), (durationSec * 1000).toLong())
    }

    @JavascriptInterface
    fun triggerMiniPlayer() {
        if (canTriggerMiniPlayer()) {
            onTriggerMiniPlayer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    animeId: Int,
    animeTitle: String,
    englishTitle: String? = null,
    coverUrl: String = "",
    format: String? = "TV",
    isMinimized: Boolean = false,
    onMinimize: () -> Unit = {},
    onExpand: () -> Unit = {},
    onClose: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val dbHelper = remember { AniverseDbHelper.getInstance(context) }
    val appPrefs = remember { AppPreferences.getInstance(context) }
    val resolver = remember { AnikotoResolver() }

    // State
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var currentEpisode by remember { mutableStateOf<Episode?>(null) }
    var servers by remember { mutableStateOf<List<ServerItem>>(emptyList()) }
    var selectedServer by remember { mutableStateOf<ServerItem?>(null) }
    var activeAudioTab by remember { mutableStateOf(if (appPrefs.preferDub) "dub" else "sub") }
    var isBookmarked by remember { mutableStateOf(false) }

    var isFullscreen by remember { mutableStateOf(false) }
    var isOrientationLocked by remember { mutableStateOf(false) }
    var showControlsOverlay by remember { mutableStateOf(true) }

    var embedUrl by remember { mutableStateOf<String?>(null) }
    var introInterval by remember { mutableStateOf<SkipInterval?>(null) }
    var outroInterval by remember { mutableStateOf<SkipInterval?>(null) }

    var isLoadingEpisodes by remember { mutableStateOf(true) }
    var episodesError by remember { mutableStateOf<String?>(null) }

    var isLoadingStream by remember { mutableStateOf(false) }
    var streamError by remember { mutableStateOf<String?>(null) }

    // Chunking for large series (30 episodes per tab)
    val chunkSize = 30
    var selectedChunkIndex by remember { mutableStateOf(0) }

    // Gesture feedback
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var volumeFeedback by remember { mutableStateOf<Float?>(null) }
    var brightnessFeedback by remember { mutableStateOf<Float?>(null) }
    var lockFeedbackText by remember { mutableStateOf<String?>(null) }
    var currentPlayPositionMs by remember { mutableLongStateOf(0L) }
    var totalPlayDurationMs by remember { mutableLongStateOf(1440000L) }
    var resumeBannerText by remember { mutableStateOf<String?>(null) }
    var streamLoadStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastLoadedEmbedUrl by remember { mutableStateOf<String?>(null) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    // ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    fun setFullscreenMode(enable: Boolean) {
        isFullscreen = enable
        activity?.let { act ->
            val window = act.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (enable) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                isOrientationLocked = false
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        // Notify iframe player of fullscreen state change
        webViewInstance?.evaluateJavascript(
            """
            (function() {
                var iframe = document.getElementById('player-frame');
                if (iframe && iframe.contentWindow) {
                    try {
                        iframe.contentWindow.postMessage({ megaFullscreenState: $enable }, '*');
                    } catch(e) {}
                }
            })();
            """.trimIndent(),
            null
        )
    }

    // Auto-Orientation Detection
    val orientationListener = remember {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                // If minimized to floating mini-player, in system PiP, or locked by user, do NOT rotate automatically
                if (isMinimized || PipController.isInPipMode || isOrientationLocked) return

                val isLandscape = (orientation in 65..115) || (orientation in 245..295)
                val isPortrait = (orientation in 340..360) || (orientation in 0..20) || (orientation in 160..200)

                if (isLandscape && !isFullscreen) {
                    setFullscreenMode(true)
                } else if (isPortrait && isFullscreen) {
                    setFullscreenMode(false)
                }
            }
        }
    }

    DisposableEffect(orientationListener) {
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
        onDispose {
            orientationListener.disable()
        }
    }

    // Manage PiP & NowPlaying Active state
    DisposableEffect(Unit) {
        PipController.isPlayerActive = true
        onDispose {
            PipController.isPlayerActive = false
            PipController.isVideoPlaying = false
            com.aniverse.app.util.NowPlayingController.setPlaybackState(false)
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.getInsetsController(act.window, act.window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
            exoPlayer.release()
        }
    }

    // Handle back button (exits fullscreen or minimizes to floating mini-player)
    BackHandler(enabled = !isMinimized && !PipController.isInPipMode) {
        if (isFullscreen) {
            setFullscreenMode(false)
        } else {
            onMinimize()
        }
    }

    fun togglePlayPause() {
        val isDirectStream = embedUrl?.endsWith(".m3u8") == true || embedUrl?.endsWith(".mp4") == true
        if (isDirectStream) {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                PipController.isVideoPlaying = false
            } else {
                exoPlayer.play()
                PipController.isVideoPlaying = true
            }
        } else {
            val newState = !PipController.isVideoPlaying
            PipController.isVideoPlaying = newState
            val jsCmd = if (newState) "play" else "pause"
            webViewInstance?.evaluateJavascript(
                """
                (function() {
                    var iframe = document.getElementById('player-frame');
                    if (iframe && iframe.contentWindow) {
                        try {
                            iframe.contentWindow.postMessage({ command: '$jsCmd' }, '*');
                            iframe.contentWindow.postMessage({ event: '$jsCmd' }, '*');
                            iframe.contentWindow.postMessage({ type: '$jsCmd' }, '*');
                        } catch(e) {}
                    }
                })();
                """.trimIndent(),
                null
            )
        }
        com.aniverse.app.util.NowPlayingController.setPlaybackState(PipController.isVideoPlaying)
    }

    LaunchedEffect(animeId) {
        isBookmarked = dbHelper.isInWatchlist(animeId)
    }

    // Stream resolution function
    fun loadStreamForEpisode(episode: Episode, preferredServer: ServerItem? = null) {
        scope.launch {
            streamLoadStartTime = System.currentTimeMillis()
            isLoadingStream = true
            streamError = null
            embedUrl = null
            try {
                val dataIds = episode.dataIds
                if (dataIds.isNullOrEmpty()) {
                    streamError = "No stream data available for Episode ${episode.number}"
                    return@launch
                }

                val serverList = resolver.getAvailableServers(dataIds)
                servers = serverList

                if (serverList.isEmpty()) {
                    streamError = "No servers found for Episode ${episode.number}"
                    return@launch
                }

                val target = preferredServer
                    ?: serverList.find { it.type == activeAudioTab && (it.name.contains("HD-1", true) || it.name.contains("Vidstream", true)) }
                    ?: serverList.find { it.type == activeAudioTab }
                    ?: serverList.first()

                selectedServer = target

                val embedResult = resolver.getServerEmbed(target.linkId)
                if (embedResult != null && embedResult.embedUrl.isNotEmpty()) {
                    embedUrl = embedResult.embedUrl
                    introInterval = embedResult.intro
                    outroInterval = embedResult.outro

                    // Mark video playing for PiP as soon as stream resolves
                    PipController.isVideoPlaying = true

                    if (embedResult.embedUrl.endsWith(".m3u8") || embedResult.embedUrl.endsWith(".mp4")) {
                        exoPlayer.setMediaItem(MediaItem.fromUri(embedResult.embedUrl))
                        exoPlayer.prepare()
                        if (currentPlayPositionMs > 2000L) {
                            exoPlayer.seekTo(currentPlayPositionMs)
                        }
                    }
                } else {
                    streamError = "Could not retrieve streaming link from ${target.name}"
                }
            } catch (e: Exception) {
                streamError = e.message ?: "Failed to connect to streaming server"
            } finally {
                isLoadingStream = false
            }
        }
    }

    // Episode list resolution function
    fun fetchEpisodesList() {
        scope.launch {
            isLoadingEpisodes = true
            episodesError = null
            try {
                val list = resolver.getEpisodes(
                    primaryTitle = animeTitle,
                    secondaryTitle = englishTitle,
                    format = format
                )
                episodes = list
                if (list.isNotEmpty()) {
                    val history = dbHelper.getWatchHistory().find { it.animeId == animeId }
                    val initialEp = history?.let { h -> list.find { it.number == h.episodeNumber } } ?: list.first()
                    currentEpisode = initialEp
                    selectedChunkIndex = (initialEp.number - 1).coerceAtLeast(0) / chunkSize

                    if (history != null && history.episodeNumber == initialEp.number && history.positionMs > 2000L) {
                        currentPlayPositionMs = history.positionMs
                        totalPlayDurationMs = if (history.durationMs > 0) history.durationMs else 1440000L
                        resumeBannerText = "Resumed at ${com.aniverse.app.util.NowPlayingController.formatTimestamp(history.positionMs)}"
                    }
                } else {
                    episodesError = "No episodes found for '$animeTitle'."
                }
            } catch (e: Exception) {
                episodesError = e.message ?: "Error loading episode list"
            } finally {
                isLoadingEpisodes = false
            }
        }
    }

    LaunchedEffect(animeTitle) {
        fetchEpisodesList()
    }

    LaunchedEffect(currentEpisode) {
        currentEpisode?.let { ep ->
            val history = dbHelper.getWatchHistory().find { it.animeId == animeId && it.episodeNumber == ep.number }
            val pos = history?.positionMs ?: 0L
            val dur = if ((history?.durationMs ?: 0L) > 0) history!!.durationMs else 1440000L
            currentPlayPositionMs = pos
            totalPlayDurationMs = dur

            if (pos > 2000L) {
                resumeBannerText = "Resumed at ${com.aniverse.app.util.NowPlayingController.formatTimestamp(pos)}"
            }

            com.aniverse.app.util.NowPlayingController.setNowPlaying(
                animeId = animeId,
                title = animeTitle,
                englishTitle = englishTitle,
                coverUrl = coverUrl,
                format = format,
                episodeNumber = ep.number,
                episodeTitle = ep.title,
                positionMs = pos,
                durationMs = dur,
                isPlaying = true
            )

            loadStreamForEpisode(ep)
        }
    }

    // Auto-dismiss resume banner after 5s
    LaunchedEffect(resumeBannerText) {
        if (resumeBannerText != null) {
            delay(5000)
            resumeBannerText = null
        }
    }

    // Auto-hide controls overlay in fullscreen
    LaunchedEffect(showControlsOverlay, isFullscreen) {
        if (showControlsOverlay && isFullscreen) {
            delay(4000)
            showControlsOverlay = false
        }
    }

    // ExoPlayer progress & listeners
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED && appPrefs.autoPlayNext) {
                    val idx = episodes.indexOfFirst { it.number == currentEpisode?.number }
                    if (idx in 0 until episodes.size - 1) {
                        currentEpisode = episodes[idx + 1]
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                PipController.isVideoPlaying = isPlaying
                com.aniverse.app.util.NowPlayingController.setPlaybackState(isPlaying)
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Universal Playback Progress Tracker (ExoPlayer & WebView)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (PipController.isVideoPlaying) {
                val isDirectStream = embedUrl?.endsWith(".m3u8") == true || embedUrl?.endsWith(".mp4") == true
                if (isDirectStream) {
                    currentPlayPositionMs = exoPlayer.currentPosition
                    if (exoPlayer.duration > 1000) {
                        totalPlayDurationMs = exoPlayer.duration
                    }
                } else {
                    currentPlayPositionMs += 1000L
                }

                introInterval?.let { intro ->
                    if (appPrefs.autoSkipIntro && currentPlayPositionMs in intro.start..(intro.start + 2500)) {
                        val skipTarget = intro.end + 1000
                        currentPlayPositionMs = skipTarget
                        if (isDirectStream) exoPlayer.seekTo(skipTarget)
                    }
                }

                com.aniverse.app.util.NowPlayingController.updateProgress(currentPlayPositionMs, totalPlayDurationMs)

                if (currentPlayPositionMs > 1000) {
                    currentEpisode?.let { ep ->
                        dbHelper.saveProgress(
                            animeId = animeId,
                            title = englishTitle ?: animeTitle,
                            coverUrl = coverUrl,
                            episodeNum = ep.number,
                            positionMs = currentPlayPositionMs,
                            durationMs = totalPlayDurationMs
                        )
                    }
                }
            }
        }
    }

    val currentEpIndex = episodes.indexOfFirst { it.number == currentEpisode?.number }
    val hasPrevEpisode = currentEpIndex > 0
    val hasNextEpisode = currentEpIndex in 0 until episodes.size - 1

    // Video Player Surface
    @Composable
    fun VideoPlayerSurface(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .background(Color.Black)
                .clickable {
                    if (isMinimized) {
                        onExpand()
                    } else {
                        showControlsOverlay = !showControlsOverlay
                    }
                }
                .then(
                    if (!isMinimized) {
                        Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { offset ->
                                        val screenWidth = size.width
                                        if (offset.x < screenWidth / 2) {
                                            val newPos = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0)
                                            exoPlayer.seekTo(newPos)
                                            seekFeedbackText = "-10s"
                                        } else {
                                            val newPos = (exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration)
                                            exoPlayer.seekTo(newPos)
                                            seekFeedbackText = "+10s"
                                        }
                                        scope.launch {
                                            delay(800)
                                            seekFeedbackText = null
                                        }
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        val screenWidth = size.width
                                        val x = change.position.x
                                        if (x < screenWidth / 2) {
                                            activity?.window?.let { window ->
                                                val lp = window.attributes
                                                var currentBrightness = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                                                currentBrightness = (currentBrightness - (dragAmount / 800f)).coerceIn(0.05f, 1.0f)
                                                lp.screenBrightness = currentBrightness
                                                window.attributes = lp
                                                brightnessFeedback = currentBrightness
                                            }
                                        } else {
                                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                            val delta = if (dragAmount < 0) 1 else -1
                                            val newVol = (currentVol + delta).coerceIn(0, maxVol)
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                            volumeFeedback = newVol.toFloat() / maxVol
                                        }
                                    },
                                    onDragEnd = {
                                        scope.launch {
                                            delay(1000)
                                            volumeFeedback = null
                                            brightnessFeedback = null
                                        }
                                    }
                                )
                            }
                    } else Modifier
                )
        ) {
            val isDirectStream = embedUrl?.endsWith(".m3u8") == true || embedUrl?.endsWith(".mp4") == true

            if (isDirectStream) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            useController = true
                            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!embedUrl.isNullOrEmpty()) {
                val embedHtml = remember(embedUrl) {
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                        <meta name="referrer" content="always">
                        <style>
                            * { margin: 0; padding: 0; box-sizing: border-box; background: #000; }
                            html, body { width: 100%; height: 100%; overflow: hidden; background: #000; }
                            iframe { width: 100%; height: 100%; border: 0; display: block; }
                        </style>
                    </head>
                    <body>
                        <iframe 
                            id="player-frame"
                            src="$embedUrl" 
                            allow="autoplay; fullscreen; picture-in-picture; encrypted-media" 
                            allowfullscreen="true" 
                            referrerpolicy="unsafe-url">
                        </iframe>

                        <script>
                            var initialSeek = Math.floor(${(currentPlayPositionMs / 1000).coerceAtLeast(0)});
                            var iframe = document.getElementById('player-frame');
                            if (initialSeek > 2 && iframe) {
                                iframe.addEventListener('load', function() {
                                    setTimeout(function() {
                                        try {
                                            iframe.contentWindow.postMessage({ command: 'seek', time: initialSeek }, '*');
                                            iframe.contentWindow.postMessage({ event: 'seek', time: initialSeek }, '*');
                                        } catch(e) {}
                                    }, 1500);
                                });
                            }

                            window.addEventListener('message', function(event) {
                                var data = event.data;
                                if (typeof data === 'string') {
                                    try { data = JSON.parse(data); } catch(e) {}
                                }
                                if (!data || typeof data !== 'object') return;

                                // In-player mini player / PIP button trigger from player controls
                                var isPipTrigger = false;
                                if (data.megaCommand === 'pip' || data.megaCommand === 'togglePip' || data.megaCommand === 'miniPlayer' || data.megaCommand === 'mini' ||
                                    data.command === 'pip' || data.command === 'togglePip' || data.command === 'miniPlayer' || data.command === 'mini') {
                                    isPipTrigger = true;
                                }
                                // handle-bridge PIP_STATE event - MUST ONLY trigger when active/pip is TRUE
                                if ((data.event === 'PIP_STATE' || data.type === 'PIP_STATE') && (data.active === true || data.pip === true)) {
                                    isPipTrigger = true;
                                }
                                if (data.event === 'pipEnter' || data.event === 'enterpictureinpicture' ||
                                    data.type === 'pipEnter' || data.type === 'enterpictureinpicture') {
                                    isPipTrigger = true;
                                }

                                if (isPipTrigger) {
                                    if (window.AndroidBridge && window.AndroidBridge.triggerMiniPlayer) {
                                        window.AndroidBridge.triggerMiniPlayer();
                                    }
                                }

                                // Handle MegaPlay in-player fullscreen button command
                                if (data.megaCommand === 'toggleFullscreen') {
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.toggleFullscreen();
                                    }
                                } else if (data.megaCommand === 'getFullscreenState') {
                                    if (window.AndroidBridge && event.source) {
                                        var isFs = window.AndroidBridge.isFullscreen();
                                        try {
                                            event.source.postMessage({ megaFullscreenState: isFs }, '*');
                                        } catch(e) {}
                                    }
                                }

                                // Handle playback state
                                var evt = (data.event || data.type || '').toLowerCase();
                                if (evt === 'play' || evt === 'playing') {
                                    if (window.AndroidBridge) window.AndroidBridge.setPlaybackState(true);
                                } else if (evt === 'pause' || evt === 'ended' || evt === 'complete') {
                                    if (window.AndroidBridge) window.AndroidBridge.setPlaybackState(false);
                                }

                                // Handle time update
                                if (evt === 'time' || evt === 'timeupdate' || data.event === 'CURRENT_TIME') {
                                    var cur = data.currentTime || data.time || (data.data && data.data.time);
                                    var dur = data.duration || (data.data && data.data.duration);
                                    if (cur && window.AndroidBridge) {
                                        window.AndroidBridge.onTimeUpdate(cur, dur || 1440);
                                    }
                                }
                            });
                        </script>
                    </body>
                    </html>
                    """.trimIndent()
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                allowFileAccess = true
                                allowContentAccess = true
                            }

                            addJavascriptInterface(
                                WebAppInterface(
                                    onToggleFullscreen = {
                                        scope.launch(Dispatchers.Main) {
                                            setFullscreenMode(!isFullscreen)
                                        }
                                    },
                                    isCurrentFullscreen = { isFullscreen },
                                    onPlaybackChanged = { isPlaying ->
                                        scope.launch(Dispatchers.Main) {
                                            PipController.isVideoPlaying = isPlaying
                                            com.aniverse.app.util.NowPlayingController.setPlaybackState(isPlaying)
                                        }
                                    },
                                    onTimeUpdate = { posMs, durMs ->
                                        scope.launch(Dispatchers.Main) {
                                            currentPlayPositionMs = posMs
                                            if (durMs > 1000) totalPlayDurationMs = durMs
                                            com.aniverse.app.util.NowPlayingController.updateProgress(posMs, durMs)
                                        }
                                    },
                                    onTriggerMiniPlayer = {
                                        scope.launch(Dispatchers.Main) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                                try {
                                                    val params = PictureInPictureParams.Builder()
                                                        .setAspectRatio(Rational(16, 9))
                                                        .build()
                                                    val entered = activity.enterPictureInPictureMode(params)
                                                    if (!entered) {
                                                        onMinimize()
                                                    }
                                                } catch (e: Exception) {
                                                    onMinimize()
                                                }
                                            } else {
                                                onMinimize()
                                            }
                                        }
                                    },
                                    canTriggerMiniPlayer = {
                                        !isMinimized && !PipController.isInPipMode && (System.currentTimeMillis() - streamLoadStartTime > 1500L)
                                    }
                                ),
                                "AndroidBridge"
                            )

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    if (url.contains("megaplay") || url.contains("anikoto") || url.contains("tcdn") || url.contains("bcdn") || url.contains("stream")) {
                                        return false
                                    }
                                    return true
                                }

                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                    val url = request?.url?.toString() ?: return null
                                    if (url.contains("parent-fullscreen-bridge")) {
                                        return WebResourceResponse(
                                            "application/javascript",
                                            "UTF-8",
                                            ENHANCED_BRIDGE_JS.byteInputStream(Charsets.UTF_8)
                                        )
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                    super.onShowCustomView(view, callback)
                                    scope.launch(Dispatchers.Main) {
                                        setFullscreenMode(true)
                                    }
                                }

                                override fun onHideCustomView() {
                                    super.onHideCustomView()
                                    scope.launch(Dispatchers.Main) {
                                        setFullscreenMode(false)
                                    }
                                }
                            }

                            lastLoadedEmbedUrl = embedUrl
                            loadDataWithBaseURL("https://anikototv.to/", embedHtml, "text/html", "UTF-8", null)
                            webViewInstance = this
                        }
                    },
                    update = { webView ->
                        webViewInstance = webView
                        if (lastLoadedEmbedUrl != embedUrl && !embedUrl.isNullOrEmpty()) {
                            lastLoadedEmbedUrl = embedUrl
                            webView.loadDataWithBaseURL("https://anikototv.to/", embedHtml, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Stream Loading Overlay
            if (isLoadingStream) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryViolet, modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Resolving stream...",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Stream Error Overlay
            if (streamError != null && !isLoadingStream) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkCanvas.copy(alpha = 0.92f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = AccentRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = streamError ?: "Stream Unavailable",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { currentEpisode?.let { loadStreamForEpisode(it) } },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry", fontSize = 13.sp)
                            }

                            val altServer = servers.firstOrNull { it.linkId != selectedServer?.linkId }
                            if (altServer != null) {
                                OutlinedButton(
                                    onClick = {
                                        selectedServer = altServer
                                        currentEpisode?.let { loadStreamForEpisode(it, altServer) }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                                ) {
                                    Text("Try ${altServer.name}", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Controls Overlay (Only shown when expanded and not in system PiP)
            AnimatedVisibility(
                visible = !isMinimized && !PipController.isInPipMode && (showControlsOverlay || isFullscreen),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Bar (Only shown in Fullscreen Landscape mode with Back button to exit fullscreen)
                    if (isFullscreen) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { setFullscreenMode(false) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface.copy(alpha = 0.8f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit Fullscreen",
                                    tint = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = englishTitle ?: animeTitle,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentEpisode?.title ?: "Episode ${currentEpisode?.number ?: 1}",
                                    color = PrimaryViolet,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Resume Timestamp HUD (YouTube style)
                    AnimatedVisibility(
                        visible = resumeBannerText != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 58.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface.copy(alpha = 0.95f))
                                .border(1.dp, PrimaryViolet.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.History, contentDescription = null, tint = PrimaryViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = resumeBannerText ?: "", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Start Over",
                                color = AccentAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    currentPlayPositionMs = 0L
                                    exoPlayer.seekTo(0)
                                    webViewInstance?.evaluateJavascript(
                                        """
                                        (function() {
                                            var iframe = document.getElementById('player-frame');
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage({ command: 'seek', time: 0 }, '*');
                                                iframe.contentWindow.postMessage({ event: 'seek', time: 0 }, '*');
                                            }
                                        })();
                                        """.trimIndent(), null
                                    )
                                    resumeBannerText = null
                                }
                            )
                        }
                    }

                    // Transparent Lock Button in Fullscreen Mode
                    if (isFullscreen) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 20.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                                .border(
                                    width = 1.dp,
                                    color = if (isOrientationLocked) PrimaryViolet else Color.White.copy(alpha = 0.35f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    isOrientationLocked = !isOrientationLocked
                                    if (isOrientationLocked) {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                        lockFeedbackText = "Screen Locked"
                                    } else {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                        lockFeedbackText = "Screen Unlocked"
                                    }
                                    scope.launch {
                                        delay(1200)
                                        lockFeedbackText = null
                                    }
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOrientationLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = if (isOrientationLocked) "Unlock Screen" else "Lock Screen",
                                tint = if (isOrientationLocked) PrimaryViolet else Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Floating Prev / Next Buttons in Fullscreen
                    if (isFullscreen && episodes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (hasPrevEpisode) {
                                IconButton(
                                    onClick = { currentEpisode = episodes[currentEpIndex - 1] },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(DarkSurface.copy(alpha = 0.85f))
                                ) {
                                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev Episode", tint = TextPrimary)
                                }
                            }
                            if (hasNextEpisode) {
                                IconButton(
                                    onClick = { currentEpisode = episodes[currentEpIndex + 1] },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(DarkSurface.copy(alpha = 0.85f))
                                ) {
                                    Icon(Icons.Filled.SkipNext, contentDescription = "Next Episode", tint = PrimaryViolet)
                                }
                            }
                        }
                    }
                }
            }

            // Orientation Lock Feedback Overlay
            lockFeedbackText?.let { text ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .border(1.dp, PrimaryViolet.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Text(text = text, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Seek Feedback Overlay
            seekFeedbackText?.let { text ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(text = text, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Volume Indicator
            volumeFeedback?.let { vol ->
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = PrimaryViolet)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${(vol * 100).toInt()}%", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Brightness Indicator
            brightnessFeedback?.let { br ->
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.BrightnessMedium, contentDescription = null, tint = AccentAmber)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${(br * 100).toInt()}%", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val playerSurface = remember {
        movableContentOf {
            VideoPlayerSurface(modifier = Modifier.fillMaxSize())
        }
    }

    if (isMinimized) {
        // FLOATING MINI-PLAYER WINDOW (Matching Screenshot)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = modifier
                .width(220.dp)
                .aspectRatio(16f / 9f)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .clickable { onExpand() }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Live video player surface playing continuously!
                playerSurface()

                // Semi-transparent dark overlay for high contrast controls (Matching Screenshot)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                // 2. Top Row: Settings (Left) & Close (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val nextAudio = if (activeAudioTab == "sub") "dub" else "sub"
                            activeAudioTab = nextAudio
                            currentEpisode?.let { loadStreamForEpisode(it) }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings / Audio Format",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Mini Player",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 3. Center: 4-Corner Expand Icon (takes user to the player page)
                IconButton(
                    onClick = { onExpand() },
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CropFree,
                        contentDescription = "Expand to Player Page",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 4. Bottom Controls Row: Previous, Pause/Play, Next
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (hasPrevEpisode) {
                                currentEpisode = episodes[currentEpIndex - 1]
                            }
                        },
                        enabled = hasPrevEpisode,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous Episode",
                            tint = if (hasPrevEpisode) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { togglePlayPause() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (PipController.isVideoPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (PipController.isVideoPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (hasNextEpisode) {
                                currentEpisode = episodes[currentEpIndex + 1]
                            }
                        },
                        enabled = hasNextEpisode,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next Episode",
                            tint = if (hasNextEpisode) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 5. Red progress bar at bottom of mini-player card
                val progress = if (totalPlayDurationMs > 0) {
                    (currentPlayPositionMs.toFloat() / totalPlayDurationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .align(Alignment.BottomCenter),
                    color = AccentRed,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    } else if (PipController.isInPipMode || isFullscreen) {
        // FULLSCREEN LANDSCAPE VIEW OR SYSTEM PICTURE-IN-PICTURE (Pure 16:9 player surface)
        Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
            playerSurface()
        }
    } else {
        // PORTRAIT VIEW (16:9 player surface with episode list below)
        Column(
            modifier = modifier
                .background(DarkCanvas)
                .statusBarsPadding()
        ) {
            // 1. Top 16:9 Video Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                playerSurface()
            }

            // 2. Scrollable Anime Details & Episode Selector
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = englishTitle ?: animeTitle,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (englishTitle != null && englishTitle != animeTitle) {
                                Text(
                                    text = animeTitle,
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentEpisode?.let { "Now Playing: Episode ${it.number}" } ?: "Select an episode to watch",
                                color = PrimaryViolet,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Bookmark Button
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val animeObj = Anime(
                                        id = animeId,
                                        title = animeTitle,
                                        englishTitle = englishTitle,
                                        coverImage = coverUrl,
                                        format = format,
                                        episodes = episodes.size.takeIf { it > 0 },
                                        currentEpisode = currentEpisode?.number
                                    )
                                    isBookmarked = dbHelper.toggleWatchlist(animeObj)
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (isBookmarked) AccentAmber else TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Audio Format (SUB / DUB) & Streaming Server Selector Row
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .padding(12.dp)
                    ) {
                        // Sub / Dub Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = activeAudioTab == "sub",
                                    onClick = {
                                        activeAudioTab = "sub"
                                        currentEpisode?.let { ep -> loadStreamForEpisode(ep) }
                                    },
                                    label = { Text("SUB", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryViolet,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkCard,
                                        labelColor = TextMuted
                                    )
                                )

                                FilterChip(
                                    selected = activeAudioTab == "dub",
                                    onClick = {
                                        activeAudioTab = "dub"
                                        currentEpisode?.let { ep -> loadStreamForEpisode(ep) }
                                    },
                                    label = { Text("DUB", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentAmber,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkCard,
                                        labelColor = TextMuted
                                    )
                                )
                            }

                            // Prev / Next Episode Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { currentEpisode = episodes[currentEpIndex - 1] },
                                    enabled = hasPrevEpisode,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = if (hasPrevEpisode) TextPrimary else TextMuted.copy(alpha = 0.4f)
                                    )
                                }

                                IconButton(
                                    onClick = { currentEpisode = episodes[currentEpIndex + 1] },
                                    enabled = hasNextEpisode,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = "Next",
                                        tint = if (hasNextEpisode) PrimaryViolet else TextMuted.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        // Available Servers for active audio
                        val filteredServers = servers.filter { it.type == activeAudioTab }
                        if (filteredServers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Server:",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredServers.size) { idx ->
                                    val srv = filteredServers[idx]
                                    val isSelected = selectedServer?.linkId == srv.linkId
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) PrimaryViolet.copy(alpha = 0.25f) else DarkCard)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) PrimaryViolet else DarkBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedServer = srv
                                                currentEpisode?.let { ep -> loadStreamForEpisode(ep, srv) }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = srv.name,
                                            color = if (isSelected) PrimaryViolet else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. EPISODE LIST SECTION
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Episodes",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (episodes.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryViolet.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${episodes.size}",
                                        color = PrimaryViolet,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (isLoadingEpisodes) {
                            CircularProgressIndicator(
                                color = PrimaryViolet,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Episode Chunking Tabs (For anime with > 30 episodes like Black Clover or One Piece)
                if (episodes.size > chunkSize) {
                    item {
                        val chunkCount = (episodes.size + chunkSize - 1) / chunkSize
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(chunkCount) { idx ->
                                val start = idx * chunkSize + 1
                                val end = minOf((idx + 1) * chunkSize, episodes.size)
                                val isSelected = selectedChunkIndex == idx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PrimaryViolet else DarkSurface)
                                        .clickable { selectedChunkIndex = idx }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$start - $end",
                                        color = if (isSelected) Color.White else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Loading / Error / Episode Grid
                if (isLoadingEpisodes) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryViolet)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Loading episodes from Anikoto...",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else if (episodesError != null && episodes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = episodesError ?: "Could not load episodes",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { fetchEpisodesList() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    val displayedEpisodes = if (episodes.size > chunkSize) {
                        val start = selectedChunkIndex * chunkSize
                        val end = minOf(start + chunkSize, episodes.size)
                        episodes.subList(start, end)
                    } else {
                        episodes
                    }

                    item {
                        val columns = 5
                        val rows = (displayedEpisodes.size + columns - 1) / columns
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (rowIndex in 0 until rows) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (colIndex in 0 until columns) {
                                        val epIdx = rowIndex * columns + colIndex
                                        if (epIdx < displayedEpisodes.size) {
                                            val ep = displayedEpisodes[epIdx]
                                            val isPlaying = currentEpisode?.id == ep.id

                                            val bgModifier = if (isPlaying) {
                                                Modifier.background(Brush.verticalGradient(listOf(PrimaryViolet, PrimaryVioletDark)))
                                            } else {
                                                Modifier.background(DarkSurface)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1.2f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .then(bgModifier)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isPlaying) VioletLight else DarkBorder,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        currentEpisode = ep
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    if (isPlaying) {
                                                        Icon(
                                                            imageVector = Icons.Filled.PlayArrow,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "${ep.number}",
                                                        color = if (isPlaying) Color.White else TextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isPlaying) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
