package se.arkalix.dto.binary;

public interface BinaryWriter {
    int writeOffset();

    void writeOffset(int offset);

    void write(byte b);

    void write(byte[] bytes);
}
