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
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class AbinitParser {
    
    AbinitGeometry geom;
    
    // Factor conversion bohr to angstrom
    protected static final double ANGSTROMPERBOHR = 0.529177249f;

    // This variable is used to parse the input file
    protected StringTokenizer st;
    protected String fieldVal;
    protected int repVal = 0;

    protected BufferedReader inputBuffer;
    
    public AbinitGeometry readAbinitInput(String fileName) throws IOException
    {
        geom = new AbinitGeometry();
        
        inputBuffer = new BufferedReader(new FileReader(fileName));
        
        fieldVal = null;
        st = null;
        
        String curVar = null;
        
        inputBuffer.mark(0);
        nextAbinitInputToken(true);
        while (fieldVal != null) {
          if (fieldVal.equals("ndtset")) {
            nextAbinitInputToken(false);
            if (atof(fieldVal) > 1) {
              // printDEB("ABINITReader: multidataset not supported");
              return null;
            }
          }
          nextAbinitInputToken(false);
        }
        inputBuffer = new BufferedReader(new FileReader(fileName));

        //First pass through the file (get variables of known dimension)
        nextAbinitInputToken(true);
        while (fieldVal != null) {
          if (fieldVal.equals("acell")) {
            geom.acell = new Double[3];
            for (int i = 0; i < 3; i++) {
              nextAbinitInputToken(false);
              geom.acell[i] = atof(fieldVal)
                * ANGSTROMPERBOHR;    //in angstrom
            }
          } else if (fieldVal.equals("rprim")) {
            geom.rprim = new Double[3][3];
            for (int i = 0; i < 3; i++) {
              for (int j = 0; j < 3; j++) {
                nextAbinitInputToken(false);
                geom.rprim[i][j] = atof(fieldVal);
              }
            }
          } else if (fieldVal.equals("rprimd")) {
            geom.rprimd = new Double[3][3];
            for (int i = 0; i < 3; i++) {
              for (int j = 0; j < 3; j++) {
                nextAbinitInputToken(false);
                geom.rprimd[i][j] = atof(fieldVal);
              }
            }
          } else if (fieldVal.equals("scalecart")) {
            geom.scalecart = new Double[3];
            for (int i = 0; i < 3; i++) {
              nextAbinitInputToken(false);
              geom.scalecart[i] = atof(fieldVal);
            }
          } else if (fieldVal.equals("angdeg")) {
            geom.angdeg = new Double[3];
            for (int i = 0; i < 3; i++) {
              nextAbinitInputToken(false);
              geom.angdeg[i] = atof(fieldVal);
            }
          } else if (fieldVal.equals("ntypat")) {
            nextAbinitInputToken(false);
            geom.ntypat = Integer.parseInt(fieldVal);
          } else if (fieldVal.equals("natom")) {
            nextAbinitInputToken(false);
            geom.natom = Integer.parseInt(fieldVal);
          }

          // It is unnecessary to scan the end of the line. 
          // Go directly to the next line
          nextAbinitInputToken(true);
        }

        //Initialize dynamic variables
        geom.znucl = new Double[geom.ntypat];
        geom.typat = new Integer[geom.natom];

        //Second pass through the file
        inputBuffer = new BufferedReader(new FileReader(fileName));
         
        nextAbinitInputToken(true);
        while (fieldVal != null) {
          if (fieldVal.equals("znucl")) {
            for (int i = 0; i < geom.ntypat; i++) {
              nextAbinitInputToken(false);
              geom.znucl[i] = (atof(fieldVal));
            }
          } else if (fieldVal.equals("typat")) {
            for (int i = 0; i < geom.natom; i++) {
              nextAbinitInputToken(false);
              geom.typat[i] = Integer.parseInt(fieldVal);
            }
          } else if (fieldVal.equals("xangst")) {
            geom.xangst = new Double[geom.natom][3];
            for (int i = 0; i < geom.natom; i++) {
              for (int j = 0; j < 3; j++) {
                nextAbinitInputToken(false);
                geom.xangst[i][j] = atof(fieldVal);
              }
            }
          } else if (fieldVal.equals("xcart")) {
            geom.xangst = new Double[geom.natom][3];
            for (int i = 0; i < geom.natom; i++) {
              for (int j = 0; j < 3; j++) {
                nextAbinitInputToken(false);
                geom.xangst[i][j] = atof(fieldVal)
                  * ANGSTROMPERBOHR;
              }
            }
          } else if (fieldVal.equals("xred")) {
            geom.xred = new Double[geom.natom][3];
            for (int i = 0; i < geom.natom; i++) {
              for (int j = 0; j < 3; j++) {
                nextAbinitInputToken(false);
                geom.xred[i][j] = atof(fieldVal);
              }
            }
          }

          // It is unnecessary to scan the end of the line. 
          // Go directly to the next line
          nextAbinitInputToken(true);

        }
        
        inputBuffer.close();
        
        return geom;
    }
    
    
  /**
   * Put the next abinit token in <code>fieldVal</code>.
   * If <code>newLine</code> is <code>true</code>, the end of the line
   * is skiped. <br>
   * The first invocation of this method should be done with
   * <code>newLine</code> is <code>true</code>.
   *
   */
  public void nextAbinitInputToken(boolean newLine) throws IOException {
    
    String line;
    
    if (newLine) { //We ignore the end of the line and go to the following line
      repVal = 0;
      if (inputBuffer.ready()) {
        line = inputBuffer.readLine();
        st = new StringTokenizer(line, " \t");
      }
    }


    if (repVal != 0) {
      repVal--;
    } else {
      while (!st.hasMoreTokens() && inputBuffer.ready()) {
        line = inputBuffer.readLine();
        st = new StringTokenizer(line, " \t");
      }
      if (st.hasMoreTokens()) {
        fieldVal = st.nextToken();
        int index = fieldVal.indexOf("*");
        if (index >= 0) {

          //We have a format integer*double
            try{
          repVal = Integer.parseInt(fieldVal.substring(0, index));
          fieldVal = fieldVal.substring(index + 1);
          repVal--;
            }catch(NumberFormatException e)
            {
                nextAbinitInputToken(true);
            }
          
        }
        if (fieldVal.startsWith("#")) {    //We have a comment
          nextAbinitInputToken(true);      //Skip the end of the line
        }
      } else {
        fieldVal = null;
      }
    }
  } //end nextAbinitInputToken(boolean newLine)
  
    public static double atof(String s) {

    int i = 0;
    int sign = 1;
    double r = 0;     // integer part
    double f = 0;     // fractional part
    double p = 1;     // exponent of fractional part
    int state = 0;    // 0 = int part, 1 = frac part
    int nbNo = 0;     // 0 = first number, 1 = second number (should divide at the end) !
    double nb1 = 0.0;
    while ((i < s.length()) && Character.isWhitespace(s.charAt(i))) {
      i++;
    }
    if ((i < s.length()) && (s.charAt(i) == '-')) {
      sign = -1;
      i++;
    } else if ((i < s.length()) && (s.charAt(i) == '+')) {
      i++;
    }
    while (i < s.length()) {
      char ch = s.charAt(i);
      if (('0' <= ch) && (ch <= '9')) {
        if (state == 0) {
          r = r * 10 + ch - '0';
        } else if (state == 1) {
          p /= 10;
          r += p * (ch - '0');
        }
      } else if (ch == '.') {
        if (state == 0) {
          state = 1;
        } else {
            if(nbNo == 0)
            {
                return sign * r;
            }
            else
            {
                return nb1 / (sign*r);
            }
        }
      } else if ((ch == 'e') || (ch == 'E') || (ch == 'd') || (ch == 'D')) {
        long e = (int) parseLong(s.substring(i + 1), 10);
        if(s.contains("/"))
        {
            int ind = s.indexOf('/');
            e = (int) parseLong(s.substring(i+1,ind), 10);
            
            double val = atof(s.substring(ind+1));
            return sign*r*Math.pow(10,e)/val;
        }
        else
        {
            return sign * r * Math.pow(10, e);
        }
      } else if ((ch == '/')) {
        nb1 = sign*r;
        nbNo = 1;
        r = 0;
        sign = 1;
      } else {
            if(nbNo == 0)
            {
                return sign * r;
            }
            else
            {
                return nb1 / (sign*r);
            }
      }
      i++;
    }
    
    if(nbNo == 0)
    {
        return sign * r;
    }
    else
    {
        return nb1 / (sign*r);
    }
  }
    
    private static long parseLong(String s, int base) {

    int i = 0;
    int sign = 1;
    long r = 0;

    while ((i < s.length()) && Character.isWhitespace(s.charAt(i))) {
      i++;
    }
    if ((i < s.length()) && (s.charAt(i) == '-')) {
      sign = -1;
      i++;
    } else if ((i < s.length()) && (s.charAt(i) == '+')) {
      i++;
    }
    while (i < s.length()) {
      char ch = s.charAt(i);
      if (('0' <= ch) && (ch < '0' + base)) {
        r = r * base + ch - '0';
      } else if (('A' <= ch) && (ch < 'A' + base - 10)) {
        r = r * base + ch - 'A' + 10;
      } else if (('a' <= ch) && (ch < 'a' + base - 10)) {
        r = r * base + ch - 'a' + 10;
      } else {
        return r * sign;
      }
      i++;
    }
    return r * sign;
  }
}
