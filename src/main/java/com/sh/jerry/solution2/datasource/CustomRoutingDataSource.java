package com.sh.jerry.solution2.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.sh.jerry.config.DBSlaveConfig;
import com.sh.jerry.context.DBContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jerry on 16/12/17.
 */
public class CustomRoutingDataSource extends AbstractDataSource implements InitializingBean {

    @Autowired
    private DBSlaveConfig dbSlaveConfig;

    private Map<Object, Object> targetDataSources;

    private Object defaultTargetDataSource;

    private boolean lenientFallback = true;

    private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

    private Map<Object, DataSource> resolvedDataSources;

    private DataSource resolvedDefaultDataSource;

    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        this.targetDataSources = targetDataSources;
    }

    public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
        this.defaultTargetDataSource = defaultTargetDataSource;
    }

    public void setLenientFallback(boolean lenientFallback) {
        this.lenientFallback = lenientFallback;
    }

    public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
        this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
    }


    @Override
    public void afterPropertiesSet() {
        if (this.targetDataSources == null) {
            throw new IllegalArgumentException("Property 'targetDataSources' is required");
        }
        this.resolvedDataSources = new HashMap<>(this.targetDataSources.size());
        DruidDataSource druidDataSource = null;
        for (Map.Entry<Object, Object> entry : this.targetDataSources.entrySet()) {
            Object lookupKey = resolveSpecifiedLookupKey(entry.getKey());
            DataSource dataSource = resolveSpecifiedDataSource(entry.getValue());
            this.resolvedDataSources.put(lookupKey, dataSource);
            druidDataSource = ((DruidDataSource) dataSource);
        }

        this.initSlaveDataSource(this.resolvedDataSources, druidDataSource);

        if (this.defaultTargetDataSource != null) {
            this.resolvedDefaultDataSource = resolveSpecifiedDataSource(this.defaultTargetDataSource);
        }
    }

    protected Object resolveSpecifiedLookupKey(Object lookupKey) {
        return lookupKey;
    }


    protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
        if (dataSource instanceof DataSource) {
            return (DataSource) dataSource;
        } else if (dataSource instanceof String) {
            return this.dataSourceLookup.getDataSource((String) dataSource);
        } else {
            throw new IllegalArgumentException(
                    "Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
        }
    }


    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = this.resolvedDataSources.get(lookupKey);
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.resolvedDefaultDataSource;
        }
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }

    protected Object determineCurrentLookupKey() {
        return DBContext.getDBKey();
    }

    public void initSlaveDataSource(Map<Object, DataSource> dataSourceMap, DruidDataSource druidDataSource) {
        String dbSlaves = dbSlaveConfig.getDbSlaves();
        if (StringUtils.isBlank(dbSlaves)) {
            return;
        }

        if (dbSlaves.startsWith("$")) {
            throw new IllegalArgumentException("disconf jdbc.properties 'db_slaves' is illegal");
        }

        String[] dbSlavesArr = dbSlaves.split(",");
        if (dbSlaves.length() <= 0) {
            throw new IllegalArgumentException("disconf jdbc.properties 'db_slaves' is illegal");
        }

        DruidDataSource slaveDataSource;
        for (String dbSlave : dbSlavesArr) {
            slaveDataSource = druidDataSource.cloneDruidDataSource();
            slaveDataSource.setUrl(MasterSlaveConf.getSlaveUrl(dbSlave));
            slaveDataSource.setUsername(MasterSlaveConf.getSlaveUser(dbSlave));
            slaveDataSource.setPassword(MasterSlaveConf.getSlavePwd(dbSlave));
            dataSourceMap.put(MasterSlaveConf.DB_SLAVE + dbSlave, slaveDataSource);
        }
    }

}
