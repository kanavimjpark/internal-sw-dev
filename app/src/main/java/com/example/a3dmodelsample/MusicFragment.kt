package com.example.a3dmodelsample

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat

class MusicFragment : Fragment(R.layout.fragment_music) {

    private lateinit var tabUsb: TextView
    private lateinit var tabBluetooth: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabUsb = view.findViewById(R.id.tabUsb)
        tabBluetooth = view.findViewById(R.id.tabBluetooth)

        tabUsb.setOnClickListener {
            selectTab("USB")
        }

        tabBluetooth.setOnClickListener {
            selectTab("Bluetooth")
        }

        // 기본으로 USB 탭 선택
        selectTab("USB")
    }

    private fun selectTab(type: String) {
        when (type) {
            "USB" -> {
                tabUsb.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_1D1D1D))
                tabUsb.setTypeface(null, Typeface.BOLD)
                tabBluetooth.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabBluetooth.setTypeface(null, Typeface.NORMAL)

                childFragmentManager.beginTransaction()
                    .replace(R.id.mediaTabContent, UsbFragment())
                    .commit()
            }

            "Bluetooth" -> {
                tabBluetooth.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_1D1D1D))
                tabBluetooth.setTypeface(null, Typeface.BOLD)
                tabUsb.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabUsb.setTypeface(null, Typeface.NORMAL)

                childFragmentManager.beginTransaction()
                    .replace(R.id.mediaTabContent, BluetoothFragment())
                    .commit()
            }
        }
    }
}
