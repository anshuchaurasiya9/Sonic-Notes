package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var startTimeMillis: Long = 0L

    fun startRecording(): File? {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "recording_${System.currentTimeMillis()}.m4a")
        currentRecordingFile = file

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                startTimeMillis = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("AudioRecorderManager", "startRecording failed", e)
                return null
            }
        }
        return file
    }

    fun stopRecording(): Long {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            val duration = System.currentTimeMillis() - startTimeMillis
            if (duration < 0) 0L else duration
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "stopRecording failed", e)
            0L
        }
    }

    fun startPlaying(filePath: String, onPlayProgress: (Int, Int) -> Unit, onCompletion: () -> Unit) {
        stopPlaying()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    onCompletion()
                    stopPlaying()
                }
                
                // Track progress on playing
                val duration = duration
                Thread {
                    try {
                        while (mediaPlayer != null && isPlaying) {
                            val current = currentPosition
                            onPlayProgress(current, duration)
                            Thread.sleep(100)
                        }
                    } catch (e: Exception) {
                        // ignore Thread disruption
                    }
                }.start()

            } catch (e: IOException) {
                Log.e("AudioRecorderManager", "prepare() / playback failed", e)
            }
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "stopPlaying failed", e)
        }
        mediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }
}
