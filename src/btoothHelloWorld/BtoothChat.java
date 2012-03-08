package btoothHelloWorld;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

public class BtoothChat extends MIDlet implements CommandListener{

	private Display display;
    private List list;
 
 
    public BtoothChat() {
 
        display = Display.getDisplay(this);
        list = new List("Select", List.EXCLUSIVE);
        list.append("Server", null);
        list.append("Client", null);
 
        Command cmd_ok = new Command("OK", Command.OK, 1);
 
        list.addCommand(cmd_ok);
        list.setCommandListener(this);
    }
 
    public void startApp() {
        display.setCurrent(list);
    }
 
    public void pauseApp() {
    }
 
    public void destroyApp(boolean unconditional) {
    }
 
    public void commandAction(Command command, Displayable d) {
 
        if (command.getCommandType()==Command.OK) {
            switch (list.getSelectedIndex()){
                case 0: {
 
                    new BluetoothServer(this); //starting instance of a server
 
                    break;
                }
 
                case 1: {
 
                    new BluetoothClient(this); //starting instance of a client
 
                    break;
                }
            }
        }
 
    }
    protected void setAlert(String info) {
        Alert a = new Alert("INFO");
        a.setString(info);
        a.setTimeout(Alert.FOREVER);
        display.setCurrent(a);
    }

}
