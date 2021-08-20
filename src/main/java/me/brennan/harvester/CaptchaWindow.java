package me.brennan.harvester;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.event.ConsoleMessageReceived;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.frame.Frame;
import com.teamdev.jxbrowser.js.ConsoleMessage;
import com.teamdev.jxbrowser.net.event.ResponseBytesReceived;
import com.teamdev.jxbrowser.view.swing.BrowserView;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

/**
 * @author Brennan
 * @since 8/19/21
 **/
public class CaptchaWindow {
    private final String siteURL, siteKey;
    private final CaptchaManager captchaManager;

    private final Engine engine;
    private Browser browser;
    private JFrame frame;

    public CaptchaWindow(String siteURL, String siteKey, CaptchaManager captchaManager) {
        this.siteURL = siteURL;
        this.siteKey = siteKey;
        this.captchaManager = captchaManager;

        this.engine = Engine.newInstance(EngineOptions
                .newBuilder(RenderingMode.HARDWARE_ACCELERATED).licenseKey("TOKEN").build());

        this.frame = new JFrame("Captcha Harvester");
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                engine.close();
            }
        });
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.frame.setSize(360, 550);
    }

    public void start() {
        createWindow();

        setupTokenInterceptor();
        this.browser.navigation().loadUrl("https://gmail.com");
        final Thread tokenThread = new Thread(() -> {

            boolean loggedIn = isCompose();
            while (!loggedIn) {
                loggedIn = isCompose();
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            this.browser.navigation().loadUrl("https://www.google.com/search?q=yotube+videos");
            this.setupTokenInterceptor();
            this.browser.navigation().loadUrl(siteURL);

        });
        tokenThread.start();
    }

    private boolean isCompose() {
        final Frame frame = browser.mainFrame().orElse(null);
        return frame != null && frame.html().contains("compose");
    }

    private void siteInterceptor() {
        this.browser.on(ConsoleMessageReceived.class, (consoleMessageReceived) -> {
           final ConsoleMessage consoleMessage = consoleMessageReceived.consoleMessage();

           final String message = consoleMessage.message();

           if(message.indexOf("03") == 0) {

           }
        });
    }

    private void setupTokenInterceptor() {
        this.engine.network().on(ResponseBytesReceived.class, (responseBytesReceived) -> {
           try {
               final String url = responseBytesReceived.urlRequest().url();

               if(url.contains("userverify")) {
                   String token = new String(responseBytesReceived.data());
                   if(token.contains("03")) {
                       token = token.substring(token.indexOf("03"));

                       this.captchaManager.addCaptcha(url, token);
                   }
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
        });
    }

    private void createWindow() {
        this.browser = engine.newBrowser();
        final BrowserView browserView = BrowserView.newInstance(browser);
        this.frame.add(browserView, "Center");
        this.frame.setVisible(true);
    }
}
