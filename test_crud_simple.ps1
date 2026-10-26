$ApiBase = "http://localhost:8080/api"

Write-Host "=====================================================`nPRUEBAS CRUD - Omnicanal Backend`n=====================================================" -ForegroundColor Cyan

# TEST 1: LOGIN
Write-Host "`n[TEST 1] LOGIN" -ForegroundColor Yellow
$loginPayload = @{email="juan@omnicanal.pe"; password="password123"} | ConvertTo-Json

try {
    $loginResp = Invoke-WebRequest -Uri "$ApiBase/auth/login" -Method POST -ContentType "application/json" -Body $loginPayload -UseBasicParsing -ErrorAction Stop
    $loginData = $loginResp.Content | ConvertFrom-Json
    $token = $loginData.token
    $idUsuario = $loginData.idUsuario
    $headers = @{"Authorization"="Bearer $token"; "X-User-Id"=$idUsuario.ToString(); "Content-Type"="application/json"}
    Write-Host "OK: Usuario ID = $idUsuario" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# TEST 2: GET PRODUCTOS EXISTENTES
Write-Host "`n[TEST 2] GET /api/productos" -ForegroundColor Yellow
try {
    $getResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
    $productos = $getResp.Content | ConvertFrom-Json
    $count = @($productos).Count
    Write-Host "OK: Productos = $count" -ForegroundColor Green
    if ($count -gt 0) {
        $productos | ForEach-Object { Write-Host "  - ID $($_.idProducto): $($_.nombreProducto) (Stock: $($_.stockTotal))" }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

# TEST 3: CREATE PRODUCTO
Write-Host "`n[TEST 3] POST /api/productos (CREATE)" -ForegroundColor Yellow
$sku = "SKU_$(Get-Random -Minimum 1000 -Maximum 9999)"
$nuevoProducto = @{
    nombreProducto="Producto Test $(Get-Random)"
    sku=$sku
    precioCosto=100
    precioVenta=150
    stockTotal=50
    descripcion="Test"
    categoria="Test"
} | ConvertTo-Json

try {
    $createResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method POST -Headers $headers -Body $nuevoProducto -UseBasicParsing -ErrorAction Stop
    $productoCreado = $createResp.Content | ConvertFrom-Json
    $idProductoCreado = $productoCreado.idProducto
    Write-Host "OK: Producto creado ID=$idProductoCreado, Nombre=$($productoCreado.nombreProducto)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    $idProductoCreado = $null
}

# TEST 4: GET POR ID
if ($idProductoCreado) {
    Write-Host "`n[TEST 4] GET /api/productos/{id}" -ForegroundColor Yellow
    try {
        $getByIdResp = Invoke-WebRequest -Uri "$ApiBase/productos/$idProductoCreado" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
        $prod = $getByIdResp.Content | ConvertFrom-Json
        Write-Host "OK: $($prod.nombreProducto) | Precio: $($prod.precioCosto)-$($prod.precioVenta) | Stock: $($prod.stockTotal)" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    # TEST 5: UPDATE PRODUCTO
    Write-Host "`n[TEST 5] PUT /api/productos/{id} (UPDATE)" -ForegroundColor Yellow
    $productoActual = @{
        nombreProducto="Producto Actualizado"
        sku=$sku
        precioCosto=120
        precioVenta=180
        stockTotal=75
        descripcion="Actualizado"
        categoria="Test"
    } | ConvertTo-Json

    try {
        $updateResp = Invoke-WebRequest -Uri "$ApiBase/productos/$idProductoCreado" -Method PUT -Headers $headers -Body $productoActual -UseBasicParsing -ErrorAction Stop
        $prodActual = $updateResp.Content | ConvertFrom-Json
        Write-Host "OK: Stock cambió a $($prodActual.stockTotal)" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    # TEST 6: VERIFICAR EN BD
    Write-Host "`n[TEST 6] Verificar cambios en BD" -ForegroundColor Yellow
    try {
        $finalResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
        $productosFinal = $finalResp.Content | ConvertFrom-Json
        Write-Host "OK: Total productos en BD = $((@($productosFinal).Count))" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    # TEST 7: DELETE
    Write-Host "`n[TEST 7] DELETE /api/productos/{id}" -ForegroundColor Yellow
    try {
        $deleteResp = Invoke-WebRequest -Uri "$ApiBase/productos/$idProductoCreado" -Method DELETE -Headers $headers -UseBasicParsing -ErrorAction Stop
        Write-Host "OK: Producto eliminado" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    # TEST 8: VERIFICAR ELIMINACION
    Write-Host "`n[TEST 8] Verificar eliminación en BD" -ForegroundColor Yellow
    try {
        $afterResp = Invoke-WebRequest -Uri "$ApiBase/productos" -Method GET -Headers $headers -UseBasicParsing -ErrorAction Stop
        $productosAfter = $afterResp.Content | ConvertFrom-Json
        Write-Host "OK: Total productos después DELETE = $((@($productosAfter).Count))" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=====================================================" -ForegroundColor Green
Write-Host "PRUEBAS COMPLETADAS" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green
