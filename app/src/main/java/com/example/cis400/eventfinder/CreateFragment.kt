package com.example.cis400.eventfinder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.flags.impl.DataUtils
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_create.view.*

class CreateFragment: Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create, container, false)
        view.btnCreate.setOnClickListener {
            checkFields()
        }


        return view
    }

    private fun checkFields(){
        if(view!!.etDate.text.toString().isNotEmpty()){
            if(view!!.etDescription.text.toString().isNotEmpty()){
                if(view!!.etImage.text.toString().isNotEmpty()){
                    if(view!!.etLocation.text.toString().isNotEmpty()){
                                if(view!!.etName.text.toString().isNotEmpty()){
                                    if(view!!.etTime.text.toString().isNotEmpty())
                                    {
                                        if(checkInputs())
                                        {
                                            if(view!!.etMediaYoutube.toString() != "")
                                            {
                                                val newYTInput : String = convertYoutubePlayBackID(view!!.etMediaYoutube.text.toString())
                                                val editable : Editable = SpannableStringBuilder(newYTInput)
                                                view!!.etMediaYoutube.text = editable
                                            }
                                            createEvent()
                                            Toast.makeText(activity, "The event has been created.", Toast.LENGTH_SHORT).show()
                                            clear()
                                        }
                                        return
                                    }
                                }

                    }
                }
            }
        }
        if(!view!!.etImage.text.toString().endsWith(".jpg") || !view!!.etImage.text.toString().endsWith(".png") || !view!!.etImage.text.toString().endsWith(".jpeg")){
            Toast.makeText(activity, "All fields need to be filled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkInputs(): Boolean{
        if(view!!.etImage.text.toString().endsWith(".jpg") || view!!.etImage.text.toString().endsWith(".png") || view!!.etImage.text.toString().endsWith(".jpeg")){
           return true
        }
        else{
            Toast.makeText(activity, "Header Image needs to end in .jpg, .png, or .jpeg", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun convertYoutubePlayBackID(s : String): String {
        try {
            if (s.contains("youtube.com/") || s.contains("www.youtube.com/") || s.contains("m.youtube.com/")) {
                return s.substring(s.indexOf("=") + 1)
            }
        } catch (e: Exception) {

        }
        return s
    }

    private fun createEvent(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("Events").child("Current")
        var key = mRef.push().key
        var newEvent = EventData(
            view!!.etDate.text.toString(),
            view!!.etDescription.text.toString(),
            view!!.etImage.text.toString(),
            0,
            view!!.etLocation.text.toString(),
            view!!.etImage.text.toString(),
            view!!.etMediaYoutube.text.toString(),
            view!!.etName.text.toString(),
            key!!,
            view!!.etTime.text.toString()
        )
        mRef.child(key).setValue(newEvent)
    }

    private fun clear(){
        view!!.etDate.setText("")
        view!!.etDescription.setText("")
        view!!.etImage.setText("")
        view!!.etLocation.setText("")
        view!!.etMediaYoutube.setText("")
        view!!.etName.setText("")
        view!!.etTime.setText("")
    }
}