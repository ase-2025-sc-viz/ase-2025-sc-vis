package org.eclipse.tracecompass.tmf.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
//import org.eclipse.tracecompass.analysis.profiling.core.base.FlameDefaultPalette2;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;


public class CustomColorPaletteProvider implements IPaletteProvider{

    /**
     * Gets an instance of {@link RGBAColor} that represents blue color
     */
    public static final RGBAColor BLUE = new RGBAColor(0, 0, 255);

    /**
     * Gets an instance of {@link RGBAColor} that represents red color
     */
    public static final RGBAColor RED = new RGBAColor(255, 0, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents gree color
     */
    public static final RGBAColor GREEN = new RGBAColor(0, 255, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents magenta color
     */
    public static final RGBAColor MAGENTA = new RGBAColor(255, 0, 255);

    /**
     * Gets an instance of {@link RGBAColor} that represents cyan color
     */
    public static final RGBAColor CYAN = new RGBAColor(0, 255, 255);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark blue color
     */
    public static final RGBAColor DARK_BLUE = new RGBAColor(0, 0, 128);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark red color
     */
    public static final RGBAColor DARK_RED = new RGBAColor(128, 0, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark green color
     */
    public static final RGBAColor DARK_GREEN = new RGBAColor(0, 128, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark magenta color
     */
    public static final RGBAColor DARK_MAGENTA = new RGBAColor(128, 0, 128);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark cyan color
     */
    public static final RGBAColor DARK_CYAN = new RGBAColor(0, 128, 125);

    /**
     * Gets an instance of {@link RGBAColor} that represents dark yellow color
     */
    public static final RGBAColor DARK_YELLOW = new RGBAColor(128, 128, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents black color
     */
    public static final RGBAColor BLACK = new RGBAColor(0, 0, 0);

    /**
     * Gets an instance of {@link RGBAColor} that represents gray color
     */
    public static final RGBAColor GRAY = new RGBAColor(192, 192, 192);

    /**
     * Gets an instance of {@link RGBAColor} that represents yellow color
     */
    public static final RGBAColor YELLOW = new RGBAColor(255, 255, 0);



    public static final RGBAColor PASTEL_CYAN = new RGBAColor(102, 197, 204);
    public static final RGBAColor PASTEL_ORANGE = new RGBAColor(246, 207, 113);
    public static final RGBAColor PASTEL_RED = new RGBAColor(248, 156, 116);
    public static final RGBAColor PASTEL_LIGHT_PURPLE = new RGBAColor(220,176,242);
    public static final RGBAColor PASTEL_GREEN = new RGBAColor(135,197,95);
    public static final RGBAColor PASTEL_BLUE = new RGBAColor(158, 185, 243);
    public static final RGBAColor PASTEL_PINK = new RGBAColor(254, 136, 177);
    public static final RGBAColor PASTEL_YELLOW = new RGBAColor(201, 219, 116);
    public static final RGBAColor PASTEL_LIGHT_GREEN = new RGBAColor(139, 224, 164);
    public static final RGBAColor PASTEL_PURPLE = new RGBAColor(180, 151, 231);
    public static final RGBAColor PASTEL_BROWN = new RGBAColor(211, 180, 132);
    public static final RGBAColor PASTEL_GRAY = new RGBAColor(179, 179, 179);






    /*private static final List<@NonNull RGBAColor> PALETTE = Arrays.asList(
            BLUE, RED, GREEN, MAGENTA, CYAN, DARK_BLUE, DARK_RED, DARK_GREEN,
            DARK_MAGENTA, DARK_CYAN, DARK_YELLOW, BLACK, GRAY, YELLOW);*/

    private static final List<@NonNull RGBAColor> PALETTE = Arrays.asList(
            PASTEL_CYAN, PASTEL_ORANGE, PASTEL_RED, PASTEL_LIGHT_PURPLE,
            PASTEL_GREEN, PASTEL_BLUE, PASTEL_PINK, PASTEL_YELLOW,
            PASTEL_LIGHT_GREEN, PASTEL_PURPLE, PASTEL_BROWN, PASTEL_GRAY);

    /**
     * Get the default default color palette provider
     */
    public static final CustomColorPaletteProvider INSTANCE = new CustomColorPaletteProvider();

    private final Map<Long, RGBAColor> assignedColors = new HashMap<>();
    private int currentIndex = 0;

    private CustomColorPaletteProvider() {
        // do nothing
    }

    @Override
    public List<@NonNull RGBAColor> get() {
        return PALETTE;
    }



    /**
     * @param name
     * @return
     */
    public RGBAColor getColor(Long name) {
        return assignedColors.computeIfAbsent(name, key -> {
            RGBAColor color = PALETTE.get(currentIndex % PALETTE.size());
            currentIndex++;
            return color;
        });
    }

    /**
     * @param name
     * @param state
     * @return
     */
    public OutputElementStyle getStyleFor(String name, ISegment state) {
        HashMap<String, Object> style = new HashMap<>();
        RGBAColor color = getColor((long)name.hashCode());
        String hexColor = toHexColor(color);
        state.getClass();
        style.put(StyleProperties.BACKGROUND_COLOR, hexColor);






        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace !=null) {

            ITmfContext ctx = trace.seekEvent(0);
            ITmfEvent event;

            while ((event = trace.getNext(ctx)) != null) {
                ITmfEventField content = event.getContent();
                if (name.equals(content.getField("name").getFormattedValue()) && String.valueOf(state.getStart()/1000).equals(content.getField("ts").getFormattedValue())) {

                    ITmfEventField argsField = content.getField("args/success");
                    if (argsField != null) {
                        if ("false".equals(argsField.getFormattedValue())) {
                            style.put(StyleProperties.BORDER_COLOR, "#ff0000");
                            style.put(StyleProperties.BORDER_WIDTH, 2);
                            style.put(StyleProperties.BORDER_STYLE, "");
                        }

                    }
                }
            }
        }

        //return new OutputElementStyle(FlameDefaultPalette2.getInstance().getStyleFor(state).getParentKey(), s);
        return new OutputElementStyle("1", style);
    }

    private static String toHexColor(RGBAColor color) {
        final String HEX_COLOR_FORMAT = "#%02x%02x%02x"; //$NON-NLS-1$

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return String.format(HEX_COLOR_FORMAT, r, g, b);
    }

}