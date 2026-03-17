package com.example.tarea4_intents_implicitos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

private const val FILE_NAME = "photo.jpg"

class camera_activity : AppCompatActivity() {
    private lateinit var photoFile: File
    private lateinit var imageView: ImageView

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { wasSuccessful ->
            if (wasSuccessful) {
                if (!::photoFile.isInitialized || !photoFile.exists()) {
                    Toast.makeText(this, "No se encontro la foto", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                val takenImage = decodeSampledBitmap(photoFile, imageView.width, imageView.height)
                if (takenImage != null) {
                    imageView.setImageBitmap(takenImage)
                } else {
                    Toast.makeText(this, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Permiso de camara denegado", Toast.LENGTH_SHORT).show()
            }
        }

    // Configura la pantalla y lanza la camara al presionar el boton.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.camera_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnTakePicture = findViewById<Button>(R.id.btnTakePicture)
        imageView = findViewById(R.id.imageView)

        btnTakePicture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Abre la app de camara y prepara la URI de salida con FileProvider.
    private fun launchCamera() {
        try {
            photoFile = getPhotoFile(FILE_NAME)
            val photoUri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Error abriendo camara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Crea un archivo temporal donde se guardara la foto.
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IllegalStateException("No hay almacenamiento externo disponible")
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    // Carga la imagen en tamaño reducido para evitar errores de memoria.
    private fun decodeSampledBitmap(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        val targetWidth = if (reqWidth > 0) reqWidth else 1080
        val targetHeight = if (reqHeight > 0) reqHeight else 1080

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

        val sampleOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(boundsOptions, targetWidth, targetHeight)
            inJustDecodeBounds = false
        }

        return BitmapFactory.decodeFile(file.absolutePath, sampleOptions)
    }

    // Calcula el factor de reduccion de la imagen segun el tamano solicitado.
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}