package net.shadew.gametest.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

public abstract class AlignableScreen extends Screen {
    protected static final int HORIZ_ALIGN_LEFT = 0;
    protected static final int HORIZ_ALIGN_CENTER = 1;
    protected static final int HORIZ_ALIGN_RIGHT = 2;
    protected static final int VERT_ALIGN_TOP = 0;
    protected static final int VERT_ALIGN_CENTER = 1;
    protected static final int VERT_ALIGN_BOTTOM = 2;

    protected int screenWidth = 200;
    protected int screenHeight = 200;
    protected int horizAlign = HORIZ_ALIGN_CENTER;
    protected int vertAlign = VERT_ALIGN_CENTER;
    protected int topMargin = 0;
    protected int bottomMargin = 0;
    protected int leftMargin = 0;
    protected int rightMargin = 0;

    protected AlignableScreen(ITextComponent title) {
        super(title);
        setSize(300, 200);

    }

    protected void realign() {
        resize(client, width, height);
    }

    protected void setSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    protected void setMargin(int t, int r, int b, int l) {
        topMargin = t;
        rightMargin = r;
        bottomMargin = b;
        leftMargin = l;
    }

    protected int left() {
        switch (horizAlign) {
            case HORIZ_ALIGN_LEFT:
                return leftMargin;
            case HORIZ_ALIGN_RIGHT:
                return width - (rightMargin + screenWidth);
            default:
            case HORIZ_ALIGN_CENTER:
                int w = width - rightMargin - leftMargin;
                return w / 2 - screenWidth / 2 + leftMargin;
        }
    }

    protected int right() {
        return left() + screenWidth;
    }

    protected int top() {
        switch (vertAlign) {
            case VERT_ALIGN_TOP:
                return topMargin;
            case VERT_ALIGN_BOTTOM:
                return height - (bottomMargin + screenHeight);
            default:
            case HORIZ_ALIGN_CENTER:
                int h = height - bottomMargin - topMargin;
                return h / 2 - screenHeight / 2 + topMargin;
        }
    }

    protected int bottom() {
        return top() + screenHeight;
    }

    protected int left(int l) {
        return left() + l;
    }

    protected int top(int l) {
        return top() + l;
    }

    protected int right(int l) {
        return right() - l;
    }

    protected int bottom(int l) {
        return bottom() - l;
    }

    protected int width(double percent) {
        return (int) (screenWidth * percent);
    }

    protected int height(double percent) {
        return (int) (screenHeight * percent);
    }

    protected int x(double percent) {
        return left((int) (screenWidth * percent));
    }

    protected int y(double percent) {
        return top((int) (screenHeight * percent));
    }

    protected int alignX(int width, double prc) {
        return x(prc) - (int) (width * prc);
    }

    protected int alignY(int height, double prc) {
        return y(prc) - (int) (height * prc);
    }

    public <T> T create(IBoxedConstructor<? extends T> constructor) {
        return constructor.create(left(), top(), screenHeight, screenHeight);
    }

    public <T> T create(int x, int y, int width, int height, IBoxedConstructor<? extends T> constructor) {
        return constructor.create(left(x), top(y), width, height);
    }

    public <T> T align(double x, double y, int width, int height, IBoxedConstructor<? extends T> constructor) {
        return constructor.create(alignX(width, x), alignY(height, y), width, height);
    }

    protected void fillRect(MatrixStack matrix, int x, int y, int w, int h, int col) {
        fillGradient(matrix, x, y, x + w, y + h, col, col);
    }

    protected void renderDebug(MatrixStack matrix, Box box) {
        fillRect(matrix, box.left(), box.top(), box.width, box.height, 0x30FFFFFF);

        fillRect(matrix, box.left(), box.y, 1, box.height, 0xFFFFFF00);
        fillRect(matrix, box.right() - 1, box.y, 1, box.height, 0xFFFFFF00);
        fillRect(matrix, box.x, box.top(), box.width, 1, 0xFFFFFF00);
        fillRect(matrix, box.x, box.bottom() - 1, box.width, 1, 0xFFFFFF00);
    }

    protected void renderDebug(MatrixStack matrix, Grid grid) {
        for (int i = 1, s = grid.cols.size(); i < s; i++) {
            fillRect(matrix, grid.x(i) - 1, grid.box.y, 2, grid.box.height, 0x5000FF00);
        }
        for (int i = 1, s = grid.rows.size(); i < s; i++) {
            fillRect(matrix, grid.box.x, grid.y(i) - 1, grid.box.width, 2, 0x5000FF00);
        }

        fillRect(matrix, grid.box.left(), grid.box.y, 1, grid.box.height, 0xFFFFFF00);
        fillRect(matrix, grid.box.right() - 1, grid.box.y, 1, grid.box.height, 0xFFFFFF00);
        fillRect(matrix, grid.box.x, grid.box.top(), grid.box.width, 1, 0xFFFFFF00);
        fillRect(matrix, grid.box.x, grid.box.bottom() - 1, grid.box.width, 1, 0xFFFFFF00);
    }

    protected void renderDebug(MatrixStack matrix) {
        fillRect(matrix, left(), top(), screenWidth, screenHeight, 0x30FFFFFF);

        fillRect(matrix, left(), 0, 1, height, 0xFF0000FF);
        fillRect(matrix, right() - 1, 0, 1, height, 0xFF0000FF);
        fillRect(matrix, 0, top(), width, 1, 0xFF0000FF);
        fillRect(matrix, 0, bottom() - 1, width, 1, 0xFF0000FF);

        fillRect(matrix, 0, 0, leftMargin, height, 0x30FF0000);
        fillRect(matrix, width - rightMargin, 0, rightMargin, height, 0x30FF0000);
        fillRect(matrix, 0, 0, width, topMargin, 0x30FF0000);
        fillRect(matrix, 0, height - bottomMargin, width, bottomMargin, 0x30FF0000);
    }

    protected void renderComponentsDebug(MatrixStack matrix) {
        for(Widget widget : buttons) {
            int top = widget.y;
            int left = widget.x;
            int width = widget.getWidth();
            int height = widget.unusedGetHeight();
            int bottom = top + height;
            int right = left + width;

            fillRect(matrix, left, top, 1, height, 0xFF00FFFF);
            fillRect(matrix, right - 1, top, 1, height, 0xFF00FFFF);
            fillRect(matrix, left, top, width, 1, 0xFF00FFFF);
            fillRect(matrix, left, bottom - 1, width, 1, 0xFF00FFFF);
        }
    }

    @FunctionalInterface
    public interface IBoxedConstructor<T> {
        T create(int x, int y, int w, int h);
    }

    public static <T> IBoxedConstructor<T> margin(int top, int right, int left, int bottom, IBoxedConstructor<? extends T> constructor) {
        return (x, y, w, h) -> constructor.create(x + left, y + top, w - left - right, h - top - bottom);
    }

    public static <T> IBoxedConstructor<T> margin(int margin, IBoxedConstructor<? extends T> constructor) {
        return margin(margin, margin, margin, margin, constructor);
    }

    public static class Grid {
        public final Box box;
        public final DoubleList cols = new DoubleArrayList();
        public final DoubleList rows = new DoubleArrayList();

        public Grid(Box box) {
            this.box = box;
        }

        public Grid(int x, int y, int w, int h) {
            this.box = new Box(x, y, w, h);
        }

        public Grid init(int c, int r) {
            cols.clear();
            rows.clear();

            for (int i = 0; i < r; i++) rows.add(1);
            for (int i = 0; i < c; i++) cols.add(1);

            return this;
        }

        public Grid rows(double... r) {
            rows.clear();
            rows.addElements(0, r);
            return this;
        }

        public Grid cols(double... c) {
            cols.clear();
            cols.addElements(0, c);
            return this;
        }

        public Grid wrapRows(int... r) {
            rows.clear();
            int h = 0;
            for(int i : r) {
                rows.add(i);
                h += i;
            }
            box.height = h;
            return this;
        }

        public Grid wrapCols(int... c) {
            cols.clear();
            int w = 0;
            for(int i : c) {
                cols.add(i);
                w += i;
            }
            box.width = w;
            return this;
        }

        public double colWgt() {
            double d = 0;
            for (double col : cols) {
                d += col;
            }
            return d;
        }

        public double rowWgt() {
            double d = 0;
            for (double row : rows) {
                d += row;
            }
            return d;
        }

        public int x(int col) {
            double wgt = colWgt();
            double leftWgt = 0;
            for (int i = 0; i < col; i++) {
                leftWgt += cols.getDouble(i);
            }
            return box.x(leftWgt / wgt);
        }

        public int y(int row) {
            double wgt = rowWgt();
            double topWgt = 0;
            for (int i = 0; i < row; i++) {
                topWgt += rows.getDouble(i);
            }
            return box.y(topWgt / wgt);
        }

        public int width(int col) {
            return x(col + 1) - x(col);
        }

        public int height(int row) {
            return y(row + 1) - y(row);
        }

        public int width(int col, int span) {
            return x(col + span) - x(col);
        }

        public int height(int row, int span) {
            return y(row + span) - y(row);
        }

        public <T> T create(int x, int y, IBoxedConstructor<? extends T> constructor) {
            return constructor.create(x(x), y(y), width(x), height(y));
        }

        public <T> T create(int x, int y, int xspan, int yspan, IBoxedConstructor<? extends T> constructor) {
            return constructor.create(x(x), y(y), width(x, xspan), height(y, yspan));
        }

        public <T> T create(int x, int y, int xspan, int yspan, int space, IBoxedConstructor<? extends T> constructor) {
            int t = 0, r = 0, b = 0, l = 0;
            if (x >= 0) l = space;
            if (x <= cols.size() - xspan) r = space;
            if (y >= 0) t = space;
            if (y <= rows.size() - yspan) b = space;
            return constructor.create(x(x) + l, y(y) + t, width(x, xspan) - l - r, height(y, yspan) - t - b);
        }
    }

    public static class Box {
        public int x;
        public int y;
        public int width;
        public int height;

        public Box(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        public int left() {
            return x;
        }

        public int right() {
            return x + width;
        }

        public int top() {
            return y;
        }

        public int bottom() {
            return y + height;
        }

        public int left(int l) {
            return left() + l;
        }

        public int right(int r) {
            return right() - r;
        }

        public int top(int t) {
            return top() + t;
        }

        public int bottom(int b) {
            return bottom() - b;
        }

        public int width(double percent) {
            return (int) (width * percent);
        }

        public int height(double percent) {
            return (int) (height * percent);
        }

        public int x(double percent) {
            return left((int) (width * percent));
        }

        public int y(double percent) {
            return top((int) (height * percent));
        }

        public int alignX(int width, double prc) {
            return x(prc) - (int) (width * prc);
        }

        public int alignY(int height, double prc) {
            return y(prc) - (int) (height * prc);
        }

        public <T> T create(IBoxedConstructor<? extends T> constructor) {
            return constructor.create(x, y, width, height);
        }

        public <T> T create(int x, int y, int width, int height, IBoxedConstructor<? extends T> constructor) {
            return constructor.create(left(x), top(y), width, height);
        }

        public <T> T align(double x, double y, int width, int height, IBoxedConstructor<? extends T> constructor) {
            return constructor.create(alignX(width, x), alignY(height, y), width, height);
        }
    }
}
