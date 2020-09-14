/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.darklaf.components.filetree;

import java.io.File;
import java.nio.file.Path;

import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;

public class FileTreeModel extends DefaultTreeModel {

    protected final FileSystemView fsv;
    protected boolean showHiddenFiles;

    public FileTreeModel(final FileSystemView fileSystemView) {
        this(fileSystemView, (Path) null, false);
    }

    public FileTreeModel(final FileSystemView fileSystemView, final File root, final boolean showHiddenFiles) {
        this(fileSystemView, root != null ? root.toPath() : null, showHiddenFiles);
    }

    public FileTreeModel(final FileSystemView fileSystemView, final Path root, final boolean showHiddenFiles) {
        super(null);
        init();
        this.showHiddenFiles = showHiddenFiles;
        this.fsv = fileSystemView;
        this.root = createRoot(root);
    }

    protected void init() {}

    protected FileTreeNode createRoot(final Path root) {
        if (root == null) {
            return new FileTreeNode.RootNode(this);
        } else {
            return createNode(null, root);
        }
    }

    @Override
    public void reload() {
        getRoot().reload();
    }

    @Override
    public FileTreeNode getRoot() {
        return (FileTreeNode) super.getRoot();
    }

    public void setShowHiddenFiles(final boolean showHiddenFiles) {
        if (showHiddenFiles == this.showHiddenFiles)
            return;
        this.showHiddenFiles = showHiddenFiles;
        reload();
    }

    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    protected FileTreeNode createNode(final FileTreeNode parent, final Path file) {
        return new FileTreeNode(parent, file, this);
    }

    protected void register(final FileTreeNode node) {}

    protected void unregister(final FileTreeNode node) {}
}
