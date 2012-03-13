/*
 * Transaction.java
 *
 * Created on 6 avril 2007, 16:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import java.util.TimerTask;

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;


/**
 * Generic message manipulated by XML Loader's core.<br/>
 * Should be inherited by protocol-specific messages.
 * @author fhenry
 */
public class RetransmitTransTask extends TimerTask
{
    
    /** the transaction to retransmit */
    private Trans trans;
    
    /** Stack who is managing the transaction */
    private Stack stack;

    /** the different runner to log */
    private ScenarioRunner scRunner; 

    /** Creates a new instance of Transaction */
    public RetransmitTransTask(Stack stack, Trans trans, ScenarioRunner scRunner)
    {     
    	this.stack = stack;
    	this.trans = trans;       
    	this.scRunner = scRunner;    	
    }
       
    public void run()
    {
		Msg msg = trans.getBeginMsg();
    	try 
    	{
    		if (trans.shallRetransmit()) 
    		{
    			int retransNumber = msg.getRetransNumber() + 1;
    			msg.setRetransNumber(retransNumber);
    			    			
        		if (stack.sendMessage(msg))
        		{        		
	                // logs in scenario and main logs as CALLFLOW topic
	            	stack.processLogsMsgSending(msg, scRunner, Stack.SEND);                	
		    		
	        		// log a info message and update statistic counter
	    			float retransmitTime = ((float) (System.currentTimeMillis()- msg.getTimestamp())) /1000;
	        		if (msg.isRequest()) 
	        		{
	                	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Send an auto retransmission (index=", msg.getRetransNumber(), ",time=", retransmitTime , "s) for the request : ", msg.toShortString());
			            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);			            			            
	        		} 
	        		else 
	        		{
	                	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Send an auto retransmission (index=", msg.getRetransNumber() , ",time=", retransmitTime , "s) for the response : ", msg.toShortString());
			            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_REQUEST, msg.getProtocol(), msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, "_retransmitNumber"), 1);
			            StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_RESPONSE, msg.getProtocol(), msg.getResultComplete() + StackFactory.PREFIX_OUTGOING, msg.getTypeComplete() + StackFactory.PREFIX_INCOMING, "_retransmitNumber"), 1);			            
	        		}
        		}
	            trans.startAutomaticRetransmit();		
    		}
    	} 
    	catch (Exception e) {
    		GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while sending automatic retransmission of the request : ", e);
    	}
    }

}
