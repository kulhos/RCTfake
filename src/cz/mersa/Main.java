package cz.mersa;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        {
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            DatagramSocket serverSocket = new DatagramSocket(995);

            while (true) {

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData(),0, receivePacket.getLength());
                System.out.println("RECEIVED: " + sentence +".");

                if (sentence.equals("RCT_DISCOVERY")) {
                    System.out.println("Got it");
                    InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    long sTime = System.currentTimeMillis();

                    while (true) {

                        JSONObject o = new JSONObject();
                        o.put("t", (System.currentTimeMillis() - sTime));

                        JSONArray s = new JSONArray();
                        JSONObject l = new JSONObject();
                        l.put("i","L");
                        l.put("u","m");
                        l.put("v",(System.currentTimeMillis()/10000));
                        s.add(l);

                        o.put("s", s);

                        // {"i":"L","u":"m","v":mmm.m},	// ujeta draha v metrech
                        // {"i":"Ub","u":"V","v":uu.u},	// napeti baterie;
                        sendData = o.toJSONString().getBytes();
                        DatagramPacket sendPacket =
                                new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);
                    }
                }
            }
        }
    }
}
