package com.example.punkrecognition.layout_managers

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class CustomLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean {
        return false
    }
}