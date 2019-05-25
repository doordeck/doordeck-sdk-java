package com.doordeck.sdk.ui.verify


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.doordeck.sdk.R
import com.doordeck.sdk.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_verify_device.*

// screen responsible to send a new verification code and validate the user
internal class VerifyDeviceActivity : BaseActivity(), VerifyDeviceView {



    private lateinit var presenter: VerifyDevicePresenter
    private var firstVerify: Boolean = true

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
        edDigit2.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //Perform Code
                if (edDigit2.text.length == 1){
                    edDigit2.text.clear();
                } else edDigit1.requestFocus()
            }
            false
        })
        edDigit3.onTextChanged {
            if (it.length == 1)
                edDigit4.requestFocus()
        }
        edDigit3.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (edDigit3.text.length == 1){
                edDigit3.text.clear();
            } else edDigit2.requestFocus()
            false
        })

        edDigit4.onTextChanged {
            if (it.length == 1)
                edDigit5.requestFocus()
        }
        edDigit4.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (edDigit4.text.length == 1){
                    edDigit4.text.clear();
                } else edDigit3.requestFocus()
            }
            false
        })


        edDigit5.onTextChanged {
            if (it.length == 1)
                edDigit6.requestFocus()
        }
        edDigit5.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (edDigit5.text.length == 1){
                    edDigit5.text.clear();
                } else edDigit4.requestFocus()
            }
            false
        })

        edDigit6.onTextChanged {
            if (it.length == 1)
                hideKeyboard()
        }
        edDigit6.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (edDigit6.text.length == 1){
                    edDigit6.text.clear();
                } else edDigit5.requestFocus()
            }
            false
        })

        showKeyboard()
        edDigit1.requestFocus()
    }

    private fun setCountdownTimer () {
        val timer = object: CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                hideVerifySend()
            }
        }
        timer.start()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edDigit6.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        edDigit1.postDelayed(Runnable {
            edDigit1.requestFocus()
            imm.showSoftInput(edDigit1, 0)
        }, 100)
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
    override fun verifyCodeSuccess() {
//       if (!this.firstVerify){
           showVerifySend()
//       } else firstVerify = false
    }

    override fun verifyCodeFail() {
//        if (!this.firstVerify){
        showError(getString(R.string.code_not_send), getString(R.string.code_not_send_message))
//        } else firstVerify = false
    }

    private fun showVerifySend () {
        tvReSendCode.setOnClickListener { }
        tvReSendCode.text = getString(R.string.code_send)
        tvReSendCodeSent.visibility = View.VISIBLE
        setCountdownTimer()
    }
    private fun hideVerifySend () {
        tvReSendCode.setOnClickListener { presenter.onSendCode() }
        tvReSendCode.text = getString(R.string.resend_code)
        tvReSendCodeSent.visibility = View.GONE
    }

    private fun showError(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton(getString(R.string.OK)){dialog, which ->
            dialog.dismiss()
        }.show()
    }

    // the code is valid, close the with and come back on the previous screen : The UnlockActivity
    override fun succeed() {
        finish()
    }

    override fun fail() {
        showError(getString(R.string.wrong_code), getString(R.string.wrong_code_message))
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
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }

}
