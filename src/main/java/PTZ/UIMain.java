package PTZ;

/**
 * Created by YiTing-003 on 2018/3/10.
 */

import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.PtzDevices;
import io.reactivex.Observable;
import org.onvif.ver10.schema.FloatRange;
import org.onvif.ver10.schema.Profile;

import java.awt.*;
import javax.swing.*;
import javax.xml.soap.SOAPException;
import java.awt.event.*;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import io.reactivex.*;

public class UIMain extends JFrame {
    private MyPanel mp = null;

    public static void main(String[] args) {
        new UIMain();
    }

    public UIMain() {
        mp = new MyPanel();
        this.add(mp);

        //事件监听
        this.addKeyListener(mp);

        this.setSize(400, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

    }
}

class XYZOOM {
    public static XYZOOM LEFT = new XYZOOM(-1f, 0, 0);
    public static XYZOOM RIGHT = new XYZOOM(1f, 0, 0);
    public static XYZOOM UP = new XYZOOM(0f, 1f, 0);
    public static XYZOOM DOWN = new XYZOOM(0, -1f, 0);

    public float x;
    public float y;
    public float zoom;
    public boolean isStop = false;

    public XYZOOM (float x,float y,float zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

}


//定义自己的面板
class MyPanel extends JPanel implements KeyListener {
    private static final String hostIP = "192.168.2.54:8080";
    private String direction = "";
    private PtzDevices ptzDevices = null;
    private String profileToken = null;
    private ObservableEmitter<XYZOOM> observableEmitter = null;
    private OnvifDevice nvt = null;

    public MyPanel() {
        try {
            // Replace these values with your camera data!
            //OnvifDevice(String hostIp) : OnvifDevice -- Less functionallity
            nvt = new OnvifDevice(hostIP, "admin", "admin");
            Date nvtDate = nvt.getDevices().getDate();
            System.out.println(new SimpleDateFormat().format(nvtDate));

            java.util.List<Profile> profiles = nvt.getDevices().getProfiles();
            profileToken = profiles.get(0).getToken();
            System.out.println("profileToken " + profileToken);
            System.out.println("Snapshot URI: " + nvt.getMedia().getSnapshotUri(profileToken));

            ptzDevices = nvt.getPtz();

            FloatRange panRange = ptzDevices.getPanSpaces(profileToken);
            System.out.println("panRange" + panRange.getMax() + "" + panRange.getMin());

            FloatRange tiltRange = ptzDevices.getTiltSpaces(profileToken);
            System.out.println("tiltRange" + tiltRange.getMax() + "" + tiltRange.getMin());

            FloatRange zoomRange = ptzDevices.getZoomSpaces(profileToken);
            System.out.println("zoomRange" + zoomRange.getMax() + "" + zoomRange.getMin());

        } catch (ConnectException e) {
            System.err.println("Could not connect to NVT with IP: " + hostIP);
        } catch (SOAPException e) {
            e.printStackTrace();
        }

        Observable.create((ObservableEmitter<XYZOOM> emitter) -> observableEmitter = emitter)
        .throttleLast(50, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
//        .subscribe(new Observer<XYZOOM>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                System.out.println("onSubscribe......");
//            }
//
//            @Override
//            public void onNext(XYZOOM value) {
//                if (!value.isStop) {
//                    System.out.println( System.currentTimeMillis() +" 动......" + value.x + " " + value.y + " " + value.zoom + " ");
//                    ptzDevices.continuousMove(profileToken, value.x, value.y, value.zoom);
//                } else {
//                    System.out.println( System.currentTimeMillis() +" 停......" + value.x + " " + value.y + " " + value.zoom + " ");
//                    ptzDevices.stopMove(profileToken);
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.out.println("onError......");
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println("onComplete......");
//            }
//        });
                .subscribe(xyzoom -> {
                    if (!xyzoom.isStop) {
                        System.out.println( System.currentTimeMillis() +" 动......" + xyzoom.x + " " + xyzoom.y + " " + xyzoom.zoom + " ");
                        ptzDevices.continuousMove(profileToken, xyzoom.x, xyzoom.y, xyzoom.zoom);
                    } else {
                        System.out.println( System.currentTimeMillis() +" 停......" + xyzoom.x + " " + xyzoom.y + " " + xyzoom.zoom + " ");
                        ptzDevices.stopMove(profileToken);
                    }
                });

    }

    private void ptzMove(XYZOOM xyzoom) {
        System.out.println("ptzMove......");
        observableEmitter.onNext(xyzoom);
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.drawString(direction, 100, 100);
    }

    @Override//键被按下
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                direction = "↑";
                ptzMove(XYZOOM.UP);
                //调用repaint()函数，重新绘制小球位置
                this.repaint();
                break;
            case KeyEvent.VK_DOWN:
                direction = "↓";
                ptzMove(XYZOOM.DOWN);
                this.repaint();
                break;
            case KeyEvent.VK_LEFT:
                direction = "←";
                ptzMove(XYZOOM.LEFT);
                this.repaint();
                break;
            case KeyEvent.VK_RIGHT:
                direction = "→";
                ptzMove(XYZOOM.RIGHT);
                this.repaint();
                break;
        }

    }

    @Override//表示具体一个值被输出，例如：F1
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    @Override//键被释放
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                XYZOOM  xyzoom = new XYZOOM(0, 0, 0);
                xyzoom.isStop = true;
                ptzMove(xyzoom);
                break;
        }
    }
}
