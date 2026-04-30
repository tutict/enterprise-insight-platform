package com.tutict.eip.agentadapter.domain;

public class GeneratedProjectFile {

    private String relativePath;
    private String absolutePath;
    private long bytesWritten;

    public GeneratedProjectFile() {
    }

    public GeneratedProjectFile(String relativePath, String absolutePath, long bytesWritten) {
        this.relativePath = relativePath;
        this.absolutePath = absolutePath;
        this.bytesWritten = bytesWritten;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }
}
