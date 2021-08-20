import me.brennan.harvester.CaptchaManager;
import me.brennan.harvester.CaptchaWindow;

/**
 * @author Brennan
 * @since 8/19/21
 **/
public class TestCaptcha {

    public static void main(String[] args) {
        final CaptchaManager captchaManager = new CaptchaManager();

        final CaptchaWindow captchaWindow = new CaptchaWindow("https://google.com/recaptcha/api2/demo",
                "6Le-wvkSAAAAAPBMRTvw0Q4Muexq9bi0DJwx_mJ-", captchaManager);
        captchaWindow.start();
    }
}
