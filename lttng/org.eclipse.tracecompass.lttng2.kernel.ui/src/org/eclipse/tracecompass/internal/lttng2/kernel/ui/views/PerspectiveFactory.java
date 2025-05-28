/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views;

import org.eclipse.tracecompass.tmf.ui.project.wizards.NewTmfProjectWizard;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The default LTTng perspective.
 */
public class PerspectiveFactory implements IPerspectiveFactory {

    /** Perspective ID */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective"; //$NON-NLS-1$


    // Standard Eclipse views
    private static final String PROJECT_VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;

    @Override
    public void createInitialLayout(IPageLayout layout) {

        layout.setEditorAreaVisible(true);

        addFastViews(layout);
        addViewShortcuts(layout);
        addPerspectiveShortcuts(layout);

        IFolderLayout topLeftFolder = layout.createFolder(
                "topLeftFolder", IPageLayout.LEFT, 0.10f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topLeftFolder.addView(PROJECT_VIEW_ID);


        IFolderLayout right = layout.createFolder(
                "right", IPageLayout.RIGHT, 0.75f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        right.addView("org.eclipse.linuxtools.tmf.ui.views.statistics"); //$NON-NLS-1$

        IFolderLayout bright = layout.createFolder(
                "bright", IPageLayout.BOTTOM, 0.60f, "right"); //$NON-NLS-1$ //$NON-NLS-2$
        bright.addView("org.example.statediagram.views.statediagramview"); //$NON-NLS-1$


        IFolderLayout topRightFolder = layout.createFolder(
                "topRightFolder", IPageLayout.TOP, 0.95f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topRightFolder.addView("org.eclipse.tracecompass.analysis.profiling.ui.flamechart:org.eclipse.tracecompass.incubator.traceevent.analysis.callstack"); //$NON-NLS-1$


        IFolderLayout bottomRightFolder = layout.createFolder(
                "bottomRightFolder", IPageLayout.BOTTOM,0.60f , "topRightFolder"); //$NON-NLS-1$ //$NON-NLS-2$

        bottomRightFolder.addPlaceholder("org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.views.xyview:Ethereum Fee Per Function"); //$NON-NLS-1$



        layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
    }

    /**
     * Add fast views to the perspective
     *
     * @param layout
     */
    private void addFastViews(IPageLayout layout) {
        // Do nothing
    }

    /**
     * Add view shortcuts to the perspective
     *
     * @param layout
     */
    private void addViewShortcuts(IPageLayout layout) {
        // Do nothing
    }

    /**
     * Add perspective shortcuts to the perspective
     *
     * @param layout
     */
    private void addPerspectiveShortcuts(IPageLayout layout) {
        // Do nothing
    }

}
