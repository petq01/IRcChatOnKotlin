package com.markovskisolutions.client

import com.markovskisolutions.server.ServerDispatcher;
import com.markovskisolutions.server.threads.*;
import java.net.Socket

/*

  @author Petya
  Created on Jul 13, 2017
*/

class Client {
    var mSocket: Socket = Socket()
    var dispatcher: ServerDispatcher = ServerDispatcher()
    var mClientListener: ClientListener = ClientListener(this, dispatcher)
    var mClientSender: ClientSender = ClientSender(this, dispatcher)

    constructor()
}