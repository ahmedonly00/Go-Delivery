package com.goDelivery.goDelivery.config;

import com.goDelivery.goDelivery.model.City;
import com.goDelivery.goDelivery.model.Country;
import com.goDelivery.goDelivery.repository.CityRepository;
import com.goDelivery.goDelivery.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class LocationDataInitializer implements CommandLineRunner {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @Override
    public void run(String... args) throws Exception {
        if (countryRepository.count() == 0) {
            log.info("Initializing location data...");
            initializeLocationData();
            log.info("Location data initialization completed");
        } else {
            log.info("Location data already exists, skipping initialization");
        }
    }

    private void initializeLocationData() {
        // Rwanda
        Country rwanda = Country.builder()
                .countryName("Rwanda")
                .countryCode("RWA")
                .build();
        rwanda = countryRepository.save(rwanda);

        createCity("Kigali", rwanda);
        createCity("Butare", rwanda);
        createCity("Gitarama", rwanda);
        createCity("Ruhengeri", rwanda);
        createCity("Gisenyi", rwanda);
        createCity("Byumba", rwanda);
        createCity("Cyangugu", rwanda);
        createCity("Kibungo", rwanda);
        createCity("Kibuye", rwanda);
        createCity("Rwamagana", rwanda);

        // Mozambique
        Country mozambique = Country.builder()
                .countryName("Mozambique")
                .countryCode("MOZ")
                .build();
        mozambique = countryRepository.save(mozambique);

        createCity("Maputo", mozambique);
        createCity("Matola", mozambique);
        createCity("Beira", mozambique);
        createCity("Nampula", mozambique);
        createCity("Chimoio", mozambique);
        createCity("Nacala", mozambique);
        createCity("Quelimane", mozambique);
        createCity("Tete", mozambique);
        createCity("Xai-Xai", mozambique);
        createCity("Pemba", mozambique);
        createCity("Inhambane", mozambique);
        createCity("Lichinga", mozambique);

        log.info("Created {} countries and {} cities", 
                countryRepository.count(), cityRepository.count());
    }

    private void createCity(String cityName, Country country) {
        City city = City.builder()
                .cityName(cityName)
                .country(country)
                .build();
        cityRepository.save(city);
    }
}
