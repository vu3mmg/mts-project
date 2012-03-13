/*
 * ConnRtp.java
 *
 * Created on 26 juin 2007, 10:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.rtp.jmf;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import java.util.Iterator;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.sun.media.rtp.util.RTPPacket;

/**
 *
 * @author fhenry
 */
public class ChannelRtp extends Channel
{  
    // RTP session manager
    private RtpManager rtpManager = null;
        
    /** Creates a new instance of Channel */
    public ChannelRtp(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol) throws Exception { 
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol);  
    }

    /** Creates a new instance of Channel */
    public ChannelRtp(String localHost, int localPort, String remoteHost, int remotePort, String aProtocol)
    {
        super(localHost, localPort, remoteHost, remotePort, aProtocol);        
    }
    
    public boolean open() throws Exception {
        rtpManager = new RtpManager(this);        
        rtpManager.open(getLocalHost(), getLocalPort(), getRemoteHost(), getRemotePort());
        return true;
    }

    /** Close a channel to each Stack */
    public boolean close() {
        try {
            rtpManager.close();                    
        } catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing the RtpManager");
        }
        return true;
    }
    
    
    /** Send a Msg to RTP Stack */
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {       
        MsgRtp msgRtp = (MsgRtp) msg; 
        Iterator<RTPPacket> iter = msgRtp.getRtpPackets(); 
        while (iter.hasNext()) {
            RTPPacket rtpPacket = iter.next();
            rtpManager.sendPacket(msgRtp.isControl(), rtpPacket);
        }                          
        return true;
    }
 
    /** Get the transport protocol of this message */
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_UDP;
    }    
    
}
