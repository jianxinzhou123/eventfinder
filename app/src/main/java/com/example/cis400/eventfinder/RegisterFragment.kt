package com.example.cis400.eventfinder

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register.view.*


class RegisterFragment: Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        view.btnRegister.setOnClickListener {
            register(view.etFirst.text.toString(), view.etLast.text.toString(), view.etEmail.text.toString(), view.etPassword1.text.toString(), view.etPassword2.text.toString())
        }

        view.tvLogin.setOnClickListener {
            activity!!.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom)
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }

        return view
    }

    private fun register(first: String, last: String, email: String, pass1: String, pass2: String) {
        if (first.isNotEmpty() && last.isNotEmpty() && email.isNotEmpty() && pass1.isNotEmpty() && pass2.isNotEmpty()) {
            if (pass1 == pass2) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    email,
                    pass1
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(activity, "Registered Successfully", Toast.LENGTH_SHORT).show()
                        login(email, pass1)
                        createChildren(first, last)
                    } else {
                        Toast.makeText(activity, "Error Registering: Make sure the password is above 6 characters and that the email is valid.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "Fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(email: String, password: String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val editor = MainActivity.sharedpreferences.edit()
                    editor.putString("LOGIN", "Logged In")
                    editor.commit()
                    editor.putString("EMAIL", email)
                    editor.commit()
                    editor.putString("PASSWORD", password)
                    editor.commit()
                    startActivity(Intent(activity, HomeActivity::class.java))
                }
                else{
                    Toast.makeText(activity, "Error Logging in after registering", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createChildren(first: String, last: String){
        var UInfo = UserData(last, first)
        var UInterested = mutableListOf<EventData>()
        var UCreate = CreateData("Not Granted")

        var mDatabase = FirebaseDatabase.getInstance().reference
        var mRefUserInfo = mDatabase.child("UserInfo")
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("UserInfo").setValue(UInfo)
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("Interested").setValue(UInterested)
        mRefUserInfo.child(FirebaseAuth.getInstance().uid!!).child("Create").setValue(UCreate)
    }
}