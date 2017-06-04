package com.sh.jerry.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by jerry on 16/12/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DBSlaveConfig extends AbstractDisconfConfig {

    private String dbSlaves;
}
