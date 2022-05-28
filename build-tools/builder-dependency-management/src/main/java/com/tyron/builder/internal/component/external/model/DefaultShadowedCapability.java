/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tyron.builder.internal.component.external.model;

public class DefaultShadowedCapability implements ShadowedCapability {
    private final CapabilityInternal shadowed;
    private final String appendix;

    public DefaultShadowedCapability(CapabilityInternal shadowed, String appendix) {
        this.shadowed = shadowed;
        this.appendix = appendix;
    }

    @Override
    public String getAppendix() {
        return appendix;
    }

    @Override
    public CapabilityInternal getShadowedCapability() {
        return shadowed;
    }

    @Override
    public String getGroup() {
        return shadowed.getGroup();
    }

    @Override
    public String getName() {
        return shadowed.getName() + appendix;
    }

    @Override
    public String getVersion() {
        return shadowed.getVersion();
    }

    @Override
    public String getCapabilityId() {
        return shadowed.getCapabilityId() + appendix;
    }
}
