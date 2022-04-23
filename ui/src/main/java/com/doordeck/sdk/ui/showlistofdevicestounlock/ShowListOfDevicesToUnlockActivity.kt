package com.doordeck.sdk.ui.showlistofdevicestounlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.doordeck.sdk.R
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.jackson.Jackson
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.showlistofdevicestounlock.adapter.DevicesToUnlockAdapter
import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.android.synthetic.main.activity_list_of_devices_to_unlock.*
import kotlinx.android.synthetic.main.activity_unlock.*
import com.doordeck.sdk.R2.id.recyclerView

import com.doordeck.sdk.R2.attr.layoutManager

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import com.doordeck.sdk.R2
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.doordeck.sdk.ui.utils.recyclerview.VerticalSpaceItemDecoration


// screen responsible to display different locks to unlock
internal class ShowListOfDevicesToUnlockActivity : BaseActivity(), ShowListOfDevicesToUnlockView {

    private var presenter: ShowListOfDevicesToUnlockPresenter? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_devices_to_unlock)

        presenter = ShowListOfDevicesToUnlockPresenter(devices)
    }

    override fun onBackPressed() {
        finish()
    }

    public override fun onStart() {
        super.onStart()
        presenter?.onStart(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter = null
    }

    override fun showDevices(devices: List<Device>) {
        with(recyclerView?:return) {
            adapter = DevicesToUnlockAdapter(devices) {
                goToUnlockDevice(it)
            }

            while (itemDecorationCount > 0) {
                removeItemDecorationAt(0)
            }
            addItemDecoration(VerticalSpaceItemDecoration(resources.getDimension(R.dimen.list_devices_to_unlock_separator).toInt()))
        }
    }

    private fun goToUnlockDevice(device: Device) {
        UnlockActivity.start(this, device)
    }

    private val devices: List<Device>
        get() = intent.extras?.getString(LIST_OF_DEVICES)?.let {
            val om = Jackson.sharedObjectMapper()
            return@let om.readValue<List<Device>?>(it, object : TypeReference<List<Device>>() {})
        } ?: listOf()

    companion object {

        private const val LIST_OF_DEVICES = "LIST_OF_DEVICES"

        fun start(context: Context, devices: List<Device>) {
            val starter = Intent(context, ShowListOfDevicesToUnlockActivity::class.java)
            val om = Jackson.sharedObjectMapper()
            starter.putExtra(LIST_OF_DEVICES, om.writeValueAsString(ArrayList(devices)))
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }

}
