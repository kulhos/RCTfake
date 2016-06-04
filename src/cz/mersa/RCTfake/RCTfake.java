package cz.mersa.RCTfake;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class RCTfake {

    static int DELAY = 100;
    DatagramSocket serverSocket;

    Executor executor = Executors.newSingleThreadExecutor();

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
                UDpServer s = new UDpServer(serverSocket, receivePacket);
                executor.execute(s);
            }
        }
    }

}

    class UDpServer implements Runnable {
        DatagramSocket socket;
        private InetAddress addr;
        private int port;

        public UDpServer(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.addr = packet.getAddress();
            this.port = packet.getPort();
        }

    public void run() {
        boolean cont = true;
        int cnt = 0;

        System.out.println("Starting thread " + Thread.currentThread().getName());

        while (cont) {
            JSONObject jo = new JSONObject(); jo.put("t", System.currentTimeMillis());
            JSONArray arr = new JSONArray();
            JSONObject dist = new JSONObject(); dist.put("i","L"); dist.put("u","m"); dist.put("v", Math.sin(System.currentTimeMillis()));
            JSONObject i = new JSONObject(); i.put("i","S1"); i.put("v",cnt++);
            arr.add(dist);
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
        }
        socket.close();

    }
}
