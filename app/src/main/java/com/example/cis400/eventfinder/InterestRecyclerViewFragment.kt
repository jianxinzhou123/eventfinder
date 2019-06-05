package com.example.cis400.eventfinder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_event.*
import java.lang.Exception

class InterestRecyclerViewFragment: Fragment(){
    private lateinit var recyclerView: RecyclerView
    companion object {
        var events = mutableListOf<EventData>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recycler_view, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = ListAdapter(events, activity!!)
        getInterested()
        return view
    }

    private fun getInterested(): MutableList<EventData>{
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("UserInfo").child(FirebaseAuth.getInstance().uid!!).child("Interested")
        mRef.addListenerForSingleValueEvent(object:
            ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                events.clear()
                for(p1 in p0.children){
                    var event = p1.getValue(EventData::class.java)
                    if(!events.contains(event)){
                        System.out.println(event)
                        events.add(event!!)
                    }
                }
                try {
                    sortEventsByInterest(events)
                    recyclerView.adapter!!.notifyDataSetChanged()
                } catch (e: Exception){}
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
                System.out.println("ENTERING 2")
            }
        })
        return events
    }

    private fun sortEventsByInterest(events: MutableList<EventData>){
        for (pass in 0 until (events.size - 1)) {
            // A single pass of bubble sort
            for (currentPosition in 0 until (events.size - pass - 1)) {
                // This is a single step
                if (events[currentPosition].interested < events[currentPosition + 1].interested) {
                    val tmp = events[currentPosition]
                    events[currentPosition] = events[currentPosition + 1]
                    events[currentPosition + 1] = tmp
                } else {

                }
            }
        }
    }
}