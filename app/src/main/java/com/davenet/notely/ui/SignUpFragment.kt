package com.davenet.notely.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.davenet.notely.R
import com.davenet.notely.util.hideKeyboard
import com.davenet.notely.util.inputValidation
import com.davenet.notely.util.setupLoadingDialog
import com.davenet.notely.util.showErrorDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_signup.*

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().apply {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            toolbar.isVisible = false
        }
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        loadingDialog = setupLoadingDialog(requireContext(), requireActivity())
    }

    private fun initListeners() {
        //Login already registered user
        buttonSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        //Register new user
        buttonSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        if (!inputValidation(userEmail, userPassword)) {
            hideKeyboard(view, requireContext())
            loadingDialog.show()
            auth.createUserWithEmailAndPassword(
                userEmail.text.toString(),
                userPassword.text.toString()
            )
                .addOnCompleteListener(requireActivity()) { task ->
                    loadingDialog.dismiss()
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user!!.sendEmailVerification()
                            .addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                                }
                            }
                    } else {
                        showErrorDialog(
                            task.exception?.message,
                            requireContext(),
                            requireActivity()
                        )
                    }
                }
        }
    }
}