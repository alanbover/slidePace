package com.example.alan.slidepace

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ImageButton

import jp.kshoji.blehid.KeyboardPeripheral;
import jp.kshoji.blehid.util.BleUtils;


class MainActivity : AppCompatActivity() {

    var keyboard: KeyboardPeripheral? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configure bluetooth keyboard
        if (!BleUtils.isBluetoothEnabled(this)) {
            println("Bluetooth not enabled")
            BleUtils.enableBluetooth(this);
            return;
        }

        if (!BleUtils.isBleSupported(this) ) {
            println("BLE not supported")
            println(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            println(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            println(Build.VERSION.SDK_INT)

            val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            println(bluetoothManager.adapter)
        } else {
            setupBlePeripheralProvider()
        }

        // Buttons detection
        val rightButton = findViewById<ImageButton>(R.id.right)
        rightButton.setOnClickListener() {
            onRight()
        }
        val leftButton = findViewById<ImageButton>(R.id.left)
        leftButton.setOnClickListener() {
            onLeft()
        }
        val upButton = findViewById<ImageButton>(R.id.up)
        upButton.setOnClickListener() {
            onUp()
        }
        val downButton = findViewById<ImageButton>(R.id.down)
        downButton.setOnClickListener() {
            onDown()
        }

        // Gesture detection
        println("Start gesture detection")
        val mainLayout = findViewById<ConstraintLayout>(R.id.mainLayout)

        mainLayout.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeDown() {
                onUp()
            }
            override fun onSwipeUp() {
                onDown()
            }
            override fun onSwipeLeft() {
                onRight()
            }
            override fun onSwipeRight() {
                onLeft()
            }
        })

        // TODO: onServerConnectionState should somehow inform the "connection" of the app
    }

    fun setupBlePeripheralProvider() {
        keyboard = KeyboardPeripheral(this)
        keyboard?.setDeviceName("SomeDeviceName")
        keyboard?.setManufacturer("SomeManufacturer")
        keyboard?.setSerialNumber("SomeSerialNumber")
        keyboard?.startAdvertising()
    }

    fun onRight() {
        print("right")
        keyboard?.sendKeys("r")
    }

    fun onLeft() {
        print("left")
        keyboard?.sendKeys("l")
    }

    fun onUp() {
        print("up")
        keyboard?.sendKeys("u")
    }

    fun onDown() {
        print("down")
        keyboard?.sendKeys("d")
    }

    override fun onDestroy() {
        super.onDestroy()

        keyboard?.stopAdvertising()
    }
}

open class OnSwipeTouchListener(c: Context) : OnTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(c, GestureListener())
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        val SWIPE_THRESHOLD = 50;
        val SWIPE_VELOCITY_THRESHOLD = 50;

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return true
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown()
                        } else {
                            onSwipeUp()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onSwipeUp() {}

    open fun onSwipeDown() {}
}
