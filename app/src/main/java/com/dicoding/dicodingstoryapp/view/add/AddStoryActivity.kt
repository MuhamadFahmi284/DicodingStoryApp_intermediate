package com.dicoding.dicodingstoryapp.view.add

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.dicodingstoryapp.data.ResultState
import com.dicoding.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.dicoding.dicodingstoryapp.utils.getImageUri
import com.dicoding.dicodingstoryapp.utils.reduceFileImage
import com.dicoding.dicodingstoryapp.utils.uriToFile
import com.dicoding.dicodingstoryapp.view.MainAdapter
import com.dicoding.dicodingstoryapp.view.MainViewModel
import com.dicoding.dicodingstoryapp.view.ViewModelFactory
import com.dicoding.dicodingstoryapp.view.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.getValue

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding

    private var currentImageUri: Uri? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupView()
        observeView()

        binding.apply {
            galleryButton.setOnClickListener { startGallery() }
            cameraButton.setOnClickListener { startCamera() }
            uploadButton.setOnClickListener { uploadStory() }
            cancelButton.setOnClickListener { finish() }
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    requestLocation()
                } else {
                    currentLatitude = null
                    currentLongitude = null
                }
            }
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            showDialog(this, "Tidak ada gambar yang dipilih.")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun observeView() {
        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Error -> {
                    showLoading(false)
                    showDialog(this, result.error)
                }

                is ResultState.Success -> {
                    showLoading(false)
                    showDialog(this, result.data.message.toString()) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                null -> false
            }
        }
    }

    private fun uploadStory() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()

            val description = binding.edInputStory.text.toString()

            if (description.isBlank()) {
                showDialog(this, "Silakan masukkan deskripsi cerita terlebih dahulu.")
                return
            }

            showLoading(true)

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            val lat = currentLatitude?.toFloat()
            val lon = currentLongitude?.toFloat()

            viewModel.uploadStory(multipartBody, requestBody, lat, lon)
        } ?: showDialog(this, "Silakan masukkan berkas gambar terlebih dahulu.")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        if (!isGpsEnabled()) {
            showDialog(this, "GPS tidak aktif. Silakan aktifkan GPS terlebih dahulu.")
            binding.checkbox.isChecked = false
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
            } else {
                showDialog(this, "Tidak dapat mendapatkan lokasi.")
                binding.checkbox.isChecked = false
            }
        }.addOnFailureListener {
            showDialog(this, "Terjadi kesalahan saat mendapatkan lokasi.")
            binding.checkbox.isChecked = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            } else {
                showDialog(this, "Izin lokasi ditolak.")
                binding.checkbox.isChecked = false
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showDialog(
        context: Context,
        message: String,
        onDismissAction: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismissAction?.invoke()
            }
            .show()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
