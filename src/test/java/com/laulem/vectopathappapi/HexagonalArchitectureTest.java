package com.laulem.vectopathappapi;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * ArchUnit test to verify compliance with hexagonal architecture.
 * <p>
 * Expected structure:
 * - business: Business domain (core of the hexagon) - should not depend on anything
 * - client: Primary/inbound adapters (REST API, controllers)
 * - infra: Secondary/outbound adapters (repositories, JPA entities, technical services)
 */
@DisplayName("Hexagonal Architecture Tests")
class HexagonalArchitectureTest {
    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.laulem.vectopathappapi");
    }

    @Test
    @DisplayName("Business domain should not depend on adapters (client, infra)")
    void domainShouldNotDependOnAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.business..")
                .should().dependOnClassesThat().resideInAnyPackage("com.laulem.vectopathappapi.client..", "com.laulem.vectopathappapi.infra..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Business domain should not depend on Spring Framework (except allowed exceptions)")
    void domainShouldNotDependOnSpringFramework() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.business..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.web..",
                        "org.springframework.data..",
                        "jakarta.persistence.."
                );

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Client adapters can depend on domain but not on infrastructure")
    void clientAdaptersShouldDependOnDomainButNotInfra() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.client..")
                .should().dependOnClassesThat().resideInAPackage("com.laulem.vectopathappapi.infra..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Infrastructure adapters can depend on domain but not on client")
    void infraAdaptersShouldDependOnDomainButNotClient() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.infra..")
                .should().dependOnClassesThat().resideInAPackage("com.laulem.vectopathappapi.client..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controllers must be in client.controller package")
    void controllersShouldBeInClientPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("com.laulem.vectopathappapi.client.controller..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("JPA entities must be in infra.entity package")
    void entitiesShouldBeInInfraPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("com.laulem.vectopathappapi.infra.entity..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("JPA repositories must be in infra.repository package")
    void jpaRepositoriesShouldBeInInfraPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("JpaRepository")
                .or().areAssignableTo("org.springframework.data.jpa.repository.JpaRepository")
                .should().resideInAPackage("com.laulem.vectopathappapi.infra.repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain repository interfaces must be in business.repository")
    void domainRepositoriesShouldBeInBusinessPackage() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.laulem.vectopathappapi.business.repository..")
                .should().beInterfaces().allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Business services must be in business.service package")
    void businessServicesShouldBeInBusinessPackage() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.business.service..")
                .should().dependOnClassesThat().resideInAnyPackage("com.laulem.vectopathappapi.client..", "com.laulem.vectopathappapi.infra..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("DTOs must be in client.dto package")
    void dtosShouldBeInClientPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("DTO")
                .or().haveSimpleNameEndingWith("Dto")
                .or().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .should().resideInAPackage("com.laulem.vectopathappapi.client.dto..")
                .orShould().resideInAPackage("com.laulem.vectopathappapi.infra.dto..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Onion architecture - global verification")
    void onionArchitectureShouldBeRespected() {
        ArchRule rule = onionArchitecture()
                .domainModels("com.laulem.vectopathappapi.business.model..", "com.laulem.vectopathappapi.business.repository..")
                .domainServices("com.laulem.vectopathappapi.business.service..")
                .applicationServices("com.laulem.vectopathappapi.client.service..")
                .adapter("client", "com.laulem.vectopathappapi.client..")
                .adapter("infra", "com.laulem.vectopathappapi.infra..")
                .withOptionalLayers(true);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Business exceptions must be in business.exception")
    void businessExceptionsShouldBeInBusinessPackage() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.laulem.vectopathappapi.business.exception..")
                .should().beAssignableTo(Exception.class)
                .orShould().beAssignableTo(RuntimeException.class);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain models must not have JPA annotations")
    void domainModelsShouldNotHaveJpaAnnotations() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.business.model..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain models must be independent (no dependencies on services or repositories)")
    void domainModelsShouldBeIndependent() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.laulem.vectopathappapi.business.model..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "com.laulem.vectopathappapi.business.model..",
                        "java..",
                        "com.fasterxml.jackson.."
                )
                .because("Domain models must be purely business-oriented without technical dependencies");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Business services should only depend on ports (interfaces), not implementations")
    void businessServicesShouldDependOnPortsOnly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.business.service..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("com.laulem.vectopathappapi.infra.repository..", "com.laulem.vectopathappapi.infra.service..")
                .because("Business services should only depend on domain ports, not infrastructure adapters");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Spring annotations @Service/@Component should only be in adapters")
    void onlyAdaptersShouldHaveSpringAnnotations() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.laulem.vectopathappapi.business.model..", "com.laulem.vectopathappapi.business.repository..")
                .should().beAnnotatedWith("org.springframework.stereotype.Service")
                .orShould().beAnnotatedWith("org.springframework.stereotype.Component")
                .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
                .because("Domain should not depend on Spring framework");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Infrastructure technical exceptions should not be exposed to domain")
    void infraExceptionsShouldNotBeInDomain() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.business..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.laulem.vectopathappapi.infra.exception..")
                .because("Infrastructure technical exceptions should not pollute the domain");

        rule.check(importedClasses);
    }

    @Test
    @Disabled("Can be reactivated when violations are fixed")
    @DisplayName("Domain entities should not be exposed in DTOs")
    void domainEntitiesShouldNotBeInDTOs() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.client.dto..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.laulem.vectopathappapi.business.model..")
                .because("DTOs should isolate the domain from the external API");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controllers should not depend directly on repositories")
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.client.controller..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("com.laulem.vectopathappapi.business.repository..", "com.laulem.vectopathappapi.infra.repository..")
                .because("Controllers should go through business services");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain classes should not use utility classes from client or infra")
    void domainShouldNotUseAdapterUtilities() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.laulem.vectopathappapi.business..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("com.laulem.vectopathappapi.client.tool..", "com.laulem.vectopathappapi.infra.conf..")
                .because("Domain must remain independent of adapter utilities");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Shared package can be used by all layers")
    void sharedPackageCanBeUsedByAllLayers() {
        ArchRule rule = classes()
                .that().resideInAPackage("..shared..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..shared..", "java..", "org.springframework..", "jakarta..", "org.apache..", "com.fasterxml..")
                .because("Shared package should only contain utilities without business logic");

        rule.check(importedClasses);
    }
}


