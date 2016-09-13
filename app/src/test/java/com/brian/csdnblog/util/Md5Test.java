package com.brian.csdnblog.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by huamm on 2016/9/13 0013.
 */
public class Md5Test {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetFileNameFromStr() throws Exception {
        assertEquals("test_path", Md5.getFileNameFromStr("test?path"));
    }

    @Test
    public void testGetMD5ofStr() throws Exception {

    }

    @Test
    public void testGetMD5ofStr1() throws Exception {

    }

    @Test
    public void testVerifyPassword() throws Exception {

    }

    @Test
    public void testVerifyPassword1() throws Exception {

    }

    @Test
    public void testGetMD5StringOfFile() throws Exception {

    }
}