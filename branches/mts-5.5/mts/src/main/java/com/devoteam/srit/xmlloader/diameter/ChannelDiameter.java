/*
 * ChannelDiameter.java
 *
 * Created on 26 juin 2007, 10:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.diameter;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import dk.i1.diameter.node.ConnectionKey;
import dk.i1.diameter.node.Peer;

/**
 *
 * @author gpasquiers
 */
public class ChannelDiameter extends Channel
{
	
	private Peer peer = null;

	private ConnectionKey connKey;
	
	private String transport = null;
	
    public ChannelDiameter(String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, String aTransport) throws Exception
    {
        super(null, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        this.transport = aTransport.toUpperCase();
    }

    public ChannelDiameter(String anUrl, String aProtocol, String aTransport) throws Exception
    {
        super(null, anUrl, aProtocol);
        this.transport = aTransport.toUpperCase();
    }

    public ChannelDiameter(Peer peer, ConnectionKey connKey) throws Exception
    {
        super(null, peer.uri().toString(), (peer.transportProtocol().equals(Peer.TransportProtocol.sctp)) ? StackFactory.PROTOCOL_SCTP : StackFactory.PROTOCOL_TCP);
        this.peer = peer;
        this.connKey = connKey; 
        this.transport = peer.transportProtocol().name().toUpperCase();
    }

    /** Open a Channel*/
    public boolean open() {
        return true;
    }

    /** Close a Channel */
    public boolean close() {
        return true;
    }
    
    /** Send a Msg to Channel */
    public synchronized boolean sendMessage(Msg msg) throws ExecutionException
    {
        return true;        
    }

    /** Get the transport protocol of a Channel */
    public String getTransport() 
    {
    	return this.transport;
    }
    
    /** Get the diameter peer of a Channel */
    public Peer getPeer() 
    {
    	return this.peer;
    }

    /** Get the diameter connection key of a Channel */
    public ConnectionKey getConnectionKey() 
    {
    	return this.connKey;
    }
    
}
