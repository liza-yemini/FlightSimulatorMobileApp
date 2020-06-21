package com.barilan.flightmobileapp.control.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.barilan.flightmobileapp.R
import com.barilan.flightmobileapp.control.connection.RetrofitBuilder
import com.barilan.flightmobileapp.control.connection.WebService
import com.barilan.flightmobileapp.control.data.Slider
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlinx.android.synthetic.main.activity_connection.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class ControlActivity : AppCompatActivity() {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var api: WebService? = null
    private var showImg: Boolean = true
    val TAG = "StateChange"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        api = RetrofitBuilder.getInstance()
        if (api == null) {
            askForBackToLogin(
                "There was a problem with the server," +
                        "do you want to get back to the login page?"
            )
        }
        //showImg()
        val connectionViewModel: ConnectionViewModel = ConnectionViewModel(this)
        rudderSlider.setOnSeekBarChangeListener(Slider("rudder", rudderView, connectionViewModel))
        throttleSlider.setOnSeekBarChangeListener(
            Slider(
                "throttle",
                throttleView,
                connectionViewModel
            )
        )
        joystick.setOnMoveListener (JoystickView.OnMoveListener  {angle, strength ->
            var newStrength = strength*0.01
            var newAngle = Math.toRadians(angle.toDouble())
            var aileron = newStrength*Math.cos(newAngle)
            var elevator = newStrength*Math.sin(newAngle)
            connectionViewModel.setCommand(aileron.toFloat(),elevator.toFloat())
        },500)
    }
    private fun loopImg() {
        if (showImg) {
            CoroutineScope(IO).launch {
                delay(5000)
                showImg()
                loopImg()
            }
        }
    }

    private fun showImg() {
        uiScope.launch(Dispatchers.IO){
            var result = api?.getImg()
            try{
                withContext(Dispatchers.Main) {
                    var input = result?.await()?.byteStream()
                    val bit = BitmapFactory.decodeStream(input)
                    withContext(Dispatchers.Main) {
                        imageSimulator.setImageBitmap(bit)
                    }
                }
            }catch(ex:Exception){
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ControlActivity, ex.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        }
        /*api?.getImg()?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                val input = response.body()?.byteStream()
                val bit = BitmapFactory.decodeStream(input)
                runOnUiThread {
                    imageSimulator.setImageBitmap(bit)
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ControlActivity,t.toString(),Toast.LENGTH_SHORT).show()
                Log.i("@AKTDEV", t.toString())
            }
        })*/

    }

    private fun askForBackToLogin(msg: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ALERT")
        builder.setMessage(msg)
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            // return to the login page
            finish()
        }
        builder.setNegativeButton(android.R.string.no) { _, _ ->
            // do nothing
        }
        builder.show()
    }


    override fun onStart() {
        super.onStart()
        showImg = true
        loopImg()
    }

    override fun onPause() {
        super.onPause()
        showImg = false
        Log.i(TAG, "onPause")
    }
}
