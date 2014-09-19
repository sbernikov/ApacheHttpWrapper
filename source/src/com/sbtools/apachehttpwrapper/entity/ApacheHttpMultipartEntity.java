package com.sbtools.apachehttpwrapper.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import com.sbtools.apachehttpwrapper.log.ApacheHttpWrapperLog;

public class ApacheHttpMultipartEntity implements HttpEntity {
    
    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private String mBoundary = null;

    private ByteArrayOutputStream mOut = new ByteArrayOutputStream();
    private boolean mIsSetLast = false;
    private boolean mIsSetFirst = false;

    public ApacheHttpMultipartEntity() {
        final StringBuffer buf = new StringBuffer();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        this.mBoundary = buf.toString();

    }

    public void writeFirstBoundaryIfNeeds(){
        if (!mIsSetFirst){
            try {
                mOut.write(("--" + mBoundary + "\r\n").getBytes());
            } catch (final IOException e) {
                ApacheHttpWrapperLog.e(e.getMessage());
            }
        }
        mIsSetFirst = true;
    }

    public void writeLastBoundaryIfNeeds() {
        if(mIsSetLast){
            return ;
        }
        try {
            mOut.write(("\r\n--" + mBoundary + "--\r\n").getBytes());
        } catch (final IOException e) {
            ApacheHttpWrapperLog.e(e.getMessage());
        }
        mIsSetLast = true;
    }

    public void addPart(final String key, final String value) {
        writeFirstBoundaryIfNeeds();
        try {
            mOut.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes());
            mOut.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes());
            mOut.write("Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes());
            mOut.write(value.getBytes());
            mOut.write(("\r\n--" + mBoundary + "\r\n").getBytes());
        } catch (final IOException e) {
            ApacheHttpWrapperLog.e(e.getMessage());
        }
    }

    public void addPart(final String key, final String fileName, final InputStream fin){
        addPart(key, fileName, fin, "application/octet-stream");
    }

    public void addPart(final String key, final String fileName, final InputStream fin, String type){
        writeFirstBoundaryIfNeeds();
        try {
            type = "Content-Type: " + type + "\r\n";
            mOut.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
            mOut.write(type.getBytes());
            mOut.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

            final byte[] tmp = new byte[4096];
            int l = 0;
            while ((l = fin.read(tmp)) != -1) {
                mOut.write(tmp, 0, l);
            }
            mOut.flush();
        } catch (final IOException e) {
            ApacheHttpWrapperLog.e(e.getMessage());
        } finally {
            try {
                fin.close();
            } catch (final IOException e) {
                ApacheHttpWrapperLog.e(e.getMessage());
            }
        }
    }

    public void addPart(final String key, final File value) {
        try {
            addPart(key, value.getName(), new FileInputStream(value));
        } catch (final FileNotFoundException e) {
            ApacheHttpWrapperLog.e(e.getMessage());
        }
    }

    @Override
    public long getContentLength() {
        writeLastBoundaryIfNeeds();
        return mOut.toByteArray().length;
    }

    @Override
    public Header getContentType() {
        return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + mBoundary);
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        outstream.write(mOut.toByteArray());
    }

    @Override
    public Header getContentEncoding() {
        return null;
    }

    @Override
    public void consumeContent() throws IOException,
    UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException(
            "Streaming entity does not implement #consumeContent()");
        }
    }

    @Override
    public InputStream getContent() throws IOException,
    UnsupportedOperationException {
        return new ByteArrayInputStream(mOut.toByteArray());
    }
}