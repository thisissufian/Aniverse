package com.aniverse.app.ui.screens.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import kotlin.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.aniverse.app.data.local.AniverseDbHelper
import com.aniverse.app.data.model.Episode
import com.aniverse.app.data.model.ServerItem
import com.aniverse.app.data.model.SkipInterval
import com.aniverse.app.data.remote.AnikotoResolver
import com.aniverse.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    animeId: Int,
    animeTitle: String,
    coverUrl: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { AniverseDbHelper.getInstance(context) }
    val resolver = remember { AnikotoResolver() }

    // State
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var currentEpisode by remember { mutableStateOf<Episode?>(null) }
    var servers by remember { mutableStateOf<List<ServerItem>>(emptyList()) }
    var selectedServer by remember { mutableStateOf<ServerItem?>(null) }
    var activeAudioTab by remember { mutableStateOf("sub") }

    var embedUrl by remember { mutableStateOf<String?>(null) }
    var introInterval by remember { mutableStateOf<SkipInterval?>(null) }
    var outroInterval by remember { mutableStateOf<SkipInterval?>(null) }

    var isLoadingEpisodes by remember { mutableStateOf(true) }
    var isLoadingStream by remember { mutableStateOf(false) }

    var showEpisodeSheet by remember { mutableStateOf(false) }
    var showServerSheet by remember { mutableStateOf(false) }

    var showSkipIntro by remember { mutableStateOf(false) }
    var showSkipOutro by remember { mutableStateOf(false) }

    // Gesture feedback overlays
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var volumeFeedback by remember { mutableStateOf<Float?>(null) }
    var brightnessFeedback by remember { mutableStateOf<Float?>(null) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    // ExoPlayer Instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Load initial episodes
    LaunchedEffect(animeTitle) {
        isLoadingEpisodes = true
        try {
            val list = resolver.getEpisodes(animeTitle)
            episodes = list
            if (list.isNotEmpty()) {
                currentEpisode = list.first()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingEpisodes = false
        }
    }

    // Resolve stream whenever current episode or selected server changes
    fun loadStreamForEpisode(episode: Episode, preferredServer: ServerItem? = null) {
        scope.launch {
            isLoadingStream = true
            try {
                val dataIds = episode.dataIds
                if (!dataIds.isNullOrEmpty()) {
                    val serverList = resolver.getAvailableServers(dataIds)
                    servers = serverList

                    val target = preferredServer
                        ?: serverList.find { it.type == activeAudioTab && it.name.contains("HD-1", ignoreCase = true) }
                        ?: serverList.find { it.type == activeAudioTab }
                        ?: serverList.firstOrNull()

                    selectedServer = target

                    if (target != null) {
                        val embedResult = resolver.getServerEmbed(target.linkId)
                        if (embedResult != null) {
                            embedUrl = embedResult.embedUrl
                            introInterval = embedResult.intro
                            outroInterval = embedResult.outro

                            // If direct m3u8/mp4, feed into ExoPlayer
                            if (embedResult.embedUrl.endsWith(".m3u8") || embedResult.embedUrl.endsWith(".mp4")) {
                                exoPlayer.setMediaItem(MediaItem.fromUri(embedResult.embedUrl))
                                exoPlayer.prepare()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingStream = false
            }
        }
    }

    LaunchedEffect(currentEpisode) {
        currentEpisode?.let { ep ->
            loadStreamForEpisode(ep)
        }
    }

    // Periodic Progress Tracking & Skip Interval Checker
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration

                introInterval?.let { intro ->
                    showSkipIntro = pos in intro.start..intro.end
                } ?: run {
                    showSkipIntro = pos in 60_000L..150_000L // sensible default OP interval
                }

                outroInterval?.let { outro ->
                    showSkipOutro = pos in outro.start..outro.end
                }

                if (pos > 1000 && dur > 1000) {
                    currentEpisode?.let { ep ->
                        dbHelper.saveProgress(
                            animeId = animeId,
                            title = animeTitle,
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

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 2) {
                            // Left double tap: Rewind 10s
                            val newPos = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0)
                            exoPlayer.seekTo(newPos)
                            seekFeedbackText = "-10s"
                        } else {
                            // Right double tap: Fast forward 10s
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
                            // Left side: Brightness
                            val activity = context as? Activity
                            activity?.window?.let { window ->
                                val lp = window.attributes
                                var currentBrightness = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                                currentBrightness = (currentBrightness - (dragAmount / 800f)).coerceIn(0.05f, 1.0f)
                                lp.screenBrightness = currentBrightness
                                window.attributes = lp
                                brightnessFeedback = currentBrightness
                            }
                        } else {
                            // Right side: Volume
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
        // VIDEO PLAYER SURFACE (ExoPlayer or Hardware-Accelerated Iframe Player)
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
                            mediaPlaybackRequiresUserGesture = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                // Inject ad-blocking & styling
                                view?.evaluateJavascript(
                                    """
                                    (function() {
                                        var style = document.createElement('style');
                                        style.innerHTML = 'div[class*="ad"], iframe[id*="aswift"], #overlay { display: none !important; }';
                                        document.head.appendChild(style);
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                            }
                        }
                        webChromeClient = WebChromeClient()
                        loadUrl(embedUrl ?: "")
                    }
                },
                update = { webView ->
                    embedUrl?.let { url ->
                        if (webView.url != url) webView.loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Loading placeholder
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryViolet)
            }
        }

        val activity = context as? Activity
        val isInPip = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            activity?.isInPictureInPictureMode == true
        } else false

        if (!isInPip) {
            // TOP CONTROLS BAR (Overlay)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(
                        Brush.verticalGradient(
                            listOf(DarkCanvas.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            IconButton(
                onClick = {
                    exoPlayer.stop()
                    onBackClick()
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animeTitle,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentEpisode?.title ?: "Loading episode...",
                    color = PrimaryViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Episode Picker Button
            IconButton(
                onClick = { showEpisodeSheet = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Episode List",
                    tint = PrimaryViolet
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Server Picker Button
            IconButton(
                onClick = { showServerSheet = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Dns,
                    contentDescription = "Server Selector",
                    tint = AccentCyan
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Picture-in-Picture Button
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                IconButton(
                    onClick = {
                        try {
                            val hostActivity = context as? Activity
                            hostActivity?.enterPictureInPictureMode(
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
                        contentDescription = "Picture in Picture",
                        tint = AccentAmber
                    )
                }
            }
        }
    }

        // SEEK FEEDBACK OVERLAY (±10s animation)
        seekFeedbackText?.let { text ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.85f))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = text,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // VOLUME / BRIGHTNESS OVERLAY INDICATOR
        volumeFeedback?.let { vol ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = PrimaryViolet)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(vol * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        brightnessFeedback?.let { br ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.BrightnessMedium, contentDescription = null, tint = AccentAmber)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(br * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // AUTO SKIP INTRO OVERLAY BUTTON
        AnimatedVisibility(
            visible = showSkipIntro && !isInPip,
            enter = fadeIn() + slideInHorizontally { it / 2 },
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
        ) {
            Button(
                onClick = {
                    val skipTarget = introInterval?.end ?: 150_000L
                    exoPlayer.seekTo(skipTarget + 1000)
                    showSkipIntro = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FastForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Skip Intro",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // EPISODE PICKER BOTTOM SHEET
        if (showEpisodeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEpisodeSheet = false },
                containerColor = DarkSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Episodes (${episodes.size})",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 64.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                    ) {
                        items(episodes, key = { it.id }) { ep ->
                            val isSelected = currentEpisode?.id == ep.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PrimaryViolet else DarkCard)
                                    .clickable {
                                        currentEpisode = ep
                                        showEpisodeSheet = false
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${ep.number}",
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // SERVER / AUDIO SELECTOR BOTTOM SHEET
        if (showServerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showServerSheet = false },
                containerColor = DarkSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Streaming Servers",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Type Selector (Sub vs Dub)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = activeAudioTab == "sub",
                            onClick = { activeAudioTab = "sub" },
                            label = { Text("Subtitled (SUB)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryViolet,
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = activeAudioTab == "dub",
                            onClick = { activeAudioTab = "dub" },
                            label = { Text("Dubbed (DUB)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentAmber,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtered server buttons
                    val filteredServers = servers.filter { it.type == activeAudioTab }
                    if (filteredServers.isEmpty()) {
                        Text(
                            text = "No $activeAudioTab servers found for this episode.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredServers.forEach { srv ->
                                val isSelected = selectedServer?.linkId == srv.linkId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) PrimaryViolet.copy(alpha = 0.2f) else DarkCard)
                                        .clickable {
                                            selectedServer = srv
                                            currentEpisode?.let { ep -> loadStreamForEpisode(ep, srv) }
                                            showServerSheet = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = srv.name,
                                        color = if (isSelected) PrimaryViolet else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = PrimaryViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
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
