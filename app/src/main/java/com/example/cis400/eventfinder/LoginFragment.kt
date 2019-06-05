package com.example.cis400.eventfinder

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.view.*

class LoginFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.btnLogin.setOnClickListener {
            login(view.etEmail.text.toString(), view.etPassword.text.toString())
        }

        view.tvRegister.setOnClickListener {
            activity!!.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom)
                .replace(R.id.fragment_container, RegisterFragment())
                .commit()
        }
        return view
    }

    private fun login(email: String, password: String){
        if(email.isNotEmpty() && password.isNotEmpty()){
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
                        Toast.makeText(activity, "Incorrect information provided", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        else{
            Toast.makeText(activity, "Fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}