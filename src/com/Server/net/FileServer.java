/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Server.net;

import com.net.FileMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
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
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Александр
 */
class FileServer implements Runnable {

    private Selector selector;
    private ServerSocketChannel ssc;
    private Map<SelectionKey, ByteBuffer> connections = new HashMap<SelectionKey, ByteBuffer>();
    private Map<String, Socket> clients = new HashMap<String, Socket>();
    private int write = 0;
    private Server server;

    FileServer(int port, Server server) {
        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(port));
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            this.server = server;
            new Thread(this).start();
        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
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
                            ByteBuffer byteB = ByteBuffer.allocate(1024*10);
                            connections.put(scr, byteB);
                       } else if (sk.isReadable() && write==0) {
                            SocketChannel sc = (SocketChannel) sk.channel();
                            int read;
                            ByteBuffer byteB = connections.get(sk);
                            try {
                                read = sc.read(byteB);
                            } catch (IOException ex) {
                                closeChannel(sk);
                                break;
                            }
                            if (read == -1) {
                                server.addRow(new Vector(Arrays.asList(server.getNumb(), "Error", null, null, null, null, ((SocketChannel)sk.channel()).socket().toString(), server.getDate())));
                                closeChannel(sk);
                                break;
                            } else if(read > 0) {
                                if (byteB.position() == 10240 || byteB.position() == 2048) {
                                    byteB.flip();
                                    byteB.mark();
                                    decode(sk, byteB);
                                } else {
                                    byteB.compact();
                                }
                            }
                        } else if (sk.isWritable()) {
                            ByteBuffer byteB = connections.get(sk);
                            SocketChannel sc = (SocketChannel) sk.channel();
                            try {
                                int result = sc.write(byteB);
                                if (result == -1) {
                                    server.addRow(new Vector(Arrays.asList(server.getNumb(), "Error", null, null, null, null, ((SocketChannel)sk.channel()).socket().toString(), server.getDate())));
                                    closeChannel(sk);
                                }
                            } catch (IOException ex) {
                                server.addRow(new Vector(Arrays.asList(server.getNumb(), "Error", null, null, null, ex, ((SocketChannel)sk.channel()).socket().toString(), server.getDate())));
                                closeChannel(sk);
                            }
                            if (byteB.position() == byteB.limit()) {
                                sk.interestOps(SelectionKey.OP_READ);
                                byteB.clear();
                                write--;
                            }
                        }
                    }
                    keys.clear();
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
    
    public void removeClient(String sa) {
        clients.remove(sa);
    }
    
    private void decode(SelectionKey sk, ByteBuffer byteB) {
        byteB.position(0);
        int msgS = byteB.getInt();
        byte[] b = new byte[msgS];
        byteB.get(b);
        FileMessage msg = null;
        try {
            msg = (FileMessage) deserialize(b);
        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        switch(msg.type) {
            case 2: {
                clients.put(msg.from, ((SocketChannel)sk.channel()).socket());
                server.addRow(new Vector(Arrays.asList(server.getNumb(), "In", msg.type, msg.from, msg.clients, msg.file, ((SocketChannel)sk.channel()).socket().toString(), server.getDate())));
                break;
            }
            case 3: {
                byteB.reset();
                List<Socket> l = new ArrayList<Socket>();
                for (int i=0; i<msg.clients.size(); i++) {
                    l.add(clients.get(msg.clients.get(i)));
                    write++;
                }
                settowrite(l, byteB);
                if (msg.end) {
                    server.addRow(new Vector(Arrays.asList(server.getNumb(), "In/Out", msg.type, msg.from, msg.clients, msg.file, ((SocketChannel)sk.channel()).socket().toString(), server.getDate())));
                }
                break;
            }
        }
        byteB.clear();
    }

    private void settowrite(List<Socket> l, ByteBuffer byteB) {
        Set<Map.Entry<SelectionKey, ByteBuffer>> entries = connections.entrySet();
        for (Map.Entry<SelectionKey, ByteBuffer> entry:entries) {
            SocketChannel s = (SocketChannel)entry.getKey().channel();
            if (l.contains(s.socket())) {
                SelectionKey selK = entry.getKey();
                selK.interestOps(SelectionKey.OP_WRITE);
                ByteBuffer entryB = entry.getValue();
                entryB.clear();
                entryB.put(byteB);
                entryB.flip();
            }
        }
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
    
}
