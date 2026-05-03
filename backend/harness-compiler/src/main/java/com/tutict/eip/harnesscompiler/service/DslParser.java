package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslModel;

public interface DslParser {

    DslModel parse(String requirement);
}
