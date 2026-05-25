package com.joao.adotec.config.bootstrap;

import com.joao.adotec.enums.AppRole;
import com.joao.adotec.enums.PetSize;
import com.joao.adotec.models.Pet;
import com.joao.adotec.models.PetPhoto;
import com.joao.adotec.models.Role;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.PetRepository;
import com.joao.adotec.repositories.RoleRepository;
import com.joao.adotec.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Componente responsável pelo bootstrap de dados essenciais para o ambiente de desenvolvimento.
 * ATENÇÃO: Esta classe deve ser executada APENAS quando o profile "dev" estiver ativo.
 * Isso resolve o BUG-01 e evita a criação de credenciais conhecidas em ambiente de produção.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando DevDataSeeder - Configuração de dados de desenvolvimento...");

        // 1. Create Roles if they don't exist
        if (roleRepository.count() == 0) {
            log.info("Criando Roles no banco de dados...");
            roleRepository.save(new Role(AppRole.ROLE_ADMIN));
            roleRepository.save(new Role(AppRole.ROLE_EMPLOYEE));
            roleRepository.save(new Role(AppRole.ROLE_ADOPTER));
        }

        // 2. Fetch Roles
        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: ADMIN Role is not found."));
        Role employeeRole = roleRepository.findByRoleName(AppRole.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Error: EMPLOYEE Role is not found."));
        Role adopterRole = roleRepository.findByRoleName(AppRole.ROLE_ADOPTER)
                .orElseThrow(() -> new RuntimeException("Error: ADOPTER Role is not found."));

        // 3. Create Admin User
        if (!userRepository.existsByEmail("admin@adotec.com")) {
            log.info("Criando usuário admin padrão...");
            User admin = new User("Admin User", "admin@adotec.com", passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(adminRole, employeeRole, adopterRole));
            userRepository.save(admin);
        }

        // 4. Create Employee User
        if (!userRepository.existsByEmail("employee@adotec.com")) {
            log.info("Criando usuário employee padrão...");
            User employee = new User("Employee User", "employee@adotec.com", passwordEncoder.encode("employee123"));
            employee.setRoles(Set.of(employeeRole, adopterRole));
            userRepository.save(employee);
        }

        // 5. Create Adopter User
        if (!userRepository.existsByEmail("adopter@adotec.com")) {
            log.info("Criando usuário adopter padrão...");
            User adopter = new User("Adopter User", "adopter@adotec.com", passwordEncoder.encode("adopter123"));
            adopter.setRoles(Set.of(adopterRole));
            userRepository.save(adopter);
        }

        // 6. Seed Pets (only if none exist yet — idempotent)
        seedPets();

        log.info("DevDataSeeder finalizado com sucesso.");
    }

    // ───────────────────────────────────────────────────────────
    //  Pet seeding
    // ───────────────────────────────────────────────────────────

    private void seedPets() {
        if (petRepository.count() > 0) {
            log.info("Pets já existem no banco — pulando seed de pets.");
            return;
        }
        log.info("Criando pets de demonstração...");

        // ── Cachorros ───────────────────────────────────────────
        createPet("Rex", "Cachorro", PetSize.BIG, 36,
                "Rex é um vira-lata grandalhão, protetor e carinhoso. Adora passeios longos e brincadeiras ao ar livre. Vacinado e castrado.",
                "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=600&h=600&fit=crop");

        createPet("Luna", "Cachorro", PetSize.MEDIUM, 18,
                "Luna é uma cachorrinha dócil e muito afetuosa. Se dá bem com crianças e outros animais. Vacinada.",
                "https://images.unsplash.com/photo-1596492784531-6e6eb5ea9993?w=600&h=600&fit=crop");

        createPet("Thor", "Cachorro", PetSize.BIG, 24,
                "Thor é brincalhão e cheio de energia. Precisa de espaço para correr. Ideal para famílias ativas.",
                "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600&h=600&fit=crop");

        createPet("Mel", "Cachorro", PetSize.SMALL, 8,
                "Mel é uma filhotinha meiga e sapeca. Perfeita para apartamento. Está em dia com as vacinas.",
                "https://images.unsplash.com/photo-1560807707-8cc77767d783?w=600&h=600&fit=crop");

        createPet("Bob", "Cachorro", PetSize.MEDIUM, 48,
                "Bob é um cão calmo e companheiro, ótimo para idosos. Muito leal ao dono. Castrado e vacinado.",
                "https://images.unsplash.com/photo-1530281700549-e82e7bf110d6?w=600&h=600&fit=crop");

        createPet("Pipoca", "Cachorro", PetSize.SMALL, 12,
                "Pipoca é agitada e adora brincar de buscar bolinha. Pequena, mas cheia de personalidade!",
                "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600&h=600&fit=crop");

        // ── Gatos ───────────────────────────────────────────────
        createPet("Mimi", "Gato", PetSize.SMALL, 6,
                "Mimi é uma gatinha tímida que precisa de um lar tranquilo. Muito carinhosa quando ganha confiança.",
                "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=600&h=600&fit=crop");

        createPet("Simba", "Gato", PetSize.MEDIUM, 30,
                "Simba é um gato independente e brincalhão. Adora escalar e observar o mundo pela janela.",
                "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=600&h=600&fit=crop");

        createPet("Nina", "Gato", PetSize.SMALL, 4,
                "Nina é uma filhote curiosa e cheia de energia. Se dá bem com outros gatos.",
                "https://images.unsplash.com/photo-1606214174585-fe31582dc6ee?w=600&h=600&fit=crop");

        createPet("Felix", "Gato", PetSize.MEDIUM, 60,
                "Felix é um gato sênior muito tranquilo e afetuoso. Perfeito para quem busca companhia calma.",
                "https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=600&h=600&fit=crop");

        // ── Outros ──────────────────────────────────────────────
        createPet("Cenoura", "Coelho", PetSize.SMALL, 14,
                "Cenoura é um coelho dócil e muito fofo. Ideal para famílias com crianças. Adora ser acariciado.",
                "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600&h=600&fit=crop");

        createPet("Bolt", "Cachorro", PetSize.BIG, 20,
                "Bolt é um cão atlético e inteligente. Aprende comandos rapidamente. Precisa de um dono experiente.",
                "https://images.unsplash.com/photo-1568572933382-74d440642117?w=600&h=600&fit=crop");

        log.info("{} pets criados com sucesso.", petRepository.count());
    }

    private void createPet(String name, String species, PetSize size, int ageInMonths,
                           String description, String photoUrl) {
        Pet pet = new Pet();
        pet.setPetName(name);
        pet.setSpecies(species);
        pet.setSize(size);
        pet.setAgeInMonths(ageInMonths);
        pet.setDescription(description);
        pet.setIsAvailableForAdoption(true);
        pet.setIsActive(true);

        // Add a primary photo using a public URL (no Cloudinary needed for seed data)
        PetPhoto photo = new PetPhoto(pet, photoUrl, "seed/" + name.toLowerCase(), true);
        pet.getPhotos().add(photo);

        petRepository.save(pet);
    }
}
