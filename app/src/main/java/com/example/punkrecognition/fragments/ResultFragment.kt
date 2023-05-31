package com.example.punkrecognition.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64.URL_SAFE
import android.util.Base64.decode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.punkrecognition.R
import com.example.punkrecognition.adapters.ImageAdapter
import com.example.punkrecognition.common.Common
import com.example.punkrecognition.model.Prediction
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ResultFragment(var prediction: Prediction, var encodedImage: String) : Fragment(), OnMapReadyCallback {

    lateinit var yourPhoto: ImageView
    lateinit var mushroomPhoto: ImageView
    lateinit var probabilityField: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var mushroomName: TextView
    lateinit var progressBar1: ProgressBar
    lateinit var progressBar2: ProgressBar
    lateinit var location: List<Double>
    var name: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.result_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        mushroomName = view.findViewById(R.id.name)
        probabilityField = view.findViewById(R.id.probability)
        yourPhoto = view.findViewById(R.id.yourPhoto)
        mushroomPhoto = view.findViewById(R.id.mushroomPhoto)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar1 = view.findViewById(R.id.progressBar)
        progressBar2 = view.findViewById(R.id.progressBar2)

        mushroomName.text = prediction.name
        name = prediction.name
        location = prediction.location

        setYourPhoto()
        setUpRecyclerView()
        Common.retrofitService.getMushroomSamples("some_sample", 11).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    setMushroomPhoto(response.body())
                    val photos = response.body()!!
                    recyclerView.adapter = ImageAdapter(photos.subList(1, photos.size))
                    progressBar1.visibility = View.GONE
                    progressBar2.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), "Error to get mushroom samples", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error to get mushroom samples", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setUpRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.isNestedScrollingEnabled = false
    }

    @SuppressLint("ResourceAsColor")
    private fun setProbabilityColor(probability: Int?) {
        if (probability != null) {
            if (probability < 25) {
                probabilityField.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            } else if (probability < 50) {
                probabilityField.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
            } else {
                probabilityField.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            }
        }
    }

    private fun setMushroomPhoto(mushrooms: List<String>?) {
        val imageAsBytes: ByteArray = decode(mushrooms?.get(0), URL_SAFE)
        mushroomPhoto.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size))
    }

    private fun setYourPhoto() {
        val imageAsBytes: ByteArray = decode(encodedImage, URL_SAFE)
        yourPhoto.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size), 800, 800, true))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(location[0], location[1])
        googleMap.addMarker(MarkerOptions().position(location).title(name))
        moveToCurrentLocation(location, googleMap)
    }

    fun moveToCurrentLocation(currentLocation: LatLng?, googleMap: GoogleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
        googleMap.animateCamera(CameraUpdateFactory.zoomIn())
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17f), 2000, null)
    }
}