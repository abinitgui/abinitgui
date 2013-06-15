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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class ScriptTable extends JTable
{
    private String curPath = ".";
    
    @Override
    public TableCellEditor getCellEditor(int row, int column)
    {
        ScriptArgument arg = (ScriptArgument)(getModel().getValueAt(row,0));
        
        if(arg.type.toUpperCase().contains("FILE"))
        {
            return new FileEditor();
        }
        else if(arg.type.toUpperCase().equals("BOOLEAN"))
        {
            return new BooleanEditor();
        }
        
        return getDefaultEditor(getModel().getValueAt(row, column).getClass());
    }
    
    private class BooleanEditor extends AbstractCellEditor
        implements TableCellEditor,
        ActionListener
    {
        private JButton but;
        private String status;
        private boolean stat;
        
        public BooleanEditor()
        {
            but = new JButton();
            but.addActionListener(this);
            but.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            but.setBackground(Color.darkGray);
        }
        
        @Override
        public Object getCellEditorValue()
        {
            return status;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            
            status = (String)value;
            stat = (status.equals("True"));
            return but;
        }

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            stat = !stat;
            
            if(stat)
            {
                status = "True";
            }
            else
            {
                status = "False";
            }
            
            fireEditingStopped();
        }
    }
    
    private class FileEditor extends AbstractCellEditor
        implements TableCellEditor,
        ActionListener
    {
        private JButton but;
        private String fileName;
                
        public FileEditor()
        {
            but = new JButton();
            but.addActionListener(this);
            but.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            but.setBackground(Color.darkGray);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            fileName = (String)value;
            //button.setText(currentAtom.getSymbol());
            return but;
        }

        @Override
        public Object getCellEditorValue() {
            return fileName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            
            JFileChooser fc = new JFileChooser(curPath);
            File currDir = new File(".");
            String currPath = currDir.getAbsolutePath();
            String basePath = basePath = currPath.replace("\\", "/").replace(".", "");
            
            int returnVal = fc.showOpenDialog(ScriptTable.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                curPath = file.getParent();
                //This is where a real application would open the file.
                System.out.println("Opening: " + file.getName() + ".");
                String relPath = file.getAbsolutePath().replace("\\", "/").replace(basePath, "./");
                this.fileName = relPath;
            } else {
                System.out.println("Open command cancelled by user.");
            }
            
            fireEditingStopped();
        }

    }

}
