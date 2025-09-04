package com.example.audioplayer

import java.io.File

interface AudioPlayer {
    fun playRecording(audioFile: File)
    fun stop()

}