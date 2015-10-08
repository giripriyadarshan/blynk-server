package cc.blynk.integration;

import cc.blynk.integration.model.ClientPair;
import cc.blynk.server.core.application.AppServer;
import cc.blynk.server.core.hardware.HardwareServer;
import cc.blynk.server.model.Profile;
import cc.blynk.server.model.widgets.others.Notification;
import cc.blynk.server.model.widgets.others.Twitter;
import cc.blynk.server.utils.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static cc.blynk.common.enums.Response.INVALID_TOKEN;
import static cc.blynk.common.enums.Response.NOT_ALLOWED;
import static cc.blynk.common.model.messages.MessageFactory.produce;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/2/2015.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ShareProfileWorkflowTest extends IntegrationBase {

    private AppServer appServer;
    private HardwareServer hardwareServer;
    private ClientPair clientPair;

    @Before
    public void init() throws Exception {
        initServerStructures();

        FileUtils.deleteDirectory(holder.fileManager.getDataDir().toFile());

        hardwareServer = new HardwareServer(holder);
        appServer = new AppServer(holder);
        new Thread(hardwareServer).start();
        new Thread(appServer).start();

        //todo improve this
        //wait util server starts.
        sleep(500);

        clientPair = initAppAndHardPair();
    }

    @After
    public void shutdown() {
        appServer.stop();
        hardwareServer.stop();
        clientPair.stop();
    }

    @Test
    public void testGetShareTokenNoDashId() throws Exception {
        clientPair.appClient.send("getShareToken");
        verify(clientPair.appClient.responseMock, timeout(1000)).channelRead(any(), eq(produce(1, NOT_ALLOWED)));
    }

    @Test
    public void testGetShareToken() throws Exception {
        clientPair.appClient.send("getShareToken 1");

        String token = getBody(clientPair.appClient.responseMock);
        assertNotNull(token);
        assertEquals(32, token.length());

        ClientPair clientPair2 = initAppAndHardPair("localhost", appPort, hardPort, "dima2@mail.ua 1", "user_profile_json_2.txt", properties, true);
        clientPair2.appClient.send("getSharedDash " + token);

        String dashboard = getBody(clientPair2.appClient.responseMock);

        assertNotNull(dashboard);
        Profile profile = JsonParser.parseProfile(readTestUserProfile(), 1);
        Twitter twitter = profile.dashBoards[0].getWidgetByType(Twitter.class);
        twitter.cleanPrivateData();
        Notification notification = profile.dashBoards[0].getWidgetByType(Notification.class);
        notification.cleanPrivateData();
        assertEquals(profile.dashBoards[0].toString(), dashboard);
        //System.out.println(dashboard);
    }

    @Test
    public void testGetShareTokenAndRefresh() throws Exception {
        clientPair.appClient.send("getShareToken 1");

        String token = getBody(clientPair.appClient.responseMock);
        assertNotNull(token);
        assertEquals(32, token.length());

        ClientPair clientPair2 = initAppAndHardPair("localhost", appPort, hardPort, "dima2@mail.ua 1", "user_profile_json_2.txt", properties, true);
        clientPair2.appClient.send("getSharedDash " + token);

        String dashboard = getBody(clientPair2.appClient.responseMock);

        assertNotNull(dashboard);

        clientPair.appClient.reset();
        clientPair.appClient.send("refreshShareToken 1");
        String refreshedToken = getBody(clientPair.appClient.responseMock);
        assertNotNull(refreshedToken);
        assertNotEquals(refreshedToken, token);

        clientPair.appClient.reset();
        clientPair.appClient.send("getSharedDash " + token);
        verify(clientPair.appClient.responseMock, timeout(500)).channelRead(any(), eq(produce(1, INVALID_TOKEN)));

        clientPair.appClient.reset();
        clientPair.appClient.send("getSharedDash " + refreshedToken);

        dashboard = getBody(clientPair.appClient.responseMock);

        assertNotNull(dashboard);
        Profile profile = JsonParser.parseProfile(readTestUserProfile(), 1);
        Twitter twitter = profile.dashBoards[0].getWidgetByType(Twitter.class);
        twitter.cleanPrivateData();
        Notification notification = profile.dashBoards[0].getWidgetByType(Notification.class);
        notification.cleanPrivateData();
        assertEquals(profile.dashBoards[0].toString(), dashboard);
        //System.out.println(dashboard);
    }


}
