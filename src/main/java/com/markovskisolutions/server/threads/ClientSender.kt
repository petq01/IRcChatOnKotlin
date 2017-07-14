package com.markovskisolutions.server.threads

import com.markovskisolutions.client.Client;
import com.markovskisolutions.server.ServerDispatcher;
import com.markovskisolutions.server.ChatServer;
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.util.Vector

/*

  @author Petya
  Created on Jul 13, 2017
*/
/**
 * Sends messages to the client. Messages waiting to be sent are stored in a
 * message queue. When the queue is empty, ClientSender falls in sleep until a
 * new message is arrived in the queue. When the queue is not empty,
 * ClientSender sends the messages from the queue to the client socket.
 */
class ClientSender : Thread {
    private val mMessageQueue by lazy { Vector<String>() }
    private var mClient: Client = Client()
    var output: OutputStream = System.out
    private var mOut: PrintWriter = PrintWriter(OutputStreamWriter(output))
    private var mServerDispatcher: ServerDispatcher = ServerDispatcher()
    private val lock = java.lang.Object()


    constructor(aClient: Client, aServerDispatcher: ServerDispatcher) {
        mClient = aClient
        mServerDispatcher = aServerDispatcher
        var socket: Socket = aClient.mSocket
        mOut = PrintWriter(OutputStreamWriter(socket.getOutputStream()))
    }

    /**
     * Adds given message to the message queue and notifies this thread
     * (actually getNextMessageFromQueue method) that a message is arrived.
     * sendMessage is always called by other threads (ServerDispatcher).
     */
    @Synchronized fun sendMessage(aMessage: String) {
        mMessageQueue.add(aMessage)
        lock.notify();
    }

    /**
     * Sends a keep-alive message to the client to check if it is still alive.
     * This method is called when the client is inactive too long to prevent
     * serving dead clients.
     */
    fun sendKeepAlive() {
        sendMessage(ChatServer().KEEP_ALIVE_MESSAGE);
    }

    /**
     * @return and deletes the next message from the message queue. If the queue
     *         is empty, falls in sleep until notified for message arrival by
     *         sendMessage method.
     */
    @Throws(InterruptedException::class)
    @Synchronized
    fun getNextMessageFromQueue(): String {
        while (mMessageQueue.size == 0)
            lock.wait()
        var message: String = mMessageQueue.get(0)
        mMessageQueue.removeElementAt(0);
        return message;
    }

    /**
     * Sends given message to the client's socket.
     */
    fun sendMessageToClient(aMessage: String) {
        mOut.println(aMessage);
        mOut.flush();
    }

    /**
     * Until interrupted, reads messages from the message queue and sends them
     * to the client's socket.
     */
    override fun run() {
        try {
            while (!isInterrupted()) {
                var message: String = getNextMessageFromQueue();
                sendMessageToClient(message);
            }
        } catch(e: Exception) {
        }
        mClient.mClientListener.interrupt()
        mServerDispatcher.deleteClient(mClient)
    }
}