# Script completo de pruebas CRUD para el Backend
# Ejecutar: powershell -ExecutionPolicy Bypass -File d:\Proyecto\test_crud.ps1

$ApiBase = "http://localhost:8080/api"

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "PRUEBAS COMPLETAS DE CRUD - Omnicanal Backend" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

# ==================== TEST 1: LOGIN ====================
Write-Host "`n[TEST 1] LOGIN" -ForegroundColor Yellow
$loginPayload = @{
    email = "juan@omnicanal.pe"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResp = Invoke-WebRequest -Uri "$ApiBase/auth/login" -Method POST -ContentType "application/json" -Body $loginPayload -UseBasicParsing -ErrorAction Stop
    $loginData = $loginResp.Content | ConvertFrom-Json
    $token = $loginData.token
    $idUsuario = $loginData.idUsuario
    $headers = @{
        "Authorization" = "Bearer $token"
        "X-User-Id" = $idUsuario.ToString()
        "Content-Type" = "application/json"
    }
    Write-Host "✅ Login exitoso: Usuario ID = $idUsuario" -ForegroundColor Green
} catch {
    Write-Host "❌ Login falló: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ==================== TEST 2: GET TODOS LOS PRODUCTOS ====================
Write-Host "`n[TEST 2] GET /api/productos (Listar todos)" -ForegroundColor Yellow
try {
    $getResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
    $productos = $getResp.Content | ConvertFrom-Json
    $count = @($productos).Count
    Write-Host "✅ Productos existentes: $count" -ForegroundColor Green
    if ($count -gt 0) {
        $productos | ForEach-Object { Write-Host "   - ID $($_.idProducto): $($_.nombreProducto) | SKU: $($_.sku) | Stock: $($_.stockTotal)" }
    }
} catch {
    Write-Host "❌ GET productos falló: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================== TEST 3: CREATE PRODUCTO ====================
Write-Host "`n[TEST 3] POST /api/productos (Crear nuevo)" -ForegroundColor Yellow
$nuevoProducto = @{
    nombreProducto = "TEST_PRODUCTO_$(Get-Random)"
    sku = "TEST_SKU_$(Get-Random)"
    precioCosto = 100.00
    precioVenta = 150.00
    stockTotal = 50
    descripcion = "Producto de prueba creado en CRUD testing"
    categoria = "Test"
} | ConvertTo-Json

try {
    $createResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method POST -Headers $headers -Body $nuevoProducto -UseBasicParsing -ErrorAction Stop
    $productoCreado = $createResp.Content | ConvertFrom-Json
    $idProductoCreado = $productoCreado.idProducto
    Write-Host "✅ Producto creado exitosamente" -ForegroundColor Green
    Write-Host "   - ID: $idProductoCreado | Nombre: $($productoCreado.nombreProducto) | SKU: $($productoCreado.sku)" -ForegroundColor Green
} catch {
    Write-Host "❌ CREATE producto falló: $($_.Exception.Message)" -ForegroundColor Red
    $idProductoCreado = $null
}

# ==================== TEST 4: GET PRODUCTO POR ID ====================
if ($idProductoCreado) {
    Write-Host "`n[TEST 4] GET /api/productos/{id} (Obtener específico)" -ForegroundColor Yellow
    try {
        $getByIdResp = Invoke-WebRequest -Uri "$ApiBase/productos/$idProductoCreado" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
        $productoObtened = $getByIdResp.Content | ConvertFrom-Json
        Write-Host "✅ Producto obtenido por ID" -ForegroundColor Green
        Write-Host "   - Nombre: $($productoObtened.nombreProducto) | Precio Costo: $($productoObtened.precioCosto) | Stock: $($productoObtened.stockTotal)" -ForegroundColor Green
    } catch {
        Write-Host "❌ GET producto por ID falló: $($_.Exception.Message)" -ForegroundColor Red
    }

    # ==================== TEST 5: UPDATE PRODUCTO ====================
    Write-Host "`n[TEST 5] PUT /api/productos/{id} (Actualizar)" -ForegroundColor Yellow
    $productoActualizado = @{
        nombreProducto = "PRODUCTO_ACTUALIZADO_$(Get-Random)"
        sku = $productoCreado.sku
        precioCosto = 120.00
        precioVenta = 180.00
        stockTotal = 75
        descripcion = "Producto actualizado en CRUD testing"
        categoria = "Test Updated"
    } | ConvertTo-Json

    try {
        $updateResp = Invoke-WebRequest -Uri "$ApiBase/productos/$idProductoCreado" -Method PUT -Headers $headers -Body $productoActualizado -UseBasicParsing -ErrorAction Stop
        $productoPut = $updateResp.Content | ConvertFrom-Json
        Write-Host "✅ Producto actualizado exitosamente" -ForegroundColor Green
        Write-Host "   - Nuevo Nombre: $($productoPut.nombreProducto) | Nuevo Stock: $($productoPut.stockTotal)" -ForegroundColor Green
    } catch {
        Write-Host "❌ UPDATE producto falló: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# ==================== TEST 6: VERIFICAR CAMBIOS EN BD ====================
Write-Host "`n[TEST 6] Verificar cambios en BD (GET nuevamente)" -ForegroundColor Yellow
try {
    $finalResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
    $productosFinales = $finalResp.Content | ConvertFrom-Json
    $countFinal = @($productosFinales).Count
    Write-Host "✅ Total de productos ahora: $countFinal" -ForegroundColor Green
} catch {
    Write-Host "❌ Verificación final falló: $($_.Exception.Message)" -ForegroundColor Red
}

# ==================== TEST 7: DELETE PRODUCTO ====================
if ($idProductoCreado) {
    Write-Host "`n[TEST 7] DELETE /api/productos/{id} (Eliminar)" -ForegroundColor Yellow
    try {
        $deleteResp = Invoke-WebRequest -Uri "$ApiBase/productos/$idProductoCreado" -Method DELETE -Headers $headers -UseBasicParsing -ErrorAction Stop
        Write-Host "✅ Producto eliminado exitosamente" -ForegroundColor Green
    } catch {
        Write-Host "❌ DELETE producto falló: $($_.Exception.Message)" -ForegroundColor Red
    }

    # Verificar que se eliminó
    Write-Host "`n[VERIFICACIÓN] GET productos después de DELETE" -ForegroundColor Yellow
    try {
        $afterDeleteResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
        $productosAfter = $afterDeleteResp.Content | ConvertFrom-Json
        $countAfter = @($productosAfter).Count
        Write-Host "✅ Total de productos después de DELETE: $countAfter" -ForegroundColor Green
    } catch {
        Write-Host "❌ Verificación POST-DELETE falló: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=====================================================================" -ForegroundColor Cyan
Write-Host "✅ PRUEBAS COMPLETADAS" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
