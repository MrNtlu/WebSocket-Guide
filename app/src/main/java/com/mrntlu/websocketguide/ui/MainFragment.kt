package com.mrntlu.websocketguide.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.mrntlu.websocketguide.Constants
import com.mrntlu.websocketguide.R
import com.mrntlu.websocketguide.service.WebSocketListener
import com.mrntlu.websocketguide.viewmodels.MainViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var webSocketListener: WebSocketListener
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        webSocketListener = WebSocketListener(viewModel)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageET = view.findViewById<EditText>(R.id.messageET)
        val sendMessageButton  = view.findViewById<ImageButton>(R.id.sendButton)
        val connectButton = view.findViewById<Button>(R.id.connectButton)
        val disconnectButton = view.findViewById<Button>(R.id.disconnectButton)
        val statusTV = view.findViewById<TextView>(R.id.statusTV)
        val messageTV = view.findViewById<TextView>(R.id.messageTV)

        viewModel.socketStatus.observe(viewLifecycleOwner) {
            statusTV.text = if (it) "Connected" else "Disconnected"
        }

        var text = ""
        viewModel.messages.observe(viewLifecycleOwner) {
            text += "${if (it.first) "You: " else "Other: "} ${it.second}\n"

            messageTV.text = text
        }

        connectButton.setOnClickListener {
            webSocket = okHttpClient.newWebSocket(createRequest(), webSocketListener)
        }

        disconnectButton.setOnClickListener {
            webSocket?.close(1000, "Canceled manually.")
        }

        sendMessageButton.setOnClickListener {
            webSocket?.send(messageET.text.toString())
            viewModel.addMessage(Pair(true, messageET.text.toString()))
        }
    }

    private fun createRequest(): Request {
        val websocketURL = "wss://${Constants.CLUSTER_ID}.piesocket.com/v3/1?api_key=${Constants.API_KEY}"

        return Request.Builder()
            .url(websocketURL)
            .build()
    }
}