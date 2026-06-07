package io.github.edadma.librsvg

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import io.github.edadma.librsvg.extern.LibRsvg as lib
import io.github.edadma.libcairo.Context

/** Raised when an SVG document cannot be loaded or parsed. Carries librsvg's own error message. */
class RsvgException(message: String) extends RuntimeException(message)

/** A loaded SVG document — a thin wrapper over librsvg's `RsvgHandle`. Create one with
  * [[handleNewFromFile]], [[handleNewFromData]] or [[handleNewFromString]], draw it into a Cairo
  * [[io.github.edadma.libcairo.Context]] with `renderDocument`, and release it with `destroy`
  * when finished. */
implicit class Handle private[librsvg] (val handle: lib.RsvgHandlep) extends AnyVal:

  /** Render the whole SVG document into the `viewport` rectangle (in the context's user space) on
    * `cr`. The drawing honours any clip, transform and group opacity already set on the context, so
    * an SVG composites into a larger scene exactly like native Cairo drawing — no pixel round-trip.
    * Returns `true` on success. */
  def renderDocument(cr: Context, x: Double, y: Double, width: Double, height: Double): Boolean =
    val rect = stackalloc[lib.RsvgRectangle]()

    rect._1 = x
    rect._2 = y
    rect._3 = width
    rect._4 = height
    lib.rsvg_handle_render_document(handle, cr.cr, rect, null) != 0

  /** The document's intrinsic size in pixels, when it declares one (a `width`/`height` in absolute
    * units on the root `<svg>`). `None` when the document is sized only in relative units. */
  def intrinsicSize: Option[(Double, Double)] =
    val w = stackalloc[CDouble]()
    val h = stackalloc[CDouble]()

    if lib.rsvg_handle_get_intrinsic_size_in_pixels(handle, w, h) != 0 then Some((!w, !h)) else None

  /** Set the resolution used to resolve physical units (`pt`, `cm`, …) in the document. */
  def setDpi(dpi: Double): Unit = lib.rsvg_handle_set_dpi(handle, dpi)

  /** Release the underlying handle. The wrapper must not be used afterwards. */
  def destroy(): Unit = lib.g_object_unref(handle.asInstanceOf[Ptr[Byte]])

end Handle

// A null handle means the load failed; librsvg leaves the reason in the GError out-parameter. Copy
// the message into a Scala string before freeing the error, then surface it as an exception.
private def loaded(h: lib.RsvgHandlep, errp: Ptr[lib.GErrorp], source: String): Handle =
  if h == null then
    val err = !errp
    val msg = if err == null then "unknown error" else fromCString(err._3)

    if err != null then lib.g_error_free(err)
    throw new RsvgException(s"librsvg: failed to load SVG from $source: $msg")
  new Handle(h)

/** Load an SVG document from a file path. Throws [[RsvgException]] if it can't be read or parsed. */
def handleNewFromFile(filename: String): Handle = Zone {
  val errp = stackalloc[lib.GErrorp]()

  !errp = null
  loaded(lib.rsvg_handle_new_from_file(toCString(filename), errp), errp, filename)
}

/** Load an SVG document from raw bytes (handles both plain `.svg` and gzip-compressed `.svgz`).
  * Throws [[RsvgException]] on a parse error. */
def handleNewFromData(data: Array[Byte]): Handle = Zone {
  val len = data.length
  val buf = alloc[Byte](len.toUSize)
  var i   = 0

  while i < len do
    buf(i) = data(i)
    i += 1

  val errp = stackalloc[lib.GErrorp]()

  !errp = null
  loaded(lib.rsvg_handle_new_from_data(buf, len.toUSize, errp), errp, "data")
}

/** Load an SVG document from a string of SVG markup. Throws [[RsvgException]] on a parse error. */
def handleNewFromString(svg: String): Handle =
  handleNewFromData(svg.getBytes("UTF-8"))
