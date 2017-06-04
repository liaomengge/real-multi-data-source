package com.sh.jerry.solution2.datasource;

import com.baidu.disconf.client.usertools.DisconfDataGetter;
import com.sh.jerry.util.StringUtil;

/**
 * Created by jerry on 16/12/17.
 */
public final class MasterSlaveConf {

    public static final String DB_MASTER = "master";
    public static final String DB_SLAVE = "slave";

    private static final String SLAVE_URL = "jdbc-url-dbname-slave";
    private static final String SLAVE_USER = "jdbc-user-dbname-slave";
    private static final String SLAVE_PASSWORD = "jdbc-password-dbname-slave";

    public static String getSlaveUrl(String dbSlave) {
        return StringUtil.getValue(DisconfDataGetter.getByFileItem("jdbc.properties", MasterSlaveConf.SLAVE_URL + dbSlave));
    }

    public static String getSlaveUser(String dbSlave) {
        return StringUtil.getValue(DisconfDataGetter.getByFileItem("jdbc.properties", MasterSlaveConf.SLAVE_USER + dbSlave));
    }

    public static String getSlavePwd(String dbSlave) {
        return StringUtil.getValue(DisconfDataGetter.getByFileItem("jdbc.properties", MasterSlaveConf.SLAVE_PASSWORD + dbSlave));
    }
}
