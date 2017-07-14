package com.markovskisolutions.client

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import org.slf4j.LoggerFactory


/*

  @author Petya
  Created on Jul 13, 2017
*/

class ChatClient {
    val SERVER_HOSTNAME: String = "localhost"
    var KEEP_ALIVE_MESSAGE: String = "!keep-alive"
    val SERVER_PORT: Int = 20021

   open fun main(args: Array<String>) {
        try {
            var socket: Socket = Socket(SERVER_HOSTNAME, SERVER_PORT)
            var mSocketReader: BufferedReader = BufferedReader(InputStreamReader(socket.inputStream))
            var mSocketWriter: PrintWriter = PrintWriter(OutputStreamWriter(socket.outputStream))
            println("Connected to the server : >> ${SERVER_HOSTNAME} : ${SERVER_PORT} >> please add message")
            // Start socket --> console transmitter thread
            var consoleWriter: PrintWriter = PrintWriter(System.out)
            var socketToConsoleTransmitter: TextDataTransmitter = TextDataTransmitter(mSocketReader, consoleWriter)
            socketToConsoleTransmitter.setDaemon(false)
            socketToConsoleTransmitter.start()

            // Start console --> socket transmitter thread
            var consoleReader: BufferedReader = BufferedReader(InputStreamReader(System.`in`))
            var consoleToSocketTransmitter: TextDataTransmitter = TextDataTransmitter(consoleReader, mSocketWriter)
            consoleToSocketTransmitter.setDaemon(false)
            consoleToSocketTransmitter.start()
        } catch(e: IOException) {
            println("Can not connect to ${SERVER_HOSTNAME} : ${SERVER_PORT} ")
            e.printStackTrace();
            System.exit(-1)
        }


    }

}