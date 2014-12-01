/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package abinitgui.parser;

import abivars.AllInputVars;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

/**
 *
 * @author yannick
 */
public class AbinitDataset implements Iterable<AbinitVariable>
{
    private HashMap<String,AbinitVariable> allData;
    
    public AbinitDataset()
    {
        allData = new HashMap<>();
    }
    
    public AbinitVariable getVariable(String name)
    {
        return allData.get(name);
    }
    
    public void setVariable(String name, AbinitVariable variable)
    {
        allData.put(name, variable);
    }
    
    public Iterator<AbinitVariable> iterator()
    {
        return allData.values().iterator();
    }
    
    public void evaluateAll(Evaluator evaluator) throws EvaluationException
    {
        LinkedList<String> varsForDim = new LinkedList<>();
        LinkedList<String> varsForVal = new LinkedList<>();
        
        varsForDim.addAll(allData.keySet());
        varsForVal.addAll(allData.keySet());
        
        // Brute force !
        int maxnbiter = 10;
        while(maxnbiter > 0 && (varsForDim.size() > 0 || varsForVal.size() > 0))
        {
            LinkedList<String> newVarsForDims = new LinkedList<>(varsForDim);
            for(String varname : newVarsForDims)
            {
                AbinitVariable abivar = allData.get(varname);
                try{
                    abivar.evaluateDim(evaluator);
                    if(abivar.getDims() != null)
                    {
                        varsForDim.remove(varname);
                    }
                }
                catch(EvaluationException exc)
                {
                    System.err.println("exception encountered : "+exc);
                }
            }
            
            LinkedList<String> newVarsForVal = new LinkedList<>(varsForVal);
            for(String varname : newVarsForVal)
            {
                AbinitVariable abivar = allData.get(varname);
                try{
                    abivar.evaluateValue(evaluator);
                    if(abivar.getValue() != null)
                    {
                        Object obj = abivar.getValue();
                        varsForVal.remove(varname);
                        evaluator.putVariable(varname, obj.toString());
                    }
                }
                catch(EvaluationException exc)
                {
                    System.err.println("exception encoutered : "+exc);
                }
            }
            
            System.out.println("evaluator = "+evaluator.getVariables());
            
            maxnbiter--;
        }
        
        if(maxnbiter == 0)
        {
            System.err.println("Number max of iteration reached !");
            System.err.println("varsForDims = "+varsForDim);
            System.err.println("varsForVal = "+varsForVal);
        }
    }
}
