package com.doordeck.sdk.ui.verify


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import com.doordeck.sdk.R
import com.doordeck.sdk.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_verify_device.*

// screen responsible to send a new verification code and validate the user
internal class VerifyDeviceActivity : BaseActivity(), VerifyDeviceView {


    private lateinit var presenter: VerifyDevicePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_device)
        presenter = VerifyDevicePresenter()
        presenter.onSendCode()
        setupListeners()
    }

    /**
     * Setup the listener on each field to move to the next one when a digit is entered
     */
    private fun setupListeners() {
        tvReSendCode.setOnClickListener { presenter.onSendCode() }
        tvSend.setOnClickListener { presenter.verifyCode(concatAllDigits()) }
        ivClose.setOnClickListener { finish() }

        edDigit1.onTextChanged {
            if (it.length == 1)
                edDigit2.requestFocus()
        }

        edDigit2.onTextChanged {
            if (it.length == 1)
                edDigit3.requestFocus()
        }

        edDigit3.onTextChanged {
            if (it.length == 1)
                edDigit4.requestFocus()
        }

        edDigit4.onTextChanged {
            if (it.length == 1)
                edDigit5.requestFocus()
        }


        edDigit5.onTextChanged {
            if (it.length == 1)
                edDigit6.requestFocus()
        }

        edDigit6.onTextChanged {
            if (it.length == 1)
                hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edDigit6.windowToken, 0)
    }

    // concat all the digits to get the entered code, to validate
    private fun concatAllDigits(): String {
        return edDigit1.text.toString() +
                edDigit2.text.toString() +
                edDigit3.text.toString() +
                edDigit4.text.toString() +
                edDigit5.text.toString() +
                edDigit6.text.toString()
    }

    override fun setEmail(email: String) {
        tvVerifyDesc.text = String.format(resources.getString(R.string.verify_device_desc), email)
    }

    override fun setPhoneNumber(phone: String) {
        tvVerifyDesc.text = String.format(resources.getString(R.string.verify_device_desc), phone)
    }

    override fun noMethodDefined() {
        tvVerifyDesc.text = String.format(resources.getString(R.string.verify_device_desc_no_method))
    }

    // the code is valid, close the with and come back on the previous screen : The UnlockActivity
    override fun succeed() {
        finish()
    }


    override fun onStart() {
        super.onStart()
        presenter.onStart(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }


    companion object {

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, VerifyDeviceActivity::class.java)
            context.startActivity(starter)
        }
    }

}
