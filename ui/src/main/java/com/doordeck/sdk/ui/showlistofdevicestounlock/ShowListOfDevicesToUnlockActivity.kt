package com.doordeck.sdk.ui.showlistofdevicestounlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doordeck.sdk.R
import com.doordeck.sdk.databinding.ActivityListOfDevicesToUnlockBinding
import com.doordeck.sdk.dto.device.Device
import com.doordeck.sdk.jackson.Jackson
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.showlistofdevicestounlock.adapter.DevicesToUnlockAdapter
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity.Companion.COMING_FROM_DIRECT_UNLOCK
import com.doordeck.sdk.ui.utils.recyclerview.VerticalSpaceItemDecoration
import com.fasterxml.jackson.core.type.TypeReference


// screen responsible to display different locks to unlock
internal class ShowListOfDevicesToUnlockActivity : BaseActivity(), ShowListOfDevicesToUnlockView {

    private var presenter: ShowListOfDevicesToUnlockPresenter? = null

    private lateinit var binding: ActivityListOfDevicesToUnlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityListOfDevicesToUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        with(binding.recyclerView) {
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
        UnlockActivity.start(this, device, comingFrom = COMING_FROM_DIRECT_UNLOCK)
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
