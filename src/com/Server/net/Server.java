/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Server.net;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import com.net.Message;
import java.nio.channels.FileChannel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javenue.csv.Csv;
/**
 *
 * @author Александр
 */
public class Server extends javax.swing.JFrame implements Runnable {
    
    private Selector selector;
    private ServerSocketChannel ssc;
    private byte[] buffer = new byte[4096];
    private Map<SelectionKey, ByteBuffer> connections = new HashMap<SelectionKey, ByteBuffer>();
    private Map<String, Socket> clients = new HashMap<String, Socket>();
    private int port = 8189;
    private SelectionKey selK = null;
    private String name = null;
    private boolean write = false;
    private Image root = Toolkit.getDefaultToolkit().createImage("images/server.png");
    private ImageIcon save_as = new ImageIcon("images/save_as.png");
    private ImageIcon exit = new ImageIcon("images/exit.png");
    private static int numb = 0;
    private FileServer FS;
    private int Fileport = 8190;

    /**
     * Creates new form Server
     */
    public Server(int port) {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(port));
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            FS = new FileServer(Fileport, this);
            new Thread(this).start();
        } catch (IOException ex) {
            System.out.println("Address already in use");
            JOptionPane.showMessageDialog(rootPane, "Address already in use", "Server", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem2 = new javax.swing.JMenuItem();

        jFileChooser1.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooser1.setFileFilter(new CustomFilter());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setIconImage(root);

        jTable1.setBackground(new java.awt.Color(240, 240, 240));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "№", "In/Out/Error", "Type", "From", "Whom/Clients", "Text", "Info", "Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jMenu1.setText("File");

        jMenuItem1.setIcon(save_as);
        jMenuItem1.setText("Save as...");
        jMenuItem1.setName(""); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);
        jMenu1.add(jSeparator1);

        jMenuItem2.setIcon(exit);
        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        try {
            SaveAs();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
             */
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>
        /* Create and display the form */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Server s = new Server(8189);
//            s.setVisible(true);
//            s.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        while (true) {
            try {
                if (selector.isOpen()) {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (SelectionKey sk:keys) {
                        if (!sk.isValid()) {
                            continue;
                        }
                        if (sk.isAcceptable()) {
                            ServerSocketChannel ssca = (ServerSocketChannel) sk.channel();
                            SocketChannel sc = ssca.accept();
                            sc.configureBlocking(false);
                            SelectionKey scr = sc.register(selector, SelectionKey.OP_READ);
                            ByteBuffer byteB = ByteBuffer.wrap(buffer);
                            connections.put(scr, byteB);
                        } else if (sk.isReadable()) {
                            SocketChannel sc = (SocketChannel) sk.channel();
                            int read;
                            ByteBuffer byteB = connections.get(sk);
                            byteB.clear();
                            try {
                                read = sc.read(byteB);
                            } catch (IOException ex) {
                                closeChannel(sk);
                                break;
                            }
                            if (read == -1) {
                                addRow(new Vector(Arrays.asList(getNumb(), "Error", null, null, null, "Empty text", ((SocketChannel)sk.channel()).socket().toString(), getDate())));
                                closeChannel(sk);
                                break;
                            } else if(read > 0) {
                                decode(sk, byteB);
                            }
                        } else if (sk.isWritable()) {
                            ByteBuffer byteB = connections.get(sk);
                            SocketChannel sc = (SocketChannel) sk.channel();
                            try {
                                int result = sc.write(byteB);
                                byte[] byteb = byteB.array();
                                Message m = null;
                                try {
                                    m = (Message)deserialize(byteb);
                                } catch (IOException ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (ClassNotFoundException ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                addRow(new Vector(Arrays.asList(getNumb(), "Out", m.type, m.from, m.clients, m.text, ((SocketChannel)sk.channel()).socket().toString(), getDate())));
                                if (result == -1) {
                                    addRow(new Vector(Arrays.asList(getNumb(), "Error", null, null, null, null, ((SocketChannel)sk.channel()).socket().toString(), getDate())));
                                    closeChannel(sk);
                                }
                            } catch (IOException ex) {
                                addRow(new Vector(Arrays.asList(getNumb(), "Error", null, null, null, ex, ((SocketChannel)sk.channel()).socket().toString(), getDate())));
                                closeChannel(sk);
                            }
                            if (byteB.position() == byteB.limit()) {
                                sk.interestOps(SelectionKey.OP_READ);
                            }
                            if (selK!=null) write = true;
                        }
                    }
                    keys.clear();
                    if (selK!=null && write) {
                        ByteBuffer byteB = connections.get(selK);
                        SocketChannel socket = (SocketChannel) selK.channel();
                        Message msg = new Message((byte)4, name);
                        byte[] bt = serialize(msg);
                        byteB.clear();
                        byteB.put(bt);
                        byteB.flip();
                        settowriteB(socket.socket(), byteB);
                        write = false;
                        selK = null;
                    }
                } else break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeChannel(SelectionKey sk) {
        connections.remove(sk);
        SocketChannel sc = (SocketChannel) sk.channel();
        if (sc.isConnected()) {
            try {
                sc.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sk.cancel();
    }
    
    private void decode(SelectionKey sk, ByteBuffer byteB) {
        byte[] byteb = byteB.array();
        Message m = null;
        try {
            m = (Message)deserialize(byteb);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        switch (m.type) {
            case 0: {
                boolean dubl = false;
                Message msg = null;
                Set<Entry<String, Socket>> clientsMap = clients.entrySet();
                for (Entry<String, Socket> client:clientsMap) {
                    String name = client.getKey();
                    if (name.equals(m.text)) {
                        msg = new Message((byte)0, "Dublicate Name!");
                        dubl = true;
                        System.out.println("Dublicate name!");
                        break;
                    }
                }
                try {
                    if (!dubl) {
                        Set<String> s = clients.keySet();
                        msg = new Message((byte)0, new Vector(Arrays.asList(s.toArray())), "OK");
                        System.out.println("User " + m.text + " connected!");
                        name = m.text;
                        selK = sk;
                    }
                    byte[] bt = serialize(msg);
                    byteB.clear();
                    byteB.put(bt);
                    byteB.flip();
                    settowrite(Arrays.asList(((SocketChannel) sk.channel()).socket()), byteB.position(), byteB.limit());
                    if (!dubl) {
                        clients.put(m.text, ((SocketChannel) sk.channel()).socket());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            case 1: {
                try {
                    byte[] bt = serialize(m);
                    byteB.clear();
                    byteB.put(bt);
                    byteB.flip();
                    List<Socket> l = new ArrayList<Socket>();
                    for (int i=0; i<m.clients.size(); i++) {
                        l.add(clients.get(m.clients.get(i)));
                    }
                    settowrite(l, byteB.position(), byteB.limit());
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            case 5: {
                try {
                    System.out.println("User " + m.text + " disconnected!");
                    Message msg = new Message((byte)5, m.text);
                    byte[] bt = serialize(msg);
                    byteB.clear();
                    byteB.put(bt);
                    byteB.flip();
                    settowriteB(((SocketChannel) sk.channel()).socket(), byteB);
                    clients.remove(m.text);
                    FS.removeClient(m.text);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
        addRow(new Vector(Arrays.asList(getNumb(), "In", m.type, m.from, m.clients, m.text, ((SocketChannel)sk.channel()).socket().toString(), getDate())));
    }
    
    private void settowrite(List<Socket> l, int pos, int lim) {
        Set<Map.Entry<SelectionKey, ByteBuffer>> entries = connections.entrySet();
        for (Map.Entry<SelectionKey, ByteBuffer> entry:entries) {
            SocketChannel s = (SocketChannel)entry.getKey().channel();
            if (l.contains(s.socket())) {
                SelectionKey selK = entry.getKey();
                selK.interestOps(SelectionKey.OP_WRITE);
                ByteBuffer entryB = entry.getValue();
                entryB.position(pos);
                entryB.limit(lim);
            }
        }
    }

    private void settowriteB(Socket sock, ByteBuffer b) {
        Set<Map.Entry<SelectionKey, ByteBuffer>> entries = connections.entrySet();
        for (Map.Entry<SelectionKey, ByteBuffer> entry:entries) {
            SocketChannel s = (SocketChannel)entry.getKey().channel();
            if (!sock.equals(s.socket())) {
                SelectionKey selK = entry.getKey();
                selK.interestOps(SelectionKey.OP_WRITE);
                ByteBuffer entryB = entry.getValue();
                entryB.position(b.position());
                entryB.limit(b.limit());
            }
        }
    }

    public void addRow(Vector v) {
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        dtm.addRow(v);
        numb++;
    }
    
    public String getDate() {
        Date d = new Date();
        DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
        return df.format(d);
    }
    
    public int getNumb() {
        return numb;
    }
    
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
    
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    
    public void SaveAs() throws FileNotFoundException, IOException {
        String cd,fn;
        File file;
        int save = jFileChooser1.showSaveDialog(this);
        if (jFileChooser1.getSelectedFile() == null || save==JFileChooser.CANCEL_OPTION) {
            return;
        }
        file = jFileChooser1.getSelectedFile();
        Csv.Writer writer = new Csv.Writer(file+".csv").delimiter(',');
        for (int i=0; i<jTable1.getRowCount(); i++) {
            for (int j=0; j<jTable1.getColumnCount(); j++) {
                if (jTable1.getValueAt(i, j)==null) {
                    writer.value("");
                } else {
                    writer.value(jTable1.getValueAt(i, j).toString());
                }
            }
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }
    
    class CustomFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File file) {
            // Allow only directories, or files with ".txt" extension
            return file.isDirectory() || file.getAbsolutePath().endsWith(".csv");
        }
        @Override
        public String getDescription() {
            // This description will be displayed in the dialog,
            // hard-coded = ugly, should be done via I18N
            return "Table (*.csv)";
        }
    } 
}