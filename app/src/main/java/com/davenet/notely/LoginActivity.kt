package com.davenet.notely

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.userPassword
import kotlinx.android.synthetic.main.error_dialog.view.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: AlertDialog

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

        //set up loading dialog
        val builder = AlertDialog.Builder(this)
        val viewGroup: ViewGroup = findViewById(android.R.id.content)
        val dialogView: View =
            LayoutInflater.from(this).inflate(R.layout.loading_dialog, viewGroup, false)
        builder.setView(dialogView)
        loadingDialog = builder.create()
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

        loadingDialog.show()

        auth.signInWithEmailAndPassword(userEmailLogin.text.toString(), userPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    showErrorDIalog(task.exception?.message)
                }
            }
    }

    private fun showErrorDIalog(message: String?) {
        if (message != null) {
            val builder = AlertDialog.Builder(this)
            val viewGroup: ViewGroup = findViewById(android.R.id.content)
            val dialogView: View = LayoutInflater.from(this).inflate(R.layout.error_dialog, viewGroup, false)
            builder.setView(dialogView)
            val dialog: AlertDialog = builder.create()
            dialogView.apply {
                dialogMessage.text = message
                dismissDialogButton.setOnClickListener {
                    dialog.dismiss()
                }
            }
            dialog.show()
        }

    }

    override fun onResume() {
        super.onResume()
        val currentUser = auth.currentUser
        updateUI(currentUser)
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
    }
}