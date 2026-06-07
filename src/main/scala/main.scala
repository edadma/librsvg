import io.github.edadma.librsvg.*
import io.github.edadma.libcairo.*

@main def run(): Unit =
  val svg =
    """<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64">
      |  <rect x="4" y="4" width="56" height="56" rx="12" fill="#4dabf7"/>
      |  <circle cx="32" cy="32" r="16" fill="#ffffff"/>
      |  <path d="M24 32 l6 6 l12 -14" stroke="#4dabf7" stroke-width="4" fill="none"
      |        stroke-linecap="round" stroke-linejoin="round"/>
      |</svg>""".stripMargin

  val handle = handleNewFromString(svg)

  handle.intrinsicSize.foreach { case (w, h) => println(s"intrinsic size: ${w}x$h") }

  val surface = imageSurfaceCreate(Format.ARGB32, 64, 64)
  val cr      = surface.create

  // The SVG draws straight into the Cairo context, scaled to fill the 64x64 viewport.
  handle.renderDocument(cr, 0, 0, 64, 64)

  surface.writeToPNG("rsvg-demo.png")
  println("wrote rsvg-demo.png")

  cr.destroy()
  surface.destroy()
  handle.destroy()
