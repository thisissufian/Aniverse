package com.aniverse.app.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
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

class WebAppInterface(
    private val onToggleFullscreen: () -> Unit,
    private val isCurrentFullscreen: () -> Boolean,
    private val onPlaybackChanged: (Boolean) -> Unit
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    animeId: Int,
    animeTitle: String,
    englishTitle: String? = null,
    coverUrl: String = "",
    format: String? = "TV",
    onBackClick: () -> Unit,
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

    var showSkipIntro by remember { mutableStateOf(false) }
    var showSkipOutro by remember { mutableStateOf(false) }

    // Chunking for large series (30 episodes per tab)
    val chunkSize = 30
    var selectedChunkIndex by remember { mutableStateOf(0) }

    // Gesture feedback
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var volumeFeedback by remember { mutableStateOf<Float?>(null) }
    var brightnessFeedback by remember { mutableStateOf<Float?>(null) }
    var lockFeedbackText by remember { mutableStateOf<String?>(null) }

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

                // If orientation is locked by user, do NOT rotate automatically
                if (isOrientationLocked) return

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

    // Manage PiP Active state
    DisposableEffect(Unit) {
        PipController.isPlayerActive = true
        onDispose {
            PipController.isPlayerActive = false
            PipController.isVideoPlaying = false
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.getInsetsController(act.window, act.window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
            exoPlayer.release()
        }
    }

    // Handle back button when in fullscreen
    BackHandler(enabled = isFullscreen) {
        setFullscreenMode(false)
    }

    LaunchedEffect(animeId) {
        isBookmarked = dbHelper.isInWatchlist(animeId)
    }

    // Stream resolution function
    fun loadStreamForEpisode(episode: Episode, preferredServer: ServerItem? = null) {
        scope.launch {
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
            loadStreamForEpisode(ep)
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
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration

                introInterval?.let { intro ->
                    showSkipIntro = pos in intro.start..intro.end
                    if (appPrefs.autoSkipIntro && pos in intro.start..(intro.start + 2500)) {
                        exoPlayer.seekTo(intro.end + 1000)
                        showSkipIntro = false
                    }
                }

                outroInterval?.let { outro ->
                    showSkipOutro = pos in outro.start..outro.end
                }

                if (pos > 1000 && dur > 1000) {
                    currentEpisode?.let { ep ->
                        dbHelper.saveProgress(
                            animeId = animeId,
                            title = englishTitle ?: animeTitle,
                            coverUrl = coverUrl,
                            episodeNum = ep.number,
                            positionMs = pos,
                            durationMs = dur
                        )
                    }
                }
            }
            delay(1000)
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
                .clickable { showControlsOverlay = !showControlsOverlay }
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
                            window.addEventListener('message', function(event) {
                                var data = event.data;
                                if (typeof data === 'string') {
                                    try { data = JSON.parse(data); } catch(e) {}
                                }
                                if (!data) return;

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
                                        }
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

                            loadDataWithBaseURL("https://anikototv.to/", embedHtml, "text/html", "UTF-8", null)
                            webViewInstance = this
                        }
                    },
                    update = { webView ->
                        webViewInstance = webView
                        webView.loadDataWithBaseURL("https://anikototv.to/", embedHtml, "text/html", "UTF-8", null)
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

            // Controls Overlay
            AnimatedVisibility(
                visible = showControlsOverlay || isFullscreen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Bar (Back button, Anime/Episode Title, PiP button)
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
                            onClick = {
                                if (isFullscreen) {
                                    setFullscreenMode(false)
                                } else {
                                    exoPlayer.stop()
                                    onBackClick()
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface.copy(alpha = 0.8f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
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

                        // PiP Button
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            IconButton(
                                onClick = {
                                    try {
                                        activity?.enterPictureInPictureMode(
                                            android.app.PictureInPictureParams.Builder()
                                                .setAspectRatio(android.util.Rational(16, 9))
                                                .build()
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface.copy(alpha = 0.8f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureInPictureAlt,
                                    contentDescription = "PiP",
                                    tint = AccentAmber
                                )
                            }
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

                    // Auto Skip Intro Button
                    AnimatedVisibility(
                        visible = showSkipIntro,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val skipTarget = introInterval?.end ?: 150_000L
                                exoPlayer.seekTo(skipTarget + 1000)
                                showSkipIntro = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.FastForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Skip Intro", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

    if (isFullscreen) {
        // FULLSCREEN LANDSCAPE VIEW
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            VideoPlayerSurface(modifier = Modifier.fillMaxSize())
        }
    } else {
        // PORTRAIT VIEW (16:9 player surface with episode list below)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DarkCanvas)
                .statusBarsPadding()
        ) {
            // 1. Top 16:9 Video Player
            VideoPlayerSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

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
