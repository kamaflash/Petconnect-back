package com.petconnect.adoptions.domain;

/**
 * Motivo por el cual una mascota cambió de propietario.
 * Se utiliza para mantener el historial de propietarios de un animal.
 */
public enum OwnershipChangeReason {
    ADOPTION_COMPLETED,  // Entrega al nuevo dueño al finalizar una adopción
    RETURNED,            // La mascota fue devuelta al refugio/propietario anterior
    TRANSFERRED,         // Transferencia entre usuarios
    OTHER                // Otro motivo
}