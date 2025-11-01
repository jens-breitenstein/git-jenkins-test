package com.anevis.example.java24;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JenkinsParamsTest
{
    @Test
    public void testJenkinsParamsAsJdbcUrlParams()
    {
        System.out.println(System.getProperty("jenkins.jobName"));
        System.out.println(System.getProperty("jenkins.buildNumber"));
        System.out.println(System.getProperty("jenkins.nodeName"));

        Assertions.assertTrue(true);
    }
}
