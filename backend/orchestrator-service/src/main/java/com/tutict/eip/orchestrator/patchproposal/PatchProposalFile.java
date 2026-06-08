package com.tutict.eip.orchestrator.patchproposal;

public class PatchProposalFile {

    private String fileId;
    private String targetPath;
    private String generatedPath;
    private String diffPath;
    private PatchProposalChangeType changeType;
    private long bytesWritten;
    private String oldSha256;
    private String newSha256;
    private String rejectedReason;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getGeneratedPath() {
        return generatedPath;
    }

    public void setGeneratedPath(String generatedPath) {
        this.generatedPath = generatedPath;
    }

    public String getDiffPath() {
        return diffPath;
    }

    public void setDiffPath(String diffPath) {
        this.diffPath = diffPath;
    }

    public PatchProposalChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(PatchProposalChangeType changeType) {
        this.changeType = changeType;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public String getOldSha256() {
        return oldSha256;
    }

    public void setOldSha256(String oldSha256) {
        this.oldSha256 = oldSha256;
    }

    public String getNewSha256() {
        return newSha256;
    }

    public void setNewSha256(String newSha256) {
        this.newSha256 = newSha256;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }
}
