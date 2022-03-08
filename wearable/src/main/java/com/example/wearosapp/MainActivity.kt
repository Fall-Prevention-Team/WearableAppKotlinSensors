package com.example.wearosapp

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import com.example.wearosapp.databinding.ActivityMainBinding


class MainActivity : Activity(), SensorEventListener {
    var myAccelerometer : Sensor ?= null
    var myGyroScope : Sensor ?= null
    var mySensorManager : SensorManager ?= null
    var mainHandler : Handler ?= null
    var messageInCounter : Int ?= null
    var messageOutCounter : Int ?= null
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        mySensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        myGyroScope = mySensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        myAccelerometer = mySensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        /*mainHandler = Handler(Looper.getMainLooper()).handleMessage()

         */
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
        val accelx = findViewById<TextView>(R.id.accel_val_x)
        val accely = findViewById<TextView>(R.id.accel_val_y)
        val accelz = findViewById<TextView>(R.id.accel_val_z)
        val gyrox = findViewById<TextView>(R.id.gyro_val_x)
        val gyroy = findViewById<TextView>(R.id.gyro_val_y)
        val gyroz = findViewById<TextView>(R.id.gyro_val_z)
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

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
}
/*
private class IncomingHandler(private val context: Context, looper: Looper) : Handler(looper) {
    override fun handleMessage(@NonNull msg: Message) {
        super.handleMessage(msg)
        val bundle = msg.data
        val displayMessage = bundle.getString("MSG_KEY")
        post { Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show() }
        post {  }
    }
}

 */