package com.yurpetr.irrenamer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class App {

    public enum DragState {

        Waiting, Accept, Reject
    }

    JTextField orderNumber;

    public static void main(String[] args) {

        new App();

    }

    public App() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                        | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame frame = new JFrame();
                frame.setLayout(new BorderLayout());
                frame.setTitle("IR Files Renamer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 250);
                frame.setResizable(false);
                frame.setLocationRelativeTo(null);
                frame.setAlwaysOnTop(true);
                frame.setVisible(true);
                JPanel labelPanel = new JPanel();
                labelPanel.setLayout(new GridLayout(0, 2));
                JLabel numberLabel = new JLabel("Order Number: ", SwingConstants.CENTER);
                numberLabel.setFont(new Font("Arial", 0, 20));
                labelPanel.add(numberLabel);
                orderNumber = new JTextField("Enter order number");
                orderNumber.setToolTipText("Enter order number");
                orderNumber.setForeground(Color.GRAY);
                orderNumber.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (orderNumber.getText().equals("Enter order number")) {
                            orderNumber.setText("");
                            orderNumber.setForeground(Color.BLACK);
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        if (orderNumber.getText().isEmpty()) {
                            orderNumber.setForeground(Color.GRAY);
                            orderNumber.setText("Enter order number");
                        }
                    }
                });

                orderNumber.setFont(new Font("Arial", 0, 20));
                labelPanel.add(orderNumber);
                frame.add(labelPanel, BorderLayout.NORTH);
                frame.add(new DropPane(), BorderLayout.SOUTH);
                frame.pack();
            }
        });
    }

    class DropPane extends JPanel {

        public DropPane() {
            setLayout(new GridLayout(2, 2, 5, 5));

            Font               font  = new Font("Arial", 0, 25);
            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);

            DropArea dropBeauty = new DropArea("beauty JPG", font, align, "beauty.jpg");
            DropArea dropLight  = new DropArea("light JPG", font, align, "combined_lighting.jpg");
            DropArea dropMatte  = new DropArea("cryptomatte EXR", font, align, "cryptomatte.exr");
            DropArea dropUV     = new DropArea("uv EXR", font, align, "uv.exr");

            add(dropBeauty);
            add(dropLight);
            add(dropMatte);
            add(dropUV);

        }

    }

    class DropArea extends JTextPane implements DropTargetListener {

        /**
         * 
         */

        private String    fileName;
        private DragState state = DragState.Waiting;

        protected DropArea(String string, Font font, SimpleAttributeSet align, String fileName) {
            super();
            this.fileName = fileName;
            new DropTarget(this, this);
            setDragEnabled(true);
            setEditable(false);
            setFocusable(false);
            setHighlighter(null);
            setBorder(BorderFactory.createDashedBorder(Color.GRAY, 4f, 6.5f, 2f, true));
            setFont(font);
            setForeground(Color.GRAY);
            setText("Drop\n" + string + "\nhere");
            StyledDocument style = getStyledDocument();
            style.setParagraphAttributes(0, style.getLength(), align, false);
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            state = DragState.Reject;
            Transferable t = dtde.getTransferable();
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                try {
                    Object td = t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (td instanceof List) {
                        state = DragState.Accept;
                        for (Object value : ((List) td)) {
                            if (value instanceof File) {
                                File   file = (File) value;
                                String name = file.getName().toLowerCase();
                                if ((fileName.endsWith(".exr") && !name.endsWith(".exr"))
                                        || (fileName.endsWith(".jpg") && !name.endsWith(".jpg"))) {
                                    state = DragState.Reject;
                                    break;
                                }
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (state == DragState.Accept) {
                dtde.acceptDrag(DnDConstants.ACTION_NONE);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {

        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {

        }

        @Override
        public void dragExit(DropTargetEvent dte) {

        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            if (!orderNumber.getText().isEmpty() && orderNumber.getText().matches("[\\d]+")) {
                String number = orderNumber.getText() + "_";
                state = DragState.Reject;
                Transferable t = dtde.getTransferable();
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Object td = t.getTransferData(DataFlavor.javaFileListFlavor);
                        if (td instanceof List) {
                            state = DragState.Accept;
                            for (Object value : ((List) td)) {
                                if (value instanceof File) {
                                    File file    = (File) value;
                                    File newFile = new File(file.getParent(), number + fileName);
                                    if (newFile.exists()) {
                                        newFile.delete();
                                    }
                                    file.renameTo(newFile);

                                }
                            }

                        }

                    } catch (UnsupportedFlavorException e) {
                        System.out.println("unsupported flavor exception");
                    } catch (IOException e) {
                        System.out.println("cant read files");
                    }

                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill correct order number",
                        "Order Number ERROR", JOptionPane.ERROR_MESSAGE);
            }

        }

    }

}
