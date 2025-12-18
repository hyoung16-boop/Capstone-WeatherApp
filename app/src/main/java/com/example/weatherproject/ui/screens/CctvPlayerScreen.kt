@file:OptIn(UnstableApi::class)
package com.example.weatherproject.ui.screens

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController

import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun CctvPlayerScreen(
    navController: NavController,
    cctvName: String,
    cctvUrl: String
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    Log.d("CctvPlayerScreen", "재생 시작: $cctvUrl")

    // ExoPlayer 초기화 (User-Agent 추가)
    val exoPlayer = remember {
        // 커스텀 DataSource Factory (User-Agent 추가)
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .setAllowCrossProtocolRedirects(true)

        ExoPlayer.Builder(context).build().apply {
            // MediaSource 생성
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(cctvUrl))

            setMediaSource(mediaSource)

            // 에러 리스너 추가
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            isLoading = false
                            Log.d("CctvPlayerScreen", "재생 준비 완료")
                        }
                        Player.STATE_BUFFERING -> {
                            isLoading = true
                            Log.d("CctvPlayerScreen", "버퍼링 중...")
                        }
                        Player.STATE_ENDED -> {
                            Log.d("CctvPlayerScreen", "재생 종료")
                        }
                        Player.STATE_IDLE -> {
                            Log.d("CctvPlayerScreen", "대기 중")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    errorMessage = "재생 오류: ${error.message}"
                    Log.e("CctvPlayerScreen", "재생 실패: ${error.message}", error)
                    Log.e("CctvPlayerScreen", "Error code: ${error.errorCode}")
                }
            })

            prepare()
            playWhenReady = true
        }
    }

    // 화면 종료 시 플레이어 해제 및 생명주기 관리
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                    Log.d("CctvPlayerScreen", "일시정지 (ON_PAUSE)")
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                    Log.d("CctvPlayerScreen", "재생 재개 (ON_RESUME)")
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
            Log.d("CctvPlayerScreen", "ExoPlayer 해제됨")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = cctvName,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color.Black,
                elevation = 0.dp
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
        ) {
            // ExoPlayer View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        useController = true
                        controllerShowTimeoutMs = 5000
                    }
                }
            )

            // 로딩 표시
            if (isLoading && errorMessage == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "영상을 불러오는 중...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // 에러 메시지
            errorMessage?.let { error ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "CCTV 영상을 재생할 수 없습니다.\n서버에서 400 에러를 반환했습니다.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                        ) {
                            Text("돌아가기", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}