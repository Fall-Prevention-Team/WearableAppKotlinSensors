package com.example.handheld


import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button

private const val TAG = "APP_MAIN_ACTIVITY"
// ... (Add other message types here as needed.)

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.Send).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                sendData();
            }

        })

    }

    private fun sendData() {

    }

    override fun onResume() {
        super.onResume()
    }


}