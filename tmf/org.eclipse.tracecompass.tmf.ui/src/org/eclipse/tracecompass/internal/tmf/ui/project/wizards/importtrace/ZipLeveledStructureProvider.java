/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat, Inc - Was ZipFileStructureProvider, performed changes from
 *     IImportStructureProvider to ILeveledImportStructureProvider
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *     Marc-Andre Laperle <marc-andre.laperle@ericsson.com> - Copied to Trace
 *     Compass to use Apache Common Compress and fix bug 501664
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;

/**
 * This class provides information regarding the context structure and content
 * of specified zip file entry objects.
 *
 * This structure provider also makes sure to return safe paths. For example,
 * if a Zip entry contains a ':' and is extracted on Windows, it will be changed
 * to a '_'
 */
@SuppressWarnings("restriction")
public class ZipLeveledStructureProvider implements
        ILeveledImportStructureProvider {
    private ZipFile zipFile;

    private ZipArchiveEntry root = new ZipArchiveEntry("/");//$NON-NLS-1$

    private Map<ZipArchiveEntry, List<ZipArchiveEntry>> children;

    private Map<IPath, ZipArchiveEntry> directoryEntryCache = new HashMap<>();

    private int stripLevel;

    /**
     * Creates a <code>ZipFileStructureProvider</code>, which will operate on
     * the passed zip file.
     *
     * @param sourceFile
     *            The source file to create the ZipLeveledStructureProvider
     *            around
     */
    public ZipLeveledStructureProvider(ZipFile sourceFile) {
        super();
        zipFile = sourceFile;
        stripLevel = 0;
    }

    /**
     * Creates a new container zip entry with the specified name, iff it has
     * not already been created. If the parent of the given element does not
     * already exist it will be recursively created as well.
     * @param pathname The path representing the container
     * @return The element represented by this pathname (it may have already existed)
     */
    protected ZipArchiveEntry createContainer(IPath pathname) {
        ZipArchiveEntry existingEntry = directoryEntryCache.get(pathname);
        if (existingEntry != null) {
            return existingEntry;
        }

        ZipArchiveEntry parent;
        if (pathname.segmentCount() == 0) {
            return null;
        } else if (pathname.segmentCount() == 1) {
            parent = root;
        } else {
            parent = createContainer(pathname.removeLastSegments(1));
        }
        ZipArchiveEntry newEntry = new ZipArchiveEntry(pathname.toString());
        directoryEntryCache.put(pathname, newEntry);
        List<ZipArchiveEntry> childList = new ArrayList<>();
        children.put(newEntry, childList);

        List<ZipArchiveEntry> parentChildList = children.get(parent);
        NonNullUtils.checkNotNull(parentChildList).add(newEntry);
        return newEntry;
    }

    /**
     * Creates a new file zip entry with the specified name.
     * @param entry the entry to create the file for
     */
    protected void createFile(ZipArchiveEntry entry) {
        IPath pathname = new Path(entry.getName());
        ZipArchiveEntry parent;
        if (pathname.segmentCount() == 1) {
            parent = root;
        } else {
            parent = directoryEntryCache.get(pathname
                    .removeLastSegments(1));
        }

        @Nullable List<ZipArchiveEntry> childList = children.get(parent);
        NonNullUtils.checkNotNull(childList).add(entry);
    }

    @Override
    public List getChildren(Object element) {
        if (children == null) {
            initialize();
        }

        return (children.get(element));
    }

    @Override
    public InputStream getContents(Object element) {
        try {
            return zipFile.getInputStream((ZipArchiveEntry) element);
        } catch (IOException e) {
            IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /*
     * Strip the leading directories from the path
     */
    private String stripPath(String path) {
        String strippedPath = path;
        String pathOrig = strippedPath;
        for (int i = 0; i < stripLevel; i++) {
            int firstSep = strippedPath.indexOf('/');
            // If the first character was a separator we must strip to the next
            // separator as well
            if (firstSep == 0) {
                strippedPath = strippedPath.substring(1);
                firstSep = strippedPath.indexOf('/');
            }
            // No separator was present so we're in a higher directory right
            // now
            if (firstSep == -1) {
                return pathOrig;
            }
            strippedPath = strippedPath.substring(firstSep);
        }
        return strippedPath;
    }

    @Override
    public String getFullPath(Object element) {
        String name = ((ZipArchiveEntry) element).getName();
        return ArchiveUtil.toValidNamesPath(name).toOSString();
    }

    @Override
    public String getLabel(Object element) {
        if (element.equals(getRoot())) {
            return ((ZipArchiveEntry) element).getName();
        }
        String name = ((ZipArchiveEntry) element).getName();
        return stripPath(ArchiveUtil.toValidNamesPath(name).lastSegment());
    }

    /**
     * Returns the entry that this importer uses as the root sentinel.
     *
     * @return ZipArchiveEntry
     */
    @Override
    public Object getRoot() {
        return root;
    }

    /**
     * Returns the zip file that this provider provides structure for.
     *
     * @return The zip file
     */
    public ZipFile getZipFile() {
        return zipFile;
    }

    @Override
    public boolean closeArchive() {
        try {
            getZipFile().close();
        } catch (IOException e) {
            IDEWorkbenchPlugin.log(DataTransferMessages.ZipImport_couldNotClose
                    + getZipFile(), e);
            return false;
        }
        return true;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     */
    // @Override annotation cannot be used with pre-e4.29 targets
    public void close() {
        closeArchive();
    }

    /**
     * Initializes this object's children table based on the contents of the
     * specified source file.
     *
     * Since an archive entry could also be an archive: a variable counting the
     * number of file entries was added to the method as per SonarCloud
     * suggestion:
     * https://sonarcloud.io/organizations/eclipse/rules?open=java%3AS5042&rule_key=java%3AS5042&tab=how_to_fix
     */
    protected void initialize() {
        children = new HashMap<>(1000);

        children.put(root, new ArrayList<>());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        long archiveEntries = 0;
        long archiveSize = 0;
        long entrySize = 0;

        while (entries.hasMoreElements()) {
            ZipEntry entry = Objects.requireNonNull(entries.nextElement());
            entrySize = ArchiveUtil.getZipEntrySize(zipFile, entry);
            IPath path = new Path(entry.getName()).addTrailingSeparator();
            archiveSize += entrySize;
            ++archiveEntries;

            if (entry.isDirectory()) {
                createContainer(path);
            } else {
                // Ensure the container structure for all levels above this is
                // initialized. Once we hit a higher-level container that's
                // already added we need go no further
                int pathSegmentCount = path.segmentCount();
                if (pathSegmentCount > 1) {
                    createContainer(path.uptoSegment(pathSegmentCount - 1));
                }
                if (ArchiveUtil.verifyZipFileIsSafe(archiveSize, archiveEntries) && entrySize != -1) {
                    createFile(new ZipArchiveEntry(entry.getName()));
                } else {
                    Activator activator = new Activator();
                    activator.logError("Unable to create file, Zip file is unsafe");
                }
            }
        }
    }

    @Override
    public boolean isFolder(Object element) {
        return ((ZipArchiveEntry) element).isDirectory();
    }

    @Override
    public void setStrip(int level) {
        stripLevel = level;
    }

    @Override
    public int getStrip() {
        return stripLevel;
    }
}
