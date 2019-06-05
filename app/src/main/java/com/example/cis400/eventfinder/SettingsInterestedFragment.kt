package com.example.cis400.eventfinder

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings_interested.view.*

class SettingsInterestedFragment: Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings_interested, container, false)

        view.tab_layout.addTab(view.tab_layout.newTab().setText("Account Settings"))
        view.tab_layout.addTab(view.tab_layout.newTab().setText("Interested Events"))
        view.tab_layout.setTabGravity(TabLayout.GRAVITY_FILL)
        var adapter = PagerAdapter(activity!!.supportFragmentManager, view.tab_layout.tabCount)
        view.pager.adapter = adapter
        view.tab_layout.setupWithViewPager(view.pager)
        view.tab_layout.getTabAt(0)!!.setText("Settings")
        view.tab_layout.getTabAt(1)!!.setText("Interested")
        view.tab_layout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                view.pager.setCurrentItem(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
        view.pager.setPageTransformer(false, FlipPageViewTransformer())

        view.fab.setOnClickListener { view ->
            Snackbar.make(view, "Please contact chmclaug@syr.edu or jzhou104@syr.edu for assistance.", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }
        return view
    }
}