package space.pandaer.context;


//实现Spring容器的机制

import space.pandaer.annotation.*;
import space.pandaer.utils.StringUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;


public class MiniSpringContext<T> {
    private final Class<T> cfgClazz;
    //存放类信息的集合
    private final Map<String,BeanDefinition> beanDefinitionMap = new HashMap<>();
    private final Map<String,Object> singleObjects = new HashMap<>();

    public MiniSpringContext(Class<T> clazz) {
        this.cfgClazz = clazz;
        ComponentScan annotation = clazz.getDeclaredAnnotation(ComponentScan.class);
        if (annotation == null) throw new RuntimeException("检查"+clazz.getName()+"是否存在@ComponentScan注解");
        String pkgName = annotation.value();
        String pkgPath = pkgName.replace(".","/");
        URL pkgURL = clazz.getClassLoader().getResource(pkgPath);
        if (pkgURL == null) throw new RuntimeException("请仔细检查@ComponentScan注解");
        //开始获取有注解的.class对象
        List<Class<?>> classes = scanPkg(pkgURL.getPath());
        //初始化beanDefinitionMap
        initBeanDefinitionMap(classes);
        //初始化单例池
        initSingleObjects();

    }


    private void initSingleObjects() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            if (entry.getValue().getScope() == ScopeType.SINGLE) {
                singleObjects.put(entry.getKey(),creatBean(entry.getValue().getClazz()));
            }
        }
    }


    //创建bean
    private<M> M creatBean(Class<M> clazz) {
        try {
            Constructor<M> constructor = clazz.getConstructor();
            M instance = constructor.newInstance();
            //依赖注入 -- 遍历字段 + 判断注解 + 为字段设置值
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Autowired autowiredAnnotation = field.getDeclaredAnnotation(Autowired.class);
                if (autowiredAnnotation == null) continue;
                String name = field.getName();
                if (!"".equals(autowiredAnnotation.value())) name = autowiredAnnotation.value();
                BeanDefinition beanDefinition = beanDefinitionMap.get(name);
                if (beanDefinition == null) {
                    if (autowiredAnnotation.required()) {
                        throw new RuntimeException("在Context中没有找到以"+name+"为索引的bean对象");
                    }else {
                        continue;
                    }
                }
                Object bean = getBean(name);
                field.setAccessible(true);
                field.set(instance,bean);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //提供给外界的API接口，获取bean
    public <M> M getBean(String beanName,Class<M> beanClass) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) throw new RuntimeException("请检查"+beanName+"是否配置正确");
        Class<M> clazz = (Class<M>) beanDefinition.getClazz(); //todo(需要优雅的处理)
        if (clazz != beanClass) throw new RuntimeException("请检查传入的Class对象("+beanClass+")是否正确");
        ScopeType scope = beanDefinition.getScope();
        if (scope == ScopeType.PROTOTYPE) {
            return creatBean(clazz);
        }else {
            return (M) singleObjects.get(beanName); //todo(需要优雅的处理)
        }

    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) throw new RuntimeException("请检查"+beanName+"是否配置正确");
        Class<?> clazz = beanDefinition.getClazz();
        ScopeType scope = beanDefinition.getScope();
        if (scope == ScopeType.PROTOTYPE) {
            return creatBean(clazz);
        }else {
            return singleObjects.get(beanName);
        }

    }


    /**
     * 判断有无Scope注解，有根据value值判断是单例与否，没有默认单例
     * @param classes 带注解的class对象
     */
    private void initBeanDefinitionMap(List<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setClazz(aClass);
            Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
            if (scopeAnnotation!= null && scopeAnnotation.value() == ScopeType.PROTOTYPE) {
                beanDefinition.setScope(ScopeType.PROTOTYPE);
            }else {
                beanDefinition.setScope(ScopeType.SINGLE);
            }
            Component componentAnnotation = aClass.getAnnotation(Component.class);
            String val = componentAnnotation.value();
            if ("".equals(val)) val = StringUtils.uncap(aClass.getSimpleName());
            beanDefinition.setBeanName(val);
            beanDefinitionMap.put(beanDefinition.getBeanName(),beanDefinition);
        }
    }


    /**
     * 获取全类名 + 类加载器 + 判断是否具有注解
     * @param pkgPath 要扫描的包路径
     * @return 包路径中带注解的class对象集合
     */
    private List<Class<?>> scanPkg(String pkgPath){
        String projectPath = Objects.requireNonNull(cfgClazz.getResource("/")).getPath();
        List<Class<?>> list = new ArrayList<>();
        File pkgFile = new File(pkgPath);
        File[] files = pkgFile.listFiles();
        if (files == null) return list;

        for (File file : files) {
            if(file.isDirectory()) {
                list.addAll(scanPkg(file.getPath()));
            }else {
                String filePath = file.getPath();
                if (filePath.endsWith(".class")) {
                    String fullPkgName = filePath
                            .replace(projectPath,"")
                            .replace(".class","")
                            .replace("/",".");
                    try {
                        Class<?> waitLoadedClass = cfgClazz.getClassLoader().loadClass(fullPkgName);
                        if (!waitLoadedClass.isAnnotationPresent(Component.class)) continue;
                        list.add(waitLoadedClass);
                    } catch (Exception e) {
                        System.err.println("加载"+fullPkgName+"出现异常");
                    }
                }
            }
        }

        return list;

    }



}
