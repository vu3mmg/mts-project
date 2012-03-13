/*
 * MsgTcp.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.tcp;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.MessageId;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author gpasquiers
 */
public class MsgTcp extends Msg
{
    private byte[] data;

    private String type;
    
    /**
     * Creates a new instance of MsgTcp from a byte array
     */
    public MsgTcp(byte[] someData) throws Exception
    {
        this.data = someData;
        this.type = "SEQ-ACK"; 
    }
        
    /** Get a parameter from the message */
    public Parameter getParameter(String path) throws Exception
    {
        Parameter var = super.getParameter(path);
        if (null != var)
        {
            return var;
        }

    	var = new Parameter();
        String[] params = Utils.splitPath(path);
        
        if(params[0].equalsIgnoreCase("data")) 
        {
            if(params[1].equalsIgnoreCase("text")) 
            {
                var.add(new String(getBytesData()));
            }
            else if(params[1].equalsIgnoreCase("binary")) 
            {
            	var.add(Array.toHexString(new DefaultArray(getBytesData())));
            }
            else 
            {
            	Parameter.throwBadPathKeywordException(path);
            }
        }
        else 
        {
        	Parameter.throwBadPathKeywordException(path);
        }

        return var;
    }    
    
    // <editor-fold desc=" generic methods ">

    public TransactionId getTransactionId() throws Exception
    {
        return null;
    }
    
    public MessageId getMessageId() throws Exception
    {
        return null;
    }

    /** Get the protocol of this message */
    @Override
    public String getProtocol()
    {
        return StackFactory.PROTOCOL_TCP;
    }
    
    /** Return true if the message is a request else return false*/
    @Override
    public boolean isRequest()
    {
        return true;
    }
    
    /** Get the type of this message */
    @Override
    public String getType()
    {
        return type;
    }
    /** Set the type of this message */    
    public void setType(String type)
    {
        this.type = type;
    }    
    
    /** Get the result of this message */
    @Override
    public String getResult()
    {
        return null;
    }

    /** Return the transport of the message*/
    @Override
    public String getTransport() {
    	return StackFactory.PROTOCOL_TCP;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData()
    {
        return data;
    }
    
    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += Utils.toStringBinary(data, Math.min(data.length, 100));
        return ret;
    }

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	return Utils.byteTabToString(data);
    }
    
}
