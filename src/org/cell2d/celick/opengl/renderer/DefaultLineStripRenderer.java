package org.cell2d.celick.opengl.renderer;

/**
 * The default version of the renderer relies of GL calls to do everything. 
 * Unfortunately this is driver dependent and often implemented inconsistantly
 * 
 * @author kevin
 */
public class DefaultLineStripRenderer implements LineStripRenderer {
	/** The access to OpenGL */
	private SGL GL = Renderer.get();
	
        @Override
	public void end() {
		GL.glEnd();
	}

        @Override
	public void setAntiAlias(boolean antialias) {
		if (antialias) {
			GL.glEnable(SGL.GL_LINE_SMOOTH);
		} else {
			GL.glDisable(SGL.GL_LINE_SMOOTH);
		}
	}

        @Override
	public void setWidth(float width) {
		GL.glLineWidth(width);
	}

        @Override
	public void start() {
		GL.glBegin(SGL.GL_LINE_STRIP);
	}

        @Override
	public void vertex(float x, float y) {
		GL.glVertex2f(x,y);
	}

        @Override
	public void color(float r, float g, float b, float a) {
		GL.glColor4f(r, g, b, a);
	}

        @Override
	public void setLineCaps(boolean caps) {
	}

        @Override
	public boolean applyGLLineFixes() {
		return true;
	}

}
