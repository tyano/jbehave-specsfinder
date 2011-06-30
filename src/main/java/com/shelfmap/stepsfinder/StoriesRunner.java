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
package com.shelfmap.stepsfinder;

import com.shelfmap.stepsfinder.CandidateStepsFactory;
import com.shelfmap.stepsfinder.MyDateConverter;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.transform;
import java.util.List;
import org.apache.commons.collections.Transformer;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepFinder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
@RunWith(JUnit4.class)
public class StoriesRunner extends JUnitStories {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoriesRunner.class);
    
    @Override
    public org.jbehave.core.configuration.Configuration configuration() {
        org.jbehave.core.configuration.Configuration configuration = new MostUsefulConfiguration()
            .useStoryReporterBuilder(
                new StoryReporterBuilder()
                    .withFormats(Format.CONSOLE, Format.IDE_CONSOLE, Format.HTML)
                    .withDefaultFormats())
            .useParameterConverters(new ParameterConverters().addConverters(new MyDateConverter()))
            .useStepFinder(new StepFinder(new StepFinder.ByLevenshteinDistance()));
        
        return configuration;
    }    
    
    protected String getStoryPathInClasspath() {
        return "stories";
    }
    
    @Override
    protected List<String> storyPaths() {
        final String directory = CodeLocations.codeLocationFromClass(getClass()).getFile() + getStoryPathInClasspath();
        List<String> paths = new StoryFinder().findPaths(directory, asList("**/*.story"), null);

        transform(paths, new Transformer() {

            @Override
            public Object transform(Object input) {
                return getStoryPathInClasspath() + "/" + input;
            }
        });

        return paths;
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<Object> steps = CandidateStepsFactory.createStepsInstances(this.getClass());
        return new InstanceStepsFactory(configuration(), steps).createCandidateSteps();
    }
}
