package com.example.wearosapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import com.example.wearosapp.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration.Companion.seconds
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import com.google.android.gms.wearable.*
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity(), SensorEventListener, AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private var activityContext: Context? = null

    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private var mobileDeviceConnected: Boolean = false


    // Payload string items
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"

    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"


    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private var data: ByteArray = byteArrayOf(1, 2)
    var myAccelerometer : Sensor ?= null
    var myGyroScope : Sensor ?= null
    var mySensorManager : SensorManager ?= null
    private lateinit var binding : ActivityMainBinding
    var SensorData = ArrayList<String>()
    var bytes : ByteArray = ByteArray(1024)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        mySensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        myGyroScope = mySensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        myAccelerometer = mySensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        activityContext = this

        // Enables Always-on
        ambientController = AmbientModeSupport.attach(this)


        //On click listener for sendmessage button
        binding.sendmessageButton.setOnClickListener {
            if (mobileDeviceConnected) {
                if (data.isNotEmpty()) {

                    val nodeId: String = messageEvent?.sourceNodeId!!
                    // Set the data of the message to be the bytes of the Uri.
                    val payload: ByteArray = data

                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)

                    binding.deviceconnectionStatusTv.visibility = View.GONE

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("send1", "Message sent successfully")
                            val sbTemp = StringBuilder()
                            sbTemp.append("\n")
                            sbTemp.append(data.toString())
                            sbTemp.append(" (Sent to mobile)")
                            Log.d("receive1", " $sbTemp")
                            binding.status.append(sbTemp)

                            binding.statusScrollView.requestFocus()
                            binding.statusScrollView.post {
                                binding.statusScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                            }
                        } else {
                            Log.d("send1", "Message failed.")
                        }
                    }
                } else {
                    Toast.makeText(
                        activityContext,
                        "Message content is empty. Please enter some message and proceed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mySensorManager!!.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_UI)
            mySensorManager!!.registerListener(this, myGyroScope, SensorManager.SENSOR_DELAY_UI)
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onPause() {
        super.onPause()
        try {
            mySensorManager!!.unregisterListener(this)
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun onSensorChanged(p0: SensorEvent?) {
        val accelx = findViewById<TextView>(R.id.accel_val_x)
        val accely = findViewById<TextView>(R.id.accel_val_y)
        val accelz = findViewById<TextView>(R.id.accel_val_z)

        val gyrox = findViewById<TextView>(R.id.gyro_val_x)
        val gyroy = findViewById<TextView>(R.id.gyro_val_y)
        val gyroz = findViewById<TextView>(R.id.gyro_val_z)
        val currentTime = Date()


        if ((currentTime.time.seconds.inWholeSeconds.toInt() % 5) == 0) {
            if ((p0 != null) && (p0.sensor.type == Sensor.TYPE_ACCELEROMETER)) {
                accelx.text = p0.values[0].toString()
                accely.text = p0.values[1].toString()
                accelz.text = p0.values[2].toString()
                SensorData.add(p0.values.toString())
                for (i in 0..bytes.size){
                    bytes[i] = SensorData[i].toByte()
                }
//                btCntService?.write(bytes)
        /*        fileWriter.append(p0.values.toString(), 0, 2)*/

            }else if((p0 != null) && (p0.sensor.type == Sensor.TYPE_GYROSCOPE)){
                gyrox.text = p0.values[0].toString()
                gyroy.text = p0.values[1].toString()
                gyroz.text = p0.values[2].toString()
                /*fileWriter.append(p0.values.toString(), 0, 2)*/
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    override fun getAmbientCallback(): AmbientCallback = MyAmbientCallback()
    private inner class MyAmbientCallback : AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
        }
        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
        }

        override fun onExitAmbient() {
            super.onExitAmbient()
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
    }

    override fun onMessageReceived(p0: MessageEvent) {
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() A message from watch was received:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s1
            )

            //Send back a message back to the source node
            //This acknowledges that the receiver activity is open
            if (messageEventPath.isNotEmpty() && messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                try {
                    // Get the node id of the node that created the data item from the host portion of
                    // the uri.
                    val nodeId: String = p0.sourceNodeId.toString()
                    // Set the data of the message to be the bytes of the Uri.
                    val returnPayloadAck = wearableAppCheckPayloadReturnACK
                    val payload: ByteArray = returnPayloadAck.toByteArray()

                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)

                    Log.d(
                        TAG_MESSAGE_RECEIVED,
                        "Acknowledgement message successfully with payload : $returnPayloadAck"
                    )

                    messageEvent = p0
                    mobileNodeUri = p0.sourceNodeId

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message sent successfully")
                            binding.status.visibility = View.VISIBLE

                            val sbTemp = StringBuilder()
                            sbTemp.append("\nMobile device connected.")
                            Log.d("receive1", " $sbTemp")
                            binding.status.append(sbTemp)

                            mobileDeviceConnected = true

                            binding.sendmessageButton.visibility = View.VISIBLE
                            binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                            binding.deviceconnectionStatusTv.text = "Mobile device is connected"
                        } else {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message failed.")
                        }
                    }
                } catch (e: Exception) {
                    Log.d(
                        TAG_MESSAGE_RECEIVED,
                        "Handled in sending message back to the sending node"
                    )
                    e.printStackTrace()
                }
            }//emd of if
            else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                try {
                    binding.status.visibility = View.VISIBLE
                    binding.sendmessageButton.visibility = View.VISIBLE
                    binding.deviceconnectionStatusTv.visibility = View.GONE

                    val sbTemp = StringBuilder()
                    sbTemp.append("\n")
                    sbTemp.append(s1)
                    sbTemp.append(" - (Received from mobile)")
                    Log.d("receive1", " $sbTemp")
                    binding.status.append(sbTemp)


                    binding.statusScrollView.requestFocus()
                    binding.statusScrollView.post {
                        binding.statusScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG_MESSAGE_RECEIVED, "Handled in onMessageReceived")
            e.printStackTrace()
        }
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
    }

}