package com.davenet.notely

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signup.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        //Register new user
        val oldUser: TextView = findViewById(R.id.textView3)
        oldUser.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        //Register new user
        userRegister = findViewById(R.id.buttonSignUp)
        userRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {

        if (username.text.toString().isEmpty()) {
            username.error = "Please enter name"
            username.requestFocus()
            return
        }

        if (userEmail.text.toString().isEmpty()){
            userEmail.error = "Please enter email"
            userEmail.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(userEmail.text.toString()).matches()){
            userEmail.error = "Please enter valid email"
            userEmail.requestFocus()
            return
        }

        if (userPassword.text.toString().isEmpty()){
            userPassword.error = "Please enter password"
            userPassword.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(userEmail.text.toString(), userPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user!!.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                startActivity(Intent (this, LoginActivity::class.java))
                                finish()
                            }
                        }
                } else {
                    Toast.makeText(baseContext, "Sign Up failed. Try again later.",
                        Toast.LENGTH_SHORT).show()
                }

            }
    }
}