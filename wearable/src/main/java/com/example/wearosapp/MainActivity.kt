package com.example.wearosapp

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import com.example.wearosapp.databinding.ActivityMainBinding
import java.io.File
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
    var bytes : ByteArray = ByteArray(1024)

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
        mySensorManager!!.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_UI)
        mySensorManager!!.registerListener(this, myGyroScope, SensorManager.SENSOR_DELAY_UI)
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

}