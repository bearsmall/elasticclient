package com.xy.parser.npm;

import com.xy.parser.npm.item.NpmDependency;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NpmParserTest {

    @Test
    public void test1(){
        NpmParser npmParser = new NpmParser();
        npmParser.parse("mysql","2.16.0");
        List<NpmDependency> npmDependencyList = npmParser.getNpmDependencyList();
        Set<NpmDependency> npmDependencySet = npmParser.getNpmDependencySet();
        System.out.println(npmDependencyList);
        System.out.println(npmDependencySet);
    }

}