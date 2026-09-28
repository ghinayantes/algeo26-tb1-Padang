package algeo.gui;

import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class PlotView extends Pane {

    private static final double PAD_LEFT = 58, PAD_RIGHT = 20, PAD_TOP = 34, PAD_BOTTOM = 40;
    private static final int SAMPLES = 400;

    private final Canvas canvas = new Canvas();
    private final String fnName;

    private DoubleUnaryOperator f;
    private double[] dataX = new double[0], dataY = new double[0];
    private double[] knots = new double[0];
    private double domMin, domMax;

    private static final class EvalPoint {
        final double x, y;
        final long startNanos;
        EvalPoint(double x, double y, long startNanos) { this.x = x; this.y = y; this.startNanos = startNanos; }
    }
    private final List<EvalPoint> evalPoints = new ArrayList<>();

    // Rentang data yang ditampilkan
    private double targetXMin, targetXMax, targetYMin, targetYMax;
    private double dispXMin, dispXMax, dispYMin, dispYMax;
    private double srcXMin, srcXMax, srcYMin, srcYMax;
    private boolean hasView = false;

    private double hoverX = Double.NaN;

    // timer animasi kurva
    private static final long ZOOM_DURATION_NS = 620_000_000L;   // kamera meluncur ke rentang baru
    private static final long REVEAL_DURATION_NS = 1100_000_000L; // kurva digambar kiri ke kanan
    private static final long EVAL_POP_DURATION_NS = 420_000_000L; // titik predikis input x muncul

    private boolean zoomActive = false;
    private long zoomStartNanos;

    private boolean revealActive = false;
    private long revealStartNanos;
    private double revealX = Double.POSITIVE_INFINITY; 

    private long frameNow = System.nanoTime();
    private AnimationTimer timer;

    public PlotView(String fnName) {
        this.fnName = fnName;
        getChildren().add(canvas);
        setMinHeight(300);
        setPrefHeight(440);
        setOnMouseMoved(e -> { hoverX = e.getX(); redraw(); });
        setOnMouseExited(e -> { hoverX = Double.NaN; redraw(); });
    }

    public void setModel(DoubleUnaryOperator f, double[] xs, double[] ys, double domMin, double domMax, double[] knots) {
        this.f = f;
        this.dataX = xs.clone();
        this.dataY = ys.clone();
        this.domMin = domMin;
        this.domMax = domMax;
        this.knots = knots == null ? new double[0] : knots.clone();
        this.evalPoints.clear();
        computeTargetView();
        beginZoomTransition();
        beginReveal();
        ensureTimerRunning();
    }

    public void clear() {
        this.f = null;
        this.evalPoints.clear();
        zoomActive = false;
        revealActive = false;
        revealX = Double.POSITIVE_INFINITY;
        redraw();
    }

    public void addEvalPoint(double x, double y) {
        evalPoints.add(new EvalPoint(x, y, System.nanoTime()));
        computeTargetView();
        beginZoomTransition();
        ensureTimerRunning();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        redraw();
    }

    // Animasi
    private void ensureTimerRunning() {
        if (timer == null) {
            timer = new AnimationTimer() {
                @Override public void handle(long now) { tick(now); }
            };
        }
        timer.start();
    }

    private void tick(long now) {
        frameNow = now;
        boolean stillAnimating = false;

        if (zoomActive) {
            double t = Math.min(1.0, (now - zoomStartNanos) / (double) ZOOM_DURATION_NS);
            double e = easeInOutCubic(t);
            dispXMin = lerp(srcXMin, targetXMin, e);
            dispXMax = lerp(srcXMax, targetXMax, e);
            dispYMin = lerp(srcYMin, targetYMin, e);
            dispYMax = lerp(srcYMax, targetYMax, e);
            if (t >= 1.0) {
                zoomActive = false;
                dispXMin = targetXMin; dispXMax = targetXMax;
                dispYMin = targetYMin; dispYMax = targetYMax;
            } else {
                stillAnimating = true;
            }
        }

        if (revealActive) {
            double t = Math.min(1.0, (now - revealStartNanos) / (double) REVEAL_DURATION_NS);
            double e = easeOutCubic(t);
            revealX = targetXMin + e * (targetXMax - targetXMin);
            if (t >= 1.0) {
                revealActive = false;
                revealX = Double.POSITIVE_INFINITY;
            } else {
                stillAnimating = true;
            }
        }

        if (!stillAnimating) {
            for (EvalPoint p : evalPoints) {
                if ((now - p.startNanos) / (double) EVAL_POP_DURATION_NS < 1.0) { stillAnimating = true; break; }
            }
        }

        redraw();
        if (!stillAnimating) timer.stop();
    }

    // transisi zoom kurva
    private void beginZoomTransition() {
        if (hasView) {
            srcXMin = dispXMin; srcXMax = dispXMax;
            srcYMin = dispYMin; srcYMax = dispYMax;
        } else {
            // Efek masuk
            double cx = (targetXMin + targetXMax) / 2, cy = (targetYMin + targetYMax) / 2;
            double halfX = (targetXMax - targetXMin) / 2 * 1.6;
            double halfY = (targetYMax - targetYMin) / 2 * 1.6;
            srcXMin = cx - halfX; srcXMax = cx + halfX;
            srcYMin = cy - halfY; srcYMax = cy + halfY;
            dispXMin = srcXMin; dispXMax = srcXMax;
            dispYMin = srcYMin; dispYMax = srcYMax;
            hasView = true;
        }
        zoomStartNanos = System.nanoTime();
        zoomActive = true;
    }

    private void beginReveal() {
        revealStartNanos = System.nanoTime();
        revealActive = true;
        revealX = targetXMin;
    }

    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }

    private static double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    private static double easeOutCubic(double t) {
        return 1 - Math.pow(1 - t, 3);
    }

    /** Ease-out-back: sedikit overshoot lalu menetap, cocok untuk efek "pop" titik evaluasi. */
    private static double easeOutBack(double t) {
        double c1 = 1.70158, c3 = c1 + 1;
        double x = t - 1;
        return 1 + c3 * x * x * x + c1 * x * x;
    }

    // range kurva
    private void computeTargetView() {
        double xmin = domMin, xmax = domMax;
        for (EvalPoint p : evalPoints) {
            xmin = Math.min(xmin, p.x);
            xmax = Math.max(xmax, p.x);
        }
        double span = xmax - xmin;
        if (span <= 0) span = Math.max(1, Math.abs(xmin));
        targetXMin = xmin - span * 0.06;
        targetXMax = xmax + span * 0.06;

        double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
        for (double v : dataY) { ymin = Math.min(ymin, v); ymax = Math.max(ymax, v); }
        for (int i = 0; i <= SAMPLES; i++) {
            double x = domMin + (domMax - domMin) * i / SAMPLES;
            double v = f.applyAsDouble(x);
            if (Double.isFinite(v)) { ymin = Math.min(ymin, v); ymax = Math.max(ymax, v); }
        }
        for (EvalPoint p : evalPoints) {
            if (Double.isFinite(p.y)) { ymin = Math.min(ymin, p.y); ymax = Math.max(ymax, p.y); }
        }
        double ySpan = ymax - ymin;
        if (ySpan <= 0) ySpan = Math.max(1, Math.abs(ymin));
        targetYMin = ymin - ySpan * 0.1;
        targetYMax = ymax + ySpan * 0.1;
    }

    private double sx(double x) {
        return PAD_LEFT + (x - dispXMin) / (dispXMax - dispXMin) * plotW();
    }

    private double sy(double y) {
        return PAD_TOP + (dispYMax - y) / (dispYMax - dispYMin) * plotH();
    }

    private double invX(double px) {
        return dispXMin + (px - PAD_LEFT) / plotW() * (dispXMax - dispXMin);
    }

    private double plotW() { return Math.max(1, canvas.getWidth() - PAD_LEFT - PAD_RIGHT); }
    private double plotH() { return Math.max(1, canvas.getHeight() - PAD_TOP - PAD_BOTTOM); }

    // gambar kurva
    private void redraw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFill(Theme.SURFACE);
        g.fillRect(0, 0, w, h);
        if (w < 50 || h < 50) return;

        if (f == null) {
            drawEmpty(g, w, h);
            return;
        }

        double left = PAD_LEFT, top = PAD_TOP, pw = plotW(), ph = plotH();

        // Pita domain
        double d0 = Math.max(left, sx(domMin)), d1 = Math.min(left + pw, sx(domMax));
        g.setFill(Theme.ACCENT_SOFT.deriveColor(0, 1, 1, 0.55));
        g.fillRect(d0, top, d1 - d0, ph);

        drawGrid(g, left, top, pw, ph);

        g.save();
        g.beginPath();
        g.rect(left, top, pw, ph);
        g.clip();

        // Garis knot
        g.setStroke(Theme.MUTED.deriveColor(0, 1, 1, 0.7));
        g.setLineWidth(1);
        g.setLineDashes(2, 4);
        for (double k : knots) {
            double px = sx(k);
            g.strokeLine(px, top, px, top + ph);
        }
        g.setLineDashes((double[]) null);

        drawCurve(g, left, pw);

        // Titik data di kurva
        for (int i = 0; i < dataX.length; i++) {
            if (dataX[i] > revealX) continue;
            double px = sx(dataX[i]), py = sy(dataY[i]);
            g.setFill(Theme.SURFACE);
            g.fillOval(px - 4.5, py - 4.5, 9, 9);
            g.setStroke(Theme.INK);
            g.setLineWidth(1.8);
            g.strokeOval(px - 4.5, py - 4.5, 9, 9);
        }

        // Titik prediksi di kurva
        for (EvalPoint p : evalPoints) {
            double prog = Math.min(1.0, (frameNow - p.startNanos) / (double) EVAL_POP_DURATION_NS);
            double scale = Math.max(0, easeOutBack(prog));
            double alpha = Math.min(1.0, easeOutCubic(prog) + 0.001);
            double px = sx(p.x), py = sy(p.y);

            g.save();
            g.setGlobalAlpha(alpha);
            g.setStroke(Theme.EVAL.deriveColor(0, 1, 1, 0.5));
            g.setLineWidth(1);
            g.setLineDashes(3, 3);
            g.strokeLine(px, py, px, top + ph);
            g.setLineDashes((double[]) null);
            double r = 5.5 * scale;
            g.setFill(Theme.EVAL);
            g.fillOval(px - r, py - r, 2 * r, 2 * r);
            g.setStroke(Theme.SURFACE);
            g.setLineWidth(2);
            g.strokeOval(px - r, py - r, 2 * r, 2 * r);
            g.restore();
        }

        drawHover(g, left, top, pw, ph);
        g.restore();

        // Label knot di bawah sumbu
        g.setFill(Theme.MUTED);
        g.setFont(Theme.sans(11.5));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.TOP);
        for (int i = 0; i < knots.length; i++) {
            double px = sx(knots[i]);
            if (px >= left && px <= left + pw) g.fillText("\u03BE" + (i + 1), px, top + ph + 22);
        }

        drawLegend(g, left, w);
    }

    private void drawEmpty(GraphicsContext g, double w, double h) {
        g.setStroke(Theme.LINE.deriveColor(0, 1, 1, 0.5));
        g.setLineWidth(1);
        for (double x = 0; x < w; x += 24) g.strokeLine(x + 0.5, 0, x + 0.5, h);
        for (double y = 0; y < h; y += 24) g.strokeLine(0, y + 0.5, w, y + 0.5);
        String msg = "Masukkan titik data lalu buat kurva untuk melihat grafiknya";
        javafx.scene.text.Text probe = new javafx.scene.text.Text(msg);
        probe.setFont(Theme.sans(14));
        double tw = probe.getLayoutBounds().getWidth() + 40;
        g.setFill(Theme.SURFACE);
        g.fillRoundRect(w / 2 - tw / 2, h / 2 - 22, tw, 44, 10, 10);
        g.setStroke(Theme.LINE);
        g.strokeRoundRect(w / 2 - tw / 2 + 0.5, h / 2 - 22 + 0.5, tw, 44, 10, 10);
        g.setFill(Theme.MUTED);
        g.setFont(Theme.sans(14));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.fillText(msg, w / 2, h / 2);
    }

    private void drawGrid(GraphicsContext g, double left, double top, double pw, double ph) {
        double[] xt = niceTicks(dispXMin, dispXMax, Math.max(3, (int) (pw / 90)));
        double[] yt = niceTicks(dispYMin, dispYMax, Math.max(3, (int) (ph / 60)));

        // garis minor kertas milimeter
        g.setStroke(Theme.LINE.deriveColor(0, 1, 1, 0.45));
        g.setLineWidth(1);
        if (xt.length > 1) {
            double step = (xt[1] - xt[0]) / 5;
            for (double x = xt[0] - 5 * step; x <= dispXMax; x += step) {
                double px = Math.round(sx(x)) + 0.5;
                if (px > left && px < left + pw) g.strokeLine(px, top, px, top + ph);
            }
        }
        if (yt.length > 1) {
            double step = (yt[1] - yt[0]) / 5;
            for (double y = yt[0] - 5 * step; y <= dispYMax; y += step) {
                double py = Math.round(sy(y)) + 0.5;
                if (py > top && py < top + ph) g.strokeLine(left, py, left + pw, py);
            }
        }

        // garis mayor + label
        g.setFont(Theme.mono(11.5));
        g.setFill(Theme.MUTED);
        g.setStroke(Theme.LINE);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.TOP);
        for (double x : xt) {
            double px = Math.round(sx(x)) + 0.5;
            if (px < left || px > left + pw) continue;
            g.strokeLine(px, top, px, top + ph);
            g.fillText(tickLabel(x, xt), px, top + ph + 6);
        }
        g.setTextAlign(TextAlignment.RIGHT);
        g.setTextBaseline(VPos.CENTER);
        for (double y : yt) {
            double py = Math.round(sy(y)) + 0.5;
            if (py < top || py > top + ph) continue;
            g.strokeLine(left, py, left + pw, py);
            g.fillText(tickLabel(y, yt), left - 8, py);
        }

        g.setStroke(Theme.MUTED.deriveColor(0, 1, 1, 0.55));
        g.setLineWidth(1.2);
        if (dispXMin < 0 && dispXMax > 0) g.strokeLine(sx(0), top, sx(0), top + ph);
        if (dispYMin < 0 && dispYMax > 0) g.strokeLine(left, sy(0), left + pw, sy(0));

        g.setStroke(Theme.LINE);
        g.setLineWidth(1);
        g.strokeRect(left + 0.5, top + 0.5, pw - 1, ph - 1);
    }

    private void drawCurve(GraphicsContext g, double left, double pw) {
        g.setLineCap(StrokeLineCap.ROUND);
        int n = (int) Math.max(SAMPLES, pw);
        // tiga potongan: kiri domain, domain, kanan domain
        strokeSegment(g, dispXMin, Math.min(domMin, dispXMax), n, true);
        strokeSegment(g, domMin, domMax, n, false);
        strokeSegment(g, Math.max(domMax, dispXMin), dispXMax, n, true);
    }

    private void strokeSegment(GraphicsContext g, double a, double b, int n, boolean extrapolated) {
        if (b - a <= 1e-12) return;
        if (a >= revealX) return; // segmen ini belum "tergambar" oleh animasi reveal
        g.setStroke(extrapolated ? Theme.ACCENT.deriveColor(0, 0.6, 1.1, 0.7) : Theme.ACCENT);
        g.setLineWidth(extrapolated ? 2 : 2.6);
        if (extrapolated) g.setLineDashes(7, 6); else g.setLineDashes((double[]) null);

        double limit = (dispYMax - dispYMin) * 50; // cegah koordinat raksasa
        g.beginPath();
        boolean started = false;
        int steps = Math.max(2, (int) (n * (b - a) / (dispXMax - dispXMin)));
        for (int i = 0; i <= steps; i++) {
            double x = a + (b - a) * i / steps;
            if (x > revealX) break; // stop tepat di ujung animasi, sehingga terlihat "menggambar" progresif
            double y = f.applyAsDouble(x);
            if (!Double.isFinite(y) || Math.abs(y - dispYMin) > limit) { started = false; continue; }
            if (!started) { g.moveTo(sx(x), sy(y)); started = true; } else { g.lineTo(sx(x), sy(y)); }
        }
        g.stroke();
        g.setLineDashes((double[]) null);
    }

    private void drawHover(GraphicsContext g, double left, double top, double pw, double ph) {
        if (Double.isNaN(hoverX) || hoverX < left || hoverX > left + pw) return;
        double x = invX(hoverX);
        double y = f.applyAsDouble(x);
        if (!Double.isFinite(y)) return;
        double py = sy(y);

        g.setStroke(Theme.INK.deriveColor(0, 1, 1, 0.25));
        g.setLineWidth(1);
        g.strokeLine(hoverX, top, hoverX, top + ph);

        if (py >= top && py <= top + ph) {
            g.setFill(Theme.ACCENT);
            g.fillOval(hoverX - 4, py - 4, 8, 8);
        }

        boolean outside = x < domMin || x > domMax;
        String l1 = "x = " + fmt(x);
        String l2 = fnName + "(x) = " + fmt(y);
        String l3 = outside ? "ekstrapolasi" : null;

        double bw = 160, bh = outside ? 62 : 46;
        double bx = hoverX + 12, by = Math.max(top + 6, Math.min(py - bh / 2, top + ph - bh - 6));
        if (bx + bw > left + pw) bx = hoverX - 12 - bw;

        g.setFill(Theme.INK.deriveColor(0, 1, 1, 0.92));
        g.fillRoundRect(bx, by, bw, bh, 10, 10);
        g.setFill(Color.WHITE);
        g.setFont(Theme.mono(12.5));
        g.setTextAlign(TextAlignment.LEFT);
        g.setTextBaseline(VPos.TOP);
        g.fillText(l1, bx + 10, by + 8);
        g.fillText(l2, bx + 10, by + 24);
        if (l3 != null) {
            g.setFill(Theme.EVAL.brighter());
            g.setFont(javafx.scene.text.Font.font(Theme.SANS_SEMIBOLD, 12));
            g.fillText(l3, bx + 10, by + 40);
        }
    }

    private void drawLegend(GraphicsContext g, double left, double w) {
        double y = 16;
        double x = left - 22;
        g.setFont(Theme.sans(12.5));
        g.setTextBaseline(VPos.CENTER);
        g.setTextAlign(TextAlignment.LEFT);

        g.setStroke(Theme.INK);
        g.setLineWidth(1.6);
        g.strokeOval(x, y - 4, 8, 8);
        g.setFill(Theme.MUTED);
        g.fillText("titik data", x + 14, y);
        x += 84;

        g.setStroke(Theme.ACCENT);
        g.setLineWidth(2.6);
        g.strokeLine(x, y, x + 18, y);
        g.fillText(fnName + "(x)", x + 24, y);
        x += 74;

        g.setFill(Theme.ACCENT_SOFT);
        g.fillRect(x, y - 6, 16, 12);
        g.setFill(Theme.MUTED);
        g.fillText("domain [" + fmt(domMin) + ", " + fmt(domMax) + "]", x + 22, y);
        x += 190;

        if (!evalPoints.isEmpty()) {
            g.setFill(Theme.EVAL);
            g.fillOval(x, y - 5, 10, 10);
            g.setFill(Theme.MUTED);
            g.fillText("prediksi", x + 16, y);
            x += 84;
        }
        if (dispXMin < domMin - 1e-12 || dispXMax > domMax + 1e-12) {
            g.setStroke(Theme.ACCENT.deriveColor(0, 0.6, 1.1, 0.7));
            g.setLineWidth(2);
            g.setLineDashes(5, 4);
            g.strokeLine(x, y, x + 18, y);
            g.setLineDashes((double[]) null);
            g.fillText("luar domain", x + 24, y);
        }
    }

    // util
    static double[] niceTicks(double min, double max, int target) {
        double range = max - min;
        if (range <= 0 || !Double.isFinite(range)) return new double[]{min};
        double rough = range / target;
        double mag = Math.pow(10, Math.floor(Math.log10(rough)));
        double norm = rough / mag;
        double step = (norm < 1.5 ? 1 : norm < 3 ? 2 : norm < 7 ? 5 : 10) * mag;
        double start = Math.ceil(min / step) * step;
        List<Double> ticks = new ArrayList<>();
        for (double v = start; v <= max + step * 1e-9; v += step) {
            ticks.add(Math.abs(v) < step * 1e-9 ? 0.0 : v);
        }
        double[] out = new double[ticks.size()];
        for (int i = 0; i < out.length; i++) out[i] = ticks.get(i);
        return out;
    }

    private static String tickLabel(double v, double[] ticks) {
        double step = ticks.length > 1 ? Math.abs(ticks[1] - ticks[0]) : 1;
        if (Math.abs(v) >= 1e5 || (step < 1e-3 && v != 0)) return String.format("%.2e", v);
        int decimals = Math.max(0, (int) Math.ceil(-Math.log10(step)));
        return String.format("%." + decimals + "f", v);
    }

    private static String fmt(double v) {
        double a = Math.abs(v);
        if (a != 0 && (a < 1e-3 || a >= 1e6)) return String.format("%.3e", v);
        return String.format("%.3f", v);
    }
}