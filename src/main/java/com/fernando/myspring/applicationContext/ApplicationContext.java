package com.fernando.myspring.applicationContext;

import com.fernando.myspring.bean.BeanDefinition;
import com.fernando.myspring.exception.DuplicateBeanClassException;
import com.fernando.myspring.exception.NoSuchBeanException;

import java.util.Map;

public interface ApplicationContext {
    /**
     * 根据bean的name获得对象
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 根据bean的type获得对象
     * @param beanType
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getBean(Class<T> beanType) throws Exception;

    /**
     * 同时根据bean的name和type获取对象
     * @param beanName
     * @param beanType
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getBean(String beanName, Class<T> beanType) throws Exception;

    /**
     * 通过bean的name获得type
     * @param name
     * @return
     * @throws NoSuchBeanException
     */
    Class<?> getType(String name) throws NoSuchBeanException;

    /**
     * 根据type获取所有该type对应的bean 以<beanName, Object>的map形式返回
     * @param beanType
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> Map<String, T> getBeansOfType(Class<T> beanType) throws Exception;

    /**
     * 获取BeanDefinition的数量 ioc管理的所有对象的数量
     * @return
     */
    int getBeanDefinitionCount();

    /**
     * 获取所有BeanDefinition的name
     * @return
     */
    String[] getBeanDefinitionNames();

    /**
     * 判断是否存在名字为beanName的bean对象
      * @param beanName
     * @return
     */
    boolean containsBean(String beanName);

    /**
     * 判断是否存在名字为beanName的BeanDefinition对象
     * 而不是bean对象实例本身是否存在 这可以区分单例模式和原型模式
     * @param beanName
     * @return
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 根据name获取BeanDefinition
     * @param beanName
     * @return
     * @throws NoSuchBeanException
     */
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanException;

    /**
     * 根据name和type获取BeanDefinition
     * @param beanName
     * @param beanType
     * @return
     * @throws NoSuchBeanException
     */
    BeanDefinition getBeanDefinition(String beanName, Class<?> beanType) throws NoSuchBeanException;

    /**
     * 根据type获取BeanDefinition
     * @param beanType
     * @return
     * @throws DuplicateBeanClassException
     * @throws NoSuchBeanException
     */
    BeanDefinition getBeanDefinition(Class<?> beanType) throws DuplicateBeanClassException, NoSuchBeanException;
}
