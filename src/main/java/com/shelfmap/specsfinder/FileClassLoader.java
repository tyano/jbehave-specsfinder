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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Tsutomu YANO
 */
public class FileClassLoader extends ClassLoader {

    public FileClassLoader() {
    }

    public FileClassLoader(ClassLoader parent) {
        super(parent);
    }
    private static final int BUFFER_SIZE = 1024;

    public Class<?> loadClassFile(File input) throws IOException {
        FileInputStream inputStream = new FileInputStream(input);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] result;
        
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int size = inputStream.read(buffer, 0, BUFFER_SIZE);
            while (size != -1) {
                if (size > 0) {
                    outputStream.write(buffer, 0, size);
                }
                size = inputStream.read(buffer, 0, BUFFER_SIZE);
            }
            
            result = outputStream.toByteArray();
        } finally {
            inputStream.close();
            outputStream.close();
        }
        
        return defineClass(null, result, 0, result.length);
    }
}
