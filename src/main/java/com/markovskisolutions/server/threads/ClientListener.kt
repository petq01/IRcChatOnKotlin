package com.markovskisolutions.server.threads

import com.markovskisolutions.server.ServerDispatcher;
import com.markovskisolutions.client.Client;
import com.markovskisolutions.server.ChatServer;
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.Socket
import java.net.SocketTimeoutException

/*

  @author Petya
  Created on Jul 13, 2017
*/

class ClientListener : Thread {
    var mServerDispatcher: ServerDispatcher = ServerDispatcher()
    var mClient: Client = Client()
    val inputStream: InputStream = System.`in`
    var mSocketReader: BufferedReader = BufferedReader(InputStreamReader(inputStream))

    constructor(aClient: Client, aSrvDispatcher: ServerDispatcher) {
        mClient = aClient
        mServerDispatcher = aSrvDispatcher
        val socket: Socket = aClient.mSocket
        socket.setSoTimeout(ChatServer().CLIENT_READ_TIMEOUT)
        mSocketReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    }

    /**
     * Until interrupted, reads messages from the client socket, forwards them
     * to the server dispatcher's queue and notifies the server dispatcher.
     */
    override fun run() {
        try {
            while (!isInterrupted()) {
                try {
                    val message: String = mSocketReader.readLine()

                    mServerDispatcher.dispatchMessage(mClient, message);
                } catch(ste: SocketTimeoutException) {
                    mClient.mClientSender.sendKeepAlive();
                }
            }

        } catch(ioex: IOException) {
          println("Lost connection")
            // Problem reading from socket (broken connection)
        }
        // Communication is broken. Interrupt both listener and
        // sender threads
        mClient.mClientSender.interrupt();
        mServerDispatcher.deleteClient(mClient);
    }
}