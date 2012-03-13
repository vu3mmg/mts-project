package com.devoteam.srit.xmlloader.udp;


import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.udp.bio.ChannelUdpBIO;
import com.devoteam.srit.xmlloader.udp.nio.ChannelUdpNIO;

public class ChannelUdp extends Channel
{
    private boolean nio = Config.getConfigByName("udp.properties").getBoolean("USE_NIO", false);

    private Channel channel;

    public ChannelUdp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, boolean aConnected) throws Exception
    {
        super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        if(nio) channel = new ChannelUdpNIO(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol, aConnected);
        else channel = new ChannelUdpBIO(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol, aConnected);
    }

    public int hashCode()
    {
        return channel.hashCode();
    }

    public boolean equals(Object obj)
    {
        return channel.equals(obj);
    }

    public String toString()
    {
        return channel.toString();
    }

    public void setRemotePort(int port)
    {
        channel.setRemotePort(port);
    }

    public void setRemoteHost(String host)
    {
        channel.setRemoteHost(host);
    }

    public void setLocalPort(int port)
    {
        channel.setLocalPort(port);
    }

    public void setLocalHost(String host)
    {
        channel.setLocalHost(host);
    }

    public boolean sendMessage(Msg msg) throws Exception
    {
        return channel.sendMessage(msg);
    }

    public boolean receiveMessageNIO(Msg msg) throws Exception
    {
        return channel.receiveMessageNIO(msg);
    }

    public boolean receiveMessage(Msg msg) throws Exception
    {
        return channel.receiveMessage(msg);
    }

    public boolean open() throws Exception
    {
        return channel.open();
    }

    public String getUID()
    {
        return channel.getUID();
    }

    public String getTransport()
    {
        return channel.getTransport();
    }

    public int getRemotePort()
    {
        return channel.getRemotePort();
    }

    public String getRemoteHost()
    {
        return channel.getRemoteHost();
    }

    public String getProtocol()
    {
        return channel.getProtocol();
    }

    public Parameter getParameter(String path) throws Exception
    {
        return channel.getParameter(path);
    }

    public String getName()
    {
        return channel.getName();
    }

    public int getLocalPort()
    {
        return channel.getLocalPort();
    }

    public String getLocalHost()
    {
        return channel.getLocalHost();
    }

    public boolean equals(Channel channel)
    {
        return this.channel.equals(channel);
    }

    public boolean close()
    {
        return channel.close();
    }
}
