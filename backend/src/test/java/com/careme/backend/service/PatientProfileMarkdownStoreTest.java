package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.entity.PatientProfile;
import com.careme.backend.entity.PatientProfileUpdate;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PatientProfileMarkdownStoreTest {

    @TempDir
    Path profileDirectory;

    @Test
    void readsAnAbsentProfileAsEmpty() throws Exception {
        PatientProfile profile = store().read();

        assertThat(profile).isEqualTo(PatientProfile.empty());
    }

    @Test
    void roundTripsConfirmedAndAbsentFields() throws Exception {
        PatientProfile profile = new PatientProfile(
                " Ana Pérez ", LocalDate.of(1984, 2, 3), null, "Médica", "O+", "positivo", new BigDecimal("170.5"));

        store().write(profile);

        assertThat(store().read()).isEqualTo(new PatientProfile(
                "Ana Pérez", LocalDate.of(1984, 2, 3), null, "Médica", "O+", "positivo", new BigDecimal("170.5")));
        assertThat(profileDirectory.resolve("profile.md")).exists();
        assertThat(profileDirectory.resolve("events")).doesNotExist();
    }

    @Test
    void appliesOnlyConfirmedValuesAndKeepsPreviousValuesForMissingCandidates() {
        PatientProfile current = new PatientProfile(
                "Ana Pérez", LocalDate.of(1984, 2, 3), "mujer", "Médica", "O", "positivo", new BigDecimal("170"));
        PatientProfileUpdate update = new PatientProfileUpdate(
                "Ana López", null, null, null, null, null, new BigDecimal("171"), true);

        assertThat(current.apply(update)).isEqualTo(new PatientProfile(
                "Ana López", LocalDate.of(1984, 2, 3), "mujer", "Médica", "O", "positivo", new BigDecimal("171")));
        assertThat(current.apply(new PatientProfileUpdate(null, null, null, null, null, null, null, false)))
                .isEqualTo(current);
    }

    @Test
    void derivesAgeWithoutStoringIt() {
        PatientProfile profile = new PatientProfile(
                null, LocalDate.of(1984, 2, 3), null, null, null, null, null);

        assertThat(profile.ageOn(LocalDate.of(2026, 2, 2))).isEqualTo(41);
        assertThat(PatientProfileMarkdownStore.serialize(profile)).doesNotContain("age");
    }

        @Test
        void keepsThePreviousSnapshotWhenPublicationFails() throws Exception {
                PatientProfile previous = new PatientProfile(
                                "Ana", LocalDate.of(1984, 2, 3), null, null, null, null, null);
                store().write(previous);
                PatientProfileMarkdownStore failingStore = new PatientProfileMarkdownStore(profileDirectory) {
                        @Override
                        void move(Path source, Path target, java.nio.file.StandardCopyOption... options) throws java.io.IOException {
                                throw new java.io.IOException("publication failed");
                        }
                };

                org.assertj.core.api.Assertions.assertThatThrownBy(() -> failingStore.write(
                                                new PatientProfile("Bea", null, null, null, null, null, null)))
                                .isInstanceOf(java.io.IOException.class);

                assertThat(store().read()).isEqualTo(previous);
        }

    @Test
    void replacesTheWholeSnapshotWhenThePublicationSucceeds() throws Exception {
        store().write(new PatientProfile("Ana", null, null, null, null, null, null));

        store().write(new PatientProfile("Bea", LocalDate.of(1990, 1, 1), null, null, null, null, null));

        assertThat(store().read().name()).isEqualTo("Bea");
        assertThat(store().read()).isEqualTo(new PatientProfile(
                "Bea", LocalDate.of(1990, 1, 1), null, null, null, null, null));
    }

    @Test
    void fallsBackToANonAtomicMoveWhenTheFilesystemRefusesAnAtomicOne() throws Exception {
        PatientProfileMarkdownStore fallbackStore = new PatientProfileMarkdownStore(profileDirectory) {
            private boolean atomic = true;

            @Override
            void move(Path source, Path target, java.nio.file.StandardCopyOption... options)
                    throws java.io.IOException {
                if (atomic) {
                    atomic = false;
                    throw new java.nio.file.FileSystemException("atomic move not supported");
                }
                super.move(source, target, options);
            }
        };

        fallbackStore.write(new PatientProfile("Ana", null, null, null, null, null, null));

        assertThat(store().read().name()).isEqualTo("Ana");
    }

    @Test
    void refusesADocumentWithoutValidFrontMatter() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> PatientProfileMarkdownStore.deserialize("# Perfil del paciente\n"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readsUnquotedValuesAndTreatsBlankOnesAsAbsent() throws Exception {
        java.nio.file.Files.createDirectories(profileDirectory);
        java.nio.file.Files.writeString(profileDirectory.resolve("profile.md"),
                "---\nname: Ana\ndate_of_birth: \nsex: mujer\noccupation: \nblood_group: O\nrh: \nheight_cm: 170\n"
                        + "---\n\n# Perfil del paciente\n");

        PatientProfile profile = store().read();

        assertThat(profile.name()).isEqualTo("Ana");
        assertThat(profile.dateOfBirth()).isNull();
        assertThat(profile.sex()).isEqualTo("mujer");
        assertThat(profile.bloodGroup()).isEqualTo("O");
        assertThat(profile.rh()).isNull();
        assertThat(profile.heightCm()).isEqualByComparingTo("170");
    }

    @Test
    void roundTripsValuesThatContainQuotesAndBackslashes() throws Exception {
        PatientProfile profile = new PatientProfile(
                "Ana \"la\" Pérez\\", null, null, null, null, null, null);

        store().write(profile);

        assertThat(store().read().name()).isEqualTo("Ana \"la\" Pérez\\");
    }

    private PatientProfileMarkdownStore store() {
        return new PatientProfileMarkdownStore(profileDirectory);
    }
}