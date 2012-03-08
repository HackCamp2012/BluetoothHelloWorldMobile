package btoothHelloWorld;
 
import java.io.IOException;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.L2CAPConnectionNotifier;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
 

public class BluetoothServer implements Runnable {
 
    private boolean listening = true;
    private LocalDevice local_device;
    private BtoothChat midlet;
    private String deviceName;
    private L2CAPConnection con;
 
    /** Creates a new instance of BluetoothServer */
    public BluetoothServer(BtoothChat midlet) {
        this.midlet = midlet;
        Thread t = new Thread(this);
        t.start();
    }
 
    public void run(){
        System.out.println("Starting server - please wait...");
 
        try {
            local_device = LocalDevice.getLocalDevice();
            //DiscoveryAgent disc_agent = local_device.getDiscoveryAgent();
            local_device.setDiscoverable(DiscoveryAgent.LIAC);
            String service_UUID = "00000000000010008000006057028A06";
            deviceName = local_device.getFriendlyName();
            String url = "btl2cap://localhost:" + service_UUID + ";name=" + deviceName;
 
            L2CAPConnectionNotifier notifier = (L2CAPConnectionNotifier)Connector.open(url);
            con = notifier.acceptAndOpen();
 
            while (listening) {
                if (con.ready()){
                    byte[] b = new byte[1000];
                    con.receive(b);
                    String s = new String(b, 0, b.length);
                    System.out.println("Recieved from client: " + s.trim());
                    midlet.setAlert(s.trim());
                    send("Hello client, my name is: " + getName());
                    listening=false;
                }
            }
        } catch(BluetoothStateException e){
        	System.out.println(e);
        } catch(IOException f){
        	System.out.println(f);
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