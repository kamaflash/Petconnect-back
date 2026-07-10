package com.petconnect.home.application;

import com.petconnect.home.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HomeService {

    public List<Story> getStories() {
        return List.of(
                new Story(UUID.randomUUID(), UUID.randomUUID(), false),
                new Story(UUID.randomUUID(), UUID.randomUUID(), false),
                new Story(UUID.randomUUID(), UUID.randomUUID(), true),
                new Story(UUID.randomUUID(), UUID.randomUUID(), false),
                new Story(UUID.randomUUID(), UUID.randomUUID(), false),
                new Story(UUID.randomUUID(), UUID.randomUUID(), true));
    }

    public List<Suggestion> getSuggestions() {
        return List.of(
                new Suggestion(UUID.randomUUID(), "laura_vet", "https://i.pravatar.cc/150?img=8", 1200),
                new Suggestion(UUID.randomUUID(), "carlos_pets", "https://i.pravatar.cc/150?img=9", 856),
                new Suggestion(UUID.randomUUID(), "ana_adopta", "https://i.pravatar.cc/150?img=10", 2300),
                new Suggestion(UUID.randomUUID(), "pedro_mascota", "https://i.pravatar.cc/150?img=11", 567));
    }

    public List<MarketplacePromo> getMarketPromos() {
        return List.of(
                new MarketplacePromo(UUID.randomUUID(), "marketplace", "Alimento Premium",
                        "30% descuento en alimentos premium para perros",
                        "https://images.unsplash.com/photo-1589924691195-41432c84c161?w=150&h=150&fit=crop",
                        "/marketplace", "Oferta"),
                new MarketplacePromo(UUID.randomUUID(), "marketplace", "Juguete Interactivo",
                        "Nuevo juguete que mantendrá a tu mascota entretenida",
                        "https://images.unsplash.com/photo-1537151608828-ea2b11777ee1?w=150&h=150&fit=crop",
                        "/marketplace", "Nuevo"));
    }

    public List<VetService> getVetServices() {
        return List.of(
                new VetService(UUID.randomUUID(), "Consulta General", "Revisión completa de salud", "pi pi-user-md"),
                new VetService(UUID.randomUUID(), "Vacunación", "Esquema de vacunación completo", "pi pi-syringe"),
                new VetService(UUID.randomUUID(), "Peluquería", "Baño y corte de pelo", "pi pi-scissors"));
    }

    public List<Adoption> getNewAdoptions() {
        return List.of(
                new Adoption(UUID.randomUUID(), "Rocky", "Labrador Retriever",
                        "https://images.unsplash.com/photo-1587300003388-59608b8a3669?w=150&h=150&fit=crop"),
                new Adoption(UUID.randomUUID(), "Luna", "Gato Persa",
                        "https://images.unsplash.com/photo-1573865526739-59b3a0c4a4c4?w=150&h=150&fit=crop"));
    }

    public List<Achievement> getUpcomingAchievements() {
        return List.of(
                new Achievement(UUID.randomUUID(), "Explorador Social", "Sigue a 10 usuarios",
                        "pi pi-users", 7, 10, false),
                new Achievement(UUID.randomUUID(), "Amigo de los Animales", "Comenta en 50 publicaciones",
                        "pi pi-comment", 32, 50, false),
                new Achievement(UUID.randomUUID(), "Coleccionista", "Guarda 25 publicaciones",
                        "pi pi-bookmark", 25, 25, true));
    }

    public UserLevel getUserLevel() {
        return new UserLevel(12, 650, 1000);
    }
}