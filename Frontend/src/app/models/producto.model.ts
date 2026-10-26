export interface Producto {
  idProducto?: number;
  idUsuario?: number;
  nombreProducto: string;
  sku: string;
  descripcion?: string;
  categoria?: string;
  precioCosto: number;
  precioVenta: number;
  margenGananciaPct?: number;
  stockTotal: number;
  stockMinimo?: number;
  pesoKg?: number;
  volumenM3?: number;
  imagenUrl?: string;
  proveedor?: string;
  paisOrigen?: string;
  estado?: string;
  createdAt?: Date;
  updatedAt?: Date;
}
