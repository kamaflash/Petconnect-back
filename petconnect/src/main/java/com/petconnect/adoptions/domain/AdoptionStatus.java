package com.petconnect.adoptions.domain;

public enum AdoptionStatus {
    PENDING,            // Pendiente de aceptación (inicial)
    PENDING_CONTACT,    // En contacto
    PENDING_DELIVERY,   // Pendiente de entrega
    COMPLETED,          // Entregado / finalizado
    APPROVED,           // legado (antes de introducir las etapas)
    REJECTED            // Cancelada / rechazada
}