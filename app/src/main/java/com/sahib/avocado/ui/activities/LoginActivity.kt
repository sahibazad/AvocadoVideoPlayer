package com.sahib.avocado.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sahib.avocado.R
import com.sahib.avocado.utils.hideKeyboard

private lateinit var usernameEditText: EditText
private lateinit var passwordEditText: EditText
private lateinit var rememberMeCheckBox: CheckBox
private lateinit var loginButton: Button
private lateinit var signUpTextView: TextView

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews();
        initClicks();
    }

    private fun initViews() {
        usernameEditText = findViewById(R.id.edit_username)
        passwordEditText = findViewById(R.id.edit_password)
        rememberMeCheckBox = findViewById(R.id.checkbox_remember_me)
        loginButton = findViewById(R.id.button_login)
        signUpTextView = findViewById(R.id.text_signup)
    }

    private fun initClicks() {
        loginButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.button_login -> {
                if (validateInputs()) {
                    successfulLogin()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        usernameEditText.error = null
        passwordEditText.error = null

        if (usernameEditText.text.toString().isEmpty()) {
            usernameEditText.error = "Input valid username."
            return false
        }

        if (passwordEditText.text.toString().isEmpty()) {
            passwordEditText.error = "Input valid password."
            return false
        }

        return true
    }

    private fun successfulLogin() {
        hideKeyboard()
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        //TODO save values to shared pref and do and API call to move to the next screen
    }
}