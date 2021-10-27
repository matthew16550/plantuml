*TL;DR:
Everyone that uses `!pragma testing_font` and a recent [Temurin][temurin] or [Zulu][zulu] Java will mostly see pixel identical outputs.*

# Pixel Identical Outputs

PlantUML outputs are slightly different on Linux / macOS / Windows and sometimes change with different Java runtimes.
The differences are caused by Java internals that we cannot control, but can work around.

Our focus has been making test outputs that are pixel identical everywhere so one set of reference files can be shared
by all developers for regression testing.

We have not been working towards pixel identical output from every possible JRE / OS.  All we need for development is a
reasonable compromise that allows identical output on developer machines and in the CI workflow.

We have a mostly working first attempt, not many people have used it, YMMV.

Suggestions are welcome - the [GitHub discussion][github-discussion] might be the best place

Two separate areas cause the differences, Fonts & Lines / Curves

## 1. Fonts

The main differences are caused by Java measuring & drawing fonts differently on each OS (even when font files are identical).
Some new Java releases make changes too.

Most of PlantUML uses a `StringBounder` to calculate the size of a piece of text then adjusts other elements to suit
(e.g. text fitted inside a box).
Different OS / JREs give us slightly different sizes so our layout becomes slightly different.

Fonts are rendered inconsistently so `PNG`, `ANIMATED_GIF` & `MJPEG` outputs will have different pixels.

### Font Sprite Sheets

Font Sprite Sheets simplify our tests by avoiding the OS / JRE font differences.

Enable them via `!pragma testing_font` or `AbstractPSystem.FORCE_TESTING_FONT == true`.

* Font names in skinparam / styles are ignored.

* Font sizes are rounded to the nearest available sprite sheet (currently 9 / 11 / 14 / 20 point).

* All text is monospaced.

* `StringBounder` calculates from the sprite sheet so sizes are always the same.

* Only the basic ASCII characters (`0x21 .. 0x7e`) are supported, all others are drawn as tofu.

* Underline, strike & wave decorations are drawn with rastered lines after the font is drawn

* These areas have not been implemented so continue to have differing pixel outouts:
    * Svek layout & Dot diagrams (because `graphviz` does the rendering outside of PlantUML)
    * `UCenteredCharacter` (e.g. the circled "C" in class diagrams)
    * `!transformation`
    * Watermarks
    * Ditaa, Embedded, Jcckit, Latex, Math, Sudoku, Wire, XEarth diagrams
    * `ANIMATED_GIF`, `MJPEG` & `PDF` output formats
    * probably more ...
    
There is a test case (`FontSpriteSheetTest.test_drawing()`) that will alert us if the sprite sheet output is not identical everywhere.

See also `docs/font-sprite-sheets.md`.

## 2. Lines / Curves

Java renders anti-aliased lines & curves via a pluggable library, there is some history [here][marlin-history].

The exact rendering details have changed over time so different Java runtimes might cause different
`PNG` / `ANIMATED_GIF` / `MJPEG` output.

The library used by OpenJDK is called [Marlin][marlin].
The OpenJDK repo has its [own copy][openjdk-marlin] of the Marlin source which seems slightly out of sync with the Marlin repo
so version numbering was not immediately obvious.

Running java with `-Dsun.java2d.renderer.log=true` will log Marlin version details,
though if you are not using Marlin it might not log anything.

We found that rendering is identical for these runtimes on all operating systems:

* Temurin 11.0.12+7, 17+35 
* Zulu 8.56.0.21-CA, 11.50+19-CA, 12.3+11-CA, 13.42+17-CA, 14.29+23-CA, 15.34+17-CA, 16.32+15-CA, 17.28+13-CA

But these had slightly different output (we did not look into details as they are old releases)

* Temurin 8.0.302+8
* Zulu 9.0.7.1, 10.3+5

### For Now

We think, at least for now, that if you use any recent OpenJDK [Temurin][temurin] or [Zulu][zulu] release
then lines & curves will be identical. Perhaps other JREs will be suitable as well.

There is a test case (`RasterTest.test_raster_engine()`) that should alert us if line or curve rendering has changed.

### Future

In future perhaps OpenJDK will tweak the anti-aliasing algorithm,
if that happens we might be able to ignore it for a long time by staying with an older JRE for testing.
Or we might be able to [use an older version][marlin-use] of the rendering library.

Eventually we may need to update reference files to match a new JRE but with any luck it will not happen often.


[github-discussion]: https://github.com/plantuml/plantuml/discussions/681
[marlin]: https://github.com/bourgesl/marlin-renderer
[marlin-history]: https://github.com/bourgesl/marlin-renderer/wiki/History-and-context
[marlin-use]: https://github.com/bourgesl/marlin-renderer/wiki/How-to-Use
[openjdk-marlin]: https://github.com/openjdk/jdk/tree/master/src/java.desktop/share/classes/sun/java2d/marlin
[temurin]: https://adoptium.net/
[zulu]: https://azul.com/downloads
