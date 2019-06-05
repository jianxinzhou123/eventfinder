package com.example.cis400.eventfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import java.lang.Exception
import android.speech.tts.TextToSpeech
import android.support.v4.app.FragmentActivity
import java.util.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.TimePicker
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalTime



class EventFragment: Fragment(){
    companion object {
        lateinit var eventInfo: EventData
        var events = mutableListOf<EventData>()
    }

    private lateinit var mTTS: TextToSpeech

    @SuppressLint("WrongConstant")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_event, container, false)
        var date : LocalDate? = try{LocalDate.parse(eventInfo!!.date)}catch (e: Exception) {LocalDate.now()}
        var time : LocalTime? = try{LocalTime.parse(eventInfo!!.time)}catch (e: Exception) {LocalTime.now()}
        val fragmentBar = view!!.collapsetoolbar
        if(RecyclerViewFragment.currORold == "old"){
            view!!.btnDelete.visibility = View.INVISIBLE
            view!!.btnStar.isEnabled = false
        }
        else{
            checkCreate()
        }
        if((activity as AppCompatActivity).supportActionBar != null){
            Picasso.with(context)
                    .load(eventInfo.image)
                    .error(R.drawable.logo_blk)
                    .into(view.app_bar_image)

            fragmentBar.title = eventInfo!!.name
            fragmentBar.subtitle = eventInfo!!.date + eventInfo!!.time
            fragmentBar.inflateMenu(R.menu.toolbar_fragment)
            fragmentBar.setOnMenuItemClickListener {

                when(it.itemId) {

                    R.id.shareEvent -> {

                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        var subject : String = "You are invited to " + eventInfo!!.name + " via Event Finder!"
                        var body : String = "Hey! I am inviting you to this awesome event (" + eventInfo!!.name + ") on " + eventInfo!!.date +
                                " at " + eventInfo!!.time + " in " + eventInfo!!.location +
                                " via EVENT FINDER. If you're interested, just click on the star button on the bottom to add interest! It's that easy!"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
                        shareIntent.putExtra(Intent.EXTRA_TEXT, body)
                        startActivity(Intent.createChooser(shareIntent, "Share Using"))

                    }

                    R.id.parseToCalendar -> {

                        val c : Calendar = Calendar.getInstance()
                        val year = c.get(Calendar.YEAR)
                        val month = c.get(Calendar.MONTH)
                        val day = c.get(Calendar.DAY_OF_MONTH)
                        val hour = c.get(Calendar.HOUR)
                        val minute = c.get(Calendar.MINUTE)+10
                        val pmOram = c.get(Calendar.AM_PM)



                        val startTime: Long = Calendar.getInstance().run {
                            set(year, month, day, hour, minute, pmOram)
                            timeInMillis
                        }

                        val calendarIntent : Intent = Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                                .putExtra(CalendarContract.Events.TITLE, eventInfo!!.name)
                                .putExtra(CalendarContract.Events.HAS_ALARM, true)
                                .putExtra(CalendarContract.Events.DESCRIPTION, "Start Date: " + eventInfo!!.date+"\nStart Time: "+ eventInfo!!.time+"\nOverview: "+eventInfo!!.description)
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, eventInfo!!.location)
                                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                        startActivity(calendarIntent)

                    }

                    else-> false
                }
                true
            }

        }

        var youtubeLink : String? = eventInfo.mediaYoutube
        view.tvDate.text = eventInfo.date
        view.tvDescription1.text = eventInfo.description
        view.tvTime.text = eventInfo.time
        interestCount(eventInfo.tableKey)


        if(eventInfo!!.mediaYoutube != "") {
            var youtubeSupportFrag = YouTubePlayerSupportFragment.newInstance()
            var player: YouTubePlayer?
            var transaction: FragmentManager = childFragmentManager

            var mOnInitializedListener = object : OnInitializedListener {
                override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
                    if (!p2) {
                        player = p1
                        player!!.setShowFullscreenButton(true)
                        player!!.cueVideo(youtubeLink)
                        /*player!!.setOnFullscreenListener (object : YouTubePlayer.OnFullscreenListener{
                            override fun onFullscreen(p0: Boolean) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }


                        })*/

                        player!!.setPlayerStateChangeListener(object : YouTubePlayer.PlayerStateChangeListener {
                            override fun onLoading() {
                                player!!.pause()
                            }


                            override fun onLoaded(s: String) {
                                player!!.pause()
                            }

                            override fun onAdStarted() {

                            }

                            override fun onVideoStarted() {
                                if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    player!!.setFullscreen(true)
                                    player!!.play()
                                }

                            }

                            override fun onVideoEnded() {
                                player!!.cueVideo(youtubeLink)
                            }


                            override fun onError(errorReason: YouTubePlayer.ErrorReason) {

                                if(errorReason == YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY)
                                {
                                    if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                        player!!.setFullscreen(true)
                                        player!!.play()
                                    }
                                    else {
                                        Toast.makeText(context, "Per Google policy, the video is now forcibly paused by overlay control. Make sure the YouTube Player is not covered!", Toast.LENGTH_LONG).show()
                                    }
                                }
                                else {

                                    Toast.makeText(context, "A video media has failed to load due to an error; possibly bad data.", Toast.LENGTH_LONG).show()

                                    childFragmentManager
                                            .beginTransaction()
                                            .remove(youtubeSupportFrag)
                                            .commit()
                                }
                            }
                        })
                    }


                }

                override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                    Log.d("YoutubePlayer", "Failed to load.")
                }
            }

            youtubeSupportFrag.initialize(YoutubeConfig().getAPIKey(), mOnInitializedListener)

            transaction
                    .beginTransaction()
                    .replace(R.id.youtubeFragmentContainer, youtubeSupportFrag)
                    .commit()
        }

        view.tvLocation.text = eventInfo.location
        view.tvName1.text = eventInfo.name
        interested(eventInfo)

        view.btnStar.setOnClickListener {
            checkIfAlreadyInterested(eventInfo)
        }


        mTTS = TextToSpeech(activity, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = mTTS.setLanguage(Locale.ENGLISH)
            } else {
                Log.e("TTS", "Initialization failed")
            }
        })

        view.tvNameHead.setOnLongClickListener{
            speak(eventInfo.name)
            true
        }

        view.tvTimeHead.setOnLongClickListener{
            speak(eventInfo.time)
            true
        }

        view.tvDescriptionHead.setOnLongClickListener{
            speak(eventInfo.description)
            true
        }

        view.tvLocationHead.setOnLongClickListener{
            speak(eventInfo.location)
            true
        }

        view.tvDateHead.setOnLongClickListener{
            speak(eventInfo.date)
            true
        }

        view.tvInterestedHead.setOnLongClickListener{
            speak(view.tvInterested.text.toString())
            true
        }

        view.btnDelete.setOnClickListener {
            var popup = PopupMenu(activity!!, view.btnDelete)
            popup.inflate(R.menu.popup_menu)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.getItemId()) {
                        R.id.action_delete -> {
                            view!!.btnStar.isEnabled = false
                            view!!.tvInterestedHead.text = "Interested (Locked due to protection)"
                            checkDeleteAccess()
                        }
                        R.id.action_dont_delete -> {
                        }
                        R.id.action_move_to_old ->{
                            view!!.btnStar.isEnabled = false
                            view!!.tvInterestedHead.text = "Interested (Locked due to protection)"
                            moveToOld()
                        }
                    }
                    return false
                }
            })
            popup.show()
        }
        return view
    }

    private fun interested(event: EventData){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("UserInfo").child(FirebaseAuth.getInstance().uid!!).child("Interested")

        mRef.addListenerForSingleValueEvent(object:
                ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var a = false

                for(p1 in p0.children){
                    var p2 = p1.getValue(EventData::class.java)
                    if(p2!!.tableKey == event.tableKey){
                        events.clear()
                        view!!.btnStar.setImageResource(android.R.drawable.btn_star_big_on)
                    }
                }
                events.clear()
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
    }

    private fun checkIfAlreadyInterested(event: EventData){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("UserInfo").child(FirebaseAuth.getInstance().uid!!).child("Interested")


        mRef.addListenerForSingleValueEvent(object:
                ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var a = false

                for(p1 in p0.children){
                    var p2 = p1.getValue(EventData::class.java)
                    if(p2!!.tableKey == event.tableKey){
                        removeInterest(event)
                        return
                    }
                }
                addInterest(event)
                events.clear()
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
    }

    private fun removeInterest(event: EventData){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("UserInfo")
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("Interested").child(event.tableKey).removeValue()
        //have to update interested count now
        removeFromInterest(event)
    }

    private fun addInterest(event: EventData){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("UserInfo")
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("Interested").child(event.tableKey).setValue(event)
        //have to update interest count now
        addToInterest(event)
    }

    private fun removeFromInterest(event: EventData){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("Events")
        var mRefCurrent = mRef.child("Current").child(eventInfo.tableKey)
        var mRefOld = mRef.child("Old").child(eventInfo.tableKey)


        mRefCurrent.addListenerForSingleValueEvent(object:
                ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    var p1 = p0.getValue(EventData::class.java)!!
                    var newEvent = EventData(
                            p1.date,
                            p1.description,
                            p1.image,
                            p1.interested - 1,
                            p1.location,
                            p1.mediaPhoto,
                            p1.mediaYoutube,
                            p1.name,
                            p1.tableKey,
                            p1.time
                    )
                    mRefCurrent.setValue(newEvent)
                    tvInterested.text = newEvent.interested.toString() + " people are interested."
                }catch (e: Exception){

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })

        mRefOld.addListenerForSingleValueEvent(object:
                ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    var p1 = p0.getValue(EventData::class.java)!!
                    var newEvent = EventData(
                            p1.date,
                            p1.description,
                            p1.image,
                            p1.interested - 1,
                            p1.location,
                            p1.mediaPhoto,
                            p1.mediaYoutube,
                            p1.name,
                            p1.tableKey,
                            p1.time
                    )
                    mRefOld.setValue(newEvent)
                    tvInterested.text = newEvent.interested.toString() + " people were interested\n\n\nYou can no longer modify interests to an old event."
                }catch (e: Exception){

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
        view!!.btnStar.setImageResource(android.R.drawable.btn_star_big_off)
    }

    private fun addToInterest(event: EventData){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("Events")
        var mRefCurrent = mRef.child("Current").child(eventInfo.tableKey)
        var mRefOld = mRef.child("Old").child(eventInfo.tableKey)

        mRefCurrent.addListenerForSingleValueEvent(object:
                ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    var p1 = p0.getValue(EventData::class.java)!!
                    var newEvent = EventData(
                            p1.date,
                            p1.description,
                            p1.image,
                            p1.interested + 1,
                            p1.location,
                            p1.mediaPhoto,
                            p1.mediaYoutube,
                            p1.name,
                            p1.tableKey,
                            p1.time
                    )
                    mRefCurrent.setValue(newEvent)
                    tvInterested.text = newEvent.interested.toString() + " people are interested."
                }catch (e: Exception){

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })

        mRefOld.addListenerForSingleValueEvent(object:
                ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    var p1 = p0.getValue(EventData::class.java)!!
                    var newEvent = EventData(
                            p1.date,
                            p1.description,
                            p1.image,
                            p1.interested + 1,
                            p1.location,
                            p1.mediaPhoto,
                            p1.mediaYoutube,
                            p1.name,
                            p1.tableKey,
                            p1.time
                    )
                    mRefOld.setValue(newEvent)
                    tvInterested.text = newEvent.interested.toString()+ " people were interested\n\n\nYou can no longer modify interests to an old event."
                }catch (e: Exception){

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
        view!!.btnStar.setImageResource(android.R.drawable.btn_star_big_on)
    }

    private fun interestCount(key: String){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefCurrent = mDatabase.child("Events").child("Current").child(key)

        mRefCurrent.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    var p1 = p0.getValue(EventData::class.java)!!
                    tvInterested.text = p1.interested.toString() + " people are interested."
                } catch (e: Exception){
                    //DNE
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        var mRefOld = mDatabase.child("Events").child("Old").child(key)

        mRefOld.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    var p1 = p0.getValue(EventData::class.java)!!
                    tvInterested.text = p1.interested.toString() +  " people were interested\n\n\nYou can no longer modify interests to an old event."
                } catch (e: Exception){
                    //DNE
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun speak(text: String) {
        mTTS.setSpeechRate(0.75f)
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onDestroy() {
        if (mTTS != null) {
            mTTS.stop()
            mTTS.shutdown()
        }

        super.onDestroy()
    }

    private fun checkDeleteAccess(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("UserInfo")
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("Create").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val permission = p0.getValue(CreateData::class.java)
                if(permission!!.access == "Granted"){
                    delete()
                }
                else{
                    Toast.makeText(context, "You do not have permission to delete events.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
    }

    private fun delete(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("Events")
        try{
            var mRefCurrent = mRef.child("Current").child(eventInfo.tableKey).removeValue()
        }catch(e: Exception){}

        try{
            var mRefOld = mRef.child("Old").child(eventInfo.tableKey).removeValue()
        }catch(e: Exception){}
        deleteFromInterest()
        Toast.makeText(context, "You have deleted the event: " + eventInfo.name, Toast.LENGTH_LONG).show()
        (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateFragment())
                .commit()
    }

    private fun deleteFromInterest(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("UserInfo")
        mRefUserInfo.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for(p1 in p0.children){
                    var userRef = mRefUserInfo.child(p1.key!!).child("Interested")
                    userRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            for(p1 in p0.children){
                                if(p1.key!! == eventInfo.tableKey){
                                    userRef.child(p1.key!!).removeValue()
                                }
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
    }

    private fun checkCreate(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("UserInfo")
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("Create").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val permission = p0.getValue(CreateData::class.java)
                if(permission!!.access != "Granted"){
                    view!!.btnDelete.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
    }

    private fun moveToOld(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("Events").child("Current").child(eventInfo.tableKey).removeValue()
        var mDatabase1 = FirebaseDatabase.getInstance().reference
        var mRefUserInfo1 = mDatabase.child("Events").child("Old").child(eventInfo.tableKey).setValue(eventInfo)
    }
}