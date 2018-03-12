/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PTZ;

import java.net.ConnectException;
import java.util.Date;
import javax.xml.soap.SOAPException;

import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.PtzDevices;
import org.onvif.ver10.schema.FloatRange;
import org.onvif.ver10.schema.Profile;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 * @author Capital
 */
public class NewMain {  
    private static final String hostIP = "192.168.2.54:8080";
    
    public static void main(String[] args) {
        try {
            // Replace these values with your camera data!
            //OnvifDevice(String hostIp) : OnvifDevice -- Less functionallity 
            OnvifDevice nvt = new OnvifDevice(hostIP, "admin", "admin");
            Date nvtDate = nvt.getDevices().getDate();
            System.out.println(new SimpleDateFormat().format(nvtDate));

            List<Profile> profiles = nvt.getDevices().getProfiles();
            String profileToken = profiles.get(0).getToken();
            System.out.println("Snapshot URI: "+nvt.getMedia().getSnapshotUri(profileToken));


            PtzDevices ptzDevices = nvt.getPtz();

            FloatRange panRange = ptzDevices.getPanSpaces(profileToken);
            System.out.println("panRange" + panRange.getMax() + "" + panRange.getMin());

            FloatRange tiltRange = ptzDevices.getTiltSpaces(profileToken);
            System.out.println("tiltRange" + tiltRange.getMax() + "" + tiltRange.getMin());

            FloatRange zoomRange = ptzDevices.getZoomSpaces(profileToken);
            System.out.println("zoomRange" + zoomRange.getMax() + "" + zoomRange.getMin());

//            float zoom = zoomRange.getMin();
//            float x = (panRange.getMax() + panRange.getMin()) / 2f;
//            float y = (tiltRange.getMax() + tiltRange.getMin()) / 2f;

//            if (ptzDevices.isAbsoluteMoveSupported(profileToken))
//                ptzDevices.absoluteMove(profileToken, x, y, zoom);

            if (ptzDevices.isContinuosMoveSupported(profileToken))
                ptzDevices.continuousMove(profileToken, -0.1f, 0, 0);


        }
        catch (ConnectException e) {
            System.err.println("Could not connect to NVT with IP: "+hostIP);
        }
        catch (SOAPException e) {
            e.printStackTrace();
        }
    }
}
