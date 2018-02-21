package com.sap.cloud.sdk.tutorial;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class SchedulerServletContextListener
               implements ServletContextListener{
	
	private SchedulerCore scheduler = new SchedulerCore();

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("ServletContextListener destroyed");		
		scheduler.stop();
	}

    //Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("ServletContextListener started");
		scheduler.run();
	}
}