package com.example.handheld


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import java.util.*

private const val TAG = "APP_MAIN_ACTIVITY"


val MY_UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
private var btService : BluetoothConnectionService ?= null
// ... (Add other message types here as needed.)

class MainActivity : Activity() {

    var BTadapter : BluetoothAdapter ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btService?.startService()

        findViewById<Button>(R.id.Send).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                sendData();
            }

        })

    }

    private fun sendData() {

    }

    /*private fun enableDisableBT() {
        if (BTadapter == null){
            Log.d(TAG, "enableDisableBluetooth: Adapter is null.")
        }
        if (BTadapter?.isEnabled == false){
            val enableIntent:Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableIntent)
            val IntentFilter BT
        }
        if (){

        }
    }
*/

    override fun onResume() {
        super.onResume()

    }

    fun btnEnableDisable_Discoverable(view: View) {}
    fun btnDiscover(view: View) {}


}