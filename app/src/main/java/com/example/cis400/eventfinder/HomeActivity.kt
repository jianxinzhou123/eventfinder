package com.example.cis400.eventfinder

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.SearchView
import android.widget.Toast
import com.example.cis400.eventfinder.EventFragment.Companion.eventInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_home.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    companion object {
        var searched = false
        var test = mutableListOf<EventData>()
        var a = true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(homebar)
        window.setBackgroundDrawableResource(R.drawable.bg)
        loadUsersName()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, homebar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        notification()

        homebar.logo = null
        var webv: WebView = findViewById(R.id.webView)
        webv.loadUrl("https://www.google.com/search?q=events+near+me")
        webv.settings.javaScriptEnabled = true
        webv.settings.loadWithOverviewMode = true
        webv.settings.useWideViewPort = true
    }

    private fun notification(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRef = mDatabase.child("Events").child("Current")

        val notificationManager = this!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mRef.addChildEventListener(object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d("DATABASE!", "Node moved.")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d("DATABASE!", "Node changed.")

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                var notificationID = 2
                var channelID = "notification_remove_event"
                var channelName : CharSequence = "Event No Longer Available"
                var importance = NotificationManager.IMPORTANCE_HIGH
                var mChannel = NotificationChannel(channelID, channelName, importance)
                notificationManager?.createNotificationChannel(mChannel)
                val notification = Notification.Builder(this@HomeActivity, channelID)
                        .setContentTitle("Event Removed: ${p0.getValue(EventData::class.java)!!.name}")
                        .setContentText("Event Finder has removed a current event: ${p0.getValue(EventData::class.java)!!.name} from the roster.\nThis will no longer show up in your interests or take new interest.")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .build()

                notificationManager!!.notify(notificationID, notification)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Create Event", "Failed to add event.")
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                var notificationID = 1
                var channelID = "notification_create_event"
                var channelName : CharSequence = "Event Created"
                var importance = NotificationManager.IMPORTANCE_HIGH
                var mChannel = NotificationChannel(channelID, channelName, importance)
                notificationManager?.createNotificationChannel(mChannel)
                val notification = Notification.Builder(this@HomeActivity, channelID)
                        .setContentTitle("New Event Added: ${p0.getValue(EventData::class.java)!!.name} ")
                        .setContentText("Event Finder has a found new event: ${p0.getValue(EventData::class.java)!!.name}. Check it out!")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .build()

                notificationManager!!.notify(notificationID, notification)
            }

        })
    }


    override fun onStop(){
        super.onStop()
        if(a) {
            System.exit(0)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.events -> {
                fragment_container.removeAllViews()
                RecyclerViewFragment.currORold = "curr"
                RecyclerViewFragment.currentEvents.clear()
                RecyclerViewFragment.events.clear()
                searched = false
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, RecyclerViewFragment())
                        .commit()

            }
            R.id.event_history -> {
                fragment_container.removeAllViews()
                RecyclerViewFragment.currORold = "old"
                RecyclerViewFragment.oldEvents.clear()
                RecyclerViewFragment.events.clear()
                searched = false
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, RecyclerViewFragment())
                        .commit()
            }
            R.id.settings -> {
                RecyclerViewFragment.currORold = "curr"
                fragment_container.removeAllViews()
                RecyclerViewFragment.events.clear()
                supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_fade_out)
                        .replace(R.id.fragment_container, SettingsInterestedFragment())
                        .commit()
            }
            R.id.sign_out -> {
                a = false
                FirebaseAuth.getInstance().signOut()
                val editor = MainActivity.sharedpreferences.edit()
                editor.remove("LOGIN")
                editor.commit()
                editor.remove("EMAIL")
                editor.commit()
                editor.remove("PASSWORD")
                editor.commit()
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.create_event -> {
                var mDatabase = FirebaseDatabase.getInstance().reference
                var mRefUserInfo = mDatabase.child("UserInfo")
                mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("Create").addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val permission = p0.getValue(CreateData::class.java)
                        if(permission!!.access == "Granted"){
                            fragment_container.removeAllViews()
                            RecyclerViewFragment.events.clear()
                            supportFragmentManager
                                    .beginTransaction()
                                    .setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_fade_out)
                                    .replace(R.id.fragment_container, CreateFragment())
                                    .commit()
                        }
                        else{
                            Toast.makeText(this@HomeActivity, "You do not have permission to create event. Contact an admin for event management.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        //Failed to read
                    }
                })
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadUsersName(){
        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("UserInfo")
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("UserInfo").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(UserData::class.java)
                try {
                    tvTheirName!!.text = user!!.first + " " + user!!.last
                    tvTheirEmail!!.text = FirebaseAuth.getInstance().currentUser!!.email
                }catch(e: Exception){}
            }

            override fun onCancelled(p0: DatabaseError) {
                //Failed to read
            }
        })
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_activity, menu)
        val searchItem = menu!!.findItem(R.id.menuSearch)
        val mapItem = menu!!.findItem(R.id.menuMaps)
        var location : Uri

        mapItem.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.menuMaps -> {
                    location = try {
                        Uri.parse("geo:0,0?q=" + eventInfo!!.location)
                    } catch(e: Exception){
                        Uri.parse("geo:0,0?q=")
                    }

                    val mapIntent = Intent(Intent.ACTION_VIEW, location)
                    startActivity(mapIntent)

                }
                else -> false

            }
            true
        }
        var searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search event..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isEmpty()){
                    searched = false
                    if(RecyclerViewFragment.events.isNotEmpty()) {
                        supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment_container, RecyclerViewFragment())
                                .commit()
                    }
                }
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                if(RecyclerViewFragment.events.size == 0){
                    Toast.makeText(this@HomeActivity, "Open Current Events/Past Events to search.", Toast.LENGTH_SHORT).show()
                }
                else{
                    System.out.println(query + "TEST")
                    test.clear()
                    var i: Int = 0
                    while(i < RecyclerViewFragment.events.size){
                        if(RecyclerViewFragment.events[i].name!!.toUpperCase().contains(query.toUpperCase())){
                            if(!test.contains(RecyclerViewFragment.events[i])) {
                                test.add(RecyclerViewFragment.events[i])
                            }
                        }
                        else{

                        }
                        searched = true
                        i++
                    }
                    if(query.isEmpty()){
                        searched = false
                    }
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_container, RecyclerViewFragment())
                            .commit()
                }
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }
}