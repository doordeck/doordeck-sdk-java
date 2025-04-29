package com.doordeck.sdk.ui.showlistofdevicestounlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doordeck.multiplatform.sdk.model.responses.LockResponse
import com.doordeck.sdk.common.utils.json
import com.github.doordeck.ui.R
import com.github.doordeck.ui.databinding.ActivityListOfDevicesToUnlockBinding
import com.doordeck.sdk.ui.BaseActivity
import com.doordeck.sdk.ui.showlistofdevicestounlock.adapter.DevicesToUnlockAdapter
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.doordeck.sdk.ui.unlock.UnlockActivity.Companion.COMING_FROM_DIRECT_UNLOCK
import com.doordeck.sdk.ui.utils.recyclerview.VerticalSpaceItemDecoration


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

    override fun showDevices(devices: List<LockResponse>) {
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

    private fun goToUnlockDevice(device: LockResponse) {
        UnlockActivity.start(this, device, comingFrom = COMING_FROM_DIRECT_UNLOCK)
    }

    private val devices: List<LockResponse>
        get() = intent.extras?.getString(LIST_OF_DEVICES)?.let {
            return@let json.decodeFromString<List<LockResponse>>(it)
        } ?: listOf()

    companion object {

        private const val LIST_OF_DEVICES = "LIST_OF_DEVICES"

        fun start(context: Context, devices: List<LockResponse>) {
            val starter = Intent(context, ShowListOfDevicesToUnlockActivity::class.java)
            starter.putExtra(LIST_OF_DEVICES, json.encodeToString(devices))
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }
    }

}
