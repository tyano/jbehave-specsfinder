/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.specsfinder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import static org.jbehave.core.io.CodeLocations.*;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.*;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.junit.Test;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class StoryRunner extends JUnitStories {

    public StoryRunner() throws IOException, URISyntaxException, InstantiationException, IllegalAccessException {
        super();
        Configuration configuration = new MostUsefulConfiguration()
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withFormats(CONSOLE, IDE_CONSOLE, HTML).withDefaultFormats())
            .useParameterConverters(new ParameterConverters().addConverters(new MyDateConverter()));
        
        useConfiguration(configuration);
        
        
        configuredEmbedder().embedderControls()
                .doGenerateViewAfterStories(true)
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(false);
    }
    
    @Test
    @Override
    public void run() throws Throwable {
        addSteps(createSteps(configuration()));
        super.run();
    }
    
    /**
     * Subclasses must override this method.
     * The overriding method must return a classpath where searching specs files start from.<br>
     * The path must be the path of a package.
     * <p>
     * ex) "my.class.path.for.search"
     * @return a classpath string, where searching specs files start from.
     */
    protected abstract String classpathForSearch();

    protected List<CandidateSteps> createSteps(Configuration configuration) throws IOException, URISyntaxException, InstantiationException, IllegalAccessException {
        return new InstanceStepsFactory(configuration, createStepsInstances()).createCandidateSteps();
    }
    
    protected List<Object> createStepsInstances() throws IOException, URISyntaxException, InstantiationException, IllegalAccessException {
        List<Object> instances = new ArrayList<Object>();
        for (Class<?> clazz : findStepsClasses()) {
            instances.add(clazz.newInstance());
        }
        return instances;
    }
    
    private String fullPathForDirectoryOfClass(Class<?> clazz) throws URISyntaxException {
        URL url = clazz.getResource("");
        return new File(url.toURI()).getAbsolutePath();
    }    
    
    private List<Class<?>> findStepsClasses() throws IOException, URISyntaxException {
        FileClassLoader loader = new FileClassLoader(getClass().getClassLoader());
        URL rootUrl = loader.getResource("");
        File startDirectory = new File(new File(rootUrl.toURI()), classpathForSearch().replace('.', '/'));
        
        @SuppressWarnings("unchecked")
        Iterator<File> fileIterator = FileUtils.iterateFiles(startDirectory, new String[]{"class"}, true);
        List<Class<?>> stepClasses = new ArrayList<Class<?>>();
         
        while(fileIterator.hasNext()) {
            File classFile = fileIterator.next();
            Class<?> clazz = loader.loadClassFile(classFile);
            if(clazz.isAnnotationPresent(Steps.class)) {
                stepClasses.add(clazz);
            }
        }
        return stepClasses;
    }

    @Override
    protected List<String> storyPaths() {
        try {
            URL search = codeLocationFromClass(this.getClass());
            String searchPath = new File(search.toURI()).getAbsolutePath();
            String include = StringUtils.removeStart(fullPathForDirectoryOfClass(this.getClass()) + "/**/*.story", searchPath + "/");
            List<String> storyPaths = new StoryFinder().findPaths(search, include, "");
            return storyPaths;
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("the location where classes exist is not on a file system. This class is aplicable only for classes on any filesystem.");
        }
    }

    public static class MyDateConverter extends DateConverter {

        public MyDateConverter() {
            super(new SimpleDateFormat("yyyy-MM-dd"));
        }
    }
}
