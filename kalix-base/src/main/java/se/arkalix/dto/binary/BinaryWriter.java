package se.arkalix.dto.binary;

public interface BinaryWriter {
    int writeOffset();

    void writeOffset(int offset);

    int writableBytes();

    void write(byte b);

    void write(byte[] bytes);
}
