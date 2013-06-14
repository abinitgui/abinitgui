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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import json.JSONArray;

public class AbinitGeometry {
    
    // Fixed sized tables
    public Double acell[] = null;
    public Double scalecart[] = null;
    public Double angdeg[] = null;
    public Double rprim[][] = null;
    public Double rprimd[][] = null;
    
    // Varying size table
    public int natom = 0;
    public Double znucl[] = null; // znucl(ntypat)
    
    public int ntypat = 0;
    public Integer typat[] = null; // typat(natom)
    
    public Double xangst[][] = null; // xangst(natom,3)
    public Double xred[][] = null; // xred(natom,3)
    public Double xcart[][] = null; // xcart(natom,3)
    
    public Double allpositions[][] = null; // allpositions(allatoms,3)
    public int allatoms;
    
    public Double allznucl[] = null; // allznucl(allatoms)
    
    public final List<String> allNames=Arrays.asList("acell","angdeg","natom",
            "ntypat","rprim","rprimd","scalecart","typat","xangst","xred",
            "xcart","znucl");
    
    protected static final double ANGSTROMPERBOHR = 0.529177249f;
    
    public AbinitGeometry()
    {
    }
    
    public String toString()
    {
        String s = "";
        
        s = "acell = "+printArray(acell)+"\n"+
                "angdeg = "+printArray(angdeg)+"\n"+
                "natom = "+printArray(natom)+"\n"+
                "ntypat = "+printArray(ntypat)+"\n"+
                "rprim = "+printArray(rprim)+"\n"+
                "rprimd = "+printArray(rprimd)+"\n"+
                "scalecart = "+printArray(scalecart)+"\n"+
                "typat = "+printArray(typat)+"\n"+
                "xangst = "+printArray(xangst)+"\n"+
                "xred = "+printArray(xred)+"\n"+
                "xcart = "+printArray(xcart)+"\n"+
                "znucl = "+printArray(znucl)+"\n";
        
        return s;
    }
    
    public String printArray(double val)
    {
        return ""+val;
    }
    
    public String printArray(Double[] array)
    {
        if(array == null)
        {
            return "null";
        }
        
        String s="[";
        
        for(int i = 0; i < array.length; i++)
        {
            s = s+array[i]+";";
        }
        
        return s+"]";
    }
    
    public String printArray(Integer[] array)
    {
        if(array == null)
        {
            return "null";
        }
        String s="[";
        
        for(int i = 0; i < array.length; i++)
        {
            s = s+array[i]+";";
        }
        
        return s+"]";
    }
    
    public String printArray(Double[][] array)
    {
        if(array == null)
        {
            return "null";
        }
        String s="[";
        
        for(int i = 0; i < array.length; i++)
        {
            s = s + "[";
            for(int j = 0;j < array[i].length; j++)
            {
                s = s+array[i][j]+";";
            }
            s = s+"]\n";
        }
        
        return s+"]";
    }
    
    public String printArray(Integer[][] array)
    {
        if(array == null)
        {
            return "null";
        }
        String s="[";
        
        for(int i = 0; i < array.length; i++)
        {
            s = s + "[";
            for(int j = 0;j < array[i].length; j++)
            {
                s = s+array[i][j]+";";
            }
            s = s+"]\n";
        }
        
        return s+"]";
    }
    
    public void readFromJSON(int natom, int ntypat, JSONArray typat, JSONArray znucl, JSONArray xred_orig, JSONArray rprimd)
    {
         this.natom = natom;
         this.ntypat = ntypat;
         this.typat = new Integer[natom];
         for(int i = 0; i < natom; i++)
         {
             this.typat[i] = typat.getInt(i);
         }
         
         this.znucl = new Double[ntypat];
         for(int i = 0; i < ntypat; i++)
         {
             this.znucl[i] = (double)znucl.getInt(i);
         }
         
         this.xred = new Double[this.natom][3];
         
         for(int i = 0; i < natom; i++)
         {
             this.xred[i][0] = xred_orig.getJSONArray(0).getJSONArray(i).getDouble(0);
             this.xred[i][1] = xred_orig.getJSONArray(0).getJSONArray(i).getDouble(1);
             this.xred[i][2] = xred_orig.getJSONArray(0).getJSONArray(i).getDouble(2);
         }
         
         
         this.rprimd = new Double[3][3];
         for(int i = 0; i < 3; i++)
         {
             this.rprimd[i][0] = rprimd.getJSONArray(0).getJSONArray(i).getDouble(0)*ANGSTROMPERBOHR;
             this.rprimd[i][1] = rprimd.getJSONArray(0).getJSONArray(i).getDouble(1)*ANGSTROMPERBOHR;
             this.rprimd[i][2] = rprimd.getJSONArray(0).getJSONArray(i).getDouble(2)*ANGSTROMPERBOHR;
         }
    }

    public void loadData(HashMap<String,Object> map)
    {
        Iterator<String> iter = map.keySet().iterator();
        
        Object o = map.get("natom");
        if(o != null) this.natom = (int) o;
        
        o = map.get("ntypat");
        if(o != null) this.ntypat = (int) o;
        
        o = map.get("typat");
        if(o != null) this.typat = (Integer[])o;
        
        o = map.get("znucl");
        if(o != null) this.znucl = (Double[])o;
        
        o = map.get("xred");
        if(o != null) this.xred = (Double[][])o;
        
        o = map.get("xcart");
        if(o != null) this.xcart = (Double[][])o;
        
        o = map.get("rprim");
        if(o != null) this.rprim = (Double[][])o;
        
        o = map.get("rprimd");
        if(o != null) this.rprimd = (Double[][])o;
        
        o = map.get("scalecart");
        if(o != null) this.scalecart = (Double[])o;
        
        o = map.get("xangst");
        if(o != null) this.xangst = (Double[][])o;
        
        o = map.get("acell");
        if(o != null) this.acell = (Double[])o;
        
        o = map.get("angdeg");
        if(o != null) this.angdeg  = (Double[])o;
        

    }

    public boolean fillData() {
        DecimalFormat df_rprim = new DecimalFormat("#0.0000000000000");

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df_rprim.setDecimalFormatSymbols(dfs);
        
        if(rprim != null && rprimd != null)
        {
            System.err.println("Rprim & Rprimd are not null");
            return false;
        }
        
        if((xred != null && xcart != null) || (xred != null && xangst != null)
                || (xcart != null && xangst != null))
        {
            System.err.println("Different positions assigned at the same time");
            return false;
        }
        
        if(rprimd == null)
        {
            rprimd = new Double[3][3];
            if(acell == null)
            {
                acell = new Double[3];
                acell[0] = 1 * ANGSTROMPERBOHR;    //in angstrom    
                acell[1] = 1 * ANGSTROMPERBOHR;
                acell[2] = 1 * ANGSTROMPERBOHR;
            }
            if(angdeg == null && rprim == null)
            {
                angdeg = new Double[3];
                angdeg[0] = 90D;
                angdeg[1] = 90D;
                angdeg[2] = 90D;
            }
            else if(angdeg == null)
            {
                angdeg = new Double[3];
                
                 double dot23 = 0;
                 double dot13 = 0;
                 double dot12 = 0;
                 
                for(int j = 0; j < 3; j++)
                {
                    dot23 += rprim[1][j]*rprim[2][j];
                    dot13 += rprim[0][j]*rprim[2][j];
                    dot12 += rprim[0][j]*rprim[1][j];
                }
                
                angdeg[0] = 180/Math.PI*Math.acos(dot23);
                angdeg[1] = 180/Math.PI*Math.acos(dot13);
                angdeg[2] = 180/Math.PI*Math.acos(dot12);
            }
           
            if(rprim == null)
            {
                rprim = new Double[3][3];
                Double angdeg1 = angdeg[0]; // angdeg(1)
                Double angdeg2 = angdeg[1]; // angdeg(2)
                Double angdeg3 = angdeg[2]; // angdeg(3)

                Double rprim11;
                Double rprim12;
                Double rprim13;
                Double rprim21;
                Double rprim22;
                Double rprim23;
                Double rprim31;
                Double rprim32;
                Double rprim33;

                if (angdeg1.equals(angdeg2) && angdeg1.equals(angdeg3) && !angdeg1.equals(90.0)) {
                    Double cosang = Math.cos(Math.PI * angdeg1 / 180.0);
                    Double a2 = 2.0 / 3.0 * (1.0 - cosang);
                    Double aa = Math.sqrt(a2);
                    Double cc = Math.sqrt(1.0 - a2);
                    rprim11 = aa;
                    rprim21 = 0.0;
                    rprim31 = cc;
                    rprim12 = -0.5 * aa;
                    rprim22 = Math.sqrt(3.0) * 0.5 * aa;
                    rprim32 = cc;
                    rprim13 = -0.5 * aa;
                    rprim23 = -Math.sqrt(3.0) * 0.5 * aa;
                    rprim33 = cc;
                } else {
                    rprim11 = new Double(1.0); // rprim(1,1) OK
                    rprim12 = new Double(Math.cos(Math.PI * angdeg3 / 180.0)); // rprim(1,2) OK
                    rprim13 = new Double(Math.cos(Math.PI * angdeg2 / 180.0)); // rprim(1,3) OK

                    rprim21 = new Double(0.0); // rprim(2,1)
                    rprim22 = new Double(Math.sin(Math.PI * angdeg3 / 180.0)); // rprim(2,2) OK
                    rprim23 = new Double(((Math.cos(Math.PI * angdeg1 / 180.0)
                            - rprim12 * rprim13) / rprim22)); // rprim(2,3) OK

                    rprim31 = new Double(0.0); // rprim(3,1)
                    rprim32 = new Double(0.0); // rprim(3,2)
                    rprim33 = new Double(Math.sqrt(1.0 - Math.pow(rprim13, 2)
                            - Math.pow(rprim23, 2))); // rprim(3,3) OK
                }

                rprim[0][0] = new Double(df_rprim.format(rprim11));
                rprim[1][0] = new Double(df_rprim.format(rprim12));
                rprim[2][0] = new Double(df_rprim.format(rprim13));

                rprim[0][1] = new Double(df_rprim.format(rprim21));
                rprim[1][1] = new Double(df_rprim.format(rprim22));
                rprim[2][1] = new Double(df_rprim.format(rprim23));

                rprim[0][2] = new Double(df_rprim.format(rprim31));
                rprim[1][2] = new Double(df_rprim.format(rprim32));
                rprim[2][2] = new Double(df_rprim.format(rprim33));
            }
            
            if(scalecart == null)
            {
                scalecart= new Double[3];
                scalecart[0] = 1D;
                scalecart[1] = 1D;
                scalecart[2] = 1D;
            }
            
            // Compute rprimd !
            for(int i = 0; i < 3; i++)
            {
                for(int j = 0; j < 3; j++)
                {
                    // Check order of indices
                    rprimd[i][j] = scalecart[j]*rprim[i][j]*acell[i]*ANGSTROMPERBOHR;
                }
            }
            System.out.println(printArray(rprimd));
            
            System.out.println(printArray(acell));
        }
        
        
        
        if(xangst == null)
        {
            xangst = new Double[natom][3];
            if(xcart != null)
            {
                for(int i = 0; i < natom; i++)
                {
                    for(int j = 0; j < 3; j++)
                    {
                        xangst[i][j] = xcart[i][j]*ANGSTROMPERBOHR;
                    }
                }
            }
            if(xred != null)
            {
                for(int i = 0; i < natom; i++)
                {
                    for(int j = 0; j < 3; j++)
                    {
                        xangst[i][j] = 0.0D;
                        for(int k = 0; k < 3; k++)
                        {
                            // Should check order of indices
                            xangst[i][j] = xangst[i][j] + rprimd[k][j]*xred[i][k];
                        }
                    }
                }
            }
        }
        
        return true;
        
        
    }
    
    public void computeReplicas(int nbX, int nbY, int nbZ)
    {
        allatoms = natom*nbX*nbY*nbZ;
        
        allpositions = new Double[allatoms][3];
        allznucl = new Double[allatoms];
        
        for(int i = 0; i < natom; i++)
        {
            for(int x = 0; x < nbX; x++)
            {
                for(int y = 0; y < nbY; y++)
                {
                    for(int z = 0; z < nbZ; z++)
                    {
                        int atomindex = (((x*nbY)+y)*nbZ+z)*natom+i;
                        for(int j = 0; j < 3; j++)
                        {
                            allznucl[atomindex] = znucl[typat[i]-1];
                            allpositions[atomindex][j] = 
                                    x*rprimd[0][j]+y*rprimd[1][j]+z*rprimd[2][j]+xangst[i][j];
                        }
                    }
                }
            }
        }
    }
    
    public void writeIntoXYZ(String fileName) throws IOException
    {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        pw.println(""+allatoms);
        String unitcell = "jmolscript: load \"\" packed unitcell {"+acell[0]+" "+acell[1]+" "+acell[2]+" "+angdeg[0]+" "+angdeg[1]+" "+angdeg[2]+"}";
        
        pw.println(unitcell);
        for(int i = 0; i < allatoms; i++)
        {
            String s = ""+Atom.getSymbolByZnucl((int)(Math.floor(allznucl[i])));
            
            for(int j = 0; j < 3; j++)
            {
                s = s + " " + allpositions[i][j];
            }
            
            pw.println(s);
        }
        
        pw.close();
    }
    
    public void writeIntoAIMS(String fileName) throws IOException
    {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        
        for(int i = 0; i < 3; i++)
        {
           String latticevect = "lattice_vector\t";
           for(int j = 0; j < 3; j++)
           {
               latticevect += rprimd[i][j]+"\t";
           }
           pw.println(latticevect);
           System.out.println("Writing inside "+fileName+" latticevect = "+latticevect);
        }
        
        for(int i = 0; i < allatoms; i++)
        {
            String s = "atom\t";
            
            for(int j = 0; j < 3; j++)
            {
                s = s + " " + allpositions[i][j]+"\t";
            }
            
            s = s + Atom.getSymbolByZnucl((int)(Math.floor(allznucl[i])));
            
            pw.println(s);
        }
        
        pw.close();
    }
    
}
