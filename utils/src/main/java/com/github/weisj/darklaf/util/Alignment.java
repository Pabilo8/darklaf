/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.weisj.darklaf.util;

import java.awt.*;
import java.util.function.BiFunction;

import static com.github.weisj.darklaf.util.AlignmentHelper.*;

/**
 * @author Jannis Weis
 */
public enum Alignment {
    NORTH(AlignmentHelper.align(HOR_CENTER_INSIDE, VERT_TOP_INSIDE),
          AlignmentHelper.align(HOR_CENTER_OUTSIDE, VERT_TOP_OUTSIDE)
    ),
    SOUTH(AlignmentHelper.align(HOR_CENTER_INSIDE, VERT_BOTTOM_INSIDE),
          AlignmentHelper.align(HOR_CENTER_OUTSIDE, VERT_BOTTOM_OUTSIDE)
    ),
    EAST(AlignmentHelper.align(HOR_RIGHT_INSIDE, VERT_CENTER_INSIDE),
         AlignmentHelper.align(HOR_RIGHT_OUTSIDE, VERT_CENTER_OUTSIDE)
    ),
    WEST(AlignmentHelper.align(HOR_LEFT_INSIDE, VERT_CENTER_INSIDE),
         AlignmentHelper.align(HOR_LEFT_OUTSIDE, VERT_CENTER_OUTSIDE)
    ),
    NORTH_EAST(AlignmentHelper.align(HOR_RIGHT_INSIDE, VERT_TOP_INSIDE),
               AlignmentHelper.align(HOR_RIGHT_OUTSIDE, VERT_TOP_OUTSIDE)
    ),
    NORTH_WEST(AlignmentHelper.align(HOR_LEFT_INSIDE, VERT_TOP_INSIDE),
               AlignmentHelper.align(HOR_LEFT_OUTSIDE, VERT_TOP_OUTSIDE)
    ),
    SOUTH_EAST(AlignmentHelper.align(HOR_RIGHT_INSIDE, VERT_BOTTOM_INSIDE),
               AlignmentHelper.align(HOR_RIGHT_OUTSIDE, VERT_BOTTOM_OUTSIDE)
    ),
    SOUTH_WEST(AlignmentHelper.align(HOR_LEFT_INSIDE, VERT_BOTTOM_INSIDE),
               AlignmentHelper.align(HOR_LEFT_OUTSIDE, VERT_BOTTOM_OUTSIDE)
    ),
    CENTER(AlignmentHelper.align(HOR_CENTER_INSIDE, VERT_CENTER_INSIDE),
           AlignmentHelper.align(HOR_CENTER_OUTSIDE, VERT_CENTER_OUTSIDE)
    );


    private final BiFunction<Dimension, Rectangle, Point> alignInside;
    private final BiFunction<Dimension, Rectangle, Point> alignOutside;


    Alignment(final BiFunction<Dimension, Rectangle, Point> alignInside,
              final BiFunction<Dimension, Rectangle, Point> alignOutside) {
        this.alignInside = alignInside;
        this.alignOutside = alignOutside;
    }

    /**
     * Get fitting alignment.
     *
     * @param point       point to align at.
     * @param size        Size of rectangle to align.
     * @param outerBounds outer boundaries to align in.
     * @param hint        preferred alignment.
     * @return fitting alignment. If none is found the default is {@link Alignment#CENTER}.
     */

    public static Alignment getAlignment(final Point point,
                                         final Dimension size,
                                         final Rectangle outerBounds,
                                         final Alignment hint) {
        if (hint.canBeAligned(point, size, outerBounds)) {
            return hint;
        }
        for (Alignment alignment : Alignment.values()) {
            if (alignment != CENTER && alignment != hint
                    && alignment.canBeAligned(point, size, outerBounds)) {
                return alignment;
            }
        }
        return CENTER;
    }

    /**
     * Check whether the given Rectangle can be aligned at point inside boundaries.
     *
     * @param point       point to align at.
     * @param size        size of rectangle to align.
     * @param outerBounds boundaries.
     * @return true if can be aligned.
     */
    public boolean canBeAligned(final Point point,
                                final Dimension size,
                                final Rectangle outerBounds) {
        Point p = relativePos(size, point);
        return p.x >= outerBounds.x && p.y >= outerBounds.y
                && p.x + size.width < outerBounds.x + outerBounds.width
                && p.y + size.height < outerBounds.x + outerBounds.height;
    }

    /**
     * Get the relative Position of Rectangle to Point with respect to the alignment.
     *
     * @param toAlign size of Rectangle to align.
     * @param alignAt point to align at.
     * @return top/left position of aligned rectangle
     */
    public Point relativePos(final Dimension toAlign, final Point alignAt) {
        return alignOutside(toAlign, new Rectangle(alignAt.x, alignAt.y, 0, 0));
    }

    /**
     * Align Rectangle outside other rectangle with respect to the alignment.
     *
     * @param toAlign     size of rectangle to align
     * @param innerBounds bounds of inside rectangle
     * @return top/left point of aligned rectangle
     */
    public Point alignOutside(final Dimension toAlign,
                              final Rectangle innerBounds) {
        return this.alignOutside.apply(toAlign, innerBounds);
    }

    /**
     * Get the index of the alignment. This function is for utility purposes where one might save settings based on
     * alignment in an array.
     *
     * @return the index.
     */

    public int getIndex() {
        return this.ordinal();
    }

    /**
     * Get the opposite alignment.
     *
     * @return Alignment opposite on the compass.
     */


    @SuppressWarnings("Duplicates")
    public Alignment opposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case NORTH_EAST:
                return SOUTH_WEST;
            case EAST:
                return WEST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH:
                return NORTH;
            case SOUTH_WEST:
                return NORTH_EAST;
            case WEST:
                return EAST;
            case NORTH_WEST:
                return SOUTH_EAST;
            case CENTER:
                return CENTER;
            default:
                throw new IllegalArgumentException();
        }
    }


    @SuppressWarnings("Duplicates")
    public Alignment anticlockwise() {
        switch (this) {
            case NORTH:
                return NORTH_WEST;
            case NORTH_EAST:
                return NORTH;
            case EAST:
                return NORTH_EAST;
            case SOUTH_EAST:
                return EAST;
            case SOUTH:
                return SOUTH_EAST;
            case SOUTH_WEST:
                return SOUTH;
            case WEST:
                return SOUTH_WEST;
            case NORTH_WEST:
                return WEST;
            case CENTER:
                return CENTER;
            default:
                throw new IllegalArgumentException();
        }
    }


    @SuppressWarnings("Duplicates")
    public Alignment clockwise() {
        switch (this) {
            case NORTH:
                return NORTH_EAST;
            case NORTH_EAST:
                return EAST;
            case EAST:
                return SOUTH_EAST;
            case SOUTH_EAST:
                return SOUTH;
            case SOUTH:
                return SOUTH_WEST;
            case SOUTH_WEST:
                return WEST;
            case WEST:
                return NORTH_WEST;
            case NORTH_WEST:
                return NORTH;
            case CENTER:
                return CENTER;
            default:
                throw new IllegalArgumentException();
        }
    }


    public Insets maskInsets(final Insets insets) {
        switch (this) {
            case NORTH:
                return new Insets(insets.top, 0, 0, 0);
            case NORTH_EAST:
                return new Insets(insets.top, 0, 0, insets.right);
            case EAST:
                return new Insets(0, 0, 0, insets.right);
            case SOUTH_EAST:
                return new Insets(0, 0, insets.bottom, insets.right);
            case SOUTH:
                return new Insets(0, 0, insets.bottom, 0);
            case SOUTH_WEST:
                return new Insets(0, insets.left, insets.bottom, 0);
            case WEST:
                return new Insets(0, insets.left, 0, 0);
            case NORTH_WEST:
                return new Insets(insets.top, insets.left, 0, 0);
            case CENTER:
                return new Insets(0, 0, 0, 0);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Align Rectangle inside other rectangle with respect to the alignment.
     *
     * @param toAlign     size of rectangle to align
     * @param outerBounds bounds of outer rectangle
     * @return top/left point of aligned rectangle
     */
    public Point alignInside(final Dimension toAlign,
                             final Rectangle outerBounds) {
        return this.alignInside.apply(toAlign, outerBounds);
    }
}
