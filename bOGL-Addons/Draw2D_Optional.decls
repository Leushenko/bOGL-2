
; Optional function declarations for the Draw2D bOGL addon
; This file is only really to provide syntax highlighting

; Array parameters (e.g. 'arr3') are represented with underscores (e.g. 'arr__3__')

.lib " "

InitDraw2D()
SetViewport2D(x, y, w, h)
SetVirtualResolution2D(x, y)
BeginDraw2D()
EndDraw2D()
SetColor2D(r, g, b, a#)
SetBlend2D(mode)
SetClsColor2D(r, g, b)
Cls2D()
LoadFont2D%(fontname$)
FreeFont2D(font)
SetFont2D(font, size, height#, spacing#, italic#)
Text2D(x#, y#, s$, align)
SetMaterial2D(tex)
Plot2D(x#, y#, radius#)
Line2D(x1#, y1#, x2#, y2#, width#)
Rect2D(x#, y#, w#, h#, fill, border#)
Oval2D(xc#, yc#, xr#, yr#, fill, border#)
Poly2D(x1#, y1#, x2#, y2#, x3#, y3#)
SetScale2D(xscale#, yscale#)
SetRotation2D(angle#)
DrawImage2D(img, x#, y#)
GrabImage2D%(img, x, y, width, height)
DrawSubRect2D(img, x#, y#, fromx, fromy, width, height)
DrawImageLine2D(img, x1#, y1#, x2#, y2#, width#, stretch)
DrawImageQuad2D(img, x1#, y1#, x2#, y2#, x3#, y3#, x4#, y4#)
ApplyAlphaMap(img, amap)
ApplyMaskColor(img, mask)
GetBuffer2D(tex, asBRGA)
CommitBuffer2D(buf)
GetPixel2D(buf, x, y)
SetPixel2D(buf, x, y, pixel)
StringWidth2D(s$)

