
 bOGL 2 beta
=============

**About:** bOGL is an OpenGL based open source 3D engine for BlitzBasic (Tested on BlitzPlus). bOGL also works with Blitz3D, if you remove the 3D canvas functions.

While the command set is loosely based on that of Blitz3D, they are not compatible, nor are they intended to be. Several commands work differently by design.

bOGL is intended to allow for a modular addon structure. Modules are provided for [2D drawing](bOGL-Addons/Draw2D.bb), [MD2 animation](bOGL-Addons/MD2.bb), [loading and saving static meshes](bOGL-Addons/MeshLoader.bb), and [loading and animating skinned meshes](bOGL-Addons/Animation.bb). Others may follow. Understanding a module is not required to understand the operation of the bOGL engine core.

For more information on the core engine and addon modules, see [the Docs folder](Docs).

**Installation:** Move the folder named "userlibs" to BlitzBasic folder. Overwrite if neccessary, or add the content of the files to the existing ones. Now you should be able to run the demo and create your own projects.

Please overwrite any userlib files remaining from bOGL version 1.01.

**Licence:** MIT, see [LICENSE file](LICENSE)

**Credits:**  
bOGL 1 by Andres Pajo  
bOGL 2 by Alex Gilding  
MD2, Draw2D, Animation and MeshLoader modules by Alex Gilding  
OGL Direct by Peter Scheutz  


 Command reference
-------------------

    Graphics3D(title$, width, height, depth, mode), EndGraphics3D()  
    CreateCanvas3D(x, y, width, height, group), FreeCanvas3D(canvas)  
    AmbientLight(red, green, blue)  
    CreateCamera([parent])  
    CameraRange(handler, near#, far#)  
    CameraFieldOfView(handler, angle#)  
    CameraDrawMode(handler, mode), CameraClsMode(handler, mode)  
    CameraClsColor(handler, red, green, blue)  
    CameraFogMode(handler, mode[, near#, far#])  
    CameraFogColor(handler, red, green, blue[, alpha#])  
    CameraViewport(handler, x, y, width, height)  
    CreateLight(red, green, blue, flag[, parent])  
    LightRange(handler, range#)  
    CreatePivot([parent])  
    CreateMesh([parent])  
    AddVertex(mesh, x#, y#, z#[, u#, v#]), AddTriangle(mesh, v0, v1, v2)  
    CountTriangles(mesh), CountVertices(mesh), TriangleVertex(mesh, tri, vert)  
    VertexCoords(mesh, v, x#, y#, z#), VertexTexCoords(mesh, v, u#, v#)  
    VertexNormal(mesh, v, nx#, ny#, nz#), VertexColor(mesh, v, r#, g#, b#)  
    VertexX#(mesh, v), VertexY#(mesh, v), VertexZ#(mesh, v), VertexU#(mesh, v), VertexV#(mesh, v)  
    CreateCube([parent])  
    CreateSprite([parent])  
    LoadTerrain(terrain$[, parent])  
    PositionEntity(handler, x#, y#, z#[, absolute])  
    MoveEntity(handler, x#, y#, z#)  
    RotateEntity(handler, x#, y#, z#[, absolute])  
    TurnEntity(handler, x#, y#, z#)  
    PointEntity(handler, x#, y#, z#[, roll#])  
    ScaleEntity(handler, x#, y#, z#[, absolute])  
    PaintEntity(handler, red, green, blue)  
    EntityAlpha(handler, alpha#), EntityFX(handler, flags)  
    EntityTexture(handler, texture)  
    ShowEntity(handler, state)  
    SetEntityParent(handler, parentH), GetEntityParent(handler)  
    CountChildren(handler), GetChildEntity(handler, index), GetChildByName(handler, name$)  
    SetEntityName(handler, name$), GetEntityName$(handler)  
    RegisterEntityUserDataSlot(), SetEntityUserData(handler, slot, val), GetEntityUserData(handler, slot)  
    CopyEntity(handler)  
    FreeEntity(handler)  
    FlipPolygons(handler)  
    RotateSubMesh(handler, vf, vt, rx#, ry#, rz#, cx#, cy#, cz#), QuatRotateSubMesh(handler, vf, vt, q#[3], cx#, cy#, cz#)  
    TranslateSubMesh(handler, vf, vt, tx#, ty#, tz#)  
    ScaleSubMesh(handler, vf, vt, sx#, sy#, sz#, cx#, cy#, cz#)  
    EntityX#(handler[, absolute]), EntityY#(handler[, absolute]), EntityZ#(handler[, absolute])  
    EntityXAngle#(handler[, absolute]), EntityYAngle#(handler[, absolute]), EntityZAngle#(handler[, absolute])  
    CreateTexture(width, height[, filter])  
    LoadTexture(file$[, quality, filter])  
    FreeTexture(handler)  
    TextureWidth(handler), TextureHeight(handler)  
    GetTextureData(handler[, doConvert]), UpdateTexture(handler, x, y, width, height, pixels[, doConvert])  
    GrabBackBuffer(x, y, width, height, pix[, doConvert])  
    RenderWorld([stencilMode]), RenderStencil()      
    Distance(x1#, y1#, z1#, x2#, y2#, z2#)  
    TFormPoint(x#, y#, z#, src, dst, out#[2])  

