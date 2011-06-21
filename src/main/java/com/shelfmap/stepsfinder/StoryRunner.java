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

import java.util.List;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepFinder;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class StoryRunner extends JUnitStory {
    
    public StoryRunner() {
        super();
        StoryPathResolver storyPathResolver = new AnnotationStoryPathResolver();
        Configuration configuration = new MostUsefulConfiguration()
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withFormats(Format.CONSOLE, Format.IDE_CONSOLE, Format.HTML)
                .withDefaultFormats()
             )
            .useParameterConverters(new ParameterConverters().addConverters(new MyDateConverter()))
            .useStoryPathResolver(storyPathResolver)
            .useStepFinder(new StepFinder(new StepFinder.ByLevenshteinDistance()));

        
        useConfiguration(configuration);
        
        
        configuredEmbedder().embedderControls()
                .doGenerateViewAfterStories(true)
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(false);
    }
    
    @Override
    public List<CandidateSteps> candidateSteps() {
        List<Object> steps = CandidateStepsFactory.createStepsInstances(this.getClass());
        return new InstanceStepsFactory(configuration(), steps).createCandidateSteps();
    }    
}
