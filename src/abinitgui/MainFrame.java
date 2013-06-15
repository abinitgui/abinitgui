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

import MDandTB.ClustepDiag;
import MDandTB.TightBindingDiag;
import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.jdom.*;

//@SuppressWarnings({"deprecation", "serial"})
public class MainFrame extends javax.swing.JFrame {

    private SSH ssh = null;
    private MyTableModel pspModel = null;
    private MyTableModel pspModel1 = null;
    public LocalExec localExec;
    public RemoteExec remoteExec;
    private SSHTunnel sshtun;
    private boolean useKey1 = false;
    private boolean useKey2 = false;
    private AboutDialog about;
    private int lport = 0;
    private DisplayerJDialog outDialog;
    private DisplayerJDialog inputFileDisplayer;
    public DisplayerJDialog clustepInputFileDisplayer;
    public DisplayerJDialog clustepPositionFileDisplayer;
    private GeomDialog geomD;
    private AlCoDialog alcoD;
    private ReReDialog rereD;
    private WaDeDialog wadeD;
    private InOuDialog inouD;
    private TheoDialog theoD;
    //private VarsHelp varsHelp;
    // Avant la version 6 de ABINIT, il y avait deux exécutables différents
    private String SequAbinit = "abinit";
    private String ParaAbinit = "abinit";
    public String CharSet = "UTF-8";
    private static boolean autoTest;
    public static String Version = "0.3";
    public static String SubVersion = "0.1";
    public static String VerMonth = "Apr";
    public static String VerYear = "2013";
    private String outputFile = "out.log";
    public static OutputStreamWriter fw;
    public static BufferedWriter bw;
    public static PrintWriter pw;
    private MainFrame mainFrame;
    public boolean DEBUG = false;
    private ClustepDiag clustepDiag;
    private TightBindingDiag tightBindingDiag;
    private DefaultListModel scriptModel;
    private final ScriptBib scriptBibs;
    private final ScriptTableModel argsModel;
    private final ScriptTableModel outModel;
    private String curPath = "."; // to save current Path !
    private ProjectManager projectManager;
    private GUIEditor guiEditor;
    private GUIEditorNew guiEditorNew;
    private AbinitInputVars abinitInputVars;
    private Simulation simulation;
    private SubmissionScriptFrame submitScriptFrame;
    private InputEditor inputEditor;
    private final AllInputVars allInputVars;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {

        mainFrame = this;

        initComponents();

        //JFrame.setDefaultLookAndFeelDecorated(true);
        //JDialog.setDefaultLookAndFeelDecorated(true);

        // -------------- Output stream files ----------------------------------
        try {
            fw = new OutputStreamWriter(new FileOutputStream(outputFile),
                    CharSet);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
        } catch (UnsupportedEncodingException ex) {
            System.err.println("UnsupportedEncodingException (MainFrame())");
        } catch (FileNotFoundException ex) {
            System.err.println("FileNotFoundException (MainFrame())");
        }
        // ---------------------------------------------------------------------

        this.setTitle("AbinitGUI (v. " + Version + " " + VerMonth
                + " " + VerYear + ")");

        outDialog = new DisplayerJDialog(this, false);
        outDialog.setTitle("..:: Global MSG Display ::..");

        inputFileDisplayer = new DisplayerJDialog(this, false);
        inputFileDisplayer.setTitle("..:: Input file preview ::..");

        clustepInputFileDisplayer = new DisplayerJDialog(this, false);
        clustepInputFileDisplayer.setTitle("..:: Clustep input file preview ::..");

        clustepPositionFileDisplayer = new DisplayerJDialog(this, false);
        clustepPositionFileDisplayer.setTitle("..:: Clustep position file preview ::..");

        geomD = new GeomDialog(this, false);
        geomD.setTitle("..:: Geometry ::..");

        alcoD = new AlCoDialog(this, false);
        alcoD.setTitle("..:: Algorithm and convergence ::..");

        rereD = new ReReDialog(this, false);
        rereD.setTitle("..:: Real and reciprocal space ::..");

        wadeD = new WaDeDialog(this, false);
        wadeD.setTitle("..:: Wavefunctions and densities ::..");

        inouD = new InOuDialog(this, false);
        inouD.setTitle("..:: Input / Output ::..");

        theoD = new TheoDialog(this, false);
        theoD.setTitle("..:: Theory (DFT) ::..");

        //varsHelp = new VarsHelp();
        //varsHelp.setTitle("..:: Abinit variables help ::..");

        pspModel = new MyTableModel(pspTable);
        pspModel.setNotEditableCol("1-3");
        pspTable.setModel(pspModel);
        initTableHeader(pspTable, new String[]{"Atom", "PSP filename", "PSP type", "PSP path"},
                new Integer[]{null, null, null, null});
        pspTable.setDefaultRenderer(Atom.class,
                new pspAtomRenderer());
        pspTable.setDefaultEditor(Atom.class,
                new AtomEditor(this));

        localExec = new LocalExec(this);

        about = new AboutDialog(this, true);

        // ---------------------------------------------------------------------
        clustepDiag = new ClustepDiag(this, false);
        clustepDiag.setTitle("..:: Clustep ::..");
        clustepDiag.setVisible(false);

        tightBindingDiag = new TightBindingDiag(this, false, clustepDiag);
        tightBindingDiag.setTitle("..:: Tight-Binding ::..");
        tightBindingDiag.setVisible(false);

        // Pour activer ou désactiver le menu Clustep & Tight-Binding
        jMenuClustepAndTB.setVisible(true);
        // ---------------------------------------------------------------------

        loadConfig("config.xml");

        outDialog.setLocationRelativeTo(this);
        outDialog.setVisible(true);

        // TODO rendre visible
        //mainTabbedPane.setEnabledAt(4, false);

        autoTest = false;

        // ---------------------------------------------------------------------

        /**
         * Script section *
         */

        argsModel = new ScriptTableModel(scriptArgTable);
        scriptArgTable.setModel(argsModel);
        initTableHeader(scriptArgTable, new String[]{"Name", "Value"},
                new Integer[]{null, null});
        scriptArgTable.setDefaultRenderer(ScriptArgument.class,
                new ScriptArgumentRenderer());


        outModel = new ScriptTableModel(scriptOutTable);
        scriptOutTable.setModel(outModel);
        initTableHeader(scriptOutTable, new String[]{"Name", "Value"},
                new Integer[]{null, null});
        scriptOutTable.setDefaultRenderer(ScriptArgument.class,
                new ScriptArgumentRenderer());

        scriptBibs = new ScriptBib();

        scriptBibs.loadScriptsFromFile("listScripts.xml");

        scriptModel = new DefaultListModel();
        scriptList.setModel(scriptModel);

        showScripts();

        abinitPathButton.setVisible(false);

        /**
         * End of script section *
         */
        projectManager = new ProjectManager(this);
        projectManager.setVisible(false);
        
        // Default values
        RemoteJob remoteJob = new RemoteJob();
        // Default values
        SubmissionScript script = new SGEScript(this);
        remoteJob.setScript(script);
        simulation = new Simulation();
        simulation.setRemoteJob(remoteJob);

        guiEditor = new GUIEditor(this);
        guiEditorNew = new GUIEditorNew(this);
        allInputVars = new AllInputVars(this);
        abinitInputVars = new AbinitInputVars(this, allInputVars);

        inputEditor = new InputEditor(this);
        submitScriptFrame = new SubmissionScriptFrame(this);

        testAnalyze.setVisible(false);
        editPYDFT.setVisible(false);
        jMenuClustepAndTB.setVisible(false);
        localAbinitRadioButton.setVisible(true);
        abipyPathPathLabel.setVisible(false);
        abipyPathTextField.setVisible(false);

        useExtIFRadioButtonActionPerformed(null);

    }

    public String getNtypat() {
        return geomD.getNtypat();
    }

    private void initTableHeader(JTable table, String header[], Integer headerWidths[]) {
        TableColumnModel tcm = new DefaultTableColumnModel();
        for (int i = 0; i < header.length; i++) {
            TableColumn tc = new TableColumn(i);
            tc.setHeaderValue(header[i]);
            tc.setResizable(false);
            if (headerWidths[i] != null) {
                tc.setMinWidth(headerWidths[i]);
                tc.setPreferredWidth(headerWidths[i]);
                tc.setMaxWidth(headerWidths[i]);
            }
            tcm.addColumn(tc);
        }
        table.setColumnModel(tcm);
    }

    public void printERR(String s) {
        if (s.endsWith("\n")) {
            outDialog.appendERR(s);
            pw.print("ERR>> " + s);
        } else {
            outDialog.appendERR(s + "\n");
            pw.print("ERR>> " + s + "\n");
            System.err.println("ERR>> " + s);
        }
        pw.flush();
    }

    public void printOUT(String s) {
        if (s.endsWith("\n")) {
            outDialog.appendOUT(s);
            pw.print("OUT>> " + s);
        } else {
            outDialog.appendOUT(s + "\n");
            pw.print("OUT>> " + s + "\n");
            System.out.println("OUT>> " + s);
        }
        pw.flush();
    }

    public String removeEndl(String str) {
        if (str.endsWith("\n")) {
            return (String) str.subSequence(0, str.lastIndexOf('\n'));
        } else {
            return str;
        }
    }

    public void printDEB(String str) {
        if (str.endsWith("\n")) {
            outDialog.appendDEB("DEB: " + str);
            pw.print("DEB>> " + str);
        } else {
            outDialog.appendDEB("DEB: " + str + "\n");
            pw.print("DEB>> " + str + "\n");
            System.out.println("DEB>> " + str);
        }
        pw.flush();
    }

    public void printGEN(String str, Color color, boolean underline, boolean bolt) {
        if (str.endsWith("\n")) {
            outDialog.appendGEN(str, color, underline, bolt);
            pw.print("GEN>> " + str);
        } else {
            outDialog.appendGEN(str + "\n", color, underline, bolt);
            pw.print("GEN>> " + str + "\n");
            System.out.println("GEN>> " + str);
        }
        pw.flush();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        whereIsAbinitbuttonGroup = new javax.swing.ButtonGroup();
        inputFilebuttonGroup = new javax.swing.ButtonGroup();
        lookAndFeelbuttonGroup = new javax.swing.ButtonGroup();
        abinixbuttonGroup = new javax.swing.ButtonGroup();
        mainTabbedPane = new javax.swing.JTabbedPane();
        configPanel = new javax.swing.JPanel();
        localAbinitRadioButton = new javax.swing.JRadioButton();
        remoteAbinitRadioButton = new javax.swing.JRadioButton();
        whereIsAbinitLabel = new javax.swing.JLabel();
        remoteGatewayRadioButton = new javax.swing.JRadioButton();
        loginPanel = new javax.swing.JPanel();
        hostTextField = new javax.swing.JTextField();
        hostLabel = new javax.swing.JLabel();
        loginLabel = new javax.swing.JLabel();
        loginTextField = new javax.swing.JTextField();
        pwdPasswordField = new javax.swing.JPasswordField();
        pwdLabel = new javax.swing.JLabel();
        jCB_useKey1 = new javax.swing.JCheckBox();
        jTF_key1 = new javax.swing.JTextField();
        gatewayLoginPanel = new javax.swing.JPanel();
        gatewayHostTextField = new javax.swing.JTextField();
        hostBFELabel = new javax.swing.JLabel();
        loginBFELabel = new javax.swing.JLabel();
        gatewayLoginTextField = new javax.swing.JTextField();
        gatewayPasswordField = new javax.swing.JPasswordField();
        pwdBFELabel = new javax.swing.JLabel();
        jCB_useKey2 = new javax.swing.JCheckBox();
        jTF_key2 = new javax.swing.JTextField();
        mySimulationsTextField = new javax.swing.JTextField();
        mySimulationsLabel = new javax.swing.JLabel();
        pspPathTextField = new javax.swing.JTextField();
        pspPathLabel = new javax.swing.JLabel();
        abinitPathTextField = new javax.swing.JTextField();
        abinitPathPathLabel = new javax.swing.JLabel();
        abinitPathButton = new javax.swing.JButton();
        abipyPathPathLabel = new javax.swing.JLabel();
        abipyPathTextField = new javax.swing.JTextField();
        inputFilePanel = new javax.swing.JPanel();
        useExtIFRadioButton = new javax.swing.JRadioButton();
        openFileTextField = new javax.swing.JTextField();
        openFileDialogButton = new javax.swing.JButton();
        openFileLabel = new javax.swing.JLabel();
        sendSIMButton = new javax.swing.JButton();
        pspTextField = new javax.swing.JTextField();
        pspTableScrollPane = new javax.swing.JScrollPane();
        pspTable = new javax.swing.JTable();
        pspLabel = new javax.swing.JLabel();
        displayFileButton = new javax.swing.JButton();
        geditButton = new javax.swing.JButton();
        SubmissionEditButton1 = new javax.swing.JButton();
        testAnalyze = new javax.swing.JButton();
        useCreIFRadioButton = new javax.swing.JRadioButton();
        openXMLFileLabel = new javax.swing.JLabel();
        openXMLFileTextField = new javax.swing.JTextField();
        openXMLFileDialogButton = new javax.swing.JButton();
        saveFileButton = new javax.swing.JButton();
        saveFileAsButton = new javax.swing.JButton();
        createButton = new javax.swing.JButton();
        inputFileTabbedPane = new javax.swing.JTabbedPane();
        basicsScrollPane = new javax.swing.JScrollPane();
        basicsPanel = new javax.swing.JPanel();
        geometryButton = new javax.swing.JButton();
        algoAndConvButton = new javax.swing.JButton();
        realAndRecipButton = new javax.swing.JButton();
        wavefuncAndDensButton = new javax.swing.JButton();
        inputOutputButton = new javax.swing.JButton();
        theoryButton = new javax.swing.JButton();
        editPYDFT = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        otherTextArea = new javax.swing.JTextArea();
        emptyPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        testAnalyze1 = new javax.swing.JButton();
        postProcPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        scriptScrollPane = new javax.swing.JScrollPane();
        scriptList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        scriptName = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        scriptDescription = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        scriptArgTable = new ScriptTable();
        launchScript = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        scriptOutTable = new ScriptTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        scriptProgram = new javax.swing.JTextField();
        openOutput = new javax.swing.JButton();
        editScripts = new javax.swing.JButton();
        reloadScripts = new javax.swing.JButton();
        connectionToggleButton = new javax.swing.JToggleButton();
        SSH2ClientButton = new javax.swing.JButton();
        SFTPButton = new javax.swing.JButton();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        LoadMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        outputMSGMenuItem = new javax.swing.JMenuItem();
        clearOutMSGMenuItem = new javax.swing.JMenuItem();
        postProcMenu = new javax.swing.JMenu();
        getOutputFileMenuItem = new javax.swing.JMenuItem();
        getLogFileMenuItem = new javax.swing.JMenuItem();
        jMenuClustepAndTB = new javax.swing.JMenu();
        jMenuItemClustep = new javax.swing.JMenuItem();
        jMenuItemTB = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AbinitGUI");
        setBackground(new java.awt.Color(245, 242, 239));
        setResizable(false);

        mainTabbedPane.setMaximumSize(new java.awt.Dimension(800, 650));
        mainTabbedPane.setMinimumSize(new java.awt.Dimension(800, 650));
        mainTabbedPane.setPreferredSize(new java.awt.Dimension(800, 650));

        configPanel.setMaximumSize(null);

        whereIsAbinitbuttonGroup.add(localAbinitRadioButton);
        localAbinitRadioButton.setForeground(java.awt.Color.blue);
        localAbinitRadioButton.setText("Local (only for Linux hosts)");
        localAbinitRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localAbinitRadioButtonActionPerformed(evt);
            }
        });

        whereIsAbinitbuttonGroup.add(remoteAbinitRadioButton);
        remoteAbinitRadioButton.setForeground(java.awt.Color.blue);
        remoteAbinitRadioButton.setText("Remote");
        remoteAbinitRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remoteAbinitRadioButtonActionPerformed(evt);
            }
        });

        whereIsAbinitLabel.setForeground(java.awt.Color.red);
        whereIsAbinitLabel.setText("ABINIT host location ?");

        whereIsAbinitbuttonGroup.add(remoteGatewayRadioButton);
        remoteGatewayRadioButton.setForeground(java.awt.Color.red);
        remoteGatewayRadioButton.setSelected(true);
        remoteGatewayRadioButton.setText("Remote (behind a gateway)");
        remoteGatewayRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remoteGatewayRadioButtonActionPerformed(evt);
            }
        });

        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Remote Abinithost login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 3, 14), java.awt.Color.darkGray)); // NOI18N

        hostLabel.setText("Hostname or IP");

        loginLabel.setText("Login");

        pwdLabel.setText("Password");

        jCB_useKey1.setText("use key");
        jCB_useKey1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCB_useKey1ActionPerformed(evt);
            }
        });

        jTF_key1.setEnabled(false);

        org.jdesktop.layout.GroupLayout loginPanelLayout = new org.jdesktop.layout.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(loginPanelLayout.createSequentialGroup()
                        .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(hostLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(hostTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(loginTextField)
                            .add(loginLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(pwdLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(pwdPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(loginPanelLayout.createSequentialGroup()
                        .add(jCB_useKey1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTF_key1)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(hostLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(loginLabel))
                    .add(pwdLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(loginTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pwdPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCB_useKey1)
                    .add(jTF_key1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gatewayLoginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Gateway login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 3, 14), java.awt.Color.darkGray)); // NOI18N

        hostBFELabel.setText("Hostname or IP");

        loginBFELabel.setText("Login");

        pwdBFELabel.setText("Password");

        jCB_useKey2.setText("use key");
        jCB_useKey2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCB_useKey2ActionPerformed(evt);
            }
        });

        jTF_key2.setEnabled(false);

        org.jdesktop.layout.GroupLayout gatewayLoginPanelLayout = new org.jdesktop.layout.GroupLayout(gatewayLoginPanel);
        gatewayLoginPanel.setLayout(gatewayLoginPanelLayout);
        gatewayLoginPanelLayout.setHorizontalGroup(
            gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gatewayLoginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(gatewayLoginPanelLayout.createSequentialGroup()
                        .add(jCB_useKey2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTF_key2))
                    .add(gatewayLoginPanelLayout.createSequentialGroup()
                        .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(hostBFELabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(gatewayHostTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(gatewayLoginTextField)
                            .add(loginBFELabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(pwdBFELabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(gatewayPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        gatewayLoginPanelLayout.setVerticalGroup(
            gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gatewayLoginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(hostBFELabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(loginBFELabel))
                    .add(pwdBFELabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(gatewayHostTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(gatewayLoginTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(gatewayPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gatewayLoginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCB_useKey2)
                    .add(jTF_key2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mySimulationsLabel.setLabelFor(mySimulationsTextField);
        mySimulationsLabel.setText("Path where to create the simulations filetree");
        mySimulationsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mySimulationsLabelMouseClicked(evt);
            }
        });

        pspPathLabel.setLabelFor(pspPathTextField);
        pspPathLabel.setText("Local path to pseudopotentials");
        pspPathLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pspPathLabelMouseClicked(evt);
            }
        });

        abinitPathPathLabel.setLabelFor(pspPathTextField);
        abinitPathPathLabel.setText("Path to the abinit program (At abinit server !)");
        abinitPathPathLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                abinitPathPathLabelMouseClicked(evt);
            }
        });

        abinitPathButton.setText("Find path");
        abinitPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abinitPathButtonActionPerformed(evt);
            }
        });

        abipyPathPathLabel.setLabelFor(pspPathTextField);
        abipyPathPathLabel.setText("Path to abipy (At abinit server !)");
        abipyPathPathLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                abipyPathPathLabelMouseClicked(evt);
            }
        });

        abipyPathTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abipyPathTextFieldActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout configPanelLayout = new org.jdesktop.layout.GroupLayout(configPanel);
        configPanel.setLayout(configPanelLayout);
        configPanelLayout.setHorizontalGroup(
            configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(abipyPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 508, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(whereIsAbinitLabel)
                    .add(configPanelLayout.createSequentialGroup()
                        .add(localAbinitRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(remoteAbinitRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(remoteGatewayRadioButton))
                    .add(configPanelLayout.createSequentialGroup()
                        .add(loginPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(gatewayLoginPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mySimulationsLabel)
                    .add(pspPathLabel)
                    .add(abinitPathPathLabel)
                    .add(configPanelLayout.createSequentialGroup()
                        .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(abinitPathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                            .add(mySimulationsTextField)
                            .add(pspPathTextField))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(abinitPathButton))
                    .add(abipyPathPathLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        configPanelLayout.setVerticalGroup(
            configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(whereIsAbinitLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(remoteGatewayRadioButton)
                    .add(localAbinitRadioButton)
                    .add(remoteAbinitRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(configPanelLayout.createSequentialGroup()
                        .add(loginPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(mySimulationsLabel))
                    .add(gatewayLoginPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mySimulationsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pspPathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pspPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(abinitPathPathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(abinitPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(abinitPathButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(abipyPathPathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(abipyPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainTabbedPane.addTab("Configuration", configPanel);

        inputFilePanel.setMaximumSize(new java.awt.Dimension(800, 600));
        inputFilePanel.setMinimumSize(new java.awt.Dimension(800, 600));
        inputFilePanel.setPreferredSize(new java.awt.Dimension(800, 600));

        inputFilebuttonGroup.add(useExtIFRadioButton);
        useExtIFRadioButton.setForeground(java.awt.Color.red);
        useExtIFRadioButton.setSelected(true);
        useExtIFRadioButton.setText("Use an external input file");
        useExtIFRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useExtIFRadioButtonActionPerformed(evt);
            }
        });

        openFileDialogButton.setText("...");
        openFileDialogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileDialogButtonActionPerformed(evt);
            }
        });

        openFileLabel.setText("Open the ABINIT input file (usualy *.in)");

        sendSIMButton.setText("<HTML> <center> <b>Send the simulation</b><br> the simulation will start at server side </HTML>");
        sendSIMButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendSIMButtonActionPerformed(evt);
            }
        });

        pspTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pspTextFieldActionPerformed(evt);
            }
        });
        pspTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pspTextFieldKeyReleased(evt);
            }
        });

        pspTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        pspTable.setRowSelectionAllowed(false);
        pspTable.getTableHeader().setReorderingAllowed(false);
        pspTableScrollPane.setViewportView(pspTable);

        pspLabel.setText("Number of different psps (=ntypat)");

        displayFileButton.setText("Display");
        displayFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayFileButtonActionPerformed(evt);
            }
        });

        geditButton.setText("Edit");
        geditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geditButtonActionPerformed(evt);
            }
        });

        SubmissionEditButton1.setText("Edit submission details");
        SubmissionEditButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SubmissionEditButton1ActionPerformed(evt);
            }
        });

        testAnalyze.setText("Test / Analyze file");
        testAnalyze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testAnalyzeActionPerformed(evt);
            }
        });

        inputFilebuttonGroup.add(useCreIFRadioButton);
        useCreIFRadioButton.setForeground(java.awt.Color.blue);
        useCreIFRadioButton.setText("Use created input file");
        useCreIFRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCreIFRadioButtonActionPerformed(evt);
            }
        });

        openXMLFileLabel.setText("Open the ABINIT input file (usualy *.ab [XML file])");
        openXMLFileLabel.setEnabled(false);

        openXMLFileTextField.setEnabled(false);

        openXMLFileDialogButton.setText("...");
        openXMLFileDialogButton.setEnabled(false);
        openXMLFileDialogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openXMLFileDialogButtonActionPerformed(evt);
            }
        });

        saveFileButton.setText("Save");
        saveFileButton.setEnabled(false);
        saveFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileButtonActionPerformed(evt);
            }
        });

        saveFileAsButton.setText("Save As");
        saveFileAsButton.setEnabled(false);
        saveFileAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileAsButtonActionPerformed(evt);
            }
        });

        createButton.setText("Generate file preview (test)");
        createButton.setEnabled(false);
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        inputFileTabbedPane.setEnabled(false);
        inputFileTabbedPane.setMaximumSize(new java.awt.Dimension(380, 550));
        inputFileTabbedPane.setMinimumSize(new java.awt.Dimension(380, 550));

        basicsScrollPane.setEnabled(false);
        basicsScrollPane.setMaximumSize(new java.awt.Dimension(352, 600));
        basicsScrollPane.setMinimumSize(new java.awt.Dimension(352, 600));
        basicsScrollPane.setPreferredSize(new java.awt.Dimension(352, 600));

        basicsPanel.setEnabled(false);
        basicsPanel.setMaximumSize(new java.awt.Dimension(241, 267));
        basicsPanel.setMinimumSize(new java.awt.Dimension(241, 267));

        geometryButton.setText("Geometry");
        geometryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geometryButtonActionPerformed(evt);
            }
        });

        algoAndConvButton.setText("Algorithm & convergence");
        algoAndConvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoAndConvButtonActionPerformed(evt);
            }
        });

        realAndRecipButton.setText("Real & reciprocal space");
        realAndRecipButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realAndRecipButtonActionPerformed(evt);
            }
        });

        wavefuncAndDensButton.setText("Wavefunctions & densities");
        wavefuncAndDensButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wavefuncAndDensButtonActionPerformed(evt);
            }
        });

        inputOutputButton.setText("Input / Output");
        inputOutputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputOutputButtonActionPerformed(evt);
            }
        });

        theoryButton.setText("Theory (DFT)");
        theoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theoryButtonActionPerformed(evt);
            }
        });

        editPYDFT.setText("Edit input file");
        editPYDFT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPYDFTActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout basicsPanelLayout = new org.jdesktop.layout.GroupLayout(basicsPanel);
        basicsPanel.setLayout(basicsPanelLayout);
        basicsPanelLayout.setHorizontalGroup(
            basicsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(basicsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(basicsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(geometryButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(algoAndConvButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(realAndRecipButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(wavefuncAndDensButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .add(inputOutputButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(theoryButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .add(basicsPanelLayout.createSequentialGroup()
                .add(81, 81, 81)
                .add(editPYDFT, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        basicsPanelLayout.setVerticalGroup(
            basicsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(basicsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(geometryButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(algoAndConvButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(realAndRecipButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(wavefuncAndDensButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(inputOutputButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(theoryButton)
                .add(60, 60, 60)
                .add(editPYDFT)
                .addContainerGap())
        );

        basicsScrollPane.setViewportView(basicsPanel);

        inputFileTabbedPane.addTab("Basics", basicsScrollPane);

        otherTextArea.setColumns(20);
        otherTextArea.setRows(5);
        jScrollPane5.setViewportView(otherTextArea);

        inputFileTabbedPane.addTab("For other variables", jScrollPane5);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("<HTML> <center> Select the <b>Use created input file</b><br>radiobutton to create graphicaly an input<br>file to send to an ABINIT host.  </HTML>");

        org.jdesktop.layout.GroupLayout emptyPanelLayout = new org.jdesktop.layout.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(emptyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addContainerGap())
        );
        emptyPanelLayout.setVerticalGroup(
            emptyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(emptyPanelLayout.createSequentialGroup()
                .add(133, 133, 133)
                .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(189, Short.MAX_VALUE))
        );

        inputFileTabbedPane.addTab("", emptyPanel);

        testAnalyze1.setText("Test / Analyze file (NEW)");
        testAnalyze1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testAnalyze1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout inputFilePanelLayout = new org.jdesktop.layout.GroupLayout(inputFilePanel);
        inputFilePanel.setLayout(inputFilePanelLayout);
        inputFilePanelLayout.setHorizontalGroup(
            inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, inputFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(testAnalyze1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, testAnalyze, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, SubmissionEditButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pspTableScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, sendSIMButton)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, inputFilePanelLayout.createSequentialGroup()
                        .add(openXMLFileDialogButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(openXMLFileTextField))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, inputFilePanelLayout.createSequentialGroup()
                        .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, openXMLFileLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, useCreIFRadioButton)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, inputFilePanelLayout.createSequentialGroup()
                                .add(saveFileButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(saveFileAsButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createButton))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, inputFilePanelLayout.createSequentialGroup()
                                .add(pspLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(pspTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, useExtIFRadioButton)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, inputFilePanelLayout.createSequentialGroup()
                                .add(openFileLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(displayFileButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(geditButton)))
                        .add(0, 0, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, inputFilePanelLayout.createSequentialGroup()
                        .add(openFileDialogButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(openFileTextField)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(inputFileTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 334, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        inputFilePanelLayout.setVerticalGroup(
            inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(inputFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(inputFileTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 431, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(inputFilePanelLayout.createSequentialGroup()
                        .add(useExtIFRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(openFileLabel)
                            .add(displayFileButton)
                            .add(geditButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(openFileDialogButton)
                            .add(openFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pspLabel)
                            .add(pspTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pspTableScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(useCreIFRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(openXMLFileLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(openXMLFileDialogButton)
                            .add(openXMLFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inputFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(saveFileButton)
                            .add(saveFileAsButton)
                            .add(createButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(SubmissionEditButton1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sendSIMButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testAnalyze)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testAnalyze1)
                .addContainerGap())
        );

        inputFilePanelLayout.linkSize(new java.awt.Component[] {openFileDialogButton, openFileTextField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        mainTabbedPane.addTab("Input File", inputFilePanel);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scripts Library", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 3, 14), java.awt.Color.darkGray)); // NOI18N
        jPanel1.setToolTipText("");

        scriptList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        scriptList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                scriptListValueChanged(evt);
            }
        });
        scriptScrollPane.setViewportView(scriptList);

        jLabel1.setText("Name of the script :");

        scriptName.setText("Name");

        jLabel3.setText("Description : ");

        scriptDescription.setEditable(false);
        scriptDescription.setColumns(20);
        scriptDescription.setLineWrap(true);
        scriptDescription.setRows(5);
        scriptDescription.setText("Description of the script");
        scriptDescription.setWrapStyleWord(true);
        jScrollPane1.setViewportView(scriptDescription);

        jLabel5.setText("Arguments :");

        scriptArgTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(scriptArgTable);

        launchScript.setText("Launch script !");
        launchScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchScriptActionPerformed(evt);
            }
        });

        scriptOutTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(scriptOutTable);

        jLabel6.setText("Output files :");

        jLabel9.setText("Program :");

        scriptProgram.setEditable(false);
        scriptProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scriptProgramActionPerformed(evt);
            }
        });

        openOutput.setText("Open output files !");
        openOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openOutputActionPerformed(evt);
            }
        });

        editScripts.setText("Edit script");
        editScripts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editScriptsActionPerformed(evt);
            }
        });

        reloadScripts.setText("Reload scripts");
        reloadScripts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadScriptsActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(scriptScrollPane)
                    .add(editScripts, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, reloadScripts, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jScrollPane1))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(18, 18, 18)
                                .add(scriptName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 287, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE)))
                        .add(12, 12, 12))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(jLabel9)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(scriptProgram, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 211, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(jLabel6)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(launchScript, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 191, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(openOutput)))
                                .add(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(scriptName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(4, 4, 4)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel9)
                            .add(scriptProgram, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 194, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(scriptScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 366, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(editScripts)
                        .add(3, 3, 3)
                        .add(reloadScripts)))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(launchScript)
                    .add(openOutput))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout postProcPanelLayout = new org.jdesktop.layout.GroupLayout(postProcPanel);
        postProcPanel.setLayout(postProcPanelLayout);
        postProcPanelLayout.setHorizontalGroup(
            postProcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(postProcPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        postProcPanelLayout.setVerticalGroup(
            postProcPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(postProcPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainTabbedPane.addTab("Post-processing", postProcPanel);

        connectionToggleButton.setText("Connect");
        connectionToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionToggleButtonActionPerformed(evt);
            }
        });

        SSH2ClientButton.setText("SSH2 Client");
        SSH2ClientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SSH2ClientButtonActionPerformed(evt);
            }
        });

        SFTPButton.setText("SFTP Client");
        SFTPButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SFTPButtonActionPerformed(evt);
            }
        });

        fileMenu.setLabel("File");

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("Save config.");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setText("Save config. as...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        LoadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        LoadMenuItem.setText("Load config.");
        LoadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(LoadMenuItem);

        mainMenuBar.add(fileMenu);

        viewMenu.setText("View");

        outputMSGMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        outputMSGMenuItem.setText("Display MSG panel");
        outputMSGMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputMSGMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(outputMSGMenuItem);

        clearOutMSGMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        clearOutMSGMenuItem.setText("Clear MSG panel");
        clearOutMSGMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearOutMSGMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(clearOutMSGMenuItem);

        mainMenuBar.add(viewMenu);

        postProcMenu.setText("PostProc");

        getOutputFileMenuItem.setText("Download output file");
        getOutputFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getOutputFileMenuItemActionPerformed(evt);
            }
        });
        postProcMenu.add(getOutputFileMenuItem);

        getLogFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.ALT_MASK));
        getLogFileMenuItem.setText("Download log file");
        getLogFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getLogFileMenuItemActionPerformed(evt);
            }
        });
        postProcMenu.add(getLogFileMenuItem);

        mainMenuBar.add(postProcMenu);

        jMenuClustepAndTB.setText("Clustep & TB");

        jMenuItemClustep.setText("Clustep");
        jMenuItemClustep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClustepActionPerformed(evt);
            }
        });
        jMenuClustepAndTB.add(jMenuItemClustep);

        jMenuItemTB.setText("Tight-Binding");
        jMenuItemTB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTBActionPerformed(evt);
            }
        });
        jMenuClustepAndTB.add(jMenuItemTB);

        mainMenuBar.add(jMenuClustepAndTB);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem1.setText("Input Variables");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        helpMenu.add(jMenuItem1);

        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK));
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 754, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(connectionToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(SSH2ClientButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(SFTPButton)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(mainTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 610, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionToggleButton)
                    .add(SSH2ClientButton)
                    .add(SFTPButton))
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void stopConnection() {
        if (remoteGatewayRadioButton.isSelected()) {
            if (remoteExec != null) {
                remoteExec.stop();
                remoteExec = null;
            }
            if (sshtun != null) {
                sshtun.stop();
                sshtun = null;
            }
            lport = 0;
        } else if (remoteAbinitRadioButton.isSelected()) {
            if (remoteExec != null) {
                remoteExec.stop();
                remoteExec = null;
            }
        } else if (localAbinitRadioButton.isSelected()) {
            // Pas besoin en local
        } else { // Le choix n'a pas été fait
            printERR("Choose a destination option please at config. tab !");
        }
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        about.setLocationRelativeTo(this);
        about.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        saveConfig(null);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void LoadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadMenuItemActionPerformed
        JFileChooser fc = new JFileChooser(".");
        fc.setMultiSelectionEnabled(false);
        int retValue = fc.showOpenDialog(this);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            loadConfig(file.getPath());
        }
    }//GEN-LAST:event_LoadMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        JFileChooser fc = new JFileChooser(".");
        fc.setMultiSelectionEnabled(false);
        int retValue = fc.showSaveDialog(this);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            saveConfig(file.getPath());
        }
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void outputMSGMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputMSGMenuItemActionPerformed
        outDialog.setLocationRelativeTo(this);
        outDialog.setVisible(true);
    }//GEN-LAST:event_outputMSGMenuItemActionPerformed

    private void clearOutMSGMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearOutMSGMenuItemActionPerformed
        outDialog.clear();
    }//GEN-LAST:event_clearOutMSGMenuItemActionPerformed

    private void getOutputFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getOutputFileMenuItemActionPerformed
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if ((remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) && remoteExec == null) {
                    printERR("Please connect to a ABINIT host before downloading anything!");
                    sendSIMButton.setEnabled(true);
                    return;
                }

                String rootPath = mySimulationsTextField.getText();
                String outputFolder = "output";

                String inputFile = "";
                String inputFileName = "";

                if (useExtIFRadioButton.isSelected()) {
                    inputFile = openFileTextField.getText();
                    inputFileName = Utils.getLastToken(inputFile.replace('\\', '/'), "/");
                } else {
                    printERR("Choose an option please ! (use an external inputfile or created a inputfile)");
                    sendSIMButton.setEnabled(true);
                    return;
                }

                // Test de l'existance de inputfile
                if (!Utils.exists(inputFile)) {
                    printERR("The file " + inputFile + " doesn't exist !");
                    sendSIMButton.setEnabled(true);
                    return;
                }

                String simName = null;
                if (inputFileName != null) {
                    if (!inputFileName.equals("")) {
                        int idx = inputFileName.indexOf('.');
                        if (idx > 0 && idx < inputFileName.length()) {
                            simName = inputFileName.substring(0, idx);
                        } else {
                            simName = inputFileName;
                        }
                    } else {
                        printERR("inputFileName == \"\"");
                        return;
                    }
                } else {
                    printERR("inputFileName == null");
                    return;
                }

                if (!inputFile.equals("")) {

                    String outputPath = rootPath + "/" + outputFolder;
                    String fileName = outputPath + "/" + simName + ".out";
                    System.out.println(fileName);
                    // Réception (copie) du fichier d'output si celui-ci est distant
                    if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
                        String file = "";
                        String outputFiles = getOutputFilesR(fileName + "*");
                        StringTokenizer st = new StringTokenizer(outputFiles, "\n");
                        while (st.hasMoreElements()) {
                            file = st.nextToken();
                            printOUT("File = " + file);
                            //if (Utils.osName().startsWith("Windows")) {
                            //    sendCommand("unix2dos " + file);
                            //}
                            getFile(file + " " + file);
                            if (Utils.osName().startsWith("Windows")) {
                                //sendCommand("dos2unix " + file);
                                Utils.unix2dos(new File(file));
                            }
                        }
                        fileName = file; // Prend le nom du dernier fichier!
                    }

                    // ****************************************************************************
                    // Tester l'existence du fichier
                    editFile(fileName, false);
                    // ****************************************************************************
                } else {
                    printERR("Please setup the inputfile textfield !");
                    return;
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }//GEN-LAST:event_getOutputFileMenuItemActionPerformed

    private void getLogFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getLogFileMenuItemActionPerformed
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if ((remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) && remoteExec == null) {
                    printERR("Please connect to a ABINIT host before downloading anything!");
                    sendSIMButton.setEnabled(true);
                    return;
                }

                String rootPath = mySimulationsTextField.getText();
                //String inputFolder = "input";
                //String outputFolder = "output";
                //String wholedataFolder = "wholedata";
                //String pseudopotFolder = "pseudopot";
                String logfilesFolder = "logfiles";

                String inputFile = "";
                String inputFileName = "";

                if (useExtIFRadioButton.isSelected()) {
                    inputFile = openFileTextField.getText();
                    inputFileName = Utils.getLastToken(inputFile.replace('\\', '/'), "/");
                } else {
                    printERR("Choose an option please ! (use an external inputfile or created a inputfile)");
                    sendSIMButton.setEnabled(true);
                    return;
                }

                // Test de l'existance de inputfile
                if (!Utils.exists(inputFile)) {
                    printERR("The file " + inputFile + " doesn't exist !");
                    sendSIMButton.setEnabled(true);
                    return;
                }

                String simName = null;
                if (inputFileName != null) {
                    if (!inputFileName.equals("")) {
                        int idx = inputFileName.indexOf('.');
                        if (idx > 0 && idx < inputFileName.length()) {
                            simName = inputFileName.substring(0, idx);
                        } else {
                            simName = inputFileName;
                        }
                    } else {
                        printERR("inputFileName == \"\"");
                        return;
                    }
                } else {
                    printERR("inputFileName == null");
                    return;
                }

                if (!inputFile.equals("")) {
                    String fileName = rootPath + "/" + logfilesFolder + "/" + simName + ".log";

                    // Réception (copie) du fichier *.log si celui-ci est distant
                    if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
                        //if (Utils.osName().startsWith("Windows")) {
                        //    sendCommand("unix2dos " + fileName);
                        //}
                        getFile(fileName + " " + fileName);
                        if (Utils.osName().startsWith("Windows")) {
                            //sendCommand("dos2unix " + fileName);
                            Utils.unix2dos(new File(fileName));
                        }
                    }

                    editFile(fileName, false);
                    // ****************************************************************************
                } else {
                    printERR("Please setup the inputfile textfield !");
                    return;
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }//GEN-LAST:event_getLogFileMenuItemActionPerformed

    private void geditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geditButtonActionPerformed
        editFile(openFileTextField.getText(), true);
}//GEN-LAST:event_geditButtonActionPerformed

    private void displayFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayFileButtonActionPerformed
        inputFileDisplayer.setVisible(true);
        // TODO : pour quand ce sera éditable
        //inputFileDisplayer.setEditable(true);

        String fileContent = "";

        try {
            File file = new File(openFileTextField.getText());

            /*FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr);

             String thisLine;
             while ((thisLine = br.readLine()) != null) {
             fileContent += thisLine + "\n";
             }*/

            Scanner scanner = new Scanner(file).useDelimiter("\\Z");
            fileContent = scanner.next();
            scanner.close();

        } catch (FileNotFoundException e) {
            printERR(e.getMessage());
        } catch (IOException e) {
            printERR(e.getMessage());
        }

        inputFileDisplayer.setText(fileContent);
}//GEN-LAST:event_displayFileButtonActionPerformed

    private void pspTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pspTextFieldKeyReleased
        try {
            int npsp = Integer.parseInt(pspTextField.getText());

            if (npsp > 1000) {
                npsp = 1000;
                Object strTab[][] = new Object[npsp][4];
                for (int i = 0; i < npsp; i++) {
                    strTab[i] = new Object[]{new Atom(), "", "", ""};
                }
                pspModel.setData(strTab);
                //znuclTable.setModel(znuclModel);
            } else {
                Object strTab[][] = new Object[npsp][4];
                for (int i = 0; i < npsp; i++) {
                    strTab[i] = new Object[]{new Atom(), "", "", ""};
                }
                pspModel.setData(strTab);
                //znuclTable.setModel(znuclModel);
            }
        } catch (Exception e) {
            //printERR(e.getMessage());
            pspModel.setData(null);
            //znuclTable.setModel(znuclModel);
        }
}//GEN-LAST:event_pspTextFieldKeyReleased

    private void sendSIMButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendSIMButtonActionPerformed
            newSendSIMButtonActionPerformed(evt);
}//GEN-LAST:event_sendSIMButtonActionPerformed

    private void newSendSIMButtonActionPerformed(java.awt.event.ActionEvent evt) {
        sendSIMButton_setEnabled(false);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                boolean localAbinitRadioButton_isSelected = localAbinitRadioButton.isSelected();
                boolean remoteGatewayRadioButton_isSelected = remoteGatewayRadioButton.isSelected();
                boolean remoteAbinitRadioButton_isSelected = remoteAbinitRadioButton.isSelected();

                String rootPath = mySimulationsTextField.getText();

                String pathToAbinit = abinitPathTextField.getText();

                SubmissionScript script = simulation.getRemoteJob().getScript();


                boolean useExtIFRadioButton_isSelected = useExtIFRadioButton.isSelected();

                String inputFile;

                if (useExtIFRadioButton_isSelected) {
                    inputFile = openFileTextField.getText();
                } else {
                    printERR("Choose an option please ! (use an external"
                            + " inputfile or created a inputfile)");
                    sendSIMButton_setEnabled(true);
                    return;
                }

                // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                if (localAbinitRadioButton_isSelected && Utils.osName().startsWith("Windows")) {
                    printERR("Please connect to a remote host before submitting a simulation on Windows platform !");
                    sendSIMButton_setEnabled(true);
                    return;
                }

                if ((remoteGatewayRadioButton_isSelected
                        || remoteAbinitRadioButton_isSelected) && remoteExec == null) {
                    printERR("Please connect to a ABINIT host before submitting a simulation !");
                    sendSIMButton_setEnabled(true);
                    return;
                }

                createFiletree();

                String inputFolder = "input";
                String outputFolder = "output";
                String wholedataFolder = "wholedata";
                String pseudopotFolder = "pseudopot";
                String logfilesFolder = "logfiles";

                //--------------------------------------------------------------

                String hostTextField_getText = hostTextField.getText();
                String gatewayHostTextField_getText = gatewayHostTextField.getText();

                // *************************************************************

                String cwd = "";

                String CMD = "pwd";

                RetMSG retmsg;
                if (remoteGatewayRadioButton_isSelected || remoteAbinitRadioButton_isSelected) {
                    if (remoteExec != null) {
                        retmsg = remoteExec.sendCommand(CMD);
                        if (retmsg.getRetCode() == RetMSG.SUCCES) {
                            printOUT("PWD: " + retmsg.getRetMSG());
                            cwd = removeEndl(retmsg.getRetMSG());
                        } else {
                            //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                            printERR("Error: " + retmsg.getRetMSG() + " !");
                        }
                    } else {
                        printERR("First connect to an abinit host please !");
                    }
                } else if (localAbinitRadioButton_isSelected) {
                    if (localExec != null) {
                        retmsg = localExec.sendCommand(CMD);
                        if (retmsg.getRetCode() == RetMSG.SUCCES) {
                            printOUT("PWD: " + retmsg.getRetMSG());
                            cwd = removeEndl(retmsg.getRetMSG());
                        } else {
                            //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                            printERR("Error: " + retmsg.getRetMSG() + " !");
                        }
                    }
                } else { // Le choix n'a pas été fait
                    printERR("Choose a destination option please at config. tab !");
                }

                // ********************************************************************************************************************************

                String inputFileName = "";

                String sep = Utils.fileSeparator();
                inputFileName = Utils.getLastToken(inputFile.replace('\\', '/'), "/");


                // Test de l'existance de inputfile
                if (!Utils.exists(inputFile)) {
                    printERR("The file " + inputFile + " doesn't exist !");
                    sendSIMButton_setEnabled(true);
                    return;
                }

                String simName = null;
                if (inputFileName != null) {
                    int idx = inputFileName.indexOf('.');
                    if (idx > 0 && idx < inputFileName.length()) {
                        simName = inputFileName.substring(0, idx);
                    } else {
                        simName = inputFileName;
                    }
                }

                if (!inputFile.equals("")) {
                    script.setAbinitPath(pathToAbinit + ParaAbinit);
                    script.setInputPath(cwd + "/" + rootPath.replaceFirst("./", "") + "/" + simName + ".files");
                    script.setLogPath(cwd + "/"
                            + rootPath.replaceFirst("./", "") + "/" + logfilesFolder + "/" + simName + ".log");

                    if (script.getSystem().equals("SGE")) {
                        String PBSfileName = rootPath + sep + simName + ".SGE.sh";
                        script.writeToFile(PBSfileName);
                    } else if (script.getSystem().equals("SLURM")) {
                        String PBSfileName = rootPath + sep + simName + ".SLURM.sh";
                        script.writeToFile(PBSfileName);
                    } else if (script.getSystem().equals("Frontend")) {
                        String SHfileName = rootPath + sep + simName + ".sh";
                        script.writeToFile(SHfileName);
                    }

                    // Envoie (copie) du fichier d'input
                    String inputFileR = rootPath + "/" + inputFolder + "/" + inputFileName;
                    putFile(inputFile + " " + inputFileR);

                    if (remoteGatewayRadioButton_isSelected
                            || remoteAbinitRadioButton_isSelected) {
                        if (Utils.osName().startsWith("Windows")) {
                            sendCommand("dos2unix " + inputFileR);
                            // TODO Util.dos2unix(new File(inputFileR)); // Transformer avant d'envoyer le fichier
                        }
                    }

                    // Création du contenu du fichier de configuration (*.files)
                    String configFileContent = "";
                    configFileContent += cwd + "/" + rootPath.replaceFirst("./", "")
                            + "/" + inputFolder + "/" + inputFileName + "\n";
                    configFileContent += cwd + "/" + rootPath.replaceFirst("./", "")
                            + "/" + outputFolder + "/" + simName + ".out\n";
                    configFileContent += cwd + "/" + rootPath.replaceFirst("./", "")
                            + "/" + wholedataFolder + "/" + simName + "/" + simName + "i\n";
                    configFileContent += cwd + "/" + rootPath.replaceFirst("./", "")
                            + "/" + wholedataFolder + "/" + simName + "/" + simName + "o\n";
                    configFileContent += cwd + "/" + rootPath.replaceFirst("./", "")
                            + "/" + wholedataFolder + "/" + simName + "/" + simName + "\n";

                    if (useExtIFRadioButton_isSelected) {
                        int row = pspTable.getRowCount();
                        if (row > 0) {
                            for (int i = 0; i < row; i++) {
                                try {
                                    Atom at = (Atom) pspTable.getValueAt(i, 0);
                                    // Envoie du fichier avec le pseudopotentiel
                                    putFile(at.getPSPPath() + sep + at.getPSPFileName()
                                            + " " + rootPath + "/" + pseudopotFolder
                                            + "/" + at.getPSPFileName());
                                    configFileContent += cwd + "/" + rootPath.replaceFirst("./", "")
                                            + "/" + pseudopotFolder + "/" + at.getPSPFileName() + "\n";
                                } catch (Exception e) {
                                    //printERR(e.getMessage());
                                    printERR("Error processing pseudopotential(s) !");
                                    sendSIMButton_setEnabled(true);
                                    return;
                                }
                            }
                        } else {
                            printERR("Please setup pseudopotential(s) !");
                            sendSIMButton_setEnabled(true);
                            return;
                        }
                    } else {
                        printERR("Choose an option please ! (use an external "
                                + "inputfile or created a inputfile)");
                        sendSIMButton_setEnabled(true);
                        return;
                    }

                    // Création du fichier de configuration
                    try {
                        String FILESfileName = rootPath + sep + simName + ".files";
                        OutputStreamWriter fw = new OutputStreamWriter(
                                new FileOutputStream(FILESfileName), CharSet);
                        //FileWriter fw = new FileWriter(FILESfileName);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter pw = new PrintWriter(bw);
                        pw.print(configFileContent);
                        pw.close();
                        bw.close();
                        fw.close();
                    } catch (IOException e) {
                        //printERR(e.getMessage());
                        printERR("The configuration file (*.files) could not"
                                + " be created !");
                        sendSIMButton_setEnabled(true);
                        return;
                    }
                    if (remoteGatewayRadioButton_isSelected
                            || remoteAbinitRadioButton_isSelected) {
                        // Envoie du fichier de configuration
                        String configFile = rootPath + sep + simName + ".files";
                        String configFileR = rootPath + "/" + simName + ".files";

                        if (Utils.osName().startsWith("Windows")) {
                            Utils.dos2unix(new File(configFile));
                        }

                        putFile(configFile + " " + configFileR);

                        //if (Utils.osName().startsWith("Windows")) {
                        //    sendCommand("dos2unix " + configFileR);
                        //}
                    }

                    // Creation du dossier simName dans wholedataFolder
                    mkdir(rootPath + "/" + wholedataFolder + "/" + simName);
                    if (remoteGatewayRadioButton_isSelected
                            || remoteAbinitRadioButton_isSelected) {
                        mkdirR(rootPath + "/" + wholedataFolder + "/" + simName);
                    }

                    if (script.getSystem().equals("SGE")) {
                        String sgeSHFile = rootPath + sep + simName + ".SGE.sh";
                        String sgeSHFileR = rootPath + "/" + simName + ".SGE.sh";
                        if (remoteGatewayRadioButton_isSelected
                                || remoteAbinitRadioButton_isSelected) {

                            // Envoie du fichier SGE
                            putFile(sgeSHFile + " " + sgeSHFileR);

                            if (Utils.osName().startsWith("Windows")) {
                                sendCommand("dos2unix " + sgeSHFileR);
                            }
                        }
                        // lancement des commandes d'exécution de la simulation
                        sendCommand("qsub " + sgeSHFileR);
                    } else if (script.getSystem().equals("Frontend")) {
                        String SHFile = rootPath + sep + simName + ".sh";
                        String SHFileR = rootPath + "/" + simName + ".sh";

                        if (remoteGatewayRadioButton_isSelected
                                || remoteAbinitRadioButton_isSelected) {
                            /*if (Utils.osName().startsWith("Windows")) {
                             Utils.dos2unix(new File(SHFileR));
                             }*/
                            // Envoie du fichier BASH
                            putFile(SHFile + " " + SHFileR);

                            if (Utils.osName().startsWith("Windows")) {
                                sendCommand("dos2unix " + SHFileR);
                            }
                        }
                        // lancement des commandes d'exécution de la simulation
                        sendCommand("bash "+SHFileR);
                    } else if (script.getSystem().equals("SLURM")) {
                        String slurmSHFile = rootPath + sep + simName + ".SLURM.sh";
                        String slurmSHFileR = rootPath + "/" + simName + ".SLURM.sh";
                        if (remoteGatewayRadioButton_isSelected
                                || remoteAbinitRadioButton_isSelected) {

                            // Envoie du fichier SGE
                            putFile(slurmSHFile + " " + slurmSHFileR);

                            if (Utils.osName().startsWith("Windows")) {
                                sendCommand("dos2unix " + slurmSHFileR);
                            }
                        }
                        // lancement des commandes d'exécution de la simulation
                        sendCommand("sbatch " + slurmSHFileR);
                    }
                } else {
                    printERR("Please setup the inputfile textfield !");
                    sendSIMButton_setEnabled(true);
                    return;
                }

                if (localAbinitRadioButton_isSelected) {
                    printOUT("The simulation was submitted to the local Abinit server.");
                } else {
                    printOUT("The simulation was submitted to the remote Abinit"
                            + " server " + hostTextField_getText);
                    if (remoteGatewayRadioButton_isSelected) {
                        printOUT(" via the gateway " + gatewayHostTextField_getText + ".");
                    } else {
                        //printOUT(".");
                    }
                }
                printOUT("The submission thread ended successfully! (Abinit)");
                sendSIMButton_setEnabled(true);

                if (MainFrame.autoTest) {
                    System.exit(0);
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    private void openFileDialogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileDialogButtonActionPerformed
        JFileChooser fc = new JFileChooser(curPath);
        File currDir = new File(".");
        String currPath = currDir.getAbsolutePath();
        String basePath = basePath = currPath.replace("\\", "/").replace(".", "");
        printDEB(basePath);
        fc.setMultiSelectionEnabled(false);

        int retValue = fc.showOpenDialog(this);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            curPath = file.getParent();
            printDEB("CurPath = " + curPath);
            String relPath = file.getAbsolutePath().replace("\\", "/").replace(basePath, "./");
            openFileTextField.setText(relPath);
        }
}//GEN-LAST:event_openFileDialogButtonActionPerformed

    private void useExtIFRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useExtIFRadioButtonActionPerformed
        useExtIFRadioButton.setForeground(Color.red);
        useCreIFRadioButton.setForeground(Color.blue);

        openFileLabel.setEnabled(true);
        openFileDialogButton.setEnabled(true);
        displayFileButton.setEnabled(true);
        geditButton.setEnabled(true);
        openFileTextField.setEnabled(true);
        pspLabel.setEnabled(true);
        pspTextField.setEnabled(true);
        pspTable.setEnabled(true);
        pspTable.setVisible(true);

        openXMLFileLabel.setEnabled(false);
        openXMLFileDialogButton.setEnabled(false);
        openXMLFileTextField.setEnabled(false);
        saveFileAsButton.setEnabled(false);
        saveFileButton.setEnabled(false);
        createButton.setEnabled(false);
        inputFileDisplayer.setVisible(false);
        inputFileTabbedPane.setEnabled(false);

        inputFileTabbedPane.setSelectedIndex(inputFileTabbedPane.getTabCount() - 1);
}//GEN-LAST:event_useExtIFRadioButtonActionPerformed

    private void abinitPathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abinitPathButtonActionPerformed
        abinitPathButton.setEnabled(false);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String CMD = "whereis " + SequAbinit;
                RetMSG retmsg;
                if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
                    if (remoteExec != null) {
                        retmsg = remoteExec.sendCommand(CMD);
                        if (retmsg.getRetCode() == RetMSG.SUCCES) {
                            StringTokenizer st = new StringTokenizer(retmsg.getRetMSG());
                            int nbt = st.countTokens();
                            for (int i = 0; i < nbt; i++) {
                                String str = st.nextToken();
                                if (i == 1) {
                                    // TODO adapter au systÃ¨me Windows
                                    int idx = str.lastIndexOf('/');
                                    abinitPathTextField.setText((String) str.subSequence(0, idx));
                                }
                                printOUT(str);
                            }
                        } else {
                            //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                            printERR("Error: " + retmsg.getRetMSG() + " !");
                        }
                    } else {
                        printERR("First connect to an abinit host please !");
                    }
                } else if (localAbinitRadioButton.isSelected()) {
                    if (localExec != null) {
                        retmsg = localExec.sendCommand(CMD);
                        if (retmsg.getRetCode() == RetMSG.SUCCES) {
                            StringTokenizer st = new StringTokenizer(retmsg.getRetMSG());
                            int nbt = st.countTokens();
                            for (int i = 0; i < nbt; i++) {
                                String str = st.nextToken();
                                if (i == 1) {
                                    // TODO adapter au systÃ¨me Windows
                                    int idx = str.lastIndexOf('/');
                                    abinitPathTextField.setText((String) str.subSequence(0, idx));
                                }
                                printOUT(str);
                            }
                        } else {
                            //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                            printERR("Error: " + retmsg.getRetMSG() + " !");
                        }
                    }
                } else { // Le choix n'a pas été fait
                    printERR("Choose a destination option please at config. tab !");
                }
                abinitPathButton.setEnabled(true);
            }
        };

        Thread t = new Thread(r);
        t.start();
}//GEN-LAST:event_abinitPathButtonActionPerformed

    private void connectionToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionToggleButtonActionPerformed
        Runnable r = new Runnable() {
            @Override
            public void run() {
                connectionToggleButton.setEnabled(false);
                if (connectionToggleButton.isSelected()) {
                    if (remoteGatewayRadioButton.isSelected()) {
                        String gwHostname = gatewayHostTextField.getText();
                        String gwLogin = gatewayLoginTextField.getText();
                        if (!gwLogin.equals("") && !gwHostname.equals("")) {
                            String abHostname = hostTextField.getText();
                            String abLogin = loginTextField.getText();
                            if (!abLogin.equals("") && !abHostname.equals("")) {
                                // Début de la création du tunnel SSH
                                printOUT("Connecting to " + gwHostname + " as " + gwLogin + ".");
                                sshtun = new SSHTunnel(mainFrame, gwLogin, gwHostname, 22, abHostname, 2568, 22);
                                String keyFile = jTF_key2.getText();
                                if (keyFile.equals("")) {
                                    keyFile = null;
                                }
                                sshtun.setPrvkeyOrPwdOnly(keyFile, new String(gatewayPasswordField.getPassword()), useKey2);
                                lport = sshtun.start();
                                if (lport > 0 && lport < 65536) {
                                    printOUT("Connected to " + gwHostname + " as " + gwLogin + ".");
                                    printOUT("Connecting to " + abHostname + " as " + abLogin + ".");
                                    remoteExec = new RemoteExec(mainFrame, abLogin, "localhost", lport);
                                    keyFile = jTF_key1.getText();
                                    if (keyFile.equals("")) {
                                        keyFile = null;
                                    }
                                    remoteExec.setPrvkeyOrPwdOnly(keyFile, new String(pwdPasswordField.getPassword()), useKey1);
                                    if (remoteExec.start()) {
                                        printOUT("Connected to " + abHostname + " as " + abLogin + ".");
                                        // Le tunnel SSH a été créé avec succÃ¨s
                                        connectionToggleButton.setText("Disconnect");
                                    } else {
                                        lport = 0;
                                        printERR("Could not connect to " + abHostname + " as " + abLogin + " !");
                                        connectionToggleButton.setSelected(false);
                                        stopConnection();
                                    }
                                } else {
                                    lport = 0;
                                    printERR("Could not connect to " + gwHostname + " as " + gwLogin + " !");
                                    connectionToggleButton.setSelected(false);
                                    stopConnection();
                                }
                            } else {
                                printERR("Please enter the ABINIT hostname AND corresponding login !");
                                connectionToggleButton.setSelected(false);
                                stopConnection();
                            }
                        } else {
                            printERR("Please enter the gateway hostname AND corresponding login !");
                            connectionToggleButton.setSelected(false);
                            stopConnection();
                        }
                    } else if (remoteAbinitRadioButton.isSelected()) {
                        String abHostname = hostTextField.getText();
                        String abLogin = loginTextField.getText();
                        if (!abLogin.equals("") && !abHostname.equals("")) {
                            printOUT("Connecting to " + abHostname + " as " + abLogin + ".");
                            remoteExec = new RemoteExec(mainFrame, abLogin, abHostname, 22);
                            String keyFile = jTF_key1.getText();
                            if (keyFile.equals("")) {
                                keyFile = null;
                            }
                            remoteExec.setPrvkeyOrPwdOnly(keyFile, new String(pwdPasswordField.getPassword()), useKey1);
                            if (remoteExec.start()) {
                                printOUT("Connected to " + abHostname + " as " + abLogin + ".");
                                connectionToggleButton.setText("Disconnect");
                            } else {
                                printERR("Could not connect to " + abHostname + " as " + abLogin + " !");
                                connectionToggleButton.setSelected(false);
                                stopConnection();
                            }
                        } else {
                            printERR("Please enter the ABINIT hostname AND corresponding login !");
                            connectionToggleButton.setSelected(false);
                            stopConnection();
                        }
                    } else if (localAbinitRadioButton.isSelected()) {
                        // Pas besoin en local
                    } else { // Le choix n'a pas été fait
                        printERR("Choose a destination option please at config. tab !");
                    }
                } else {
                    stopConnection();
                    connectionToggleButton.setText("Connect");
                    printOUT("You are now disconnected!");
                }
                connectionToggleButton.setEnabled(true);
            }
        };

        Thread t = new Thread(r);
        t.start();
}//GEN-LAST:event_connectionToggleButtonActionPerformed

    private void remoteGatewayRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remoteGatewayRadioButtonActionPerformed
        connectionToggleButton.setEnabled(true);

        localAbinitRadioButton.setForeground(Color.blue);
        remoteGatewayRadioButton.setForeground(Color.red);
        remoteAbinitRadioButton.setForeground(Color.blue);

        gatewayLoginPanel.setVisible(true);
        loginPanel.setVisible(true);
        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
                "Remote Abinithost login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Arial", 3, 14), java.awt.Color.darkGray));
}//GEN-LAST:event_remoteGatewayRadioButtonActionPerformed

    private void remoteAbinitRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remoteAbinitRadioButtonActionPerformed
        connectionToggleButton.setEnabled(true);

        localAbinitRadioButton.setForeground(Color.blue);
        remoteGatewayRadioButton.setForeground(Color.blue);
        remoteAbinitRadioButton.setForeground(Color.red);

        gatewayLoginPanel.setVisible(false);
        loginPanel.setVisible(true);
        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
                "Remote Abinithost login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Arial", 3, 14), java.awt.Color.darkGray));
}//GEN-LAST:event_remoteAbinitRadioButtonActionPerformed

    private void localAbinitRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localAbinitRadioButtonActionPerformed
        connectionToggleButton.setEnabled(false);

        localAbinitRadioButton.setForeground(Color.red);
        remoteGatewayRadioButton.setForeground(Color.blue);
        remoteAbinitRadioButton.setForeground(Color.blue);

        gatewayLoginPanel.setVisible(false);
        loginPanel.setVisible(false);
        loginPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
                "Local Abinithost login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Arial", 3, 14), java.awt.Color.darkGray));
}//GEN-LAST:event_localAbinitRadioButtonActionPerformed

    private void mySimulationsLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mySimulationsLabelMouseClicked
        printGEN("--- HINT ------------------------------------------", Color.BLACK, false, true);
        printGEN("You have to start your path with ./ and give a folder name where"
                + " to create the abinit filetree\n", Color.RED, false, true);
        printGEN("Example: ./MySimulations\n", new Color(0, 100, 0), false, true);
        printGEN("The filetree will be created in your local computer and at the"
                + " Abinit server side when using remote Abinit servers", Color.DARK_GRAY, false, true);
        printGEN("---------------------------------------------------", Color.BLACK, false, true);
    }//GEN-LAST:event_mySimulationsLabelMouseClicked

    private void pspPathLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pspPathLabelMouseClicked
        printGEN("--- HINT ------------------------------------------", Color.BLACK, false, true);
        printGEN("Please fill in the path to your local pseudopotential database\n", Color.RED, false, true);
        printGEN("Examples: ./PSP (when the database is in your root folder or in the"
                + " same one as AbinitGUI.jar) or something like /home/user/PSP\n", new Color(0, 100, 0), false, true);
        printGEN("You can find the database at http://www.flavio-abreu.net", Color.DARK_GRAY, false, true);
        printGEN("---------------------------------------------------", Color.BLACK, false, true);
    }//GEN-LAST:event_pspPathLabelMouseClicked

    private void abinitPathPathLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_abinitPathPathLabelMouseClicked
        printGEN("--- HINT ------------------------------------------", Color.BLACK, false, true);
        printGEN("Remote path where to find the abinit program\n", Color.RED, false, true);
        printGEN("Example: /Users/me/Abinit6.7.2/bin\n", new Color(0, 100, 0), false, true);
        printGEN("---------------------------------------------------", Color.BLACK, false, true);
    }//GEN-LAST:event_abinitPathPathLabelMouseClicked

    private void jCB_useKey1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCB_useKey1ActionPerformed
        if (jCB_useKey1.isSelected()) {
            this.useKey1 = true;
            pwdLabel.setText("Passphrase");
            jTF_key1.setEnabled(true);
        } else {
            this.useKey1 = false;
            pwdLabel.setText("Password");
            jTF_key1.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_useKey1ActionPerformed

    private void jCB_useKey2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCB_useKey2ActionPerformed
        if (jCB_useKey2.isSelected()) {
            this.useKey2 = true;
            pwdBFELabel.setText("Passphrase");
            jTF_key2.setEnabled(true);
        } else {
            this.useKey2 = false;
            pwdBFELabel.setText("Password");
            jTF_key2.setEnabled(false);
        }
    }//GEN-LAST:event_jCB_useKey2ActionPerformed

    private void SSH2ClientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SSH2ClientButtonActionPerformed
        MySSHTerm ssh2;
        if (remoteGatewayRadioButton.isSelected()) {
            if (lport != 0) {
                String host = "localhost";
                String user = loginTextField.getText();
                String keyFile = jTF_key1.getText();
                String pass = new String(pwdPasswordField.getPassword());
                if (useKey1) {
                    ssh2 = new MySSHTerm(mainFrame, host, lport, user, keyFile, pass);
                } else {
                    ssh2 = new MySSHTerm(mainFrame, host, lport, user, pass);
                }
                Thread thread = new Thread(ssh2);
                thread.start();
            } else {
                printERR("The ssh tunnel is not working, please connect at the config. tab before !");
            }
        } else if (remoteAbinitRadioButton.isSelected()) {
            String host = hostTextField.getText();
            String user = loginTextField.getText();
            String keyFile = jTF_key1.getText();
            String pass = new String(pwdPasswordField.getPassword());
            if (useKey1) {
                ssh2 = new MySSHTerm(mainFrame, host, 22, user, keyFile, pass);
            } else {
                ssh2 = new MySSHTerm(mainFrame, host, 22, user, pass);
            }
            Thread thread = new Thread(ssh2);
            thread.start();
        } else if (localAbinitRadioButton.isSelected()) {
            printERR("To connect to the local host, please choose the remote option"
                    + " where you specify localhost as hostname !");
        } else { // Le choix n'a pas été fait
            printERR("Choose a destination option please at config. tab !");
        }
    }//GEN-LAST:event_SSH2ClientButtonActionPerformed

    private void jMenuItemClustepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClustepActionPerformed
        clustepDiag.setVisible(true);
    }//GEN-LAST:event_jMenuItemClustepActionPerformed

    private void jMenuItemTBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTBActionPerformed
        tightBindingDiag.setVisible(true);
    }//GEN-LAST:event_jMenuItemTBActionPerformed

    private void scriptListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_scriptListValueChanged
        int index = scriptList.getSelectedIndex();
        if (index < 0) {
            scriptName.setText("Name");
            scriptDescription.setText("Description of the script");
            scriptProgram.setText("");
            argsModel.resetScripts();
            outModel.resetScripts();
        } else {
            Script scr = scriptBibs.getList().get(index);
            if (scr == null) {
                scriptName.setText("Name");
                scriptDescription.setText("Description of the script");
                scriptProgram.setText("");
                argsModel.resetScripts();
                outModel.resetScripts();
                return;
            }

            scriptName.setText(scr.title);

            scriptDescription.setText(scr.description.replace("\\n", "\n"));

            scriptProgram.setText(scr.program);
            argsModel.resetScripts();
            outModel.resetScripts();

            ArrayList<ScriptArgument> listArgs = scr.listArgs;

            int nbArgs = listArgs.size();

            for (int i = 0; i < nbArgs; i++) {
                argsModel.addScript(listArgs.get(i));
            }

            ArrayList<ScriptArgument> outArgs = scr.listOutput;

            int nbOut = outArgs.size();

            for (int i = 0; i < nbOut; i++) {
                outModel.addScript(outArgs.get(i));
            }
        }

    }//GEN-LAST:event_scriptListValueChanged

    private void launchScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchScriptActionPerformed
        Runnable r = new Runnable() {
            @Override
            public void run() {

                boolean localAbinitRadioButton_isSelected = localAbinitRadioButton.isSelected();
                boolean remoteGatewayRadioButton_isSelected = remoteGatewayRadioButton.isSelected();
                boolean remoteAbinitRadioButton_isSelected = remoteAbinitRadioButton.isSelected();

                String rootPath = mySimulationsTextField.getText();

                String folder = "scripts";

                int index = scriptList.getSelectedIndex();

                if (index == -1) {
                    printERR("Please select a script before running");
                    return;
                }

                Script scr = scriptBibs.getList().get(index);

                if (scr == null) {
                    printERR("Please select a script before running");
                    return;
                }

                String inputFile = scr.fileName;

                String program = scr.program;

                // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                if (Utils.osName().startsWith("Windows") || Utils.osName().startsWith("MAC")) {
                    printERR("Scripts are not yet supported for Windows platform");
                    launchScript.setEnabled(true);
                    return;
                }

                // ********************************************************************************************************************************

                String cwd = "";

                String CMD = "pwd";

                RetMSG retmsg;
                
                retmsg = localExec.sendCommand(CMD);
                if (retmsg.getRetCode() == RetMSG.SUCCES) {
                    printOUT("PWD: " + retmsg.getRetMSG());
                    cwd = removeEndl(retmsg.getRetMSG());
                } else {
                    printERR("Error: " + retmsg.getRetMSG() + " !");
                }
                        
                String inputFileName = Utils.getLastToken(inputFile.replace('\\', '/'), "/");

                // Test de l'existance de inputfile
                if (!Utils.exists(inputFile)) {
                    printERR("The file " + inputFile + " doesn't exist !");
                    launchScript.setEnabled(true);
                    return;
                }

                if (!inputFile.equals("")) {
                    // Will do the computation in rootpath/folder
                    String inputFileR = rootPath + "/" + folder + "/" + inputFileName;
                    putFile(inputFile + " " + inputFileR);
                    
                    ArrayList<String> allCommand = new ArrayList<>();
                    ArrayList<ScriptArgument> listArgs = scr.listArgs;
                    allCommand.add(program);
                    allCommand.add(inputFileR);
                    
                    String command = program + " " + inputFileR;

                    for (int i = 0; i < listArgs.size(); i++) {
                        String input = (String) scriptArgTable.getValueAt(i, 1);
                        command = command + " --" + listArgs.get(i).name + " \'" + input + "\'";
                        allCommand.add("--" + listArgs.get(i).name );
                        allCommand.add(input);
                    }
                    
                    System.out.println("CWD = "+cwd);

                    ArrayList<ScriptArgument> listOut = scr.listOutput;

                    for (int i = 0; i < listOut.size(); i++) {
                        String outFile = (String) scriptOutTable.getValueAt(i, 1);

                        String outFileR = rootPath + "/" + folder + "/" + outFile;

                        command = command + " --" + listOut.get(i).name + " \'" + outFileR + "\'";
                        allCommand.add("--" + listOut.get(i).name);
                        allCommand.add(outFileR);
                    }
                    
                    String[] arrayCMD = allCommand.toArray(new String[0]);
                    sendCommand(arrayCMD);
                    printDEB(command);

                }

            }
        };

        Thread t = new Thread(r);
        t.start();
    }//GEN-LAST:event_launchScriptActionPerformed

    private void scriptProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scriptProgramActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_scriptProgramActionPerformed

    private void openOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openOutputActionPerformed

        int index = scriptList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        Script scr = scriptBibs.getList().get(index);
        if (scr == null) {
            return;
        }

        ArrayList<ScriptArgument> listOut = scr.listOutput;

        String rootPath = mySimulationsTextField.getText();

        String folder = "scripts";

        // Open files
        for (int i = 0; i < listOut.size(); i++) {
            String outFile = (String) scriptOutTable.getValueAt(i, 1);
            String outFileR = rootPath + "/" + folder + "/" + outFile;

            File f = new File(rootPath);

            if (!f.exists()) {
                createFiletree();
                f = new File(rootPath);
            }

            String abs = "";
            try {
                abs = f.getCanonicalPath();
            } catch (IOException ex) {
                printERR("Unable to get output file canonical path");
                return;
            }

            outFileR = abs + "/" + folder + "/" + outFile;

            f = new File(outFileR);

            printOUT("Trying to open file : " + outFileR);
            if (f.exists()) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        if (Utils.osName().contains("Windows")) {
                            if (outFileR.endsWith(".txt") || outFileR.endsWith(".dbs") || outFileR.endsWith(".agr")
                                    || outFileR.endsWith(".files") || outFileR.endsWith(".in") || outFileR.endsWith(".out")
                                    || outFileR.endsWith(".sh")) {
                                editFile(outFileR, true);
                            } else {
                                Desktop.getDesktop().open(f);
                            }
                        } else {
                            Desktop.getDesktop().open(f);
                        }
                    } else {
                        printOUT("Not able to open the file with the default editor. Use basic editor instead.");
                        editFile(outFileR, true);
                    }
                } catch (IOException ex) {
                    printOUT("Not able to open the file with the default editor. Use editor instead.");
                    editFile(outFileR, true);
                }
            } else {
                printERR("Please execute the script before opening the output files.");
            }

        }
    }//GEN-LAST:event_openOutputActionPerformed

    private void SFTPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SFTPButtonActionPerformed
        MySFTP client;
        if (remoteGatewayRadioButton.isSelected()) {
            if (lport != 0) {
                String host = "localhost";
                String user = loginTextField.getText();
                String keyFile = jTF_key1.getText();
                String pass = new String(pwdPasswordField.getPassword());
                if (useKey1) {
                    client = new MySFTP(this, host, lport, user, keyFile, pass);
                } else {
                    client = new MySFTP(this, host, lport, user, pass);
                }
                Thread thread = new Thread(client);
                thread.start();
            } else {
                printERR("The ssh tunnel is not working, please connect at the config. tab before !");
            }
        } else if (remoteAbinitRadioButton.isSelected()) {
            String host = hostTextField.getText();
            String user = loginTextField.getText();
            String keyFile = jTF_key1.getText();
            String pass = new String(pwdPasswordField.getPassword());
            if (useKey1) {
                client = new MySFTP(this, host, 22, user, keyFile, pass);
            } else {
                client = new MySFTP(this, host, 22, user, pass);
            }
            Thread thread = new Thread(client);
            thread.start();
        } else if (localAbinitRadioButton.isSelected()) {
            printERR("To connect to the local host, please choose the remote option"
                    + " where you specify localhost as hostname !");
        } else { // Le choix n'a pas été fait
            printERR("Choose a destination option please at config. tab !");
        }
    }//GEN-LAST:event_SFTPButtonActionPerformed

    private void pspTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pspTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pspTextFieldActionPerformed

    private void editScriptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editScriptsActionPerformed
        int idScript = scriptList.getSelectedIndex();

        if (idScript == -1) {
            printERR("Please select a script first");
        } else {
            Script scr = scriptBibs.getList().get(idScript);

            String inputFile = scr.fileName;

            if (Utils.osName().startsWith("Windows")) {
                Utils.unix2dos(new File(inputFile));
            }
            editFile(inputFile, true);
        }
    }//GEN-LAST:event_editScriptsActionPerformed

    private void SubmissionEditButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SubmissionEditButton1ActionPerformed
        submitScriptFrame.setScript(simulation.getRemoteJob().getScript());
        submitScriptFrame.setVisible(true);
    }//GEN-LAST:event_SubmissionEditButton1ActionPerformed

    private void testAnalyzeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testAnalyzeActionPerformed

        new Thread(
                new Runnable() {
                    public void run() {
                        boolean localAbinitRadioButton_isSelected = localAbinitRadioButton.isSelected();
                        boolean remoteGatewayRadioButton_isSelected = remoteGatewayRadioButton.isSelected();
                        boolean remoteAbinitRadioButton_isSelected = remoteAbinitRadioButton.isSelected();

                        String rootPath = mySimulationsTextField.getText();

                        boolean useExtIFRadioButton_isSelected = useExtIFRadioButton.isSelected();

                        String inputFile;

                        if (useExtIFRadioButton_isSelected) {
                            inputFile = openFileTextField.getText();
                        } else {
                            printERR("This option only works with external input files");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        if (localAbinitRadioButton_isSelected && Utils.osName().startsWith("Windows")) {
                            printERR("Please connect to a remote host before pre-processing input files !");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        if ((remoteGatewayRadioButton_isSelected
                                || remoteAbinitRadioButton_isSelected) && remoteExec == null) {
                            printERR("Please connect to a remote host before pre-processing input files !");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        // Disabled since too long !
                        createFiletree();

                        String inputFolder = "input";

                        // ********************************************************************************************************************************
                        String cwd = "";

                        String CMD = "pwd";

                        RetMSG retmsg;
                        if (remoteGatewayRadioButton_isSelected || remoteAbinitRadioButton_isSelected) {
                            if (remoteExec != null) {
                                retmsg = remoteExec.sendCommand(CMD);
                                if (retmsg.getRetCode() == RetMSG.SUCCES) {
                                    printOUT("PWD: " + retmsg.getRetMSG());
                                    cwd = removeEndl(retmsg.getRetMSG());
                                } else {
                                    //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                                    printERR("Error: " + retmsg.getRetMSG() + " !");
                                }
                            } else {
                                printERR("First connect to an abinit host please !");
                            }
                        } else if (localAbinitRadioButton_isSelected) {
                            if (localExec != null) {
                                retmsg = localExec.sendCommand(CMD);
                                if (retmsg.getRetCode() == RetMSG.SUCCES) {
                                    printOUT("PWD: " + retmsg.getRetMSG());
                                    cwd = removeEndl(retmsg.getRetMSG());
                                } else {
                                    //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                                    printERR("Error: " + retmsg.getRetMSG() + " !");
                                }
                            }
                        } else { // Le choix n'a pas été fait
                            printERR("Choose a destination option please at config. tab !");
                        }

                        // ********************************************************************************************************************************

                        String inputFileName = "";

                        String pathToAbinit = abinitPathTextField.getText();

                        inputFileName = Utils.getLastToken(inputFile.replace('\\', '/'), "/");

                        // Test de l'existance de inputfile
                        if (!Utils.exists(inputFile)) {
                            printERR("The file " + inputFile + " doesn't exist !");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        String simName = null;
                        if (inputFileName != null) {
                            int idx = inputFileName.indexOf('.');
                            if (idx > 0 && idx < inputFileName.length()) {
                                simName = inputFileName.substring(0, idx);
                            } else {
                                simName = inputFileName;
                            }
                        }

                        if (!inputFile.equals("")) {
                            // Envoie (copie) du fichier d'input
                            String inputFileR = rootPath + "/" + inputFolder + "/" + inputFileName;
                            putFile(inputFile + " " + inputFileR);
                            
                            // Envoi du script :> a faire en local plus tard !
                            String scriptFile = "abinit2json.py";
                            String scriptFileR = rootPath + "/" + inputFolder + "/" + scriptFile;
                            putFile(scriptFile + " " + scriptFileR);

                            // "PYTHONPATH=\""+abipyPathTextField.getText()+"\":$PYTHONPATH 
                            String cmd = "python " + scriptFileR + " " + inputFileR;

                            sendCommand(cmd);

                            getFile(cwd + "/" + simName + ".in.json.txt" + " " + inputFile + ".json.txt");

                            final Path out = FileSystems.getDefault().getPath(inputFile + ".json.txt");

                            guiEditor.loadFile(out.toString());
                        }

                    }
                }).start();
    }//GEN-LAST:event_testAnalyzeActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

        abinitInputVars.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void editPYDFTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPYDFTActionPerformed
        new Thread(
                new Runnable() {
                    public void run() {
                        boolean localAbinitRadioButton_isSelected = localAbinitRadioButton.isSelected();
                        boolean remoteGatewayRadioButton_isSelected = remoteGatewayRadioButton.isSelected();
                        boolean remoteAbinitRadioButton_isSelected = remoteAbinitRadioButton.isSelected();

                        String rootPath = mySimulationsTextField.getText();

                        boolean useExtIFRadioButton_isSelected = useExtIFRadioButton.isSelected();

                        String inputFile;

                        if (useExtIFRadioButton_isSelected) {
                            inputFile = openFileTextField.getText();
                        } else {
                            printERR("Choose an option please ! (use an external"
                                    + " inputfile or created a inputfile)");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        if (localAbinitRadioButton_isSelected && Utils.osName().startsWith("Windows")) {
                            printERR("Please connect to a remote CLUSTEP host before submitting a simulation !");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        if ((remoteGatewayRadioButton_isSelected
                                || remoteAbinitRadioButton_isSelected) && remoteExec == null) {
                            printERR("Please connect to a ABINIT host before submitting a simulation !");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        // Disabled since too long !
                        //createFiletree();

                        String inputFolder = "input";
                        String logfilesFolder = "logfiles";

                        // ********************************************************************************************************************************
                        String cwd = "";

                        String CMD = "pwd";

                        RetMSG retmsg;
                        if (remoteGatewayRadioButton_isSelected || remoteAbinitRadioButton_isSelected) {
                            if (remoteExec != null) {
                                retmsg = remoteExec.sendCommand(CMD);
                                if (retmsg.getRetCode() == RetMSG.SUCCES) {
                                    printOUT("PWD: " + retmsg.getRetMSG());
                                    cwd = removeEndl(retmsg.getRetMSG());
                                } else {
                                    //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                                    printERR("Error: " + retmsg.getRetMSG() + " !");
                                }
                            } else {
                                printERR("First connect to an abinit host please !");
                            }
                        } else if (localAbinitRadioButton_isSelected) {
                            if (localExec != null) {
                                retmsg = localExec.sendCommand(CMD);
                                if (retmsg.getRetCode() == RetMSG.SUCCES) {
                                    printOUT("PWD: " + retmsg.getRetMSG());
                                    cwd = removeEndl(retmsg.getRetMSG());
                                } else {
                                    //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                                    printERR("Error: " + retmsg.getRetMSG() + " !");
                                }
                            }
                        } else { // Le choix n'a pas été fait
                            printERR("Choose a destination option please at config. tab !");
                        }

                        // ********************************************************************************************************************************

                        String inputFileName = "";

                        String sep = Utils.fileSeparator();

                        String pathToAbinit = abinitPathTextField.getText();

                        inputFileName = Utils.getLastToken(inputFile.replace('\\', '/'), "/");

                        // Test de l'existance de inputfile
                        if (!Utils.exists(inputFile)) {
                            printERR("The file " + inputFile + " doesn't exist !");
                            sendSIMButton_setEnabled(true);
                            return;
                        }

                        String simName = null;
                        if (inputFileName != null) {
                            int idx = inputFileName.indexOf('.');
                            if (idx > 0 && idx < inputFileName.length()) {
                                simName = inputFileName.substring(0, idx);
                            } else {
                                simName = inputFileName;
                            }
                        }

                        if (!inputFile.equals("")) {
                            // Envoie (copie) du fichier d'input
                            String inputFileR = rootPath + "/" + inputFolder + "/" + inputFileName;
                            putFile(inputFile + " " + inputFileR);
                            
                            // Envoi du script :> a faire en local plus tard !
                            String scriptFile = "pydft_parser.py";
                            String scriptFileR = rootPath + "/" + inputFolder + "/" + scriptFile;
                            putFile(scriptFile + " " + scriptFileR);

                            String cmd = "python " + scriptFileR + " " + inputFileR;

                            sendCommand(cmd);

                            getFile(cwd + "/" + simName + ".in.json.txt" + " " + inputFile + ".json.txt");

                            String fileName = openFileTextField.getText();

                            final Path out = FileSystems.getDefault().getPath(inputFile + ".json.txt");

                            inputEditor.loadFile(out.toString());
                        }

                    }
                }).start();
    }//GEN-LAST:event_editPYDFTActionPerformed

    private void useCreIFRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCreIFRadioButtonActionPerformed
        useCreIFRadioButton.setForeground(Color.red);
        useExtIFRadioButton.setForeground(Color.blue);

        openXMLFileLabel.setEnabled(true);
        openXMLFileDialogButton.setEnabled(true);
        openXMLFileTextField.setEnabled(true);
        saveFileAsButton.setEnabled(true);
        saveFileButton.setEnabled(true);
        createButton.setEnabled(true);
        inputFileTabbedPane.setEnabled(true);

        openFileLabel.setEnabled(false);
        openFileDialogButton.setEnabled(false);
        displayFileButton.setEnabled(false);
        geditButton.setEnabled(false);
        openFileTextField.setEnabled(false);
        pspLabel.setEnabled(false);
        pspTextField.setEnabled(false);
        pspTable.setEnabled(false);
        pspTable.setVisible(false);

        inputFileTabbedPane.setSelectedIndex(0);
    }//GEN-LAST:event_useCreIFRadioButtonActionPerformed

    private void openXMLFileDialogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openXMLFileDialogButtonActionPerformed
        JFileChooser fc = new JFileChooser(mySimulationsTextField.getText());
        fc.setMultiSelectionEnabled(false);
        int retValue = fc.showOpenDialog(this);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            openXMLFileTextField.setText(file.getPath());
        }
    }//GEN-LAST:event_openXMLFileDialogButtonActionPerformed

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        inputFileDisplayer.setVisible(true);
        //inputFileDisplayer.setText(getBasics()); // TODO
        inputFileDisplayer.setText(geomD.getData() + alcoD.getData() + rereD.getData()
                + wadeD.getData() + inouD.getData() + theoD.getData() + otherTextArea.getText());
    }//GEN-LAST:event_createButtonActionPerformed

    private void geometryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geometryButtonActionPerformed
        geomD.setVisible(true);
    }//GEN-LAST:event_geometryButtonActionPerformed

    private void algoAndConvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoAndConvButtonActionPerformed
        alcoD.setVisible(true);
    }//GEN-LAST:event_algoAndConvButtonActionPerformed

    private void realAndRecipButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_realAndRecipButtonActionPerformed
        rereD.setVisible(true);
    }//GEN-LAST:event_realAndRecipButtonActionPerformed

    private void wavefuncAndDensButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wavefuncAndDensButtonActionPerformed
        wadeD.setVisible(true);
    }//GEN-LAST:event_wavefuncAndDensButtonActionPerformed

    private void inputOutputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputOutputButtonActionPerformed
        inouD.setVisible(true);
    }//GEN-LAST:event_inputOutputButtonActionPerformed

    private void theoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_theoryButtonActionPerformed
        theoD.setVisible(true);
    }//GEN-LAST:event_theoryButtonActionPerformed

    private void saveFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileButtonActionPerformed
        printERR("Save file is not implemented yet!");
    }//GEN-LAST:event_saveFileButtonActionPerformed

    private void saveFileAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileAsButtonActionPerformed
        printERR("Save file as is not implemented yet!");
    }//GEN-LAST:event_saveFileAsButtonActionPerformed

    private void reloadScriptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadScriptsActionPerformed
        scriptBibs.getList().clear();
        scriptBibs.loadScriptsFromFile("listScripts.xml");
        showScripts();
    }//GEN-LAST:event_reloadScriptsActionPerformed

    private void abipyPathPathLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_abipyPathPathLabelMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_abipyPathPathLabelMouseClicked

    private void abipyPathTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abipyPathTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_abipyPathTextFieldActionPerformed

    private void testAnalyze1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testAnalyze1ActionPerformed
        guiEditorNew.loadFile(openFileTextField.getText());
    }//GEN-LAST:event_testAnalyze1ActionPerformed

    public void sendCommand(String CMD) /*throws CMDException*/ {
        RetMSG retmsg;
        if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
            retmsg = remoteExec.sendCommand(CMD);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else if (localAbinitRadioButton.isSelected()) {
            retmsg = localExec.sendCommand(CMD);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                printERR("Error (RetVal = " + retmsg.getRetCode() + ") : " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else { // Le choix n'a pas été fait
            printERR("Choose a destination option please at config. tab !");
        }
    }      

    public void sendCommand(String CMD[]) /*throws CMDException*/ {
        RetMSG retmsg;
        if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
            retmsg = remoteExec.sendCommand(CMD);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else if (localAbinitRadioButton.isSelected()) {
            retmsg = localExec.sendCommand(CMD);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                printERR("Error (RetVal = " + retmsg.getRetCode() + ") : " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else { // Le choix n'a pas été fait
            printERR("Choose a destination option please at config. tab !");
        }
    }

    public void localCommand(String CMD) /*throws CMDException*/ {
        RetMSG retmsg;
        retmsg = localExec.sendCommand(CMD);
        if (retmsg.getRetCode() == RetMSG.SUCCES) {
            printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
        } else {
            //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
            printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
        }
    }

    public void mkdir(String dir) {
        if (Utils.mkdir(dir)) {
            printOUT("Succes: mkdir " + dir + ".");
        } else {
            if (Utils.exists(dir)) {
                printDEB("The local directory `" + dir + "' exists !");
            } else {
                printERR("Error: mkdir: cannot create directory `" + dir + "' !");
            }
        }
    }

    public void mkdirR(String dir) {
        String CMD = "mkdir " + dir;
        RetMSG retmsg;
        if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
            retmsg = remoteExec.sendCommand(CMD);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                if (retmsg.getRetCode() == 1) {
                    printDEB("The remote directory `" + dir + "' exists !");
                } else {
                    //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                    printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
                }
            }
        } else {
            printERR("Error: local use of mkdirR !");
        }
    }

    private String getOutputFilesR(String dir) {
        String CMD = "ls " + dir;
        RetMSG retmsg;
        if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
            retmsg = remoteExec.sendCommand(CMD);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD());
                return removeEndl(retmsg.getRetMSG());
            } else {
                //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
                return "";
            }
        } else {
            printERR("Error: local use of lsR !");
            return "";
        }
    }

    private void localCopy(String parameters) {
        RetMSG retmsg = localExec.sendCommand("cp " + parameters);
        System.out.println(retmsg.getRetMSG());
        if (retmsg.getRetCode() == RetMSG.SUCCES) {
            printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
        } else {
            //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
            printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
        }
    }

    public void putFile(String parameters) {
        RetMSG retmsg;
        if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
            retmsg = remoteExec.sendCommand("put " + parameters);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else if (localAbinitRadioButton.isSelected()) {
            retmsg = localExec.sendCommand("cp " + parameters);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else { // Le choix n'a pas été fait
            printERR("Choose a destination option please at config. tab !");
        }
    }

    public void getFile(String parameters) {
        RetMSG retmsg;
        if (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected()) {
            retmsg = remoteExec.sendCommand("get " + parameters);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else if (localAbinitRadioButton.isSelected()) {
            retmsg = localExec.sendCommand("cp " + parameters);
            if (retmsg.getRetCode() == RetMSG.SUCCES) {
                printOUT("Succes: " + retmsg.getCMD() + " => " + removeEndl(retmsg.getRetMSG()) + ".");
            } else {
                //printERR("Error (RetVal = " + retmsg.getRetCode() + "): " + retmsg.getRetMSG());
                printERR("Error: " + removeEndl(retmsg.getRetMSG()) + " !");
            }
        } else { // Le choix n'a pas été fait
            printERR("Choose a destination option please at config. tab !");
        }
    }

    private void showScripts() {
        scriptList.clearSelection();
        scriptModel.clear();

        ArrayList<Script> list = scriptBibs.getList();
        int nbScripts = list.size();

        String strName[] = new String[nbScripts];

        for (int i = 0; i < nbScripts; i++) {
            Script scr = list.get(i);

            strName[i] = scr.title;
        }

        scriptList.setListData(strName);
    }

    void saveConfig(String confFile) {
        String tmpvar = "";

        XMLConfigWriter conf = new XMLConfigWriter("tabbedpaneconf");

        // ********************* congfiguration ********************************
        // *********************************************************************
        Element congfiguration = conf.add2root("configuration");
        //**********************************************************************
        Enumeration en = whereIsAbinitbuttonGroup.getElements();
        while (en.hasMoreElements()) {
            JRadioButton jrb = (JRadioButton) en.nextElement();
            if (jrb.isSelected()) {
                tmpvar = jrb.getText();
            }
        }
        conf.setAttr(congfiguration, "hostlocation", "lacation", tmpvar);
        //**********************************************************************
        tmpvar = loginTextField.getText();
        conf.setAttr(congfiguration, "remotelogin", "login", tmpvar);
        //**********************************************************************
        tmpvar = hostTextField.getText();
        conf.setAttr(congfiguration, "remotehost", "host", tmpvar);
        //**********************************************************************
        tmpvar = new String(pwdPasswordField.getPassword());
        conf.setAttr(congfiguration, "remotepwd", "pwd", tmpvar);
        //**********************************************************************
        if (jCB_useKey1.isSelected()) {
            tmpvar = "1";
        } else {
            tmpvar = "0";
        }
        conf.setAttr(congfiguration, "keyRemote", "checked", tmpvar);
        //**********************************************************************
        tmpvar = jTF_key1.getText();
        conf.setAttr(congfiguration, "keyFileRemote", "keyFile", tmpvar);
        //**********************************************************************
        tmpvar = gatewayLoginTextField.getText();
        conf.setAttr(congfiguration, "gatewaylogin", "login", tmpvar);
        //**********************************************************************
        tmpvar = gatewayHostTextField.getText();
        conf.setAttr(congfiguration, "gatewayhost", "host", tmpvar);
        //**********************************************************************
        tmpvar = new String(gatewayPasswordField.getPassword());
        conf.setAttr(congfiguration, "gatewaypwd", "pwd", tmpvar);
        //**********************************************************************
        if (jCB_useKey2.isSelected()) {
            tmpvar = "1";
        } else {
            tmpvar = "0";
        }
        conf.setAttr(congfiguration, "keyGateway", "checked", tmpvar);
        //**********************************************************************
        tmpvar = jTF_key2.getText();
        conf.setAttr(congfiguration, "keyFileGateway", "keyFile", tmpvar);
        //**********************************************************************
        tmpvar = mySimulationsTextField.getText();
        conf.setAttr(congfiguration, "mySimulationspath", "path", tmpvar);
        //**********************************************************************
        tmpvar = pspPathTextField.getText();
        conf.setAttr(congfiguration, "psppath", "path", tmpvar);
        //**********************************************************************
        tmpvar = abinitPathTextField.getText();
        conf.setAttr(congfiguration, "abinitpath", "path", tmpvar);
        //**********************************************************************
        tmpvar = abipyPathTextField.getText();
        conf.setAttr(congfiguration, "abipypath", "path", tmpvar);
        //**********************************************************************
        tmpvar = openFileTextField.getText();
        conf.setAttr(congfiguration, "openfile", "file", tmpvar);
        //**********************************************************************
        tmpvar = pspTextField.getText();
        conf.setAttr(congfiguration, "psp", "psp", tmpvar);
        //**********************************************************************
        int nbelem = 0;
        try {
            nbelem = Integer.parseInt(pspTextField.getText());
        } catch (Exception e) {
            nbelem = 0;
        }
        if (nbelem > 0) {
            Element pspTable_ = new Element("psptable");
            congfiguration.addContent(pspTable_);

            int row = pspTable.getRowCount();
            if (row > 0) {
                for (int i = 0; i < row; i++) {

                    try {
                        // L'élément pspTable.getValueAt(i, 0) est de type Atom
                        // et toutes les infos du pseudopotentiel sont dedans.
                        // J'aurais pu utiliser cet objet pour obtenir tous les champs!
                        String symbol = (pspTable.getValueAt(i, 0)).toString();
                        String pspfile = (pspTable.getValueAt(i, 1)).toString();
                        String psptype = (pspTable.getValueAt(i, 2)).toString();
                        String psppath = (pspTable.getValueAt(i, 3)).toString();

                        conf.setAttr(pspTable_, "entry", new String[]{"i", "symbol", "psppath", "pspfile", "psptype"},
                                new String[]{Integer.toString(i), symbol, psppath, pspfile, psptype});
                    } catch (Exception e) {
                        printERR("Bug reading pspTable (XML) !");
                    }

                }
            }
        }

        // ********************* input file ************************************
        // *********************************************************************
        Element inputfile = conf.add2root("inputfile");

        // *********************************************************************
        // Pour le débuggage
        //conf.display();

        if (confFile == null) {
            conf.save2file("config.xml");
        } else {
            conf.save2file(confFile);
        }
    }

    void loadConfig(String file2load) {

        XMLConfigReader conf = new XMLConfigReader(file2load);

        if (conf.getRoot() != null) {
            List l1 = conf.getRoot().getChildren();
            Iterator i1 = l1.iterator();
            while (i1.hasNext()) {
                Element cur1 = (Element) i1.next();
                // *************** configuration ***********************************
                if (cur1.getName().equals("configuration")) {
                    List l2 = cur1.getChildren();
                    Iterator i2 = l2.iterator();
                    while (i2.hasNext()) {
                        Element cur2 = (Element) i2.next();
                        String elemName = cur2.getName();
                        Attribute attr;
                        List lAttr = cur2.getAttributes();
                        Iterator iAttr = lAttr.iterator();
                        // On s'intéresse qu'Ã  un seul atribut
                        if (iAttr.hasNext()) {
                            attr = (Attribute) iAttr.next();
                            String attrValue = attr.getValue();

                            if (elemName.equals("hostlocation")) {
                                if (attrValue.equals(localAbinitRadioButton.getText())) {
                                    localAbinitRadioButton.setSelected(true);
                                    localAbinitRadioButtonActionPerformed(null);
                                } else if (attrValue.equals(remoteAbinitRadioButton.getText())) {
                                    remoteAbinitRadioButton.setSelected(true);
                                    remoteAbinitRadioButtonActionPerformed(null);
                                } else if (attrValue.equals(remoteGatewayRadioButton.getText())) {
                                    remoteGatewayRadioButton.setSelected(true);
                                    remoteGatewayRadioButtonActionPerformed(null);
                                }
                            } else if (elemName.equals("remotelogin")) {
                                loginTextField.setText(attrValue);
                            } else if (elemName.equals("remotehost")) {
                                hostTextField.setText(attrValue);
                            } else if (elemName.equals("remotepwd")) {
                                pwdPasswordField.setText(attrValue);
                            } else if (elemName.equals("keyRemote")) {
                                if (attrValue.equals("1")) {
                                    jCB_useKey1.setSelected(true);
                                    jTF_key1.setEnabled(true);
                                    this.useKey1 = true;
                                    pwdLabel.setText("Passphrase");
                                } else {
                                    jCB_useKey1.setSelected(false);
                                    jTF_key1.setEnabled(false);
                                    this.useKey1 = false;
                                    pwdLabel.setText("Password");
                                }
                            } else if (elemName.equals("keyFileRemote")) {
                                jTF_key1.setText(attrValue);
                            } else if (elemName.equals("gatewaylogin")) {
                                gatewayLoginTextField.setText(attrValue);
                            } else if (elemName.equals("gatewayhost")) {
                                gatewayHostTextField.setText(attrValue);
                            } else if (elemName.equals("gatewaypwd")) {
                                gatewayPasswordField.setText(attrValue);
                            } else if (elemName.equals("keyGateway")) {
                                if (attrValue.equals("1")) {
                                    jCB_useKey2.setSelected(true);
                                    jTF_key2.setEnabled(true);
                                    this.useKey2 = true;
                                    pwdBFELabel.setText("Passphrase");
                                } else {
                                    jCB_useKey2.setSelected(false);
                                    jTF_key2.setEnabled(false);
                                    this.useKey2 = false;
                                    pwdBFELabel.setText("Password");
                                }
                            } else if (elemName.equals("keyFileGateway")) {
                                jTF_key2.setText(attrValue);
                            } else if (elemName.equals("mySimulationspath")) {
                                mySimulationsTextField.setText(attrValue);
                            } else if (elemName.equals("psppath")) {
                                pspPathTextField.setText(attrValue);
                            } else if (elemName.equals("abinitpath")) {
                                abinitPathTextField.setText(attrValue);
                            } else if (elemName.equals("abipypath")) {
                                abipyPathTextField.setText(attrValue);
                            } else if (elemName.equals("openfile")) {
                                openFileTextField.setText(attrValue);
                            } else if (elemName.equals("psp")) {
                                pspTextField.setText(attrValue);
                            } else {
                                printERR("Unknown element called " + elemName);
                            }
                        } else {
                            if (elemName.equals("psptable")) {
                                try {
                                    int npsp = Integer.parseInt(pspTextField.getText());

                                    if (npsp > 1000) {
                                        npsp = 1000;
                                        Object strTab[][] = new Object[npsp][4];
                                        for (int i = 0; i < npsp; i++) {
                                            strTab[i] = new Object[]{new Atom(), "", "", ""};
                                        }
                                        pspModel.setData(strTab);
                                        //znuclTable.setModel(znuclModel);
                                    } else {
                                        Object strTab[][] = new Object[npsp][4];
                                        for (int i = 0; i < npsp; i++) {
                                            strTab[i] = new Object[]{new Atom(), "", "", ""};
                                        }
                                        pspModel.setData(strTab);
                                        //znuclTable.setModel(znuclModel);
                                    }
                                } catch (Exception e) {
                                    //printERR(e.getMessage());
                                    pspModel.setData(null);
                                    //znuclTable.setModel(znuclModel);
                                }
                                List l3 = cur2.getChildren();
                                Iterator i3 = l3.iterator();
                                while (i3.hasNext()) {
                                    Element cur3 = (Element) i3.next();
                                    String elemName2 = cur3.getName();

                                    if (elemName2.equals("entry")) {
                                        int i = -1;
                                        String symbol = "";
                                        String psppath = "";
                                        String pspfile = "";
                                        String psptype = "";

                                        Attribute attr2;
                                        List lAttr2 = cur3.getAttributes();
                                        Iterator iAttr2 = lAttr2.iterator();

                                        while (iAttr2.hasNext()) {
                                            attr2 = (Attribute) iAttr2.next();
                                            String attrName = attr2.getName();
                                            String attrValue = attr2.getValue();

                                            if (attrName.equals("i")) {
                                                // TODO géréer une évantuelle exception
                                                i = Integer.parseInt(attrValue);
                                            } else if (attrName.equals("symbol")) {
                                                symbol = attrValue;
                                            } else if (attrName.equals("psppath")) {
                                                psppath = attrValue;
                                            } else if (attrName.equals("pspfile")) {
                                                pspfile = attrValue;
                                            } else if (attrName.equals("psptype")) {
                                                psptype = attrValue;
                                            } else {
                                                printERR("Unknown attribute called " + attrName);
                                            }
                                        }

                                        if (i != -1) {
                                            printDEB(i + " " + symbol + " " + psppath + " " + pspfile + " " + psptype);

                                            Atom atom = (Atom) pspTable.getValueAt(i, 0);

                                            AtomEditor.setAtom(atom, symbol, psptype, psppath, pspfile);
                                        } else {
                                            printERR("The xml entry (psptable) is not well defined !");
                                        }
                                    } else {
                                        printERR("Unknown element called " + elemName2);
                                    }
                                }
                            } else {
                                printERR("There is no attribute for " + elemName);
                            }
                        }
                    }
                } else // *************** inputfile ***************************************
                if (cur1.getName().equals("inputfile")) {
                    //printOUT(cur1.getName());
                } else {
                    printERR("Unknown configuration section " + cur1.getName());
                }
            }
        }
    }

    public void createFiletree() {
        if (localExec != null) {
            String path = mySimulationsTextField.getText();
            if (path.equals("")) {
                path = ".";
            }
            if (localAbinitRadioButton.isSelected()) {
                // Création de l'arborescence locale
                mkdir(path);
                mkdir(path + "/input");
                mkdir(path + "/output");
                mkdir(path + "/wholedata");
                mkdir(path + "/logfiles");
                mkdir(path + "/pseudopot");
                mkdir(path + "/scripts");
            } else {
                if (remoteExec != null && (remoteGatewayRadioButton.isSelected() || remoteAbinitRadioButton.isSelected())) {
                    mkdirR(path);
                    mkdirR(path + "/input");
                    mkdirR(path + "/output");
                    mkdirR(path + "/wholedata");
                    mkdirR(path + "/logfiles");
                    mkdirR(path + "/pseudopot");
                    mkdirR(path + "/scripts");
                    // Création de l'arborescence locale
                    mkdir(path);
                    mkdir(path + "/input");
                    mkdir(path + "/output");
                    mkdir(path + "/wholedata");
                    mkdir(path + "/logfiles");
                    mkdir(path + "/pseudopot");
                    mkdir(path + "/scripts");
                } else {
                    printERR("Please connect to an abinit Host before creating the remote filetree !");
                }
            }
        } else {
            printERR("Program bug in createFiletreeButtonActionPerformed (localExec == null) !");
        }
    }

    public void sendSIMButton_setEnabled(boolean enab) {
        if (MainFrame.autoTest) {
            // Do nothing in autotest mode
        } else {
            sendSIMButton.setEnabled(enab);
        }
    }

    public void editFile(final String fileName, boolean threaded) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                // ****************************************************************************
                // Tester l'existance du fichier
                if (!Utils.exists(fileName)) {
                    printERR("File " + fileName + " doesn't exist !");
                    return;
                } else {
                    if (Utils.osName().equals("Linux")) {
                        localCommand("gedit " + fileName);
                    } else if (Utils.osName().equals("Mac OS X")) {
                        localCommand("open -a textedit " + fileName);
                    } else if (Utils.osName().startsWith("Windows")) {
                        Utils.unix2dos(new File(fileName));
                        localCommand("notepad " + fileName);
                    } else {
                        printDEB("You must be in a UNIX platform to edit the input"
                                + "\nfile with a text editor from this GUI!");
                    }
                }
                // ****************************************************************************
            }
        };

        if (threaded) {
            Thread t = new Thread(r);
            t.start();
        } else {
            r.run();
        }
    }

    public void autoTestProc(boolean autoTest) {
        MainFrame.autoTest = autoTest;
        sendSIMButtonActionPerformed(null);
    }
    
    // For accessing some private variables from outside of abinitgui package
    
    public JRadioButton localAbinitRadioButton() {
        return localAbinitRadioButton;
    }
    
    public JRadioButton remoteGatewayRadioButton() {
        return remoteGatewayRadioButton;
    }
    
    public JRadioButton remoteAbinitRadioButton() {
        return remoteAbinitRadioButton;
    }
    
    public JTextField mySimulationsTextField() {
        return mySimulationsTextField;
    }
    
    public JTextField gatewayHostTextField() {
        return gatewayHostTextField;
    }
    
    public JTextField hostTextField() {
        return hostTextField;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JMenuItem LoadMenuItem;
    javax.swing.JButton SFTPButton;
    javax.swing.JButton SSH2ClientButton;
    javax.swing.JButton SubmissionEditButton1;
    javax.swing.JButton abinitPathButton;
    javax.swing.JLabel abinitPathPathLabel;
    javax.swing.JTextField abinitPathTextField;
    javax.swing.ButtonGroup abinixbuttonGroup;
    javax.swing.JLabel abipyPathPathLabel;
    javax.swing.JTextField abipyPathTextField;
    javax.swing.JMenuItem aboutMenuItem;
    javax.swing.JButton algoAndConvButton;
    javax.swing.JPanel basicsPanel;
    javax.swing.JScrollPane basicsScrollPane;
    javax.swing.JMenuItem clearOutMSGMenuItem;
    javax.swing.JPanel configPanel;
    javax.swing.JToggleButton connectionToggleButton;
    javax.swing.JButton createButton;
    javax.swing.JButton displayFileButton;
    javax.swing.JButton editPYDFT;
    javax.swing.JButton editScripts;
    javax.swing.JPanel emptyPanel;
    javax.swing.JMenu fileMenu;
    javax.swing.JTextField gatewayHostTextField;
    javax.swing.JPanel gatewayLoginPanel;
    javax.swing.JTextField gatewayLoginTextField;
    javax.swing.JPasswordField gatewayPasswordField;
    javax.swing.JButton geditButton;
    javax.swing.JButton geometryButton;
    javax.swing.JMenuItem getLogFileMenuItem;
    javax.swing.JMenuItem getOutputFileMenuItem;
    javax.swing.JMenu helpMenu;
    javax.swing.JLabel hostBFELabel;
    javax.swing.JLabel hostLabel;
    javax.swing.JTextField hostTextField;
    javax.swing.JPanel inputFilePanel;
    javax.swing.JTabbedPane inputFileTabbedPane;
    javax.swing.ButtonGroup inputFilebuttonGroup;
    javax.swing.JButton inputOutputButton;
    javax.swing.JCheckBox jCB_useKey1;
    javax.swing.JCheckBox jCB_useKey2;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JLabel jLabel6;
    javax.swing.JLabel jLabel9;
    javax.swing.JMenu jMenuClustepAndTB;
    javax.swing.JMenuItem jMenuItem1;
    javax.swing.JMenuItem jMenuItemClustep;
    javax.swing.JMenuItem jMenuItemTB;
    javax.swing.JPanel jPanel1;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JScrollPane jScrollPane2;
    javax.swing.JScrollPane jScrollPane3;
    javax.swing.JScrollPane jScrollPane5;
    javax.swing.JTextField jTF_key1;
    javax.swing.JTextField jTF_key2;
    javax.swing.JButton launchScript;
    javax.swing.JRadioButton localAbinitRadioButton;
    javax.swing.JLabel loginBFELabel;
    javax.swing.JLabel loginLabel;
    javax.swing.JPanel loginPanel;
    javax.swing.JTextField loginTextField;
    javax.swing.ButtonGroup lookAndFeelbuttonGroup;
    javax.swing.JMenuBar mainMenuBar;
    javax.swing.JTabbedPane mainTabbedPane;
    javax.swing.JLabel mySimulationsLabel;
    javax.swing.JTextField mySimulationsTextField;
    javax.swing.JButton openFileDialogButton;
    javax.swing.JLabel openFileLabel;
    javax.swing.JTextField openFileTextField;
    javax.swing.JButton openOutput;
    javax.swing.JButton openXMLFileDialogButton;
    javax.swing.JLabel openXMLFileLabel;
    javax.swing.JTextField openXMLFileTextField;
    javax.swing.JTextArea otherTextArea;
    javax.swing.JMenuItem outputMSGMenuItem;
    javax.swing.JMenu postProcMenu;
    javax.swing.JPanel postProcPanel;
    javax.swing.JLabel pspLabel;
    javax.swing.JLabel pspPathLabel;
    javax.swing.JTextField pspPathTextField;
    javax.swing.JTable pspTable;
    javax.swing.JScrollPane pspTableScrollPane;
    javax.swing.JTextField pspTextField;
    javax.swing.JLabel pwdBFELabel;
    javax.swing.JLabel pwdLabel;
    javax.swing.JPasswordField pwdPasswordField;
    javax.swing.JButton realAndRecipButton;
    javax.swing.JButton reloadScripts;
    javax.swing.JRadioButton remoteAbinitRadioButton;
    javax.swing.JRadioButton remoteGatewayRadioButton;
    javax.swing.JMenuItem saveAsMenuItem;
    javax.swing.JButton saveFileAsButton;
    javax.swing.JButton saveFileButton;
    javax.swing.JMenuItem saveMenuItem;
    javax.swing.JTable scriptArgTable;
    javax.swing.JTextArea scriptDescription;
    javax.swing.JList scriptList;
    javax.swing.JLabel scriptName;
    javax.swing.JTable scriptOutTable;
    javax.swing.JTextField scriptProgram;
    javax.swing.JScrollPane scriptScrollPane;
    javax.swing.JButton sendSIMButton;
    javax.swing.JButton testAnalyze;
    javax.swing.JButton testAnalyze1;
    javax.swing.JButton theoryButton;
    javax.swing.JRadioButton useCreIFRadioButton;
    javax.swing.JRadioButton useExtIFRadioButton;
    javax.swing.JMenu viewMenu;
    javax.swing.JButton wavefuncAndDensButton;
    javax.swing.JLabel whereIsAbinitLabel;
    javax.swing.ButtonGroup whereIsAbinitbuttonGroup;
    // End of variables declaration//GEN-END:variables

    void setSimulation(Simulation simulation) {
        printDEB("is setting simulation " + simulation);
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public AllInputVars getAllInputVars() {
        return allInputVars;
    }
}
