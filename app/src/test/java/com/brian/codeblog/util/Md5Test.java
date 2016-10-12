package com.brian.codeblog.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by huamm on 2016/9/13 0013.
 */
public class Md5Test {

    @Test
    public void testGetFileNameFromStr() throws Exception {
        assertEquals("test_path", Md5.getFileNameFromStr("test?path"));
    }
}