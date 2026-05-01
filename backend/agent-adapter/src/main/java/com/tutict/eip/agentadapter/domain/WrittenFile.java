package com.tutict.eip.agentadapter.domain;

public class WrittenFile {

    private final String targetPath;
    private final String absolutePath;
    private final long bytesWritten;

    public WrittenFile(String targetPath, String absolutePath, long bytesWritten) {
        this.targetPath = targetPath;
        this.absolutePath = absolutePath;
        this.bytesWritten = bytesWritten;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }
}
