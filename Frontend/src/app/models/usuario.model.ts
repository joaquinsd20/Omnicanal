export type TipoUsuario = 'EMPRENDEDOR' | 'ADMINISTRADOR' | 'ADMINISTRADOR_GLOBAL' | 'SUPERVISOR';
export type PlanSuscripcion = 'PLAN_EMPRENDEDOR' | 'PLAN_PRO';
export type EstadoUsuario = 'ACTIVO' | 'INACTIVO' | 'SUSPENDIDO' | 'BLOQUEADO';

export interface Usuario {
  idUsuario?: number;
  nombreUsuario: string;
  email: string;
  password?: string;
  telefono?: string;
  razonSocial?: string;
  ruc?: string;
  pais?: string;
  ciudad?: string;
  direccion?: string;
  tipoUsuario?: TipoUsuario;
  planSuscripcion?: PlanSuscripcion;
  estado?: EstadoUsuario;
  idUsuarioCreador?: number;
  usuarioCreador?: Usuario;
  usuariosCreadosPorMi?: Usuario[];
  createdAt?: Date;
  updatedAt?: Date;
}
