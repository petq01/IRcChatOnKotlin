package com.markovskisolutions.client

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import org.slf4j.LoggerFactory

/*

  @author Petya
  Created on Jul 13, 2017
*/

class TextDataTransmitter : Thread {
    val logger = LoggerFactory.getLogger("TextDataTransmitter")
    val inputStream: InputStream = System.`in`
    var mReader: BufferedReader = BufferedReader(InputStreamReader(inputStream))
    var output: OutputStream = System.out
    var mWriter: PrintWriter = PrintWriter(OutputStreamWriter(output))

    constructor(mReader: BufferedReader, mWriter: PrintWriter)

    override fun run(): Unit {
        try {
            while (!isInterrupted()) {
                var data: String = mReader.readLine()
                if (!data.equals(ChatClient().KEEP_ALIVE_MESSAGE)) {
                    mWriter.println(data)
                    mWriter.flush()
                }
            }
        } catch(e: IOException) {
            logger.error("Lost connection to server")
            System.exit(-1)
        }
    }
}