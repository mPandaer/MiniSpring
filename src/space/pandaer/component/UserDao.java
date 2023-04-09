package space.pandaer.component;

import space.pandaer.annotation.Component;
import space.pandaer.annotation.Scope;
import space.pandaer.annotation.ScopeType;

@Component("dao")
@Scope(ScopeType.PROTOTYPE)
public class UserDao {

    public void save() {
        System.out.println(this);
        System.out.println("UserDao public void save");
    }
}
