package com.su.wemedia.config;


import com.su.model.common.wemedia.pojos.WmSensitive;
import com.su.utils.common.SensitiveWordUtil;
import com.su.wemedia.mapper.WmSensitiveMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 写一个类继承ApplicationContextAware
 */
@Component
public class InitLoadService implements ApplicationContextAware {

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;
    /**
     * 这是ApplicationContextAware接口中定义的一个方法。
     * 当类的实例被创建并注册到Spring容器中时，Spring容器会调用这个方法，传递容器自身的引用。
     * 这允许在类中存储ApplicationContext的引用，进而可以用来获取其他的Beans、资源、配置等。
     *
     * 即：程序启动就会初始化一次。程序启动时会调用这个方法
     */
    @Override        //ApplicationContext是Spring中高级的IoC容器接口。
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //查询
        List<WmSensitive> SensitivesList = wmSensitiveMapper.selectList( null);
        //2
        // List<String> list = new ArrayList<>();
        // for (WmSensitive wmSensitive : wmSensitives) {
        //     list.add(wmSensitive.getSensitives());
        // }

        List<String> list = SensitivesList.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        SensitiveWordUtil.initMap(list);
    }
}
