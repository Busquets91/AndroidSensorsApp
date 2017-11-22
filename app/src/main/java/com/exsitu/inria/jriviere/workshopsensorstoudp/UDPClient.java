package com.exsitu.inria.jriviere.workshopsensorstoudp;


import android.os.StrictMode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

public  class UDPClient {
    String name = "";
    int port = 8000;
    InetAddress adresse;
    DatagramSocket client = null;

    String IP = "192.168.43.122";//192.168.43.131
    OscP5 oscP5;
    NetAddress myRemoteLocation;
   static UDPClient udpClient = null;

    public static UDPClient getInstance(){
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (udpClient == null){
            udpClient = new UDPClient("UDPClient");
        }

        return udpClient;
    }


    private UDPClient(String pName){
        name = pName;
        oscP5 = new OscP5(this,port);
        myRemoteLocation = new NetAddress(IP,port);
    }

    public void run() {
        int nbre = 0;
        try {
            client = new DatagramSocket();
            //On crée notre datagramme
            adresse = InetAddress.getByName(IP);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }





    public void sendMessageUDP(final String s){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    byte[] buffer = s.getBytes();
                    System.out.println();

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, port);

                    //On lui affecte les données à envoyer
                    packet.setData(buffer);

                    //On envoie au serveur
                    client.send(packet);
                    System.out.println("envoie");


                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }




        public void sendMessageOSC(final String s) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                OscMessage myMessage = new OscMessage(s);
                //myRemoteLocation = new NetAddress(IP,port);
                 /* send the message */
                oscP5.send(myMessage, myRemoteLocation);
                System.out.println("Sending data"  );
            }
        });
        t.start();

    }



    public void setPort(int port){
        this.port = port;
        myRemoteLocation = new NetAddress(IP,port);

    }

    public int getPort() {
        return this.port;
    }

    public void setIP(String s) {

        this.IP = s;
        myRemoteLocation = new NetAddress(s,port);

    }

    public String getIP() {
        return this.IP;
    }
}