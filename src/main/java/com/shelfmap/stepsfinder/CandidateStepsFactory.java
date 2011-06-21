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

import static org.apache.commons.collections.CollectionUtils.transform;
import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;
import static org.apache.commons.lang.StringUtils.removeEnd;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.Transformer;
import org.jbehave.core.io.StoryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class CandidateStepsFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CandidateStepsFactory.class);
    
    private CandidateStepsFactory() {
        super();
    }
    
    public static URL codeLocationFromParentPackage(Class<?> codeLocationClass) {
        String simpleName = codeLocationClass.getSimpleName() + ".class";
        String pathOfClass = codeLocationClass.getName().replace(".", "/") + ".class";
        URL classResource = codeLocationClass.getClassLoader().getResource(pathOfClass);
        String codeLocationPath = removeEnd(classResource.getFile(), simpleName);
        return codeLocationFromPath(codeLocationPath);
    }

    public static String packagePath(Class<?> codeLocationClass) {
        String classPath = codeLocationClass.getName().replace(".", "/");
        return removeEnd(classPath, codeLocationClass.getSimpleName());
    }
    
    public static List<Object> createStepsInstances(Class<?> embedderClass) {
        final String classPath = packagePath(embedderClass);
        List<String> paths = new StoryFinder().findPaths(
                codeLocationFromParentPackage(embedderClass).getFile(),
                asList("**/*.class"),
                null);

        transform(paths, new Transformer() {
            @Override
            public Object transform(Object input) {
                return classPath + (removeEnd((String)input, ".class"));
            }
        });

        List<Object> steps = new ArrayList<Object>();
        for (String path : paths) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(path.replace("/", "."));
                if(clazz.isAnnotationPresent(Steps.class)) {
                    steps.add(clazz.newInstance());
                }
            } catch (InstantiationException ex) {
                LOGGER.error("Could not instanciate a class: " + clazz.getCanonicalName(), ex);
            } catch (IllegalAccessException ex) {
                LOGGER.error("Could not access ot the constructer of the class: " + clazz.getCanonicalName(), ex);
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Cound not load a class of path: " + path, ex);
            }
        }
        return steps;        
    }
}
