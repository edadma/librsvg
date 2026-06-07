package io.github.edadma.librsvg.extern

import scala.scalanative.unsafe.*

import io.github.edadma.libcairo.extern.LibCairo.cairo_tp

// Raw librsvg C entry points. librsvg's internals were rewritten in Rust, but it still ships a
// stable C ABI (`librsvg-2`), so it binds like any other C library. An `RsvgHandle` is a GObject,
// hence the `gobject-2.0` link for `g_object_unref` (the documented way to release a handle).
@link("rsvg-2")
@link("gobject-2.0")
@extern
object LibRsvg:
  type RsvgHandle  = CStruct0
  type RsvgHandlep = Ptr[RsvgHandle]

  // GError { GQuark domain; gint code; gchar *message; } — the message is read on a load failure.
  type GError  = CStruct3[CUnsignedInt, CInt, CString]
  type GErrorp = Ptr[GError]

  // RsvgRectangle { double x, y, width, height; } — the destination viewport for a render.
  type RsvgRectangle  = CStruct4[CDouble, CDouble, CDouble, CDouble]
  type RsvgRectanglep = Ptr[RsvgRectangle]

  def rsvg_handle_new_from_file(filename: CString, error: Ptr[GErrorp]): RsvgHandlep                  = extern
  def rsvg_handle_new_from_data(data: Ptr[Byte], data_len: CSize, error: Ptr[GErrorp]): RsvgHandlep   = extern
  def rsvg_handle_render_document(
      handle: RsvgHandlep,
      cr: cairo_tp,
      viewport: RsvgRectanglep,
      error: Ptr[GErrorp],
  ): CInt = extern
  def rsvg_handle_get_intrinsic_size_in_pixels(
      handle: RsvgHandlep,
      out_width: Ptr[CDouble],
      out_height: Ptr[CDouble],
  ): CInt = extern
  def rsvg_handle_set_dpi(handle: RsvgHandlep, dpi: CDouble): Unit = extern

  def g_object_unref(obj: Ptr[Byte]): Unit = extern
  def g_error_free(error: GErrorp): Unit   = extern
