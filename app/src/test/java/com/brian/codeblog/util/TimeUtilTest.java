package com.brian.codeblog.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by huamm on 2016/10/10 0010.
 */
public class TimeUtilTest {

    @Test
    public void convCountTime() throws Exception {
        assertEquals("1小时1分钟", TimeUtil.convCountTime(3680_000));
    }

}