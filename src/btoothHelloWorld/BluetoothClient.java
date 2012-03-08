package btoothHelloWorld;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
 

public class BluetoothClient implements Runnable {
 
    private InquiryListener inq_listener;
    private ServiceListener serv_listener;
    private boolean listening = true;
    private BtoothChat midlet;
    private String deviceName;
    private L2CAPConnection con;
 
    /** Creates a new instance of BluetoothClient */
    public BluetoothClient(BtoothChat midlet){
        this.midlet = midlet;
        Thread t = new Thread(this);
        t.start();
    }
    
    public void run() {
        System.out.println("Starting client - please wait...");
        try {
            LocalDevice local_device = LocalDevice.getLocalDevice();
            DiscoveryAgent disc_agent = local_device.getDiscoveryAgent();
            local_device.setDiscoverable(DiscoveryAgent.LIAC);
            inq_listener = new InquiryListener();
            synchronized(inq_listener)	{
                disc_agent.startInquiry(DiscoveryAgent.LIAC, inq_listener);
                try {
                	inq_listener.wait(); 
                } catch(InterruptedException e){}
            }
 
            Enumeration devices = inq_listener.cached_devices.elements();
 
            UUID[] u = new UUID[]{new UUID( "00000000000010008000006057028A06", false )};
            int attrbs[] = { 0x0100 };
            serv_listener = new ServiceListener();
            while( devices.hasMoreElements() ) {
                synchronized(serv_listener)	{
                    disc_agent.searchServices(attrbs, u, (RemoteDevice)devices.nextElement(), serv_listener);
                    try {serv_listener.wait();} catch(InterruptedException e){}
                }
            }
        } catch (BluetoothStateException e) {System.out.println(e);}
 
        if (serv_listener.service!=null){
            try {
                String url;
                url = serv_listener.service.getConnectionURL(0, false);
                deviceName = LocalDevice.getLocalDevice().getFriendlyName();
                con = (L2CAPConnection) Connector.open( url );
                send("Hello server, my name is: " + getName());
 
                byte[] b = new byte[1000];
                while (listening) {
                    if (con.ready()){
                        con.receive(b);
                        String s = new String(b, 0, b.length);
                        System.out.println("Received from server: " + s.trim());
                        midlet.setAlert(s.trim());
                        listening = false;
                    }
                }
            } catch (IOException g) {System.out.println(g);}
        }
    }
    private void send(String s){
        byte[] b = s.getBytes();
        try {
            con.send(b);
        } catch(IOException e){
            System.out.println(e);
        }
    }
    private String getName(){
        return deviceName;
    }
}


class InquiryListener implements DiscoveryListener {
    public Vector cached_devices;
    public InquiryListener() {
        cached_devices = new Vector();
    }
 
    public void deviceDiscovered( RemoteDevice btDevice, DeviceClass cod )	{
        int major = cod.getMajorDeviceClass();
        if( ! cached_devices.contains( btDevice ) )	{
            cached_devices.addElement( btDevice );
        }
    }
 
    public void inquiryCompleted( int discType )	{
        synchronized(this){	this.notify(); }
    }
 
    public void servicesDiscovered( int transID, ServiceRecord[] servRecord )	{}
    public void serviceSearchCompleted( int transID, int respCode )	{}
}
 

class ServiceListener implements DiscoveryListener	{
    public ServiceRecord service;
    public ServiceListener()	{	}
 
    public void servicesDiscovered( int transID, ServiceRecord[] servRecord )	{
        service = servRecord[0];
        System.out.println("foundService");
    }
 
    public void serviceSearchCompleted( int transID, int respCode )	{
        synchronized( this ){	this.notify();}
    }
 
    public void deviceDiscovered( RemoteDevice btDevice, DeviceClass cod ){}
    public void inquiryCompleted( int discType ){}
}
