package com.davenet.notely

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.userPassword

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        //Register new user
        buttonRegister.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        //login user if credentials matches
        buttonLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {

        if (userEmailLogin.text.toString().isEmpty()){
            userEmailLogin.error = "Please enter email"
            userEmailLogin.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(userEmailLogin.text.toString()).matches()){
            userEmailLogin.error = "Please enter valid email"
            userEmailLogin.requestFocus()
            return
        }

        if (userPassword.text.toString().isEmpty()){
            userPassword.error = "Please enter password"
            userPassword.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(userEmailLogin.text.toString(), userPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {

                    updateUI(null)
                }
            }
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    private fun updateUI(currentUser: FirebaseUser?){
        if(currentUser != null) {
            if (currentUser.isEmailVerified) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    baseContext, "Please verify your  email.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
//        } else{
//            Toast.makeText(baseContext, "Login failed.",
//                Toast.LENGTH_SHORT).show()
//        }
    }
}