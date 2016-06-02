package cz.mersa;

import net.sf.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

    DatagramSocket serverSocket;
    static int DELAY = 100;
    public static void main(String[] args) throws IOException {
        Main m = new Main();
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
            String sentence = new String(receivePacket.getData(),0,receivePacket.getLength());
            System.out.println("RECEIVED: " + sentence + ".");

            if (! sentence.equals("RCT_DISCOVERY")) continue;

            // InetAddress IPAddress = receivePacket.getAddress();
            // int port = receivePacket.getPort();
            serverLoop(receivePacket.getAddress(),receivePacket.getPort());
        }
    }

    void serverLoop(InetAddress addr, int port) {
        boolean cont = true;
        int cnt = 0;

        while (cont) {
            JSONObject jo = new JSONObject().element("t", System.currentTimeMillis());
            Object[] arr = new Object[]{
                    new JSONObject().accumulate("i","L").accumulate("u","m").accumulate("v", Math.sin(System.currentTimeMillis())),
                    new JSONObject().accumulate("i","S1").accumulate("v",cnt++)
            };
            jo.accumulate("s", arr);
            System.out.println(jo.toString());
            try {

                // send it
                DatagramPacket packet = new DatagramPacket(jo.toString().getBytes(), jo.toString().length(), addr, port);
                serverSocket.send(packet);

                // sleep for a while
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
                e.printStackTrace();
                cont = false;
            }
        }
        serverSocket.close();

    }
}
