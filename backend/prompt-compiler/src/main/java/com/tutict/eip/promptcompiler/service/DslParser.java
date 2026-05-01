package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.domain.DslDocument;

public interface DslParser {

    DslDocument parse(String yaml);
}
