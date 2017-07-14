package com.markovskisolutions.server

import com.markovskisolutions.client.Client;
import java.net.Socket
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Vector
import org.slf4j.LoggerFactory

/*

  @author Petya
  Created on Jul 13, 2017
*/

class ServerDispatcher : Thread {
    private val logger = LoggerFactory.getLogger("ServerDispatcher")
    private val mMessageQueue by lazy { Vector<String>() }
    private val mClients by lazy { Vector<Client>() }
    private val lock = java.lang.Object()

    constructor()

    /**
     * Adds given client to the server's client list.
     */
    @Synchronized fun addClient(aClient: Client): Unit {
        mClients.add(aClient)
    }

    /**
     * Deletes given client from the server's client list if the client is in
     * the list.
     */
    @Synchronized fun deleteClient(aClient: Client): Unit {
        var clientIndex: Int = mClients.indexOf(aClient)
        if (clientIndex != -1) mClients.removeElementAt(clientIndex)
    }

    /**
     * Adds given message to the dispatcher's message queue and notifies this
     * thread to wake up the message queue reader (getNextMessageFromQueue
     * method). dispatchMessage method is called by other threads
     * (ClientListener) when a message is arrived.
     */
    @Synchronized fun dispatchMessage(aClient: Client, aMessage: String): Unit {
        var dateFormat: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        var cal: Calendar = Calendar.getInstance()
        var socket: Socket = aClient.mSocket
        var senderIP: String = socket.getInetAddress().getHostAddress()
        var senderPort: String = " ${socket.getPort()}"
        var concatenate = "${senderIP} : ${senderPort} : ${dateFormat.format(cal.getTime())} >> ${aMessage}"
        mMessageQueue.add(concatenate)
        lock.notify()

    }

    /**
     * @return and deletes the next message from the message queue. If there is
     *         no messages in the queue, falls in sleep until notified by
     *         dispatchMessage method.
     */
    @Throws(InterruptedException::class)
    @Synchronized private fun getNextMessageFromQueue(): String {
        while (mMessageQueue.isEmpty())
            lock.wait()
        var message: String = mMessageQueue.get(0)
        mMessageQueue.removeElementAt(0)
        return message
    }

    /**
     * Sends given message to all clients in the client list. Actually the
     * message is added to the client sender thread's message queue and this
     * client sender thread is notified to process it.
     */
    private fun sendMessageToAllClients(aMessage: String): Unit {
        for (i in 0 until mClients.size) {
            var client: Client = mClients.get(i)
            client.mClientSender.sendMessage(aMessage)
        }
    }

    /**
     * Infinitely reads messages from the queue and dispatches them to all
     * clients connected to the server.
     */
    override fun run(): Unit {
        try {
            while (true) {
                var message: String = getNextMessageFromQueue()
                sendMessageToAllClients(message)
            }
        } catch(ie: InterruptedException) {
            logger.error("Innterrupted")
        }
    }
}