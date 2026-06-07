librsvg
=======

![Maven Central](https://img.shields.io/maven-central/v/io.github.edadma/librsvg_native0.5_3)
[![Last Commit](https://img.shields.io/github/last-commit/edadma/librsvg)](https://github.com/edadma/librsvg/commits)
![License](https://img.shields.io/github/license/edadma/librsvg)
![Scala Version](https://img.shields.io/badge/Scala-3.8.4-blue.svg)
![Scala Native Version](https://img.shields.io/badge/Scala_Native-0.5.12-blue.svg)

*librsvg* provides Scala Native bindings for [librsvg](https://gitlab.gnome.org/GNOME/librsvg), the
GNOME SVG rendering library. It renders SVG documents **directly into a [Cairo](https://www.cairographics.org/)
context**, so a scalable vector graphic composites into a larger Cairo scene with full clip,
transform and opacity support — no intermediate raster step.

Overview
--------

librsvg's internals were rewritten in Rust, but the library still ships a stable C API/ABI
(`librsvg-2`), so it binds like any other C library. This project pairs it with
[libcairo](https://github.com/edadma/libcairo): you load an SVG into a `Handle`, then render it onto
a libcairo `Context`.

The programmer-friendly facade lives in the `io.github.edadma.librsvg` package — that is the only
package you need to import. The `io.github.edadma.librsvg.extern` package holds the raw C interop
using Scala Native's `unsafe` types; nothing in the public facade exposes those types, so you never
deal with manual memory management or pointer conversions.

Usage
-----

This library is published for **Scala Native** to Maven Central. The librsvg C library (and Cairo)
must be installed on your system:

```shell
brew install librsvg              # macOS (Homebrew) — pulls in cairo + glib
sudo apt install librsvg2-dev     # Debian / Ubuntu
```

Enable Scala Native in `project/plugins.sbt`:

```sbt
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.12")
```

Add the dependency in `build.sbt` (it brings `libcairo` transitively):

```sbt
libraryDependencies += "io.github.edadma" %%% "librsvg" % "0.0.1"
```

Then import the facade:

```scala
import io.github.edadma.librsvg._
import io.github.edadma.libcairo._
```

Example
-------

Load an SVG from a string, render it into a Cairo image surface, and write a PNG:

```scala
import io.github.edadma.librsvg._
import io.github.edadma.libcairo._

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

  handle.renderDocument(cr, 0, 0, 64, 64)   // draws into the 64x64 viewport

  surface.writeToPNG("rsvg-demo.png")

  cr.destroy()
  surface.destroy()
  handle.destroy()
```

API
---

Loading (each throws `RsvgException` on a parse error):

- `handleNewFromFile(filename: String): Handle`
- `handleNewFromData(data: Array[Byte]): Handle` — plain `.svg` or gzip-compressed `.svgz`
- `handleNewFromString(svg: String): Handle`

On a `Handle`:

- `renderDocument(cr: Context, x, y, width, height): Boolean` — render into a viewport rectangle on
  a libcairo `Context`
- `intrinsicSize: Option[(Double, Double)]` — the document's pixel size when it declares one
- `setDpi(dpi: Double): Unit` — resolution for physical units (`pt`, `cm`, …)
- `destroy(): Unit` — release the handle

Documentation
-------------

API documentation is forthcoming; documentation for the C library is found
[here](https://gnome.pages.gitlab.gnome.org/librsvg/Rsvg-2.0/).

License
-------

[ISC](https://github.com/edadma/librsvg/blob/main/LICENSE)
