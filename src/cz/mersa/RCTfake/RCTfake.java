package cz.mersa.RCTfake;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class RCTfake {

    public static final long CLIENT_TIMEOUT = 10; // 10 seconds heartbeat
    static int DELAY = 100;
    DatagramSocket serverSocket;

    Executor executor = Executors.newSingleThreadExecutor();
    Table<InetAddress, Integer, UDPServer> servers = HashBasedTable.create();

    public static void main(String[] args) throws IOException {
        RCTfake m = new RCTfake();
        m.run(args);
    }

    public void run(String[] args) throws IOException {
        // serverSocket = new DatagramSocket(9876);
        serverSocket = new DatagramSocket(995);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        Boolean cont = true;
        while (cont) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("RECEIVED: " + sentence + ".");

            if (sentence.equals("RCT_DISCOVERY")) {
                UDPServer s = new UDPServer(serverSocket, receivePacket);
                servers.put(receivePacket.getAddress(), receivePacket.getPort(), s);
                executor.execute(s);
            }

            if (sentence.equals("HRTBT")) {
                UDPServer s = servers.get(receivePacket.getAddress(), receivePacket.getPort());
                if (s != null) {
                    System.out.println("Heartbeat received");
                    s.setAlive();
                }
            }
        }
    }

}

    class UDPServer implements Runnable {
        DatagramSocket socket;
        private InetAddress addr;
        private int port;
        private long lastAlive;

        public UDPServer(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.addr = packet.getAddress();
            this.port = packet.getPort();
            setAlive();
        }

    public void run() {
        boolean cont = true;
        int cnt = 0;

        System.out.println("Starting thread " + Thread.currentThread().getName());

        while (cont) {
            JSONObject jo = new JSONObject(); jo.put("t", System.currentTimeMillis());
            JSONArray arr = new JSONArray();
            JSONObject dist = new JSONObject(); dist.put("i","L"); dist.put("u","m"); dist.put("v", Math.sin(System.currentTimeMillis()));
            arr.add(dist);
            JSONObject i = new JSONObject(); i.put("i","Ub"); i.put("u","V"); i.put("v",24 - (cnt++/10));
            arr.add(i);
            JSONObject b1 = new JSONObject(); b1.put("i","Ib"); i.put("u","A"); i.put("v",3);
            arr.add(i);
            jo.put("s", arr);
            System.out.println(jo.toString());
            try {

                // send it
                DatagramPacket packet = new DatagramPacket(jo.toString().getBytes(), jo.toString().length(), addr, port);
                socket.send(packet);

                // sleep for a while
                try {
                    Thread.sleep(RCTfake.DELAY);
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
                e.printStackTrace();
                cont = false;
            }

            if (! isAlive()) {
                System.out.println("Client not responding, closing stream");
                cont=false;
            }
        }

    }

        public void setAlive() {
            this.lastAlive = System.currentTimeMillis();
        }

        public boolean isAlive() {
            if ((System.currentTimeMillis() - this.lastAlive)/1000 > RCTfake.CLIENT_TIMEOUT) return false;
            return true;
        }
}
