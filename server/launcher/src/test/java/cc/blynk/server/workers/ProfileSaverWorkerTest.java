package cc.blynk.server.workers;

import cc.blynk.server.core.dao.FileManager;
import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.stats.GlobalStats;
import cc.blynk.server.db.DBManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 3/3/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileSaverWorkerTest {

    @Mock
    private UserDao userDao;

    @Mock
    private FileManager fileManager;

    @Mock
    private GlobalStats stats;

    @Test
    public void testCorrectProfilesAreSaved() throws IOException {
        ProfileSaverWorker profileSaverWorker = new ProfileSaverWorker(userDao, fileManager, new DBManager());

        User user1 = new User("1", "");
        User user2 = new User("2", "");
        User user3 = new User("3", "");
        User user4 = new User("4", "");

        Map<String, User> userMap = new HashMap<>();
        userMap.put("1", user1);
        userMap.put("2", user2);
        userMap.put("3", user3);
        userMap.put("4", user4);

        when(userDao.getUsers()).thenReturn(userMap);
        profileSaverWorker.run();

        verify(fileManager, times(4)).overrideUserFile(any());
        verify(fileManager).overrideUserFile(user1);
        verify(fileManager).overrideUserFile(user2);
        verify(fileManager).overrideUserFile(user3);
        verify(fileManager).overrideUserFile(user4);
    }

    @Test
    public void testNoProfileChanges() throws Exception {
        User user1 = new User("1", "");
        User user2 = new User("2", "");
        User user3 = new User("3", "");
        User user4 = new User("4", "");

        Map<String, User> userMap = new HashMap<>();
        userMap.put("1", user1);
        userMap.put("2", user2);
        userMap.put("3", user3);
        userMap.put("4", user4);

        Thread.sleep(1);

        ProfileSaverWorker profileSaverWorker = new ProfileSaverWorker(userDao, fileManager, new DBManager());

        when(userDao.getUsers()).thenReturn(userMap);
        profileSaverWorker.run();

        verifyNoMoreInteractions(fileManager);
    }

}
