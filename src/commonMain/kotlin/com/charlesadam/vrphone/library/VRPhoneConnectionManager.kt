package com.charlesadam.vrphone.library

import com.github.aakira.napier.Napier
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

class VRPhoneConnectionManager {
    private val dispatcher = Dispatchers.Default
    private lateinit var server: ServerSocket
    private var notificationList: MutableList<NotificationInfo> = mutableListOf()

    fun sendNotification(notificationInfo: NotificationInfo) {
        Napier.v("Adding Notification to List ${notificationInfo.title}")
        notificationList.add(notificationInfo)
    }

    fun startMessagingServer() {
        CoroutineScope(dispatcher).launch {
            Napier.v("Starting Sever")
            bindServerSocket()
            listenConnections()
        }
    }

    @OptIn(InternalAPI::class)
    private fun bindServerSocket(){
        //TODO: REMOVE OPTIN
        server = aSocket(SelectorManager(dispatcher)).tcp().bind("192.168.0.27", 53558)
    }

    private suspend fun listenConnections(){
        while (true){
            val socket = waitConnection()
            Napier.v("Connection Accepted: ${socket.remoteAddress}")
            val notificationData = executeOnMainThread { retrieveNotificationData() }

            if (notificationData.notificationList.isEmpty()){
                sendEmptyData(socket)
                continue
            }

            sendNotificationData(socket, notificationData)
        }
    }

    private suspend fun waitConnection(): Socket {
        Napier.v("Waiting Connection")
        return server.accept()
    }

    private fun retrieveNotificationData(): NotificationData {
            val notificationData = NotificationData(notificationList.toList())
            notificationList.clear()
            return notificationData
    }

    private suspend fun sendEmptyData(socket: Socket){
        writeData(socket, "EMPTY")
    }

    private suspend fun sendNotificationData(socket: Socket, notificationData: NotificationData){
        val payload = Json.encodeToString(NotificationData.serializer(), notificationData)
        writeData(socket, payload)
    }

    private suspend fun writeData(socket: Socket, data:String){
        val writeChannel = socket.openWriteChannel(true)
        writeChannel.writeStringUtf8(data)
        Napier.v("Writing Data: $data")
    }

    private suspend fun <T>executeOnMainThread(
        f:() -> T,
    ): T{
        return withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {
            f()
        }
    }
}