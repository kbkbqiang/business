package com.backend.business.base.helper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Map;

public class BeanFactoryHelper implements BeanFactoryPostProcessor {

    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanFactoryHelper.beanFactory = beanFactory;
    }

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public static <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return beanFactory.getBeansOfType(type);
    }

}
