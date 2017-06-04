package com.sh.jerry.aspect;

import com.google.common.collect.Lists;
import com.sh.jerry.config.DBSlaveConfig;
import com.sh.jerry.context.DBContext;
import com.sh.jerry.context.Master;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by jerry on 16/8/29.
 */
@Component("masterSlaveAspect")
public class MasterSlaveAspect {

    @Autowired
    private DBSlaveConfig dbSlaveConfig;

    private static final List<String> writeMethod = Lists.newArrayList("update", "insert", "delete", "save");
    private static final String DB_MASTER = "master";
    private static final String DB_SLAVE = "slave";

    public void doBefore(JoinPoint point) {
        Object target = point.getTarget();
        Class<?>[] clazz = target.getClass().getInterfaces();

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        //约定Mappper上必须打@Resource注解
        if (clazz[0].isAnnotationPresent(Resource.class)) {

            //方法上的@Master/@Slave注解优先
            DBContext.setDBKey(this.chooseSlave());
            if (method.isAnnotationPresent(Master.class)) {
                DBContext.setDBKey(DB_MASTER);
                return;
            }

            //然后根据方法前缀
            String methodName = point.getSignature().getName();
            if (isWriteMethod(methodName)) {
                DBContext.setDBKey(DB_MASTER);
            }
        }
    }

    private String chooseSlave() {
        String dbSlaves = dbSlaveConfig.getDbSlaves();
        if (StringUtils.isBlank(dbSlaves)) {
            return DB_MASTER;
        }

        String[] dbSlavesArr = dbSlaves.split(",");
        int len = dbSlavesArr.length;
        if (len == 0) {
            return DB_MASTER;
        }

        int mod = (int) (System.currentTimeMillis() % len);
        return DB_SLAVE + StringUtils.trim(dbSlavesArr[mod]);
    }

    private boolean isWriteMethod(String methodName) {
        for (String prefix : writeMethod) {
            if (methodName.toLowerCase().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
