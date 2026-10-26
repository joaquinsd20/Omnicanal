export interface Importacion {
  idImportacion?: number;
  idUsuario?: number;
  numeroFactura: string;
  fechaFactura: Date;
  proveedorInternacional: string;
  paisOrigen?: string;
  montoOriginal: number;
  monedaOriginal?: string;
  tipoCambio?: number;
  montoEnSoles?: number;
  costoFlete?: number;
  costoAduanas?: number;
  costoAranceles?: number;
  costoTransporteLocal?: number;
  otrosCostos?: number;
  costoTotal?: number;
  cantidadItems: number;
  pesoTotalKg?: number;
  volumenTotalM3?: number;
  documentoRuta?: string;
  estadoProcesamiento?: string;
  estadoImportacion?: string;
  createdAt?: Date;
  updatedAt?: Date;
}
