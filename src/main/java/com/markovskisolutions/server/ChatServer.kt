package com.markovskisolutions.server

import java.io.IOException
import java.net.ServerSocket
import com.markovskisolutions.server.threads.*;

import java.net.Socket
import org.slf4j.LoggerFactory
import com.markovskisolutions.client.Client;

/*

  @author Petya
  Created on Jul 13, 2017
*/
/**
 * Chat Server is multithreaded chat server. It accepts multiple clients
 * simultaneously and serves them. Clients are able to send messages to the
 * server. When some client sends a message to the server, the message is
 * dispatched to all the clients connected to the server.
 *
 * The server consists of two components - "server core" and "client handlers".
 *
 * The "server core" consists of two threads: - ChatServer - accepts client
 * connections, creates client threads to handle them and starts these threads -
 * ServerDispatcher - waits for messages and when some message arrive sends it
 * to all the clients connected to the server
 *
 * The "client handlers" consist of two threads: - ClientListener - listens for
 * message arrivals from the socket and forwards them to the ServerDispatcher
 * thread - ClientSender - sends messages to the client
 *
 * For each accepted client, a ClientListener and ClientSender threads are
 * created and started. A Client object is also created to maintain the
 * information about the client and is added to the ServerDispatcher's clients
 * list. When some client is disconnected, is it removed from the clients list
 * and both its ClientListener and ClientSender threads are interrupted.
 */
class ChatServer {
    val logger = LoggerFactory.getLogger("ChatServer")
    val LISTENING_PORT: Int = 2002
    var KEEP_ALIVE_MESSAGE: String = "!keep-alive"
    var CLIENT_READ_TIMEOUT: Int = 5 * 60 * 1000
    var mServerSocket: ServerSocket = ServerSocket()
    var mServerDispatcher: ServerDispatcher = ServerDispatcher()

    fun main(args: Array<String>) {
        bindServerSocket()
        mServerDispatcher.start()
        handleClientConnections()
    }

    private fun bindServerSocket(): Unit {
        try {
            mServerSocket = ServerSocket(LISTENING_PORT)
            logger.info("ChatServer started on port ${LISTENING_PORT} ")
        } catch(ioex: IOException) {
            logger.error("Can not start listening on port ${LISTENING_PORT} ")
            ioex.printStackTrace()
            System.exit(-1)
        }
    }

    private fun handleClientConnections(): Unit {
        while (true) {
            try {
                var socket: Socket = mServerSocket!!.accept()
                var client: Client = Client()
                client.mSocket = socket
                var clientListener: ClientListener = ClientListener(client, mServerDispatcher)
                var clientSender: ClientSender = ClientSender(client, mServerDispatcher)
                client.mClientListener = clientListener
                clientListener.start()
                client.mClientSender = clientSender
                clientSender.start()
                mServerDispatcher.addClient(client)
            } catch(ioe: IOException) {
                ioe.printStackTrace()
            }
        }
    }
}