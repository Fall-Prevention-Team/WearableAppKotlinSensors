package com.example.wearosapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.wearosapp.databinding.ActivityMainBinding
import java.io.File
import java.io.*
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration.Companion.seconds


class MainActivity : Activity(), SensorEventListener {
    var myAccelerometer : Sensor ?= null
    var myGyroScope : Sensor ?= null
    var mySensorManager : SensorManager ?= null
    var file : File ?= null
    var startTime : Long ?= null
    private lateinit var binding : ActivityMainBinding
    var SensorData = ArrayList<String>()
    var uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
    @Suppress("DEPRECATION")
    var bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        mySensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        myGyroScope = mySensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        myAccelerometer = mySensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    override fun onResume() {
        super.onResume()
        val myHandler: Handler = Handler(Looper.getMainLooper())
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val device = bluetoothAdapter.bondedDevices.firstOrNull()
        /*file = File.createTempFile("SensorData", ".txt", null)*/
        /*file!!.setWritable(true)*/
        /*val time = Date()
        startTime = time.time.seconds.inWholeSeconds*/
        val socket = device!!.createInsecureRfcommSocketToServiceRecord(uuid)
        val stream = MyBluetoothService(myHandler)
        mySensorManager!!.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_UI)
        mySensorManager!!.registerListener(this, myGyroScope, SensorManager.SENSOR_DELAY_UI)
        val byteArrayPackage = ByteArray(1024)
        if (SensorData.size == 60){
            for (i in 1..60){
                byteArrayPackage[i] = SensorData[i].toByte()
            }
            stream.ConnectedThread(socket).write(byteArrayPackage)
        }

    }


    override fun onPause() {
        super.onPause()
        mySensorManager!!.unregisterListener(this)

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
    private val TAG = "MY_APP_DEBUG_TAG"

    class MyBluetoothService(private val handler: Handler) {

        inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
            var MESSAGE_READ: Int = 0
            var MESSAGE_WRITE: Int = 1
            var MESSAGE_TOAST: Int = 2
            private val mmInStream: InputStream = mmSocket.inputStream
            private val mmOutStream: OutputStream = mmSocket.outputStream
            private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

            override fun run() {
                var numBytes: Int // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    // Read from the InputStream.
                    numBytes = try {
                        mmInStream.read(mmBuffer)
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }

                    // Send the obtained bytes to the UI activity.
                    val readMsg = handler.obtainMessage(
                        MESSAGE_READ, numBytes, -1,
                        mmBuffer)
                    readMsg.sendToTarget()
                }
            }

            // Call this from the main activity to send data to the remote device.
            fun write(bytes: ByteArray) {
                try {
                    mmOutStream.write(bytes)
                } catch (e: IOException) {
                    Log.e(TAG, "Error occurred when sending data", e)

                    // Send a failure message back to the activity.
                    val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                    val bundle = Bundle().apply {
                        putString("toast", "Couldn't send data to the other device")
                    }
                    writeErrorMsg.data = bundle
                    handler.sendMessage(writeErrorMsg)
                    return
                }

                // Share the sent message with the UI activity.
                val writtenMsg = handler.obtainMessage(
                    MESSAGE_WRITE, -1, -1, mmBuffer)
                writtenMsg.sendToTarget()
            }

            // Call this method from the main activity to shut down the connection.
            fun cancel() {
                try {
                    mmSocket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the connect socket", e)
                }
            }
        }
    }
}