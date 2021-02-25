package de.riegraf.lockexplorer.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserData {
  private final String userId;
  private final String password;
  private LocalDateTime lastAction = LocalDateTime.now();
  final Map<Integer, Connection> connections = new HashMap<>(2);
}