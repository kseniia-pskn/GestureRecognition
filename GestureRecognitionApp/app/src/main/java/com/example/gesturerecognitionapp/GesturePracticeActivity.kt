package com.kseniia.gestureapp

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GesturePracticeActivity : AppCompatActivity() {

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var videoFile: File? = null
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_practice)

        // Initialize Firebase Storage
        storage = Firebase.storage

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        findViewById<Button>(R.id.btn_record).setOnClickListener {
            if (recording == null) {
                recordVideo()
            } else {
                stopRecording()
            }
        }

        findViewById<Button>(R.id.btn_upload).setOnClickListener {
            videoFile?.let { uploadVideoToFirebase(it) } ?: Toast.makeText(this, "No video to upload", Toast.LENGTH_SHORT).show()
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
        }

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val recorder = Recorder.Builder().build()

            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun recordVideo()  {
        videoFile = getVideoFile()
        val outputOptions = FileOutputOptions.Builder(videoFile!!).build()

        recording = videoCapture?.output
            ?.prepareRecording(this, outputOptions)
            ?.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()

                        // Automatically stop the recording after 5 seconds
                        Handler(Looper.getMainLooper()).postDelayed({
                            stopRecording()
                        }, 5000)
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            Toast.makeText(
                                this,
                                "Recording finished. File saved at: ${videoFile!!.absolutePath}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Recording error: ${recordEvent.error}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }


    private fun stopRecording() {
        recording?.stop()
        recording = null
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
        Log.d("Recording", "Recording stopped, file is: ${videoFile!!.absolutePath}")
    }

    private fun uploadVideoToFirebase(file: File) {
        // Get Firebase storage reference
        val storageRef = storage.reference
        val videoRef = storageRef.child("videos/${file.name}")

        // Convert file to Uri
        val fileUri = Uri.fromFile(file)

        // Upload video to Firebase Storage
        val uploadTask = videoRef.putFile(fileUri)

        // Monitor the upload process
        uploadTask.addOnSuccessListener {
            Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            Log.d("Upload Progress", "Upload is $progress% done")
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun getVideoFile(): File {
        val timestamp = System.currentTimeMillis()
        return File(outputDirectory, "gesture_video_$timestamp.mp4")
    }

    override fun onDestroy() {
        super.onDestroy()
        recording?.close()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }
}
