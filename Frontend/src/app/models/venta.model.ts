export interface Venta {
  idVenta?: number;
  idUsuario?: number;
  idCanal?: number;
  numeroVenta: string;
  fechaVenta?: Date;
  nombreCliente: string;
  emailCliente?: string;
  telefonoCliente?: string;
  provinciaDestino?: string;
  distritoDestino?: string;
  direccionEntrega?: string;
  subtotal: number;
  costoEnvio?: number;
  descuento?: number;
  total: number;
  estadoVenta?: string;
  estadoPago?: string;
  agenciaEnvio?: string;
  numeroTracking?: string;
  createdAt?: Date;
  updatedAt?: Date;
}
