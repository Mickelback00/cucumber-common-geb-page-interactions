/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jdev.geb.cucumber.core

import cucumber.api.DataTable
import cucumber.api.Scenario
import geb.Page
import io.jdev.cucumber.variables.core.Decoder
import io.jdev.geb.cucumber.core.util.TableUtil

import java.lang.reflect.Modifier

class NavigationSteps extends StepsBase {
    PageFinder pageFinder

    public void before (Scenario scenario, Binding binding, PageFinder pageFinder, Decoder variableDecoder) {
        super.before(scenario, binding, variableDecoder)
        this.pageFinder = pageFinder
    }

    public void to(String pageName, Map params = [:]) {
        Class<? extends Page> pageClass = pageFinder.getPageClass(pageName)
        assert pageClass
        browser.to(params, pageClass)
    }

    public void to(String pageName, String niceParamName, String paramRawValue) {
        Class<? extends Page> pageClass = pageFinder.getPageClass(pageName)
        assert pageClass
        browser.to(getNiceParams(pageClass, niceParamName, paramRawValue), pageClass)
    }

    public void to(String path, DataTable dataTable) {
        to(path, tableToParams(dataTable))
    }

    public void via(String pageName, Map params = [:]) {
        Class<? extends Page> pageClass = pageFinder.getPageClass(pageName)
        assert pageClass
        browser.via(params, pageClass)
    }

    public void via(String pageName, String niceParamName, String paramRawValue) {
        Class<? extends Page> pageClass = pageFinder.getPageClass(pageName)
        assert pageClass
        browser.via(getNiceParams(pageClass, niceParamName, paramRawValue), pageClass)
    }

    public void via(String path, DataTable dataTable) {
        via(path, tableToParams(dataTable))
    }

    public void at(String pageName) {
        Class<? extends Page> pageClass = pageFinder.getPageClass(pageName)
        assert pageClass
        assert browser.at(pageClass)
    }

    public void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000)
        } catch(InterruptedException e) {}
    }

    public void go(String path, Map params = [:]) {
        browser.go(params, path)
    }

    public void go(String path, DataTable dataTable) {
        Map<String,Object> params = tableToParams(dataTable)
        browser.go(params, path)
    }

    private Map<String,Object> tableToParams(DataTable dataTable) {
        assert dataTable.cells(0).size() == 2, "must specify exactly one row for parameters"
        TableUtil.dataTableToMaps(variableScope, dataTable).first()
    }

    public void atPath(String path) {
        String expectedUrl = buildUrl(path)
        assert browser.driver.currentUrl == expectedUrl
    }

    private String buildUrl(String path) {
        URI uri = new URI(path)
        if(!uri.absolute) {
            uri = new URI(browser.baseUrl).resolve(uri)
        }
        uri.toString()
    }

    private static final String PARAM_NAMES_MAP_PROPERTY_NAME = 'paramNames'
    private String getParamFromName(Class<? extends Page> pageClass, String paramDescriptiveName) {
        MetaProperty prop = pageClass.metaClass.getMetaProperty(PARAM_NAMES_MAP_PROPERTY_NAME)
        assert prop && Modifier.isStatic(prop.modifiers),
                "Cannot look up parameter name $paramDescriptiveName on class $pageClass.name as it does not have a static $PARAM_NAMES_MAP_PROPERTY_NAME field"
        def paramNameMap = pageClass[PARAM_NAMES_MAP_PROPERTY_NAME]
        assert paramNameMap instanceof Map, "Cannot look up parameter name $paramDescriptiveName on class $pageClass.name as it the $PARAM_NAMES_MAP_PROPERTY_NAME field is not a java.util.Map"

        String paramName = paramNameMap[paramDescriptiveName]
        assert paramName, "Cannot look up parameter name $paramDescriptiveName on class $pageClass.name as it is not present in the $PARAM_NAMES_MAP_PROPERTY_NAME map"
        paramName
    }

    private Map<String,Object> getNiceParams(Class<? extends Page> pageClass, String niceParamName, String paramRawValue) {
        String paramName = getParamFromName(pageClass, niceParamName)
        def paramValue = variableScope.decodeVariable(paramRawValue)
        assert paramValue != null, "Could not find variable named $paramRawValue"
        [(paramName): paramValue]
    }

}