package com.tutict.eip.agentadapter.storage;

import com.tutict.eip.agentadapter.domain.WrittenFile;

public interface CodeFileWriter {

    WrittenFile write(String targetPath, String content);
}
