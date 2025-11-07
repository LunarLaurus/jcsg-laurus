package net.laurus.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Timer;

import eu.mihosoft.vvecmath.Vector3d;
import lombok.Getter;
import lombok.Setter;
import net.laurus.builder.LayeredShapeBuilder;
import net.laurus.shape.ColoredTriangle;
import net.laurus.shape.Edge;
import net.laurus.shape.Triangle;
import net.laurus.util.Graphics3DUtils;
import net.laurus.util.ShapeUtils;

@Getter
@Setter
public class ModelPanel extends JPanel {

    private LayeredShapeBuilder builder;

    private List<ColoredTriangle> coloredTris;

    private double scale = 10;

    private double rotX = 20;

    private double rotY = -30;

    private Point lastDrag;

    private boolean wireframe = false;

    private boolean showEdges = true;

    private boolean showAxes = true;

    private boolean showShading = false;

    private final ModelPanelState prevState;

    // Add this field to ModelPanel
    private Vector3d lightDir = Vector3d.xyz(0, 0, 1).normalized(); // light pointing +Z

    public ModelPanel(LayeredShapeBuilder builder) {
        this.builder = builder;
        this.prevState = new ModelPanelState(this);

        initMouseControls();
        startAutoRepaintCheck();
        generateTriangles();
    }

    /** Generates colored triangles from the builder's layers */
    private void generateTriangles() {

        if (wireframe) {
            coloredTris = ShapeUtils.buildWireframeTriangles(builder.getLayers());
        }
        else {
            coloredTris = ShapeUtils.buildSolidColoredTriangles(builder.getLayers());
        }

    }

    /** Initialize mouse drag and zoom controls */
    private void initMouseControls() {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                lastDrag = e.getPoint();
            }

        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastDrag.x;
                int dy = e.getY() - lastDrag.y;
                rotY += dx * 0.5;
                rotX += dy * 0.5;
                lastDrag = e.getPoint();
                repaint();
            }

        });

        addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            scale *= Math.pow(1.1, -notches);
            scale = Math.max(1, Math.min(scale, 100));
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (coloredTris == null) {
            return;
        }

        Graphics2D g2 = setupGraphics2D(g);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        double[][] rotMat = Graphics3DUtils.rotationMatrix(rotX, rotY);
        Map<Vector3d, Point> vertexMap = computeVertexMap(rotMat, cx, cy);

        if (!wireframe) {
            drawSolidFaces(g2, vertexMap);
        }

        drawEdges(g2, vertexMap);
        drawOriginAndAxes(g2, vertexMap);
        drawViewInfo(g2);
    }

    /** Configures Graphics2D with anti-aliasing and background */
    private Graphics2D setupGraphics2D(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        return g2;
    }

    /** Precompute vertex positions with rotation and scale */
    private Map<Vector3d, Point> computeVertexMap(double[][] rotMat, int cx, int cy) {
        return Graphics3DUtils
                .computeVertexMap(
                        coloredTris.stream().map(ct -> ct.triangle).toList(), rotMat, cx, cy, scale
                );
    }

    /** Draw all solid (non-wireframe) triangles with flat shading */
    private void drawSolidFaces(Graphics2D g2, Map<Vector3d, Point> vertexMap) {

        for (ColoredTriangle ct : coloredTris) {

            if (showShading) {
                drawShadedTriangle(g2, vertexMap, ct, lightDir);
            }
            else {
                g2.setColor(ct.color);
                Graphics3DUtils.drawTriangle(g2, vertexMap, ct.triangle);

            }

        }

    }

    /** Draw edges, either wireframe or boundary edges */
    private void drawEdges(Graphics2D g2, Map<Vector3d, Point> vertexMap) {

        if (wireframe) {

            // Wireframe: draw all triangle edges
            for (ColoredTriangle ct : coloredTris) {
                g2.setColor(ct.subtractive ? Color.RED : Color.BLACK);
                Graphics3DUtils.drawTriangleEdges(g2, vertexMap, ct.triangle);
            }

        }
        else if (showEdges) {
            // Solid: only draw boundary edges for additive triangles
            List<Triangle> additiveTris = coloredTris
                    .stream()
                    .filter(ct -> !ct.subtractive)
                    .map(ct -> ct.triangle)
                    .toList();

            Set<Edge> boundaryEdges = Graphics3DUtils.computeBoundaryEdges(additiveTris);

            g2.setColor(Color.BLACK);
            Graphics3DUtils.drawEdges(g2, vertexMap, boundaryEdges);
        }

    }

    /** Draw origin marker and axes */
    private void drawOriginAndAxes(Graphics2D g2, Map<Vector3d, Point> vertexMap) {
        // Helper to map arbitrary vertex (rotated and scaled) to screen
        java.util.function.Function<Vector3d, Point> mapVertex = v -> {
            Point p = vertexMap.get(v);

            if (p != null) {
                return p;
            }

            double[][] rotMat = Graphics3DUtils.rotationMatrix(rotX, rotY);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            double[] r = Graphics3DUtils.multiply(rotMat, new double[] {
                    v.x(), v.y(), v.z()
            });
            return new Point((int) (cx + r[0] * scale), (int) (cy - r[1] * scale));
        };

        // Origin
        Vector3d origin = Vector3d.xyz(0, 0, 0);
        Point p0 = mapVertex.apply(origin);

        if (p0 != null) {
            int size = 10;
            g2.setColor(Color.RED);
            g2.drawLine(p0.x - size, p0.y - size, p0.x + size, p0.y + size);
            g2.drawLine(p0.x - size, p0.y + size, p0.x + size, p0.y - size);
        }

        if (showAxes && p0 != null) {
            int axisLength = 50;
            // X axis
            Point px = mapVertex.apply(Vector3d.xyz(axisLength, 0, 0));

            if (px != null) {
                g2.setColor(Color.RED);
                g2.drawLine(p0.x, p0.y, px.x, px.y);
            }

            // Y axis
            Point py = mapVertex.apply(Vector3d.xyz(0, axisLength, 0));

            if (py != null) {
                g2.setColor(Color.GREEN);
                g2.drawLine(p0.x, p0.y, py.x, py.y);
            }

            // Z axis
            Point pz = mapVertex.apply(Vector3d.xyz(0, 0, axisLength));

            if (pz != null) {
                g2.setColor(Color.BLUE);
                g2.drawLine(p0.x, p0.y, pz.x, pz.y);
            }

        }

    }

    /** Draw rotation and scale info in top-left corner */
    private void drawViewInfo(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        String info = String.format("Rotation: X=%.1f°, Y=%.1f°, Scale=%.2f", rotX, rotY, scale);
        g2.drawString(info, 10, 20);
    }

    /** Refresh triangles and repaint */
    public void refresh() {
        generateTriangles();
        repaint();
    }

    /** Auto repaint when state changes */
    private void startAutoRepaintCheck() {
        Timer timer = new Timer(16, e -> {

            if (prevState.hasChanged(this)) {
                prevState.update(this);
                repaint();
            }

        });
        timer.start();
    }

    /** Draw a single triangle with flat shading */
    private void drawShadedTriangle(
            Graphics2D g2,
            Map<Vector3d, Point> vertexMap,
            ColoredTriangle ct,
            Vector3d lightDir
    ) {
        Triangle t = ct.triangle;
        Vector3d normal = computeNormal(t);

        // compute brightness (ambient + directional)
        double ambient = 0.2;
        double brightness = ambient + (1 - ambient) * Math.max(0, normal.dot(lightDir));

        Color orig = ct.color;
        Color shaded = new Color(
                Math.min(255, (int) (orig.getRed() * brightness)),
                Math.min(255, (int) (orig.getGreen() * brightness)),
                Math.min(255, (int) (orig.getBlue() * brightness))
        );

        Point p0 = vertexMap.get(t.a());
        Point p1 = vertexMap.get(t.b());
        Point p2 = vertexMap.get(t.c());

        if (p0 == null || p1 == null || p2 == null) {
            return;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(p0.x, p0.y);
        path.lineTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.closePath();

        g2.setColor(shaded);
        g2.fill(path);
    }

    /** Compute triangle normal */
    private Vector3d computeNormal(Triangle t) {
        Vector3d ab = t.b().minus(t.a());
        Vector3d ac = t.c().minus(t.a());
        return ab.crossed(ac).normalized();
    }

    public void setBuilder(LayeredShapeBuilder newBuilder) {
        builder = newBuilder;
        refresh();
    }

}
