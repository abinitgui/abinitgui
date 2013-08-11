/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import parser.AbinitInput;

/**
 *
 * @author yannick
 */
public class TestParser {
    
    public static void testAllInputsInPath(String pathAbinit, String outputFile)
    {
        AbinitInput ai = new AbinitInput();
        
        String path = pathAbinit+"/tests/";
        
        File pFile = new File(path);
        
        try{
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        
        
        if(pFile.isDirectory())
        {
            for(File child : pFile.listFiles())
            {
                if(child.isDirectory())
                {
                    String pathChild = child.getAbsolutePath();
                    
                    //System.out.println("pathChild = "+pathChild);
//                    if(!pathChild.contains("fast"))
//                    {
//                        continue;
//                    }
                    
                    File pathInput = new File(pathChild+"/Input/");
                    
                    if(pathInput.exists())
                    {
                    
                        for(File childInput : pathInput.listFiles())
                        {
                            String name = childInput.getAbsolutePath();

                            if(checkIfAbinitInput(name))
                            {
                                System.out.println("Testing : "+name);
                                testOneFile(name, pw);
                            }
                            else
                            {
                                System.out.println("Not testing : "+name);
                            }
                        }
                    }
                }
            }
        }
        
        pw.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void testOneInputFile(String inputFile, String outputFile)
    {
        try{
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            testOneFile(inputFile, pw);
            pw.close();
        }
        catch(Exception e)
        {
            ;
        }
    }
    
    public static boolean checkIfAbinitInput(String name)
    {
        boolean test = false;
        if(!name.endsWith(".in") || name.endsWith("report.in"))
        {
            return false;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(name));
            String line;
            while((line = br.readLine())!=null)
            {
                if(line.contains("executable") && line.contains("abinit"))
                {
                    test = true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TestParser.class.getName()).log(Level.SEVERE, null, ex);
            test = false;
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TestParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return test;
    }
    
    public static void testOneFile(String name, PrintWriter pw)
    {
        try {
            //pw.print("Reading file : "+name+" ...");

            new AbinitInput().readFromFile(name);
            //pw.println("OK !");
        } catch (Exception ex) {
            ex.printStackTrace();
            pw.println("Parsing file : "+name+" : Failed (Msg = "+ex.getMessage()+")");
            //Logger.getLogger(TestParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    public static void main(String[] args)
    {
        testAllInputsInPath("/home/yannick/abinit/7.5.1-private","test-all.txt");
        
        //testOneInputFile("/home/yannick/abinit/7.5.1-private/tests/v5/Input/t75.in","test-v5-75.txt");
    }
    
}
