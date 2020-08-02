package cn.wode490390.nukkit.geoip.util;

import SevenZip.Compression.LZMA.Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class LZMALib {

    public static void decode(InputStream inputStream, OutputStream outputStream) throws IOException {
        int propertiesSize = 5;
        byte[] properties = new byte[propertiesSize];
        if (inputStream.read(properties, 0, propertiesSize) != propertiesSize) {
            throw new IOException("input .lzma file is too short");
        }
        Decoder decoder = new Decoder();
        if (!decoder.SetDecoderProperties(properties)) {
            throw new IOException("Incorrect stream properties");
        }
        long outSize = 0;
        for (int i = 0; i < 8; i++) {
            int v = inputStream.read();
            if (v < 0) {
                throw new IOException("Can't read stream size");
            }
            outSize |= ((long) v) << (8 * i);
        }
        if (!decoder.Code(inputStream, outputStream, outSize)) {
            throw new IOException("Error in data stream");
        }
    }

    private LZMALib() {

    }
}
