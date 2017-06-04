package com.sh.jerry.solution1.datasource;

import com.sh.jerry.context.DBContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by jerry on 6/6/16.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DBContext.getDBKey();
    }
}