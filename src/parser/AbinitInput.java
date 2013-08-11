package parser;

import abinitgui.AllInputVars;
import abinitgui.InputVar;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import json.JSONArray;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author yannick
 */
public class AbinitInput 
{
    private String content = null;
    private double Ha_to_eV = 27.21138386;
    private double ev_to_Ha = 1.0/Ha_to_eV;
    private double kb_to_HaK = 8.617343E-5/Ha_to_eV;
    private double tesla_to_au = 4.254383E-6*0.5;
    private double bohr_to_angstrom=0.52917720859;
    private double angstrom_to_bohr=1.0/bohr_to_angstrom;
    private AllInputVars allInputs = null;
    private HashMap<String,ArrayList> map;
    private HashMap<String,String> mapString;
    private HashMap<String,Double> listOfUnits = null;
    private final String[] units = {"HA","HARTREE","RY","RYDBERG","BOHR","AU", "T","TE", "EV", "ANGSTROM", "ANGSTR", "ANGSTROMS", "K" };
    private final double[] scaling = {1.0,1.0,0.5,0.5,1.0,1.0,tesla_to_au,tesla_to_au,ev_to_Ha,angstrom_to_bohr,angstrom_to_bohr,angstrom_to_bohr,1.0};
    
    private HashMap<Integer,HashMap<String,Object>> allDatasets;
    private int ndtset = 0;
    private int udtset[] = new int[]{0,0};
    private ArrayList<Integer> jdtsets;
    private boolean usedtsets = false;
    private boolean useudtset = false;
    private boolean usejdtset = false;
    
    public AbinitInput()
    {
        allInputs = new AllInputVars(null);
        
        listOfUnits = new HashMap<>();
        
        for(int i = 0; i < units.length; i++)
        {
            listOfUnits.put(units[i],scaling[i]);
        }
    }
    public void readFromFile(String fileName) throws IOException
    {
        content = fileToString(fileName);
        content = content.trim(); // Remove spaces at the beginning and at the end
        content = content.replaceAll(" +"," "); // Replace multiple spaces by 1 space
        
        mapString = tabularize(content, allInputs);
        
        buildDtset(mapString);
    }
    
    @Override
    public String toString()
    {
        return "Abinit Input : "+mapString;
    }
    
    private HashMap<String, String> tabularize(String content, AllInputVars allInputs) throws InvalidInputFileException
    {
        HashMap<String,String> newMap = new HashMap<>();
        
        String[] splitted = content.split(" ");
        
        String keyword = null;
        StringBuilder sb = new StringBuilder();
        
        for(int i = 0; i < splitted.length; i++)
        {
            String word = splitted[i].toLowerCase();
            
            //System.out.print("Looking for word "+word+"...");
            
            if(isVariable(word))
            {
                if(keyword != null)
                {
                    newMap.put(keyword,sb.toString().trim());
                    sb = new StringBuilder();
                }
                keyword = word;
            }
            else if(isUnit(word) || isNumber(word) || word.contains("*"))
            {
                sb.append(word+" ");
            }
            else
            {
                throw new InvalidInputFileException("Unrecognized token : "+word);
            }
            
        }
        
        if(keyword != null)
        {
            newMap.put(keyword, sb.toString().trim());
        }
        
        
        return newMap;
    }
    
    // http://stackoverflow.com/questions/7597485/how-to-check-if-a-string-is-a-number
    public boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("(^-?)(\\d*)(.?)(\\d*$)"); // \\'D'?\\-?\\d* [^/]
        Matcher matcher = pattern.matcher(str);
        boolean isSimpleNumber = matcher.matches();
        pattern = Pattern.compile("(^-?)(\\d*)(.?)(\\d*)[DEFdef]([+-]?)(\\d+)(.?)(\\d*$)");
        matcher = pattern.matcher(str);
        boolean isExponential = matcher.matches();
        pattern = Pattern.compile("(^-?)(\\d*)(.?)(\\d*)/(-?)(\\d+)(.?)(\\d*$)");
        matcher = pattern.matcher(str);
        boolean isFraction = matcher.matches();
        boolean isSQRT = false;
        if(str.startsWith("sqrt(") || str.startsWith("SQRT("))
        {
            isSQRT = isNumber(str.substring(5,str.length()-1));
        }
        else if(str.startsWith("-sqrt(") || str.startsWith("-SQRT("))
        {
            isSQRT = isNumber(str.substring(6,str.length()-1));
        }
        return isSimpleNumber || isFraction || isExponential || isSQRT;
    }
    
    private boolean isUnit(String word)
    {
        return listOfUnits.keySet().contains(word.toUpperCase());
    }
    
    private boolean isVariable(String word)
    {
        word = word.trim();
        Pattern pattern = Pattern.compile("(^[a-zA-Z_]+)(\\d*)([a-zA-Z_]*)(\\d*$)");
        Matcher matcher = pattern.matcher(word);
        boolean isText = matcher.matches();
        pattern = Pattern.compile("");
        matcher = pattern.matcher(word);
        boolean isTextWithPlus = word.contains("+");
        boolean isTextWithPoint = word.contains(":");
        boolean isTextWithInter = word.contains("?");
        String curWord = word.replaceAll("\\+","").replaceAll(":", "").replaceAll("\\?","").replaceFirst("(\\d*$)", "");
        boolean isVar = allInputs.getListKeys().contains(curWord);
        return (isText || isTextWithPoint || isTextWithPlus || isTextWithInter) && isVar ;
    }
    
    // Returns a string from a file where the next line are appended.
    // Remove comments
    public String fileToString(String fileName) throws FileNotFoundException, IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        StringBuilder sb = new StringBuilder();
        while((line = br.readLine()) != null)
        {
            // Remove comments from line (starting with # or !)
            if(line.contains("#"))
            {
                if(line.startsWith("#"))
                    continue;
                line = line.split("#")[0];
            }
            
            if(line.contains("!"))
            {
                if(line.startsWith("!"))
                    continue;
                line = line.split("!")[0];
            }
            
            sb.append(line);
            sb.append(" ");
        }
        
        return sb.toString();
    }

    private void buildDtset(HashMap<String, String> mapString) throws InvalidInputFileException {
        
        // Find ndtset
        jdtsets = new ArrayList<>();
        
        String ndtset_S = mapString.get("ndtset");
        
        if(ndtset_S != null)
        {
            ndtset = Integer.parseInt(ndtset_S);
            usedtsets = true;
        }
        
        String udtset_S = mapString.get("udtset");
        
        if(udtset_S != null)
        {
            useudtset = true;
            String[] split = udtset_S.split(" ");
            if(split.length != 2)
            {
                throw new InvalidInputFileException("Udtset should contain exactly two int");
            }
            udtset[0] = Integer.parseInt(split[0]);
            udtset[1] = Integer.parseInt(split[1]);
            if(isUsedtsets())
            {
                if(getUdtset()[0]*getUdtset()[1] != getNdtset())
                {
                    throw new InvalidInputFileException("Ndtset should be the product of udtsets");
                }
                if(getUdtset()[0] > 999 || getUdtset()[0] < 1)
                {
                    throw new InvalidInputFileException("Udtset(1) should be between 1 and 999");
                }
                if(getUdtset()[1] > 9 || getUdtset()[1] < 1)
                {
                    throw new InvalidInputFileException("Udtset(2) should be between 1 and 9");
                }
            }
            usedtsets = true;
        }
        
        String jdtset_S = mapString.get("jdtset");
        
        if(jdtset_S == null)
        {
            if(isUseudtset())
            {
                for(int i = 1; i <= getUdtset()[0]; i++)
                {
                    for(int j = 1; j <= getUdtset()[1]; j++)
                    {
                        getJdtsets().add((i*10+j));
                    }
                }
                usejdtset = true;
            }
            else if(isUsedtsets())
            {
                for(int i = 1; i <= getNdtset(); i++)
                {
                    getJdtsets().add(i);
                }
                usejdtset = true;
            }
            else
            {
                usejdtset = false;
            }
            // Range from 1 to ndtset
        }
        else
        {
            if(isUseudtset())
            {
                throw new InvalidInputFileException("udtset and jdtset cannot be used at the same time !");
            }
            else
            {
                String[] split = jdtset_S.split(" ");
                for(String s : split)
                {
                    try
                    {
                        getJdtsets().add(new Integer(s));
                    }
                    catch(NumberFormatException e)
                    {
                        throw new InvalidInputFileException("Error in the syntax of jdtset");
                    }
                }
                usejdtset = true;
            }
        }
        
        //System.out.println("jdtsets = "+getJdtsets());
        
        allDatasets = new HashMap<>();
        
        if(isUsejdtset())
        {
            for(int idtset : getJdtsets())
            {
                HashMap<String,Object> values = readDataSet(mapString,idtset);
                
                getAllDatasets().put(idtset, values);
            }
        }
        else
        {
            HashMap<String,Object> values = readDataSet(mapString,0);
            
            getAllDatasets().put(0, values);
        }
        
        //System.out.println(getAllDatasets());
        
    }

    private HashMap<String, Object> readDataSet(HashMap<String, String> mapString, int idtset) throws InvalidInputFileException {
        
        HashMap<String,Object> curMap = new HashMap<>();
        
        Iterator<String> iter = allInputs.getListKeys().iterator();

        int units = idtset%10;
        int dozens = idtset/10;
        
        while(iter.hasNext())
        {
            String name = iter.next();
            
            InputVar var = allInputs.get(name);
            String value = mapString.get(name);
            if(isUsejdtset())
            {
                // Look for : and + and * possibilities !
                String cscolon = mapString.get(name+":");
                String csplus = mapString.get(name+"+");
                String cstimes = mapString.get(name+"*");
                
                String cs1colon = mapString.get(name+"?:");
                String cs1plus = mapString.get(name+"?+");
                String cs1times = mapString.get(name+"?*");
                
                String cs2colon = mapString.get(name+":?");
                String cs2plus = mapString.get(name+"+?");
                String cs2times = mapString.get(name+"*?");
                
                
                if(cscolon != null)
                {
                    if(csplus != null)
                    {
                        value = "=0+|"+cscolon+"|"+csplus;
                    }
                    else if(csplus != null)
                    {
                        value = "=0*|"+cscolon+"|"+cstimes;
                    }
                    else
                    {
                        throw new InvalidInputFileException("Check the increment for variable : "+name);
                    }
                }
                else if(cs1colon != null)
                {
                    if(cs1plus != null)
                    {
                        value = "=1+|"+cs1colon+"|"+cs1plus;
                    }
                    else if(cs1plus != null)
                    {
                        value = "=1*|"+cs1colon+"|"+cs1times;
                    }
                    else
                    {
                        throw new InvalidInputFileException("Check the increment for variable : "+name);
                    }
                }
                else if(cs2colon != null)
                {
                    if(cs2plus != null)
                    {
                        value = "=2+|"+cs2colon+"|"+cs2plus;
                    }
                    else if(cs2plus != null)
                    {
                        value = "=2*|"+cs2colon+"|"+cs2times;
                    }
                    else
                    {
                        throw new InvalidInputFileException("Check the increment for variable : "+name);
                    }
                }
                String curVal = mapString.get(name+"?"+units);
                if(curVal != null)
                {
                    // Parse the string to get the value
                    value = curVal;
                }
                
                curVal = mapString.get(name+dozens+"?");
                if(curVal != null)
                {
                    // Parse the string to get the value
                    value = curVal;
                }
                
                curVal = mapString.get(name+idtset);
                if(curVal != null)
                {
                    // Parse the string to get the value
                    value = curVal;
                }
                
            }
                    
            if(value != null)
            {
                ArrayList<Object> valueArray = getValue(name,value,var.vartype, var.dimensions,idtset);
                Object o = getObjectFromArray(valueArray,name,var.vartype,var.dimensions);
                curMap.put(name,o);
            }
        }
        
        return curMap;
    }
    
    public ArrayList<Object> getValue(String name, String text, String type, String dimensions, int jdtset) throws InvalidInputFileException
    {
        
        //System.out.println("Reading variable "+name+" with text = "+text+", type = "+type+", dimensions = "+dimensions);
        
        ArrayList<Object> listValues = new ArrayList<>();
        
        if(text.startsWith("="))
        {
            String splitted[] = text.split("\\|");
            
            String cmd = splitted[0];
            String startVal = splitted[1];
            String incVal = splitted[2];
            
            int unities = (jdtset%10);
            int dozens = (jdtset/10);
            int number = 0;
            
            switch(cmd.charAt(1))
            {
                case '0':
                    number = (jdtset-1);
                    break;
                case '1':
                    number = (unities-1);
                    break;
                case '2':
                    number = (dozens-1);
                    break;
            }
            
            ArrayList<Object> o1 = getValue(name,startVal,type,dimensions,jdtset);
            ArrayList<Object> o2 = getValue(name,incVal,type,dimensions,jdtset);
            
            if(o1.size() != o2.size())
            {
                throw new InvalidInputFileException("The increment and the starting value should have the same length");
            }
                
            for(int i = 0; i < o1.size(); i++)
            {
                if(type.contains("integer"))
                {
                    int i1 = (int)o1.get(i);
                    int i2 = (int)o2.get(i);

                    if(cmd.charAt(2) == '+')
                    {
                        listValues.add(i1+i2*number);
                    }
                    else
                    {
                        listValues.add((int)(i1*(Math.pow(i2,number))));
                    }
                }
                else if(type.contains("real"))
                {
                    double i1 = (double)o1.get(i);
                    double i2 = (double)o2.get(i);

                    if(cmd.charAt(2) == '+')
                    {
                        listValues.add(i1+i2*number);
                    }
                    else
                    {
                        listValues.add((i1*(Math.pow(i2,number))));
                    }
                }
            }
        }
        else
        {
            String[] allText = text.split(" ");

            String unit = allText[allText.length-1];
            double scalingFactor = 1.0d;
            int nbValues = allText.length;
            boolean isUnit = false;
            if(isUnit(unit))
            {
                if(type.contains("integer"))
                {
                    throw new InvalidInputFileException("Units can only be provided for real numbers !");
                }
                scalingFactor = listOfUnits.get(unit.toUpperCase());
                nbValues--;
                isUnit = true;
            }

            for(int i = 0; i < nbValues; i++)
            {
                String curValue = allText[i];

                if(curValue.contains("*"))
                {
                    if(curValue.startsWith("*"))
                    {
                        for(int j = 0; j < nbValues; j++)
                        {
                            if(isUnit)
                            {
                                listValues.add(scalingFactor*(Double)readData(curValue.substring(1), type));
                            }
                            else
                            {
                                listValues.add(readData(curValue.substring(1), type));
                            }
                        }
                    }
                    else
                    {
                        // Will split
                        //System.out.println("Will split value : "+curValue);
                        int nbTimes = Integer.parseInt(curValue.split("\\*")[0]);
                        for(int j = 0; j < nbTimes; j++)
                        {
                            if(isUnit)
                            {
                                listValues.add(scalingFactor*(Double)readData(curValue.split("\\*")[1],type));
                            }
                            else
                            {
                                listValues.add(readData(curValue.split("\\*")[1],type));
                            }
                        }
                    }
                }
                else
                {
                    if(isUnit)
                    {
                        listValues.add(scalingFactor*(Double)readData(curValue,type));
                    }
                    else
                    {
                        listValues.add(readData(curValue,type));
                    }
                }
            }
        }

        return listValues;
    }
    
    public int getDim(Object dim) throws InvalidInputFileException
    {
        int nb = 0;
        if(dim instanceof String)
        {
            String var = (String)dim;
            try{
                String data = mapString.get(var);
                if(data == null)
                {
                    // default value ?
                    switch(var)
                    {
                        // Since we have not yet decided how to deal with 
                        // the default value, ntypat and natom are hard-coded
                        // to make the parser with abinit test input files
                        case "ntypat":
                            nb = 1;
                            break;
                        case "natom":
                            nb = 1;
                            break;
                        default:
                            throw new Exception(var);
                    }
                }
                else
                {
                    nb = (int)readData(data,"integer"); // Temporary
                }
            }
            catch(Exception e)
            {
                if(mapString.get(var) == null)
                {
                    throw new InvalidInputFileException("Default values for "+var+" for dimensions are not yet supported by the parser... sorry for inconvenience");
                }
                else
                {
                    throw new InvalidInputFileException("Problem with dimension : "+var);
                }
            }
        }
        else
        {
            nb = (Integer)dim;
        }
        
        return nb;
    }
    
    public Number readData(String text, String type) throws InvalidInputFileException
    {
        Double val = null;
        
        if(type.contains("integer"))
        {
            int i = -1;
            try{
                
                i = Integer.parseInt(text);
            }
            catch(NumberFormatException ex)
            {
                throw new InvalidInputFileException("Error parsing text : "+text+" [should be integer]");
            }
            return i;
        }
        else if(type.contains("real"))
        {
            text = text.replace("d", "e");

            if(text.contains("/"))
            {
                String[] splitted = text.split("/");
                Double d1 = (Double)readData(splitted[0],type);
                Double d2 = (Double)readData(splitted[1],type);
                val = d1/d2;
            }
            else if(text.startsWith("sqrt(") || text.startsWith("SQRT("))
            {
                String sub= text.substring(5,text.length()-1);
                Double v = (Double)readData(sub,type);
                return Math.sqrt(v);
            }
            else if(text.startsWith("-sqrt(") || text.startsWith("-SQRT("))
            {
                String sub= text.substring(6,text.length()-1);
                Double v = (Double)readData(sub,type);
                return -Math.sqrt(v);
            }
            else
            {
               // System.out.println("ParseDouble : "+text);
                try
                {
                    val = Double.parseDouble(text);
                }
                catch(NumberFormatException ex)
                {
                    throw new InvalidInputFileException("Error parsing text : "+text+" [should be real]");
                }
            }

            return val;
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the allDatasets
     */
    public HashMap<Integer,HashMap<String,Object>> getAllDatasets() {
        return allDatasets;
    }

    /**
     * @return the ndtset
     */
    public int getNdtset() {
        return ndtset;
    }

    /**
     * @return the udtset
     */
    public int[] getUdtset() {
        return udtset;
    }

    /**
     * @return the jdtsets
     */
    public ArrayList<Integer> getJdtsets() {
        return jdtsets;
    }

    /**
     * @return the usedtsets
     */
    public boolean isUsedtsets() {
        return usedtsets;
    }

    /**
     * @return the useudtset
     */
    public boolean isUseudtset() {
        return useudtset;
    }

    /**
     * @return the usejdtset
     */
    public boolean isUsejdtset() {
        return usejdtset;
    }

    private Object getObjectFromArray(ArrayList<Object> listValues, String name, String type, String dimensions) 
            throws InvalidInputFileException 
    {
        
        JSONArray dims = new JSONArray(dimensions);
        
        if(dims.length() == 0)
        {
            // We don't know the size of table
            if(listValues.size() == 1)
            {
                if(type.contains("integer"))
                {
                    return (int)(listValues.get(0));
                }
                else if(type.contains("real"))
                {
                    return (double)(listValues.get(0));
                }
            }
            else
            {
                if(type.contains("integer"))
                {
                    return listValues.toArray(new Integer[0]);
                }
                else if(type.contains("real"))
                {
                    return listValues.toArray(new Double[0]);
                }
            }
            
        }
        else if(dims.length() == 1)
        {
            int length = getDim(dims.get(0));
            
            if(length != listValues.size())
            {
                throw new InvalidInputFileException("Mismatch for var = "+name+" between doc dim ("+length+") and input file ("+listValues.size()+")");
            }
            if(type.contains("integer"))
            {
                return listValues.toArray(new Integer[0]);
            }
            else if(type.contains("real"))
            {
                return listValues.toArray(new Double[0]);
            }
        }
        else if(dims.length() == 2)
        {
            int length1 = getDim(dims.get(0));
            int length2 = getDim(dims.get(1));
            
            if(length1*length2 != listValues.size())
            {
                throw new InvalidInputFileException("Mismatch between doc dim ("+length1+"x"+length2+") and input file ("+listValues.size()+")");
            }
            
            Number[][] tab = null;
            if(type.contains("integer"))
            {
                tab = new Integer[length1][length2];
            }
            else if(type.contains("real"))
            {
                tab = new Double[length1][length2];
            }
            
            int index = 0;
            for(int i = 0; i < length1; i++)
            {
                for(int k = 0; k < length2; k++)
                {
                    tab[i][k] = (Number)listValues.get(index);
                    index++;
                }
            }
            return tab;
        }
        
        return null;
    }

    private static class InvalidInputFileException extends IOException {

        public InvalidInputFileException(String unit_without_values_) {
            super(unit_without_values_);
        }
    }

}
