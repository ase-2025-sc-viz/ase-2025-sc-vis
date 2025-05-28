/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.tracecompass.tmf.ui.project.wizards.NewTmfProjectWizard;
//import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramView;
//import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The tracing perspective definition.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TracingPerspectiveFactory implements IPerspectiveFactory {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** The Perspective ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.perspective"; //$NON-NLS-1$

    // Standard TMF views
//    private static final String HISTOGRAM_VIEW_ID = HistogramView.ID;

    // Standard Eclipse views
    private static final String PROJECT_VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;
//    private static final String STATISTICS_VIEW_ID = TmfStatisticsView.ID;
//    private static final String PROPERTIES_VIEW_ID = IPageLayout.ID_PROP_SHEET;
//    private static final String BOOKMARKS_VIEW_ID = IPageLayout.ID_BOOKMARKS;


    // ------------------------------------------------------------------------
    // IPerspectiveFactory
    // ------------------------------------------------------------------------

    @Override
    public void createInitialLayout(IPageLayout layout) {

        // Editor area
        layout.setEditorAreaVisible(true);

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


        // Populate menus, etc
        layout.addPerspectiveShortcut(ID);
        layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
    }

}
