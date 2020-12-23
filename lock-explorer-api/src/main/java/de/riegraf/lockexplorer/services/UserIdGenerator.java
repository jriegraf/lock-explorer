package de.riegraf.lockexplorer.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UserIdGenerator {

  final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  final int ID_LENGTH = 5;
  private final SecureRandom rnd = new SecureRandom();

  public String generateId() {
    return generateId(ID_LENGTH);
  }

  private String generateId(int len) {
    StringBuilder sb = new StringBuilder(len);
    sb.append(ALPHABET.charAt(10 + rnd.nextInt(ALPHABET.length() - 10)));
    for (int i = 1; i < len; i++)
      sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
    return sb.toString();
  }
}
