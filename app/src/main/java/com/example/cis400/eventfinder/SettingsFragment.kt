package com.example.cis400.eventfinder

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        view.btnUpdate.setOnClickListener {
            updatePassword(etCurrent.text.toString(), etPassword1.text.toString(), etPassword2.text.toString())
        }

        return view
    }

    private fun updatePassword(current: String, pass1: String, pass2: String){
        if(current.isNotEmpty() && pass1.isNotEmpty() && pass2.isNotEmpty()){
            if(pass1 == pass2){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(FirebaseAuth.getInstance().currentUser!!.email!!, current)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                FirebaseAuth.getInstance().currentUser!!.updatePassword(pass1).addOnCompleteListener {
                                    if(it.isSuccessful){
                                        Toast.makeText(activity, "Updated password", Toast.LENGTH_SHORT).show()
                                        val editor = MainActivity.sharedpreferences.edit()
                                        editor.remove("PASSWORD")
                                        editor.commit()
                                        editor.putString("PASSWORD", pass1)
                                        editor.commit()
                                        etCurrent.setText("")
                                        etPassword1.setText("")
                                        etPassword2.setText("")
                                    }
                                    else{
                                        Toast.makeText(activity, "Could not update", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            else{
                                Toast.makeText(activity, "Incorrect information provided", Toast.LENGTH_SHORT).show()
                            }
                        }
            }
            else{
                Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(activity, "Fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}