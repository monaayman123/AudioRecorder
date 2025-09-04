package com.example.audioplayer

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.audioplayer.ui.theme.AudioPlayerTheme
import kotlinx.coroutines.delay
import java.io.File


class MainActivity : ComponentActivity() {
    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }
    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioPlayerTheme {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.RECORD_AUDIO), 101
                    )
                }
                var durationText by remember { mutableStateOf("00:00") }
                var isRecording by remember { mutableStateOf(false) }
                var isPlaying by remember { mutableStateOf(false) }

                var sliderPosition by remember { mutableStateOf(0f) }
                var maxPosition by remember { mutableStateOf(1f) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.Black)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "Audio", fontSize = 20.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    shape = RoundedCornerShape(16.dp), color = Color.DarkGray
                                )
                                .padding(12.dp)
                                .clickable {
                                    if (!isRecording) {
                                        File(cacheDir, "audio.mp3").also {
                                            recorder.startRecording(it)
                                            audioFile = it
                                        }
                                        isRecording = true
                                    } else {
                                        recorder.stopRecording()
                                        isRecording = false
                                        recorder.stopRecording()
                                        isRecording = false

                                        audioFile?.let { file ->
                                            if (file.exists() && file.length() > 0) {
                                                val mp = MediaPlayer()
                                                mp.setDataSource(file.absolutePath)
                                                mp.prepare()
                                                val durationMillis = mp.duration
                                                val minutes = (durationMillis / 1000) / 60
                                                val seconds = (durationMillis / 1000) % 60
                                                durationText = String.format("%02d:%02d", minutes, seconds)
                                                maxPosition = durationMillis.toFloat()
                                                mp.release()
                                            } else {
                                                durationText = "00:00"
                                            }
                                        }

                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(
                                    id = if (isRecording) R.drawable.mic__play else R.drawable.mic
                                ),
                                contentDescription = "Recording",
                                tint = Color.White
                            )
                            Text(
                                text = "Hold to Record",
                                modifier = Modifier.padding(start = 16.dp),
                                color = Color.White

                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        LaunchedEffect(isPlaying) {
                            if (isPlaying) {
                                while (player.isPlaying()) {
                                    sliderPosition = player.currentPosition().toFloat()
                                    delay(500)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        shape = RoundedCornerShape(16.dp), color = Color.DarkGray
                                    )
                                    .padding(12.dp)
                                    .clickable(enabled = audioFile != null) {
                                        if (!isPlaying) {
                                            player.playRecording(audioFile ?: return@clickable)
                                            isPlaying = true
                                            player.setOnCompletionListener {
                                                isPlaying = false
                                                sliderPosition = 0f
                                            }
                                        } else {
                                            player.stop()
                                            isPlaying = false
                                        }
                                    }
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = if (isPlaying) painterResource(id = R.drawable.pause_button) else painterResource(
                                        R.drawable.play
                                    ),
                                    contentDescription = "Play",
                                    tint = Color.White,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        shape = RoundedCornerShape(16.dp), color = Color.DarkGray
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction =sliderPosition / maxPosition)
                                        .fillMaxHeight()
                                        .background(Color.Blue)
                                )
                            }
                            Text(
                                text = durationText,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 12.dp),
                                color = Color.DarkGray
                            )

                        }
                    }
                }
            }
        }
    }
}

