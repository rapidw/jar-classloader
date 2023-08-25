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
