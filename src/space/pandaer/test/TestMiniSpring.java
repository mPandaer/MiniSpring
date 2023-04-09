package space.pandaer.test;

import org.junit.jupiter.api.Test;

import space.pandaer.component.UserService;
import space.pandaer.context.MiniSpringContext;

public class TestMiniSpring {

    @Test
    public void testPkgName() {
        MiniSpringContext<PandaerConfig> ioc = new MiniSpringContext<>(PandaerConfig.class);
        UserService userService = ioc.getBean("userService", UserService.class);
        userService.save();
    }
}
