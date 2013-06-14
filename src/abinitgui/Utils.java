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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Utils {

    private static DecimalFormat df = new DecimalFormat("#0.00");
    private static final Pattern DOS = Pattern.compile("\\r\\n", Pattern.MULTILINE);
    private static final Pattern UNIX = Pattern.compile("([^\\r])(\\n)", Pattern.MULTILINE);

    public static boolean mkdir(String dir) {
        File f = new File(dir);
        if (f.mkdir()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean exists(String dirOrFile) {
        File f = new File(dirOrFile);
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static String osName() {
        return System.getProperty("os.name");
    }

    public static String osArch() {
        return System.getProperty("os.arch");
    }

    public static String fileSeparator() {
        return System.getProperty("file.separator");
    }

    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    public static String inByteFormat(double size) {
        double tmp = size / 1024;
        if (tmp < 1) {
            return df.format(tmp * 1024) + " B";
        } else {
            tmp /= 1024;
            if (tmp < 1) {
                return df.format(tmp * 1024) + " kB";
            } else {
                tmp /= 1024;
                if (tmp < 1) {
                    return df.format(tmp * 1024) + " MB";
                } else {
                    tmp /= 1024;
                    if (tmp < 1) {
                        return df.format(tmp * 1024) + " GB";
                    } else {
                        return df.format(tmp) + " TB";
                    }
                }
            }
        }
    }

    public static String formatDouble(double d) {
        return df.format(d);
    }

    /*
     * Time is given in seconds
     */
    public static String formatTime(double seconds) {
        return df.format(seconds) + " s";
    }

    public static String getLastToken(String str, String delim) {
        StringTokenizer strt = new StringTokenizer(str, delim);
        String token = null;
        String tmp = null;
        int nbt = strt.countTokens();
        for (int i = 0; strt.hasMoreTokens(); i++) {
            tmp = strt.nextToken();
            if (i == nbt - 1) {
                token = tmp;
            }
        }
        return token;
    }

    public static String dos2unix(String text) {
        return DOS.matcher(text).replaceAll("\n");
    }

    public static String unix2dos(String text) {
        return UNIX.matcher(text).replaceAll("$1\r\n");
    }

    public static void dos2unix(File file) {

        try {
            Scanner scanner = new Scanner(file).useDelimiter("\\Z");
            String fileContent = scanner.next();
            scanner.close();
            String result = DOS.matcher(fileContent).replaceAll("\n");

            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(result);
            bw.close();
            fw.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void unix2dos(File file) {

        try {
            Scanner scanner = new Scanner(file).useDelimiter("\\Z");
            String fileContent = scanner.next();
            scanner.close();
            String result = UNIX.matcher(fileContent).replaceAll("$1\r\n");

            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(result);
            bw.close();
            fw.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String fileToString(String fileName) throws IOException {
        
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(fileName));
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append(" ");
            }
            
            return sb.toString();
    }
}
