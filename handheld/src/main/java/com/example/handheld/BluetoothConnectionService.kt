package com.example.handheld

import android.Manifest

import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

private const val TAG = "BluetoothConnectionServer"
private const val APP_NAME = "HandheldApp"


private class BluetoothConnectionService(pContext: Context) {
    private var APP_UUID_INSECURE = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
    private var appContext : Context = pContext
    private var appBluetoothManager : BluetoothManager = pContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var appBluetoothAdapter : BluetoothAdapter = appBluetoothManager.adapter
    private var insecAcceptThread : AcceptThread ?= null
    private var connectedThread : ConnectedThread ?= null

    private var connectThread : ConnectThread  ?= null

    private var bluetoothDevice : BluetoothDevice ?= null

    private var DEVICE_UUID : UUID ?= null
    //Waits for connection
    private inner class AcceptThread : Thread(){
        private var bluetoothServerSocket : BluetoothServerSocket ?= null
        init {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
                throw Exception("AcceptThread: Connect permission denied")
            }else {
                try {
                    Log.d(TAG, "AcceptThread: Trying to create bluetooth server socket...")
                    bluetoothServerSocket =
                        appBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            APP_NAME,
                            APP_UUID_INSECURE
                        )
                    Log.d(TAG, "AcceptThread: Bluetooth server socket successfully created.")
                }catch(exception : Exception){
                    Log.e(TAG, "AcceptThread: Failed to create bluetooth server socket using UUID: "+APP_UUID_INSECURE+", " + exception.message)
                }
            }
        }
        //Initiates thread
        override fun run() {
           Log.d(TAG, "run: AcceptThread Running.")
            var socket : BluetoothSocket ?= null
            try {
                Log.d(TAG, "run: RFCOMM server socket start...")
                socket = bluetoothServerSocket!!.accept()
                Log.d(TAG, "run: RFCOMM server socket accepted connection.")
            }catch (exception : Exception){
                Log.e(TAG, "AcceptThread: " + exception.message, exception)
            }
            if (socket != null){
                connected(socket, bluetoothDevice)
            }
            Log.i(TAG, "run: End AcceptThread")
        }
        //Closes the thread
        private fun cancel(){
            Log.d(TAG, "cancel: Closing AcceptThread")
            try {
                bluetoothServerSocket!!.close()
                Log.d(TAG,"cance: Bluetooth server socket successfully closed")
            }catch (exception : Exception){
                Log.e(TAG, "cancel: Closing of AcceptThread ServerSocket failed. " + exception.message)
            }
        }
    }
    //Waits for incoming connect threads, synchronously with the  AcceptThread
    private inner class ConnectThread(pDevice: BluetoothDevice, pUuid: UUID) : Thread(){
        private var bluetoothSocket : BluetoothSocket ?= null
        init {
            Log.d(TAG, "ConnectThread: initialized.")
            bluetoothDevice = pDevice
            DEVICE_UUID = pUuid
        }
        //Check permission to connect
        //
        override fun run() {
            var tmp : BluetoothSocket ?= null
            Log.i(TAG, "run: ConnectThread start...")
            //Permission check
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                throw Exception("run: Connect permission denied")
            }else  {
                //Create Socket
                try {
                    Log.d(TAG, "run: Trying to create InsecureRfcommSocket using UUID: " + APP_UUID_INSECURE)
                    tmp = bluetoothDevice?.createRfcommSocketToServiceRecord(DEVICE_UUID)
                    Log.d(TAG,"run: InsecureRfcommSocket successfully ctreated using UUID: " + APP_UUID_INSECURE)
                }catch (exception : Exception){
                    Log.e(TAG, "run: Failed to create InsecureRfcommSocket using UUID: " + APP_UUID_INSECURE +", " + exception.message)
                }
                bluetoothSocket = tmp
                //Discovery slows application and is unnecessary for now
                appBluetoothAdapter.cancelDiscovery()

                //Try connecting to socket
                try {
                    Log.d(TAG, "run: Trying to connect bluetooth socket...")
                    bluetoothSocket!!.connect()
                    Log.d(TAG, "run: Connection successful.")
                }catch (exception : Exception){
                    Log.e(TAG, "run: Failed to connect bluetooth socket "+exception.message)
                    try {
                        bluetoothSocket!!.close()
                        Log.d(TAG,"run: Bluetooth socket successfully closed")
                    }catch (exception : Exception){
                        Log.e(TAG, "run: Closing of ConnectThread Socket failed " + exception.message)
                    }
                }
                connected(bluetoothSocket!!, bluetoothDevice)
            }
        }
        fun cancel(){
            Log.d(TAG, "cancel: Closing client thread")
            try {
                bluetoothSocket!!.close()
                Log.d(TAG,"cancel: Client socket successfully closed")
            }catch (exception : Exception){
                Log.e(TAG, "cancel: Closing of Client socket failed. " + exception.message)
            }
        }
    }
    private fun startService() {
        Log.d(TAG, "start: ")
        if (connectThread == null){
            connectThread?.cancel()
            connectThread = null
        }
        if (insecAcceptThread == null){
            insecAcceptThread = AcceptThread()
            insecAcceptThread!!.start()
        }
    }
    private fun startClient(pBluetoothDevice: BluetoothDevice, pUuid: UUID){
        Log.d(TAG, "startClient: Started.")
        connectThread = ConnectThread(pBluetoothDevice, pUuid)
        connectThread?.start()
    }
    private inner class ConnectedThread(pBluetoothSocket: BluetoothSocket) : Thread() {
        private var bluetoothSocket : BluetoothSocket ?= null
        private var inputStream : InputStream ?= null
        private var outputStream : OutputStream ?= null

        init {
            Log.d(TAG,"ConnectedThread: Started.")
            try {
                Log.d(TAG, "ConnectedThread: Trying to get streams...")
                bluetoothSocket = pBluetoothSocket
                inputStream = bluetoothSocket!!.inputStream
                outputStream = bluetoothSocket!!.outputStream
            }catch (exception : Exception){
                Log.e(TAG, "ConnectedThread: Failed to get streams "+exception.message)
                exception.printStackTrace()
            }
        }

        override fun run() {
            val dataBuffer = ByteArray(1024)
            var bytes : Int ?= null
            Log.d(TAG, "run: Trying to read input from stream...")
            while (true){
                try {
                    bytes = inputStream!!.read(dataBuffer)
                    val incomingMsg : String = String(dataBuffer, 0,bytes)
                    Log.d(TAG, "Input stream: "+ incomingMsg)
                }catch (exception : Exception){
                    Log.e(TAG,"run: failed to read input from stream.")
                    exception.printStackTrace()
                    break
                }
            }
        }
        fun write(pBytes : ByteArray){
            var text : String = String(pBytes, Charset.defaultCharset())
            Log.d(TAG, "write: Trying to write to output stream...")
            try {
                outputStream!!.write(pBytes)
            }catch (exception : Exception){
                Log.e(TAG, "write: Failed writing to output stream. " +exception.message)
            }
        }
        private fun cancel(){
            Log.d(TAG, "cancel: Trying to close client bluetooth socket...")
            try {
                bluetoothSocket?.close()
                Log.d(TAG, "cancel: Client socket successfully closed.")
            }catch (exception : Exception){
                Log.e(TAG, "cancel: Failed to close client socket." + exception.message)
                exception.printStackTrace()
            }
        }
    }
    private fun connected(pBluetoothSocket: BluetoothSocket, pBluetoothDevice: BluetoothDevice?) {
        Log.d(TAG, "connected: Starting.")
        connectedThread = ConnectedThread(pBluetoothSocket)
        connectThread?.start()
    }
    private fun write(pOutput: ByteArray){
        var r : ConnectedThread ?= null
        Log.d(TAG, "write: Write called.")
        connectedThread?.write(pOutput)
    }
}
