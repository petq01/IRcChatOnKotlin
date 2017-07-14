package com.markovskisolutions.runner
import com.markovskisolutions.client.ChatClient;
/**
 * Created by User on 14-Jul-17.
 */
class RunnerClient {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            var client: ChatClient = ChatClient()
            client.main(args)
        }
    }
}