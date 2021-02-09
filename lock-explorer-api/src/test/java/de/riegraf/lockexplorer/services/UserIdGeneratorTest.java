package de.riegraf.lockexplorer.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
class UserIdGeneratorTest {

  @Autowired
  private UserIdGenerator idGenerator;

  @Test
  void lengthTest() {
    assertThat(idGenerator.generateId().length()).isEqualTo(idGenerator.ID_LENGTH);
  }

  @Test
  void idIsDifferentTest() {
    final String idA = idGenerator.generateId();
    final String idB = idGenerator.generateId();
    final String idC = idGenerator.generateId();
    assertThat(idA).isNotEqualTo(idB);
    assertThat(idB).isNotEqualTo(idC);
    assertThat(idA).isNotEqualTo(idC);
  }

  @Test
  void idStartsWithLetterTest() {
    for (int i = 0; i < 1000; i++) {
      assertThat(Character.isDigit(idGenerator.generateId().charAt(0))).isFalse();
    }
  }
}