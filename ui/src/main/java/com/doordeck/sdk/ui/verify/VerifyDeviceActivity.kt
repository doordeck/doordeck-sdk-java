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
import com.github.doordeck.ui.R
import com.github.doordeck.ui.databinding.ActivityVerifyDeviceBinding
import com.doordeck.sdk.ui.BaseActivity

// screen responsible to send a new verification code and validate the user
internal class VerifyDeviceActivity : BaseActivity(), VerifyDeviceView {

    private lateinit var presenter: VerifyDevicePresenter

    private lateinit var binding: ActivityVerifyDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerifyDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvVerifyDesc.visibility = View.GONE
        presenter = VerifyDevicePresenter()
        presenter.onSendCode()
        setupListeners()
    }

    /**
     * Setup the listener on each field to move to the next one when a digit is entered
     */
    private fun setupListeners() {
        binding.tvReSendCode.setOnClickListener { presenter.onSendCode() }
        binding.tvSend.setOnClickListener { presenter.verifyCode(concatAllDigits()) }
        binding.ivClose.setOnClickListener { finish() }


        binding.edDigit1.onTextChanged {
            if (it.length == 1)
                binding.edDigit2.requestFocus()
        }

        binding.edDigit2.onTextChanged {
            if (it.length == 1)
                binding.edDigit3.requestFocus()
        }
        binding.edDigit2.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                //Perform Code
                if (binding.edDigit2.text.length == 1) {
                    binding.edDigit2.text.clear()
                } else binding.edDigit1.requestFocus()
            }
            false
        }
        binding.edDigit3.onTextChanged {
            if (it.length == 1)
                binding.edDigit4.requestFocus()
        }
        binding.edDigit3.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (binding.edDigit3.text.length == 1) {
                    binding.edDigit3.text.clear()
                } else binding.edDigit2.requestFocus()
            }
            false
        }

        binding.edDigit4.onTextChanged {
            if (it.length == 1)
                binding.edDigit5.requestFocus()
        }
        binding.edDigit4.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (binding.edDigit4.text.length == 1) {
                    binding.edDigit4.text.clear()
                } else binding.edDigit3.requestFocus()
            }
            false
        }


        binding.edDigit5.onTextChanged {
            if (it.length == 1)
                binding.edDigit6.requestFocus()
        }
        binding.edDigit5.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (binding.edDigit5.text.length == 1) {
                    binding.edDigit5.text.clear()
                } else binding.edDigit4.requestFocus()
            }
            false
        }

        binding.edDigit6.onTextChanged {
            if (it.length == 1)
                hideKeyboard()
        }
        binding.edDigit6.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (binding.edDigit6.text.length == 1) {
                    binding.edDigit6.text.clear()
                } else binding.edDigit5.requestFocus()
            }
            false
        }

        showKeyboard()
        binding.edDigit1.requestFocus()
    }

    private fun setCountdownTimer() {
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                hideVerifySend()
            }
        }
        timer.start()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edDigit6.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.edDigit1.postDelayed({
            binding.edDigit1.requestFocus()
            imm.showSoftInput(binding.edDigit1, 0)
        }, 100)
    }

    // concat all the digits to get the entered code, to validate
    private fun concatAllDigits(): String {
        return binding.edDigit1.text.toString() +
                binding.edDigit2.text.toString() +
                binding.edDigit3.text.toString() +
                binding.edDigit4.text.toString() +
                binding.edDigit5.text.toString() +
                binding.edDigit6.text.toString()
    }

    override fun setEmail(email: String) {
        binding.tvVerifyDesc.visibility = View.VISIBLE
        binding.tvVerifyDesc.text = String.format(resources.getString(R.string.verify_device_desc), email)
    }

    override fun setPhoneNumber(phone: String) {
        binding.tvVerifyDesc.visibility = View.VISIBLE
        binding.tvVerifyDesc.text = String.format(resources.getString(R.string.verify_device_desc), phone)
    }

    override fun setPhoneNumberWhatsapp(phone: String) {
        binding.tvVerifyDesc.visibility = View.VISIBLE
        binding.tvVerifyDesc.text = String.format(resources.getString(R.string.verify_device_desc_whatsapp), phone)
    }

    override fun noMethodDefined() {
        binding.tvVerifyDesc.visibility = View.VISIBLE
        binding.tvVerifyDesc.text = String.format(resources.getString(R.string.verify_device_desc_no_method))
    }

    override fun verifyCodeSuccess() {
        showVerifySend()
    }

    override fun verifyCodeFail() {
        showError(getString(R.string.code_not_send), getString(R.string.code_not_send_message))
    }

    private fun showVerifySend() {
        binding.tvReSendCode.setOnClickListener { }
        binding.tvReSendCode.text = getString(R.string.code_send)
        binding.tvReSendCodeSent.visibility = View.VISIBLE
        setCountdownTimer()
    }

    private fun hideVerifySend() {
        binding.tvReSendCode.setOnClickListener { presenter.onSendCode() }
        binding.tvReSendCode.text = getString(R.string.resend_code)
        binding.tvReSendCodeSent.visibility = View.GONE
    }

    private fun showError(title: String, message: String) {
        AlertDialog
            .Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.OK)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
