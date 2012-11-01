
; bOGL 2
;========


; Command reference:

; Graphics3D(title$, width, height, depth, mode)
; CreateCanvas3D(x, y, width,height, group)
; AmbientLight(red, green, blue)
; CreateCamera([parent])
; CameraRange(handler, near#, far#)
; CameraFieldOfView(handler, angle#)
; CameraDrawMode(handler, mode), CameraClsMode(handler, mode)
; CameraClsColor(handler, red, green, blue)
; CameraFogMode(handler, mode[, near#[, far#]])
; CameraFogColor(handler, red, green, blue[, alpha#])
; CameraViewport(handler, x, y, width, height)
; CreateLight(red, green, blue, flag[, parent])
; LightRange(handler, range#)
; CreatePivot([parent])
; CreateMesh([parent])
; AddVertex(mesh, x#, y#, z#[, u#, v#]), AddTriangle(mesh, v0, v1, v2)
; VertexCoords(mesh, v, x#, y#, z#), VextexTexCoords(mesh, v, u#, v#)
; VertexNormal(mesh, v, nx#, ny#, nz#), VertexColor(mesh, v, r#, g#, b#)
; VertexX#(mesh, v), VertexY#(mesh, v), VertexZ#(mesh, v), VertexU#(mesh, v), VertexV#(mesh, v)
; CreateCube([parent])
; CreateSprite([parent])
; LoadTerrain(terrain$[, parent])
; PositionEntity(handler, x#, y#, z#[, absolute])
; MoveEntity(handler, x#, y#, z#)
; RotateEntity(handler, x#, y#, z#[, absolute])
; TurnEntity(handler, x#, y#, z#)
; PointEntity(handler, x#, y#, z#[, roll#])
; ScaleEntity(handler, x#, y#, z#[, absolute])
; PaintEntity(handler, red, green, blue)
; EntityAlpha(handler, alpha#), EntityFX(handler, flags)
; EntityTexture(handler, texture)
; ShowEntity(handler, state)
; EntityParent(handler, parentH), GetParentEntity(handler)
; CountChildren(handler), GetChildEntity(handler, index)
; RegisterEntityUserDataSlot(), SetEntityUserData(handler, slot, val), GetEntityUserData(handler, slot)
; CopyEntity(handler)
; FreeEntity(handler)
; FlipPolygons(handler)
; RotateSubMesh(handler, vf, vt, rx#, ry#, rz#, cx#, cy#, cz#)
; TranslateSubMesh(handler, vf, vt, tx#, ty#, tz#)
; ScaleSubMesh(handler, vf, vt, sx#, sy#, sz#, cx#, cy#, cz#)
; EntityX#(handler[, absolute]), EntityY#(handler[, absolute]), EntityZ#(handler[, absolute])
; EntityXAngle#(handler[, absolute]), EntityYAngle#(handler[, absolute]), EntityZAngle#(handler[, absolute])
; CreateTexture(width, height[, filter])
; LoadTexture(file$[, quality, filter])
; FreeTexture(handler)
; TextureWidth(handler), TextureHeight(handler)
; GetTextureData(handler), UpdateTexture(handler, x, y, width, height, pixels)
; RenderWorld([stencilMode]), RenderStencil()
; Distance(x1#, y1#, z1#, x2#, y2#, z2#)
; TFormPoint(x#, y#, z#, src, dst, out#[2])


Include "bOGL\ogld_utils.bb"
Include "bOGL\wgl_const.bb"
Include "bOGL\gl_const.bb"
Include "bOGL\glu_const.bb"
Include "bOGL\blitz_gl.bb"


Global bOGL_hMainDC					; Render window's device context
Global bOGL_bbHwnd					; Window's handle.
Global bOGL_bbHwndW, bOGL_bbHwndH	; Window's size.

Const BOGL_CLASS_MESH = 1, BOGL_CLASS_PIV = 2, BOGL_CLASS_CAM = 3, BOGL_CLASS_LIGHT = 4
Const BOGL_LIGHT_PT = 1, BOGL_LIGHT_DIR = 2, BOGL_LIGHT_AMB = 3
Const BOGL_FX_FULLBRIGHT = 1, BOGL_FX_FLATSHADED = 2, BOGL_FX_NOFOG = 4, BOGL_FX_ADDBLEND = 8, BOGL_FX_MULBLEND = 16, BOGL_FX_NOCULL = 32
Const BOGL_FX_STENCIL_KEEP = 0, BOGL_FX_STENCIL_INCR = 128, BOGL_FX_STENCIL_DECR = 256, BOGL_FX_STENCIL_BOTH = 1024
Const BOGL_CAM_OFF = 0, BOGL_CAM_PERSPECTIVE = 1, BOGL_CAM_ORTHO = 2, BOGL_CAM_STENCIL = 3
Const BOGL_CAM_CLRCOL = GL_COLOR_BUFFER_BIT, BOGL_CAM_CLRZ = GL_DEPTH_BUFFER_BIT, BOGL_CAM_CLRSTEN = GL_STENCIL_BUFFER_BIT
Const BOGL_STENCIL_OFF = GL_ALWAYS, BOGL_STENCIL_FALSE = GL_EQUAL, BOGL_STENCIL_TRUE = GL_LESS
Const BOGL_DEFAULT_COLOR = (200 Or (200 Shl 8) Or (200 Shl 16) Or ($FF000000))
Const BOGL_VERT_STRIDE = 32, BOGL_TRIS_STRIDE = 6, BOGL_COL_STRIDE = 3, BOGL_VERT_CAP = 65536
Const BOGL_EPSILON# = 0.001, BOGL_LIGHT_EPSILON# = 1.0 / 255.0


Type bOGL_Ent
	Field handler, parentH
	Field hidden, userData
	Field x#, y#, z#, sx#, sy#, sz#, r#[3], q#[3], Rv, Qv
	Field g_x#, g_y#, g_z#, g_sx#, g_sy#, g_sz#, g_r#[3], g_q#[3], g_Rv, g_Qv, Gv
	Field eClass, c.bOGL_Cam, l.bOGL_Light, m.bOGL_Mesh
	Field children
End Type

Type bOGL_Cam
	Field ent.bOGL_Ent
	Field vpx, vpy, vpw, vph, drawmode
	Field viewangle#, near#, far#, clscol, clsmode
	Field fogmode, fognear#, fogfar#, fogcolor
End Type

Type bOGL_Light
	Field ent.bOGL_Ent
	Field flag, glFlag, pos, col, att
End Type

Type bOGL_Mesh
	Field ent.bOGL_Ent
	Field texture.bOGL_Tex, argb, alpha#, FX
	Field vp, vc, poly	;UV+Normal+Position, Colour, Tris
End Type

Type bOGL_Tex
	Field glName, width, height, filter		;Do not move glName from first position
	Field rc, asVar
End Type


; Interface functions
;=====================

Function Graphics3D(title$, width, height, depth, mode)
	AppTitle(title)
	Graphics width, height, depth, mode
	bOGL_bbHwnd = ogld_GetWindow()
	Dim bOGL_EntList_.bOGL_Ent(0) : bOGL_Init_ width, height
End Function

Function CreateCanvas3D(x, y, width, height, group)
	Local canvas = CreateCanvas(x, y, width, height, group)
	bOGL_bbHwnd = QueryObject(canvas, 1)
	bOGL_Init_ width, height
	Return canvas
End Function

Function AmbientLight(red, green, blue)
	If Not bOGL_AmbientLight_ Then bOGL_AmbientLight_ = CreateBank(16) : PokeFloat bOGL_AmbientLight_, 8, 1.0
	PokeFloat bOGL_AmbientLight_, 0, red / 255.0
	PokeFloat bOGL_AmbientLight_, 4, green / 255.0
	PokeFloat bOGL_AmbientLight_, 8, blue / 255.0
End Function

Function CreateCamera(parentH = 0)
	Local this.bOGL_Cam = New bOGL_Cam, ent.bOGL_Ent = bOGL_NewEnt_(BOGL_CLASS_CAM, Handle this, parentH)
	
	this\ent = ent
	this\vpx = 0 : this\vpy = 0
	this\vpw = bOGL_bbHwndW : this\vph = bOGL_bbHwndH
	this\drawmode = BOGL_CAM_PERSPECTIVE : this\clsmode = BOGL_CAM_CLRCOL Or BOGL_CAM_CLRZ
	this\viewangle# = 45.0
	this\near# = 1 ;0.1
	this\far# = 1000
	
	Return ent\handler
End Function

Function CreateLight(red, green, blue, flag, parentH = 0)
	Local this.bOGL_Light = New bOGL_Light, ent.bOGL_Ent = bOGL_NewEnt_(BOGL_CLASS_LIGHT, Handle this, parentH)
	this\ent = ent
	
	this\pos = CreateBank(16)
	PokeFloat this\pos, 0, 0
	PokeFloat this\pos, 4, 0
	PokeFloat this\pos, 8, 0
	PokeFloat this\pos, 12, (flag <> BOGL_LIGHT_DIR)
	
	this\col = CreateBank(16)
	PokeFloat this\col, 0, red / 255.0
	PokeFloat this\col, 4, green / 255.0
	PokeFloat this\col, 8, blue / 255.0
	PokeFloat this\col, 12, 1
	
	this\att = CreateBank(4)
	PokeFloat this\att, 0, 0
	
	this\flag = flag
	If flag = BOGL_LIGHT_PT Or flag = BOGL_LIGHT_DIR Then this\glFlag = GL_DIFFUSE : Else this\glFlag = GL_AMBIENT
	
	Return ent\handler
End Function

Function LightRange(handler, range#)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	If range > 0 Then PokeFloat this\l\att, 0, 255.0 / range : Else PokeFloat this\l\att, 0, 0
End Function

Function CreatePivot(parentH = 0)
	Local this.bOGL_Ent = bOGL_NewEnt_(BOGL_CLASS_PIV, 0, parentH)
	Return this\handler
End Function

Function CreateMesh(parentH = 0)
	Local this.bOGL_Mesh = New bOGL_Mesh, ent.bOGL_Ent = bOGL_NewEnt_(BOGL_CLASS_MESH, Handle this, parentH)
	this\ent = ent
	this\argb = BOGL_DEFAULT_COLOR : this\alpha = 1.0
	
	this\vp = CreateBank(0)
	this\poly = CreateBank(0)
	
	Return ent\handler
End Function

Function AddVertex(handler, x#, y#, z#, u# = 0.0, v# = 0.0)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	Local idx = BankSize(m\vp)
	ResizeBank m\vp, idx + BOGL_VERT_STRIDE
	PokeFloat m\vp, idx, u		;UVs go first
	PokeFloat m\vp, idx + 4, v
	PokeFloat m\vp, idx + 20, x	;3D coords go last (normals set elsewhere)
	PokeFloat m\vp, idx + 24, y
	PokeFloat m\vp, idx + 28, z
	Return idx / BOGL_VERT_STRIDE
End Function

Function AddTriangle(handler, v0, v1, v2)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	Local idx = BankSize(m\poly)
	ResizeBank m\poly, idx + BOGL_TRIS_STRIDE
	PokeShort m\poly, idx, v0
	PokeShort m\poly, idx + 2, v1
	PokeShort m\poly, idx + 4, v2
	Return idx / BOGL_TRIS_STRIDE
End Function

Function VertexCoords(handler, v, x#, y#, z#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m, ptr = v * BOGL_VERT_STRIDE
	PokeFloat m\vp, ptr + 20, x : PokeFloat m\vp, ptr + 24, y : PokeFloat m\vp, ptr + 28, z
End Function

Function VextexTexCoords(handler, vi, u#, v#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m, ptr = v * BOGL_VERT_STRIDE
	PokeFloat m\vp, ptr, u : PokeFloat m\vp, ptr + 4, v
End Function

Function VertexNormal(handler, v, nx#, ny#, nz#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m, ptr = v * BOGL_VERT_STRIDE
	PokeFloat m\vp, ptr + 8, nx : PokeFloat m\vp, ptr + 12, ny : PokeFloat m\vp, ptr + 16, nz
End Function

Function VertexColor(handler, v, r, g, b)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	If Not m\vc
		Local sz = (BankSize(m\vp) / BOGL_VERT_STRIDE) * BOGL_COL_STRIDE, c
		m\vc = CreateBank(sz)
		For c = 0 To sz - 1
			PokeByte m\vc, c, $FF	;Set all vert colours to white to begin
		Next
	EndIf
	PokeByte m\vc, v * BOGL_COL_STRIDE, r
	PokeByte m\vc, v * BOGL_COL_STRIDE + 1, g
	PokeByte m\vc, v * BOGL_COL_STRIDE + 2, b
End Function

Function VertexX#(handler, v)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	Return PeekFloat(m\vp, v * BOGL_VERT_STRIDE + 20)
End Function

Function VertexY#(handler, v)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	Return PeekFloat(m\vp, v * BOGL_VERT_STRIDE + 24)
End Function

Function VertexZ#(handler, v)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	Return PeekFloat(m\vp, v * BOGL_VERT_STRIDE + 28)
End Function

Function VertexU#(handler, v)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	Return PeekFloat(m\vp, v * BOGL_VERT_STRIDE)
End Function

Function VertexV#(handler, v)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	Return PeekFloat(m\vp, v * BOGL_VERT_STRIDE + 4)
End Function

Function CreateCube(parentH = 0)
	Local m = CreateMesh(parentH), v0, v1, v2, v3
	
	v0 = AddVertex(m,-1, 1,-1, 0, 0) : VertexNormal m, v0, 0, 1, 0	;Top
	v1 = AddVertex(m,-1, 1, 1, 0, 1) : VertexNormal m, v1, 0, 1, 0
	v2 = AddVertex(m, 1, 1, 1, 1, 1) : VertexNormal m, v2, 0, 1, 0
	v3 = AddVertex(m, 1, 1,-1, 1, 0) : VertexNormal m, v3, 0, 1, 0
	AddTriangle m, v0, v1, v2 : AddTriangle m, v0, v2, v3
	
	v0 = AddVertex(m,-1,-1,-1, 0, 1) : VertexNormal m, v0, 0,-1, 0	;Bottom
	v1 = AddVertex(m,-1,-1, 1, 0, 0) : VertexNormal m, v1, 0,-1, 0
	v2 = AddVertex(m, 1,-1, 1, 1, 0) : VertexNormal m, v2, 0,-1, 0
	v3 = AddVertex(m, 1,-1,-1, 1, 1) : VertexNormal m, v3, 0,-1, 0
	AddTriangle m, v0, v2, v1 : AddTriangle m, v0, v3, v2
	
	v0 = AddVertex(m,-1, 1, 1, 1, 0) : VertexNormal m, v0,-1, 0, 0	;Left
	v1 = AddVertex(m,-1, 1,-1, 0, 0) : VertexNormal m, v1,-1, 0, 0
	v2 = AddVertex(m,-1,-1,-1, 0, 1) : VertexNormal m, v2,-1, 0, 0
	v3 = AddVertex(m,-1,-1, 1, 1, 1) : VertexNormal m, v3,-1, 0, 0
	AddTriangle m, v0, v1, v2 : AddTriangle m, v0, v2, v3
	
	v0 = AddVertex(m, 1, 1, 1, 0, 0) : VertexNormal m, v0, 1, 0, 0	;Right
	v1 = AddVertex(m, 1, 1,-1, 1, 0) : VertexNormal m, v1, 1, 0, 0
	v2 = AddVertex(m, 1,-1,-1, 1, 1) : VertexNormal m, v2, 1, 0, 0
	v3 = AddVertex(m, 1,-1, 1, 0, 1) : VertexNormal m, v3, 1, 0, 0
	AddTriangle m, v0, v2, v1 : AddTriangle m, v0, v3, v2
	
	v0 = AddVertex(m,-1, 1,-1, 1, 0) : VertexNormal m, v0, 0, 0,-1	;Front
	v1 = AddVertex(m, 1, 1,-1, 0, 0) : VertexNormal m, v1, 0, 0,-1
	v2 = AddVertex(m, 1,-1,-1, 0, 1) : VertexNormal m, v2, 0, 0,-1
	v3 = AddVertex(m,-1,-1,-1, 1, 1) : VertexNormal m, v3, 0, 0,-1
	AddTriangle m, v0, v1, v2 : AddTriangle m, v0, v2, v3
	
	v0 = AddVertex(m,-1, 1, 1, 0, 0) : VertexNormal m, v0, 0, 0, 1	;Back
	v1 = AddVertex(m, 1, 1, 1, 1, 0) : VertexNormal m, v1, 0, 0, 1
	v2 = AddVertex(m, 1,-1, 1, 1, 1) : VertexNormal m, v2, 0, 0, 1
	v3 = AddVertex(m,-1,-1, 1, 0, 1) : VertexNormal m, v3, 0, 0, 1
	AddTriangle m, v0, v2, v1 : AddTriangle m, v0, v3, v2
	
	Return m
End Function

Function CreateSprite(parentH = 0)
	Local m = CreateMesh(parentH)
	
	AddVertex m,-1, 1, 0, 1, 0 : VertexNormal m, 0, 0, 0,-1	;Forward-facing
	AddVertex m, 1, 1, 0, 0, 0 : VertexNormal m, 1, 0, 0,-1
	AddVertex m, 1,-1, 0, 0, 1 : VertexNormal m, 2, 0, 0,-1
	AddVertex m,-1,-1, 0, 1, 1 : VertexNormal m, 3, 0, 0,-1
	AddTriangle m, 0, 1, 2 : AddTriangle m, 0, 2, 3
	
	Return m
End Function

Function LoadTerrain(terrain$, parentH = 0)
	Local image = LoadImage(terrain$)
	If image
		Local m = CreateMesh(parentH), y, x, col
		Local width = ImageWidth(image), height = ImageHeight(image)
		
		For y = 0 To width - 1
			For x = 0 To height - 1
				Local argb = ReadPixel(x, y, ImageBuffer(image))
				col = (((argb Shr 16) And $FF) + ((argb Shr 8) And $FF) + (argb And $FF)) / 3
				AddVertex m, x, col / 255.0, y, x / Float width, y / Float height
			Next
		Next
		
		For y = 0 To height - 2
			For x = 0 To width - 2
				AddTriangle m, x + y * width, x + (y + 1) * width, x + 1 + (y + 1) * width
				AddTriangle m, x + y * width, x + 1 + (y + 1) * width, x + 1 + y * width
			Next
		Next
		
		FreeImage image
		Return m
	EndIf
End Function

Function CameraRange(handler, near#, far#)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	this\c\near = Abs(near) : this\c\far = Abs(far)
End Function

Function CameraFieldOfView(handler, viewangle#)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	this\c\viewangle = viewangle
End Function

Function CameraDrawMode(handler, mode)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : this\c\drawmode = mode	;Also reset the cls mode
	If mode = BOGL_CAM_STENCIL Then this\c\clsmode = BOGL_CAM_CLRZ + BOGL_CAM_CLRSTEN : Else this\c\clsmode = BOGL_CAM_CLRZ + BOGL_CAM_CLRCOL
End Function

Function CameraClsMode(handler, mode)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : this\c\clsmode = mode
End Function

Function CameraClsColor(handler, red, green, blue)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	this\c\clscol = $FF000000 Or ((red And $FF) Shl 16) Or ((green And $FF) Shl 8) Or (blue And $FF)
End Function

Function CameraFogMode(handler, mode, near# = 10.0, far# = 100.0)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	this\c\fogmode = mode
	this\c\fognear = near
	this\c\fogfar = far
	this\c\fogcolor = CreateBank(16)
End Function

Function CameraFogColor(handler, red, green, blue, alpha# = 1.0)
	Local this.bOGL_Ent = bOGL_EntList_(handler), c.bOGL_Cam = this\c
	If Not c\fogcolor Then c\fogcolor = CreateBank(16)
	PokeFloat c\fogcolor, 0, red
	PokeFloat c\fogcolor, 4, green
	PokeFloat c\fogcolor, 8, blue
	PokeFloat c\fogcolor, 12, alpha
End Function

Function CameraViewport(handler, x, y, width, height)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	this\c\vpx = x : this\c\vpw = width : this\c\vph = height
	this\c\vpy = bOGL_bbHwndH - (height + y)	;GL measures the viewport from the bottom
End Function

Function PositionEntity(handler, x#, y#, z#, absolute = False)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	If absolute Then x = x - this\g_x : y = y - this\g_y : z = z - this\g_z
	this\x = x : this\y = y : this\z = z : bOGL_InvalidateGlobalPosition_ this
End Function

Function MoveEntity(handler, x#, y#, z#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), v#[2]
	If Not this\Rv Then bOGL_UpdateAxisAngle_ this\r, this\q : this\Rv = True
	bOGL_RotateVector_ v, x, y, z, this\r
	this\x = this\x + v[0] : this\y = this\y + v[1] : this\z = this\z + v[2]
	bOGL_InvalidateGlobalPosition_ this
End Function

Function RotateEntity(handler, x#, y#, z#, absolute = False)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	If (absolute <> 0) And (this\parentH <> 0)
		Local p.bOGL_Ent = bOGL_EntList_(this\parentH), q#[3]
		If Not p\g_Qv Then bOGL_UpdateQuat_ p\g_q, p\g_r
		If Not p\Gv Then bOGL_UpdateGlobalPosition_ p
		bOGL_QuatFromEuler_ q, x, y, z
		p\g_q[0] = -p\g_q[0] : bOGL_QuatMul_ this\q, q, p\g_q : p\g_q[0] = -p\g_q[0]
	Else
		bOGL_QuatFromEuler_ this\q, x, y, z : this\Qv = True : this\Rv = False
	EndIf
	bOGL_InvalidateGlobalPosition_ this
End Function

Function TurnEntity(handler, x#, y#, z#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), turn#[3], res#[3] : bOGL_QuatFromEuler_ turn, x, y, z
	If Not this\Qv Then bOGL_UpdateQuat_ this\q, this\r : this\Qv = True
	bOGL_QuatMul_ res, this\q, turn
	this\q[0] = res[0] : this\q[1] = res[1] : this\q[2] = res[2] : this\q[3] = res[3]
	this\Rv = False : bOGL_InvalidateGlobalPosition_ this
End Function

Function PointEntity(handler, x#, y#, z#, roll# = 0.0)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	Local xd# = this\x - x, yd# = this\y - y, zd# = this\z - z
	Local pit# = ATan2(-yd, Sqr(xd * xd + zd * zd)), yaw# = ATan2(xd, zd)
	RotateEntity handler, pit, yaw, roll
End Function

Function ScaleEntity(handler, x#, y#, z#, absolute = False)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	If absolute Then x = x / this\g_sx : y = y / this\g_sy : z = z / this\g_sz
	this\sx = x : this\sy = y : this\sz = z : If this\children Then bOGL_InvalidateGlobalPosition_ this
End Function

Function PaintEntity(handler, red, green, blue)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	this\m\argb = (blue Or (green Shl 8) Or (red Shl 16) Or ($FF000000))
End Function

Function EntityAlpha(handler, alpha#)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : this\m\alpha = alpha
End Function

Function EntityFX(handler, flags)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : this\m\FX = flags
End Function

Function EntityTexture(handler, texture)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m
	If m\texture <> Null Then bOGL_ReleaseTexture_ m\texture
	m\texture = Object.bOGL_Tex texture
	If m\texture <> Null Then m\texture\rc = m\texture\rc + 1
End Function

Function ShowEntity(handler, state)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : this\hidden = (Not state)
End Function

Function EntityParent(handler, parentH)
	Local this.bOGL_Ent = bOGL_EntList_(handler), p.bOGL_Ent, c
	If this\parentH		;Remove from the list of current parent
		p = bOGL_EntList_(this\parentH) : For c = 0 To BankSize(p\children) - 4 Step 4
			If PeekInt(p\children, c) = handler
				CopyBank p\children, c + 4, p\children, c, BankSize(p\children) - (c + 4)
				ResizeBank p\children, BankSize(p\children) - 4 : Exit
			EndIf
		Next
	EndIf
	this\parentH = parentH
	If parentH
		p = bOGL_EntList_(parentH)
		If Not p\children Then p\children = CreateBank(4) : Else ResizeBank p\children, BankSize(p\children) + 4
		PokeInt p\children, BankSize(p\children) - 4, handler
	EndIf
End Function

Function GetParentEntity(handler)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : Return this\parentH
End Function

Function CountChildren(handler)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	If this\children Then Return BankSize(this\children) / 4 : Else Return 0
End Function

Function GetChildEntity(handler, index)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : Return PeekInt(this\children, index * 4)
End Function

Function RegisterEntityUserDataSlot()
	Local s = bOGL_UDScounter_ : bOGL_UDScounter_ = bOGL_UDScounter_ + 1 : Return s
End Function

Function SetEntityUserData(handler, slot, val)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	If Not this\userData
		CreateBank((slot + 1) * 4)
	ElseIf BankSize(this\userData) < ((slot + 1) * 4)
		ResizeBank this\userData, ((slot + 1) * 4)
	EndIf
	PokeInt this\userData, slot * 4, val
End Function

Function GetEntityUserData(handler, slot)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : Return PeekInt(this\userData, slot * 4)
End Function

Function CopyEntity(handler, parentH = 0)
	Local old.bOGL_Ent = bOGL_EntList_(handler), copy.bOGL_Ent
	
	Select old\eClass
		Case BOGL_CLASS_CAM
			copy = bOGL_EntList_(CreateCamera(parentH))
			copy\c\vpx = old\c\vpx : copy\c\vpy = old\c\vpy : copy\c\vpw = old\c\vpw : copy\c\vph = old\c\vph
			copy\c\viewangle = old\c\viewangle : copy\c\near = old\c\near : copy\c\far = old\c\far
			copy\c\fogmode = old\c\fogmode : copy\c\fognear = old\c\fognear : copy\c\fogfar = old\c\fogfar : copy\c\fogcolor = old\c\fogcolor
		Case BOGL_CLASS_LIGHT
			copy = bOGL_EntList_(CreateLight(0., 0., 0., old\l\flag, parentH))
			CopyBank old\l\pos, 0, copy\l\pos, 0, 16
			CopyBank old\l\col, 0, copy\l\col, 0, 16
			CopyBank old\l\att, 0, copy\l\att, 0, 4
		Case BOGL_CLASS_MESH
			copy = bOGL_EntList_(CreateMesh(parentH))
			copy\m\texture = old\m\texture : copy\m\texture\rc = copy\m\texture\rc + 1
			copy\m\argb = old\m\argb : copy\m\alpha = old\m\alpha
			ResizeBank copy\m\vp, BankSize(old\m\vp) : CopyBank old\m\vp, 0, copy\m\vp, 0, BankSize(old\m\vp)
			If old\m\vc Then copy\m\vc = CreateBank(BankSize(old\m\vc)) : CopyBank old\m\vc, 0, copy\m\vc, 0, BankSize(old\m\vc)
			ResizeBank copy\m\poly, BankSize(old\m\poly) : CopyBank old\m\poly, 0, copy\m\poly, 0, BankSize(old\m\poly)
		Default
			copy = bOGL_EntList_(CreatePivot(parentH))
	End Select
	
	If old\children
		Local c : For c = 0 To BankSize(old\children) - 4 Step 4
			CopyEntity PeekInt(old\children, c), copy\handler
		Next
	EndIf
	
	If old\userData
		copy\userData = CreateBank(BankSize(old\userData)) : CopyBank old\userData, 0, copy\userData, 0, BankSize(old\userData)
	EndIf
	
	Return copy\handler	;Note that we haven't copied the entity properties: scale, position, rotation are all default
End Function

Function FreeEntity(handler)
	Local this.bOGL_Ent = bOGL_EntList_(handler)
	Select this\eClass
		Case BOGL_CLASS_CAM
			If this\c\fogcolor Then FreeBank this\c\fogcolor
			Delete this\c
		Case BOGL_CLASS_LIGHT
			If this\l\pos Then FreeBank this\l\pos
			If this\l\col Then FreeBank this\l\col
			If this\l\att Then FreeBank this\l\att
			Delete this\l
		Case BOGL_CLASS_MESH
			If this\m\texture <> Null Then bOGL_ReleaseTexture_ this\m\texture
			If this\m\vp Then FreeBank this\m\vp
			If this\m\vc Then FreeBank this\m\vc
			If this\m\poly Then FreeBank this\m\poly
			Delete this\m
	End Select
	If this\children
		Local c : For c = 0 To BankSize(this\children) - 4 Step 4
			FreeEntity PeekInt(this\children, c)
		Next
		FreeBank this\children
	EndIf
	If this\userData Then FreeBank this\userData	;Note that this may leave data unreferenced!
	Delete this : bOGL_FreeHandler_ handler
End Function

Function FlipPolygons(handler)
	Local this.bOGL_Ent = bOGL_EntList_(handler), i, m.bOGL_Mesh = this\m
	For i = 0 To BankSize(m\poly) - BOGL_TRIS_STRIDE Step BOGL_TRIS_STRIDE
		Local a = PeekShort(m\poly, i + 2), b = PeekShort(m\poly, i + 4)
		PokeShort m\poly, i + 2, b : PokeShort m\poly, i + 4, a
	Next
End Function

Function RotateSubMesh(handler, vf, vt, rx#, ry#, rz#, cx#, cy#, cz#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m, v, p, q#[3], r#[3]
	bOGL_QuatFromEuler_ q, rx, ry, rz : bOGL_UpdateAxisAngle_ r, q
	For v = vf To vt
		p = v * BOGL_VERT_STRIDE
		Local x# = PeekFloat(m\vp, p + 20), y# = PeekFloat(m\vp, p + 24), z# = PeekFloat(m\vp, p + 28)
		Local vec#[2] : bOGL_RotateVector_ vec, x - cx, y - cy, z - cz, r
		PokeFloat m\vp, p + 20, cx + vec[0] : PokeFloat m\vp, p + 24, cy + vec[1] : PokeFloat m\vp, p + 28, cz + vec[2]
	Next
End Function

Function TranslateSubMesh(handler, vf, vt, tx#, ty#, tz#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m, v, ptr
	For v = vf To vt
		ptr = v * BOGL_VERT_STRIDE
		PokeFloat m\vp, ptr + 20, PeekFloat(m\vp, ptr + 20) + tx
		PokeFloat m\vp, ptr + 24, PeekFloat(m\vp, ptr + 24) + ty
		PokeFloat m\vp, ptr + 28, PeekFloat(m\vp, ptr + 28) + tz
	Next
End Function

Function ScaleSubMesh(handler, vf, vt, sx#, sy#, sz#, cx#, cy#, cz#)
	Local this.bOGL_Ent = bOGL_EntList_(handler), m.bOGL_Mesh = this\m, v, p
	For v = vf To vt
		p = v * BOGL_VERT_STRIDE
		Local x# = PeekFloat(m\vp, p + 20), y# = PeekFloat(m\vp, p + 24), z# = PeekFloat(m\vp, p + 28)
		PokeFloat m\vp, p + 20, cx + (x - cx) * sx
		PokeFloat m\vp, p + 24, cy + (y - cy) * sy
		PokeFloat m\vp, p + 28, cz + (z - cz) * sz
	Next
End Function

Function EntityX#(handler, absolute = False)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : If absolute
		If Not this\Gv Then bOGL_UpdateGlobalPosition_ this
		Return this\g_x
	EndIf
	Return this\x
End Function

Function EntityY#(handler, absolute = False)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : If absolute
		If Not this\Gv Then bOGL_UpdateGlobalPosition_ this
		Return this\g_x
	EndIf
	Return this\x
End Function

Function EntityZ#(handler, absolute = False)
	Local this.bOGL_Ent = bOGL_EntList_(handler) : If absolute
		If Not this\Gv Then bOGL_UpdateGlobalPosition_ this
		Return this\g_x
	EndIf
	Return this\x
End Function

Function EntityXAngle#(handler, absolute = False)
	Local e.bOGL_Ent = bOGL_EntList_(handler) : If Not e\Qv Then bOGL_UpdateQuat_ e\q, e\r : e\Qv = True
	If absolute
		If Not e\Gv Then bOGL_UpdateGlobalPosition_ e
		Return ATan2(2. * e\g_q[1] * e\g_q[0] - 2. * e\g_q[2] * e\g_q[3], 1. - 2. * e\g_q[1] * e\g_q[1] - 2. * e\g_q[3] * e\g_q[3])
	EndIf
	Return ATan2(2. * e\q[1] * e\q[0] - 2. * e\q[2] * e\q[3], 1. - 2. * e\q[1] * e\q[1] - 2. * e\q[3] * e\q[3])
End Function

Function EntityYAngle#(handler, absolute = False)
	Local e.bOGL_Ent = bOGL_EntList_(handler) : If Not e\Qv Then bOGL_UpdateQuat_ e\q, e\r : e\Qv = True
	If absolute
		If Not e\Gv Then bOGL_UpdateGlobalPosition_ e
		Return ATan2(2. * e\g_q[2] * e\g_q[0] - 2. * e\g_q[1] * e\g_q[3], 1. - 2. * e\g_q[2] * e\g_q[2] - 2. * e\g_q[3] * e\g_q[3])
	EndIf
	Return ATan2(2. * e\q[2] * e\q[0] - 2. * e\q[1] * e\q[3], 1. - 2. * e\q[2] * e\q[2] - 2. * e\q[3] * e\q[3])
End Function

Function EntityZAngle#(handler, absolute = False)
	Local e.bOGL_Ent = bOGL_EntList_(handler) : If Not e\Qv Then bOGL_UpdateQuat_ e\q, e\r : e\Qv = True
	If absolute
		If Not e\Gv Then bOGL_UpdateGlobalPosition_ e
		Return ASin(2. * e\g_q[1] * e\g_q[2] + 2. * e\g_q[3] * e\g_q[0])
	EndIf
	Return ASin(2. * e\q[1] * e\q[2] + 2. * e\q[3] * e\q[0])
End Function

Function CreateTexture(width, height, filter = 1)
	Local this.bOGL_Tex = New bOGL_Tex
	this\width = width : this\height = height
	this\rc = 1 : this\asVar = True
	If filter <> 0 And filter <> 1 Then this\filter = 2 : Else this\filter = filter
	
	Local pixels = CreateBank(this\width * this\height * 3)
	Local y : For y = 0 To this\height - 1
		Local offset = BankSize(pixels) - (y + 1) * this\width * 3
		Local x : For x = 0 To this\width - 1
			PokeShort pixels, offset, $FFFF
			PokeByte pixels, offset + 2, $FF
			offset = offset + 3
		Next
	Next
	
	glGenTextures 1, this	;This command sets this\glName
	glBindTexture GL_TEXTURE_2D, this\glName
	
	Select filter
		Case 0	; Nearest Filtered Texture
			glTexParameteri GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST
			glTexParameteri GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST
			glTexImage2D GL_TEXTURE_2D, 0, 3, this\width, this\height, 0, GL_RGB, GL_UNSIGNED_BYTE, pixels
		Case 1	; Linear Filtered Texture
			glTexParameteri GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR
			glTexParameteri GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR
			glTexImage2D GL_TEXTURE_2D, 0, 3, this\width, this\height, 0, GL_RGB, GL_UNSIGNED_BYTE, pixels
		Default	; MipMapped Texture
			glTexParameteri GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR
			glTexParameteri GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST
			gluBuild2DMipmaps GL_TEXTURE_2D, 3, this\width, this\height, GL_RGB, GL_UNSIGNED_BYTE, pixels
	End Select
	
	FreeBank pixels
	Return Handle this
End Function

Function LoadTexture(file$, quality = 8, filter = 1)
	Local image = LoadImage(file), width, height : If Not image Then Return False
	
	If quality
		width = 2 ^ quality : height = width : ResizeImage image, width, height
	Else
		width = ImageWidth(image) : height = ImageHeight(image)
	EndIf
	
	Local pixels = CreateBank(width * height * 4), buf = ImageBuffer(image), offset
	LockBuffer buf
	Local y : For y = 0 To ImageHeight(image) - 1
		Local x : For x = 0 To ImageWidth(image) - 1
			PokeInt pixels, offset, ReadPixelFast(x, y, buf)
			offset = offset + 4
		Next
	Next
	UnlockBuffer buf
	
	Local tex = CreateTexture(ImageWidth(image), ImageHeight(image), filter)
	UpdateTexture tex, 0, 0, ImageWidth(image), ImageHeight(image), pixels
	FreeBank pixels : Return tex
End Function

Function FreeTexture(handler)
	If handler
		Local this.bOGL_Tex = Object.bOGL_Tex handler
		If this\asVar Then this\asVar = False : bOGL_ReleaseTexture_ this
	EndIf
End Function

Function TextureWidth(handler)
	If handler
		Local this.bOGL_Tex = Object.bOGL_Tex handler
		Return this\width
	EndIf
End Function

Function TextureHeight(handler)
	If handler
		Local this.bOGL_Tex = Object.bOGL_Tex handler
		Return this\height
	EndIf
End Function

Function GetTextureData(handler)
	Local this.bOGL_Tex = Object.bOGL_Tex handler, pixels = CreateBank(this\width * this\height * 4)
	glBindTexture GL_TEXTURE_2D, this\glName
	glGetTexImage GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels
	Local p : For p = 0 To BankSize(pixels) - 4 Step 4	;Convert to BGRA format
		Local col = PeekInt(pixels, p) : PokeInt pixels, p, (col And $FF00FF00) Or ((col And $FF0000) Shr 16) Or ((col And $FF) Shl 16)
	Next
	Return pixels
End Function

Function UpdateTexture(handler, x, y, width, height, pixels)
	Local this.bOGL_Tex = Object.bOGL_Tex handler, temp = CreateBank(BankSize(pixels))
	CopyBank pixels, 0, temp, 0, BankSize(temp)
	Local p : For p = 0 To BankSize(temp) - 4 Step 4	;Convert to RGBA format
		Local col = PeekInt(temp, p) : PokeInt temp, p, (col And $FF00FF00) Or ((col And $FF0000) Shr 16) Or ((col And $FF) Shl 16)
	Next
	glBindTexture GL_TEXTURE_2D, this\glName
	glTexSubImage2D GL_TEXTURE_2D, 0, x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, temp
	FreeBank temp
End Function

Function RenderWorld(stencilMode = BOGL_STENCIL_OFF)
	Local x#, y#, z#, light
	
	glStencilMask 0 : glColorMask_Unsafe GL_TRUE,GL_TRUE, GL_TRUE, GL_TRUE	;Disable stencil writes, enable rendering
	glStencilFunc stencilMode, 0, $FF
	
	; Lights
	glEnable GL_LIGHTING
	If bOGL_AmbientLight_ Then glLightModelfv GL_LIGHT_MODEL_AMBIENT, bOGL_AmbientLight_
	Local lig.bOGL_Light : For lig = Each bOGL_Light
		If Not lig\ent\hidden
			light = light + 1 : If light > 7 Then Exit;light = 7
			bOGL_UpdateLight_ lig\ent
			glLightfv GL_LIGHT0 + light, lig\glFlag, lig\col
			glLightfv GL_LIGHT0 + light, GL_POSITION, lig\pos
			glLightfv GL_LIGHT0 + light, GL_LINEAR_ATTENUATION, lig\att
			glEnable GL_LIGHT0 + light
		EndIf
	Next
	
	Local cam.bOGL_Cam : For cam = Each bOGL_Cam
		If bOGL_EntityIsVisible_(cam\ent) And (cam\drawmode = BOGL_CAM_ORTHO Or cam\drawmode = BOGL_CAM_PERSPECTIVE)
			bOGL_InitializeRenderCamera cam		;Set up draw frustum/ortho area, camera position, rotation and scale, viewport and cls
			
			; Fog
			If cam\fogmode
				Select cam\fogmode
					Case 2
						glFogi GL_FOG_MODE, GL_EXP
					Case 3
						glFogi GL_FOG_MODE, GL_EXP2
					Default
						glFogi GL_FOG_MODE, GL_LINEAR
				End Select
				glFogfv GL_FOG_COLOR, cam\fogcolor
				glHint GL_FOG_HINT, GL_DONT_CARE
				glFogf GL_FOG_START, cam\fognear
				glFogf GL_FOG_END, cam\fogfar
				glEnable GL_FOG
			Else
				glDisable GL_FOG
			EndIf
			
			Local msh.bOGL_Mesh, ent.bOGL_Ent : For msh = Each bOGL_Mesh
				ent = msh\ent
				If bOGL_EntityIsVisible_(ent) And (msh\alpha >= BOGL_LIGHT_EPSILON)
					Local textured = msh\texture <> Null : If textured Then glEnable GL_TEXTURE_2D : Else glDisable GL_TEXTURE_2D
					
					glPushMatrix : bOGL_PushEntityTransform ent	;push entity position/rotation/scale
					
					; Bind texture and paint the entity
					If textured Then glBindTexture GL_TEXTURE_2D, msh\texture\glName
					glColor4f ((msh\argb Shr 16) And $FF) / 255.0, ((msh\argb Shr 8) And $FF) / 255.0, (msh\argb And $FF) / 255.0, msh\alpha
					
					; Blending and FX
					If msh\FX And BOGL_FX_FULLBRIGHT Then glDisable GL_LIGHTING : Else glEnable GL_LIGHTING
					If msh\FX And BOGL_FX_FLATSHADED Then glShadeModel GL_FLAT : Else glShadeModel GL_SMOOTH
					If msh\FX And BOGL_FX_NOFOG Then glDisable GL_FOG : ElseIf cam\fogmode Then glEnable GL_FOG
					If msh\FX And BOGL_FX_ADDBLEND
						glBlendFunc GL_ONE, GL_ONE : glEnable GL_BLEND
					ElseIf msh\FX And BOGL_FX_MULBLEND
						glBlendFunc GL_DST_COLOR, GL_ZERO : glEnable GL_BLEND
					Else
						glBlendFunc GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
						If msh\alpha < 1 Then glEnable GL_BLEND : Else glDisable GL_BLEND
					EndIf
					If msh\FX And BOGL_FX_NOCULL Then glDisable GL_CULL_FACE : Else glEnable GL_CULL_FACE
					
					glInterleavedArrays GL_T2F_N3F_V3F, BOGL_VERT_STRIDE, msh\vp
					If msh\vc Then glEnableClientState GL_COLOR_ARRAY : glColorPointer 3, GL_UNSIGNED_BYTE, 0, msh\vc
					glDrawElements GL_TRIANGLES, BankSize(msh\poly) / 2, GL_UNSIGNED_SHORT, msh\poly
					If msh\vc Then glDisableClientState GL_COLOR_ARRAY
					
					glPopMatrix
				EndIf
			Next
			glPopMatrix
		EndIf
	Next
	
	glFlush : glScissor 0, 0, bOGL_bbHwndW, bOGL_bbHwndH
	Local GLError = glGetError() : If GLError <> GL_NO_ERROR Then DebugLog "OpenGL Error:  " + GLError
End Function

Function RenderStencil()
	glStencilMask $FF : glColorMask_Unsafe 0, 0, 0, 0	;Enable stencil writes, disable rendering
	glStencilFunc GL_ALWAYS, 1, $FF
	
	Local cam.bOGL_Cam : For cam = Each bOGL_Cam
		If bOGL_EntityIsVisible_(cam\ent) And (cam\drawmode = BOGL_CAM_STENCIL)
			bOGL_InitializeRenderCamera cam		;Clear relevant buffers and push camera matrix and frustum
			glCullFace GL_BACK
			
			Local msh.bOGL_Mesh, ent.bOGL_Ent : For msh = Each bOGL_Mesh
				ent = msh\ent : If bOGL_EntityIsVisible_(ent, True); And (msh\FX And bogl_stencil
					glPushMatrix : bOGL_PushEntityTransform ent
					
					If (msh\FX And BOGL_FX_STENCIL_INCR) Or (msh\FX And BOGL_FX_STENCIL_BOTH)	;Apply stencil functions
						glStencilOp GL_KEEP, GL_KEEP, GL_INCR
					ElseIf msh\FX And BOGL_FX_STENCIL_DECR
						glStencilOp GL_KEEP, GL_KEEP, GL_DECR
					EndIf
					If msh\FX And BOGL_FX_NOCULL Then glDisable GL_CULL_FACE : Else glEnable GL_CULL_FACE
					
					glInterleavedArrays GL_T2F_N3F_V3F, BOGL_VERT_STRIDE, msh\vp
					glDrawElements GL_TRIANGLES, BankSize(msh\poly) / 2, GL_UNSIGNED_SHORT, msh\poly
					
					glPopMatrix
				EndIf
			Next
			
			glEnable GL_CULL_FACE : glCullFace GL_FRONT
			
			For msh = Each bOGL_Mesh	;Those meshes set to "both" run DECR on the backfaces
				ent = msh\ent : If bOGL_EntityIsVisible_(ent) And ((msh\FX And BOGL_FX_STENCIL_BOTH) > 0)
					glPushMatrix : bOGL_PushEntityTransform ent
					glStencilOp GL_KEEP, GL_KEEP, GL_DECR
					glInterleavedArrays GL_T2F_N3F_V3F, BOGL_VERT_STRIDE, msh\vp
					glDrawElements GL_TRIANGLES, BankSize(msh\poly) / 2, GL_UNSIGNED_SHORT, msh\poly
					glPopMatrix
				EndIf
			Next
			
			glPopMatrix		;Camera matrix
		EndIf
	Next
	
	glEnable GL_CULL_FACE : glCullFace GL_BACK
	glStencilOp GL_KEEP, GL_KEEP, GL_KEEP	;Reset this to not be affected later
	glFlush : glScissor 0, 0, bOGL_bbHwndW, bOGL_bbHwndH
	Local GLError = glGetError() : If GLError <> GL_NO_ERROR Then DebugLog "OpenGL Error:  " + GLError
End Function

Function Distance#(x1#, y1#, z1#, x2#, y2#, z2#)
	Return Sqr((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) + (z2-z1)*(z2-z1))
End Function

Function TFormPoint(x#, y#, z#, src, dst, out#[2])
	If src
		Local s.bOGL_Ent = bOGL_EntList_(src) : If Not s\Gv Then bOGL_UpdateGlobalPosition_ s
		If Not s\Rv Then bOGL_UpdateAxisAngle_ s\r, s\q : s\Rv = True
		bOGL_RotateVector_ out, x, y, z, s\r
		out[0] = s\g_x + out[0] * s\g_sx : out[1] = s\g_y + out[1] * s\g_sy : out[2] = s\g_z + out[2] * s\g_sz
	EndIf
	If dst
		Local d.bOGL_Ent = bOGL_EntList_(dst) : If Not d\Gv Then bOGL_UpdateGlobalPosition_ d
		If Not d\Rv Then bOGL_UpdateAxisAngle_ d\r, d\q : d\Rv = True
		x = (out[0] - d\g_x) / d\g_sx : y = (out[1] - d\g_y) / d\g_sy : z = (out[2] - d\g_z) / d\g_sz
		bOGL_RotateVector_ out, x, y, z, d\r
	EndIf
End Function


; Private functions and variables (internal use only)
;=====================================================

; Use an array to map integer handles to objects extremely quickly
Const BOGL_ENTLIST_MINSIZE_ = 1024
Dim bOGL_EntList_.bOGL_Ent(0), bOGL_EntCpList_.bOGL_Ent(0)
Global bOGL_EntCnt_, bOGL_EntOpen_, bOGL_EntMax_, bOGL_EntLSz_
Global bOGL_AmbientLight_, bOGL_UDScounter_

Function bOGL_Init_(width, height)
	If Not bOGL_bbHwnd Then RuntimeError "Couldn't get the handle of the window!"
	bOGL_bbHwndW = width : bOGL_bbHwndH = height
	
	BlitzGL_Init
	bOGL_EntOpen_ = 1 : bOGL_EntLSz_ = BOGL_ENTLIST_MINSIZE_
	Dim bOGL_EntList_.bOGL_Ent(bOGL_EntLSz_)
	bOGL_UDScounter_ = 0
	
	Local pf.ogld_PixelFormat = ogld_MakeDefaultPixelFormat()
	bOGL_hMainDC = ogld_SetUp_OpenGL(bOGL_bbHwnd, pf)
	If Not bOGL_hMainDC Then RuntimeError "Could not initialize OpenGl!"
	
	glEnable GL_TEXTURE_2D
	glEnable GL_DEPTH_TEST
	glEnable GL_SCISSOR_TEST
	glEnable GL_STENCIL_TEST
	glShadeModel GL_SMOOTH
	glHint GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST
	glEnable GL_CULL_FACE
	glCullFace GL_BACK
	glColorMaterial GL_FRONT, GL_AMBIENT_AND_DIFFUSE	;So lights don't erase colours
	glEnable GL_COLOR_MATERIAL
End Function

Function bOGL_EntHandler_(this.bOGL_Ent)
	If bOGL_EntCnt_ = bOGL_EntLSz_	;Out of slots, allocate more space in the index by doubling it
		Dim bOGL_EntCpList_.bOGL_Ent(bOGL_EntCnt_)
		Local e : For e = 1 To bOGL_EntCnt_
			bOGL_EntCpList_(e) = bOGL_EntList_(e)	;Copy entities to temporary array
		Next
		bOGL_EntLSz_ = bOGL_EntLSz_ * 2
		Dim bOGL_EntList_.bOGL_Ent(bOGL_EntLSz_)		;Resize main list
		For e = 1 To bOGL_EntCnt_
			bOGL_EntList_(e) = bOGL_EntCpList_(e)	;Copy them back
		Next
		Dim bOGL_EntCpList_.bOGL_Ent(0)	;Get rid of the temporary array
	EndIf
	Local h = bOGL_EntOpen_ : bOGL_EntList_(h) = this		;Set the waiting open slot
	bOGL_EntCnt_ = bOGL_EntCnt_ + 1
	If bOGL_EntOpen_ > bOGL_EntMax_ Then bOGL_EntMax_ = bOGL_EntOpen_
	For bOGL_EntOpen_ = bOGL_EntOpen_ To bOGL_EntLSz_
		If bOGL_EntList_(bOGL_EntOpen_) = Null Then Exit	;Find the next open slot
	Next
	Return h
End Function

Function bOGL_FreeHandler_(handler)
	bOGL_EntList_(handler) = Null
	bOGL_EntCnt_ = bOGL_EntCnt_ - 1
	If handler = bOGL_EntMax_		;Last entity
		For bOGL_EntMax_ = bOGL_EntMax_ To 1 Step -1	;Find the new last entity
			If bOGL_EntList_(bOGL_EntMax_) <> Null Then Exit
		Next
		If bOGL_EntLSz_ > BOGL_ENTLIST_MINSIZE_
			While bOGL_EntMax_ < bOGL_EntLSz_ / (100 / 20)		;If the *range* of remaining entities is below the shrink threshold
				bOGL_EntLSz_ = bOGL_EntLSz_ / 2
				Dim bOGL_EntCpList_.bOGL_Ent(bOGL_EntLSz_)
				Local e : For e = 1 To bOGL_EntLSz_
					bOGL_EntCpList_(e) = bOGL_EntList_(e)
				Next
				Dim bOGL_EntList_.bOGL_Ent(bOGL_EntLSz_)
				For e = 1 To bOGL_EntLSz_
					bOGL_EntList_(e) = bOGL_EntCpList_(e)
				Next
				Dim bOGL_EntCpList_.bOGL_Ent(0)
			Wend
		EndIf
	EndIf
	If handler < bOGL_EntOpen_ Then bOGL_EntOpen_ = handler
End Function

Function bOGL_NewEnt_.bOGL_Ent(eClass, hdl, parentH)	;New base entity
	Local ent.bOGL_Ent = New bOGL_Ent
	ent\handler = bOGL_EntHandler_(ent)
	EntityParent ent\handler, parentH
	ent\eClass = eClass
	Select eClass
		Case BOGL_CLASS_CAM : ent\c = Object.bOGL_Cam hdl
		Case BOGL_CLASS_LIGHT : ent\l = Object.bOGL_Light hdl
		Case BOGL_CLASS_MESH : ent\m = Object.bOGL_Mesh hdl
	End Select
	RotateEntity ent\handler, 0., 0., 0.	;Zero the starting quat
	ent\sx = 1. : ent\sy = 1. : ent\sz = 1.
	ent\Gv = False
	Return ent
End Function

Function bOGL_ReleaseTexture_(this.bOGL_Tex)	;When there are no more references to a texture, delete it
	this\rc = this\rc - 1
	If this\rc < 1 Then glDeleteTextures 1, this : Delete this
End Function

Function bOGL_UpdateQuat_(q#[3], r#[3])	;Convert normalised axis-angle r[3] to normalised quaternion q[3]
	q[0] = Cos(r[0] / 2.)		;w
	q[1] = r[1] * Sin(r[0] / 2.) : q[2] = r[2] * Sin(r[0] / 2.) : q[3] = r[3] * Sin(r[0] / 2.)
End Function

Function bOGL_UpdateAxisAngle_(r#[3], q#[3])	;Convert normalised quaternion q[3] to normalised axis-angle r[3]
	If Abs q[0] > 1. Then bOGL_NormaliseQuat_ q
	r[0] = 2 * ACos(q[0])
	Local s# = Sqr(1. - q[0] * q[0])
	If s >= BOGL_EPSILON
		r[1] = q[1] / s : r[2] = q[2] / s : r[3] = q[3] / s
	Else
		r[1] = 1. : r[2] = 0. : r[3] = 0.
	EndIf
End Function

Function bOGL_QuatFromEuler_(q#[3], x#, y#, z#)
	Local c1# = Cos(y / 2.), c2# = Cos(z / 2.), c3# = Cos(x / 2.)
    Local s1# = Sin(y / 2.), s2# = Sin(z / 2.), s3# = Sin(x / 2.)
	q[0] = c1 * c2 * c3 - s1 * s2 * s3
	q[1] = s1 * s2 * c3 + c1 * c2 * s3
	q[2] = s1 * c2 * c3 + c1 * s2 * s3
	q[3] = c1 * s2 * c3 - s1 * c2 * s3
End Function

Function bOGL_QuatMul_(outq#[3], q1#[3], q2#[3])
	outq[0] = q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2] - q1[3] * q2[3]
	outq[1] = q1[0] * q2[1] + q1[1] * q2[0] + q1[2] * q2[3] - q1[3] * q2[2]
	outq[2] = q1[0] * q2[2] - q1[1] * q2[3] + q1[2] * q2[0] + q1[3] * q2[1]
	outq[3] = q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1] + q1[3] * q2[0]
End Function

Function bOGL_NormaliseQuat_(q#[3])
	Local l# = Sqr(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
	q[0] = q[0] / l : q[1] = q[1] / l : q[2] = q[2] / l : q[3] = q[3] / l
End Function

Function bOGL_RotateVector_(out#[2], x#, y#, z#, r#[3])	;Rotate a vector x,y,z by normalised axis-angle r (Rodrigues' rotation)
	Local cth# = Cos(r[0]), sth# = Sin(r[0])	; vrot = v cos(theta) + (k cross v) sin(theta) + k(k dot v)(1 - cos(theta))
	Local kdv# = (r[1] * x + r[2] * y + r[3] * z) * (1. - cth)	;(k dot v)(1 - cos(theta))
	out[0] = cth * x + sth * (r[2] * z - r[3] * y) + r[1] * kdv
	out[1] = cth * y + sth * (r[3] * x - r[1] * z) + r[2] * kdv
	out[2] = cth * z + sth * (r[1] * y - r[2] * x) + r[3] * kdv
End Function

Function bOGL_UpdateGlobalPosition_(ent.bOGL_Ent)
	If ent\parentH
		Local par.bOGL_Ent = bOGL_EntList_(ent\parentH), pos#[2] : TFormPoint ent\x * par\sx, ent\y * par\sy, ent\z * par\sz, par\handler, 0, pos
		ent\g_x = pos[0] : ent\g_y = pos[1] : ent\g_z = pos[2] : ent\g_sx = par\sx : ent\g_sy = par\sy : ent\g_sz = par\sz
		If Not ent\Qv Then bOGL_UpdateQuat_ ent\q, ent\r : ent\Qv = True
		If Not par\Qv Then bOGL_UpdateQuat_ par\q, par\r : par\Qv = True
		Local q#[3] : bOGL_QuatMul_ q, par\g_q, ent\q : ent\g_q[0] = q[0] : ent\g_q[1] = q[1] : ent\g_q[2] = q[2] : ent\g_q[3] = q[3]
		ent\g_Qv = True : ent\g_Rv = False
	Else
		ent\g_x = ent\x : ent\g_y = ent\y : ent\g_z = ent\z : ent\g_sx = 1.0 : ent\g_sy = 1.0 : ent\g_sz = 1.0
		ent\g_r[0] = ent\r[0] : ent\g_r[1] = ent\r[1] : ent\g_r[2] = ent\r[2] : ent\g_r[3] = ent\r[3] : ent\g_Rv = ent\Rv
		ent\g_q[0] = ent\q[0] : ent\g_q[1] = ent\q[1] : ent\g_q[2] = ent\q[2] : ent\g_q[3] = ent\q[3] : ent\g_Qv = ent\Qv
	EndIf
	ent\Gv = True
End Function

Function bOGL_UpdateLocalPosition(ent.bOGL_Ent)
	Local pos#[2] : TFormPoint ent\g_x, ent\g_y, ent\g_z, 0, ent\parentH, pos
End Function

Function bOGL_InvalidateGlobalPosition_(ent.bOGL_Ent)
	If ent\Gv
		ent\Gv = False : If ent\children
			Local lst = BankSize(ent\children) - 4, c : For c = 0 To lst Step 4
				bOGL_InvalidateGlobalPosition_ bOGL_EntList_(PeekInt(ent\children, c))
			Next
		EndIf
	EndIf
End Function

Function bOGL_EntityIsVisible_(ent.bOGL_Ent, stencilTest = False)
	If ent\hidden Then Return False
	If stencilTest
		If Not ((ent\m\FX And BOGL_FX_STENCIL_INCR) Or (ent\m\FX And BOGL_FX_STENCIL_DECR) Or (ent\m\FX And BOGL_FX_STENCIL_BOTH)) Then Return False
	EndIf
	While ent\parentH
		ent = bOGL_EntList_(ent\parentH) : If ent\hidden Then Return False
	Wend
	Return True
End Function

Function bOGL_UpdateLight_(this.bOGL_Ent)
	If Not this\Gv Then bOGL_UpdateGlobalPosition_ this
	If this\l\pos
		If this\l\flag <> BOGL_LIGHT_DIR
			PokeFloat this\l\pos, 0, this\g_x	;Need to update the passable position of point sources
			PokeFloat this\l\pos, 4, this\g_y
			PokeFloat this\l\pos, 8, this\g_z
		Else
			Local dir#[2] : TFormPoint 0, 0, -1, this\handler, 0, dir	;Need to update the orientation of dir sources
			PokeFloat this\l\pos, 0, this\g_x - dir[0]
			PokeFloat this\l\pos, 4, this\g_y - dir[1]
			PokeFloat this\l\pos, 8, this\g_z - dir[2]
		EndIf
	EndIf
End Function

Function bOGL_InitializeRenderCamera(cam.bOGL_Cam)	;Identical code shared by RenderWorld and RenderStencil
	glScissor cam\vpx, cam\vpy, cam\vpw, cam\vph
	glClearColor ((cam\clscol And $FF0000) Shr 16) / 255., ((cam\clscol And $FF00) Shr 8) / 255., (cam\clscol And $FF) / 255., 1.
	glClear cam\clsmode
	
	glMatrixMode GL_PROJECTION
	glLoadIdentity()
	If cam\drawmode = BOGL_CAM_ORTHO
		Local oCamY# = bOGL_bbHwndH * cam\viewangle / bOGL_bbHwndW : glOrtho -cam\viewangle, cam\viewangle, oCamY, -oCamY, cam\near, cam\far
	Else
		gluPerspective cam\viewangle, Float cam\vpw / cam\vph, cam\near, cam\far
	EndIf
	glMatrixMode GL_MODELVIEW
	glViewport cam\vpx, cam\vpy, cam\vpw, cam\vph
	
	; Rotate camera
	glPushMatrix()
	If Not cam\ent\Gv Then bOGL_UpdateGlobalPosition_ cam\ent
	If Not cam\ent\g_Rv Then bOGL_UpdateAxisAngle_ cam\ent\g_r, cam\ent\g_q : cam\ent\g_Rv = True
	glRotatef cam\ent\g_r[0], cam\ent\g_r[1], cam\ent\g_r[2], cam\ent\g_r[3]
	glTranslatef -cam\ent\g_x, -cam\ent\g_y, -cam\ent\g_z
	glScalef cam\ent\g_sx * cam\ent\sx, cam\ent\g_sy * cam\ent\sy, cam\ent\g_sz * cam\ent\sz
End Function

Function bOGL_PushEntityTransform(ent.bOGL_Ent)		;Push a mesh's position, rotation and scale for rendering (shared)
	If Not ent\Gv Then bOGL_UpdateGlobalPosition_ ent
	glTranslatef ent\g_x, ent\g_y, ent\g_z
	If Not ent\g_Rv Then bOGL_UpdateAxisAngle_ ent\g_r, ent\g_q : ent\g_Rv = True
	glRotatef ent\g_r[0], ent\g_r[1], ent\g_r[2], ent\g_r[3]
	glScalef ent\sx * ent\g_sx, ent\sy * ent\g_sy, ent\sz * ent\g_sz
End Function


;~IDEal Editor Parameters:
;~F#53#5C#63#68#6E#77#7E#85#8C#9A#B3#B8#BD#C8#D4#DE#E3#E8#ED#FB
;~F#100#105#10A#10F#114#13E#14A#164#169#16E#173#177#17C#184#18D#193#199#1A1#1AF#1B7
;~F#1BE#1C4#1C9#1CD#1D1#1D8#1DC#1EE#1F2#1F7#1FB#1FF#209#20D#233#24F#257#262#26C#277
;~F#27F#287#28F#298#2A1#2AA#2D0#2E8#2EF#2F6#2FD#307#312#366#397#39B#3B4#3CD#3E3#3FC
;~F#40C#411#416#421#42A#431#436#43E#44E#452#45C#467#477#48F
;~C#BlitzPlus