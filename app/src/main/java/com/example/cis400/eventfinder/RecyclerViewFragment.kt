package com.example.cis400.eventfinder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.google.firebase.database.*
import java.lang.Exception
import com.google.firebase.database.DataSnapshot
import kotlinx.android.synthetic.main.fragment_create.view.*
import kotlinx.android.synthetic.main.fragment_recycler_view.view.*
import kotlinx.android.synthetic.main.list_adapter.*
import kotlinx.android.synthetic.main.list_adapter.view.*

class RecyclerViewFragment: Fragment() {
    companion object {
        var currORold = "curr"
        var currentEvents = mutableListOf<EventData>()
        var oldEvents = mutableListOf<EventData>()
        var events = mutableListOf<EventData>()
    }

    private lateinit var recyclerView: RecyclerView
    private var mDatabase = FirebaseDatabase.getInstance().reference
    private var mRefCurrent = mDatabase.child("Events").child("Current")
    private var mRefOld = mDatabase.child("Events").child("Old")



    private var childEventListener = object: ChildEventListener{
        override fun onChildRemoved(p0: DataSnapshot) {
            try {
                if (::recyclerView.isInitialized) {
                    val data = p0.getValue<EventData>(EventData::class.java)
                    val key = p0.key
                    if (events.contains(data)) {
                        events.remove(data!!)
                        try {
                            sortEventsByInterest(events)
                            recyclerView.adapter = ListAdapter(events, activity!!)
                            recyclerView.adapter!!.notifyDataSetChanged()
                            recyclerView!!.requestLayout()


                        }catch(e: Exception){
                        }
                    }
                }
            }
            catch(e: Exception){
                System.out.println(e)
            }
        }
        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            try {
                if (::recyclerView.isInitialized) {
                    val data = p0.getValue<EventData>(EventData::class.java)
                    val key = p0.key
                    if (!events.contains(data)) {
                        events.add(data!!)
                    }
                    if(!HomeActivity.searched) {
                        try {
                            sortEventsByInterest(events)
                            recyclerView.adapter = ListAdapter(events, activity!!)
                            recyclerView.adapter!!.notifyDataSetChanged()
                            recyclerView!!.requestLayout()



                        } catch(e: Exception){}
                    }
                }
            }
            catch(e: Exception){
                System.out.println(e)
                System.out.println("ASDASDASD")
            }
        }
        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

        }
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            if (::recyclerView.isInitialized) {
                if (currORold == "curr") {

                } else if (currORold == "old") {

                }
            }
        }
        override fun onCancelled(p0: DatabaseError) {
            try {
                Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
            catch (e: Exception){
                System.out.println(e)
            }
        }
    }

    init{
        if(currORold == "curr"){
            mRefCurrent.addChildEventListener(childEventListener)
        }
        else if(currORold == "old"){
            mRefOld.addChildEventListener(childEventListener)

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recycler_view, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        getEvents()
        return view
    }


    private fun getEvents(){
        try {
            if (currORold == "curr") {
                mRefCurrent.addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if (!HomeActivity.searched) {
                            events = mutableListOf()
                            events.clear()
                            for (p1 in p0.getChildren()) {
                                var event = p1.getValue(EventData::class.java)!!
                                if (!events.contains(event)) {
                                    events.add(event)
                                    System.out.println(events)
                                }
                            }
                            try {
                                sortEventsByInterest(events)
                                recyclerView.adapter = ListAdapter(events, activity!!)
                                recyclerView.adapter!!.notifyDataSetChanged()
                                recyclerView!!.requestLayout()




                            } catch(e: Exception){

                            }
                        } else {
                            try {
                                sortEventsByInterest(HomeActivity.test)
                                recyclerView.adapter = ListAdapter(HomeActivity.test, activity!!)
                                recyclerView.adapter!!.notifyDataSetChanged()
                                recyclerView!!.requestLayout()





                            }catch(e: Exception){

                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        //Failed to read
                    }
                })
            } else {
                mRefOld.addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if (!HomeActivity.searched) {
                            events = mutableListOf()
                            events.clear()
                            for (p1 in p0.getChildren()) {
                                var event = p1.getValue(EventData::class.java)!!
                                if (!events.contains(event)) {
                                    events.add(event)
                                    System.out.println(events)
                                }
                            }
                            try {
                                sortEventsByInterest(events)
                                recyclerView.adapter = ListAdapter(events, activity!!)
                                recyclerView.adapter!!.notifyDataSetChanged()
                                recyclerView!!.requestLayout()




                            } catch (e: Exception){

                            }
                        } else {
                            try {
                                sortEventsByInterest(HomeActivity.test)
                                recyclerView.adapter = ListAdapter(HomeActivity.test, activity!!)
                                recyclerView.adapter!!.notifyDataSetChanged()
                                recyclerView!!.requestLayout()



                            }catch (e: Exception){

                            }

                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        //Failed to read
                    }
                })
            }
        }catch(e: Exception){
            System.out.println("175")
        }
    }

    private fun sortEventsByInterest(events: MutableList<EventData>) {
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