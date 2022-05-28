/*
 * Copyright 2012 the original author or authors.
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

package com.tyron.builder.api.internal.artifacts.ivyservice.resolveengine.result;

import com.tyron.builder.internal.resolve.ModuleVersionResolveException;

import com.tyron.builder.api.artifacts.ModuleVersionIdentifier;
import com.tyron.builder.api.artifacts.ModuleVersionSelector;
import com.tyron.builder.api.artifacts.UnresolvedDependency;
import com.tyron.builder.api.artifacts.component.ComponentIdentifier;
import com.tyron.builder.api.artifacts.component.ModuleComponentSelector;
import com.tyron.builder.api.artifacts.result.ComponentSelectionCause;
import com.tyron.builder.api.artifacts.result.ComponentSelectionReason;
import com.tyron.builder.api.artifacts.result.DependencyResult;
import com.tyron.builder.api.artifacts.result.ResolutionResult;
import com.tyron.builder.api.artifacts.result.ResolvedComponentResult;
import com.tyron.builder.api.artifacts.result.ResolvedDependencyResult;
import com.tyron.builder.api.artifacts.result.ResolvedVariantResult;
import com.tyron.builder.api.attributes.AttributeContainer;
import com.tyron.builder.api.internal.artifacts.ivyservice.resolveengine.graph.ResolvedGraphComponent;
import com.tyron.builder.api.internal.artifacts.ivyservice.resolveengine.graph.ResolvedGraphDependency;
import com.tyron.builder.api.internal.artifacts.result.DefaultResolutionResult;
import com.tyron.builder.api.internal.artifacts.result.DefaultResolvedComponentResult;
import com.tyron.builder.internal.Describables;
import com.tyron.builder.internal.Factory;
import com.tyron.builder.internal.component.external.model.DefaultModuleComponentSelector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultResolutionResultBuilder {
    private static final DefaultComponentSelectionDescriptor DEPENDENCY_LOCKING = new DefaultComponentSelectionDescriptor(ComponentSelectionCause.CONSTRAINT, Describables.of("Dependency locking"));
    private final Map<Long, DefaultResolvedComponentResult> modules = new HashMap<>();
    private final CachingDependencyResultFactory dependencyResultFactory = new CachingDependencyResultFactory();
    private AttributeContainer requestedAttributes;

    public static ResolutionResult empty(ModuleVersionIdentifier id, ComponentIdentifier componentIdentifier, AttributeContainer attributes) {
        DefaultResolutionResultBuilder builder = new DefaultResolutionResultBuilder();
        builder.setRequestedAttributes(attributes);
        builder.visitComponent(new DetachedComponentResult(0L, id, ComponentSelectionReasons.root(), componentIdentifier, Collections.emptyList(), null));
        return builder.complete(0L);
    }

    public void setRequestedAttributes(AttributeContainer attributes) {
        requestedAttributes = attributes;
    }

    public ResolutionResult complete(Long rootId) {
        return new DefaultResolutionResult(new RootFactory(modules.get(rootId)), requestedAttributes);
    }

    public void visitComponent(ResolvedGraphComponent component) {
        create(component.getResultId(), component.getModuleVersion(), component.getSelectionReason(), component.getComponentId(), component.getResolvedVariants(), component.getRepositoryName());
    }

    public void visitOutgoingEdges(Long fromComponent, Collection<? extends ResolvedGraphDependency> dependencies) {
        DefaultResolvedComponentResult from = modules.get(fromComponent);
        for (ResolvedGraphDependency d : dependencies) {
            DependencyResult dependencyResult;
            ResolvedVariantResult fromVariant = d.getFromVariant();
            if (d.getFailure() != null) {
                dependencyResult = dependencyResultFactory.createUnresolvedDependency(d.getRequested(), from, d.isConstraint(), d.getReason(), d.getFailure());
            } else {
                DefaultResolvedComponentResult selected = modules.get(d.getSelected());
                if (selected == null) {
                    throw new IllegalStateException("Corrupt serialized resolution result. Cannot find selected module (" + d.getSelected() + ") for " + (d.isConstraint() ? "constraint " : "") + fromVariant + " -> " + d.getRequested().getDisplayName());
                }
                dependencyResult = dependencyResultFactory.createResolvedDependency(d.getRequested(), from, selected, d.getSelectedVariant(), d.isConstraint());
                selected.addDependent((ResolvedDependencyResult) dependencyResult);
            }
            from.addDependency(dependencyResult);
            if (fromVariant != null) {
                from.associateDependencyToVariant(dependencyResult, fromVariant);
            }
        }
    }

    private void create(Long id, ModuleVersionIdentifier moduleVersion, ComponentSelectionReason selectionReason, ComponentIdentifier componentId, List<ResolvedVariantResult> variants, String repoName) {
        if (!modules.containsKey(id)) {
            modules.put(id, new DefaultResolvedComponentResult(moduleVersion, selectionReason, componentId, variants, repoName));
        }
    }

    public void addExtraFailures(Long rootId, Set<UnresolvedDependency> extraFailures) {
        DefaultResolvedComponentResult root = modules.get(rootId);
        for (UnresolvedDependency failure : extraFailures) {
            ModuleVersionSelector failureSelector = failure.getSelector();
            ModuleComponentSelector failureComponentSelector = DefaultModuleComponentSelector.newSelector(failureSelector.getModule(), failureSelector.getVersion());
            root.addDependency(dependencyResultFactory.createUnresolvedDependency(failureComponentSelector, root, true,
                    ComponentSelectionReasons.of(DEPENDENCY_LOCKING),
                new ModuleVersionResolveException(failureComponentSelector, () -> "Dependency lock state out of date", failure.getProblem())));
        }
    }

    private static class RootFactory implements Factory<ResolvedComponentResult> {
        private final DefaultResolvedComponentResult rootModule;

        public RootFactory(DefaultResolvedComponentResult rootModule) {
            this.rootModule = rootModule;
        }

        @Override
        public ResolvedComponentResult create() {
            return rootModule;
        }
    }
}
