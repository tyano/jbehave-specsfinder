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
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CasePreservingResolver;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStory;
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
public abstract class StoryRunner extends JUnitStory {

    private boolean recursive;
    private String regularExpression;
    
    public StoryRunner() throws IOException, URISyntaxException, InstantiationException, IllegalAccessException {
        super();
        StoryPathResolver storyPathResolver = new CasePreservingResolver(".story");
        Configuration configuration = new MostUsefulConfiguration()
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withFormats(CONSOLE, IDE_CONSOLE, HTML).withDefaultFormats())
            .useParameterConverters(new ParameterConverters().addConverters(new MyDateConverter()))
            .useStoryPathResolver(storyPathResolver);
        
        useConfiguration(configuration);
        
        
        configuredEmbedder().embedderControls()
                .doGenerateViewAfterStories(true)
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(false);
        
        this.recursive = false;
        this.regularExpression = this.getClass().getSimpleName() + "Steps\\.class";
        
        
    }
    
    @Test
    @Override
    public void run() throws Throwable {
        addSteps(createSteps(configuration()));
        super.run();
    }
    
    public StoryRunner recursive(boolean isRecursive) {
        this.recursive = isRecursive;
        return this;
    }
    
    public StoryRunner regularExpression(String regex) {
        this.regularExpression = regex;
        return this;
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
        
        IOFileFilter dirFileFilter = recursive ? TrueFileFilter.INSTANCE : null;
        IOFileFilter fileFileFilter = new RegexFileFilter(regularExpression);
        
        @SuppressWarnings("unchecked")
        Iterator<File> fileIterator = FileUtils.iterateFiles(startDirectory, fileFileFilter, dirFileFilter);
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

    public static class MyDateConverter extends DateConverter {

        public MyDateConverter() {
            super(new SimpleDateFormat("yyyy-MM-dd"));
        }
    }
}
