package com.example.cis400.eventfinder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class InterestedFragment: Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_interested, container, false)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_settings, InterestRecyclerViewFragment())
            .commit()

        return view
    }
}