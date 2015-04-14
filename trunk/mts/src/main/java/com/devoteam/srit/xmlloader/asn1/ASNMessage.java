/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.asn1;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import gp.utils.arrays.Array;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;
import com.devoteam.srit.xmlloader.asn1.dictionary.EmbeddedMap;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;


/**
 *
 * @author fhenry
 */
public abstract class ASNMessage 
{
	
	public static HashMap<String, ASNDictionary> dictionaries = new  HashMap<String, ASNDictionary>();
	
	public static boolean configMAPSmsCommand = false;
	
	protected ASNDictionary dictionary;
	
	// list of embedded objects
	private EmbeddedMap embeddedList;

	public ASNMessage() throws Exception
	{
		this.embeddedList = new EmbeddedMap();
		configMAPSmsCommand = StackFactory.getStack(StackFactory.PROTOCOL_SIGTRAN).getConfig().getBoolean("map.DECODE_SMS_COMMAND", false);
	}
	
	public ASNMessage(String dictionaryFile) throws Exception
	{
		this();
		initDictionary(dictionaryFile);
		
	}
	
    public String getClassName()
    {
    	return this.dictionary.getClassName();
    }

    public abstract Array encode(String rule) throws Exception; 

    public abstract void decode(Array array, String className, String rule) throws Exception;
    
    public abstract boolean isRequest();
    public abstract String getType();
    public abstract String getResult();
    
    public abstract Parameter getParameter(String path);
    
    public void decode(Array array, String rule) throws Exception
    {
    	decode(array, this.dictionary.getClassName(), rule);
    }

    public abstract void parseFromXML(Element root, String className) throws Exception;
    
    public void parseFromXML(Element root) throws Exception
    {
    	parseFromXML(root, this.dictionary.getClassName());
    }
    
    public abstract String toXML();
    
    public Embedded getEmbeddedByInitial(String initial) 
 	{
     	Embedded init = this.embeddedList.getEmbeddedByInitial(initial);
     	if (init ==  null)
     	{
     		init = this.dictionary.getEmbeddedByInitial(initial);
     	}
     	if (init != null && init.getCondition() == null)
     	{
     		return init;
     	}
     	return null;
 	}
    
    public ElementAbstract getElementByLabel(String label)
    {
    	return this.dictionary.getElementByLabel(label);
    }
    
    public List<Embedded> getEmbeddedByCondition(String condition) 
	{
    	return this.dictionary.getEmbeddedByCondition(condition);
	}
    
    public void addConditionalEmbedded(List<Embedded> embeddeds) 
 	{
    	for( int i = 0; i < embeddeds.size(); i++)
    	{
    		Embedded embedded = embeddeds.get(i);
	    	this.embeddedList.addEmbedded(embedded);
	    	embedded.setCondition(null);
    	}
 	}

	// get the element definition (enumeration binary data) from the dictionary
    public ElementAbstract getElementFromDico(String name, Object parentObject, String resultPath, byte[] bytes) 
 	{    
    	// find using the name of the field
		ElementAbstract elementDico = getElementByLabel(name);
		// find using the type of the parent object
		if (elementDico == null && parentObject != null)
		{
    		String simpleClassName = parentObject.getClass().getSimpleName();
    		if (bytes != null && "Sm_RP_UI".equals(simpleClassName))
    		{
    			int TP_MTI = bytes[0] & (byte) 3;
    			boolean request = isRequest();
    			if (TP_MTI == 0 && request)
    			{
    				simpleClassName = simpleClassName + "_SMS-DELIVER";
    			}
    			else if (TP_MTI == 0 && !request)
    			{
    				simpleClassName = simpleClassName + "_SMS-DELIVER-REPORT";
    			}
    			

    			else if (TP_MTI == 1 && request)
    			{
    				simpleClassName = simpleClassName + "_SMS-SUBMIT";
    			}
    			else if (TP_MTI == 1 && !request)
    			{
    				simpleClassName = simpleClassName + "_SMS-SUBMIT-REPORT";
    			}

    			else if (TP_MTI == 2 & !configMAPSmsCommand)
    			{
    				simpleClassName = simpleClassName + "_SMS-STATUS-REPORT";
    			}
    			else if (TP_MTI == 2 && configMAPSmsCommand)
    			{
    				simpleClassName = simpleClassName + "_SMS-COMMAND";
    			}
    		}
	    	elementDico = getElementByLabel(simpleClassName);
		}
		// find using the end of the returnPath value (before the last dot or the before-last dot)
    	if (elementDico == null)
    	{
	    	String pathName = resultPath;
	    	int pos = resultPath.lastIndexOf('.');
	    	pathName = resultPath.substring(pos + 1);
	    	elementDico = getElementByLabel(pathName);
	    	if (elementDico == null)
	    	{
		    	if (pos >= 0)
		    	{
		    		pos = resultPath.lastIndexOf('.', pos - 1);
		    		if (pos >= 0)
		    		{
		    			pathName = resultPath.substring(pos + 1);
		    		}
		    	}
		    	elementDico = getElementByLabel(pathName);
	    	}
		}
    	return elementDico;
 	}
    
	// get the element definition (enumeration binary data) from the dictionary
    public ElementAbstract getElementFromDico(Object parentObject, String resultPath) 
 	{
    	return getElementFromDico(null, parentObject, resultPath, null);
 	}

    // get the embedded definition form the message (for conditional) and the dictionary
    public Embedded getEmbeddedFromDico(String name, String type) 
 	{
    	Embedded embedded = getEmbeddedByInitial(type);
    	if (embedded == null)
    	{
    		embedded = getEmbeddedByInitial(name);
    	}
    	return embedded;
 	}
	
	public void initDictionary(String dictionaryFile) throws Exception 
	{
		this.dictionary = dictionaries.get(dictionaryFile);
		if (this.dictionary == null)
		{
	        this.dictionary = new ASNDictionary(dictionaryFile);
	        dictionaries.put(dictionaryFile, dictionary);
		}
	}

}