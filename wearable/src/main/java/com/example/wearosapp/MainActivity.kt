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
import java.sql.Timestamp
import java.util.*
import kotlin.time.Duration.Companion.seconds


class MainActivity : Activity(), SensorEventListener {
    var myAccelerometer : Sensor ?= null
    var myGyroScope : Sensor ?= null
    var mySensorManager : SensorManager ?= null
    var file : File ?= null
    var startTime : Long ?= null
    private lateinit var binding : ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        mySensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        myGyroScope = mySensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        myAccelerometer = mySensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        file = File.createTempFile("SensorData", ".txt", null)
        file.run { this!!.setWritable(true) }
    }

    override fun onResume() {
        super.onResume()
        val time = Date()
        startTime = time.time.seconds.inWholeSeconds
        mySensorManager!!.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_UI)
        mySensorManager!!.registerListener(this, myGyroScope, SensorManager.SENSOR_DELAY_UI)
    }

    private fun WriteToFile(accelx : Float, accely: Float, accelz:Float, file : File){

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
        if ((currentTime.time.seconds.inWholeSeconds.toInt() % 60) == 0) {
            if ((p0 != null) && (p0.sensor.type == Sensor.TYPE_ACCELEROMETER)) {
                accelx.text = p0.values[0].toString()
                accely.text = p0.values[1].toString()
                accelz.text = p0.values[2].toString()

            }else if((p0 != null) && (p0.sensor.type == Sensor.TYPE_GYROSCOPE)){
                gyrox.text = p0.values[0].toString()
                gyroy.text = p0.values[1].toString()
                gyroz.text = p0.values[2].toString()

            }

        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
}