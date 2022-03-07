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

class MainActivity : Activity(), SensorEventListener {
    var myAccelerometer : Sensor ?= null
    var myGyroScope : Sensor ?= null
    var mySensorManager : SensorManager ?= null
    private lateinit var binding: ActivityMainBinding


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
        mySensorManager!!.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mySensorManager!!.registerListener(this, myGyroScope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mySensorManager!!.unregisterListener(this)
    }
    override fun onSensorChanged(p0: SensorEvent?) {
        val accel_x = findViewById<TextView>(R.id.accel_val_x)
        val accel_y = findViewById<TextView>(R.id.accel_val_y)
        val accel_z = findViewById<TextView>(R.id.accel_val_z)
        val gyro_x = findViewById<TextView>(R.id.gyro_val_x)
        val gyro_y = findViewById<TextView>(R.id.gyro_val_y)
        val gyro_z = findViewById<TextView>(R.id.gyro_val_z)
        if (p0 != null) {
            accel_x.text = p0.values[0].toString()
            accel_y.text = p0.values[1].toString()
            accel_z.text = p0.values[2].toString()
            gyro_x.text = p0.values[0].toString()
            gyro_y.text = p0.values[1].toString()
            gyro_z.text = p0.values[2].toString()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
}