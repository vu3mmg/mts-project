/*
 * Transaction.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.protocol;

import java.util.Iterator;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 * Generic transaction manipulated by XML Loader's core.
 * @author fhenry
 */
public class Trans extends GroupMsg<Msg>
{
	
	private Sess session = null;
	
    /** Creates a new instance of Transaction */
    public Trans(Stack stack, Msg beginMsg) throws Exception
    {
    	super(stack, beginMsg);    	
    }

    /** Add a end message to the list of the transaction */
    public boolean addEndMessage(Msg msg) throws Exception
    {
    	boolean ret = super.addEndMessage(msg);
    	if (msg.endTransaction())
    	{
    		this.active = false;
    	}
    	return ret;
    }

    /** Get a parameter from the message */
    public Parameter getParameter(String path) throws Exception
    {
        String[] params = Utils.splitPath(path);
    	Parameter parameter = new Parameter();

        if (params.length == 1)
        {
        	parameter.add(this);
        }
        else if (params[1].equalsIgnoreCase("id"))
        {
            parameter.add(getBeginMsg().getTransactionId().toString());
        }
        else if (params[1].equalsIgnoreCase("name"))
        {
            parameter.add(getSummary(beginMsg.isSend()));
        }        
        else if (params[1].equalsIgnoreCase("request"))
        {
            parameter.add(getBeginMsg());
        }
        else if (params[1].equalsIgnoreCase("responses"))
        {
        	Iterator<Msg> iter = endListMsg.values().iterator();
        	while (iter.hasNext())
        	{
        		Msg msg = iter.next();
        		parameter.add(msg);	
        	}        
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
        return parameter;
    }
    
    /** Shall retransmit the transaction */
    public boolean shallRetransmit() throws Exception
    {
        Iterator<Msg> iter = endListMsg.values().iterator();
        while (iter.hasNext())
        {
            Msg msg = (Msg) iter.next();
            if (msg.shallStopRetransmit())
            {
                return false;
            }
        }
        return true;
    }

    /** 
     * Start the automatic retransmission mechanism 
     * for the begin message of the transaction
     */
    public boolean startAutomaticRetransmit() throws Exception
    {
        int retransNumber = beginMsg.getRetransNumber();
        if (beginMsg.shallBeRetransmitted() &&
                (retransNumber < stack.retransmitTimes.length))
        {
            RetransmitTransTask retransmitTask = new RetransmitTransTask(stack, this, scRunner);
            stack.retransmissionTimer.schedule(retransmitTask, (long) (stack.retransmitTimes[retransNumber] * 1000));
        }
        return true;
    }

    /** 
     * Start the automatic retransmission mechanism 
     * for the end messages (only for those who stop the retransmission mechanism) 
     * of the transaction
     */
    public boolean retransmitAutomaticResponses() throws Exception
    {
        Iterator<Msg> iter = endListMsg.values().iterator();
        while (iter.hasNext())
        {
            Msg msg = (Msg) iter.next();
            if (msg.shallStopRetransmit())
            {
                if (stack.sendMessage(msg))
                {
                    int retransNumber = msg.getRetransNumber() + 1;
                    msg.setRetransNumber(retransNumber);

                    // logs in scenario and main logs as CALLFLOW topic
                    stack.processLogsMsgSending(msg, scRunner, Stack.SEND);

                    // log a info message and update statistic counter
                    float retransmitTime = ((float) (System.currentTimeMillis() - msg.getTimestamp())) / 1000;
                    if (msg.isRequest())
                    {
                        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Send an auto retransmission (index=", msg.getRetransNumber(), ",time=", retransmitTime, "s) for the request : ", msg.toShortString());
                        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);			                                    
                    }
                    else
                    {
                        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Send an auto retransmission (index=", msg.getRetransNumber(), ",time=", retransmitTime, "s) for the response : ", msg.toShortString());
                        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);
			            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_retransmitNumber"), 1);			                                    
                    }
                }
            }
        }
        return true;
    }
        
    public String getSummary(boolean send) throws Exception
    {
    	String ret = beginMsg.getSummary(send, true);
    	ret += " / ";
        Iterator<Msg> iter = endListMsg.values().iterator();
        while (iter.hasNext())
        {
            Msg msg = (Msg) iter.next();
            if (iter.hasNext())
            {
           		ret += msg.getSummary(!send, false) + " ";
            }
            else
            {
           		ret += msg.getSummary(!send, true);
            }
        }    
        return ret;
    }

    /** 
     * Interface Removable 
     * */
    public void onRemove() throws Exception
    {
    	String statsKey = getSummary(beginMsg.isSend());
    	if (statsKey == null)
    	{
    		GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "StatPoll : statskey = null for transaction = " + this);
    		return;
    	}
	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, "_beginTrans"), 1);
       	StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, "_activeTrans"), 1);

    	String key = beginMsg.getSummary(beginMsg.isSend(), true);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, key, "_responseNumber"), 1);
	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, key, "_responseTime"), 0);

        Iterator<Msg> iter = endListMsg.values().iterator();
        Msg msg = null;
        while (iter.hasNext())
        {
            msg = (Msg) iter.next();
    	    float respTime = Stack.getTimeDuration(msg, beginMsg.getTimestamp());
	    	key = msg.getSummary(msg.isSend(), true);
            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, key, "_responseNumber"), 1);
    	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, key, "_responseTime"), respTime);
        }
        
        if (!this.active)
        {
    	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, "_endTrans"), 1);
    	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, "_activeTrans"), -1);
    	    float transTime = Stack.getTimeDuration(msg, beginMsg.getTimestamp());
    	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSACTION, beginMsg.getProtocol(), statsKey, "_transTime"), transTime);
            GlobalLogger.instance().getApplicationLogger().info(Topic.PROTOCOL, "Finish a outgoing transaction (time = ", transTime, " s) : ", msg.toShortString());
        }
    }

	public Sess getSession() {
		return session;
	}

	public void setSession(Sess session) {
		this.session = session;
	}

    /** Returns the string description of the message. Used for logging as DEBUG level */
    public String toString()
    {
   		return super.toString("TRANSACTION:Request=", "TRANSACTION:Responses=");
	}

}
