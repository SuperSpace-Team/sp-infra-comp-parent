package com.sp.infra.comp.consul.common;

import com.ecwid.consul.v1.ConsulClient;
import com.sp.infra.comp.logger.log.CompLogger;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Properties;

import static com.sp.infra.comp.consul.log.LogType.LOG_TAG;

/**
 * @Description 公共ConsulClient
 * @Author alexlu
 * @date 2021.08.13
 */
public class CommonConsulClient {

    private static ConsulClient DEFAULT_CONSUL_CLIENT;

    private static  String consulK8sHostVMKEY;

    private static String consulK8sHostVM;

    private static String consulK8sPortVMKEY;

    private static Integer consulK8sPortVM;

    private static  String consulHost;

    private static  String consulPort;

    private static  String consulPath;

    static {
        Properties properties = new Properties();
        try (InputStream ios = CommonConsulClient.class.getClassLoader().getResourceAsStream("consul-config.properties");) {
            properties.load(ios);
            consulK8sHostVMKEY = properties.getProperty("base.config.consulK8sHost.vm.property");
            consulK8sPortVMKEY = properties.getProperty("base.config.consulK8sPort.vm.property");
            consulHost = properties.getProperty("base.config.consulHost");
            consulPort = properties.getProperty("base.config.consulPort");

            //K8S consul配置
            if(StringUtils.isNotBlank(consulK8sHostVMKEY)) {
                consulK8sHostVM = System.getProperty(consulK8sHostVMKEY);
            }

            if(StringUtils.isNotBlank(consulK8sPortVMKEY)) {
                String property = System.getProperty(consulK8sPortVMKEY);
                consulK8sPortVM = property!=null&&!"".equals(property)?Integer.parseInt(property):8500 ;
            }else {
                consulK8sPortVM=8500;
            }

            if(StringUtils.isNotBlank(consulK8sHostVM) && consulK8sPortVM != null) {
                //K8S consul
                CompLogger.debugx(LOG_TAG, "This is K8S env consul host : {} consul port : {}  ",
                        consulK8sHostVM, consulK8sPortVM);
                DEFAULT_CONSUL_CLIENT = new ConsulClient(consulK8sHostVM, consulK8sPortVM);
            }else {
                //默认本地Consul
                CompLogger.debugx(LOG_TAG,"This is default env consul host : {} consul port : {} ",
                        consulHost, consulPort);
                DEFAULT_CONSUL_CLIENT = new ConsulClient(consulHost,Integer.parseInt(consulPort));
            }
        } catch (Exception e) {
            CompLogger.warnx(LOG_TAG,"The comp common consul client initialized failed... " );
            CompLogger.error(LOG_TAG, e.getMessage());
        }
    }

    public static String getConsulK8sHostVM() {
        return consulK8sHostVM;
    }

    public static Integer getConsulK8sPortVM() {
        return consulK8sPortVM;
    }

    public static String getConsulHost() {
        return consulHost;
    }

    public static String getConsulPort() {
        return consulPort;
    }

    public static ConsulClient getDefaultConsulClient() {
        return DEFAULT_CONSUL_CLIENT;
    }
}