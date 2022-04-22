package com.fernando.myspring.applicationContext.impl;

import com.fernando.myspring.annotation.AOP.*;
import com.fernando.myspring.annotation.Bean;
import com.fernando.myspring.annotation.Configuration;
import com.fernando.myspring.annotation.IOC.*;
import com.fernando.myspring.applicationContext.ApplicationContext;
import com.fernando.myspring.bean.BeanDefinition;
import com.fernando.myspring.bean.ProxyMethod;
import com.fernando.myspring.exception.*;
import com.fernando.myspring.extension.Extension;
import com.fernando.myspring.proxyFactory.ProxyFactory;
import com.fernando.myspring.proxyFactory.impl.CGLibProxyFactory;
import com.fernando.myspring.proxyFactory.impl.JDKProxyFactory;
import com.fernando.myspring.utils.ScanPackageUtil;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyAnnotationConfigApplicationContext implements ApplicationContext {
    //======================================全局集合====================================================//

    // 一级缓存 beanName->bean对象
    private final Map<String, Object> iocByName = new ConcurrentHashMap<>(256);

    // 二级缓存 存放半成品对象
    private final Map<String, Object> earlyRealObjects = new ConcurrentHashMap<>(256);

    // 二级缓存 存放半成品的代理对象
    private final Map<String, Object> earlyProxyObjects = new ConcurrentHashMap<>(16);

    // 保存所有的beanDefinition
    private final Set<BeanDefinition> beanDefinitions = new HashSet<>(256);

    // 保存beanName和beanDefinition之间的关系
    private final Map<String, BeanDefinition> allBeansByName = new HashMap<>();

    // 保存beanType和beanDefinition之间的关系
    private final Map<Class<?>, BeanDefinition> allBeansByType = new HashMap<>();

    // 保存beanType对应的beanName集合
    private final Map<Class<?>, Set<String>> beanTypeAndName = new HashMap<>();

    // 保存目标方法类和他的通知方法的集合（不分通知方式）
    private final Map<Class<?>, Set<Method>> aspect = new HashMap<>();

    // 对外扩展接口实现类的对象
    private List<? extends Extension> extensions = new ArrayList<>();

    // property配置文件的位置
    private final String propertyFile;

    // 记录注解->标注了这个注解的类
    private Map<Class<?>, List<Class<?>>> annotationType2Clazz = new HashMap<>();

    // 记录需要被代理的类
    private List<Class<?>> needBeProxy = new ArrayList<>();

    // 记录关键位置的日志
    private final Logger logger = LoggerFactory.getLogger(getClass());

//========================================构造方法====================================//

    // 没有配置文件需要加载
    public MyAnnotationConfigApplicationContext(String... basePackages) throws Exception {
        this(null, basePackages);
    }

    // 有配置文件
    public MyAnnotationConfigApplicationContext(String propertyFile, String[] basePackages) throws Exception {
        this.propertyFile = propertyFile;

        // 扫描指定路径 找到目标类
        findBeanDefinitions(basePackages);
        // 创建bean对象
        createObject();
        // 将需要代理的对象进行动态代理
        proxyObject();
        // 自动装载并将切面类中的方法横切目标方法并装入ioc容器中
        autowireObject();
        // 注入配置类
        addConfig();
        // 容器初始化日志
        System.out.println("IoC容器初始化完成");
    }

    // 带有扩展性的构建IoC容器 需要加载配置文件 且有对此框架扩展框架的适配
    public MyAnnotationConfigApplicationContext(String propertyFile, List<? extends Extension> extensions,
                                                String... basePackages) throws Exception {
        this.extensions = extensions;
        this.propertyFile = propertyFile;
        for (Extension extension : this.extensions) {
            extension.doOperation0(this);
        }
        // 扫描指定路径 找到目标类
        findBeanDefinitions(basePackages);
        for (Extension extension : this.extensions) {
            extension.doOperation1(this);
        }
        // 创建bean对象
        createObject();
        for (Extension extension : this.extensions) {
            extension.doOperation2(this);
        }
        // 将需要代理的对象进行动态代理
        proxyObject();
        for (Extension extension : this.extensions) {
            extension.doOperation3(this);
        }
        // 自动装载并将切面类中的方法横切目标方法并装入ioc容器中
        autowireObject();
        for (Extension extension : this.extensions) {
            extension.doOperation4(this);
        }
        // 注入配置类
        addConfig();
        for (Extension extension : this.extensions) {
            extension.doOperation9(this);
        }
        // 容器初始化日志
        System.out.println("IoC容器初始化完成");
    }

//=====================================第一步============================================//

    /**
     * 第一步 获取某包下的所有类并根据注解情况生成BeanDefinition 维护本地缓存
     *
     * @param basePackages
     * @throws Exception
     */
    private void findBeanDefinitions(String... basePackages) throws DuplicateBeanNameException, ClassNotFoundException {
        for (String basePackage : basePackages) { // 获取包路径下的所有类
            Set<Class<?>> classes = ScanPackageUtil.getClasses(basePackage);
            for (Class<?> clazz : classes) { // 对每个类
                for (Annotation annotation : clazz.getAnnotations()) { // 对于类的每个注解 将其存放到<注解, 有该注解的类的列表>
                    List<Class<?>> clazzList = annotationType2Clazz.getOrDefault(annotation.annotationType(), new ArrayList<>());
                    clazzList.add(clazz);
                    annotationType2Clazz.put(annotation.annotationType(), clazzList);
                }
                // 找到所有切面类 保存对应信息
                Aspect aspect = clazz.getAnnotation(Aspect.class);
                if (aspect != null) {
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) { // 将切面中的所有通知方法 添加到注解中写明的目标方法的map中
                        Before before = method.getAnnotation(Before.class);
                        After after = method.getAnnotation(After.class);
                        AfterThrowing afterThrowing = method.getAnnotation(AfterThrowing.class);
                        AfterReturning afterReturning = method.getAnnotation(AfterReturning.class);
                        if (before != null) {
                            keepAspectMethod(method, before.value());
                        }
                        if (after != null) {
                            keepAspectMethod(method, after.value());
                        }
                        if (afterThrowing != null) {
                            keepAspectMethod(method, afterThrowing.value());
                        }
                        if (afterReturning != null) {
                            keepAspectMethod(method, afterReturning.value());
                        }
                    }
                }

                // 根据注解情况生成beanDefinition 并维护多个集合
                Component componentAnnotation = clazz.getAnnotation(Component.class);
                Repository repositoryAnnotation = clazz.getAnnotation(Repository.class);
                Service serviceAnnotation = clazz.getAnnotation(Service.class);
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);
                Configuration configurationAnnotation = clazz.getAnnotation(Configuration.class);
                String beanName = null;
                if (componentAnnotation != null) beanName = componentAnnotation.value();
                if (repositoryAnnotation != null) beanName = repositoryAnnotation.value();
                if (serviceAnnotation != null) beanName = serviceAnnotation.value();
                if (controllerAnnotation != null) beanName = controllerAnnotation.value();
                if (configurationAnnotation != null) beanName = configurationAnnotation.value();
                if (aspect != null) beanName = aspect.value();
                if (beanName != null) {
                    beanName = checkBeanName(beanName, clazz);
                    boolean lazy = clazz.getAnnotation(Lazy.class) != null;
                    boolean singleton = true;
                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                    if (scopeAnnotation != null) {
                        String value = scopeAnnotation.value();
                        if ("prototype".equals(value)) {
                            singleton = false;
                        } else if (!"singleton".equals(value)) {
                            throw new IllegalStateException();
                        }
                    }
                    // 获取到beanDefinition的所有信息 生成一个并保存到对应的集合中
                    BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz, lazy, singleton);
                    beanDefinitions.add(beanDefinition);
                    allBeansByName.put(beanName, beanDefinition);
                    allBeansByType.put(clazz, beanDefinition);
                }
            }
            System.out.println("扫描包" + basePackage + "完成");
        }
    }

    /**
     * 维护aspect的map <目标方法的类, 通知方法的set>
     * @param method
     * @param value2
     * @throws ClassNotFoundException
     */
    private void keepAspectMethod(Method method, String value2) throws ClassNotFoundException {
        // 获取目标方法的类名
        String className = value2.substring(0, value2.substring(0, value2.indexOf("(")).lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        Set<Method> set = aspect.getOrDefault(clazz, new HashSet<>());
        set.add(method);
        aspect.put(clazz, set);
    }

    /**
     * beanName的适配环节 并检查是否有重复name情况
     *
     * @param beanName
     * @param clazz
     * @return
     * @throws DuplicateBeanNameException
     */
    private String checkBeanName(String beanName, Class<?> clazz) throws DuplicateBeanNameException {
        if ("".equals(beanName)) {
            String className = clazz.getSimpleName();
            beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
        }
        if (allBeansByName.containsKey(beanName)) {
            throw new DuplicateBeanNameException(beanName);
        }
        return beanName;
    }

//===================================================第二步===================================================//
    /**
     * 第二步 对每个非懒加载且单例模式的bean创建对象
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws DuplicateBeanNameException
     * @throws InvocationTargetException
     * @throws DataConversionException
     */
    private void createObject() throws NoSuchMethodException, IllegalAccessException, InstantiationException, DuplicateBeanNameException, InvocationTargetException, DataConversionException {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (!beanDefinition.isLazy() && beanDefinition.isSingleton()) {
                createObject(beanDefinition);
            }
        }
        System.out.println("所有单例且非懒加载的bean实例化完成");
    }

    /**
     * 为bean创建对象 并根据需要注入的值
     *
     * @param beanDefinition
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws DataConversionException
     */
    private Object createObject(BeanDefinition beanDefinition) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, DataConversionException, DuplicateBeanNameException {
        Class<?> beanClass = beanDefinition.getBeanClass();
        String beanName = beanDefinition.getBeanName();
        // 通过反射得到一个新的对象 并对对象中的域赋值
        Object object = beanClass.getConstructor().newInstance();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            Value annotation = field.getAnnotation(Value.class);
            if (annotation != null) {
                String value = annotation.value();
                // 注解中的注入值均为字符串 需要做一些转换
                Object val = convertVal(value, field);
                // 注入方式采用暴力的设置访问field的访问权限 这样就不限制bean中一定要有相应的set方法
                field.setAccessible(true);
                field.set(object, val);
            }
        }
        if (beanDefinition.isSingleton()) {
            if (earlyRealObjects.containsKey(beanName)) {
                throw new DuplicateBeanNameException(beanName);
            }
            earlyRealObjects.put(beanName, object);
        }
        return object;
    }

    /**
     * String类型转换成不同的类型的值
     *
     * @param value
     * @param field
     * @return
     * @throws DataConversionException
     */
    private Object convertVal(String value, Field field) throws DataConversionException {
        Object val;
        switch (field.getType().getName()) {
            case "int":
            case "java.lang.Integer":
                try {
                    val = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new DataConversionException(value, field.getType().getName());
                }
                break;
            case "java.lang.String":
                val = value;
                break;
            case "char":
            case "java.lang.Character":
                if (value.length() < 1)
                    throw new DataConversionException(value, field.getType().getName());
                val = value.charAt(0);
                break;
            case "long":
            case "java.lang.Long":
                try {
                    val = Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new DataConversionException(value, field.getType().getName());
                }
                break;
            case "short":
            case "java.lang.Short":
                try {
                    val = Short.parseShort(value);
                } catch (NumberFormatException e) {
                    throw new DataConversionException(value, field.getType().getName());
                }
                break;
            case "double":
            case "java.lang.Double":
                try {
                    val = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new DataConversionException(value, field.getType().getName());
                }
                break;
            case "boolean":
            case "java.lang.Boolean":
                try {
                    val = Boolean.parseBoolean(value);
                } catch (NumberFormatException e) {
                    throw new DataConversionException(value, field.getType().getName());
                }
                break;
            case "float":
            case "java.lang.Float":
                try {
                    val = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    throw new DataConversionException(value, field.getType().getName());
                }
                break;
            default:
                throw new DataConversionException(value, field.getType().getName());
        }
        return val;
    }

//==========================================第三步=======================================//

    /**
     * 第三步 对二级缓存中的所有bean对象调用proxyObject方法
     */
    private void proxyObject() throws Exception {
        for(Map.Entry<String, Object> objectEntry : earlyRealObjects.entrySet()){
            proxyObject(objectEntry.getValue());
        }
    }

    private Object proxyObject(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        String beanName = allBeansByType.get(clazz).getBeanName();
        if(isNeedBeProxy(clazz, beanName)){ // 如果一个bean需要被代理 (是目标方法所在的类)
            return setProxy(obj);
        }
        return obj;
    }

    /**
     * 判断一个bean是否需要设置代理
     * @param clazz
     * @param beanName
     * @return
     */
    private boolean isNeedBeProxy(Class<?> clazz, String beanName){
        if(aspect.containsKey(clazz) && !earlyProxyObjects.containsKey(beanName)){ // 如果该类有通知方法 且 没有出现在代理对象的二级缓存中
            return true;
        }
        for(Method method : clazz.getDeclaredMethods()){ // 如果该类中有任意方法的注解集与目前记录需要代理的所有类之间有交集
            if(hasIntersection(method.getAnnotations())){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个方法中的注解集合与当前需要代理的所有类之间是否有交集
     * @param annotations
     * @return
     */
    private boolean hasIntersection(Annotation[] annotations){
        Set<Class<?>> allElements = new HashSet<>(this.needBeProxy);
        for(Annotation annotation : annotations){
            allElements.add(annotation.annotationType());
        }
        return allElements.size() < (annotations.length + this.needBeProxy.size());
    }

    /**
     * JDK或cglib实现动态代理
     * 将代理对象加入到二级缓存中的earlyProxyObjects
     * @param object
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    private Object setProxy(Object object) throws Exception {
        // 需要代理的bean的类对象
        Class<?> clazz = object.getClass();
        // 最后的返回的代理好的对象
        Object proxy = object;
        // 用于记录使用了哪种代理方式
        boolean whichProxy;
        // 获取目标方法所在类的所有方法
        Set<Method> methods = aspect.getOrDefault(clazz, new HashSet<>());
        Map<Method, Set<Method>> aspectBefore = new HashMap<>();
        Map<Method, Set<Method>> aspectAfter = new HashMap<>();
        Map<Method, Set<Method>> aspectAfterThrowing = new HashMap<>();
        Map<Method, Set<Method>> aspectAfterReturning = new HashMap<>();
        for(Method method : methods){ // 遍历目标类中的所有方法 目的是维护四个map 记录<目标方法, 通知方法的集合>
            Before before = method.getAnnotation(Before.class);
            After after = method.getAnnotation(After.class);
            AfterThrowing afterThrowing = method.getAnnotation(AfterThrowing.class);
            AfterReturning afterReturning = method.getAnnotation(AfterReturning.class);
            if(before != null) addMethodsForAspect(before.value(), clazz, method, aspectBefore);
            if(after != null) addMethodsForAspect(after.value(), clazz, method, aspectAfter);
            if(afterThrowing != null) addMethodsForAspect(afterThrowing.value(), clazz, method, aspectAfterThrowing);
            if(afterReturning != null) addMethodsForAspect(afterReturning.value(), clazz, method, aspectAfterReturning);
        }

        // 记录目标方法到代理后方法的映射关系 用于记录需要交给底层实现动态代理的方法
        Map<Method, ProxyMethod> method2ProxyMethod = new HashMap<>();

        // 找出每个目标方法及其下面的通知方法 实现组装
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            List<Method> beforeMethods = new LinkedList<>(aspectBefore.getOrDefault(declaredMethod, new HashSet<>()));
            List<Object> beforeObject = getObjByAspect(beforeMethods);

            List<Method> returningMethods = new LinkedList<>(aspectAfterReturning.getOrDefault(declaredMethod, new HashSet<>()));
            List<Object> returningObject = getObjByAspect(returningMethods);

            List<Method> throwingMethods = new LinkedList<>(aspectAfterThrowing.getOrDefault(declaredMethod, new HashSet<>()));
            List<Object> throwingObject = getObjByAspect(throwingMethods);

            List<Method> afterMethods = new LinkedList<>(aspectAfter.getOrDefault(declaredMethod, new HashSet<>()));
            List<Object> afterObject = getObjByAspect(afterMethods);

            for (Extension extension : extensions) {
                extension.doOperationWhenProxy(this, declaredMethod,
                        beforeMethods, beforeObject, returningMethods, returningObject,
                        throwingMethods, throwingObject, afterMethods, afterObject);
            }

            // 如果一个方法至少有其中一个注解则将其添加到需要交给底层实现动态代理的map中
            if(beforeMethods.size() != 0 || returningMethods.size() != 0 || throwingMethods.size() != 0 || afterMethods.size() != 0){
                method2ProxyMethod.put(declaredMethod, new ProxyMethod(beforeMethods, beforeObject,
                        returningMethods, returningObject, throwingMethods, throwingObject, afterMethods, afterObject));
            }
        }

        // 如果方法需要代理
        if(method2ProxyMethod.size() > 0){
            ProxyFactory proxyFactory;
            if(clazz.getInterfaces().length == 0){ // 没有实现接口 采用cglib生成代理子类
                proxyFactory = new CGLibProxyFactory(object);
                whichProxy = true;
            } else { // 实现了接口 采用JDK方式
                proxyFactory = new JDKProxyFactory(object);
                whichProxy = false;
            }

            proxy = proxyFactory.getProxyInstance(method2ProxyMethod);

            final BeanDefinition beanDefinition = allBeansByType.get(clazz);
            if(beanDefinition.isSingleton()){
                earlyProxyObjects.put(beanDefinition.getBeanName(), proxy);
            }
            System.out.println("class:" + clazz + "代理对象通过" + (whichProxy ? "CGLib" : "JDK") + "方式设置完成");
        }

        return proxy;
    }

    private List<Object> getObjByAspect(List<Method> methods) throws Exception {
        List<Object> objects = new LinkedList<>();
        for(Method method : methods){
            objects.add(getObject(method.getDeclaringClass()));
        }
        return objects;
    }

    /**
     * 将目标方法下的同一类型的通知方法添加到对应的map中
     * @param value
     * @param clazz
     * @param method
     * @param map
     */
    private void addMethodsForAspect(String value, Class<?> clazz, Method method, Map<Method, Set<Method>> map) throws NoSuchMethodException, ClassNotFoundException {
        // 获取目标方法
        Method proxyMethod = getProxyMethod(value, clazz);
        Set<Method> aspectSet = map.getOrDefault(proxyMethod, new HashSet<>());
        aspectSet.add(method);
        map.put(proxyMethod, aspectSet);
    }

    /**
     * 根据value和clazz得到具体的方法 为避免方法重载找到多个方法 需要做一些小处理
     * @param value
     * @param clazz
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    private Method getProxyMethod(String value, Class<?> clazz) throws ClassNotFoundException, NoSuchMethodException {
        // 处理方法名
        String fullMethodName = value.substring(0, value.indexOf("("));
        String methodName = fullMethodName.substring(fullMethodName.lastIndexOf(".") + 1);
        // 处理参数
        String argStr = value.substring(value.indexOf("(") + 1, value.indexOf(")"));
        argStr = argStr.replaceAll(" ", "");
        String[] args = argStr.split(",");
        // 获取参数的类型
        Class<?>[] argType = new Class[args.length];
        if(!"".equals(argStr)){
            for(int i = 0; i < args.length; i++){
                argType[i] = Class.forName(args[i]);
            }
        }
        Method proxyMethod;
        if(!"".equals(argStr)){ // 有参时 通过方法名和参数类型反射找到方法
            proxyMethod = clazz.getDeclaredMethod(methodName, argType);
        } else { // 无参时 通过方法名找到方法
            proxyMethod = clazz.getDeclaredMethod(methodName);
        }
        return proxyMethod;
    }

//=================================================第四步=========================================//

    /**
     * 第四步 向二级缓存的对象中自动注入依赖 当所有依赖都注入完成后即可将对象加入一级缓存 成为一个完整的对象
     */
    private void autowireObject() throws Exception {
        for (Map.Entry<String, Object> objectEntry : earlyRealObjects.entrySet()) {
            autowireObject(objectEntry.getValue());
        }
        System.out.println("所有单例且非懒加载的bean初始化完成");
    }

    private void autowireObject(Object object) throws Exception {
        final Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            final Autowired autowired = field.getAnnotation(Autowired.class);
            final Qualifier qualifier = field.getAnnotation(Qualifier.class);
            field.setAccessible(true);
            if (field.get(object) != null) { // 如果已经赋过值
                continue;
            }
            if (autowired != null) { // 这个对象还有域需要注入
                Object bean;
                if (qualifier != null) { // 通过beanName进行注入
                    bean = getObject(qualifier.value());
                } else { // 通过beanType进行注入
                    bean = getObject(field.getType());
                }
                field.set(object, bean);
            }
        }
        // 检查此对象 如果是单例、非懒加载的则加入一级缓存中 并从二级缓存中删除
        final BeanDefinition beanDefinition = allBeansByType.get(clazz);
        if(beanDefinition.isSingleton()){
            String beanName = beanDefinition.getBeanName();
            iocByName.put(beanName, getObject(beanName));
            earlyRealObjects.remove(beanName);
            earlyProxyObjects.remove(beanName);
        }
    }

//================================第五步=================================//

    /**
     * 第五步 将配置类加入到容器中
     * @throws DuplicateBeanNameException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void addConfig() throws DuplicateBeanNameException, InvocationTargetException, IllegalAccessException {
        // 对一级缓存做检查 如果是配置类 则需要执行其中标注了@Beand的方法 然后将方法的返回结果加入一级缓存
        final Set<Map.Entry<String, Object>> entrySet = iocByName.entrySet();
        for(Map.Entry<String, Object> objectEntry : entrySet){
            final Object o = checkConfig(objectEntry.getValue());
            if(o != null){
                iocByName.put(objectEntry.getKey(), o);
            }
        }
    }

    /**
     * 如果是配置类则将所有标有@Bean注解的方法执行一次 将返回对象放入一级缓存中
     * @param obj
     * @return
     * @throws DuplicateBeanNameException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object checkConfig(Object obj) throws DuplicateBeanNameException, InvocationTargetException, IllegalAccessException {
        final Class<?> clazz = obj.getClass();
        final Configuration configuration = clazz.getAnnotation(Configuration.class);
        if(configuration != null){
            for(Method method : clazz.getDeclaredMethods()){
                final Bean bean = method.getAnnotation(Bean.class);
                if(bean != null){
                    Class<?> returnType = method.getReturnType();
                    String beanName = bean.name();
                    if("".equals(beanName)){
                        beanName = method.getName();
                    }
                    if(allBeansByName.containsKey(beanName) || iocByName.containsKey(beanName)){
                        throw new DuplicateBeanNameException(beanName);
                    }
                    final Object[] args = new Object[]{};
                    final Object result = method.invoke(obj, args);
                    iocByName.put(beanName, result);
                    BeanDefinition beanDefinition = new BeanDefinition(beanName, returnType, false, true);
                    allBeansByName.put(beanName, beanDefinition);
                }
            }

            if(configuration.proxyBeanMethods())
                return setConfigProxy(obj);
        }
        return null;
    }

    /**
     * 配置类对象的代理
     * 如果Bean已经存在于一级缓存中 则直接从容器中获取并返回 如果不存在则执行后加入容器并返回
     * @param object
     * @return
     */
    private Object setConfigProxy(Object object){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(object.getClass());
        enhancer.setCallback((MethodInterceptor) (o, method, args, methodProxy) -> {
            Object result;
            final Bean bean = method.getAnnotation(Bean.class);
            if(bean != null){
                Class<?> clazz = method.getReturnType();
                String beanName = bean.name();
                if("".equals(beanName)){
                    beanName = method.getName();
                }
                if(iocByName.containsKey(beanName)){
                    result = iocByName.get(beanName);
                } else {
                    result = methodProxy.invoke(object, args);
                    iocByName.put(beanName, result);
                    BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz, false, true);
                    allBeansByName.put(beanName, beanDefinition);
                }
            } else {
                result = methodProxy.invoke(object, args);
            }
            return result;
        });
        return enhancer.create();
    }

//=================================全局辅助方法======================================//

    /**
     * 根据bean的名字按缓存层级获取bean对象
     * 此方法用于ioc容器构造过程中 因此获取某个bean需要按缓存优先层级获取
     * @param beanName
     * @return
     */
    private Object getObject(String beanName) throws Exception {
        // 先尝试从缓存中获取
        final Object o = iocByName.get(beanName);
        if(o != null) return o;
        if(earlyProxyObjects.containsKey(beanName)) return earlyProxyObjects.get(beanName);
        if(earlyRealObjects.containsKey(beanName)) return earlyRealObjects.get(beanName);
        // 缓存中没有 则可能是压根没有这个BeanDefinition或者该bean为非单例或者懒加载的
        final BeanDefinition beanDefinition = allBeansByName.get(beanName);
        if(beanDefinition == null) throw new NoSuchBeanException();

        final Object bean = createBean(beanDefinition); // 为其创建一个对象 并完成一系列工作
        if(beanDefinition.isSingleton()){ // 这个判断其实也可以不用
            return iocByName.get(beanName);
        }
        return bean;
    }

    /**
     * 根据bean的type获取bean对象 要求一个beanType对应一个bean 如果出现多个则要抛出异常
     * @param beanType
     * @return
     * @throws Exception
     */
    private Object getObject(Class<?> beanType) throws Exception {
        return getObject(getNameByType(beanType));
    }

    /**
     * 通过beanType获取beanName
     * 需要确保只有一个beanName
     * @param beanType
     * @return
     * @throws DuplicateBeanClassException
     * @throws NoSuchBeanException
     */
    private String getNameByType(Class<?> beanType) throws DuplicateBeanClassException, NoSuchBeanException {
        final Set<String> namesByType = getNamesByType(beanType);
        if(namesByType.size() == 1){
            return namesByType.iterator().next();
        } else if(namesByType.size() > 1){
            throw new DuplicateBeanClassException(beanType);
        } else {
            throw new NoSuchBeanException();
        }
    }

    /**
     * 通过beanType获取beanNames
     * 此方法可以获取此类型对应的所有派生类或者实现类的对象的beanName
     * @param beanType
     * @return
     */
    private Set<String> getNamesByType(Class<?> beanType){
        if(beanTypeAndName.containsKey(beanType)){
            return beanTypeAndName.get(beanType);
        } else {
            // 缓存中没有 需要新建一个
            Set<String> set = new HashSet<>();
            for (Map.Entry<String, BeanDefinition> entry : allBeansByName.entrySet()) {
                if(beanType.isAssignableFrom(entry.getValue().getBeanClass())){
                    set.add(entry.getKey());
                }
            }
            beanTypeAndName.put(beanType, set);
            return set;
        }
    }

    /**
     * 对于非单例或者延迟加载的bean在此完成创建、实例化、代理、初始化工作
     * @param beanDefinition
     * @return
     */
    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        for (Extension extension : extensions){
            extension.doOperation5(this, beanDefinition);
        }

        // 实例化bean对象
        final Object object = createObject(beanDefinition);

        for(Extension extension : extensions){
            extension.doOperation6(this, object);
        }

        // 代理实例化的对象
        final Object o = proxyObject(object);

        for(Extension extension : extensions){
            extension.doOperation7(this, object);
        }

        // 如果需要 完成注入工作
        autowireObject(object);

        for(Extension extension : extensions){
            extension.doOperation8(this, object);
        }

        return o;
    }

    /**
     * 根据beanName获取bean对象
     * 此方法用于容器构造完成后(public) 因此获取某个bean不用按缓存优先层级获取 直接看一级缓存即可
     * @param beanName
     * @return
     * @throws Exception
     */
    public Object getBean(String beanName) throws Exception {
        // 在一级缓存中获取
        final Object o = iocByName.get(beanName);
        if(o != null){
            return o;
        }
        final BeanDefinition beanDefinition = allBeansByName.get(beanName);
        // 如果这个bean不由ioc容器管理
        if(beanDefinition == null){
            throw new NoSuchBeanException();
        }
        // 懒加载或者非单例模式
        final Object bean = createBean(beanDefinition);
        if(beanDefinition.isSingleton()){
            return iocByName.get(beanName);
        }
        return bean;
    }

//=================================复写抽象方法===============================//
    /**
     * 根据bean的type获得对象
     * @param beanType
     * @return
     * @throws Exception
     */
    @Override
    public <T> T getBean(Class<T> beanType) throws Exception {
        return (T) getBean(getNameByType(beanType));
    }

    /**
     * 同时根据bean的name和type获取对象
     * @param beanName
     * @param beanType
     * @return
     * @throws Exception
     */
    @Override
    public <T> T getBean(String beanName, Class<T> beanType) throws Exception {
        final Object o = getBean(beanName);
        if(beanType.isInstance(o)){
            return (T) o;
        } else {
            throw new NoSuchBeanException();
        }
    }

    /**
     * 根据bean的name获取bean的类型
     * @param name
     * @return
     * @throws NoSuchBeanException
     */
    @Override
    public Class<?> getType(String name) throws NoSuchBeanException {
        if(allBeansByName.containsKey(name)){
            return allBeansByName.get(name).getBeanClass();
        } else {
            throw new NoSuchBeanException();
        }
    }

    /**
     * 根据type获取所有该type对应的bean 以<beanName, Object>的map形式返回
     * @param beanType
     * @param <T>
     * @return
     * @throws Exception
     */
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> beanType) throws Exception {
        Map<String, T> map = new HashMap<>();
        for(String s : getNamesByType(beanType)){
            map.put(s, (T) getBean(s));
        }
        return map;
    }

    /**
     * 获取BeanDefinition的数量 ioc管理的所有对象的数量
     *
     * @return
     */
    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitions.size();
    }

    /**
     * 获取所有beanDefinition的名称
     * @return
     */
    public String[] getBeanDefinitionNames() {
        String[] res = new String[beanDefinitions.size()];
        int i = 0;
        for (BeanDefinition beanDefinition : beanDefinitions) {
            res[i++] = beanDefinition.getBeanName();
        }
        return res;
    }

    @Override
    public boolean containsBean(String name) {
        return allBeansByName.containsKey(name);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return allBeansByName.containsKey(beanName);
    }

    /**
     * 根据bean的name获取beanDefinition
     * @param beanName
     * @return
     * @throws NoSuchBeanException
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanException {
        if(allBeansByName.containsKey(beanName)){
            return allBeansByName.get(beanName);
        } else {
            throw new NoSuchBeanException();
        }
    }

    /**
     * 根据bean的name和type获取beanDefinition
     * @param beanName
     * @param beanType
     * @return
     * @throws NoSuchBeanException
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> beanType) throws NoSuchBeanException {
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        if(beanType.isAssignableFrom(beanDefinition.getBeanClass())){
            return beanDefinition;
        }
        throw new NoSuchBeanException();
    }

    /**
     * 根据bean的type获取beanDefinition
     * @param beanType
     * @return
     * @throws DuplicateBeanClassException
     * @throws NoSuchBeanException
     */
    @Override
    public BeanDefinition getBeanDefinition(Class<?> beanType) throws DuplicateBeanClassException, NoSuchBeanException {
        return getBeanDefinition(getNameByType(beanType));
    }
}
