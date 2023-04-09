package space.pandaer.context;


import space.pandaer.annotation.ScopeType;

//存储着Bean信息
public class BeanDefinition {
    private String beanName;
    private ScopeType scope;
    private Class<?> clazz;

    public BeanDefinition() {
    }

    public BeanDefinition(String beanName, ScopeType scope, Class<?> clazz) {
        this.beanName = beanName;
        this.scope = scope;
        this.clazz = clazz;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public ScopeType getScope() {
        return scope;
    }

    public void setScope(ScopeType scope) {
        this.scope = scope;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "beanName='" + beanName + '\'' +
                ", scope=" + scope +
                ", clazz=" + clazz +
                '}';
    }
}
