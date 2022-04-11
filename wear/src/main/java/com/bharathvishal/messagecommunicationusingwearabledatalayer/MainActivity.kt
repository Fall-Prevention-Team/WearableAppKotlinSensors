package com.bharathvishal.messagecommunicationusingwearabledatalayer

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import com.bharathvishal.messagecommunicationusingwearabledatalayer.databinding.ActivityMainBinding
import com.google.android.gms.wearable.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener,
    SensorEventListener {
    var myAccelerometer : Sensor ?= null
    var mySensorManager : SensorManager ?= null
    var textviewCounter : Int = 0
    private var activityContext: Context? = null

    private lateinit var binding: ActivityMainBinding

    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private var mobileDeviceConnected: Boolean = false
    // Payload string items
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"

    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"


    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private val MAX_COUNT = 60
    private var sendableData: ByteArray = ByteArray(MAX_COUNT * 4)
    private var count = 0
    private var timeout = 0;
    private var fall : String ?= null
    private var recording : Boolean = false
    private var recordingFall : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mySensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        myAccelerometer = mySensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        activityContext = this

        // Enables Always-on
        ambientController = AmbientModeSupport.attach(this)
        binding.manualFallDetection.isActivated = false
        binding.manualFallDetection.visibility = View.INVISIBLE


        //On click listener for sendmessage button
        binding.recordFallButton.setOnClickListener {
            if (mobileDeviceConnected) {
                if (recording == false) {
                    timeout = 0
                    count = 0
                    recordingFall = true
                    recording = true
                    binding.recordFallButton.text = "Recording fall... "

                }
            }
        }
        binding.recordNotFallButton.setOnClickListener {
            if (mobileDeviceConnected) {
                if (recording == false) {
                    timeout = 0
                    count = 0
                    recording = true
                    binding.recordNotFallButton.text = "Recording not fall... "

                }else if(recording == true){
                    timeout = 0
                    count = 0
                    binding.recordNotFallButton.text = "Record not fall "
                    recording = false
                }
            }
        }
        binding.manualFallDetection.setOnClickListener {
            if (mobileDeviceConnected) {
                if (recording == true) {
                    timeout = 0
                    count = 0
                    recording = true
                    binding.recordNotFallButton.text = "Recording not fall... "

                }else if(recording == true){
                    timeout = 0
                    count = 0
                    binding.recordNotFallButton.text = "Record not fall "
                    recording = false
                }
            }
        }

    }



    private fun floatToByteArray (value: Float): ByteArray {
        var returnValue:ByteArray = ByteBuffer.allocate(4).putFloat(value).array()
        return returnValue
    }
    var textBuffer = arrayOf(" ", " ", " ")


    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(p0: SensorEvent?) {
        if (textviewCounter == 3){
            textviewCounter = 0
        }
        if ((p0 != null) && (p0.sensor.type == Sensor.TYPE_ACCELEROMETER) && (recording == true)) {
            val i = textviewCounter
            storeData(p0)
            if (textviewCounter == 0) {
                textBuffer[i] = "Accelerometer: " + p0.values[0].toString() + ", " + p0.values[1].toString() + ", " + p0.values[2].toString()
                textviewCounter++
            }else{
                textviewCounter++
                textBuffer[i] = "Accelerometer: " + p0.values[0].toString() + ", " + p0.values[1].toString() + ", " + p0.values[2].toString()
            }
            if (textviewCounter == 0) {
                binding.sensordatatexttop.text = textBuffer[0]
            }
            if (textviewCounter == 1) {
                binding.sensordatatextmid.text = textBuffer[1]
            }
            if (textviewCounter  == 2) {
                binding.sensordatatextbot.text = textBuffer[2]
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    private fun storeData(p0: SensorEvent){
        timeout += 1
        if (timeout == 100){
            if (count < MAX_COUNT){
                for (value in 0..p0.values.size-1){
                    Log.d("Sensor value " + value.toString(), p0.values[value].toString())
                    for (some in 0..3){
                        sendableData[some + count * 4] = floatToByteArray(p0.values[value])[some]
                    }
                    count += 1
                }
            }else{
                Log.d("sendable data size", sendableData.size.toString())
                sendData(sendableData)
                var buffer = ByteBuffer.wrap(sendableData)
                buffer.clear()
                count = 0
            }
            timeout = 0
        }
    }

    @SuppressLint("SetTextI18n")
    private fun sendData(data:ByteArray){
        if (mobileDeviceConnected) {
            val debugString = String(data)
            Log.d("data is not empty", debugString)
            if (data.isNotEmpty()) {
                val nodeId: String = messageEvent?.sourceNodeId!!
                // Set the data of the message to be the bytes of the Uri.
                val payload: ByteArray = data
                val debugString1 = String(data)
                Log.d("payload", debugString1)
                // Send the rpc
                // Instantiates clients without member variables, as clients are inexpensive to
                // create. (They are cached and shared between GoogleApi instances.)
                val sendMessageTask =
                    Wearable.getMessageClient(activityContext!!)
                        .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)

                sendMessageTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("send1", "Message sent successfully")
                        val sbTemp = StringBuilder()
                        sbTemp.append("\n")

                        sbTemp.append(" (Sent to mobile)")
                        Log.d("receive1", " $sbTemp")

                    } else {
                        Log.d("send1", "Message failed.")
                    }
                }
            } else {
                Toast.makeText(
                    activityContext,
                    "Data object is empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        if (recordingFall ==true ){
            recordingFall = false
            recording = false
            binding.recordFallButton.text = "Record fall"
        }
    }


    override fun onDataChanged(p0: DataEventBuffer) {
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
    }


    @SuppressLint("SetTextI18n")
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


                            val sbTemp = StringBuilder()
                            sbTemp.append("\nMobile device connected.")
                            Log.d("receive1", " $sbTemp")


                            mobileDeviceConnected = true


                            binding.recordFallButton.visibility = View.VISIBLE


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
                    val sbTemp = StringBuilder()
                    sbTemp.append("\n")
                    sbTemp.append(s1)
                    sbTemp.append(" - (Received from mobile)")
                    Log.d("receive1", " $sbTemp")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG_MESSAGE_RECEIVED, "Handled in onMessageReceived")
            e.printStackTrace()
        }
    }


    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        try {
            mySensorManager!!.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_UI)
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

}
