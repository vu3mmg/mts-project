/*
 * GtppAttribute.java
 * 
 */

package com.devoteam.srit.xmlloader.gtpp.data;

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import gp.utils.arrays.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 *
 * @author Benjamin Bouvier
 */
public class GtppAttribute extends Attribute
{    
    public GtppAttribute()
    {}

    @Override
    public void setValue(Object value) throws Exception {
        this.value = value;
        value_q = true;
    }

    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();

        if(getValueQuality())
        {
            if(getFormat().equals("int"))
            {
                if(getLength() == 1)
                    array.addLast(new Integer08Array((Integer)getValue()));
                else if(getLength() == 2)
                    array.addLast(new Integer16Array((Integer)getValue()));
            }
            else if(getFormat().equals("list"))
            {
                if(((LinkedList)getValue()).size() > 0)//search in attribute list
                {
                    for(int i = 0; i < ((LinkedList)getValue()).size(); i++)
                    {
                        Array ar = ((GtppAttribute)((LinkedList)getValue()).get(i)).getArray();
                        if(ar != null)
                            array.addLast(ar);
                    }
                }
            }
            else//same for ip format
                array.addLast(new DefaultArray((byte[])getValue()));
        }
        
        return array;
    }

    @Override
    public GtppAttribute clone()
    {
        GtppAttribute clone = new GtppAttribute();

        clone.setName(getName());
        clone.setFormat(getFormat());
        clone.setLength(getLength());
        clone.setMandatory(isMandatory());
        clone.setOccurenceAttribute(getOccurenceAttribute());
        clone.setNotApplicable(getNotApplicable());
        
        if(getValueQuality())
        {
            try {
                if((getValue() != null) && (getValue() instanceof LinkedList))
                {
                    LinkedList<GtppAttribute> list = (LinkedList)getValue();
                    LinkedList<GtppAttribute> cloneList = new LinkedList();
                    for(int i = 0; i < list.size(); i++)
                        cloneList.add(list.get(i).clone());
                    clone.setValue(cloneList);
                }
            } catch (Exception ex) {
            }
        }

        return clone;
    }

    @Override
    public String toString()
    {
        String str = new String();

        str += "Attribute: " + name;// + ", format " + format + ", length " + length;
        if(isMandatory())
        {
            str += ", mandatory ";
        }

        if(occurenceAttribute != null)
        {
            str += " , occurence attribute " + occurenceAttribute;// + ", occurence " + occurence;
        }

        if(getValueQuality())
        {
            if(getFormat().equals("int"))
                    str += ", value " + (Integer)getValue();
            else if(getFormat().equals("ip"))
            {
                try {
                    str += ", value " + InetAddress.getByAddress((byte[]) getValue()).getHostAddress();
                } catch (UnknownHostException ex) {
                }
            }
            else if(getFormat().equals("list"))
            {
                str += ", value\r\n";
                for(int i = 0; i < ((LinkedList<GtppAttribute>)getValue()).size(); i++)
                    str += ((LinkedList<GtppAttribute>)getValue()).get(i).toString();
            }
            else
                str += ", value " + new String((byte[])getValue());
        }
        if((str.charAt(str.length()-1) != '\n') && (str.charAt(str.length()-2) != '\r'))
            str += "\r\n";
        
        return str;
    }
    
}
