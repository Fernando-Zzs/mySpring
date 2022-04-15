package com.fernando.myspring.pojo;

import com.fernando.myspring.annotation.Autowired;
import com.fernando.myspring.annotation.Component;
import com.fernando.myspring.annotation.Qualifier;
import com.fernando.myspring.annotation.Value;
import com.fernando.myspring.utils.ScanPackageUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MyAnnotationConfigApplicationContext {
    private List<String> beanNames = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();

    public MyAnnotationConfigApplicationContext(String pack){
        Set<BeanDefinition> beanDefinitions = findBeanDefinitions(pack);
        createObject(beanDefinitions);
        autowireObject(beanDefinitions);
    }

    private void autowireObject(Set<BeanDefinition> beanDefinitions) {
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            Class clazz = beanDefinition.getBeanClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Autowired annotation = declaredField.getAnnotation(Autowired.class);
                if(annotation != null){
                    Qualifier qualifier = declaredField.getAnnotation(Qualifier.class);
                    if(qualifier != null){
                        try {
                            String beanName = qualifier.value();
                            Object bean = getBean(beanName);
                            String fieldName = declaredField.getName();
                            String methodName = "set"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
                            Method method = clazz.getMethod(methodName, declaredField.getType());
                            Object object = getBean(beanDefinition.getBeanName());
                            method.invoke(object, bean);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        return ioc.get(beanName);
    }

    public String[] getBeanDefinitionNames(){
        return beanNames.toArray(new String[0]);
    }

    public Integer getBeanDefinitionCount(){
        return beanNames.size();
    }

    private void createObject(Set<BeanDefinition> beanDefinitions) {
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            Class clazz = beanDefinition.getBeanClass();
            String beanName = beanDefinition.getBeanName();
            try {
                Object object = clazz.getConstructor().newInstance();
                Field[] declaredFields = clazz.getDeclaredFields();
                for(Field declaredField : declaredFields){
                    Value valueAnnotation = declaredField.getAnnotation(Value.class);
                    if(valueAnnotation != null){
                        String value = valueAnnotation.value();
                        String fieldName = declaredField.getName();
                        String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Method method = clazz.getMethod(methodName, declaredField.getType());

                        Object val = null;
                        switch (declaredField.getType().getName()){
                            case "java.lang.Integer":
                                val = Integer.parseInt(value);
                                break;
                            case "java.lang.Long":
                                val = Long.parseLong(value);
                                break;
                            case "java.lang.String":
                                val = value;
                                break;
                            case "java.lang.Float":
                                val = Float.parseFloat(value);
                                break;
                            case "java.lang.Double":
                                val = Double.parseDouble(value);
                                break;
                        }
                        method.invoke(object, val);
                    }
                }
                ioc.put(beanName, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Set<BeanDefinition> findBeanDefinitions(String pack) {
        Set<Class<?>> classes = ScanPackageUtil.getClasses(pack);
        Iterator<Class<?>> iterator = classes.iterator();
        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        while(iterator.hasNext()){
            Class<?> clazz = iterator.next();
            Component componentAnnotation = clazz.getAnnotation(Component.class);
            if(componentAnnotation != null){
                String beanName = componentAnnotation.value();
                if("".equals(beanName)){
                    String className = clazz.getSimpleName();
                    beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
                }
                beanDefinitions.add(new BeanDefinition(beanName, clazz));
                beanNames.add(beanName);
            }
        }
        return beanDefinitions;
    }
}
