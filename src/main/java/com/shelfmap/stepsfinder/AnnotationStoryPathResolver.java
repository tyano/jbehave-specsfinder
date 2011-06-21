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

import org.jbehave.core.Embeddable;
import org.jbehave.core.io.StoryPathResolver;

/**
 *
 * @author Tsutomu YANO
 */
public class AnnotationStoryPathResolver implements StoryPathResolver {

    public AnnotationStoryPathResolver() {
        super();
    }

    @Override
    public String resolve(Class<? extends Embeddable> embeddableClass) {
        StoryPath storyPath = embeddableClass.getAnnotation(StoryPath.class);
        if(storyPath == null) {
            throw new IllegalStateException("You must put @StoryPath annotation on your Embedder for assigning the path for your story file.");
        }
        
        return storyPath.value();
    }
}
