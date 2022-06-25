package com.sp.infra.comp.core.convertor;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * spring工厂调用辅助类
 */
public class ApplicationContextHelper implements ApplicationContextAware {


	private static DefaultListableBeanFactory springFactory;

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		context = applicationContext;
		if (applicationContext instanceof AbstractRefreshableApplicationContext) {
			AbstractRefreshableApplicationContext springContext =
					(AbstractRefreshableApplicationContext) applicationContext;
			springFactory = (DefaultListableBeanFactory) springContext.getBeanFactory();
		} else if (applicationContext instanceof GenericApplicationContext) {
			GenericApplicationContext springContext = (GenericApplicationContext) applicationContext;
			springFactory = springContext.getDefaultListableBeanFactory();
		}
	}

	public static DefaultListableBeanFactory getSpringFactory() {
		return springFactory;
	}

	public static ApplicationContext getContext() {
		return context;
	}
}
