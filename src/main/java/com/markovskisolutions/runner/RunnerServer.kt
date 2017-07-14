package com.markovskisolutions.runner

import com.markovskisolutions.server.ChatServer

class RunnerServer {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            var server:ChatServer=ChatServer()
            server.main(args)
        }
    }
}