package edu.udacity.java.nano;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebSocketChatApplicationTest {

    private WebDriver driver;
    private String user = "User";
    private String anotherUser = "AnotherUser";
    private String msg = "Hi, this is User";
    private String msgFromAnotherUser = "Hi, this is AnotherUser";

    @Before
    public void setUp() throws Exception {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.get("http://localhost:8080");

    }

    @After
    public void teardown() throws Exception {
        driver.quit();
    }

    @Test
    public void test() throws  Exception{
        login(user);
        chat(user,msg);
        join();
        leave();
    }


    public void login(String username) {
        WebElement inputElement = driver.findElement(By.id("username"));
        WebElement submitElement = driver.findElement(By.className("submit"));
        inputElement.sendKeys(username);
        submitElement.click();
        WebElement chatRoom  = driver.findElement(By.id("username"));
        Assert.assertEquals(username,chatRoom.getText());
    }

    public void chat(String username,String userMsg) throws Exception{
        WebElement msgElement = driver.findElement(By.id("msg"));
        msgElement.sendKeys(userMsg);
        WebElement send = driver.findElement(By.id("send"));
        send.click();
        Thread.sleep(1000);
        WebElement msgContent = driver.findElement(By.className("message-content"));
        Assert.assertEquals(username+": "+userMsg,msgContent.getText());

    }

    public void join() throws Exception{
        Set<String> windows = driver.getWindowHandles();
        String adminToolHandle = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open();");
        Set<String> customerWindow = driver.getWindowHandles();
        customerWindow.removeAll(windows);
        String customerSiteHandle = ((String) customerWindow.toArray()[0]);
        driver.switchTo().window(customerSiteHandle);
        driver.get("http://localhost:8080");
        login(anotherUser);
        chat(anotherUser,msgFromAnotherUser);
        WebElement chatNum = driver.findElement(By.className("chat-num"));
        int onlineUsers = Integer.parseInt(chatNum.getText());
        Assert.assertTrue(onlineUsers==2);
        leave();
        driver.switchTo().window(adminToolHandle);
    }

    public void leave() {
        WebElement exit = driver.findElement(By.id("exit"));
        exit.click();
        WebElement login = driver.findElement(By.className("submit"));
        Assert.assertEquals("Login",login.getText());

    }
}