package space.pandaer.component;

import space.pandaer.annotation.Autowired;
import space.pandaer.annotation.Component;
import space.pandaer.annotation.Scope;

@Component
@Scope
public class UserService {

    @Autowired(required = false,value = "dao")
    private UserDao xxx;

    public void save() {
        System.out.println("UserService public void save");
        xxx.save();
    }
}
