package com.tutict.eip.harness.service;

import com.tutict.eip.harness.domain.DSLModel;

public interface DSLParser {

    DSLModel parse(String requirement);
}
