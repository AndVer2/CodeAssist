/*
 * Copyright 2015 the original author or authors.
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

package com.tyron.builder.model.internal.inspect;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.tyron.builder.model.internal.core.*;
import com.tyron.builder.model.internal.core.rule.describe.ModelRuleDescriptor;

public class ProjectionOnlyNodeInitializer implements NodeInitializer {
    private final ModelProjection[] projections;

    public ProjectionOnlyNodeInitializer(ModelProjection... projections) {
        this.projections = projections;
    }

    @Override
    public Multimap<ModelActionRole, ModelAction> getActions(ModelReference<?> subject, ModelRuleDescriptor descriptor) {
        return ImmutableSetMultimap.<ModelActionRole, ModelAction>builder()
            .put(ModelActionRole.Discover, AddProjectionsAction.of(subject, descriptor, projections))
            .build();
    }
}