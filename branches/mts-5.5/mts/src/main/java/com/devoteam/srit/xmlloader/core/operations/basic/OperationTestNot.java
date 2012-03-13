/*
 * Created on 9 sept. 2005
 *
 */
package com.devoteam.srit.xmlloader.core.operations.basic;


import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Scenario;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;

import java.util.ArrayList;
import org.dom4j.Element;

/**
 * enables your scenario to execute operations according to value of a boolean variable named condition
 *
 * @author ma007141
 *
 */
public class OperationTestNot extends Operation
{
    
    private Operation operation;
    
    private Scenario scenario;
    
    /**
     * Constructor
     *
     * @param operationsTests list of tests
     */
    public OperationTestNot(Element root, Scenario scenario) throws Exception
    {
        super(root);
        this.scenario = scenario;
        if(root.elements().size() != 1) throw new ParsingException("<not> operation should not contain more thant one operation");

        this.operation = scenario.parseOperation((Element) root.elements().get(0));
    }
    
    
    /**
     * Execute operation
     * 
     * 
     * @param runner Current runner
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);
        
        // Replace elements in XMLTree
        // No attribute to replace on <if> operation
        // replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);        
        
        boolean error = false;
        try
        {
            operation.executeAndStat(runner);
        }
        catch(AssertException e)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</test> (KO)\n", e.getMessage());
            error = true;
        }

        if(error)
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</not> (OK) (test failed)");
        }
        else
        {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "</not> (KO) (test succeeded)");
            throw new AssertException("</not> (KO) (test succeeded)");
        }

        return null;
    }
}
