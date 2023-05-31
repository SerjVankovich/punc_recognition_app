package com.example.punkrecognition.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.desmond.squarecamera.CameraActivity
import com.example.punkrecognition.MainActivity.Companion.CAMERA_REQUEST
import com.example.punkrecognition.R
import com.example.punkrecognition.common.Common
import com.example.punkrecognition.model.Picture
import com.example.punkrecognition.model.Prediction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


class MainFragment(var fragmentTransaction: FragmentTransaction) : Fragment() {
    val CHOOSE_IMAGE = 1234

    private lateinit var takePictureButton: Button
    private lateinit var choosePictureButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        takePictureButton = view.findViewById(R.id.take_picture)
        choosePictureButton = view.findViewById(R.id.take_gallery)

        takePictureButton.setOnClickListener {
            makePhoto()
        }

        choosePictureButton.setOnClickListener {
            choosePhoto()
        }
    }

    private fun choosePhoto() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_IMAGE)
    }

    private fun makePhoto() {
        val cameraPermission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            val startCameraIntent = Intent(requireContext(), CameraActivity::class.java)
            startActivityForResult(startCameraIntent, CAMERA_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != AppCompatActivity.RESULT_OK) {
            return
        }
        if (requestCode == CAMERA_REQUEST || requestCode == CHOOSE_IMAGE) {
            val encodedImage = createEncodedImage(data)
            sendImageToRecognize(encodedImage)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendImageToRecognize(encodedImage: String) {
        takePictureButton.isEnabled = false
        Common.retrofitService.sendPictureForRecognize(Picture(encodedImage)).enqueue(object :
            Callback<Prediction> {
            override fun onResponse(call: Call<Prediction>, response: Response<Prediction>) {
                if (response.isSuccessful) {
                    loadResultFragment(response.body()!!, encodedImage)
                } else {
                    Toast.makeText(requireContext(), "Empty mushroom name", Toast.LENGTH_SHORT).show()
                    enableButton()
                }
            }

            override fun onFailure(call: Call<Prediction>, t: Throwable) {
                Toast.makeText(requireContext(), "Server does not response", Toast.LENGTH_SHORT).show()
                enableButton()
            }

        })
    }

    fun enableButton() {
        takePictureButton.isEnabled = true
    }

    fun setNewTransaction(fragmentTransaction: FragmentTransaction) {
        this.fragmentTransaction = fragmentTransaction
    }

    private fun createEncodedImage(data: Intent?): String {
        val photoUri = data?.data
        val bitMap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, photoUri)

        var outputStream = ByteArrayOutputStream()
        bitMap.compress(Bitmap.CompressFormat.JPEG,30, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.URL_SAFE)
    }

    private fun loadResultFragment(prediction: Prediction, encodedImage: String) {
        val resultFragment = ResultFragment(prediction, encodedImage)
        fragmentTransaction.replace(R.id.fragment_container, resultFragment)
            .addToBackStack(null)
            .commit()
    }
}