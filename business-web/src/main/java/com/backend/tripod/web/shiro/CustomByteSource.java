package com.backend.tripod.web.shiro;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.util.ByteSource;

import java.io.Serializable;
import java.util.Arrays;

public class CustomByteSource implements ByteSource, Serializable {

    private static final long serialVersionUID = 7655402676924785830L;

    private final byte[] bytes;
    private String cachedHex;
    private String cachedBase64;

    public CustomByteSource(String source) {
        this.bytes = CodecSupport.toBytes(source);
    }

    @Override
    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public String toHex() {
        if (this.cachedHex == null) {
            this.cachedHex = Hex.encodeToString(getBytes());
        }
        return this.cachedHex;
    }

    @Override
    public String toBase64() {
        if (this.cachedBase64 == null) {
            this.cachedBase64 = Base64.encodeToString(getBytes());
        }
        return this.cachedBase64;
    }

    @Override
    public boolean isEmpty() {
        return this.bytes == null || this.bytes.length == 0;
    }

    public String toString() {
        return toBase64();
    }

    public int hashCode() {
        if (this.bytes == null || this.bytes.length == 0) {
            return 0;
        }
        return Arrays.hashCode(this.bytes);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof CustomByteSource) {
            CustomByteSource bs = (CustomByteSource) o;
            return Arrays.equals(getBytes(), bs.getBytes());
        }
        return false;
    }

}
