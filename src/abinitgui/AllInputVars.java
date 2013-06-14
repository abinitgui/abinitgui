/*
 Copyright (c) 2009-2013 Flavio Miguel ABREU ARAUJO (flavio.abreuaraujo@uclouvain.be)
                         Yannick GILLET (yannick.gillet@uclouvain.be)

Université catholique de Louvain, Louvain-la-Neuve, Belgium
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions, and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions, and the disclaimer that follows
these conditions in the documentation and/or other materials
provided with the distribution.

3. The names of the author may not be used to endorse or promote
products derived from this software without specific prior written
permission.

In addition, we request (but do not require) that you include in the
end-user documentation provided with the redistribution and/or in the
software itself an acknowledgement equivalent to the following:
"This product includes software developed by the
Abinit Project (http://www.abinit.org/)."

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

For more information on the Abinit Project, please see
<http://www.abinit.org/>.
 */

package abinitgui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import json.JSONObject;

public class AllInputVars 
{
    
    private HashMap<String, InputVar> database;
    private TreeSet<String> listKeys; 
    private MainFrame mf;
    
    public AllInputVars(MainFrame mf)
    {
        this.mf = mf;
        database = new HashMap<String, InputVar>();
        listKeys = new TreeSet<String>();
        loadVars();
    }
    
    
    
    public void loadVars()
    {
        try {
            
            String s = Utils.fileToString("ABINIT_variables.json");
            JSONObject obj = new JSONObject(s);
            listKeys.clear();
            // Sort names by insertion
            Iterator<String> iter = obj.keys();
            while(iter.hasNext())
            {
                String o = iter.next();
                listKeys.add(o);
            }

            iter = listKeys.iterator();
            
            while(iter.hasNext())
            {
                String varName = (String)iter.next();
                
                InputVar curVar = new InputVar(varName,obj.getJSONObject(varName));
                
                database.put(varName, curVar);
            }
            
        } catch (IOException ex) {
            mf.printERR("File 'ABINIT_variables.json' does not exist. Help and pre-processing might not be available");
        }
        
    }

    public TreeSet<String> getListKeys() {
        return listKeys;
    }

    public InputVar get(String varName) {
        return database.get(varName);
    }
    
    
    
}
