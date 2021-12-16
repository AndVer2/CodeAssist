package com.tyron.builder.project;

import com.tyron.builder.model.ProjectSettings;
import com.tyron.builder.project.api.Module;
import com.tyron.builder.project.impl.AndroidModuleImpl;
import com.tyron.builder.project.mock.MockProjectSettings;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class Project {

    private List<Module> mModules;
    private final Module mMainModule;
    private final File mRoot;
    
    public Project(File root) {
        mRoot = root;
        mMainModule = new AndroidModuleImpl(new File(mRoot, "app"));
    }
    
    public Module getMainModule() {
        return mMainModule;
    }

    public File getRootFile() {
        return mRoot;
    }

    public ProjectSettings getSettings() {
        return new MockProjectSettings();
    }

    public Module getModule(File file) {
        // TODO: implement this on modular project
        return getMainModule();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return mRoot.equals(project.mRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mRoot);
    }
}