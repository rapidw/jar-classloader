/**
 * Copyright 2023 Rapidw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rapidw.utils.jarclassloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.JarInputStream;

class Utils {

    static String pathToClassName(String path) {
        return path.substring(0, path.length() - 6).replace("/", ".");
    }

    static byte[] readCurrentJarEntry(JarInputStream jarInStream)
            throws IOException {
        // read the whole contents of the
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        while ((len = jarInStream.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        return out.toByteArray();
    }

    static class InputStreamURLStreamHandler extends URLStreamHandler {

        InputStream inputStream;
        public InputStreamURLStreamHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new InputStreamURLConnection(u, inputStream);
        }

        private static class InputStreamURLConnection extends URLConnection {
            private final InputStream inStream;
            public InputStreamURLConnection(URL url,InputStream inStream) {
                super(url);
                this.inStream = inStream;
            }

            @Override
            public InputStream getInputStream() {
                return inStream;
            }

            @Override
            public void connect() throws IOException {
            }
        }
    }
}
