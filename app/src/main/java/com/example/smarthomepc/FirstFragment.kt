package com.example.smarthomepc

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jcraft.jsch.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.slf4j.helpers.Util
import java.lang.Exception
import java.net.*
import java.util.*
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences
    private var enabled by Delegates.observable(false) {
        property, oldValue, newValue ->

        val onBtn = activity?.findViewById<Button>(R.id.on_btn)
        val offBtn = activity?.findViewById<Button>(R.id.off_btn)
        val rebootBtn = activity?.findViewById<Button>(R.id.reboot_btn)
        val stateTextView = activity?.findViewById<TextView>(R.id.state)

        when (newValue) {
            true -> powerOn(onBtn, offBtn, rebootBtn, stateTextView)
            false -> powerOff(onBtn, offBtn, rebootBtn, stateTextView)
        }
    }

    private lateinit var spinner: ProgressBar

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val refreshBtn = view.findViewById<FloatingActionButton>(R.id.refreshButton)
        spinner = view.findViewById(R.id.spinner)
        spinner.visibility = View.GONE

        refreshBtn.setOnClickListener {
            spinner.visibility = View.VISIBLE
            GlobalScope.async {
                enabled = pingPc()
            }
        }

        view.findViewById<Button>(R.id.on_btn).setOnClickListener{
            wakeOnLan()
        }

        view.findViewById<Button>(R.id.off_btn).setOnClickListener{
            showPasswordDialog(getString(R.string.shutdown_command))
        }

        view.findViewById<Button>(R.id.reboot_btn).setOnClickListener{
            showPasswordDialog(getString(R.string.reboot_command))
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        view.findViewById<TextView>(R.id.MacAddress).text = sharedPref.getString("edit_text_mac_address", "")

        refreshBtn.performClick()

    }

    private fun powerOn(onBtn: Button?, offBtn: Button?, rebootBtn: Button?, stateTextView: TextView?) {
        onBtn?.setBackgroundColor(Color.DKGRAY)
        context?.let { ContextCompat.getColor(it, R.color.purple_500) }?.let { offBtn?.setBackgroundColor(it) }
        context?.let { ContextCompat.getColor(it, R.color.purple_500) }?.let { rebootBtn?.setBackgroundColor(it) }

        stateTextView?.text = getString(R.string.enabled)

        onBtn?.isClickable = false
        offBtn?.isClickable= true
        rebootBtn?.isClickable = true
    }

    private fun powerOff(onBtn: Button?, offBtn: Button?, rebootBtn: Button?, stateTextView: TextView?) {
        context?.let { ContextCompat.getColor(it, R.color.purple_500) }?.let { onBtn?.setBackgroundColor(it) }
        offBtn?.setBackgroundColor(Color.DKGRAY)
        rebootBtn?.setBackgroundColor(Color.DKGRAY)

        stateTextView?.text = getString(R.string.disabled)

        onBtn?.isClickable = true
        offBtn?.isClickable = false
        rebootBtn?.isClickable = false
    }


    private fun pingPc(): Boolean {
        return try {
            val inetStr = sharedPref.getString("edit_text_inet", "-1")
            val inet = InetAddress.getByName(inetStr)

            val response = inet.isReachable(1000)

            // creating spinner for a really short period of time
            if (response) {
                spinner.postDelayed({
                    spinner.visibility = View.GONE
                }, 100)
            } else {
                spinner.visibility = View.GONE
            }

            return response
        } catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    private fun bytes(mac: String): ByteArray {
        val hex: List<String> = mac.split(":", ignoreCase = true)
        val macAddress = ByteArray(6)


        if (hex.size!= 6){
            throw IllegalArgumentException("Invalid MAC address.")
        }

        try {
            for (i in 0..5) {
                macAddress[i] = hex[i].toInt(radix = 16).toByte()
            }
        }
        catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid hex digit in MAC address.")
        }

        return macAddress
    }

    private fun sshCon(pass: String, commandToExecute: String) {
        val username = sharedPref.getString("edit_text_username", "")
        val inet = sharedPref.getString("edit_text_inet", "")
        val port = sharedPref.getString("edit_text_port", "22")?.toInt()

        val key = "user.home"
        val dir = context?.applicationInfo?.dataDir

        System.setProperty(key, dir)

        var channel: Channel? = null
        var session : Session? = null

        try {
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session?.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            val jsch = JSch()

            session = port?.let { jsch.getSession(username, inet, it) }
            session?.setPassword(pass)
            session?.setConfig(config)
            session?.connect(1500)

            Log.d("Connection", "Established")

            channel = session?.openChannel("exec")
            val chan = channel as ChannelExec
            channel.setCommand(commandToExecute)

            channel?.inputStream = null
            val out = channel?.outputStream
            chan.setErrStream(System.err)
            val input = channel.inputStream
            chan.setPty(true)
            channel.connect()

            out.write((pass + "\n").toByteArray())
            out.flush()

            while (channel.isConnected()) {
                Thread.sleep(100)
            }

            val tmp = ByteArray(1024)
            while (input.available()>0) {
                val i = input.read(tmp, 0, 1024)
                if(i < 0) break
                Log.d("Output", String(tmp, 0, i))
            }

            if (channel.isClosed()) {
                Log.d("Exit-status:", channel.exitStatus.toString())
                if (channel.exitStatus == 0) {
                    val handler = Handler(Looper.getMainLooper())
                    enabled = true
                    handler.post(Runnable {
                        Toast.makeText(activity, R.string.pc_shutdown_done, Toast.LENGTH_SHORT).show()
                    })
                }
            }

            /*while (true) {
                while (input.available()>0) {
                    val i = input.read(tmp, 0, 1024)
                    if(i < 0) break
                    Log.d("Output", String(tmp, 0, i))
                }
                if (channel.isClosed()) {
                    Log.d("Exit-status:", channel.exitStatus.toString())
                    if (channel.exitStatus != 0) {
                    }
                    break
                }
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {}
                channel.disconnect()
                session.disconnect()

            }*/

        } catch (e: JSchException) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                when (e.message.toString()) {
                    "Auth fail" -> Toast.makeText(activity, R.string.wrong_password, Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(activity, R.string.ssh_error, Toast.LENGTH_SHORT).show()
                }
            }
        } finally {
            channel?.disconnect()
            session?.disconnect()
            activity?.runOnUiThread {
                spinner.visibility = View.GONE
            }
        }
    }

    private fun wakeOnLan() {
        /*
            Magic packet in its payload contains 6 bytes of all 255 (FF in hexadecimal),
            followed by sixteen repetitions of the target computer's 48-bit MAC address,
            for a total of 102 bytes.
         */


        val macAddress = sharedPref?.getString("edit_text_mac_address", "Mac Address")
        val broadcastStr = sharedPref?.getString("edit_text_broadcast", "Broadcast")
        val port = 9

        val broadcast = Inet4Address.getByName(broadcastStr)


        try{
            val macBytes = bytes(macAddress.toString())
            val bytes: ByteArray = ByteArray(6 + 16 * macBytes.size)

            // Put 0xff 6 times at the beginning
            for(i in 0..5) {
                bytes[i] = 0xFF.toByte()
            }

            // Copy bytes of MAC address 16 times
            for(i in 6 until bytes.size step macBytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
            }

            val packet = DatagramPacket(
                    bytes, bytes.size,
                    broadcast, port
            )
            val socket = DatagramSocket()

            socket.send(packet)
            socket.close()
            enabled = true
            Toast.makeText(activity, R.string.wake_on_lan_msg, Toast.LENGTH_SHORT).show()

        }
        catch (e: IllegalArgumentException) {
            e.printStackTrace()
            wrongMacAlert()
        }
    }

    private fun showPasswordDialog(commandToExecute: String) {
        val li = LayoutInflater.from(context)
        val promptsView = li.inflate(R.layout.password_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(context)

        alertDialogBuilder.setView(promptsView)

        val userInput = promptsView.findViewById<EditText>(R.id.user_input)

        var pass: String? = null

        alertDialogBuilder
            .setCancelable(false)
            .setTitle(sharedPref.getString("edit_text_username", resources.getString(R.string.default_user)) + resources.getString(R.string.alert_title))
            .setPositiveButton(
                "OK"
            ) { _, _ ->
                pass = userInput.text.toString()

                spinner.visibility = View.VISIBLE
                Thread {
                    sshCon(pass!!, commandToExecute)
                }.start()
            }
                .setNegativeButton("Cancel"
                ) { dialog, _ ->
                    dialog.cancel()
                }

        // Creating alert dialog
        val alertDialog = alertDialogBuilder.create()

        // show alert
        alertDialog.show()
    }

    private fun wrongMacAlert() {
        val builder = AlertDialog.Builder(activity)
        with(builder) {
            setTitle(getString(R.string.mac_alert_title))
            setMessage(getString(R.string.mac_alert_message))
            setNeutralButton(getString(R.string.close), null)

            show()
        }
    }
}