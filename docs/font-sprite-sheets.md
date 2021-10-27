# Font Sprite Sheets

See also `docs/pixel-identical-outputs.md`

# Details

Stored as PNGs in `font-sprite-sheets/generated`

This would also shield our tests from any text rendering changes in different Java versions.


Making sheets:

	java -cp plantuml.jar net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetMaker

Printing data:

	java -cp plantuml.jar net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetDumper <FONT_SPRITE_FILE>

A brief unscientific experiment on my laptop suggests `FontSpriteSheet.drawString()` is only 1.5x slower
than `Graphics2D.drawString()` so not very significant.

font diff explained in:
loopmacros.h line 1672 "Antialiased glyph drawing results in artifacts around the character edges"

* sometimes off by one vertically
* alpha is different

// I think it's because Graphics2D.drawString() uses floating point for alpha calculations
// but alphaImage stores 8-bit integers so there is less accuracy here.
// but some values are off by one when colorAlpha is between 128 and 254.

https://stackoverflow.com/questions/63849522/why-does-java-awt-font-getstringbounds-give-different-result-on-different-machin
