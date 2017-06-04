# **real-multi-data-source**

**1. 问题描述**
    现在一般数据库的配置都是主从结构，所以在项目中一般都是会配置多数据源，网上一大堆，不是本篇叙述的重点。本篇主要讲的是，如何在多数据源（一主多从）下，如果其中一个从down了，快速的恢复服务（当然，直接恢复从节点是最快的，本篇主要是如果从节点无法在短时间内回复的情况下叙述的），而不需要改任何代码~~~

**2. 解决方案**
    - 方案一
    最简单的方案，就是你有多少个从（比如：3个），那么就将这三个从全部配置在数据源中，通过disconf配置，只需要重启服务，便解决部分从节点down了快速恢复。
    缺点：如果从比较多，配置臃肿
    - 方案二
    重新AbstractDataSource类，在Master数据源初始化的时候，动态的初始化从数据源（看源码的实现，一目了然），这样就不需要配置多数据源了，只需要在xml配置主和默认的数据源了，如下：
>    
    <!--db数据源-->
    <bean id="dsRestaurant" class="com.sh.jerry.solution2.datasource.CustomRoutingDataSource">
        <property name="targetDataSources">
            <map key-type="java.lang.String">
                <entry key="master" value-ref="dsRestaurant_master"/>
            </map>
        </property>
        <property name="defaultTargetDataSource" ref="dsRestaurant_master"/>
    </bean>
    
同样的也需要重启服务，修改disconf配置
    缺点：代码侵入，比较复杂
    
如有更好的解决方案，希望留言探讨下，谢谢...
    