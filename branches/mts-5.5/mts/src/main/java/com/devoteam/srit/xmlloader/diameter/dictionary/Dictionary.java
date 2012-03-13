/*
 * Dictionary.java
 *
 * Created on 26 mars 2007, 14:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.diameter.dictionary;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;


/**
 *
 * @author gpasquiers
 */
public class Dictionary
{
    
    // current instance of dictionary
    static private Dictionary _dictionary ;
    
    // harcoded path of dictionary.xml
    static private String DICTIONARY_PATH="../conf/diameter/dictionary.xml" ;
    
    // hashmaps
    private HashMap<String, Application>  applicationByName ;
    private HashMap<String, Application>  applicationById ;
    
    /**
     * Returns the current instance of Dictionary.
     * Creates one if it does not already exists.
     */
    static public synchronized Dictionary getInstance() throws ParsingException
    {
        // creates an instance if needed
        {
            if(_dictionary==null)
            {
                _dictionary = new Dictionary();
                try
                {
                    _dictionary.parseFromFile(DICTIONARY_PATH);
                }
                catch(Exception e)
                {
                    throw new ParsingException(e);
                }
            }
        }
        
        // return the instance of dictionary
        return _dictionary ;
    }
    
    /**
     * Parse the dictionary from a file.
     */
    private void parseFromFile(String path) throws Exception
    {
        XMLDocument dictionaryDocument = new XMLDocument();
        dictionaryDocument.setXMLSchema(URIFactory.newURI("../conf/schemas/diameter-dictionary.xsd"));
        dictionaryDocument.setXMLFile(URIFactory.newURI(path));
        dictionaryDocument.parse();
        
        Document document = dictionaryDocument.getDocument();
        
        // parse vendors
        
        applicationByName = new HashMap();
        applicationById = new HashMap();
        
        traceDebug("try to parsing application base");

        //
        // base first, important for references from other applications
        //
        parseApplication(document.getRootElement().element("base"));
        
        traceDebug("try to parsing all application");

        //
        // all applications
        //
        List<Element> elements = document.getRootElement().elements("application");
        for(Element element:elements)
        {
            parseApplication(element);
        }
    }
    
    private void parseApplication(Element root) throws ParsingException
    {
        boolean isBase = false ;
        if(root.getName().equals("base"))
        {
            isBase = true ;
        }
        
        int id = -1 ;
        try
        {
            if(null != root.attribute("id"))
            {
                id = Integer.parseInt(root.attributeValue("id"));
            }
            else if(false == isBase)
            {
                traceWarning("No application.id, skipping");
                return ;
            }
        }
        catch(Exception e)
        {
            traceWarning("Invalid application.code, skipping");
            return ;
        }
        
        String name = null ;
        if(null != root.attribute("name"))
        {
            name = root.attributeValue("name");
        }
        else if(false == isBase)
        {
            traceWarning("No application.name, skipping");
            return ;
        }
        
        if(isBase)
        {
            name = "base" ;
            id = -1 ;
        }
        
        traceDebug("parsing application " + name);
        
        
        Application application = new Application(name, id);
        
        if(null != getApplicationByName(name)) traceWarning("Application of name " + name + " already exists, overwriting");
        if(null != getApplicationById(id)) traceWarning("Application of id " + id + " already exists, overwriting");
        
        applicationByName.put(name, application);
        applicationById.put(Integer.toString(id), application);
        
        application.parseApplication(root);
        application.fillGroupedAvpsReferences();
    }
    
    private Application getApplicationById(int code)
    {
        return applicationById.get(Integer.toString(code));
    }
    
    private Application getApplicationByName(String name)
    {
        return applicationByName.get(name);
    }
    
    public Application getApplication(String key)
    {
        if(Utils.isInteger(key))  return getApplicationById(Integer.parseInt(key));
        else                      return getApplicationByName(key);
    }
    
    public VendorDef getVendorDefByName(String name, String applicationId )
    {
        VendorDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getVendorDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("base");
        if(null != application) result = applicationBase.getVendorDefByName(name);
        if(null != application) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getVendorDefByName(name);
            if(null != result)
            {
                traceWarning("got Vendor definition for " + result.get_vendor_id() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public VendorDef getVendorDefByCode(int code, String applicationId )
    {
        Application application ;
        VendorDef result = null ;
        
        // try with specified application
        application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getVendorDefByCode(code);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("base");
        if(null != applicationBase) result = applicationBase.getVendorDefByCode(code);
        if(null != applicationBase) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getVendorDefByCode(code);
            if(null != result)
            {
                traceWarning("got Vendor definition for " + result.get_vendor_id() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public TypeDef getTypeDefByName(String name, String applicationId )
    {
        TypeDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getTypeDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("base");
        if(null != applicationBase) result = applicationBase.getTypeDefByName(name);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getTypeDefByName(name);
            if(null != result)
            {
                traceWarning("got Type definition for " + result.get_type_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public CommandDef getCommandDefByName(String name, String applicationId )
    {
        CommandDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getCommandDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("base");
        if(null != applicationBase) result = applicationBase.getCommandDefByName(name);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getCommandDefByName(name);
            if(null != result)
            {
                traceWarning("got Command definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public CommandDef getCommandDefByCode(int code, String applicationId )
    {
        CommandDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getCommandDefByCode(code);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("base");
        if(null != applicationBase) result = applicationBase.getCommandDefByCode(code);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getCommandDefByCode(code);
            if(null != result)
            {
                traceWarning("got Command definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public AvpDef getAvpDefByCode(int code, String applicationId )
    {
        AvpDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getAvpDefByCode(code);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("base");
        if(null != applicationBase) result = applicationBase.getAvpDefByCode(code);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getAvpDefByCode(code);
            if(null != result)
            {
                traceWarning("got AVP definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public AvpDef getAvpDefByName(String name, String applicationId )
    {
        AvpDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getAvpDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("base");
        if(null != applicationBase) result = applicationBase.getAvpDefByName(name);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getAvpDefByName(name);
            if(null != result)
            {
                traceWarning("got AVP definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public static boolean isInteger(String string)
    {
        if(null == string) return false ;
        for(int i=0; i<string.length(); i++) if(!Character.isDigit(string.charAt(i))) return false ;
        return true ;
    }
    
    public static void traceWarning(String text)
    {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Dictionary: ", text);
    }
    
    public static void traceDebug(String text)
    {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Dictionary: ", text);
    }
    
}
